package tensorflow.tsuru;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.Status;
import io.grpc.internal.LogExceptionRunnable;
import io.grpc.internal.SharedResourceHolder;
import io.grpc.internal.SharedResourceHolder.Resource;

/**
 * A Tsuru-based {@link NameResolver}.
 *
 * @see io.grpc.internal.DnsNameResolver
 */
public class TsuruNameResolver extends NameResolver {

	public static class Tsuru {

		private final String apiUrl;
		private final String token;

		public static Tsuru fromEnv() {
			String apiUrl = System.getenv("TSURU_HOST");
			String token = System.getenv("TSURU_USER_TOKEN");
			return new Tsuru(apiUrl, token);
		}
		
		Tsuru(String apiUrl, String token) {
			this.apiUrl = apiUrl;
			this.token = token;
		}

		@SuppressWarnings("unchecked")
		public InetSocketAddress[] listUnitAddress(String appName) throws Exception {
			URL u = new URL(apiUrl + "apps/" + appName);
			HttpURLConnection cx = (HttpURLConnection) u.openConnection();
			cx.setRequestProperty("Authorization", "Bearer " + token);
			cx.setConnectTimeout(5000);
			cx.setReadTimeout(5000);

			List<InetSocketAddress> addresses = new ArrayList<>();

			try (InputStream stream = cx.getInputStream();
					InputStreamReader inReader = new InputStreamReader(stream);
					BufferedReader reader = new BufferedReader(inReader)) {
				Gson g = new Gson();
				Map<String, Object> resp = g.fromJson(reader, Map.class);
				List<Map<String, Object>> units = (List<Map<String, Object>>) resp.get("units");
				for (Map<String, Object> unit : units) {
					String status = (String) unit.get("Status");
					if (!status.equals("started"))
						continue;
					Map<String, Object> address = (Map<String, Object>) unit.get("Address");
					String _address = (String) address.get("Host");
					int i = _address.indexOf(':');
					String host = _address.substring(0, i);
					int port = Integer.valueOf(_address.substring(i + 1));
					InetSocketAddress addr = new InetSocketAddress(host, port);
					try (Socket s = new Socket()) {
						s.connect(addr, 100);
						addresses.add(addr);
					} catch (Exception e) {
						// skip unreachable address
					}
				}
			}

			return addresses.toArray(new InetSocketAddress[addresses.size()]);
		}

	}

	private final String appName;
	private final Resource<ScheduledExecutorService> timerServiceResource;
	private final Resource<ExecutorService> executorResource;

	private boolean shutdown;

	private ScheduledExecutorService timerService;

	private ExecutorService executor;

	private ScheduledFuture<?> resolutionTask;

	private boolean resolving;

	private Listener listener;

	public TsuruNameResolver(URI targetUri, Attributes params, Resource<ScheduledExecutorService> timerServiceResource,
			Resource<ExecutorService> executorResource) {
		super();
		appName = targetUri.getAuthority();
		this.timerServiceResource = timerServiceResource;
		this.executorResource = executorResource;
	}

	@Override
	public String getServiceAuthority() {
		return appName;
	}

	@Override
	public final synchronized void start(Listener listener) {
		Preconditions.checkState(this.listener == null, "already started");
		timerService = SharedResourceHolder.get(timerServiceResource);
		executor = SharedResourceHolder.get(executorResource);
		this.listener = Preconditions.checkNotNull(listener, "listener");
		resolve();
	}

	@Override
	public final synchronized void refresh() {
		Preconditions.checkState(listener != null, "not started");
		resolve();
	}

	@Override
	public final synchronized void shutdown() {
		if (shutdown) {
			return;
		}
		shutdown = true;
		if (resolutionTask != null) {
			resolutionTask.cancel(false);
		}
		if (timerService != null) {
			timerService = SharedResourceHolder.release(timerServiceResource, timerService);
		}
		if (executor != null) {
			executor = SharedResourceHolder.release(executorResource, executor);
		}
	}

	private void resolve() {
		if (resolving || shutdown) {
			return;
		}
		executor.execute(resolutionRunnable);
	}

	InetSocketAddress[] listUnits() throws Exception {
		System.out.println("************");
		System.out.println("Loading units...");
		Tsuru tsuru = Tsuru.fromEnv();
		InetSocketAddress[] units = tsuru.listUnitAddress(appName);
		System.out.println("Units: " + units.length);
		for (InetSocketAddress unit : units)
			System.out.println(unit);
		System.out.println();
		return units;
	}

	private final Runnable resolutionRunnable = new Runnable() {
		@Override
		public void run() {
			InetSocketAddress[] inetAddrs;
			Listener savedListener;
			synchronized (TsuruNameResolver.this) {
				// If this task is started by refresh(), there might already be
				// a scheduled task.
				if (resolutionTask != null) {
					resolutionTask.cancel(false);
					resolutionTask = null;
				}
				if (shutdown) {
					return;
				}
				savedListener = listener;
				resolving = true;
			}
			try {
				try {
					inetAddrs = listUnits();
				} catch (Exception e) {
					synchronized (TsuruNameResolver.this) {
						if (shutdown) {
							return;
						}
					}
					savedListener.onError(Status.UNAVAILABLE.withCause(e));
					return;
				}
				List<ResolvedServerInfo> servers = new ArrayList<ResolvedServerInfo>(inetAddrs.length);
				for (int i = 0; i < inetAddrs.length; i++) {
					InetSocketAddress inetAddr = inetAddrs[i];
					servers.add(new ResolvedServerInfo(inetAddr, Attributes.EMPTY));
				}
				savedListener.onUpdate(Collections.singletonList(servers), Attributes.EMPTY);
			} finally {
				synchronized (TsuruNameResolver.this) {
					// Because timerService is the single-threaded
					// GrpcUtil.TIMER_SERVICE in production,
					// we need to delegate the blocking work to the executor
					resolutionTask = timerService.schedule(new LogExceptionRunnable(resolutionRunnableOnExecutor),
							1, TimeUnit.MINUTES);
					resolving = false;
				}
			}
		}
	};

	private final Runnable resolutionRunnableOnExecutor = new Runnable() {
		@Override
		public void run() {
			synchronized (TsuruNameResolver.this) {
				if (!shutdown) {
					executor.execute(resolutionRunnable);
				}
			}
		}
	};

}

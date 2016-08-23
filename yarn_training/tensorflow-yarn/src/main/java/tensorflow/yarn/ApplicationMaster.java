package tensorflow.yarn;

import static tensorflow.yarn.ApplicationMasterOptions.OPT_APP_ATTEMPT_ID;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_APP_ATTEMPT_ID_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_CONTAINER_MEMORY;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_CONTAINER_MEMORY_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_CONTAINER_VCORES;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_CONTAINER_VCORES_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_DEBUG;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_DEBUG_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_HELP;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_HELP_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_NUM_CONTAINERS;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_NUM_CONTAINERS_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_PRIORITY;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_PRIORITY_DESC;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_SHELL_ENV;
import static tensorflow.yarn.ApplicationMasterOptions.OPT_SHELL_ENV_DESC;
import static tensorflow.yarn.TFConstants.SCRIPT_PATH;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_LEN;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_LOCATION;
import static tensorflow.yarn.TFConstants.TENSORFLOW_SCRIPT_TIMESTAMP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.util.ExitUtil;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.ContainerManagementProtocol;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerExitStatus;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.NodeReport;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.timeline.TimelineEntity;
import org.apache.hadoop.yarn.api.records.timeline.TimelineEvent;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.TimelineClient;
import org.apache.hadoop.yarn.client.api.async.AMRMClientAsync;
import org.apache.hadoop.yarn.client.api.async.NMClientAsync;
import org.apache.hadoop.yarn.client.api.async.impl.NMClientAsyncImpl;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.security.AMRMTokenIdentifier;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.apache.log4j.LogManager;

public class ApplicationMaster {

	private static final Log log = LogFactory.getLog(ApplicationMaster.class);

	public static enum TFEvent {
		TF_APP_ATTEMPT_START, TF_APP_ATTEMPT_END, TF_CONTAINER_START, TF_CONTAINER_END
	}

	public static enum TFEntity {
		TF_APP_ATTEMPT, TF_CONTAINER
	}

	private Configuration hadoopConf;

	// Handle to communicate with the Resource Manager
	@SuppressWarnings("rawtypes")
	private AMRMClientAsync amRMClient;

	// In both secure and non-secure modes, this points to the job-submitter.
	private UserGroupInformation appSubmitterUgi;

	// Handle to communicate with the Node Manager
	private NMClientAsync nmClientAsync;
	// Listen to process the response from the Node Manager
	private NMCallbackHandler containerListener;

	// Application Attempt Id ( combination of attemptId and fail count )
	protected ApplicationAttemptId appAttemptID;

	// TODO
	// For status update for clients - yet to be implemented
	// Hostname of the container
	private String appMasterHostname = "";
	// Port on which the app master listens for status updates from clients
	private int appMasterRpcPort = -1;
	// Tracking url to which app master publishes info for clients to monitor
	private String appMasterTrackingUrl = "";

	// App Master configuration
	// No. of containers to run shell command on
	private int numTotalContainers = 1;
	// Memory to request for the container on which the shell command will run
	private int containerMemory = 10;
	// VirtualCores to request for the container on which the shell command will
	// run
	private int containerVirtualCores = 1;
	// Priority of the request
	private int requestPriority;

	// Counter for completed containers ( complete denotes successful or failed
	// )
	private AtomicInteger numCompletedContainers = new AtomicInteger();
	// Allocated container count so that we know how many containers has the RM
	// allocated to us
	protected AtomicInteger numAllocatedContainers = new AtomicInteger();
	// Count of failed containers
	private AtomicInteger numFailedContainers = new AtomicInteger();
	// Count of containers already requested from the RM
	// Needed as once requested, we should not request for containers again.
	// Only request for more if the original requirement changes.
	protected AtomicInteger numRequestedContainers = new AtomicInteger();

	// Shell command to be executed
	private String shellCommand = "";
	// Args to be passed to the shell command
	private String shellArgs = "";
	// Env variables to be setup for the shell command
	private Map<String, String> shellEnv = new HashMap<String, String>();

	// Location of shell script ( obtained from info set in env )
	// Shell script path in fs
	private String scriptPath = "";
	// Timestamp needed for creating a local resource
	private long shellScriptPathTimestamp = 0;
	// File length needed for local resource
	private long shellScriptPathLen = 0;

	// Hardcoded path to shell script in launch container's local env
	private static final String ExecShellStringPath = SCRIPT_PATH + ".sh";
	private static final String ExecBatScripStringtPath = SCRIPT_PATH + ".bat";

	// Hardcoded path to custom log_properties
	private static final String log4jPath = "log4j.properties";

	private static final String shellCommandPath = "shellCommands";
	private static final String shellArgsPath = "shellArgs";

	private volatile boolean done;

	private ByteBuffer allTokens;

	// Launch threads
	private List<Thread> launchThreads = new ArrayList<Thread>();

	// Timeline Client
	private TimelineClient timelineClient;

	private final String linux_bash_command = "bash";
	private final String windows_command = "cmd /c";

	public static void main(String[] args) {
		boolean result = false;
		try {
			ApplicationMaster appMaster = new ApplicationMaster();
			log.info("Initializing ApplicationMaster");
			boolean doRun = appMaster.init(args);
			if (!doRun) {
				System.exit(0);
			}
			appMaster.run();
			result = appMaster.finish();
		} catch (Throwable t) {
			log.fatal("Error running ApplicationMaster", t);
			LogManager.shutdown();
			ExitUtil.terminate(1, t);
		}
		if (result) {
			log.info("Application Master completed successfully. exiting");
			System.exit(0);
		} else {
			log.info("Application Master failed. exiting");
			System.exit(2);
		}
	}

	private void dumpOutDebugInfo() {
		log.info("Dump debug output");
		Map<String, String> envs = System.getenv();
		for (Map.Entry<String, String> env : envs.entrySet()) {
			log.info("System env: key=" + env.getKey() + ", val=" + env.getValue());
			System.out.println("System env: key=" + env.getKey() + ", val=" + env.getValue());
		}

		BufferedReader buf = null;
		try {
			String lines = Shell.WINDOWS ? Shell.execCommand("cmd", "/c", "dir") : Shell.execCommand("ls", "-al");
			buf = new BufferedReader(new StringReader(lines));
			String line = "";
			while ((line = buf.readLine()) != null) {
				log.info("System CWD content: " + line);
				System.out.println("System CWD content: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.cleanup(log, buf);
		}
	}

	public ApplicationMaster() {
		hadoopConf = new YarnConfiguration();
	}

	public boolean init(String[] args) throws ParseException, IOException {
		Options opts = new Options();
		opts.addOption(OPT_APP_ATTEMPT_ID, true, OPT_APP_ATTEMPT_ID_DESC);
		opts.addOption(OPT_SHELL_ENV, true, OPT_SHELL_ENV_DESC);
		opts.addOption(OPT_CONTAINER_MEMORY, true, OPT_CONTAINER_MEMORY_DESC);
		opts.addOption(OPT_CONTAINER_VCORES, true, OPT_CONTAINER_VCORES_DESC);
		opts.addOption(OPT_NUM_CONTAINERS, true, OPT_NUM_CONTAINERS_DESC);
		opts.addOption(OPT_PRIORITY, true, OPT_PRIORITY_DESC);
		opts.addOption(OPT_DEBUG, false, OPT_DEBUG_DESC);
		opts.addOption(OPT_HELP, false, OPT_HELP_DESC);
		
		CommandLine cliParser = new GnuParser().parse(opts, args);

		if (args.length == 0) {
			printUsage(opts);
			throw new IllegalArgumentException("No args specified for application master to initialize");
		}

		if (fileExist(log4jPath)) {
			try {
				Log4jPropertyHelper.updateLog4jConfiguration(ApplicationMaster.class, log4jPath);
			} catch (Exception e) {
				log.warn("Can not set up custom log4j properties. " + e);
			}
		}

		if (cliParser.hasOption(OPT_HELP)) {
			printUsage(opts);
			return false;
		}

		if (cliParser.hasOption(OPT_DEBUG)) {
			dumpOutDebugInfo();
		}

		Map<String, String> envs = System.getenv();

		if (!envs.containsKey(Environment.CONTAINER_ID.name())) {
			if (cliParser.hasOption(OPT_APP_ATTEMPT_ID)) {
				String appIdStr = cliParser.getOptionValue(OPT_APP_ATTEMPT_ID, "");
				appAttemptID = ConverterUtils.toApplicationAttemptId(appIdStr);
			} else {
				throw new IllegalArgumentException("Application Attempt Id not set in the environment");
			}
		} else {
			ContainerId containerId = ConverterUtils.toContainerId(envs.get(Environment.CONTAINER_ID.name()));
			appAttemptID = containerId.getApplicationAttemptId();
		}

		if (!envs.containsKey(ApplicationConstants.APP_SUBMIT_TIME_ENV)) {
			throw new RuntimeException(ApplicationConstants.APP_SUBMIT_TIME_ENV + " not set in the environment");
		}
		if (!envs.containsKey(Environment.NM_HOST.name())) {
			throw new RuntimeException(Environment.NM_HOST.name() + " not set in the environment");
		}
		if (!envs.containsKey(Environment.NM_HTTP_PORT.name())) {
			throw new RuntimeException(Environment.NM_HTTP_PORT + " not set in the environment");
		}
		if (!envs.containsKey(Environment.NM_PORT.name())) {
			throw new RuntimeException(Environment.NM_PORT.name() + " not set in the environment");
		}

		log.info("Application master for app, appId=" + appAttemptID.getApplicationId().getId() + ", clustertimestamp="
				+ appAttemptID.getApplicationId().getClusterTimestamp() + ", attemptId=" + appAttemptID.getAttemptId());

		if (!fileExist(shellCommandPath) && envs.get(TENSORFLOW_SCRIPT_LOCATION).isEmpty()) {
			throw new IllegalArgumentException(
					"No shell command or shell script specified to be executed by application master");
		}

		if (fileExist(shellCommandPath)) {
			shellCommand = readContent(shellCommandPath);
		}

		if (fileExist(shellArgsPath)) {
			shellArgs = readContent(shellArgsPath);
		}

		if (cliParser.hasOption(OPT_SHELL_ENV)) {
			String shellEnvs[] = cliParser.getOptionValues(OPT_SHELL_ENV);
			for (String env : shellEnvs) {
				env = env.trim();
				int index = env.indexOf('=');
				if (index == -1) {
					shellEnv.put(env, "");
					continue;
				}
				String key = env.substring(0, index);
				String val = "";
				if (index < (env.length() - 1)) {
					val = env.substring(index + 1);
				}
				shellEnv.put(key, val);
			}
		}

		if (envs.containsKey(TENSORFLOW_SCRIPT_LOCATION)) {
			scriptPath = envs.get(TENSORFLOW_SCRIPT_LOCATION);

			if (envs.containsKey(TENSORFLOW_SCRIPT_TIMESTAMP)) {
				shellScriptPathTimestamp = Long.valueOf(envs.get(TENSORFLOW_SCRIPT_TIMESTAMP));
			}
			if (envs.containsKey(TENSORFLOW_SCRIPT_LEN)) {
				shellScriptPathLen = Long.valueOf(envs.get(TENSORFLOW_SCRIPT_LEN));
			}

			if (!scriptPath.isEmpty() && (shellScriptPathTimestamp <= 0 || shellScriptPathLen <= 0)) {
				log.error("Illegal values in env for shell script path, path=" + scriptPath + ", len="
						+ shellScriptPathLen + ", timestamp=" + shellScriptPathTimestamp);
				throw new IllegalArgumentException("Illegal values in env for shell script path");
			}
		}

		containerMemory = Integer.parseInt(cliParser.getOptionValue(OPT_CONTAINER_MEMORY, "10"));
		containerVirtualCores = Integer.parseInt(cliParser.getOptionValue(OPT_CONTAINER_VCORES, "1"));
		numTotalContainers = Integer.parseInt(cliParser.getOptionValue(OPT_NUM_CONTAINERS, "1"));
		if (numTotalContainers == 0) {
			throw new IllegalArgumentException("Cannot run distributed shell with no containers");
		}
		requestPriority = Integer.parseInt(cliParser.getOptionValue(OPT_PRIORITY, "0"));

		// Creating the Timeline Client
		timelineClient = TimelineClient.createTimelineClient();
		timelineClient.init(hadoopConf);
		timelineClient.start();

		return true;
	}

	private void printUsage(Options opts) {
		new HelpFormatter().printHelp("ApplicationMaster", opts);
	}

	@SuppressWarnings({ "unchecked" })
	public void run() throws YarnException, IOException {
		log.info("Starting ApplicationMaster");
		try {
			publishApplicationAttemptEvent(timelineClient, appAttemptID.toString(), TFEvent.TF_APP_ATTEMPT_START);
		} catch (Exception e) {
			log.error("App Attempt start event coud not be pulished for " + appAttemptID.toString(), e);
		}

		Credentials credentials = UserGroupInformation.getCurrentUser().getCredentials();
		DataOutputBuffer dob = new DataOutputBuffer();
		credentials.writeTokenStorageToStream(dob);
		// Now remove the AM->RM token so that containers cannot access it.
		Iterator<Token<?>> iter = credentials.getAllTokens().iterator();
		log.info("Executing with tokens:");
		while (iter.hasNext()) {
			Token<?> token = iter.next();
			log.info(token);
			if (token.getKind().equals(AMRMTokenIdentifier.KIND_NAME)) {
				iter.remove();
			}
		}
		allTokens = ByteBuffer.wrap(dob.getData(), 0, dob.getLength());

		// Create appSubmitterUgi and add original tokens to it
		String appSubmitterUserName = System.getenv(ApplicationConstants.Environment.USER.name());
		appSubmitterUgi = UserGroupInformation.createRemoteUser(appSubmitterUserName);
		appSubmitterUgi.addCredentials(credentials);

		AMRMClientAsync.CallbackHandler allocListener = new RMCallbackHandler();
		amRMClient = AMRMClientAsync.createAMRMClientAsync(1000, allocListener);
		amRMClient.init(hadoopConf);
		amRMClient.start();

		containerListener = createNMCallbackHandler();
		nmClientAsync = new NMClientAsyncImpl(containerListener);
		nmClientAsync.init(hadoopConf);
		nmClientAsync.start();

		// Setup local RPC Server to accept status requests directly from
		// clients
		// TODO need to setup a protocol for client to be able to communicate to
		// the RPC server
		// TODO use the rpc port info to register with the RM for the client to
		// send requests to this app master

		// Register self with ResourceManager
		// This will start heartbeating to the RM
		appMasterHostname = NetUtils.getHostname();
		RegisterApplicationMasterResponse response = amRMClient.registerApplicationMaster(appMasterHostname,
				appMasterRpcPort, appMasterTrackingUrl);
		// Dump out information about cluster capability as seen by the
		// resource manager
		int maxMem = response.getMaximumResourceCapability().getMemory();
		log.info("Max mem capabililty of resources in this cluster " + maxMem);

		int maxVCores = response.getMaximumResourceCapability().getVirtualCores();
		log.info("Max vcores capabililty of resources in this cluster " + maxVCores);

		// A resource ask cannot exceed the max.
		if (containerMemory > maxMem) {
			log.info("Container memory specified above max threshold of cluster. Using max value, specified="
					+ containerMemory + ", max=" + maxMem);
			containerMemory = maxMem;
		}

		if (containerVirtualCores > maxVCores) {
			log.info("Container virtual cores specified above max threshold of cluster. Using max value"
					+ ", specified=" + containerVirtualCores + ", max=" + maxVCores);
			containerVirtualCores = maxVCores;
		}

		List<Container> previousAMRunningContainers = response.getContainersFromPreviousAttempts();
		log.info("Received " + previousAMRunningContainers.size()
				+ " previous AM's running containers on AM registration.");
		numAllocatedContainers.addAndGet(previousAMRunningContainers.size());

		int numTotalContainersToRequest = numTotalContainers - previousAMRunningContainers.size();
		// Setup ask for containers from RM
		// Send request for containers to RM
		// Until we get our fully allocated quota, we keep on polling RM for
		// containers
		// Keep looping until all the containers are launched and shell script
		// executed on them ( regardless of success/failure).
		for (int i = 0; i < numTotalContainersToRequest; ++i) {
			ContainerRequest containerAsk = setupContainerAskForRM();
			amRMClient.addContainerRequest(containerAsk);
		}
		numRequestedContainers.set(numTotalContainersToRequest);
		try {
			publishApplicationAttemptEvent(timelineClient, appAttemptID.toString(), TFEvent.TF_APP_ATTEMPT_END);
		} catch (Exception e) {
			log.error("App Attempt start event coud not be pulished for " + appAttemptID.toString(), e);
		}
	}

	NMCallbackHandler createNMCallbackHandler() {
		return new NMCallbackHandler(this);
	}

	protected boolean finish() {
		// wait for completion.
		while (!done && (numCompletedContainers.get() != numTotalContainers)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException ex) {
			}
		}

		// Join all launched threads
		// needed for when we time out
		// and we need to release containers
		for (Thread launchThread : launchThreads) {
			try {
				launchThread.join(10000);
			} catch (InterruptedException e) {
				log.info("Exception thrown in thread join: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// When the application completes, it should stop all running containers
		log.info("Application completed. Stopping running containers");
		nmClientAsync.stop();

		// When the application completes, it should send a finish application
		// signal to the RM
		log.info("Application completed. Signalling finish to RM");

		FinalApplicationStatus appStatus;
		String appMessage = null;
		boolean success = true;
		if (numFailedContainers.get() == 0 && numCompletedContainers.get() == numTotalContainers) {
			appStatus = FinalApplicationStatus.SUCCEEDED;
		} else {
			appStatus = FinalApplicationStatus.FAILED;
			appMessage = "Diagnostics, total=" + numTotalContainers + ", completed=" + numCompletedContainers.get()
					+ ", allocated=" + numAllocatedContainers.get() + ", failed=" + numFailedContainers.get();
			success = false;
		}
		try {
			amRMClient.unregisterApplicationMaster(appStatus, appMessage, null);
		} catch (YarnException ex) {
			log.error("Failed to unregister application", ex);
		} catch (IOException e) {
			log.error("Failed to unregister application", e);
		}

		amRMClient.stop();

		return success;
	}

	private class RMCallbackHandler implements AMRMClientAsync.CallbackHandler {
		@SuppressWarnings("unchecked")
		@Override
		public void onContainersCompleted(List<ContainerStatus> completedContainers) {
			log.info("Got response from RM for container ask, completedCnt=" + completedContainers.size());
			for (ContainerStatus containerStatus : completedContainers) {
				log.info("Got container status for containerID=" + containerStatus.getContainerId() + ", state="
						+ containerStatus.getState() + ", exitStatus=" + containerStatus.getExitStatus()
						+ ", diagnostics=" + containerStatus.getDiagnostics());

				// non complete containers should not be here
				assert (containerStatus.getState() == ContainerState.COMPLETE);

				// increment counters for completed/failed containers
				int exitStatus = containerStatus.getExitStatus();
				if (0 != exitStatus) {
					// container failed
					if (ContainerExitStatus.ABORTED != exitStatus) {
						// shell script failed
						// counts as completed
						numCompletedContainers.incrementAndGet();
						numFailedContainers.incrementAndGet();
					} else {
						// container was killed by framework, possibly preempted
						// we should re-try as the container was lost for some
						// reason
						numAllocatedContainers.decrementAndGet();
						numRequestedContainers.decrementAndGet();
						// we do not need to release the container as it would
						// be done
						// by the RM
					}
				} else {
					// nothing to do
					// container completed successfully
					numCompletedContainers.incrementAndGet();
					log.info("Container completed successfully." + ", containerId=" + containerStatus.getContainerId());
				}
				try {
					publishContainerEndEvent(timelineClient, containerStatus);
				} catch (Exception e) {
					log.error("Container start event could not be pulished for "
							+ containerStatus.getContainerId().toString(), e);
				}
			}

			// ask for more containers if any failed
			int askCount = numTotalContainers - numRequestedContainers.get();
			numRequestedContainers.addAndGet(askCount);

			if (askCount > 0) {
				for (int i = 0; i < askCount; ++i) {
					ContainerRequest containerAsk = setupContainerAskForRM();
					amRMClient.addContainerRequest(containerAsk);
				}
			}

			if (numCompletedContainers.get() == numTotalContainers) {
				done = true;
			}
		}

		@Override
		public void onContainersAllocated(List<Container> allocatedContainers) {
			log.info("Got response from RM for container ask, allocatedCnt=" + allocatedContainers.size());
			numAllocatedContainers.addAndGet(allocatedContainers.size());
			for (Container allocatedContainer : allocatedContainers) {
				log.info("Launching shell command on a new container, containerId=" + allocatedContainer.getId()
						+ ", containerNode=" + allocatedContainer.getNodeId().getHost() + ":"
						+ allocatedContainer.getNodeId().getPort() + ", containerNodeURI="
						+ allocatedContainer.getNodeHttpAddress() + ", containerResourceMemory"
						+ allocatedContainer.getResource().getMemory() + ", containerResourceVirtualCores"
						+ allocatedContainer.getResource().getVirtualCores());
				// + ", containerToken"
				// +allocatedContainer.getContainerToken().getIdentifier().toString());

				LaunchContainerRunnable runnableLaunchContainer = new LaunchContainerRunnable(allocatedContainer,
						containerListener);
				Thread launchThread = new Thread(runnableLaunchContainer);

				// launch and start the container on a separate thread to keep
				// the main thread unblocked
				// as all containers may not be allocated at one go.
				launchThreads.add(launchThread);
				launchThread.start();
			}
		}

		@Override
		public void onShutdownRequest() {
			done = true;
		}

		@Override
		public void onNodesUpdated(List<NodeReport> updatedNodes) {
		}

		@Override
		public float getProgress() {
			// set progress to deliver to RM on next heartbeat
			float progress = (float) numCompletedContainers.get() / numTotalContainers;
			return progress;
		}

		@Override
		public void onError(Throwable e) {
			done = true;
			amRMClient.stop();
		}
	}

	static class NMCallbackHandler implements NMClientAsync.CallbackHandler {

		private ConcurrentMap<ContainerId, Container> containers = new ConcurrentHashMap<ContainerId, Container>();
		private final ApplicationMaster applicationMaster;

		public NMCallbackHandler(ApplicationMaster applicationMaster) {
			this.applicationMaster = applicationMaster;
		}

		public void addContainer(ContainerId containerId, Container container) {
			containers.putIfAbsent(containerId, container);
		}

		@Override
		public void onContainerStopped(ContainerId containerId) {
			if (log.isDebugEnabled()) {
				log.debug("Succeeded to stop Container " + containerId);
			}
			containers.remove(containerId);
		}

		@Override
		public void onContainerStatusReceived(ContainerId containerId, ContainerStatus containerStatus) {
			if (log.isDebugEnabled()) {
				log.debug("Container Status: id=" + containerId + ", status=" + containerStatus);
			}
		}

		@Override
		public void onContainerStarted(ContainerId containerId, Map<String, ByteBuffer> allServiceResponse) {
			if (log.isDebugEnabled()) {
				log.debug("Succeeded to start Container " + containerId);
			}
			Container container = containers.get(containerId);
			if (container != null) {
				applicationMaster.nmClientAsync.getContainerStatusAsync(containerId, container.getNodeId());
			}
			try {
				ApplicationMaster.publishContainerStartEvent(applicationMaster.timelineClient, container);
			} catch (Exception e) {
				log.error("Container start event coud not be pulished for " + container.getId().toString(), e);
			}
		}

		@Override
		public void onStartContainerError(ContainerId containerId, Throwable t) {
			log.error("Failed to start Container " + containerId);
			containers.remove(containerId);
			applicationMaster.numCompletedContainers.incrementAndGet();
			applicationMaster.numFailedContainers.incrementAndGet();
		}

		@Override
		public void onGetContainerStatusError(ContainerId containerId, Throwable t) {
			log.error("Failed to query the status of Container " + containerId);
		}

		@Override
		public void onStopContainerError(ContainerId containerId, Throwable t) {
			log.error("Failed to stop Container " + containerId);
			containers.remove(containerId);
		}
	}

	/**
	 * Thread to connect to the {@link ContainerManagementProtocol} and launch
	 * the container that will execute the shell command.
	 */
	private class LaunchContainerRunnable implements Runnable {

		// Allocated container
		Container container;

		NMCallbackHandler containerListener;

		/**
		 * @param lcontainer
		 *            Allocated container
		 * @param containerListener
		 *            Callback handler of the container
		 */
		public LaunchContainerRunnable(Container lcontainer, NMCallbackHandler containerListener) {
			this.container = lcontainer;
			this.containerListener = containerListener;
		}

		@Override
		/**
		 * Connects to CM, sets up container launch context for shell command
		 * and eventually dispatches the container start request to the CM.
		 */
		public void run() {
			log.info("Setting up container launch container for containerid=" + container.getId());
			ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);

			// Set the environment
			ctx.setEnvironment(shellEnv);

			// Set the local resources
			Map<String, LocalResource> localResources = new HashMap<String, LocalResource>();

			// The container for the eventual shell commands needs its own local
			// resources too.
			// In this scenario, if a shell script is specified, we need to have
			// it
			// copied and made available to the container.
			if (!scriptPath.isEmpty()) {
				Path renamedScriptPath = null;
				if (Shell.WINDOWS) {
					renamedScriptPath = new Path(scriptPath + ".bat");
				} else {
					renamedScriptPath = new Path(scriptPath + ".sh");
				}

				try {
					// rename the script file based on the underlying OS syntax.
					renameScriptFile(renamedScriptPath);
				} catch (Exception e) {
					log.error("Not able to add suffix (.bat/.sh) to the shell script filename", e);
					// We know we cannot continue launching the container
					// so we should release it.
					numCompletedContainers.incrementAndGet();
					numFailedContainers.incrementAndGet();
					return;
				}

				LocalResource shellRsrc = Records.newRecord(LocalResource.class);
				shellRsrc.setType(LocalResourceType.FILE);
				shellRsrc.setVisibility(LocalResourceVisibility.APPLICATION);
				try {
					shellRsrc.setResource(ConverterUtils.getYarnUrlFromURI(new URI(renamedScriptPath.toString())));
				} catch (URISyntaxException e) {
					log.error("Error when trying to use shell script path specified" + " in env, path="
							+ renamedScriptPath, e);

					// A failure scenario on bad input such as invalid shell
					// script path
					// We know we cannot continue launching the container
					// so we should release it.
					// TODO
					numCompletedContainers.incrementAndGet();
					numFailedContainers.incrementAndGet();
					return;
				}
				shellRsrc.setTimestamp(shellScriptPathTimestamp);
				shellRsrc.setSize(shellScriptPathLen);
				localResources.put(Shell.WINDOWS ? ExecBatScripStringtPath : ExecShellStringPath, shellRsrc);
				shellCommand = Shell.WINDOWS ? windows_command : linux_bash_command;
			}
			ctx.setLocalResources(localResources);

			// Set the necessary command to execute on the allocated container
			Vector<CharSequence> vargs = new Vector<CharSequence>(5);

			// Set executable command
			vargs.add(shellCommand);
			// Set shell script path
			if (!scriptPath.isEmpty()) {
				vargs.add(Shell.WINDOWS ? ExecBatScripStringtPath : ExecShellStringPath);
			}

			// Set args for the shell command if any
			vargs.add(shellArgs);
			// Add log redirect params
			vargs.add("1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout");
			vargs.add("2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr");

			// Get final commmand
			StringBuilder command = new StringBuilder();
			for (CharSequence str : vargs) {
				command.append(str).append(" ");
			}

			List<String> commands = new ArrayList<String>();
			commands.add(command.toString());
			ctx.setCommands(commands);

			// Set up tokens for the container too. Today, for normal shell
			// commands,
			// the container in distribute-shell doesn't need any tokens. We are
			// populating them mainly for NodeManagers to be able to download
			// any
			// files in the distributed file-system. The tokens are otherwise
			// also
			// useful in cases, for e.g., when one is running a "hadoop dfs"
			// command
			// inside the distributed shell.
			ctx.setTokens(allTokens.duplicate());

			containerListener.addContainer(container.getId(), container);
			nmClientAsync.startContainerAsync(container, ctx);
		}
	}

	private void renameScriptFile(final Path renamedScriptPath) throws IOException, InterruptedException {
		appSubmitterUgi.doAs(new PrivilegedExceptionAction<Void>() {
			@Override
			public Void run() throws IOException {
				FileSystem fs = renamedScriptPath.getFileSystem(hadoopConf);
				fs.rename(new Path(scriptPath), renamedScriptPath);
				return null;
			}
		});
		log.info("User " + appSubmitterUgi.getUserName() + " added suffix(.sh/.bat) to script file as "
				+ renamedScriptPath);
	}

	/**
	 * Setup the request that will be sent to the RM for the container ask.
	 *
	 * @return the setup ResourceRequest to be sent to RM
	 */
	private ContainerRequest setupContainerAskForRM() {
		// setup requirements for hosts
		// using * as any host will do for the distributed shell app
		// set the priority for the request
		Priority pri = Records.newRecord(Priority.class);
		// TODO - what is the range for priority? how to decide?
		pri.setPriority(requestPriority);

		// Set up resource type requirements
		// For now, memory and CPU are supported so we set memory and cpu
		// requirements
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(containerMemory);
		capability.setVirtualCores(containerVirtualCores);

		ContainerRequest request = new ContainerRequest(capability, null, null, pri);
		log.info("Requested container ask: " + request.toString());
		return request;
	}

	private boolean fileExist(String filePath) {
		return new File(filePath).exists();
	}

	private String readContent(String filePath) throws IOException {
		DataInputStream ds = null;
		try {
			ds = new DataInputStream(new FileInputStream(filePath));
			return ds.readUTF();
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(ds);
		}
	}

	private static void publishContainerStartEvent(TimelineClient timelineClient, Container container)
			throws IOException, YarnException {
		TimelineEntity entity = new TimelineEntity();
		entity.setEntityId(container.getId().toString());
		entity.setEntityType(TFEntity.TF_CONTAINER.toString());
		entity.addPrimaryFilter("user", UserGroupInformation.getCurrentUser().toString());
		TimelineEvent event = new TimelineEvent();
		event.setTimestamp(System.currentTimeMillis());
		event.setEventType(TFEvent.TF_CONTAINER_START.toString());
		event.addEventInfo("Node", container.getNodeId().toString());
		event.addEventInfo("Resources", container.getResource().toString());
		entity.addEvent(event);

		timelineClient.putEntities(entity);
	}

	private static void publishContainerEndEvent(TimelineClient timelineClient, ContainerStatus container)
			throws IOException, YarnException {
		TimelineEntity entity = new TimelineEntity();
		entity.setEntityId(container.getContainerId().toString());
		entity.setEntityType(TFEntity.TF_CONTAINER.toString());
		entity.addPrimaryFilter("user", UserGroupInformation.getCurrentUser().toString());
		TimelineEvent event = new TimelineEvent();
		event.setTimestamp(System.currentTimeMillis());
		event.setEventType(TFEvent.TF_CONTAINER_END.toString());
		event.addEventInfo("State", container.getState().name());
		event.addEventInfo("Exit Status", container.getExitStatus());
		entity.addEvent(event);

		timelineClient.putEntities(entity);
	}

	private static void publishApplicationAttemptEvent(TimelineClient timelineClient, String appAttemptId,
			TFEvent appEvent) throws IOException, YarnException {
		TimelineEntity entity = new TimelineEntity();
		entity.setEntityId(appAttemptId);
		entity.setEntityType(TFEntity.TF_APP_ATTEMPT.toString());
		entity.addPrimaryFilter("user", UserGroupInformation.getCurrentUser().toString());
		TimelineEvent event = new TimelineEvent();
		event.setEventType(appEvent.toString());
		event.setTimestamp(System.currentTimeMillis());
		entity.addEvent(event);

		timelineClient.putEntities(entity);
	}
}

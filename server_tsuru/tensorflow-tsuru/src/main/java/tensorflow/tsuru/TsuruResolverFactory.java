package tensorflow.tsuru;

import java.net.URI;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.internal.GrpcUtil;

public class TsuruResolverFactory extends NameResolver.Factory {

	@Override
	public NameResolver newNameResolver(URI targetUri, Attributes params) {
		return new TsuruNameResolver(targetUri, params,  GrpcUtil.TIMER_SERVICE, GrpcUtil.SHARED_CHANNEL_EXECUTOR);
	}

	@Override
	public String getDefaultScheme() {
		return "tsuru";
	}

}

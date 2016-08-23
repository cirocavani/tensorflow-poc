package tensorflow.tsuru;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import skeleton.MnistServiceGrpc;
import skeleton.MnistServiceGrpc.MnistServiceBlockingStub;
import skeleton.Skeleton.MnistRequest;
import skeleton.Skeleton.MnistResponse;
import tensorflow.tsuru.MNISTData.MNIST;

public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("TensorFlow Tsuru start...");

		String appName = args.length > 0 ? args[0] : "tf-skeleton";
		int repeats = args.length > 1 ? Integer.valueOf(args[1]) : 1_000_000;

		MnistServiceBlockingStub service = createService(appName);
		evaluate(service, repeats);

		System.out.println("TensorFlow Tsuru shutdown.");
	}

	static MnistServiceBlockingStub createService(String appName) throws Exception {
		System.out.println("Tsuru app " + appName);
		System.out.println();

		final ManagedChannel channel = ManagedChannelBuilder
				.forTarget("tsuru://" + appName)
				.nameResolverFactory(new TsuruResolverFactory())
				.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
				.usePlaintext(true)
				.build();
		final MnistServiceBlockingStub blockingStub = MnistServiceGrpc.newBlockingStub(channel);
		// final MnistServiceStub asyncStub = MnistServiceGrpc.newStub(channel);
		return blockingStub;
	}

	static void evaluate(MnistServiceBlockingStub service, int repeats) throws Exception {
		System.out.println("Evaluating " + repeats + " requests...");
		System.out.println();

		int errors = 0;
		int trials = 0;

		MNIST mnist = MNISTData.test();
		System.out.println();

		int N = mnist.images.size();
		Random rnd = new Random();

		while (trials < repeats) {
			int example = rnd.nextInt(N);
			MnistRequest request = image(mnist, example);
			try {
			MnistResponse response = service.classify(request);
			int result = prediction(response);

			int expected = mnist.labels[example];

			trials++;
			if (result != expected)
				errors++;

			// System.out.println("Trial: " + trials);
			// System.out.println("Expected: " + expected);
			// System.out.println("Result: " + result);
			// System.out.println();
			
			if (trials % 100 == 0) {
				System.out.println("Trials: " + trials);
				System.out.println("Errors: " + errors);
				float err = 100f * errors;
				err /= trials;
				float acc = 100f - err;
				System.out.println("Accuracy: " + acc);
				System.out.println("Error Rate: " + err);
				System.out.println();
			}
			} catch (Exception e) {
				System.out.println("Request error " + trials + ": " + e.getMessage());
				System.out.println();
			}
		}

		((ManagedChannel) service.getChannel()).shutdown().awaitTermination(5, TimeUnit.SECONDS);

		System.out.println("Trials: " + trials);
		System.out.println("Errors: " + errors);
		float err = 100f * errors;
		err /= trials;
		float acc = 100f - err;
		System.out.println("Accuracy: " + acc);
		System.out.println("Error Rate: " + err);
	}

	static MnistRequest image(MNIST data, int i) {
		byte[] raw = data.images.get(i);
		MnistRequest.Builder req = MnistRequest.newBuilder();
		for (byte px : raw) {
			int gray = Byte.toUnsignedInt(px);
			// System.out.println("Pixel: " + gray);
			float input = gray / 255f;
			// System.out.println("Input: " + input);
			req.addImageData(input);
		}
		return req.build();
	}

	static int prediction(MnistResponse response) {
		int pred = -1;
		float prob = 0;
		// System.out.println("Classes: " + response.getValueCount());
		for (int i = 0; i < response.getValueCount(); i++) {
			float p = response.getValue(i);
			// System.out.println("pred: " + i);
			// System.out.println("prob: " + p);
			if (p > prob) {
				pred = i;
				prob = p;
			}
		}
		return pred;
	}

}

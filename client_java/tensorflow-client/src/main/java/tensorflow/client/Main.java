package tensorflow.client;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import skeleton.MnistServiceGrpc;
import skeleton.MnistServiceGrpc.MnistServiceBlockingStub;
import skeleton.Skeleton.MnistRequest;
import skeleton.Skeleton.MnistResponse;
import tensorflow.client.MNISTData.MNIST;

public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("TensorFlow Client start...");
		String host = args.length > 0 ? args[0] : "172.17.0.2";
		int port = args.length > 1 ? Integer.valueOf(args[1]) : 9000;
		int repeats = args.length > 2 ? Integer.valueOf(args[2]) : 1000;
		evaluate(host, port, repeats);
		System.out.println("TensorFlow Client shutdown.");
	}

	static MnistRequest image(MNIST data, int i) {
		byte[] raw = data.images.get(i);
		MnistRequest.Builder req = MnistRequest.newBuilder();
		for (byte px : raw) {
			int gray = Byte.toUnsignedInt(px);
//			System.out.println("Pixel: " + gray);
			float input = gray / 255f;
//			System.out.println("Input: " + input);
			req.addImageData(input);
		}
		return req.build();
	}
	
	static int prediction(MnistResponse response) {
		int pred = -1;
		float prob = 0;
//		System.out.println("Classes: " + response.getValueCount());
		for (int i = 0; i < response.getValueCount(); i++) {
			float p = response.getValue(i);
//			System.out.println("pred: " + i);
//			System.out.println("prob: " + p);
			if (p > prob) {
				pred = i;
				prob = p;
			}
		}
		return pred;
	}
	
	static void evaluate(String host, int port, int repeats) throws Exception {
		System.out.println("Evaluating " + host + ":" + port + " with " + repeats + " requests...");
		System.out.println();
		final ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
		final MnistServiceBlockingStub blockingStub = MnistServiceGrpc.newBlockingStub(channel);
//		final MnistServiceStub asyncStub = MnistServiceGrpc.newStub(channel);

		int errors = 0;
		int trials = 0;
		
		MNIST mnist = MNISTData.test();
		int N = mnist.images.size();
		Random rnd = new Random();
		
		while (trials < repeats) {
			int example = rnd.nextInt(N);
			MnistRequest request = image(mnist, example);
			MnistResponse response = blockingStub.classify(request);
			int result = prediction(response);
		
			int expected = mnist.labels[example];
		
			trials++;
			if (result != expected)
				errors++;
		
//			System.out.println("Trial: " + trials);
//			System.out.println("Expected: " + expected);
//			System.out.println("Result: " + result);
//			System.out.println();
		}
		
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);

		System.out.println("Trials: " + trials);
		System.out.println("Errors: " + errors);
		float err = 100f * errors;
		err /= trials;
		float acc = 100f - err;
		System.out.println("Accuracy: " + acc);
		System.out.println("Error Rate: " + err);
	}
}

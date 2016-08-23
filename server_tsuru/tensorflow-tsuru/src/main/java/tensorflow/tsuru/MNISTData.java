package tensorflow.tsuru;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class MNISTData {

	private MNISTData() {
	}

	private static final Path DATA_DIR = Paths.get("data");

	private static final Path IMAGES_GZ = DATA_DIR.resolve("t10k-images-idx3-ubyte.gz");

	private static final Path LABELS_GZ = DATA_DIR.resolve("t10k-labels-idx1-ubyte.gz");

	public static class MNIST {
		public final List<byte[]> images;
		public final int[] labels;
		private MNIST(List<byte[]> images, int[] labels) {
			this.images = images;
			this.labels = labels;
		}
	}

	public static MNIST test() throws Exception {
		System.out.println("Downloading MNIST...");
		downloadMNIST();
		System.out.println("Downloading MNIST done.");
		System.out.println("Loading MNIST...");
		MNIST data = loadMNIST();
		System.out.println("Loading MNIST done.");
		return data;
	}

	static void download(String url, Path outputFile) throws Exception {
		URL u = new URL(url);
		try (InputStream src = u.openStream()) {
			Files.copy(src, outputFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	static void downloadMNIST() throws Exception {
		if (!Files.exists(DATA_DIR))
			Files.createDirectory(DATA_DIR);
		if (!Files.exists(IMAGES_GZ)) {
			System.out.println("... " + IMAGES_GZ.getFileName());
			download("http://yann.lecun.com/exdb/mnist/t10k-images-idx3-ubyte.gz", IMAGES_GZ);
		}
		if (!Files.exists(LABELS_GZ)) {
			System.out.println("... " + LABELS_GZ.getFileName());
			download("http://yann.lecun.com/exdb/mnist/t10k-labels-idx1-ubyte.gz", LABELS_GZ);
		}
	}

	static MNIST loadMNIST() throws Exception {
		System.out.println("Loading " + IMAGES_GZ.getFileName() + "...");
		List<byte[]> images = new ArrayList<>();
		try (InputStream file = Files.newInputStream(IMAGES_GZ);
				GZIPInputStream gz = new GZIPInputStream(file);
				BufferedInputStream in = new BufferedInputStream(gz)) {
			byte[] magic = new byte[4];
			in.read(magic);
			System.out.println("Magic (2051): " + ByteBuffer.wrap(magic).getInt());

			byte[] items = new byte[4];
			in.read(items);
			int n = ByteBuffer.wrap(items).getInt();
			System.out.println("Images (10000): " + n);

			byte[] rows = new byte[4];
			in.read(rows);
			int u = ByteBuffer.wrap(rows).getInt();
			System.out.println("Row Size (28): " + u);

			byte[] cols = new byte[4];
			in.read(cols);
			int v = ByteBuffer.wrap(cols).getInt();
			System.out.println("Column Size (28): " + v);

			int imgSize = u * v;
			System.out.println("Image Size (784): " + imgSize);
			for (int i = 0; i < n; i++) {
				byte[] img = new byte[imgSize];
				int read = in.read(img);
				if (read != imgSize)
					throw new RuntimeException("Missing bytes (expected " + imgSize + "): " + read);
				images.add(img);
			}
		}
		System.out.println("Loading " + LABELS_GZ.getFileName() + "...");
		int[] labels = new int[images.size()];
		try (InputStream file = Files.newInputStream(LABELS_GZ);
				GZIPInputStream in = new GZIPInputStream(file)) {
			byte[] magic = new byte[4];
			in.read(magic);
			System.out.println("Magic (2049): " + ByteBuffer.wrap(magic).getInt());

			byte[] items = new byte[4];
			in.read(items);
			int n = ByteBuffer.wrap(items).getInt();
			System.out.println("Images (10000): " + n);

			for (int i = 0; i < n; i++)
				labels[i] = in.read();
		}
		return new MNIST(images, labels);
	}

}

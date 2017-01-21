package examples.vrc;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jsucuri.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Created by alexandrenery on 10/26/16. volumes
 * <numWorker> <numrayCastNodes> <imageFile> <imWidth> <imHeight> <numSamples>
 *  2 8 foot.raw 5120 2160 2
 */
public class ParVRayCast2Jsucuri {

	static float[] data = null;
	static BufferedImage im = null;
	static int samples = 0;
	static int numRayCastNodes;

	public static void main(String args[]) throws Exception {
		int nx = 256;
		int ny = 256;
		int nz = 256;

		// String filepath = "foot.raw";
		int numWorkers = new Integer(args[0]);
		numRayCastNodes = new Integer(args[1]);
		String filepath = args[2];
		int imWidth = new Integer(args[3]);
		int imHeight = new Integer(args[4]);
		samples = new Integer(args[5]).intValue();

		System.out.println("numWorkers:" + numWorkers);
		System.out.println("numRayCastNodes:" + numRayCastNodes);
		System.out.println("imWidth:" + imWidth);
		System.out.println("imHeight:" + imHeight);
		System.out.println("Samples:" + samples);

		// public Camera(Point3d eye, Point3d look, int width, int height)

		Point3d eye = new Point3d(-2000.0f, -2000.0f, 2000.0f);
		Point3d lookat = new Point3d(0.0f, -100.0f, 0.0f);
		Point3d min = new Point3d(-1.0f, -1.0f, -1.0f);
		Point3d max = new Point3d(1.0f, 1.0f, 1.0f);

		min.scale(200.0f);
		max.scale(200.0f);

		Camera cam = new Camera(imWidth, imHeight, eye, lookat);

		Grid grid = new Grid(min, max, nx, ny, nz);
		im = new BufferedImage(cam.getWidth(), cam.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

		float rcp_samples = 1.0f / (float) samples;

		NodeFunction rayCast = (NodeFunction & Serializable) (Object[] inputs) -> {
			int threadId = (int) inputs[0];

			int chunck = cam.getWidth() / numRayCastNodes;
			System.out.println("chunck: " +chunck);
			int start = threadId * chunck;
			int end = start + chunck < cam.getWidth() ? start + chunck : cam.getWidth();
			System.out.println("threadId: "+threadId + " start: "+start +" end: " + end);
			for (int i = start; i < end; i++) {
				for (int j = 0; j < cam.getHeight(); j++) {
					float r, g, b;
					r = g = b = 0.0f;

					for (int s = 0; s < samples; s++) {
						Ray ray = cam.get_primary_ray(i, j, samples);
						// Ray ray = get_primary_ray(cam, i, j, samples);

						// System.out.println("ray: " + ray);

						Color c = grid.intersectGrid(ray, data, 1.0f);

						r += c.r;
						g += c.g;
						b += c.b;

					}

					r = r * rcp_samples;
					g = g * rcp_samples;
					b = b * rcp_samples;

					float maxColor = Math.max(Math.max(r, g), b);
					if (maxColor > 1.0f) {
						r = r / maxColor;
						g = g / maxColor;
						b = b / maxColor;
					}

					// System.out.println("rgb = " + r + "," + g + "," + b);

					java.awt.Color c = new java.awt.Color(r, g, b);

					im.setRGB(i, j, c.getRGB());
				}
			}
			return 0;
		};

		NodeFunction readImage = (NodeFunction & Serializable) (Object[] inputs) -> {
			try {
				data = Util.loadRawFileFloats(filepath, nx * ny * nz);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (data == null) {
				return null;
			}
			return 0;
		};

		NodeFunction outPrices = (NodeFunction & Serializable) (Object[] inputs) -> {
			// System.out.println("prices " + inputs[0]);

			RenderedImage image = im;// (RenderedImage) inputs[0];
			File outputfile = new File("output.png");
			try {
				ImageIO.write(image, "png", outputfile);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//ImageIcon imageIcon = new ImageIcon(im);

			//JOptionPane.showMessageDialog(null, imageIcon, "Output image", JOptionPane.PLAIN_MESSAGE);
			return 0;

		};

		DFGraph graph = new DFGraph();
		Scheduler sched = new Scheduler(graph, numWorkers, false);

		/*
		 * BufferedReader fp = null; try { fp = new BufferedReader(new
		 * InputStreamReader(new FileInputStream("text.txt"))); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); }
		 */

		// FilterTagged filter = new FilterTagged(filterPrices, 1);
		Node readImageNode = new Node(readImage, 0);

		Node out = new Node(outPrices, numRayCastNodes);

		graph.add(readImageNode);

		graph.add(out);

		List<Node> rayCastNodesList = new ArrayList<>();
		List<Feeder> feederNodesList = new ArrayList<>();
		for (int i = 0; i < numRayCastNodes; i++) {
			feederNodesList.add(new Feeder(i));
			graph.add(feederNodesList.get(i));
			rayCastNodesList.add(new Node(rayCast, 2));
			graph.add(rayCastNodesList.get(i));
			feederNodesList.get(i).add_edge(rayCastNodesList.get(i), 0);
			readImageNode.add_edge(rayCastNodesList.get(i), 1);
			rayCastNodesList.get(i).add_edge(out, i);
		}

		System.out.println("Tracing...");
		long time1 = System.currentTimeMillis();

		sched.start();

		long time2 = System.currentTimeMillis();
		System.out.println("Time: " + (time2 - time1) + " ms");
	}
}
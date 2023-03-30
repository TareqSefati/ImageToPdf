package com.tareq.imagetopdf;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javax.imageio.ImageIO;

public class ImageServiceTest {

    //Using the built-in JavaFX APIs -- Image quality is decreased by rotation operation
    public static Image getRotatedImageWithBufferedImage(ImageView imageView, double angle) {
        SnapshotParameters params = new SnapshotParameters();
        params.setTransform(new Rotate(imageView.getRotate() + angle, imageView.getFitWidth() / 2, imageView.getFitHeight() / 2));
        BufferedImage bImage = SwingFXUtils.fromFXImage(imageView.snapshot(params, null), null);
        return SwingFXUtils.toFXImage(bImage, null);
    }

    public static Image getRotatedImage(ImageView imageView, double angle) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setTransform(new Rotate(imageView.getRotate(), imageView.getImage().getWidth() / 2, imageView.getImage().getHeight() / 2));
        return imageView.snapshot(parameters, null);
    }

    public static void saveRotatedImage(ImageView imageView, File file) throws IOException {
        Image rotatedImage = getRotatedImage(imageView, 90);
        ImageIO.write(SwingFXUtils.fromFXImage(rotatedImage, null), "png", file);
    }

    //Using JavaCV APIs -- For better image quality
//    public static Image rotate90Degree(Image image) {
//        int width = (int) image.getWidth();
//        int height = (int) image.getHeight();
//        PixelReader pixelReader = image.getPixelReader();
//        //Mat mat = new Mat(height, width, CvType.CV_8UC4);
//        Mat mat = new Mat(height, width, CvType.CV_8UC4);
//        for (int row = 0; row < height; row++) {
//            for (int col = 0; col < width; col++) {
//                mat.put(row, col, pixelReader.getArgb(col, row));
//            }
//        }
//        Mat rotatedMat = new Mat();
//        //Imgproc.rotate(mat, rotatedMat, Imgproc.ROTATE_90_CLOCKWISE);
//        Core.rotate(mat, rotatedMat, Core.ROTATE_90_CLOCKWISE);
//        int rotatedWidth = rotatedMat.width();
//        int rotatedHeight = rotatedMat.height();
//        WritableImage rotatedImage = new WritableImage(rotatedWidth, rotatedHeight);
//        PixelWriter pixelWriter = rotatedImage.getPixelWriter();
//        for (int row = 0; row < rotatedHeight; row++) {
//            for (int col = 0; col < rotatedWidth; col++) {
//                double[] data = rotatedMat.get(row, col);
//                int argb = ((int) data[3] & 0xFF) << 24 | ((int) data[2] & 0xFF) << 16
//                        | ((int) data[1] & 0xFF) << 8 | ((int) data[0] & 0xFF);
//                pixelWriter.setArgb(col, row, argb);
//            }
//        }
//        return rotatedImage;
//    }
    //Using ImageJ APIs -- For better image quality
    public static Image rotate90Deg(Image image) {
        //Image image = new Image("path/to/image.jpg");
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        //ImagePlus imagePlus = new ImagePlus("", bufferedImage);
        //ImageProcessor processor = imagePlus.getProcessor(); //works but image color convert to gray when rotate multiple times. 
        ImageProcessor processor = new ColorProcessor(bufferedImage); //works good. image color is preserved.
        //processor.setInterpolate(true);
        //processor.setBackgroundValue(0.0);

        processor.rotate(90.0);
        //imagePlus.setProcessor(processor);
        //bufferedImage = imagePlus.getBufferedImage();
        bufferedImage = processor.getBufferedImage();
        image = SwingFXUtils.toFXImage(bufferedImage, null);
        return image;
    }

    public static Image rotate90Deg(ImageView iv) {
        Image image = iv.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
//        ImagePlus imagePlus = new ImagePlus("", bufferedImage);
        //ImageProcessor processor = imagePlus.getProcessor(); //works but image color convert to gray when rotate multiple times. 
//        ImageProcessor processor = new ColorProcessor(bufferedImage); //works good. image color is preserved.
        //processor.setInterpolate(true);
        //processor.setBackgroundValue(0.0);

//        processor.rotate(90.0);
//        imagePlus.setProcessor(processor);
//        bufferedImage = imagePlus.getBufferedImage();
//        bufferedImage = processor.getBufferedImage();

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newBufferedImage = new BufferedImage(height, width, bufferedImage.getType());

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newBufferedImage.setRGB(height - 1 - j, i, bufferedImage.getRGB(i, j));
            }
        }

        //return newBufferedImage;

        image = SwingFXUtils.toFXImage(newBufferedImage, null);
        //iv.setRotate(iv.getRotate() + 90);
        iv.setImage(image);
        return image;
    }

}

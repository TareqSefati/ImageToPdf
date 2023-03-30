package com.tareq.imagetopdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

// Class to rotate image
public class Test {

    // Main driver method
    public static void main(String args[]) {

        // Load library required for OpenCV functions
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        // Read an image and store in a Matrix object
//        // Local directory from where image is picked
//        String file = "C:/opencv/image.jpg";
//        Mat src = Imgcodecs.imread(file);
//
//        // Create empty Mat object to store output image
//        Mat dst = new Mat();
//
//        // Define Rotation Angle
//        double angle = 90;
//
//        // Image rotation according to the angle provided
//        if (angle == 90 || angle == -270) {
//            Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE);
//        } else if (angle == 180 || angle == -180) {
//            Core.rotate(src, dst, Core.ROTATE_180);
//        } else if (angle == 270 || angle == -90) {
//            Core.rotate(src, dst,
//                    Core.ROTATE_90_COUNTERCLOCKWISE);
//        } else {
//
//            // Center of the rotation is given by
//            // midpoint of source image :
//            // (width/2.0,height/2.0)
//            Point rotPoint = new Point(src.cols() / 2.0,
//                    src.rows() / 2.0);
//
//            // Create Rotation Matrix
//            Mat rotMat = Imgproc.getRotationMatrix2D(
//                    rotPoint, angle, 1);
//
//            // Apply Affine Transformation
//            Imgproc.warpAffine(src, dst, rotMat, src.size(),
//                    Imgproc.WARP_INVERSE_MAP);
//
//            // If counterclockwise rotation is required use
//            // following: Imgproc.warpAffine(src, dst,
//            // rotMat, src.size());
//        }
//
//        // Save rotated image
//        // Destination where rotated image is saved
//        // on local directory
//        Imgcodecs.imwrite("C:/opencv/rotated_image.jpg", dst);
//        // Print message for successful execution of program
//        System.out.println("Image Rotated Successfully");
    }
    
    
    public static void createPdf(List<javafx.scene.image.Image> images, Stage stage) throws IOException, DocumentException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            String fileName = directory.getAbsolutePath() + "/images.pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            for (javafx.scene.image.Image image : images) {
                java.awt.Image awtImage = SwingFXUtils.fromFXImage(image, null);
                com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(awtImage, null);
                itextImage.scaleToFit(document.getPageSize());
                document.add(itextImage);
            }
            document.close();
        }
    }
}

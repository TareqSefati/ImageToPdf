package com.tareq.imagetopdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.coobird.thumbnailator.Thumbnails;

public class ImageService {

    //Using the core java code
    public static Image rotate90Deg(ImageView iv) {
        //Need to rotate the real image and then set it to the imageview. Not to rotate only image view.
        Image image = iv.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        BufferedImage newBufferedImage = new BufferedImage(height, width, bufferedImage.getType());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newBufferedImage.setRGB(height - 1 - j, i, bufferedImage.getRGB(i, j)); //Rotate 90 degrees logic
            }
        }
        image = SwingFXUtils.toFXImage(newBufferedImage, null);
        iv.setImage(image);
        return image;
    }

    private ImageView rotate90DegreeIV(ImageView iv) {
        ImageView rotatedImageView = new ImageView(iv.getImage());
        rotatedImageView.setRotate(90);
        rotatedImageView.setFitHeight(iv.getFitWidth());
        rotatedImageView.setFitWidth(iv.getFitHeight());
        return rotatedImageView;
    }

    public static boolean compressAndSaveFile(BufferedImage inputImage, float compressionQuality, String fullPath){
        try {
            Thumbnails.of(inputImage)
                    .size(inputImage.getWidth(), inputImage.getHeight()).outputQuality(compressionQuality)
                    .toFile(fullPath);
            return true;
        } catch (IOException e) {
            System.err.println("Image compression and save Error!!!: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, Integer> compressImages(Map<String, Image> fileNameWithImageMap, String imgCompressionLevel, File directory) {
        float factor = calculatedImgCompression(imgCompressionLevel);
//        DirectoryChooser directoryChooser = new DirectoryChooser();
//        directoryChooser.setTitle("Choose folder");
//        File directory = directoryChooser.showDialog(stage);
        System.out.println(factor + " - " + directory.getAbsolutePath());
        String compressedImagePath = "";
        //Map<String, Boolean> successMap = new HashMap<>();
        int count = 0;
        for(Map.Entry<String, Image> entry: fileNameWithImageMap.entrySet()){
            Image image = entry.getValue();
            String fileNameWithPath = directory.getAbsolutePath() + File.separator + "Compressed-"+ entry.getKey()+".jpg";
            boolean status = compressAndSaveFile(SwingFXUtils.fromFXImage(image, null), factor, fileNameWithPath);
            //successMap.put(entry.getKey(), status);
            if(status){
                count++;
                compressedImagePath = fileNameWithPath;
            }
        }
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put(compressedImagePath, count);
        return resultMap;
//        showSuccessDialog(compressedImagePath);
//        System.out.println("Image compression and save Done.");
    }

    private static float calculatedImgCompression(String imgCompressionLevel) {
        float value = 0.8f;
        switch (imgCompressionLevel) {
            case "High":
                value = 0.20f;
                break;
            case "Medium":
                value = 0.50f;
                break;
            case "Low":
                value = 0.85f;
                break;
            default:
                break;
        }
        return value;
    }

    public static void showSuccessDialog(String path){
        if(path.isEmpty())
            return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("All files are compressed successfully.");

        ButtonType buttonTypeOne = new ButtonType("Show in folder");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            try {
                //Desktop.getDesktop().browse(new File(path).toURI()); //use this for java 9 or higher.
                Process exec = Runtime.getRuntime().exec("explorer /select,"+path);
            } catch (IOException ex) {
                Logger.getLogger(ImageService.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Could not open file in file explorer!");
            }
        }
    }
}

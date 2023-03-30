package com.tareq.imagetopdf;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

}

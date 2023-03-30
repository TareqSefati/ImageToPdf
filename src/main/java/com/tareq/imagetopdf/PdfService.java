package com.tareq.imagetopdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class PdfService {

    public static void imagesToPdf(List<Image> images, Stage stage) throws IOException, DocumentException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            String fileName = directory.getAbsolutePath() + "/images1.pdf";
            Document document = new Document(PageSize.A4);
            System.out.println("A4 document P size: " + document.getPageSize().toString());
            document.setPageSize(PageSize.A4.rotate());
            System.out.println("A4 document L size: " + document.getPageSize().toString());
            document.setMargins(20f, 20f, 20f, 20f);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            float imgPosX = 0;
            float imgPosY = 0;
            for (Image image : images) {
                java.awt.Image awtImage = SwingFXUtils.fromFXImage(image, null);
                com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(awtImage, null);


                //----------------------works good for protraint mode 
                itextImage.scaleToFit(PageSize.A4.getWidth() - document.leftMargin() - document.rightMargin(),
                        PageSize.A4.getHeight() - document.topMargin() - document.bottomMargin());
                imgPosX = (PageSize.A4.getWidth() - itextImage.getScaledWidth()) / 2;
                imgPosY = (PageSize.A4.getHeight() - itextImage.getScaledHeight()) / 2;
                itextImage.setAbsolutePosition(imgPosX, imgPosY);
                document.newPage();
                document.add(itextImage);
                //^^^^^^^^^^^^^^^^^ works good for protraint mode 

                //----------------------works good for Landscape mode 
                itextImage.scaleToFit(PageSize.A4.rotate().getWidth()-document.leftMargin()-document.rightMargin(),PageSize.A4.rotate().getHeight()-document.topMargin()-document.bottomMargin());
                imgPosX = (PageSize.A4.rotate().getWidth() - itextImage.getScaledWidth()) / 2;
                imgPosY = (PageSize.A4.rotate().getHeight() - itextImage.getScaledHeight()) / 2;
                itextImage.setAbsolutePosition(imgPosX, imgPosY);
                document.newPage();
                document.add(itextImage);
                //----------------------works good for Landscape mode 
            }
            document.close();
        }
    }
}

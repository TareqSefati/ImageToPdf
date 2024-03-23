package com.tareq.imagetopdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfService {

    public static String imagesToPdf(List<Image> images, File directory, String pageSize, String pageOrientation, String compression) {
        //Todo: save the images as pdf in protrait and landscape mode.
        if (directory != null) {
            String fileName = directory.getAbsolutePath();
            String tempFileName = directory.getParent() + File.separator + "temp_" + directory.getName();
            System.out.println("Tempurary file name: " + tempFileName);
            Document document = new Document(PageSize.A4); //Default page size is set to A4
            System.out.println("Default page size: A4");
            if (pageSize.equals("Letter")) {
                document.setPageSize(PageSize.LETTER);
                System.out.println("Page size is set to: LEETTER");
            } else if (pageSize.equals("Legal")) {
                document.setPageSize(PageSize.LEGAL);
                System.out.println("Page size is set to: LEGAL");
            }
            if (pageOrientation.equals("Landscape")) {
                System.out.println("Page orientatin is set to: Landscape");
                document.setPageSize(document.getPageSize().rotate());
            }
            document.setMargins(15f, 15f, 15f, 15f);
            float compressionFactor = 0;
            try {
                PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(tempFileName));
                pdfWriter.setFullCompression();
//            PdfWriter.getInstance(document, new FileOutputStream(fileName)).setCompressionLevel(9);
                compressionFactor = 0.8f;
                switch (compression) {
                    case "High":
                        compressionFactor = 0.20f;
                        break;
                    case "Medium":
                        compressionFactor = 0.50f;
                        break;
                    case "Low":
                        compressionFactor = 0.85f;
                        break;
                    default:
                        break;
                }
                document.open();
                float imgPosX = 0;
                float imgPosY = 0;
                for (Image image : images) {
                    java.awt.Image awtImage = SwingFXUtils.fromFXImage(image, null);
                    com.itextpdf.text.Image itextImage = com.itextpdf.text.Image.getInstance(awtImage, null);
                    itextImage.scaleToFit(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin(),
                            document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin());
                    imgPosX = (document.getPageSize().getWidth() - itextImage.getScaledWidth()) / 2;
                    imgPosY = (document.getPageSize().getHeight() - itextImage.getScaledHeight()) / 2;
                    itextImage.setAbsolutePosition(imgPosX, imgPosY);
                    document.newPage();
                    document.add(itextImage);
                }
                document.close();
            } catch (DocumentException | IOException e) {
                return ResponseStatus.ERROR.name();
            }
            compressPdfFile(tempFileName, fileName, compressionFactor);
            return fileName;
        }
        return ResponseStatus.FAIL.name();
    }

    private static void compressPdfFile(String src, String dest, float factor) {
        try {
            // Read the file
            PdfReader reader = new PdfReader(src);
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;
            // Look for image and manipulate image stream
            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isStream()) {
                    continue;
                }
                stream = (PRStream) object;
                // if (value.equals(stream.get(key))) {
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                System.out.println(stream.type());
                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    PdfImageObject image = new PdfImageObject(stream);
                    BufferedImage bi = image.getBufferedImage();
                    if (bi == null) {
                        continue;
                    }
                    int width = (int) (bi.getWidth() * factor);
                    int height = (int) (bi.getHeight() * factor);
                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    AffineTransform at = AffineTransform.getScaleInstance(factor, factor);
                    Graphics2D g = img.createGraphics();
                    g.drawRenderedImage(bi, at);
                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                    ImageIO.write(img, "JPG", imgBytes);
                    stream.clear();
                    stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                    stream.put(PdfName.TYPE, PdfName.XOBJECT);
                    stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                    stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                    stream.put(PdfName.WIDTH, new PdfNumber(width));
                    stream.put(PdfName.HEIGHT, new PdfNumber(height));
                    stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                }
            }
            // Save altered PDF
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
            stamper.close();
            reader.close();
            new File(src).delete();
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(PdfService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void showSuccessDialog(String path){
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("PDF file is created successfully.");

        ButtonType buttonTypeOne = new ButtonType("Show in folder");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            try {
                //Desktop.getDesktop().browse(new File(path).toURI()); //use this for java 9 or higher.
                Process exec = Runtime.getRuntime().exec("explorer /select,"+path);
            } catch (IOException ex) {
                Logger.getLogger(PdfService.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Could not open file in file explorer!");
            }
        } 
    }
}

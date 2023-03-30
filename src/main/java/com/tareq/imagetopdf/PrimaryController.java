package com.tareq.imagetopdf;

import com.itextpdf.text.DocumentException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PrimaryController {

    private List<File> imageFileList = new ArrayList<>();
    private int tileCounter = 0;

    @FXML
    private Button btnOpenFiles;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TilePane tilePane;
    @FXML
    private Text imageLabel;
    @FXML
    private Button btnConvertPdf;
    @FXML
    private ImageView testImageView;

    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void openFiles(ActionEvent event) {
        //choose the file/s from directory
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(fileExtensions);
        imageFileList = fileChooser.showOpenMultipleDialog(((Node) event.getSource()).getScene().getWindow());
        if (imageFileList != null) {
            generateTiles();
        } else {
            imageLabel.setVisible(true);
            imageLabel.setText("No file is selected!!!");
        }

    }

    //generating tiles from image file list
    private void generateTiles() {
        for (int i = 0; i < imageFileList.size(); i++) {
            //prepare a Tile with image and its detais.
            Image im = new Image(imageFileList.get(i).toURI().toString());
            ImageView imageView = new ImageView(im);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(120);
            imageView.setFitHeight(120);
            Label label = new Label();
            label.setPrefWidth(100);
            label.setTextAlignment(TextAlignment.CENTER);
            label.setText(imageFileList.get(i).getName());
            label.setStyle(" -fx-background-color: white;");
            Tooltip tooltip = new Tooltip(label.getText());
            tooltip.setWrapText(true);
            label.setTooltip(tooltip);
            VBox vBox = new VBox(5);
            vBox.setAlignment(Pos.CENTER);
            vBox.setId(String.valueOf(tileCounter++));

            Button btnRotate = new Button("R");
            btnRotate.setOnAction((e) -> {
                Image rotatedImage = ImageService.rotate90Deg(imageView);
                testImageView.setImage(rotatedImage);
            });
            Button btnDelete = new Button("D");

            HBox hBox = new HBox(5, btnRotate, btnDelete);
            hBox.setVisible(false);
            StackPane stackPane = new StackPane(imageView, hBox);

            vBox.getChildren().addAll(stackPane, label);
            tilePane.getChildren().add(vBox);

            vBox.setOnMouseEntered((e) -> {
                imageLabel.setText(label.getText());
                imageLabel.setTextAlignment(TextAlignment.CENTER);
                imageLabel.setVisible(true);
                hBox.setVisible(true);
            });
            vBox.setOnMouseExited((e) -> {
                imageLabel.setVisible(false);
                imageLabel.setText("");
                hBox.setVisible(false);
            });
            vBox.setOnDragDetected((e) -> {
                System.out.println("Trying to drag this tile with id: " + vBox.getId());
                Dragboard db = vBox.startDragAndDrop(TransferMode.ANY);
                ClipboardContent cb = new ClipboardContent();
                cb.putString(vBox.getId());
                db.setContent(cb);
                e.consume();
            });
            vBox.setOnDragOver((e) -> {
                if (e.getDragboard().hasString()) {
                    e.acceptTransferModes(TransferMode.ANY);
                }
            });
            vBox.setOnDragDropped((e) -> {
                String sourceId = e.getDragboard().getString();
                System.out.println("Source Tile id: " + sourceId);
                System.out.println("Destination Tile id: " + vBox.getId());
                //Here do the tile swaping or tile move operation
                if (!sourceId.equals(vBox.getId())) {
                    rearrangeTiles(tilePane, sourceId, vBox.getId());
                } else {
                    System.out.println("Source & destination tiles are same. No rearrange!!!");
                }
            });
            vBox.setOnDragDone((e) -> {
                System.out.println("Drag is done.");
                System.out.println("------------------- -!!!!!!!!!!!!!--------------------\n");
            });
            btnDelete.setOnAction((ActionEvent e) -> {
                tilePane.getChildren().remove(vBox);
            });
        }
    }

    private void rearrangeTiles(TilePane workingTilePane, String sourceId, String destinationId) {
        ObservableList<Node> tileList = workingTilePane.getChildren();
        System.out.println("Initializing the Tiles rearrange.....");
        System.out.println("Total number of tiles: " + tileList.size());
        System.out.println("Source tile id: " + sourceId + "\nDestinatio tile id: " + destinationId);

        int sourceIndex = 0, destinationIndex = 0;
        for (int i = 0; i < tileList.size(); i++) {
            if (sourceId.equals(tileList.get(i).getId())) {
                sourceIndex = i;
            }
            if (destinationId.equals(tileList.get(i).getId())) {
                destinationIndex = i;
            }
        }
        if (sourceIndex != destinationIndex) {
            Node n = tileList.remove(sourceIndex);
            if (destinationIndex > (tileList.size() - 1)) {
                System.out.println("Adding tile at last index.");
                tileList.add(n);
            } else {
                System.out.println("Adding tile within the range of index.");
                tileList.add(destinationIndex, n);
            }
        }
        tilePane = workingTilePane;
    }

    @FXML
    private void convertToPdf(ActionEvent event) {
        System.out.println("Convert to pdf-----------------------");
        List<Image> imageList = new ArrayList<>();
        for (int i = 0; i < tilePane.getChildren().size(); i++) {
            VBox vBox = (VBox) tilePane.getChildren().get(i);
            StackPane stackPane = (StackPane) vBox.getChildren().get(0);
            ImageView imageView = (ImageView) stackPane.getChildren().get(0);
            imageList.add(imageView.getImage());
        }

        System.out.println("Total number of image: " + imageList.size());
        for (Image image : imageList) {
            System.out.println("Image height: " + image.getHeight() + "--- Image width: " + image.getWidth());
        }

        try {
            PdfService.imagesToPdf(imageList, ((Stage) ((Node) event.getSource()).getScene().getWindow()));
            System.out.println("PDF file is created successfully.");
        } catch (IOException | DocumentException ex) {
            System.out.println("Unable to create PDF file!!!!!!!!!!!!!");
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ImageView rotate90Degree(ImageView iv) {
        ImageView rotatedImageView = new ImageView(iv.getImage());
        rotatedImageView.setRotate(90);
        rotatedImageView.setFitHeight(iv.getFitWidth());
        rotatedImageView.setFitWidth(iv.getFitHeight());
        return rotatedImageView;
    }
}

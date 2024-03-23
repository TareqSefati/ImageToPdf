package com.tareq.imagetopdf;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimaryController {

    private List<File> imageFileList = new ArrayList<>();
    private Map<String, Image> fileNameWithImageMap = new HashMap<>();
    private int tileCounter = 0;

    @FXML
    private Button btnOpenFiles;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TilePane tilePane;
    @FXML
    private Button btnConvertPdf;
    @FXML
    private ImageView testImageView;
    @FXML
    private ToggleGroup pageSize;
    @FXML
    private ToggleGroup pageOrientation;
    @FXML
    private ToggleGroup compressionFactor;
    @FXML
    private ToggleGroup imgCompressionFactor;
    @FXML
    private Button btnCompressImg;
    @FXML
    private Button btnClearImage;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Text activityStatus;

    @FXML
    private void openFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(fileExtensions);
        imageFileList = fileChooser.showOpenMultipleDialog(((Node) event.getSource()).getScene().getWindow());
        if (imageFileList != null) {
            generateTiles();
        } else {
            activityStatus.setVisible(true);
            activityStatus.setText("No file is selected!!!");
        }

    }

    //generating tiles from image file list
    private void generateTiles() {
        btnOpenFiles.setDisable(true);
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                progressBar.setVisible(true);
                activityStatus.setText("Loading files...");
                for (int i = 0; i < imageFileList.size(); i++) {
                    Thread.sleep(50);
                    int index = i;
                    Platform.runLater(() -> {
                        if (!fileNameWithImageMap.containsKey(imageFileList.get(index).getName())) {
                            Image im = new Image(imageFileList.get(index).toURI().toString());
                            ImageView imageView = new ImageView(im);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(140);
                            imageView.setFitHeight(140);
                            Label label = new Label();
                            label.setPrefWidth(100);
                            label.setTextAlignment(TextAlignment.CENTER);
                            label.setText(imageFileList.get(index).getName());
                            label.setStyle(" -fx-background-color: white;");
                            Tooltip tooltip = new Tooltip(label.getText());
                            tooltip.setWrapText(true);
                            label.setTooltip(tooltip);
                            VBox vBox = new VBox(5);
                            vBox.setAlignment(Pos.CENTER);
                            vBox.setId(String.valueOf(tileCounter++));
                            Button btnRotate = new Button();
                            ImageView rotateIconIV = new ImageView(new Image(this.getClass().getResourceAsStream("/images/rotate.png")));
                            rotateIconIV.setFitHeight(15);
                            rotateIconIV.setFitWidth(15);
                            btnRotate.setGraphic(rotateIconIV);
                            btnRotate.setOnAction((e) -> {
                                Image rotatedImage = ImageService.rotate90Deg(imageView);
                            });
                            Button btnDelete = new Button();
                            ImageView deleteIconIV = new ImageView(new Image(this.getClass().getResourceAsStream("/images/close.png")));
                            deleteIconIV.setFitHeight(15);
                            deleteIconIV.setFitWidth(15);
                            btnDelete.setGraphic(deleteIconIV);
                            HBox hBox = new HBox(5, btnRotate, btnDelete);
                            hBox.setVisible(false);
                            StackPane stackPane = new StackPane(imageView, hBox);
                            vBox.getChildren().addAll(stackPane, label);
                            fileNameWithImageMap.put(label.getText(), imageView.getImage());
                            tilePane.getChildren().add(vBox);
                            vBox.setOnMouseEntered((e) -> {
                                hBox.setVisible(true);
                                vBox.setCursor(Cursor.HAND);
                                vBox.setOpacity(0.7);
                            });
                            vBox.setOnMouseExited((e) -> {
                                hBox.setVisible(false);
                                vBox.setCursor(Cursor.DEFAULT);
                                vBox.setOpacity(1.0);
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
                                //Here do the tile swapping or tile move operation
                                if (!sourceId.equals(vBox.getId())) {
                                    rearrangeTiles(tilePane, sourceId, vBox.getId());
                                } else {
                                    System.out.println("Source & destination tiles are same. No rearrange!!!");
                                }
                            });
                            vBox.setOnDragDone((e) -> {
                                System.out.println("Drag is done.");
                            });
                            btnDelete.setOnAction((ActionEvent e) -> {
                                tilePane.getChildren().remove(vBox);
                                fileNameWithImageMap.remove(((Label) vBox.getChildren().get(1)).getText());
                            });
                        }
                    });
                    updateProgress(i, imageFileList.size());
                    updateValue(i);
                }
                return imageFileList.size();
            }
            @Override
            protected void failed() {
                super.failed();
                progressBar.setVisible(false);
                btnOpenFiles.setDisable(false);
                activityStatus.setText("Open files - Failed!");
            }
            @Override
            protected void succeeded() {
                super.succeeded();
                progressBar.setVisible(false);
                btnOpenFiles.setDisable(false);
                activityStatus.setText("Success! - "+ getValue() +"/"+imageFileList.size());
            }
        };

        task.valueProperty().addListener((observable, oldValue, newValue) -> {
            activityStatus.setText("Loading files - "+newValue+"/"+imageFileList.size());
        });
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
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
        List<Image> imageList = getTilePaneImages();
        if(imageList.isEmpty()){
            activityStatus.setText("No Image selected!");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Pdf");
        fileChooser.setInitialFileName("test.pdf");
        File directory = fileChooser.showSaveDialog(((Stage) ((Node) event.getSource()).getScene().getWindow()));
        if(directory == null){
            return;
        }
        btnConvertPdf.setDisable(true);
        activityStatus.setText("Choose directory...");
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                progressBar.setVisible(true);
                activityStatus.setText("Converting to pdf...");

                String size = ((RadioButton)pageSize.getSelectedToggle()).getText();
                String orientation = ((RadioButton)pageOrientation.getSelectedToggle()).getText();
                String compression = ((RadioButton)compressionFactor.getSelectedToggle()).getText();
                System.out.println("page size is: "+size);
                System.out.println("page orientation is: "+orientation);
                System.out.println("File compression is: "+compression);
                return PdfService.imagesToPdf(imageList, directory, size, orientation, compression);
            }
            @Override
            protected void failed() {
                super.failed();
                btnConvertPdf.setDisable(false);
                progressBar.setVisible(false);
                activityStatus.setText("Pdf conversion failed!");
            }
            @Override
            protected void succeeded() {
                super.succeeded();
                btnConvertPdf.setDisable(false);
                progressBar.setVisible(false);
                if(getValue().equals(ResponseStatus.FAIL.name()) || getValue().equals(ResponseStatus.ERROR.name())){
                    activityStatus.setText("Pdf service failed!");
                }else{
                    activityStatus.setText("Pdf conversion Succeed!");
                    PdfService.showSuccessDialog(getValue());
                }
            }
        };
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private List<Image> getTilePaneImages() {
        List<Image> imageList = new ArrayList<>();
        for (int i = 0; i < tilePane.getChildren().size(); i++) {
            VBox vBox = (VBox) tilePane.getChildren().get(i);
            StackPane stackPane = (StackPane) vBox.getChildren().get(0);
            ImageView imageView = (ImageView) stackPane.getChildren().get(0);
            imageList.add(imageView.getImage());
        }
        return imageList;
    }

    @FXML
    private void compressImg(ActionEvent event) {
        List<Image> imageList = getTilePaneImages();
        if(imageList.isEmpty()){
            activityStatus.setText("No Image selected!");
            return;
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose folder");
        File directory = directoryChooser.showDialog((Stage) ((Node) event.getSource()).getScene().getWindow());
        if(directory == null){
            return;
        }
        btnCompressImg.setDisable(true);
        activityStatus.setText("Choose directory...");
        Task<Map<String, Integer>> task = new Task<Map<String, Integer>>() {
            @Override
            protected Map<String, Integer> call() throws Exception {
                progressBar.setVisible(true);
                activityStatus.setText("Compressing Image...");
                String imgCompressionLevel = ((RadioButton) imgCompressionFactor.getSelectedToggle()).getText();
                Map<String, Integer> resultMap = ImageService.compressImages(fileNameWithImageMap, imgCompressionLevel, directory);
                return resultMap;
            }
            @Override
            protected void failed() {
                super.failed();
                btnCompressImg.setDisable(false);
                progressBar.setVisible(false);
                activityStatus.setText("Image compression failed!");
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                btnCompressImg.setDisable(false);
                progressBar.setVisible(false);
                Map<String, Integer> resultMap = getValue();
                Map.Entry<String, Integer> entry = resultMap.entrySet().iterator().next();
                activityStatus.setText(entry.getValue() + " Image(s) compressed!");
                ImageService.showSuccessDialog(entry.getKey());
            }

        };

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void clearSelectedImage(ActionEvent event) {
        tilePane.getChildren().clear();
        fileNameWithImageMap.clear();
        activityStatus.setText("Image Tile Cleared.");
    }

    @FXML
    void showAbout(MouseEvent event) throws IOException {
        System.out.println("show about page");
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("about.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("About");
        stage.setResizable(false);
        stage.sizeToScene();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/images/tsp-rounded-logo.png")));
        stage.show();
    }
}

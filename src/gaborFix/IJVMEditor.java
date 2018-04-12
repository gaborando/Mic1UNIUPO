package gaborFix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reactfx.Subscription;

public class IJVMEditor extends Application {


    private static final String RTFX_FILE_EXTENSION = ".jas";
    private MyCodeArea codeArea;
    private Scene scene;
    private Stage priamryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.priamryStage = primaryStage;

        codeArea = new MyCodeArea();

        Button loadBtn = createButton("loadfile", this::loadDocument,
                "Load document.\n\n" +
                        "Note: the demo will load only previously-saved \"" + RTFX_FILE_EXTENSION + "\" files. " +
                        "This file format is abitrary and may change across versions.", "folder-open.png");
        Button saveBtn = createButton("savefile", this::saveDocument,
                "Save document.\n\n" +
                        "Note: the demo will save the area's content to a \"" + RTFX_FILE_EXTENSION + "\" file. " +
                        "This file format is abitrary and may change across versions.","content-save.png");

        Button undoBtn = createButton("undo", codeArea::undo, "Undo","undo-variant.png");
        Button redoBtn = createButton("redo", codeArea::redo, "Redo","redo-variant.png");
        Button cutBtn = createButton("cut", codeArea::cut, "Cut","content-cut.png");
        Button copyBtn = createButton("copy", codeArea::copy, "Copy","content-copy.png");
        Button pasteBtn = createButton("paste", codeArea::paste, "Paste", "content-paste.png");

        ToolBar toolBar1 = new ToolBar(
                loadBtn, saveBtn,  new Separator(Orientation.VERTICAL),
                undoBtn, redoBtn, new Separator(Orientation.VERTICAL),
                cutBtn, copyBtn, pasteBtn, new Separator(Orientation.VERTICAL));

        VBox vbox = new VBox();
        VBox.setVgrow(codeArea, Priority.ALWAYS);
        vbox.getChildren().addAll(toolBar1, codeArea);

        scene = new Scene(vbox, 600, 400);
        scene.getStylesheets().add(IJVMEditor.class.getResource("../ijvm-keywords.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("IJVM Editor");
        primaryStage.show();
    }

    private Button createButton(String styleClass, Runnable action, String toolTip, String image) {
        Button button = new Button();
        button.getStyleClass().add(styleClass);
        button.setOnAction(evt -> {
            action.run();
            codeArea.requestFocus();
        });
        button.setMaxWidth(12);
        button.setMaxHeight(12);
        if (toolTip != null) {
            button.setTooltip(new Tooltip(toolTip));
        }
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("../"+image)));
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        button.setGraphic(imageView);
        return button;
    }

    private void loadDocument() {
        String initialDir = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load document");
        fileChooser.setInitialDirectory(new File(initialDir));
        fileChooser.setSelectedExtensionFilter(
                new FileChooser.ExtensionFilter("JAS file", "*" + RTFX_FILE_EXTENSION));
        File selectedFile = fileChooser.showOpenDialog(priamryStage);
        if (selectedFile != null) {
            codeArea.clear();

            try {
                InputStream is = new FileInputStream(selectedFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));

                String line = buf.readLine();


                while (line != null) {
                    codeArea.appendText(line);
                    codeArea.appendText("\n");
                    line = buf.readLine();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    private void saveDocument() {
        String initialDir = System.getProperty("user.dir");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save document");
        fileChooser.setInitialDirectory(new File(initialDir));
        fileChooser.setInitialFileName("example rtfx file" + RTFX_FILE_EXTENSION);
        File selectedFile = fileChooser.showSaveDialog(priamryStage);
        if (selectedFile != null) {
            try (PrintWriter out = new PrintWriter(selectedFile)) {
                out.println(codeArea.getText());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}

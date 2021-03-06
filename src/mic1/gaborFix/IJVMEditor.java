package mic1.gaborFix;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mic1.IJVMAssembler;
import mic1.mic1sim;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.Stream;

public class IJVMEditor extends RememberPositionJFrame
{
	private static IJVMCodeArea codeArea;
	private final JFXPanel fxPanel;

	private static final String RTFX_FILE_EXTENSION = "jas";
	private Scene scene;
	private BuilderProgramHandler builderProgramHandler;
	private File currentFile = null;
	private Label msg;
	private PrintStream err;
	private TextArea errConsole;
	private HashMap<Integer, Integer> debugMapping = null;
	private HashMap<Integer, Integer> reverseDebugMapping = null;
	private Vector breakpoint_vector;

	public IJVMEditor(BuilderProgramHandler builderProgramHandler) throws HeadlessException
	{
		super("IJVM Editor");
		fxPanel = new JFXPanel();
		add(fxPanel);
		setSize(getLastSize());
		setVisible(true);
		//setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Scene scene = createScene();
		fxPanel.setScene(scene);
		this.builderProgramHandler = builderProgramHandler;
		msg = new Label();

		Platform.runLater(() -> {
			String lastMacroprogramm = prefs.get("last_macroprogram", null);
			if (lastMacroprogramm != null && new File(lastMacroprogramm).exists())
			{
				load(new File(lastMacroprogramm));
				buildAndLoad();
			}
		});


	}


	private Scene createScene()
	{
		codeArea = new IJVMCodeArea(this::handleBreakpoint);

		Button newBtn = createButton("newfile", this::newfile,
				"New document.\n\n" +
						"Note: the demo will load only previously-saved \"" + RTFX_FILE_EXTENSION + "\" files. " +
						"This file format is abitrary and may change across versions.", "file-empty.png");
		Button loadBtn = createButton("loadfile", this::loadDocument,
				"Load document.\n\n" +
						"Note: the demo will load only previously-saved \"" + RTFX_FILE_EXTENSION + "\" files. " +
						"This file format is abitrary and may change across versions.", "folder-open.png");
		Button saveBtn = createButton("savefile", this::save,
				"Save document.\n\n" +
						"Note: the demo will save the area's content to a \"" + RTFX_FILE_EXTENSION + "\" file. " +
						"This file format is abitrary and may change across versions.", "content-save.png");

		Button undoBtn = createButton("undo", codeArea::undo, "Undo", "undo-variant.png");
		Button redoBtn = createButton("redo", codeArea::redo, "Redo", "redo-variant.png");
		Button cutBtn = createButton("cut", codeArea::cut, "Cut", "content-cut.png");
		Button copyBtn = createButton("copy", codeArea::copy, "Copy", "content-copy.png");
		Button pasteBtn = createButton("paste", codeArea::paste, "Paste", "content-paste.png");

		Button build = createButton("build", this::compile, "Build", "wrench.png");
		Button load = createButton("build_load", this::buildAndLoad, "Build & Load", "arrow-right-bold.png");

		ToolBar toolBar1 = new ToolBar(newBtn,
				loadBtn, saveBtn, new Separator(Orientation.VERTICAL),
				undoBtn, redoBtn, new Separator(Orientation.VERTICAL),
				cutBtn, copyBtn, pasteBtn, new Separator(Orientation.VERTICAL),
				build, load, new Separator(Orientation.VERTICAL));

		errConsole = new TextArea();
		errConsole.setEditable(false);
		err = new PrintStream(new FxTextAreaOutputStream(errConsole));

		VBox vbox = new VBox();
		VBox.setVgrow(codeArea, Priority.ALWAYS);
		vbox.getChildren().addAll(toolBar1, codeArea, errConsole);

		scene = new Scene(vbox, 600, 400);
		scene.getStylesheets().add(IJVMEditor.class.getResource("ijvm-keywords.css").toExternalForm());

		return (scene);
	}

	private Integer handleBreakpoint(Integer integer)
	{


		if (breakpoint_vector != null && reverseDebugMapping != null)
		{
			int address = reverseDebugMapping.getOrDefault(integer + 1, -1);
			if (integer < 0) // se l'indice è negativo controlla semplicemente che sia presente
			{
				if( breakpoint_vector.contains(String.valueOf(reverseDebugMapping.getOrDefault(-integer + 1, -1))))
				{
					errConsole.appendText("\nBREACKPOINT ADDED: Line: "+(-integer+1)+" Address: 0x"+Integer.toHexString(address));
					return reverseDebugMapping.getOrDefault(-integer + 1, -1);
				}
				return -1;
			}

			if (address == -1)
				return -1;

			if (breakpoint_vector.contains(String.valueOf(address)))
			{
				breakpoint_vector.remove(String.valueOf(address));
				mic1sim.Breakpoint = breakpoint_vector.size() > 0;
				return -1;
			} else
			{
				breakpoint_vector.add(String.valueOf(address));
				if(mic1sim.Breakpoint = breakpoint_vector.size() > 0){
					errConsole.appendText("\nBREACKPOINT ADDED: Line: "+(integer+1)+" Address: 0x"+Integer.toHexString(address));
					return address;
				}
				return -1;
			}
		}
		mic1sim.Breakpoint = false;
		return -1;

	}

	private void newfile()
	{
		codeArea.clear();
		currentFile = null;

	}

	private void buildAndLoad()
	{
		try
		{
			SwingUtilities.invokeLater(() ->
					builderProgramHandler.handle(compile())
			);
		} catch (Exception ignored)
		{

		}
	}

	private String compile()
	{
		save();
		if (currentFile == null)
			return null;
		errConsole.clear();
		InputStream in = null;
		OutputStream out = null;
		IJVMAssembler ia = null;
		String infile = currentFile.getPath();
		String outfile = infile.substring(0, infile.length() - 4) + ".ijvm";
		try
		{
			in = new BufferedInputStream(new FileInputStream(currentFile));
		} catch (FileNotFoundException fnfe)
		{
			err.println("File not found: " + infile + ", unable to compile");
			return null;
		} catch (Exception ex)
		{
			err.println("Error opening file: " + infile + ", unable to compile");
			return null;
		}
		try
		{
			out = new FileOutputStream(outfile);
		} catch (IOException ioe)
		{
			err.println("Error opening file: " + outfile + ", unable to compile");
			return null;
		}
		err.println("Compiling " + infile + "...");

		try
		{
			ia = new IJVMAssembler(in, out, outfile, err, Files.exists(Paths.get("ijvm.conf")) ? "ijvm.conf" : getClass().getResource("../ijvm.conf").getFile());
			in.close();
			out.close();
		} catch (Exception e)
		{
			err.println(e.getMessage());
			return outfile;
		}
		if (ia.getStatus())
		{
			err.println(" Compilation successfull");
		} else
		{
			err.println("Error compiling file: " + infile);
		}
		loadDebugMapping();

		return outfile;
	}

	private void loadDebugMapping()
	{
		debugMapping = new HashMap<>();
		reverseDebugMapping = new HashMap<>();
		String outfile = currentFile.getPath().substring(0, currentFile.getPath().length() - 4) + ".dbg";
		try (Stream<String> stream = Files.lines(Paths.get(outfile)))
		{
			stream.forEach(line -> {
				Scanner scanner = new Scanner(line);
				scanner.useDelimiter(",");
				int a = scanner.nextInt();
				int b = scanner.nextInt();
				debugMapping.put(a, b);
				reverseDebugMapping.put(b, a);
			});
		} catch (IOException e)
		{
			debugMapping = null;
		}
	}


	private Button createButton(String styleClass, Runnable action, String toolTip, String image)
	{
		Button button = new Button();
		button.getStyleClass().add(styleClass);
		button.setOnAction(evt -> {
			action.run();
			codeArea.requestFocus();
		});
		button.setMaxWidth(12);
		button.setMaxHeight(12);
		if (toolTip != null)
		{
			button.setTooltip(new Tooltip(toolTip));
		}
		ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(image)));
		imageView.setFitHeight(20);
		imageView.setFitWidth(20);
		button.setGraphic(imageView);
		return button;
	}

	private void loadDocument()
	{
		//FileDialog fd = new FileDialog(this, "Load Macroprogram", FileDialog.LOAD);
		//fd.setFile("*.jas");
		//fd.setVisible(true);
		// fd.paintAll(fd.getGraphics());

		String initialDir = System.getProperty("user.dir");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open document");
		fileChooser.setInitialDirectory(currentFile != null ? currentFile.getParentFile() : new File(initialDir));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAS FILE", "*." + RTFX_FILE_EXTENSION));
		File selectedFile = fileChooser.showOpenDialog(null);
		//if (fd.getFile() != null) {
		//	selectedFile = new File(fd.getDirectory() + fd.getFile());
		//}

		load(selectedFile);
	}

	private void save()
	{
		if (currentFile == null)
			saveDocument();
		else
			save(currentFile);
	}

	private void saveDocument()
	{
		//FileDialog fd = new FileDialog(this, "Save Macroprogram", FileDialog.SAVE);
		//fd.setFile("*.jas");
		//fd.setVisible(true);
		String initialDir = System.getProperty("user.dir");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save document");
		fileChooser.setInitialDirectory(currentFile != null ? currentFile.getParentFile() : new File(initialDir));
		fileChooser.setInitialFileName("program." + RTFX_FILE_EXTENSION);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAS FILE", "*." + RTFX_FILE_EXTENSION));
		File selectedFile = fileChooser.showSaveDialog(null);
		//if (fd.getFile() != null) {
		//		selectedFile = new File(fd.getDirectory() + fd.getFile());
		//}

		save(selectedFile);

	}

	private void save(File selectedFile)
	{
		if (selectedFile != null)
		{
			try (PrintWriter out = new PrintWriter(selectedFile))
			{
				out.println(codeArea.getText());
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			prefs.put("last_macroprogram", selectedFile.getPath());
			currentFile = selectedFile;
		}
	}

	private void load(File selectedFile)
	{
		if (selectedFile != null)
		{
			codeArea.clear();

			try
			{
				InputStream is = new FileInputStream(selectedFile);
				BufferedReader buf = new BufferedReader(new InputStreamReader(is));

				String line = buf.readLine();


				while (line != null)
				{
					codeArea.appendText(line);
					codeArea.appendText("\n");
					line = buf.readLine();
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			currentFile = selectedFile;
			prefs.put("last_macroprogram", selectedFile.getPath());

		}
	}

	public HashMap<Integer, Integer> getDebugMapping()
	{
		return debugMapping;
	}

	public void debug(int value)
	{

		Platform.runLater(

				() -> {
					if (debugMapping != null)
					{
						int line = debugMapping.getOrDefault(value, -1);
						if (line < 0)
							return;
						int startPos = codeArea.position(line - 1, 0).toOffset();
						int endPos = codeArea.position(line, 0).toOffset();
						codeArea.selectRange(startPos, endPos);
						Bounds bounds = codeArea.getSelectionBounds().orElse(null);
						if (bounds == null)
						{

							int i = 10;
							while (line - i < 0)
								i++;
							codeArea.showParagraphAtTop(line - 5);
						}
					}
					//codeArea.setStyle(debugMapping.getOrDefault(value,0), Collections.singleton("-fx-highlight-fill: lightgray; -fx-highlight-text-fill: firebrick;"));
					//	codeArea.setParagraphStyle(12, Collections.singleton("-fx-background-color: red;"));
				}
		);

		//	codeArea.appendText("ciao\n");
	}

	public void setBreakPointVector(Vector breakpoint_vector)
	{
		this.breakpoint_vector = breakpoint_vector;
	}
}

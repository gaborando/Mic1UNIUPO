package mic1.gaborFix;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IJVMCodeArea extends CodeArea {
    /**
     * The existing autocomplete entries.
     */
    private final SortedSet<String> entries;

    private static final String[] KEYWORDS = new String[]{
            "BIPUSH", "DUP", "GOTO", "IADD", "IAND", "IFEQ", "IFLT", "IF_ICMPEQ", "IINC", "ILOAD", "INVOKEVIRTUAL", "IOR", "IRETURN", "ISTORE", "ISUB", "LDC_W", "NOP", "POP", "SWAP", "WIDE ILOAD", "WIDE ISTORE", "OUT", "HALT", "IN"
    };

    private static String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static String COMMENT_PATTERN = "[/]{2}.*";
    private static String LABEL_PATTERN = "(?m)^((?![/.]).)*:";
    private static String DECLARATION_PATTERN = "(?m)^[^\\S\\n]*?[.].+?((?=[/:])|$)";
    private  static Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<LABEL>" + LABEL_PATTERN + ")"
                    + "|(?<DECLARATION>" + DECLARATION_PATTERN + ")"
    );
    /**
     * The popup used to select an entry.
     */
    private final Pattern lastWordPattern = Pattern.compile("([^ \\t\\n][a-zA-Z0-9_:]+)$");
    private final Pattern firstWordPattern = Pattern.compile("([^ \t]\\w+)");
    private LinkedList<String> searchResult = new LinkedList<>();

    public IJVMCodeArea() {
        super();
        entries = new TreeSet<>();
        final Popup popup = new Popup();
        popup.setAutoHide(true);
        VBox suggestionBox = new VBox();
        suggestionBox.setStyle("-fx-background-colorr: white;");
        popup.getContent().add(suggestionBox);
        setEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                int paragraph = getCurrentParagraph();
                if (event.getCode() == KeyCode.ENTER)
                    paragraph--;
                Matcher m = lastWordPattern.matcher(getText(paragraph));
                String lastWord = "";
                if (m.find())
                    lastWord = m.group();
                m = firstWordPattern.matcher(getText(paragraph));
                String first = "";
                if (m.find())
                    first = m.group();
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {

                    if (event.getCode() == KeyCode.ENTER) {
                        if (searchResult.size() > 0 && searchResult.getFirst().length() != lastWord.length()) {
                            insertText(getCaretPosition() - 1, searchResult.getFirst().substring(lastWord.length()));
                            deleteNextChar();
                            popup.hide();
                            return;
                        }

                        if (!first.startsWith(".") || first.startsWith(".end"))
                            return;
                        //insertText(getCaretPosition(), "\t");
                        int pos = getCaretPosition();
                        insertText(pos, "\n.end-" + first.replace(".", ""));
                        moveTo(pos);
                    }

                    if (event.getCode() == KeyCode.SPACE) {
                        popup.hide();
                        searchResult.clear();
                    }

					/*
					if (!entries.contains(lastWord))
						entries.add(lastWord.replaceAll("[^0-9a-zA-Z]+", ""));
						*/

                }


            }
        });
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                Matcher m = lastWordPattern.matcher(getText(getCurrentParagraph(), 0, getCurrentParagraph(), getCaretColumn()));
                String lastWord = "";
                if (m.find())
                    lastWord = m.group();
                searchResult.clear();
                suggestionBox.getChildren().clear();
                if (lastWord.length() > 1)
                    searchResult.addAll(entries.subSet(lastWord, lastWord + Character.MAX_VALUE));
                searchResult.forEach(x -> suggestionBox.getChildren().add(new Label(x)));
                if (lastWord.length() > 1) {
                    if(!searchResult.isEmpty() && searchResult.getFirst()!=null)
                        if(searchResult.getFirst().equals(lastWord)) {
                            popup.hide();
                            return;
                        }
                    getCaretBounds().ifPresent(
                            bounds -> popup.show(IJVMCodeArea.this, bounds.getMaxX() + 20, bounds.getMinY()));
                } else {
                    popup.hide();
                }

            }
        });
        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                //	popup.hide();
            }
        });
        getEntries().addAll(Arrays.asList(KEYWORDS));

        // add line numbers to the left of area
        setParagraphGraphicFactory(LineNumberFactory.get(this));
        plainTextChanges()
                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(1))
                // run the following code block when previous stream emits an event
                .subscribe(ignore -> setStyleSpans(0, computeHighlighting(getText())));

    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("COMMENT") != null ? "comment" :
                                    matcher.group("LABEL") != null ? "label" :
                                            matcher.group("DECLARATION") != null ? "declaration" :
											/*		matcher.group("SEMICOLON") != null ? "semicolon" :
															matcher.group("STRING") != null ? "string" :*/
                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Get the existing set of autocomplete entries.
     *
     * @return The existing autocomplete entries.
     */
    public SortedSet<String> getEntries() {
        return entries;
    }


}

package mic1.gaborFix;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.GenericStyledArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.Function;
import java.util.function.IntFunction;

public class LineBreakPointNumberFactory implements IntFunction<Node>
{
	private static final Insets DEFAULT_INSETS = new Insets(0.0D, 5.0D, 0.0D, 5.0D);
	private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
	private static final Font DEFAULT_FONT;
	private static final Background DEFAULT_BACKGROUND;
	private final Val<Integer> nParagraphs;
	private final IntFunction<String> format;
	private final Function<Integer, Boolean> breakpointHandler;

	public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, Function<Integer, Boolean> breakpointHandler)
	{
		return get(area, (digits) -> {
			return "%1$" + digits + "s";
		}, breakpointHandler);
	}

	public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, IntFunction<String> format, Function<Integer, Boolean> breakpointHandler)
	{
		return new LineBreakPointNumberFactory(area, format, breakpointHandler);
	}

	private LineBreakPointNumberFactory(GenericStyledArea<?, ?, ?> area, IntFunction<String> format, Function<Integer, Boolean> breakpointHandler)
	{
		this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
		this.format = format;
		this.breakpointHandler = breakpointHandler;
	}

	public Node apply(int idx)
	{
		Val<String> formatted = this.nParagraphs.map((n) -> {
			return this.format(idx + 1, n);
		});
		Label lineNo = new Label();
		lineNo.setFont(DEFAULT_FONT);
		lineNo.setBackground(DEFAULT_BACKGROUND);
		lineNo.setTextFill(DEFAULT_TEXT_FILL);
		lineNo.setPadding(DEFAULT_INSETS);
		lineNo.setAlignment(Pos.TOP_RIGHT);
		lineNo.getStyleClass().add("lineno");
		lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
		if (breakpointHandler != null)
		{
			if (breakpointHandler.apply(-idx))
				//lineNo.getStyle("-rtfx-background-color: red");
				lineNo.setBackground(new Background(new BackgroundFill[]{new BackgroundFill(Color.web("red"), (CornerRadii) null, (Insets) null)}));
			else
				lineNo.setBackground(DEFAULT_BACKGROUND);
		}
		lineNo.setOnMouseClicked(event -> {
			if (breakpointHandler != null)
			{
				if (breakpointHandler.apply(idx))
					//lineNo.getStyle("-rtfx-background-color: red");
					lineNo.setBackground(new Background(new BackgroundFill[]{new BackgroundFill(Color.web("red"), (CornerRadii) null, (Insets) null)}));
				else
					lineNo.setBackground(DEFAULT_BACKGROUND);
			}

		});
		return lineNo;
	}

	private String format(int x, int max)
	{
		int digits = (int) Math.floor(Math.log10((double) max)) + 1;
		return String.format((String) this.format.apply(digits), x);
	}

	static
	{
		DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13.0D);
		DEFAULT_BACKGROUND = new Background(new BackgroundFill[]{new BackgroundFill(Color.web("#ddd"), (CornerRadii) null, (Insets) null)});
	}
}

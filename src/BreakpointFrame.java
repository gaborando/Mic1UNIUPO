/****
 *BreakpointFrame.java
 *
 *Window that displays the List of Breakpoint.
 *
 * @author 
 *   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
 *   U.P.O.
 *   Alessandria Italy
 ****/
import java.awt.Event;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.TextArea;
import java.util.Vector;

public class BreakpointFrame extends Frame {

	private static final long serialVersionUID = 1L;

	Vector BK_Vector_Local = new Vector();

	TextArea List_of_BK = new TextArea();

	public BreakpointFrame() {

		super("List of Breakpoint:");
		add(List_of_BK);
	}

	public Insets insets() {
		return new Insets(50, 10, 20, 10);
	}

	public boolean handleEvent(Event event) {

		switch (event.id) {
		case Event.WINDOW_DESTROY:
			setVisible(false);
			break;
		}
		return false;
	}
}
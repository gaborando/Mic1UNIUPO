package mic1.gaborFix;


import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

public class FxTextAreaOutputStream extends OutputStream
{
	private TextArea textControl;

	/**
	 * Creates a new instance of mic1.TextAreaOutputStream which writes
	 * to the specified instance of javax.swing.JTextArea control.
	 *
	 * @param control   A reference to the javax.swing.JTextArea
	 *                  control to which the output must be redirected
	 *                  to.
	 */
	public FxTextAreaOutputStream( TextArea control ) {
		textControl = control;
	}

	/**
	 * Writes the specified byte as a character to the
	 * javax.swing.JTextArea.
	 *
	 * @param   b   The byte to be written as character to the
	 *              JTextArea.
	 */
	public void write( int b ) throws IOException
	{
		// append the data as characters to the JTextArea control
		textControl.appendText( String.valueOf( ( char )b ) );
	}
}

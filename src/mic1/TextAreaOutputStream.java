package mic1;
/*
*
* @(#) mic1.TextAreaOutputStream.java
*
*/

import java.awt.TextArea;
import java.io.IOException;
import java.io.OutputStream;

/**
* An output stream that writes its output to a javax.swing.JTextArea
* control.
*
* @author  Ranganath Kini
* @see      javax.swing.JTextArea
*/
public class TextAreaOutputStream extends OutputStream {
    private TextArea textControl;
    
    /**
     * Creates a new instance of mic1.TextAreaOutputStream which writes
     * to the specified instance of javax.swing.JTextArea control.
     *
     * @param control   A reference to the javax.swing.JTextArea
     *                  control to which the output must be redirected
     *                  to.
     */
    public TextAreaOutputStream( TextArea control ) {
        textControl = control;
    }
    
    /**
     * Writes the specified byte as a character to the 
     * javax.swing.JTextArea.
     *
     * @param   b   The byte to be written as character to the 
     *              JTextArea.
     */
    public void write( int b ) throws IOException {
        // append the data as characters to the JTextArea control
        textControl.append( String.valueOf( ( char )b ) );
    }   
}

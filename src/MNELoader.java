 
/**
* MNELoader.java
*
* Loads an MNE file in a Vector for MainMemoryFrame.
*
* @author (original version)
*   Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
*
* updated by
*   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
*   U.P.O.
*   Alessandria Italy
*                       
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;



public class MNELoader implements Mic1Constants{

BufferedReader in = null;
String mnefile = null;
String msg = null;
Vector mnemonic = null;
Vector constant = null;
Vector labels = null;
int variables=0;

public MNELoader(String filename){

	try{
		int index = filename.lastIndexOf('.');

		mnefile = filename.substring(0,index)+".mne";

		in = new BufferedReader(new FileReader(mnefile));
      
		mnemonic = new Vector();
		constant = new Vector();
		labels = new Vector();
      	if (validateFile())  readFile();
	
	}catch(IOException e) {
		msg = "Error while reading file " + mnefile;
	}
  }

private boolean validateFile() {
    try {

        if (in.read() != mne_magic1 || in.read() != mne_magic2 || in.read() != mne_magic3 || in.read() != mne_magic4) {
  	      msg = "File error: invalid file format: " +mnefile;
      	return false;
        }
        else {
        	msg = null;
        	return true;
        }
    }
    catch (IOException e) {
      msg = "Error while reading file " + mnefile;
      return false;
    }
  }

 public String isValid() {
    return msg;
 }

  private void readFile() {
    String simbol = null;
    boolean main = false, first = true;
    int offset;
    int address = 0;
    int byte_count;
    int i;	
    Simbol s = null;

    try {
	address = Integer.parseInt(in.readLine());
	simbol = in.readLine();
	while (!simbol.equals("end_constants")){
		s = new Simbol(simbol,address);
		constant.add(s);
		address++;
		/* reads next constant */
		simbol = in.readLine();
      }
	address = 0;
	/* reads the first method's name (main)*/
   	simbol = in.readLine();
	main = true;			      	
	while (!simbol.equals("end_methods")){
		if (main){
			/* reads the labels in method main */
			simbol = in.readLine();
			while (!simbol.equals("end_labels")){
				/* reads the label's address */
				offset = Integer.parseInt(in.readLine());
				s = new Simbol(simbol,offset+address);
				labels.add(s);
				/* reads next label */
			   	simbol = in.readLine();
			}
			main = false;
		}
		else {
			/* reads the labels in the other method  */
			simbol = in.readLine();
			while (!simbol.equals("end_labels")){
				/* reads the label's address */
				offset = Integer.parseInt(in.readLine());
				s = new Simbol(simbol,offset+address+4);
				labels.add(s);
				/* reads next label */
			   	simbol = in.readLine();
			}
		}


		/* reads # of byte in the method */
		if (first) variables = Integer.parseInt(in.readLine());
		first = false;
		byte_count = Integer.parseInt(in.readLine());

		/* reads mnemonic instructions */
		for(i=0;i<byte_count;i++)
			mnemonic.add(in.readLine());
		address += byte_count;

		/* reads the next method's name */
   		simbol = in.readLine();			      	
		labels.add(s);
	}
    }catch (Exception ioe) {}
}

public int number_variables_main(){
	return variables;
}

public Vector getConstant(){
	return constant;
}

public Vector getMnemonic(){
	return mnemonic ;
}

public Vector getLabels(){
	return labels;
}

}
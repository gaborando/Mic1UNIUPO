/*
*
*  IJVMAssembler.java
*
*  mic1 microarchitecture simulator 
*  Copyright (C) 1999, Prentice-Hall, Inc. 
* 
*  This program is free software; you can redistribute it and/or modify 
*  it under the terms of the GNU General Public License as published by 
*  the Free Software Foundation; either version 2 of the License, or 
*  (at your option) any later version. 
* 
*  This program is distributed in the hope that it will be useful, but 
*  WITHOUT ANY WARRANTY; without even the implied warranty of 
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
*  Public License for more details. 
* 
*  You should have received a copy of the GNU General Public License along with 
*  this program; if not, write to: 
* 
*    Free Software Foundation, Inc. 
*    59 Temple Place - Suite 330 
*    Boston, MA 02111-1307, USA. 
* 
*  A copy of the GPL is available online the GNU web site: 
* 
*    http://www.gnu.org/copyleft/gpl.html
* 
*/ 

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
* Main part of assembler.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*
*
*   Modification History
*
*	Name             		Date       	Comment
*	---------------- 		---------- 	----------------------------------------
*	Dan Stone        			 		Created
*	Claudio Bertoncello		1999.12.21 	write a file for mnemonic instruction
*										for the visualization of main memory
*	Francesco Poli			5/2008		Support for varnum varnum instructions
*
*/

public class IJVMAssembler implements Mic1Constants {
 
  private InputStream in = null;
  private OutputStream out = null;
  private BufferedWriter out_mne = null;
  private Hashtable<String, Instruction> ops = null;
  private Vector<IJVMConstant> constants = null;
  private Vector<IJVMMethod> methods = null;
  private IJVMMethod main = null;
  private Hashtable<String, Integer> method_refs = null;
  private static int lineno;
  private static String conf_file = "ijvm.conf";
  private boolean status;
  static final byte
    magic1 = (byte)0x1D,
    magic2 = (byte)0xEA,
    magic3 = (byte)0xDF,
    magic4 = (byte)0xAD,
    mne_magic1 = (byte)0x2E,
    mne_magic2 = (byte)0x4F,
    mne_magic3 = (byte)0x6D,
    mne_magic4 = (byte)0x7C;

  int CPP_B = CPP * 4;
  int byte_count;
  int const_count;
  private PrintStream err;
  public IJVMAssembler(InputStream in, OutputStream out, String outfile, PrintStream err) {
    this.in = in;
    this.out = out;
    this.err = err;
    ops = new Hashtable<String, Instruction>();
    constants = new Vector<IJVMConstant>();
    methods = new Vector<IJVMMethod>();
    method_refs = new Hashtable<String, Integer>();
    init();
    if (parse()) {
      try {
		int index=outfile.indexOf('.');
		String mnefile = outfile.substring(0,index)+".mne";
		out_mne = new BufferedWriter(new FileWriter(mnefile));
	      generate();
      }
      catch (IOException ioe) {
    	  error("Exception encountered while generating code");
      }
    }
  }

  public IJVMAssembler(String infile, String outfile) {
    try {
      lineno = 0;
      in = new BufferedInputStream(new FileInputStream(infile));
      ops = new Hashtable<String, Instruction>();
      constants = new Vector<IJVMConstant>();
      methods = new Vector<IJVMMethod>();
      method_refs = new Hashtable<String, Integer>();
      init();
    }
    catch (Exception e) {
    	error("Error opening file " + infile);
    }
    try {
      if (parse()) {
	out = new FileOutputStream(outfile);
	int index=outfile.indexOf('.');
	String mnefile = outfile.substring(0,index)+".mne";
	out_mne = new BufferedWriter(new FileWriter(mnefile));
	generate();
      }
    }
    catch (IOException e) {
    	error("Error opening file " + outfile);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
    * init() initializes the ops Vector, which contains descriptions of 
    * each assembly language instruction--mnemonic, opcode, parameters.
    * This information comes from the file <code>ijvm.conf</code>, which
    * can be customized to suit the requirements of an altered instruction
    * set or simulator architecture.
    */
  private void init() {
		ops.clear();
		ConfParser cp = new ConfParser(conf_file);
		Vector<Instruction> ins = cp.getInstructionSet();
		for (int i = 0; i < ins.size(); i++) {
			Instruction op = ins.elementAt(i);
			if (op.getMnemonic() != null) {
				ops.put(op.getMnemonic(), op);
			} else {
				error("\n Error: null instruction mnemonic, opcode: " + op.getOpcode());
			}
		}
		byte_count = 0;
		const_count = 0;
	}

  public boolean getStatus() {
    return status;
  }

  private boolean parse() {
    status = true;
    boolean const_parsed = false;
    boolean main_parsed = false;
    String line = readLine();
    while (true) {
      while (line != null && line.trim().length() == 0)
	line = readLine();
      if (line == null) 
	break;
      else {
	line = line.trim();
	if (line.equals(".constant")) {
	  if (main_parsed) {
	    error("Constants must be defined before method definitions");
	    status = false;
	  }
	  else {
	    if (!const_parsed) {
	      status = parseConstants() && status;
	      const_parsed = true;
	    }
	    else {
	      error("Constants already declared");
	      status = false;
	    }
	  }
	}
	else if (line.equals(".main")) {
	  if (main_parsed) {
	    error("Method main already declared");
	    status = false;
	    //	    line = readLine();
	  }
	  else {
	    status = parseMain() && status;
	    main_parsed = true;
	  }
	}
	else if (line.startsWith(".method")) {
	  if (!main_parsed) {
	    error("main method must be defined before other methods");
	    status = false;
	  }
	  else 
	    status = parseMethod(line) && status;
	}
	else {
	  error("Unexpected directive: " + line);
	  status = false;
	  //	  line = readLine();
	}
      }
      line = readLine();
    }
    status = linkMethods() && status;
    return status;
  }

  private boolean parseConstants() {
    boolean status = true;
    String line = readLine();
    StringTokenizer st = null;
    while (line != null && !line.equals(".end-constant")) {
      if (line.trim().length() > 0) {
	st = new StringTokenizer(line);
	String name = st.nextToken();
	if (!st.hasMoreTokens()) {
	  error("Constant must have a value");
	  status = false;
	}
	else {
	  Integer value = Integer.decode(st.nextToken());
	  constants.addElement(new IJVMConstant(name, value.intValue()));
	}
      }
      line = readLine();
    }
    if (line == null) {
      error("Unexpected end of file");
      status = false;
    }
    return status;
  }
  
  private boolean parseMain() {
    main = new IJVMMethod("main", ops, constants, in, lineno, err);
    if (main.getParameterCount() > 1) {
      error("main may not have parameters");
      lineno = main.getLineno();
      return false;
    }
    lineno = main.getLineno();
    return main.getStatus();
  }

  private boolean parseMethod(String line) {
    boolean status = true;
    String name_params = line.substring(7); // strip off ".method", remainder is method name & parameters
    if (name_params.trim().length() == 0) {
      error("Method must be named");
      status = false;
    }
    else {
      IJVMMethod method = new IJVMMethod(name_params, ops, constants, in, lineno, err);
      status = method.getStatus();
      lineno = method.getLineno();
      methods.addElement(method);
    }
    return status;
  }

  private boolean linkMethods() {
    byte_count = main.getByteCount();
    boolean status = true;
    for (int i = 0; i < methods.size(); i++) {
      IJVMMethod method = methods.elementAt(i);
      IJVMConstant constant = new IJVMConstant(method.getName(), byte_count);
      constants.add(constant);
      method_refs.put(method.getName(), new Integer(constants.indexOf(constant)));
      byte_count = byte_count + method.getByteCount() + 4;  // four bytes for param & local var count
    }
    for (int i = 0; i < methods.size(); i++) 
      status = methods.elementAt(i).linkMethods(method_refs) && status;
    status = main.linkMethods(method_refs) && status;
    return status;
  }

  private void generate() throws IOException {

    	String name;
	String byte_str;
	String constant_name = null;

	/* validate the files */	   
    	out.write(magic1);
    	out.write(magic2);
    	out.write(magic3);
    	out.write(magic4);
	
      out_mne.write(mne_magic1);
      out_mne.write(mne_magic2);
      out_mne.write(mne_magic3);
      out_mne.write(mne_magic4);
	//out_mne.newLine();
	

    	/* write constants */
    	out.write(CPP_B >> 24);
    	out.write(CPP_B >> 16);
    	out.write(CPP_B >> 8);
    	out.write(CPP_B);
    	String cpp_b = (new Integer(CPP_B)).toString();
    	out_mne.write(cpp_b,0,cpp_b.length());
    	out_mne.newLine();
    	out.write((constants.size()*4) >> 24);
    	out.write((constants.size()*4) >> 16);
    	out.write((constants.size()*4) >> 8);
    	out.write(constants.size()*4);
    	for (int i = 0; i < constants.size(); i++) {
      	IJVMConstant con = constants.elementAt(i);
		out.write(con.getValue() >> 24);
		out.write(con.getValue() >> 16);
		out.write(con.getValue() >> 8);
		out.write(con.getValue());
		
		constant_name = con.getName();		
		out_mne.write(constant_name + "_1",0,constant_name.length()+2);
      	out_mne.newLine();
		out_mne.write(constant_name + "_2",0,constant_name.length()+2);
      	out_mne.newLine();
		out_mne.write(constant_name + "_3",0,constant_name.length()+2);
      	out_mne.newLine();
		out_mne.write(constant_name + "_4",0,constant_name.length()+2);
      	out_mne.newLine();

    	}
    	out_mne.write("end_constants",0,13);
    	out_mne.newLine();

    	/* write methods */
    	out.write(0);
    	out.write(0);
    	out.write(0);
    	out.write(0);
    	out.write(byte_count >> 24);
    	out.write(byte_count >> 16);
    	out.write(byte_count >> 8);
    	out.write(byte_count);
	
	out_mne.write("Method main",0,11);
      out_mne.newLine();

	main.generateLabels(out_mne);

/* CB  12/3/01  */	
	/* write number of main's variable */
	byte_str = (new Integer(main.getVarnumCount())).toString();
	out_mne.write(byte_str,0,byte_str.length());
      out_mne.newLine();
/* CB  12/3/01  */

	byte_str = (new Integer(main.getByteCount())).toString();
    	out_mne.write(byte_str,0,byte_str.length());
      out_mne.newLine();
	
	main.generate(out,out_mne);

	for (int i = 0; i < methods.size(); i++) {
		IJVMMethod method = methods.elementAt(i);
		out.write(method.getParameterCount() >> 8);
      	out.write(method.getParameterCount());
		out.write(method.getVarnumCount() >> 8);
      	out.write(method.getVarnumCount());
		
		name = method.getName();
		out_mne.write("Method "+name,0,name.length()+7);
      	out_mne.newLine();

		/*scrivo le eventuali labels */
		method.generateLabels(out_mne);

		byte_str = (new Integer(method.getByteCount()+4)).toString();
    	out_mne.write(byte_str,0,byte_str.length());
      	out_mne.newLine();
	
	  	out_mne.write("#param_1",0,8);
      	out_mne.newLine();
		out_mne.write("#param_2",0,8);
      	out_mne.newLine();
		out_mne.write("#var_1",0,6);
	    out_mne.newLine();
		out_mne.write("#var_2",0,6);
      	out_mne.newLine();


      
		method.generate(out,out_mne);
    	}
    	out_mne.write("end_methods",0,11);
    	out_mne.newLine();
    	out.close();
    	out_mne.close();
  }




  private void error(String msg) {
    err.println(lineno + ": " + msg);
  }

  private String readLine() {
    int ctemp;
    String s = new String();
    try {
      int ch = 0;
      while (((char)ch) != '\n' && ch > -1) {
        ch = in.read();
        if (ch == -1 && s.length() == 0) {
          return null;
        }
        if (ch == '/') {
          ctemp = in.read();
          if ((char)ctemp == '/') {
            ch = in.read();
            while (((char)ch) != '\n' && ch > -1)
              ch = in.read();
            lineno++;
            return s + " ";
          }
          else if (ch != 13)
            s = s + ((char)ch) + Character.toLowerCase((char)ctemp);
        }
        if (ch != '\n' && ch > -1 && ch != 13) {
          if ((char)ch == '\'') {
            ctemp = in.read();
            s = s + (char)ch + (char)ctemp;
          }
          else
            s = s + Character.toLowerCase((char)ch);
        }
      }
      lineno++;
      return s;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

//  private Integer decode(String str) throws NumberFormatException {
//    if (str.startsWith("0x") || str.startsWith("0X")) {
//      return Integer.valueOf(str.substring(2), 16);
//    }
//    if (str.startsWith("#")) {
//      return Integer.valueOf(str.substring(1), 16);
//    }
//    if (str.startsWith("0") && str.length() > 1) {
//      return Integer.valueOf(str.substring(1), 8);
//    }
//    if (str.charAt(0) == '\'' && str.length() > 1) {
//      return new Integer((int)str.charAt(1));
//    }
//    return Integer.valueOf(str);
//  }    

}

/*
 *
 *  ConfParser.java
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Parses a macrolanguage description file
 * 
 * @author Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
 *         Ray Ontko & Co, Richmond, Indiana, US
 */
public class ConfParser {

//	private static final int radix = 16;
//	private static final int exit_code = 1;
//	private BufferedInputStream in = null;
	private BufferedReader reader = null;
	private int lineno;
	private String filename = null;
	private Vector<Instruction> instruction_set = null;

//	public ConfParser(InputStream instream) {
//		in = new BufferedInputStream(instream);
//		parse();
//	}

	public ConfParser(String filename) {
		this.filename = filename;
		try {
                        String conffile = System.getenv("HOME") + "/." + filename;
                        File f = new File(conffile);
                        if (f.exists()) {
                            filename = conffile;
                        } else if (System.getenv("MIC1SIM_PATH") != null) {
                            conffile = System.getenv("MIC1SIM_PATH") + '/' + filename;
                            f = new File(conffile);
                            if (f.exists()) {
                                filename = conffile;
                            }
                        } else {
			    conffile = "/opt/mic2006/" + filename;
                            f = new File(conffile);
                            if (f.exists()) {
                                filename = conffile;
                            }
                        }
                        System.out.println("Conf File: " + filename);
//			in = new BufferedInputStream(new FileInputStream(filename));
			reader = new BufferedReader(new FileReader(filename));
			parse();
		} catch (FileNotFoundException ex) {
			System.out.println("File not found: " + filename);
			ex.printStackTrace();
			System.exit(1);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public Vector<Instruction> getInstructionSet() {
		return instruction_set;
	}

	private void parse() throws IOException {
		instruction_set = new Vector<Instruction>();
		lineno = -1;
		String s = null;
		while ( (s = reader.readLine()) != null ) {
			++lineno;
//		String s = readLine();
//		while (s.length() > 0) {
			if(s.contains("//")) {
				int index = s.indexOf("//");
				s = s.substring(0, index).trim();
			}
			s = s.toLowerCase();
			if(s.length() == 0) {
				continue;
			}
			
			StringTokenizer st = new StringTokenizer(s);
			int opcode;
			String mnemonic = null;
			try {
				if (st.hasMoreTokens()) {
					String str = st.nextToken();
					opcode = Integer.decode(str).intValue();
					
					// Franz2008
					mnemonic = st.nextToken();
					Vector<ParamType> params = new Vector<ParamType>();
					boolean check = true;
					while(st.hasMoreTokens()) {
						String token = st.nextToken().trim();
						
						if(token.equalsIgnoreCase("varnum")) {
							params.add(ParamType.VARNUM);
						} else if(token.equalsIgnoreCase("const")) {
							params.add(ParamType.CONST);
						} else if(token.equalsIgnoreCase("byte")) {
							params.add(ParamType.BYTE);
						} else if(token.equalsIgnoreCase("label")) {
							params.add(ParamType.LABEL);
						} else if(token.equalsIgnoreCase("offset")) {
							params.add(ParamType.OFFSET);
						} else if(token.equalsIgnoreCase("index")) {
							params.add(ParamType.INDEX);
						} else {
							System.out.println("Error: parameter " + token + " not supported.");
							check = false;
						}
					}
					
					if(check) {
						instruction_set.add(new Instruction(opcode, mnemonic, params));
					}
					// Fine Franz2008
					
//					if (st.hasMoreTokens()) {
//						mnemonic = st.nextToken();
//						int type = Instruction.NOPARAM;
//						if (st.hasMoreTokens()) {
//							String param = st.nextToken();
//							if (param.equalsIgnoreCase("varnum")) {
//								if (st.hasMoreTokens()) { 
//									String next = st.nextToken();
//									if( next.equalsIgnoreCase("const") ) {
//										type = Instruction.VARNUM_CONST;
//									} else if( next.equalsIgnoreCase("varnum")) {
//										type = Instruction.VARNUM_VARNUM;
//									} else {
//										System.out.println("Parameter combination " + param
//												+ " " + next + " not supported");
//									}
//								} else {
//									type = Instruction.VARNUM;
//								}
//							} else if (param.equalsIgnoreCase("index"))
//								type = Instruction.INDEX;
//							else if (param.equalsIgnoreCase("label"))
//								type = Instruction.LABEL;
//							else if (param.equalsIgnoreCase("byte"))
//								type = Instruction.BYTE;
//							else if (param.equalsIgnoreCase("const"))
//								type = Instruction.CONST;
//							else if (param.equalsIgnoreCase("offset"))
//								type = Instruction.OFFSET;
//							else
//								System.out.println("Parameter type " + param
//										+ " not supported");
//						}
//						instruction_set.add(new Instruction(opcode, mnemonic, type));
//					}
				}
			} catch (NumberFormatException nfe) {
				System.out.println(" " + filename + " " + lineno
						+ ": invalid number format for opcode");
				nfe.printStackTrace();
			} catch (Exception ex) {
				System.out.println("Exception thrown parsing line " + lineno + "\n:" + ex.getLocalizedMessage());
				ex.printStackTrace();
			}
//			s = readLine();
		}
	}

//	/**
//	 * decode() parses a String and creates an Integer object with the
//	 * appropriate value. This method performs the same function as the
//	 * java.lang.Integer.decode() method. It is included to ensure compatability
//	 * with Java 1.0 compilers.
//	 */
//	private Integer decode(String str) throws NumberFormatException {
//		if (str.startsWith("0x") || str.startsWith("0X")) {
//			return Integer.valueOf(str.substring(2), 16);
//		}
//		if (str.startsWith("#")) {
//			return Integer.valueOf(str.substring(1), 16);
//		}
//		if (str.startsWith("0") && str.length() > 1) {
//			return Integer.valueOf(str.substring(1), 8);
//		}
//		return Integer.valueOf(str);
//	}

//	private String readLine() {
//		String s = new String();
//		try {
//			int ch = 0;
//			while (((char) ch) != '\n' && ch > -1) {
//				ch = in.read();
//				if (ch == '/' && in.read() == '/') {
//					ch = in.read();
//					while (((char) ch) != '\n' && ch > -1)
//						ch = in.read();
//					++lineno;
//					return s.toLowerCase() + " ";
//				}
//				if (ch != '\n' && ch > -1)
//					s += (char) ch;
//			}
//			++lineno;
//			return s.toLowerCase();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return s.toLowerCase();
//	}
}

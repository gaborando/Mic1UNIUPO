/*
*
*  IJVMMethod.java
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
* Parses a method, stores information about parameters, local variables,
* and instructions, and writes this information to an output stream.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*
*   Modification History
*
*	Name             		Date       	Comment
*	---------------- 		---------- 	----------------------------------------
*	Dan Stone        			 	Created
*	Claudio Bertoncello		1/1999 	add generateLabel method and modify generate
*									method to write a .mne file
*	Francesco Poli			5/2008	Compiler improvement
*							
*/
public class IJVMMethod {

	private String name = null;
	private String params = null;
	private String end_method = null;
	private int byte_count;
	private Vector<IJVMInstruction> code = null;
	private Vector<String> varnums = null;
	private Vector<IJVMConstant> constants = null;
	private Hashtable<String, Integer> labels = null;
	private Hashtable<String, Instruction> ops = null;
	private InputStream in = null;
	private int lineno;
	private boolean isWide = false;
	private boolean status;
	private int param_count;
	private int var_count;
	private PrintStream err;

	public IJVMMethod(String name_params, Hashtable<String, Instruction> ops, Vector<IJVMConstant> constants,
			InputStream in, int lineno, PrintStream err) {
		this.ops = ops;
		this.constants = constants;
		this.in = in;
		this.lineno = lineno;
		this.err = err;
		code = new Vector<IJVMInstruction>();
		labels = new Hashtable<String, Integer>();
		varnums = new Vector<String>();
		byte_count = 0;
		var_count = 0;
		status = true;
		if (name_params.equals("main")) {
			param_count = 0; // main has no parameters
			name = "main";
			end_method = ".end-main";
		} else {
			param_count = 1; // OBJREF always counts as a parameter
			varnums.addElement("LINK PTR"); // OBJREF gets overwritten with the link-pointer, needed for IRETURN
			end_method = ".end-method";
			if (name_params.indexOf('(') < 0) {
				error(lineno, "Invalid method declaration: " + name_params + "\n  must contain ()");
				status = false;
			} else {
				name = name_params.substring(0, name_params.indexOf('(')).trim();
				params = name_params.substring(name_params.indexOf('(') + 1).trim();
				parseParameters();
			}
		}
		parse();

	}

	public int getByteCount() {
		return byte_count;
	}

	public Vector<IJVMInstruction> getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getLineno() {
		return lineno;
	}

	public boolean getStatus() {
		return status;
	}

	public int getParameterCount() {
		return param_count;
	}

	public int getVarnumCount() {
		return var_count;
	}

	private void parse() {
		String line = readLine();

		while (line != null && line.trim().length() == 0) {
			line = readLine();
		}
		if (line.trim().equals(".var")) {
			parseVarnums();
			line = readLine();
			while (line != null && line.trim().length() == 0) {
				line = readLine();
			}
		}
		while (line != null && !line.trim().equals(end_method)) {
			isWide = parseInstruction(line.trim());
			line = readLine();
		}
		linkLabels();
	}

	private void parseParameters() {
		if (params.indexOf(')') < 0) {
			error(lineno, "Missing ')' in method declaration");
			status = false;
		} else {
			char ch = ' ';
			while (ch != ')') {
				String param = "";
				ch = params.charAt(0);
				params = params.substring(1);
				while (ch != ',' && ch != ')') {
					param = param + ch;
					ch = params.charAt(0);
					params = params.substring(1);
				}
				if (param.trim().length() > 0) {
					varnums.addElement(param.trim());
					++param_count;
				}
			}
		}
	}

	private void parseVarnums() {
		String line = readLine();
		while (!line.trim().equals(".end-var")) {
			if (line == null) {
				error(lineno, "Unexpected end of file");
				status = false;
				break;
			}
			varnums.addElement(line.trim());
			++var_count;
			line = readLine();
		}
	}

	/**
	 * Effettua il parsing della linea di codice IJVM passata per argomento
	 * 
	 * @param line
	 * @return
	 */
	private boolean parseInstruction(String line) {
		if (line.length() > 0) {
			StringTokenizer st = new StringTokenizer(line);
			String mnemonic = st.nextToken();
			
			if (mnemonic.indexOf(':') > -1) {
				String label = mnemonic.substring(0, mnemonic.indexOf(':'));
				labels.put(label, new Integer(byte_count));
				mnemonic = mnemonic.substring(mnemonic.indexOf(':') + 1);

				if (mnemonic.trim().length() == 0) {
					if (!st.hasMoreTokens()) {
						return false;
					} 
					mnemonic = st.nextToken();
				}
			}

			if (mnemonic.equals("wide")) {
				isWide = true;
				if(!st.hasMoreTokens()) {
					error(lineno, "Error: WIDE must be a prefix.");
					status = false;
					return false;
				}
				mnemonic = st.nextToken(); // Ottieni l'operatore su cui applicare il wide
			} else {
				isWide = false;
			}
			
			Instruction instruction = ops.get(mnemonic);
			IJVMInstruction inst = null;
			if (instruction == null) {
				error(lineno, "Invalid instruction: " + mnemonic);
				status = false;
			} else {
				inst = new IJVMInstruction(instruction, byte_count, lineno);
				inst.setWide(isWide);
				if(isWide) {
//					inst.setMnemonic("wide_" + inst.getMnemonic());
					++byte_count; // c'è anche l'opcode del WIDE
//					inst.setOpcode(0x100 + inst.getOpcode());
				}
				
				// parse parameters
				if (st.hasMoreTokens()) {
					if(inst.getParamNumber() == 0) {
						error(lineno, "Instruction takes no parameters");
						status = false;
					}
					
//					String const_name = null;
//					int const_index;
					
					for(ParamType paramType : inst.getParamTypes()) {
						String paramValue = st.nextToken();
						if (paramType.equals(ParamType.BYTE)) {
							if(isWide) {
								inst.addParameterValue(Integer.decode(paramValue).intValue());
								byte_count += 2;
							} else {
								inst.addParameterValue(Integer.decode(paramValue).intValue());
								++byte_count;
							}
						} else if (paramType.equals(ParamType.CONST)) {
							if(isWide) {
								inst.addParameterValue(Integer.decode(paramValue).intValue());
								byte_count += 2;
							} else {
								inst.addParameterValue(Integer.decode(paramValue).intValue());
								++byte_count;
							}
						} else if (paramType.equals(ParamType.INDEX)) {
							// Non ha il wide; la label usa 2 byte
							if (paramValue.startsWith("=")) {
								int const_value = Integer.decode(paramValue.substring(1)).intValue();
								int const_count = constants.size();
								constants.add(new IJVMConstant(paramValue, const_value));
								inst.addParameterValue(const_count);
								byte_count += 2;
							} else {
								int const_index = findConstant(paramValue);
								if (const_index >= 0) {
									inst.addParameterValue(const_index);
									byte_count += 2;
								} else {
									error(lineno, "Constant not declared: " + paramValue);
								}
							}
						} else if (paramType.equals(ParamType.LABEL)) {
							// Non ha il wide; la label usa 2 byte
							inst.setLabel(paramValue);
							inst.addParameterValue(0);
							byte_count += 2;
						} else if (paramType.equals(ParamType.OFFSET)) {
							// Non ha il wide; la label usa 2 byte
							inst.setLabel(paramValue);
							inst.addParameterValue(0);
							byte_count += 2;
						} else if (paramType.equals(ParamType.VARNUM)) {
							if(isWide) {
								int varIndex = varnums.indexOf(paramValue);
								if(varIndex < 0) {
									error(lineno, "Unknown variable " + paramValue);
									status = false;
									break;
								}
								inst.addParameterValue(varnums.indexOf(paramValue));
								byte_count += 2;
							} else {
								int varIndex = varnums.indexOf(paramValue);
								if(varIndex < 0) {
									error(lineno, "Unknown variable " + paramValue);
									status = false;
									break;
								}
								inst.addParameterValue(varnums.indexOf(paramValue));
								++byte_count;
							}
						} else {
							error(lineno, "Unmanaged parameter type.");
							status = false;
							break;
						}
					}
					
					if(st.hasMoreTokens()) {
						error(lineno, "Instruction takes " + instruction.getParamNumber() + " parameters");
						status = false;
					}
					
					code.add(inst);
					++byte_count;
					
//					switch (inst.getType()) {

//					case Instruction.NOPARAM:
//						error(lineno, "Instruction takes no parameters");
//						status = false;
//						break;

//					case Instruction.BYTE:
//						if (isWide == true) {
//							Integer param = Integer.decode(st.nextToken());
//							String hex = Integer.toHexString(param.intValue());
//							// error(lineno,"HEX: "+hex);
//							if (hex.length() <= 2) {
//								inst.setParam_num(2);
//								inst.setParameter2(Integer.parseInt(hex));
//								inst.setParameter(0);
//								byte_count += 3;
//							} else if (hex.length() == 4) {
//								inst.setParam_num(2);
//								inst.setParameter2(Integer.parseInt(hex
//										.substring(2, 4), 16));
//								inst.setParameter(Integer.parseInt(hex
//										.substring(0, 2), 16));
//								// error(lineno,"HEX: "+hex +" SUB1: "+hex.substring(0, 2) +" SUB2: "+hex.substring(2, 4));
//								byte_count += 3;
//							} else if (hex.length() == 3) {
//								inst.setParam_num(2);
//								inst.setParameter2(Integer.parseInt(hex
//										.substring(1, 3), 16));
//								inst.setParameter(Integer.parseInt(hex
//										.substring(0, 1), 16));
//								byte_count += 3;
//							} else {
//
//								error(lineno, "Parameters: OPcode: "
//										+ inst.getOpcode()
//										+ " MUST TAKE 1-2 byte parameter.");
//								System.exit(2);
//							}
//							inst.setOpcode(inst.getOpcode() + 0x100);
//
//							inst.setMnemonic("wide_" + inst.getMnemonic());
//							//error(lineno,"Parameters: "+ inst.getMnemonic() +" OPcode: "+ inst.getOpcode() +" First: "+inst.getParameter()+" Second: "+inst.getParameter2());
//							code.addElement(inst);
//							isWide = false;
//
//						} else {
//							inst
//									.setParameter(Integer.decode(st.nextToken())
//											.intValue());
//							code.addElement(inst);
//							byte_count += 2;
//						}
//						break;

//					case Instruction.VARNUM:
//						String varname = st.nextToken();
//						if (varnums.indexOf(varname) < 0) {
//							error(lineno, "Undeclared variable: " + varname);
//							status = false;
//						} else {
//							String hex = Integer.toHexString(varnums
//									.indexOf(varname));
//							if (isWide == true) {
//								// error(lineno,"HEX: "+hex);
//								if (hex.length() <= 2) {
//									inst.setParam_num(2);
//									inst.setParameter2(Integer.parseInt(hex));
//									inst.setParameter(0);
//									byte_count += 3;
//								} else if (hex.length() == 4) {
//									inst.setParam_num(2);
//									inst.setParameter2(Integer.parseInt(hex
//											.substring(2, 4), 16));
//									inst.setParameter(Integer.parseInt(hex
//											.substring(0, 2), 16));
//									// error(lineno,"HEX: "+hex +" SUB1: "+hex.substring(0, 2) +" SUB2: "+hex.substring(2, 4));
//									byte_count += 3;
//								} else if (hex.length() == 3) {
//									inst.setParam_num(2);
//									inst.setParameter2(Integer.parseInt(hex
//											.substring(1, 3), 16));
//									inst.setParameter(Integer.parseInt(hex
//											.substring(0, 1), 16));
//									byte_count += 3;
//								} else {
//
//									error(lineno, "Parameters: OPcode: "
//											+ inst.getOpcode()
//											+ " MUST TAKE 1-2 byte parameter.");
//									System.exit(2);
//								}
//								inst.setOpcode(inst.getOpcode() + 0x100);
//
//								inst.setMnemonic("wide_" + inst.getMnemonic());
//								//error(lineno,"Parameters: "+ inst.getMnemonic() +" OPcode: "+ inst.getOpcode() +" First: "+inst.getParameter()+" Second: "+inst.getParameter2());
//								code.addElement(inst);
//								isWide = false;
//
//							} else {
//								inst.setParameter(Integer.parseInt(hex));
//								code.addElement(inst);
//								byte_count += 2;
//							}
//
//						}
//						break;

//					case Instruction.VARNUM_VARNUM:
//						String var1 = st.nextToken();
//						String var2 = st.nextToken();
//
//						if (varnums.indexOf(var1) < 0) {
//							error(lineno, "Undeclared variable: " + var1);
//							status = false;
//							break;
//						}
//
//						if (varnums.indexOf(var2) < 0) {
//							error(lineno, "Undeclared variable: " + var2);
//							status = false;
//							break;
//						}
//
//						inst.setParam_num(2);
//						inst.setParameter(varnums.indexOf(var1));
//						inst.setParameter2(varnums.indexOf(var2));
//						code.addElement(inst);
//						byte_count += 3;
//						break;
//					case Instruction.LABEL:
//						String label = st.nextToken();
//						inst.setLabel(label);
//						code.addElement(inst);
//						byte_count += 3;
//						break;
//					case Instruction.OFFSET:
//						const_name = st.nextToken();
//						inst.setLabel(const_name);
//						code.addElement(inst);
//						byte_count += 3;
//						break;
//					case Instruction.VARNUM_CONST:
//						varname = st.nextToken();
//						if (varnums.indexOf(varname) < 0) {
//							error(lineno, "Undeclared variable: " + varname);
//							status = false;
//						} else {
//							inst.setParameter(varnums.indexOf(varname));
//							inst.setParameter2(Integer.decode(st.nextToken()).intValue());
//							code.addElement(inst);
//							byte_count += 3;
//							break;
//						}
//						break;

//					case Instruction.CONST:
//						break;

//					case Instruction.INDEX:
//						const_name = st.nextToken();
//						if (const_name.startsWith("=")) {
//							int const_value = Integer.decode(const_name.substring(1))
//									.intValue();
//							int const_count = constants.size();
//							constants.add(new IJVMConstant(const_name, const_value));
//							inst.setParameter(const_count);
//							code.addElement(inst);
//							byte_count += 3;
//						} else {
//							const_index = findConstant(const_name);
//							if (const_index >= 0) {
//								inst.setParameter(const_index);
//								code.addElement(inst);
//								byte_count += 3;
//							} else
//								error(lineno, "Constant not declared: "
//										+ const_name);
//						}
//						break;

//						default:
//					}
					// error(lineno,"Parameters: OPcode: "+ inst.getOpcode() +" Type: "+inst.getType()+" Type: "+inst.getLabel()+" First: "+inst.getParameter()+" Second: "+inst.getParameter2());  
				} else if (inst.getParamNumber() == 0) {
					code.add(inst);
					++byte_count;
				} else {
					error(lineno, "Parameter(s) expected");
				}
			}
		}
		
		return isWide;
	}

	private void linkLabels() {
		for(IJVMInstruction instruction : code) {
			for(int count = 0; count < instruction.getParamNumber(); ++count) {
				if(instruction.getParamType(count).equals(ParamType.LABEL)) {
					Integer offset = labels.get(instruction.getLabel());
					if(offset == null) {
						error(instruction.getLineno(), "Invalid goto label: "
								+ instruction.getLabel());
						status = false;
					} else {
						instruction.getParameterValues().set(count,
								Integer.valueOf(offset.intValue() - instruction.getAddress()));
					}
				}
			}
		}
	}

	public boolean linkMethods(Hashtable<String, Integer> method_refs) {
		status = true;

		for(IJVMInstruction instruction : code) {
			for(int count = 0; count < instruction.getParamNumber(); ++count) {
				if(instruction.getParamType(count).equals(ParamType.OFFSET)) {
					String methodName = instruction.getLabel();
					Integer offset = method_refs.get(instruction.getLabel());
					if(offset == null) {
						error(instruction.getLineno(), "Method " + methodName + " is not defined");
						status = false;
					} else {
						instruction.getParameterValues().set(count, offset);
					}
				}
			}
		}

		return status;
	}

	public void generateLabels(BufferedWriter out_mne) throws IOException {

		String label_name = null;
		String label_address = null;
		Enumeration<String> enum_name = labels.keys();
		Enumeration<Integer> enum_address = labels.elements();

		while (enum_name.hasMoreElements()) {
			label_name = enum_name.nextElement();
			label_address = enum_address.nextElement().toString();
			out_mne.write(label_name, 0, label_name.length());
			out_mne.newLine();
			out_mne.write(label_address, 0, label_address.length());
			out_mne.newLine();
		}
		out_mne.write("end_labels", 0, 10);
		out_mne.newLine();
	}

	public void generate(OutputStream out, BufferedWriter out_mne)
			throws IOException {

		for(IJVMInstruction inst : code) {
			if(inst.isWide()) {
				out.write(ops.get("wide").getOpcode());
				out_mne.write("wide");
				out_mne.newLine();
			}
			out.write(inst.getOpcode());
			out_mne.write((inst.isWide() ? "wide_" : "") + inst.getInstruction().getMnemonic());
			out_mne.newLine();
			
			for(int count = 0; count < inst.getParamNumber(); ++count) {
				int paramValue = inst.getParameterValue(count);
				if(inst.getParamType(count).equals(ParamType.BYTE)) {
					if(inst.isWide()) {
						out.write((paramValue >> 8) & 255);
						out.write(paramValue & 255);
						out_mne.write("byte_1");
						out_mne.newLine();
						out_mne.write("byte_2");
						out_mne.newLine();
					} else {
						out.write(paramValue);
						out_mne.write("byte");
						out_mne.newLine();
					}
				} else if(inst.getParamType(count).equals(ParamType.CONST)) {
					if(inst.isWide()) {
						out.write((paramValue >> 8) & 255);
						out.write(paramValue & 255);
						out_mne.write("const_1");
						out_mne.newLine();
						out_mne.write("const_2");
						out_mne.newLine();
					} else {
						out.write(paramValue);
						out_mne.write("const");
						out_mne.newLine();
					}
				} else if(inst.getParamType(count).equals(ParamType.INDEX)) {
					out.write((paramValue >> 8) & 255);
					out.write(paramValue & 255);
					String index = (constants.elementAt(paramValue)).getName();
					out_mne.write(index + "_1");
					out_mne.newLine();
					out_mne.write(index + "_2");
					out_mne.newLine();
				} else if(inst.getParamType(count).equals(ParamType.LABEL)) {
					out.write((paramValue >> 8) & 255);
					out.write(paramValue & 255);
					String label = inst.getLabel();
					out_mne.write(label + "_1");
					out_mne.newLine();
					out_mne.write(label + "_2");
					out_mne.newLine();
				} else if(inst.getParamType(count).equals(ParamType.OFFSET)) {
					out.write((paramValue >> 8) & 255);
					out.write(paramValue & 255);
					String label = inst.getLabel();
					out_mne.write(label + "_1");
					out_mne.newLine();
					out_mne.write(label + "_2");
					out_mne.newLine();
				} else if(inst.getParamType(count).equals(ParamType.VARNUM)) {
					if(inst.isWide()) {
						out.write((paramValue >> 8) & 255);
						out.write(paramValue & 255);
						String var = varnums.elementAt(paramValue);
						out_mne.write(var + "_1");
						out_mne.newLine();
						out_mne.write(var + "_2");
						out_mne.newLine();
					} else {
						out.write(paramValue);
						out_mne.write(varnums.elementAt(paramValue));
						out_mne.newLine();
					}
				} else {
					error(inst.getLineno(), "Error in code generation");
					break;
				}
			}
		}
	}

	private int findConstant(String const_name) {
		for (int i = 0; i < constants.size(); ++i) {
			if (constants.elementAt(i).getName().equals(const_name)) {
				return i;
			}
		}
		return -1;
	}

	private String readLine() {
		int ctemp;
		String s = new String();
		try {
			int ch = 0;
			while (((char) ch) != '\n' && ch > -1) {
				ch = in.read();
				if (ch == -1 && s.length() == 0) {
					return null;
				}
				if (ch == '/') {
					ctemp = in.read();
					if ((char) ctemp == '/') {
						ch = in.read();
						while (((char) ch) != '\n' && ch > -1)
							ch = in.read();
						lineno++;
						return s + " ";
					} else if (ch != 13)
						s = s + ((char) ch)
								+ Character.toLowerCase((char) ctemp);
				}
				if (ch != '\n' && ch > -1 && ch != 13) {
					if ((char) ch == '\'') {
						ctemp = in.read();
						s = s + (char) ch + (char) ctemp;
					} else
						s = s + Character.toLowerCase((char) ch);
				}
			}
			lineno++;
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	private void error(int line, String msg) {
		err.println(line + ": " + msg);
	}

}

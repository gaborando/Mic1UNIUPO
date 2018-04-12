package mic1;

import java.util.Vector;

/*
 *
 *  mic1.IJVMInstruction.java
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

/**
 * Class that stores the instruction, address, and parameters for a given line
 * in a .jas file.
 * 
 * @author Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
 *         Ray Ontko & Co, Richmond, Indiana, US
 * @author Poli Francesco (modificato per gestire pi� parametri)
 */
public class IJVMInstruction {

	private Instruction instruction = null;
	private int address;
	private int lineno;
	private String label = null;
	private Vector<Integer> parameterValues = null;
	private boolean isWide = false;
	
	public IJVMInstruction(Instruction instruction) {
		this.instruction = instruction;
		parameterValues = new Vector<Integer>(instruction.getParamNumber());
	}

	public IJVMInstruction(Instruction instruction, int address) {
		this.instruction = instruction;
		this.address = address;
		parameterValues = new Vector<Integer>(instruction.getParamNumber());
	}

	public IJVMInstruction(Instruction instruction, int address, int lineno) {
		this.instruction = instruction;
		this.address = address;
		this.lineno = lineno;
		parameterValues = new Vector<Integer>(instruction.getParamNumber());
	}

	public IJVMInstruction(Instruction instruction, int address, String label) {
		this.instruction = instruction;
		this.address = address;
		this.label = label;
		parameterValues = new Vector<Integer>(instruction.getParamNumber());
	}

	public Instruction getInstruction() {
		return instruction;
	}

	public int getAddress() {
		return address;
	}

	public void setOpcode(int opcode) {
		this.instruction.setOpcode(opcode);
	}
	
	public int getOpcode() {
		return instruction.getOpcode();
	}
	
	public int getParameterValue(int paramIndex) {
		return parameterValues.elementAt(paramIndex).intValue();
	}
	
	public Vector<Integer> getParameterValues() {
		return parameterValues;
	}
	
	public void addParameterValue(int parameter) {
		parameterValues.add(Integer.valueOf(parameter));
	}
	
	public void setParameterValues(Vector<Integer> parameters) {
		this.parameterValues = parameters;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public int getLineno() {
		return lineno;
	}

	public void addParamTypes(ParamType paramType) {
		instruction.addParamTypes(paramType);
	}
	
	public void setParamTypes(Vector<ParamType> paramTypes) {
		instruction.setParamTypes(paramTypes);
		parameterValues = new Vector<Integer>(paramTypes.size());
	}
	
	public ParamType getParamType(int paramIndex) {
		return instruction.getParamType(paramIndex);
	}
	
	public Vector<ParamType> getParamTypes() {
		return instruction.getParamTypes();
	}
	
	public int getParamNumber() {
		return instruction.getParamNumber();
	}

	public String getMnemonic() {
		return this.instruction.getMnemonic();
	}

	public void setMnemonic(String nmemonic) {
		this.instruction.setMnemonic(nmemonic);
	}
	
	public boolean isWide() {
		return this.isWide;
	}
	
	public void setWide(boolean wide) {
		this.isWide = wide;
	}

}



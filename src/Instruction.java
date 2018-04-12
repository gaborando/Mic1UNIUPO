import java.util.Vector;

public class Instruction {

	private int opcode;
	private String mnemonic = null;
	private Vector<ParamType> paramTypes;

	public Instruction(int opcode, String mnemonic) {
		this(opcode, mnemonic, new Vector<ParamType>());
	}

	public Instruction(int opcode, String mnemonic, Vector<ParamType> paramTypes) {
		this.opcode = opcode;
		this.mnemonic = mnemonic;
		this.paramTypes = paramTypes;
	}
	
	public void setOpcode(int opcode) {
		this.opcode = opcode;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public int getOpcode() {
		return opcode;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void addParamTypes(ParamType paramType) {
		paramTypes.add(paramType);
	}
	
	public void setParamTypes(Vector<ParamType> paramTypes) {
		this.paramTypes = paramTypes;
	}
	
	public ParamType getParamType(int index) {
		return paramTypes.elementAt(index);
	}
	
	public Vector<ParamType> getParamTypes() {
		return paramTypes;
	}
	
	public int getParamNumber() {
		return paramTypes.size();
	}

//	/* *************** Deprecated members *************** */
//	
//	@Deprecated
//	public static final int NOPARAM = 0;
//
//	@Deprecated
//	public static final int BYTE = 1;
//
//	@Deprecated
//	public static final int CONST = 2;
//
//	@Deprecated
//	public static final int VARNUM = 3;
//
//	@Deprecated
//	public static final int LABEL = 4;
//
//	@Deprecated
//	public static final int OFFSET = 5;
//
//	@Deprecated
//	public static final int INDEX = 6;
//
//	@Deprecated
//	public static final int VARNUM_CONST = 7;
//
//	@Deprecated
//	public static final int VARNUM_VARNUM = 8;
//
//	@Deprecated
//	private int type;
//
//	@Deprecated
//	public Instruction(int opcode, String mnemonic, int type) {
//		this.opcode = opcode;
//		this.mnemonic = mnemonic;
//		this.type = type;
//	}
//	
//	@Deprecated
//	public int getType() {
//		return type;
//	}
//	
//	@Deprecated
//	public void setType(int type) {
//		this.type = type;
//	}
	
}


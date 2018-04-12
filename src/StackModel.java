/**
 * StackModel.java
 *
 * Data model for StackFrame
 *
 * @author 
 *   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
 *   U.P.O.
 *   Alessandria Italy
 */

import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

class StackModel extends AbstractTableModel implements Mic1Constants {

	private static final long serialVersionUID = 1L;

	MainMemory mM = null;
	StackFrame sF = null;
	int start = 0;

	static final public String address = "Word Address (hex)";
	static final public String hexWord = "Content (hex)";
	static final public String pointer = "Pointers";

	protected Vector data = new Vector(MEM_SHOWED);

	public StackModel(MainMemory mM, StackFrame sF) {
		this.mM = mM;
		this.sF = sF;
		for (int i = 0; i < MEM_SHOWED; i++)
			data.add(new Object());
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return MEM_SHOWED;
	}

	public Object getValueAt(int row, int col) {

		try {
			MemoryEntryL e = (MemoryEntryL) data.elementAt(row);
			switch (col) {
			case 0:
				return e.getAddress();
			case 1:
				return e.getHexWord();
			case 2:
				return e.getPointer();
			}
		} catch (Exception e) {
		}

		return null;

	}

	public String getColumnName(int col) {
		switch (col) {
		case 0:
			return address;
		case 1:
			return hexWord;
		case 2:
			return pointer;
		}
		return "";
	}

	public boolean isCellEditable(int row, int col) {

		if (col == 1)
			return true;
		else
			return false;
	}

	private int thereIsSimbol(Vector simbol, int address) {
		int i;

		for (i = 0; i < simbol.size(); i++)
			if (((Simbol) simbol.elementAt(i)).getAddress() == address)
				return i;
		return -1;
	}

	void setData(Vector Contain, int start) {
		String hexWord = null;
		int i, index;

		this.start = start;
		for (i = 0 + start; i < start + MEM_SHOWED; i++) {
			hexWord = (String) Contain.elementAt(i - start);
			MemoryEntryL e = new MemoryEntryL(i, hexWord, "");
			data.set(i - start, e);
		}

//		fireTableRowsUpdated(0, MEM_SHOWED);
		fireTableDataChanged();
	}

	public void setData(int index, int address) {

		String tempS = "";
		String cumul = "";
		for (int j = address * 4; j < (address * 4) + 4; j++) {
			tempS = MyByte.toHexString(mM.getByte(j));
			if (tempS.length() == 1)
				tempS = "0" + tempS;
			cumul += tempS;
		}
		MemoryEntryL e = (MemoryEntryL) data.elementAt(index);
		e.setHexWord(cumul);
		data.setElementAt(e, index);
		fireTableCellUpdated(index, 1);
	}

	public void setPointer(Vector Contain_Pointers) {

		String pointer = null;
		int i, index;

		for (i = 0; i < MEM_SHOWED; i++) {
			pointer = (String) Contain_Pointers.elementAt(i);
			MemoryEntryL e = (MemoryEntryL) data.elementAt(i);
			e.setPointer(pointer);
			data.setElementAt(e, i);
			fireTableCellUpdated(i, 3);
		}
//		fireTableRowsUpdated(2, MEM_SHOWED);
		fireTableDataChanged();
	}

	public void setValueAt(Object value, int row, int col) {
		// Only the column one is editable

		if (col == 1) {
			try {

				/* controlla se il byte e' corretto */
				String tmpS = (String) value;
				int lungh = ((String) value).length();
				if (lungh > 8)
					throw new OutRangeNumberException();
				if (lungh < 8)
					for (int i = lungh + 1; i <= 8; i++)
						tmpS += "0";
				int a;
				char[] value_char = tmpS.toCharArray();
				for (int i = 0; i < lungh; i++)
					a = Integer.parseInt("" + value_char[i], 16);
				int addr = row + start;
				int cont = 0;
				for (int i = 0; i < 8; i += 2) {
					byte b = MyByte.parseHexByte(((String) value).substring(i,
							i + 2));
					/* aggiorna la parola di memoria (visualizzata) */
					mM.setByte(4 * addr + cont, b);
					cont++;
				}
				MemoryEntryL e = (MemoryEntryL) data.elementAt(row);
				e.setHexWord((String) value);
				data.setElementAt(e, row);
				fireTableCellUpdated(row, col);

			} catch (NumberFormatException e) {
				JOptionPane
						.showMessageDialog(
								sF,
								"The string is not a valid representation of a number in base 16",
								"Error format", 2);
			} catch (OutRangeNumberException e1) {
				JOptionPane.showMessageDialog(sF,
						"The maximum length of the string must be 8",
						"Error lenght", 2);
			}
		}
	}

}

class MemoryEntryL {

	private String address = null;
	private String hexWord = null;
	private String pointer = null;

	public MemoryEntryL(int address, String hexWord, String pointer) {
		this.address = Integer.toHexString(address);
		this.hexWord = hexWord;
		this.pointer = pointer;
	}

	public String getAddress() {
		return address;
	}

	public String getPointer() {
		return pointer;
	}

	public String getHexWord() {
		return hexWord;
	}

	public void setHexWord(String hexWord) {
		this.hexWord = hexWord;
	}

	public void setPointer(String pointer) {
		this.pointer = pointer;
	}

}

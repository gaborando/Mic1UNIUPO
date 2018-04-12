import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * MainMemoryModel.java
 * 
 * Data model for MainMemoryFrame
 * 
 * @author Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
 *         U.P.O. Alessandria Italy
 */
class MainMemoryModel extends AbstractTableModel implements Mic1Constants {

	MainMemory mM = null;
	int start = 0;

	static final public String address = "Byte Address (hex)";
	static final public String hexWord = "Content (hex)";
	static final public String mnemonic = "Mnemonic";
	static final public String labels = "Labels";

	protected Vector data = new Vector(MEM_SHOWED);

	public MainMemoryModel(MainMemory mM) {
		this.mM = mM;
		for (int i = 0; i < MEM_SHOWED; i++)
			data.add(new Object());
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return MEM_SHOWED;
	}

	public Object getValueAt(int row, int col) {

		try {
			MemoryEntry e = (MemoryEntry) data.elementAt(row);
			switch (col) {
			case 0:
				return e.getAddress();
			case 1:
				return e.getHexWord();
			case 2:
				return e.getMnemonic();
			case 3:
				return e.getLabel();
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
			return mnemonic;
		case 3:
			return labels;
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
		if (simbol != null)
			for (i = 1; i < simbol.size(); ++i)
				// Attenzione... originariamente i partiva da 0
				if (((Simbol) simbol.elementAt(i)).getAddress() == address)
					return i;
		return -1;
	}

	void setData(Vector hexInstruction, Vector mnemonic, Vector constant,
			Vector labels, int start) {
		String hexWord = null;
		String mnemonic_word;
		String label_word;
		int i, index;

		this.start = start;

		for (i = start; i < (start + MEM_SHOWED); i++) {
			hexWord = (String) hexInstruction.elementAt(i - start);

			if (mnemonic != null && i < mnemonic.size())
				mnemonic_word = (String) mnemonic.elementAt(i);
			else if ((index = thereIsSimbol(constant, i)) != -1)
				mnemonic_word = ((Simbol) constant.elementAt(index)).getName();
			else
				mnemonic_word = null;

			/* recupero la label se c'e' */
			if ((index = thereIsSimbol(labels, i)) != -1)
				label_word = ((Simbol) labels.elementAt(index)).getName();
			else
				label_word = null;

			MemoryEntry e = new MemoryEntry(i, hexWord, mnemonic_word,
					label_word);
			data.set(i - start, e);
		}

		// fireTableRowsUpdated(0,MEM_SHOWED);
		fireTableDataChanged();
	}

	public void setData(int index, int address) {

		String hex_word = MyByte.toHexString(mM.getByte(address));
		MemoryEntry e = (MemoryEntry) data.elementAt(index);
		e.setHexWord(hex_word);
		data.setElementAt(e, index);
		fireTableCellUpdated(index, 1);
	}

	public void setValueAt(Object value, int row, int col) {
		// Only the column one is editable

		if (col == 1) {
			try {
				/* controlla se il byte e' corretto */
				byte b = MyByte.parseHexByte((String) value);
				/* aggiorna il byte di memoria (visualizzata) */
				mM.setByte(row + start, b);
				MemoryEntry e = (MemoryEntry) data.elementAt(row);
				e.setHexWord((String) value);
				data.setElementAt(e, row);
				fireTableCellUpdated(row, col);
			} catch (NumberFormatException e) {
				new ErrorDialog("error format",
						"The string is not a representation of a number in base 16");
			} catch (OutRangeNumberException e1) {
				new ErrorDialog("error format",
						"The maximum length of the string must be 2");
			}
		}
	}

}

class MemoryEntry {

	private String address = null;
	private String hexWord = null;
	private String mnemonic = null;
	private String label = null;

	public MemoryEntry(int address, String hexWord) {
		this.address = Integer.toHexString(address);
		this.hexWord = hexWord;
	}

	public MemoryEntry(int address, String hexWord, String mnemonic) {
		this.address = Integer.toHexString(address);
		this.hexWord = hexWord;
		this.mnemonic = mnemonic;
	}

	public MemoryEntry(int address, String hexWord, String mnemonic,
			String label) {
		this.address = Integer.toHexString(address);
		this.hexWord = hexWord;
		this.mnemonic = mnemonic;
		this.label = label;
	}

	public String getAddress() {
		return address;
	}

	public String getHexWord() {
		return hexWord;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public String getLabel() {
		return label;
	}

	public void setHexWord(String hexWord) {
		this.hexWord = hexWord;
	}

}

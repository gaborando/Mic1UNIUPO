/**
 * ControlStoreModel.java
 *
 *data model for ControlStoreFrame
 *
 * @author 
 *   Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
 *   U.P.O.
 *   Alessandria Italy
 */

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

class ControlStoreModel extends AbstractTableModel implements Mic1Constants {

	static final public String address = "Address";
	static final public String word = "Instruction";
	static final public String hexWord = "Hex instruction ";

	protected Vector<Entry> data = null;

	public ControlStoreModel() {
		data = new Vector<Entry>();
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return INSTR_COUNT;
	}

	public Object getValueAt(int row, int col) {

		try {
			Entry e = data.elementAt(row);
			switch (col) {
			case 0:
				return e.getAddress();
			case 1:
				return e.getWord();
			case 2:
				return e.getHexWord();
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
			return word;
		case 2:
			return hexWord;
		}
		return "";
	}

	public boolean isCellEditable(int row, int col) {
		if (col == 2)
			return true;
		return false;
	}

	void setData(ControlStore cs, Vector<String> hexInstruction) {
		String word = null;
		String hexWord = null;

		for (int i = 0; i < INSTR_COUNT; i++) {
			word = new String((cs.getInstruction(i)).toString());
			hexWord = hexInstruction.elementAt(i);
			Entry e = new Entry(i, word, hexWord);
			data.add(e);
		}
	}

	void updateEntry(int address, String word) {

		Entry e = data.elementAt(address);
		String hex = e.getHexWord();
		data.setElementAt(new Entry(address, word, hex), address);
		// fireTableRowsUpdated(address,address);
		fireTableDataChanged();
	}

	public void setValueAt(Object value, int row, int col) {
		// Only the second column is editable

		if (col == 2) {
			try {
				long l = Long.parseLong((String) value, 16);
				if (((String) value).length() > 9)
					throw new OutRangeNumberException();
				Entry e = new Entry(row, "", (String) value);
				data.setElementAt(e, row);
				fireTableCellUpdated(row, col);
			} catch (NumberFormatException e) {
				// JOptionPane.showMessageDialog(this, "The string is not a
				// representation of a number in base 16", "Error number
				// format", 2);
				new ErrorDialog("Error number format",
						"The string is not a representation of a number in base 16");
			} catch (OutRangeNumberException e1) {
				// JOptionPane.showMessageDialog(this, "The maximum length of
				// the string must be 9", "Error number format", 2);
				new ErrorDialog("Error number format",
						"The maximum length of the string must be 9");
			}
		}
	}
}

class Entry {

	private String address = null;
	private String word = null;
	private String hexWord = null;

	public Entry(int address, String word, String hexWord) {
		this.address = Integer.toHexString(address);
		this.word = word;
		this.hexWord = hexWord;
	}

	public String getAddress() {
		return address;
	}

	public String getWord() {
		return word;
	}

	public String getHexWord() {
		return hexWord;
	}

}

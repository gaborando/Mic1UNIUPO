/**
 * ControlStoreFrame.java
 *
 * Window that displays the control store.
 * Consists of:
 *  - a table with 3 coloumn:
 *    	- address of word (in base 16)
 *	- mnemonic code
 *	- word of memory (in base 16)
 *
 * @author 
 *   Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
 *   U.P.O.
 *   Alessandria Italy
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

public class ControlStoreFrame extends JFrame implements TableModelListener,
		Mic1Constants {

	ControlStoreModel model = null;
	ControlStore cs = null;
	JTable table = null;
	private Vector hexInstruction = new Vector();

	/* select memory word at address MPC */
	int old_mpc = -1;

	public ControlStoreFrame(ControlStore cs) {

		super("Control Store");
		this.cs = cs;

		model = new ControlStoreModel();

		for (int i = 0; i < INSTR_COUNT; i++) {
			hexInstruction.add(Long.toHexString(cs.getInstruction(i).toHex()).toUpperCase());
		}
		model.setData(cs, hexInstruction);
		model.addTableModelListener(this);

		table = new JTable(model);
		table.setFont(Font.decode("Courier New"));
		table.setPreferredScrollableViewportSize(new Dimension(400, 330));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this window.
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				mic1sim.control_memory = false;
			}
		});

		setLocation(30, 360);
		pack();
		setVisible(true);
		table.removeRowSelectionInterval(0, 0);
	}

	public void tableChanged(TableModelEvent e) {

		int row = e.getFirstRow();
		int column = e.getColumn();
		String columnName = model.getColumnName(column);
		String value = (String) model.getValueAt(row, column);

		try {
			long l = Long.parseLong(value, 16);
			Mic1Instruction i = new Mic1Instruction();
			i.read(l);
			cs.setInstruction(row, i);
			model.updateEntry(row, i.toString());
		} catch (NumberFormatException e1) {
		}
	}

	public void selectMpc(int mpc) {

		table.setRowSelectionInterval(mpc, mpc);
		old_mpc = mpc;

	}

	public void removeSelectMpc() {
		if (old_mpc >= 0)
			table.removeRowSelectionInterval(old_mpc, old_mpc);
	}

	public void reset() {
		old_mpc = -1;
	}

}

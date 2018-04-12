
/**
 * MainMemoryFrame.java
 *
 * Window that displays the main memory.
 * Consists of:
 *  - a table with 4 coloumn:
 *    	- address of word (in base 16)
 *	- word of memory (in base 16)
 *	- mnemonic code
 *	- label
 *  - a text field for the first address of memory that to be displayed
 *  - a button for load the portion of memory
 *
 * @author 
 *   Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
 *   U.P.O.
 *   Alessandria Italy
 *
 * updated by
 *   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
 *   U.P.O.
 *   Alessandria Italy
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class MainMemoryFrame extends JFrame implements TableModelListener,
		ActionListener, Mic1Constants {

	MainMemoryModel model = null;
	MainMemory mM = null;
	private Vector hexInstruction = new Vector(MEM_SHOWED);
	private Vector mnemonic = null;
	private Vector constant = null;
	private Vector labels = null;
	JButton okButton = null;
	JTextField start = null;
	JTable table = null;
	Color p = new Color(50, 200, 150);

	int begin = 0;
	int end = MEM_SHOWED;

	/* select memory word at address PC */
	int old_index = -1;
	int old_pc = -1;
	boolean selected = false;

	boolean Breakpointed = false;

	public MainMemoryFrame(MainMemory mM, Vector mnemonic, Vector constant,
			Vector labels, Vector pippo) {

		super("Method Area");
		this.mM = mM;
		this.mnemonic = mnemonic;
		this.constant = constant;
		this.labels = labels;

		model = new MainMemoryModel(mM);

		for (int i = 0; i < MEM_SHOWED; i++)
			hexInstruction.add(MyByte.toHexString(mM.getByte(i)));
		model.setData(hexInstruction, mnemonic, constant, labels, begin);

		table = new JTable(model);
		table.setFont(Font.decode("Courier New"));
		table.setPreferredScrollableViewportSize(new Dimension(300, 550));

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		JPanel p1 = new JPanel(new FlowLayout());
		Label l1 = new Label(
				"Displayed memory address range: [initial address , initial address+512]");
		p1.add(l1);

		JPanel p2 = new JPanel(new FlowLayout());
		Label l2 = new Label(
				"To modify the initial address insert a new hexadecimal value (smaller than 0x3fdff) and press OK");
		p2.add(l2);

		JPanel p3 = new JPanel(new FlowLayout());
		Label l3 = new Label("First address");
		start = new JTextField(4);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		p3.add(l3);
		p3.add(start);
		p3.add(okButton);

		JPanel p4 = new JPanel(new GridLayout(3, 1));

		p4.add(p1);
		p4.add(p2);
		p4.add(p3);

		// Add the scroll pane to this window.
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(p4, BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				mic1sim.memory = false;
			}
		});
		pack();
		setLocation(550, 0);
		setVisible(true);
		table.removeRowSelectionInterval(0, 0);

	}

	public void tableChanged(TableModelEvent e) {

	}

	public void selectBreakpoint(int BC) {

		int index = BC;
		if ((begin <= BC) && (end >= BC)) {
			index = BC - begin;
			table.setRowSelectionInterval(index, index);

			Breakpointed = true;
		}
	}

	public void selectPc(int pc) {

		int index = pc;
		if ((begin <= pc) && (end >= pc)) {
			index = pc - begin;
			table.setRowSelectionInterval(index, index);
			table.setGridColor(p);
			selected = true;
		} else if (selected)
			removeSelectPc();
		old_pc = pc;
		old_index = index;
	}

	public void removeSelectPc() {
		if (selected) {
			table.removeRowSelectionInterval(old_index, old_index);
			selected = false;
		}
	}

	public void reset() {
		removeSelectPc();
		old_index = -1;
		old_pc = -1;
	}

	public void update(int address) {

		int i = 0;

		if ((begin <= address) && (end + 4 >= address)) {
			int index = address - begin;
			do {
				model.setData(index, address + i);
				++i;
				++index;
			} while ( (i < 4) && (index <= end + 4));
		}

		mic1sim.update = false;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "ok") {

			try {
				begin = Integer.parseInt(start.getText(), 16);
				if (begin >= MEM_MAX - MEM_SHOWED)
					throw new OutRangeNumberException();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(this,
						"The string is not a valid representation of a number",
						"Error format", 2);
				// new ErrorDialog("error format","The string is not a valid
				// representation of a number");
				begin = 0;
			} catch (OutRangeNumberException e2) {
				JOptionPane.showMessageDialog(this,
						"The start address must be less than 0x3fdff",
						"Error range", 2);
				// new ErrorDialog("Error range","The start address must be less
				// than 0x3fdff");
				begin = 0;
			}

			end = begin + MEM_SHOWED;
			for (int i = begin; i < end; i++)
				hexInstruction
						.add(i - begin, MyByte.toHexString(mM.getByte(i)));

			model.setData(hexInstruction, mnemonic, constant, labels, begin);
			selectPc(old_pc);
		}
	}

}

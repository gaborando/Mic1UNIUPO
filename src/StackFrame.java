/**
 * StackFrame.java
 *
 * Window that displays the Stack (Memory).
 *
 * @author 
 *   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
 *   U.P.O.
 *   Alessandria Italy
 */

import java.awt.BorderLayout;
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

public class StackFrame extends JFrame implements TableModelListener,
		ActionListener, Mic1Constants {

	StackModel model = null;
	MainMemory mM = null;
	private Vector<String> Contain = new Vector<String>(MEM_SHOWED);
//	private Vector constant = null;
	JButton okButton = null;
	JTextField start = null;
	JTable table = null;

	String string_pointer = new String("<-- SP");

	int Local_Pointers[][];

	int temp, begin_old, begin = Mic1Constants.SP_ORIGINAL;
	int end = begin + MEM_SHOWED;
	String N = "";

	public StackFrame(MainMemory mM, int P[][]) {

		super("Main Memory (4 byte words)");
		this.mM = mM;

		model = new StackModel(mM, this);
		String tempS = "";
		String cumul = "";

		for (int i = begin; i < begin + MEM_SHOWED; i++) {
			cumul = "";
			for (int j = i * 4; j < i * 4 + 4; j++) {
				tempS = MyByte.toHexString(mM.getByte(j));
				if (tempS.length() == 1)
					tempS = "0" + tempS;
				cumul += tempS;
			}
			Contain.add(cumul);
		}
		model.setData(Contain, begin);
		model.addTableModelListener(this);

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
				"To modify the initial address insert a new hexadecimal value (smaller than 0xff7f) and press OK");
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
				mic1sim.stack = false;
			}
		});
		pack();
		setLocation(550, 10);
		setVisible(true);
		table.removeRowSelectionInterval(0, 0);
	}

	public void tableChanged(TableModelEvent e) {
	}

	public void reset() {
	}

	public void update(int address) {
		if ((begin <= address) && (end >= address)) {
			int index = address - begin;
			model.setData(index, address);
		}
		mic1sim.update_Stack = false;
	}

	public void updatePointer(int Pointers[][]) {

		Vector<String> P = new Vector<String>(MEM_SHOWED);

		Local_Pointers = Pointers;

		for (int i = 0; i < MEM_SHOWED; i++) {
			if (Pointers[0][i + begin] != 0)
				N = "#" + Integer.toString(Pointers[0][i + begin]);
			else
				N = "";

			{
				if ((Pointers[1][i + begin] == 0)
						&& (Pointers[2][i + begin] == 0)
						&& (Pointers[3][i + begin] == 0))
					P.add("<-- CPP, SP " + N + ", LV " + N);
				else {
					if ((Pointers[1][i + begin] == 0)
							&& (Pointers[2][i + begin] == 0)
							&& (Pointers[3][i + begin] != 0))
						P.add("<-- SP " + N + ", LV " + N);
					else {
						if ((Pointers[3][i + begin] == 0)
								&& (Pointers[2][i + begin] == 0)
								&& (Pointers[1][i + begin] != 0))
							P.add("<-- CPP, LV " + N);
						else {
							if ((Pointers[1][i + begin] == 0)
									&& (Pointers[3][i + begin] == 0)
									&& (Pointers[2][i + begin] != 0))
								P.add("<-- CPP, SP " + N);
							else {
								if ((Pointers[1][i + begin] == 0)
										&& (Pointers[3][i + begin] != 0)
										&& (Pointers[2][i + begin] != 0))
									P.add("<-- SP " + N);
								else {
									if ((Pointers[2][i + begin] == 0)
											&& (Pointers[3][i + begin] != 0)
											&& (Pointers[1][i + begin] != 0))
										P.add("<-- LV " + N);
									else {
										if ((Pointers[3][i + begin] == 0)
												&& (Pointers[1][i + begin] != 0)
												&& (Pointers[2][i + begin] != 0))
											P.add("<-- CPP");
										else
											P.add(N);
									}
								}
							}
						}
					}
				}
			}
		}
		// qui si passa dall'array rappresentante tutta l'area di Stack ad un
		// vector che rappresenta solo l'area visualizzata.
		if (Pointers[4][0] == 1) {
			model.setPointer(P);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "ok") {

			try {
				temp = Integer.parseInt(start.getText(), 16);
				begin_old = begin;
				begin = temp;
				if (begin >= MEM_MAX_S - MEM_SHOWED)
					throw new OutRangeNumberException();
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(this,
						"The string is not a valid representation of a number",
						"Error format", 2);
				begin = Mic1Constants.SP_ORIGINAL;
			} catch (OutRangeNumberException e2) {
				JOptionPane.showMessageDialog(this,
						"The start address must be less than oxfdff",
						"Error range", 2);
				begin = Mic1Constants.SP_ORIGINAL;
			}

			end = begin + MEM_SHOWED;

			String cumul = "";
			String tempS = "";

			Contain.removeAllElements();
			for (int i = begin; i < end; i++) {
				cumul = "";
				for (int j = i * 4; j < (i * 4) + 4; j++) {
					tempS = MyByte.toHexString(mM.getByte(j));
					if (tempS.length() == 1)
						tempS = "0" + tempS;
					cumul += tempS;
				}
				Contain.add(cumul);
			}
			model.setData(Contain, begin);
			updatePointer(Local_Pointers);
		}
	}

}

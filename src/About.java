import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class About extends Frame implements WindowListener {

	private static final long serialVersionUID = 1L;

	boolean on = true;

	About() {
		super("About");
		this.setSize(500, 500);
		String infos[] = {
				"MIC1 microarchitecture simulator",
				"",
				"Copyright (C) 1999, Prentice-Hall, Inc.",
				"",
				"  This program is free software; you can redistribute it and/or modify ",
				"  it under the terms of the GNU General Public License as published by ",
				"  the Free Software Foundation; either version 2 of the License, or ",
				"  (at your option) any later version. ",
				"",
				"  This program is distributed in the hope that it will be useful, but",
				"  WITHOUT ANY WARRANTY; without even the implied warranty of ",
				"  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.",
				"  See the GNU General Public License for more details. ",
				"",
				"  You should have received a copy of the GNU General Public License along with",
				"  this program; if not, write to: ", "",
				"  Free Software Foundation, Inc.",
				"  59 Temple Place - Suite 330 ",
				"  Boston, MA 02111-1307, USA. ", "",
				"  A copy of the GPL is available online the GNU web site: ",
				"  http://www.gnu.org/copyleft/gpl.html", "", "Modified by ",
				"  1999 Claudio Bertoncello", "  2001 Simone Alciati",
				"  2006 Silvio Colombaro ( silvio.colombaro@email.it )",
				"  2008 Francesco Poli" };

		Label l;
		this.setLayout(new GridLayout(infos.length, 1));
		for (int i = 0; i < infos.length; i++) {
			l = new Label(infos[i]);
			this.add(l);
		}

		this.addWindowListener(this);
		this.setVisible(true);
	}

	public boolean isOn() {
		return on;
	}

	public void windowActivated(WindowEvent arg0) {

	}

	public void windowClosed(WindowEvent arg0) {
		// on=false;
		// this.dispose();

	}

	public void windowClosing(WindowEvent arg0) {
		on = false;
		this.dispose();

	}

	public void windowDeactivated(WindowEvent arg0) {

	}

	public void windowDeiconified(WindowEvent arg0) {

	}

	public void windowIconified(WindowEvent arg0) {

	}

	public void windowOpened(WindowEvent arg0) {

	}
}

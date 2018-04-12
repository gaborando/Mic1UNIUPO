/****
*InsertBreakpointFrame.java
*
*Window used to insert a Breakpoint.
*
* @author 
*   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
*   U.P.O.
*   Alessandria Italy
****/


import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class InsertBreakpointFrame extends JFrame implements ActionListener, Mic1Constants{

  Vector Local_Breakpoint_Vector;
  BreakpointFrame Local_BK_Frame;
  JButton INSERT = null;
  JButton CLOSE_INS = null;
  JLabel INS = null;
  JTextField TESTO_INS = null;
  int begin = 0;

  
  public InsertBreakpointFrame(Vector Breakpoint_Vector, BreakpointFrame BK_Frame){
    
    Dialog BD_INS = new Dialog(this, "Insert Breakpoint");
    INSERT = new JButton("OK");
    CLOSE_INS = new JButton("Cancel");
    INS = new JLabel("Ins. breakpoint at line");
    TESTO_INS = new JTextField(4);    
    

    
    BD_INS.setLayout(new BorderLayout(10,20));

    Panel North_Panel = new Panel();
    North_Panel.setLayout(new GridLayout(1,2,10,10));
    
    Panel Center_Panel = new Panel(new GridLayout(2,2,10,10));
    Center_Panel.add(INS);
    Center_Panel.add(TESTO_INS);

    Center_Panel.add(INSERT);
    INSERT.addActionListener(this);

    Center_Panel.add(CLOSE_INS);
    CLOSE_INS.addActionListener(this);
      
    BD_INS.add("Center", Center_Panel);
    BD_INS.add("North", North_Panel);

    BD_INS.setVisible(true);
    BD_INS.setSize(getPreferredSize());
    BD_INS.paintAll(getGraphics());    

    Local_Breakpoint_Vector = Breakpoint_Vector;
    Local_BK_Frame = BK_Frame;
  }

  
  public Insets insets(){
	return new Insets(70,180,70,180);
  }
  
  public void actionPerformed(ActionEvent e)   
   {
   if (e.getActionCommand() == "OK")
	{	
	try{		
		begin = Integer.parseInt(TESTO_INS.getText(),16); 
		if (begin >= MEM_MAX) throw new OutRangeNumberException ();  		
		if (!Local_Breakpoint_Vector.contains("" + begin)){
			Local_Breakpoint_Vector.addElement("" + begin);
			ModificaFrame();
		      }

		dispose();		

	    }catch(NumberFormatException e1){
		JOptionPane.showMessageDialog(this, "The string is not a valid representation of a number", "Error format", 2);
		//new ErrorDialog("error format","The string is not a valid representation of a number");		
		begin=0;
	    }catch(OutRangeNumberException e2){			
		JOptionPane.showMessageDialog(this, "The specify address must be a valid Main Memory addres", "Error range", 2);		
		//new ErrorDialog("Error range","The specify address must be a valid Main Memory addres");
		begin=0;
		}
	 }
   if (e.getActionCommand() == "Cancel"){dispose();}
   
	
   }
   
     public boolean handleEvent(Event event) {

      if (event.id == Event.WINDOW_DESTROY) {dispose(); return true;} else return false; 
    }

  public void ModificaFrame(){
  String tmp = "";
  int cont = 0;
  int cont_BK = 0;

	cont_BK = Local_Breakpoint_Vector.size();
	for (int i = 0; i <= MEM_MAX; i++){
		if (cont_BK<=cont) break;
		if (Local_Breakpoint_Vector.contains((Object)("" + i))) {
			tmp = tmp + "-> " + Integer.toHexString(i) + "\n\n";
			cont++;
			}
		}

  	Local_BK_Frame.List_of_BK.setText(tmp);
	Local_BK_Frame.repaint();
	Local_BK_Frame.setVisible(true);

	Local_BK_Frame.setSize(getPreferredSize());
	Local_BK_Frame.paintAll(getGraphics());

  }



}  

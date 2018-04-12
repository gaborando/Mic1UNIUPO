
/****
*DeleteBreakpointFrame.java
*
*Window used to delete a Breakpoint.
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

public class DeleteBreakpointFrame extends JFrame implements ActionListener, Mic1Constants{

  Vector<String> Local_Breakpoint_Vector;
  BreakpointFrame Local_BK_Frame;
  JButton DELETE = null;
  JButton CLOSE_DEL = null;
  JLabel DEL = null;
  JTextField TESTO_DEL = null;
  int begin = 0;

  public DeleteBreakpointFrame(Vector<String> Breakpoint_Vector, BreakpointFrame BK_Frame){
    
    Dialog BD_DEL = new Dialog(this, "Delete Breakpoint");
    DELETE = new JButton("OK");
    CLOSE_DEL = new JButton("Cancel");
    DEL = new JLabel("Del. breakpoint at line ");
    TESTO_DEL = new JTextField(4);
      
    BD_DEL.setLayout(new BorderLayout(10,20));
    

    Panel North_Panel = new Panel();
    North_Panel.setLayout(new GridLayout(1,2,10,10));
    Panel Center_Panel = new Panel(new GridLayout(2,2,10,10));
    Center_Panel.add(DEL);
    Center_Panel.add(TESTO_DEL);    
    
//    DELETE.setSize(30,20);
    Center_Panel.add(DELETE);
    DELETE.addActionListener(this);

 //   CLOSE_DEL.setSize(30,20);
    Center_Panel.add(CLOSE_DEL);
    CLOSE_DEL.addActionListener(this);

 //   Center_Panel.setSize(300,120);


    BD_DEL.add("Center", Center_Panel);
    BD_DEL.add("North", North_Panel);    

    BD_DEL.setVisible(true);
    BD_DEL.setSize(getPreferredSize());
    BD_DEL.paintAll(getGraphics());    

    Local_BK_Frame = BK_Frame;
    Local_Breakpoint_Vector = Breakpoint_Vector;
  }
  
  public Insets insets(){
	return new Insets(70,180,70,180);
  }  
  
  
  public void actionPerformed(ActionEvent e)
   {
   if (e.getActionCommand() == "OK")
	{		
	try{
		begin = Integer.parseInt(TESTO_DEL.getText(),16); 
		if (Local_Breakpoint_Vector.contains(Integer.toString(begin)))
			{ 
			Local_Breakpoint_Vector.removeElement(Integer.toString(begin));
			ModificaFrame();
			dispose();
			}
			else throw new ElementInVectorException();
 		
		
	}catch(NumberFormatException e1){
		JOptionPane.showMessageDialog(this, "The string is not a valid representation of a number", "Error format", 2);		
		//new ErrorDialog("Error format","The string is not a valid representation of a number");		
		begin=0;
	}catch(ElementInVectorException e2){		
		if (Local_Breakpoint_Vector.isEmpty())
		{ 
			JOptionPane.showMessageDialog(this, "You must insert a Breakpoint before delete it!!!", "Error", 2);
			//new ErrorDialog("Error","You mast insert a Breakpoint before delete it!!!"); 
			dispose();
		}
		else {
			new ErrorDialog("Error","There is no a Breakpoint at the address " + TESTO_DEL.getText());
			begin=0;
		}
		}
	}

   if (e.getActionCommand() == "Cancel"){dispose();}
	}

   
     public boolean handleEvent(Event event) {
		if (event.id == Event.WINDOW_DESTROY) {
			dispose();
			return true;
		}
		return false;
	}


  public void ModificaFrame(){
  String tmp = "";
  int cont = 0;
  int cont_BK = 0;

	cont_BK = Local_Breakpoint_Vector.size();
	if (cont_BK == 0) tmp = "There are no valid Breakpoints now !!!";
		else for (int i = 0; i <= MEM_MAX; i++){
			if (cont_BK<=cont) break;
			if (Local_Breakpoint_Vector.contains(("" + i))) {
				tmp = tmp + "-> " + Integer.toHexString(i) + "\n\n";
				cont++;
				}
			}

  	Local_BK_Frame.List_of_BK.setText(tmp);
	Local_BK_Frame.repaint();
	Local_BK_Frame.setSize(getPreferredSize());
	Local_BK_Frame.paintAll(getGraphics());

  }



}  

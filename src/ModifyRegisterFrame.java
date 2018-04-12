
/****
*ModifyRegisterFrame.java
*
*Window used to modify the value of a register.
*
* @author 
*   Simone Alciati (e-mail: alciati@edu-al.unipmn.it),
*   U.P.O.
*   Alessandria Italy
****/


import java.awt.Choice;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ModifyRegisterFrame extends JFrame implements ActionListener, Mic1Constants{

  mic1sim s_local;
  JLabel JLabel1 = new JLabel("Change register");
  Choice os;
  JLabel JLabel2 = new JLabel("to value");
  JTextField Value = new JTextField(4);
  JButton CloseButton = new JButton("Close");
  JButton ModifyButton = new JButton("Modify");

  int test, sp_value_old, lv_value_old;

  public ModifyRegisterFrame(mic1sim s){

	Dialog ModifyDialog = new Dialog(this, "Modify Register");
	ModifyDialog.setLayout(new GridLayout(3,2,10,20));

	os = new Choice();

	os.addItem("MAR");
	os.addItem("MDR");
	os.addItem("PC");
	os.addItem("MBR");
	os.addItem("SP");
	os.addItem("LV");
	os.addItem("CPP");
	os.addItem("TOS");
	os.addItem("OPC");
	os.addItem("H");

	ModifyButton.setSize(30,20);
	CloseButton.setSize(30,20);

	ModifyDialog.add(JLabel1);
	ModifyDialog.add(os);
	ModifyDialog.add(JLabel2);
	ModifyDialog.add(Value);
	ModifyDialog.add(ModifyButton);
	ModifyDialog.add(CloseButton);

	CloseButton.addActionListener(this);
	ModifyButton.addActionListener(this);

	ModifyDialog.setVisible(true);
 	ModifyDialog.setSize(getPreferredSize());
	ModifyDialog.paintAll(getGraphics());    

	s_local = s;
  }

  public Insets insets(){
	return new Insets(70,180,70,180);
  }

  public void actionPerformed(ActionEvent e)   
   {

   if (e.getActionCommand() == "Modify")
	{
	   try{
		test = Integer.parseInt(Value.getText(),16);
		if ((s_local.Pointers[4][0]==1) && (test < -1) && ((os.getSelectedIndex()==4)||(os.getSelectedIndex()==5)||(os.getSelectedIndex()==6))) throw new NumberFormatException();
		Modify(os.getSelectedIndex(),Value.getText());
	    }catch(NumberFormatException e1){
		JOptionPane.showMessageDialog(this, "The string is not a valid representation of an hexadecimal number \n (greater than -1)", "Error format", 2);		
		//new ErrorDialog("error format","The string is not a valid representation of a number");		
	    }
	}
   if (e.getActionCommand() == "Close"){dispose();}

   }

   public void Modify(int SelectedRegister, String ValueOfRegister){
	try{
	  if (false) throw new RegisterValueException();
	/*"MAR" correspond to index 0
	"MDR" correspond to index 1
	"PC" correspond to index 2
	"MBR" correspond to index 3
	"SP" correspond to index 4
	"LV" correspond to index 5
	"CPP" correspond to index 6
	"TOS" correspond to index 7
	"OPC" correspond to index 8
	"H" correspond to index 9*/
	  switch (SelectedRegister){
	     case (0):
	     	s_local.mar.forceValue(Integer.parseInt(ValueOfRegister,16));
		s_local.o_addr_cl.setValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (1):
	     	s_local.mdr.forceValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (2):
	     	s_local.pc.forceValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (3):
	     	s_local.mbr.forceValue(Integer.parseInt(ValueOfRegister,16));
		s_local.o_mbr_cl.setValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (4):
	        if (s_local.Pointers[4][0] == 1)
		    {
		    s_local.Pointers[1][s_local.sp.value] = 1;
		    sp_value_old = s_local.sp.value;
		    }
	     	s_local.sp.forceValue(Integer.parseInt(ValueOfRegister,16));
		if (s_local.Pointers[4][0] == 1)
		    {
		        s_local.Pointers[1][s_local.sp.value] = 0;
			if (s_local.lv.value<s_local.sp.value){
				for(int r=s_local.lv.value; r<=s_local.sp.value; r++){
  					s_local.Pointers[0][r]=s_local.allocation_level; 
    					}
				if (sp_value_old>s_local.sp.value) 
				       for(int r=s_local.sp.value+1; r<=sp_value_old; r++){
  					s_local.Pointers[0][r]=0; 
    					}
			}
   			 if (s_local.lv.value==s_local.sp.value)
				for(int r=s_local.lv.value; r<=sp_value_old; r++){
  					s_local.Pointers[0][r]=0; 
    				}
			 if (s_local.lv.value>s_local.sp.value) /*?????????*/
				for(int r=s_local.lv.value; r<=sp_value_old; r++){
  					s_local.Pointers[0][r]=0; 
    				}
	      	     }
		break;
	     case (5):
	        if (s_local.Pointers[4][0] == 1)
		    {
	     		s_local.Pointers[2][s_local.lv.value] = 1;
			lv_value_old = s_local.lv.value;
		     }
		s_local.lv.forceValue(Integer.parseInt(ValueOfRegister,16));
		if (s_local.Pointers[4][0] == 1)
		    {
			s_local.Pointers[2][s_local.lv.value] = 0;
			if (s_local.lv.value<s_local.sp.value){
				for(int r=s_local.lv.value; r<=s_local.sp.value; r++){
  					s_local.Pointers[0][r]=s_local.allocation_level; 
    					}
					if (s_local.lv.value>lv_value_old) 
					       for(int r=lv_value_old; r<=s_local.lv.value-1; r++){
  						s_local.Pointers[0][r]=0; 
    						}
				}
		
	
	   		 if (s_local.lv.value==s_local.sp.value)
				for(int r=lv_value_old; r<=s_local.sp.value; r++){
	  				s_local.Pointers[0][r]=0; 
	    			}
			 if (s_local.lv.value>s_local.sp.value) /*?????????*/
				for(int r=lv_value_old; r<=s_local.sp.value; r++){
	  				s_local.Pointers[0][r]=0; 
	    			}
			 if (s_local.allocation_level == 0) s_local.LV_0 = s_local.lv.value;
		     }
		break;
	     case (6):
	     	if (s_local.Pointers[4][0] == 1) s_local.Pointers[3][s_local.cpp.value] = 1;
	     	s_local.cpp.forceValue(Integer.parseInt(ValueOfRegister,16));
		if (s_local.Pointers[4][0] == 1) s_local.Pointers[3][s_local.cpp.value] = 0;
		break;
	     case (7):
	     	s_local.tos.forceValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (8):
	     	s_local.opc.forceValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     case (9):
	     	s_local.h.forceValue(Integer.parseInt(ValueOfRegister,16));
		break;
	     default:
	     }
	    if (s_local.stack) s_local.Stack_Frame.updatePointer(s_local.Pointers);
	    dispose();
	}catch(RegisterValueException e1){
	    JOptionPane.showMessageDialog(this, "The register  " + SelectedRegister + " can' t equals " + ValueOfRegister + ".", "Error", 2);
	}

   }

}

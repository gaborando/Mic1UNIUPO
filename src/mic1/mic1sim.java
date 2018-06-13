package mic1;/*
*
*  mic1.mic1sim.java
*
*  mic1 microarchitecture simulator 
*  Copyright (C) 1999, Prentice-Hall, Inc. 
* 
*  This program is free software; you can redistribute it and/or modify 
*  it under the terms of the GNU General Public License as published by 
*  the Free Software Foundation; either version 2 of the License, or 
*  (at your option) any later version. 
* 
*  This program is distributed in the hope that it will be useful, but 
*  WITHOUT ANY WARRANTY; without even the implied warranty of 
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
*  Public License for more details. 
* 
*  You should have received a copy of the GNU General Public License along with 
*  this program; if not, write to: 
* 
*    Free Software Foundation, Inc. 
*    59 Temple Place - Suite 330 
*    Boston, MA 02111-1307, USA. 
* 
*  A copy of the GPL is available online the GNU web site: 
* 
*    http://www.gnu.org/copyleft/gpl.html
* 
*/ 

import mic1.gaborFix.IJVMEditor;
import mic1.gaborFix.RememberPositionFrame;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Vector;

import javax.swing.UIManager;

/**
* Main component of the mic1 simulator.  All components are
* created in this class, and displayed in the mic1.mic1sim frame.
* mic1.mic1sim also handles all keyboard and mouse events, loads
* micro and macro programs, and coordinates the sequence of 
* activities for each clock cycle.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
* updatet by
    Simone Alciati (e-mail: alciati@edu-al.unipmn.it) (last in date 5.2001)
    Claudio Bertoncello (e-mail: cle@edu-al.unipmn.it) (last in date 12.1999)
*/
public class mic1sim extends RememberPositionFrame implements Mic1Constants {

  static mic1sim s = null;
  private Vector mnemonic = null;	
  private Vector constant = null;	
  private Vector labels = null;
  private IJVMcompiler ijvmCompiler = null;	
  private Mic1compiler mic1Compiler = null;
  public static TextArea stdout = new TextArea(5, 50);
  public static boolean debug = false;
  public static boolean stack = false;	// a Stack frame is open
  public static boolean Breakpoint = false;
  public static boolean memory = false;	// a main memory frame is open
  public static boolean update_Stack = false;	// if stack = true updates data Stack
  public static boolean update = false;	// if memory = true updates data memory
  public static int address = -1;	// address of memory that must be update
  public static int address_Stack = -1;	// address of Stack that must be update
  public static boolean control_memory = false; // a control store frame is open
  public static boolean halt = false;
  public static boolean run = false;
  
  public static Vector key_buffer = new Vector();
  public Vector Breakpoint_Vector = new Vector();
  public int Pointers[][] = new int [5][MEM_MAX_S]; // 4 colonne: una per i livelli di annidamento, una per la posizione di SP (valore = 0 se li c'e' SP) , una per la posizione di LV(idem come per SP), una che contiene il flag per la visualizzazione della terza colonna mella finstra "memory"
  
  DebugFrame debug_frame = null;
  ControlStoreFrame cs_frame = null;
  MainMemoryFrame mM_frame = null;
  StackFrame Stack_Frame = null;
  InsertBreakpointFrame IB_frame = null;
  BreakpointFrame BK_List = new BreakpointFrame();
  DeleteBreakpointFrame DB_frame = null;
  ModifyRegisterFrame Modify = null;
  MenuBar menubar = null;
  Menu file = null;
  Menu window = null; //MODOFICA
  Menu breakpoint = null; //MODIFICA: Inserisce un menu breakpoint a tendina
  Menu compiler =null;
  Menu about =null;
  GridBagLayout gridbag = new GridBagLayout();
  RunThread run_thread = null;
  About ab = null;
  
  
  private Button INSERT = null;
  private Button CLOSE_INS = null;
  private Button DELETE = null;
  private Button CLOSE_DEL = null;
  private Button reset_button = null;
  private Button run_button = null;
  private Button stop_button = null;
  private Button modify_button = null;
  private Button microStep_button = null;
  private Button macroStep_button = null;
  private Label mic1_status = null;
  private Label ijvm_status = null;
  private Label INS = null;
  private Label DEL = null;
  private TextField next_micro = null;
  private TextField TESTO_INS = null;
  private TextField TESTO_DEL = null;

  private int cycle_count;
  public static int SP = SP_ORIGINAL;
  public int allocation_level, LV_0=LV;

  public String flag; //This variable is used to don't show the therd column in table called "memory"
  
  //private boolean pippo = false;
  
  private boolean null_array[] = {false, false, false, false, false, false};
  private boolean true_array[] = {true};
  private boolean mp_loaded = false;

  // Components 
  private ControlStore control_store = null;
  private MainMemory main_memory = null;
  private ALU alu = null;
  private Shifter shifter = null;
  private Decoder decoder = null;
  private O o = null;
  private HighBit high_bit = null;

  // Registers
  public Register
    sp = null,
    lv = null,
    cpp = null,
    tos = null,
    opc = null,
    h = null;
  public MAR mar = null;
  public PC pc = null;
  public PC old_pc = null;
  public MDR mdr = null;
  public MBR mbr = null;
  public MPC mpc = null;
  private MIR mir = null;

  // Busses
  private Bus 
    byte_address_bus = null, 
    word_address_bus = null,
    byte_data_bus = null,
    word_data_bus = null,
    a_bus = null,
    b_bus = null,
    c_bus = null,
    alu_bus = null;
  private ObjectBus mir_bus = null;

  // Component control lines
  public IntControlLine
    control_store_cl = null,
    decoder_cl = null,
    o_addr_cl = null,  // Carries next microinstruction address to O
    o_mbr_cl = null,   // Carries MBR value to O
    mpc_cl = null;     // Carries O value to MPC
  private ControlLine 
    memory_cl = null,
    alu_cl = null,
    shifter_cl = null,
    o_jmpc_cl = null,    // Carries JMPC from MIR to O
    jam_cl = null,       // Carries JAMN/JAMZ from MIR to High bit
    high_bit_cl = null,  // Carries bit from High bit to MPC
    n_cl = null,         // Negative flag from ALU to High bit
    z_cl = null;         // Zero flag from ALU to High bit

  // Register control lines
  //   Each register has 2 control lines, one to indicate whether to store the value
  //   on the IN bus, the other to indicate whether to put its value onto the OUT
  //   bus.  Two control lines are used instead of one because the control signals
  //   come from two different sources--STORE from the MIR, and PUT from the decoder--
  //   and the two operations occur at different times in the clock cycle
  private ControlLine
    mar_store_cl = null,
    mar_put_cl = null,
    mdr_store_cl = null,
    mdr_put_cl = null,
    mdr_mem_cl = null,
    pc_store_cl = null,
    pc_put_cl = null,
    mbr_store_cl = null,
    mbr_put_cl = null,  // MBR stores only on a memory fetch, so it does not have a
    mbru_put_cl = null, // store control line.  It has 2 put control lines, signed and unsigned
    sp_store_cl = null,
    sp_put_cl = null,
    lv_store_cl = null,
    lv_put_cl = null,
    cpp_store_cl = null,
    cpp_put_cl = null,
    tos_store_cl = null,
    tos_put_cl = null,
    opc_store_cl = null,
    opc_put_cl = null,
    h_store_cl = null,
    h_put_cl = null;  // In the dual-bus data path, this control line will always be asserted
  private boolean editor;
  private IJVMEditor code_editor;


  //**************************************************************************
  /**   Constructor.  Creates all components, busses, and control lines
  */
  public mic1sim(boolean flag) {
    super("Mic-1 Simulator");
    init(flag);
  }

  public mic1sim(String filename, boolean flag) {
    super("Mic-1 Simulator");
    init(flag);
    loadMicroprogram(filename);
  }

  public mic1sim(String file1, String file2, boolean flag) {
    super("Mic-1 Simulator");
    init(flag);
    loadMicroprogram(file1);
    loadProgram(file2);
  }

  private void init(boolean flag) {
	  try {
		  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	  } catch (Exception ex) {
		  // Do nothing
	  }
    stdout.setFont(new Font("Courier",Font.PLAIN,12));
    menubar = new MenuBar();
    setMenuBar(menubar);
    file = new Menu("File");
    file.add(new MenuItem("Load Microprogram"));
    file.add(new MenuItem("Load Macroprogram"));
    file.add(new MenuItem("Exit"));
    menubar.add(file);

    window = new Menu("Windows");    // INIZIO MODIFICA
    window.add(new MenuItem("Control Store"));
    window.add(new MenuItem("Memory"));
    window.add(new MenuItem("Method Area"));
    window.add(new MenuItem("Debug"));
    window.add(new MenuItem("IJVM Editor"));

    menubar.add(window);		// FINE

    breakpoint = new Menu("Breakpoint");    // INIZIO MODIFICA: inserimento menu breakpoint
    breakpoint.add(new MenuItem("Insert Breakpoint"));
    breakpoint.add(new MenuItem("Delete Breakpoint"));
    breakpoint.add(new MenuItem("List of Breakpoint"));
    menubar.add(breakpoint);		// FINE

    compiler = new Menu("Compiler");  
    compiler.add(new MenuItem("Compile MAL code"));
    compiler.add(new MenuItem("Compile IJVM code"));
    menubar.add(compiler);	
    
    about = new Menu("About");
    about.add(new MenuItem("About"));
    menubar.add(about);	
    
    
    next_micro = new TextField();
    next_micro.setEditable(false);

    // First, create busses and control lines

    // Busses    
    byte_address_bus = new Bus();
    word_address_bus = new Bus();
    byte_data_bus = new Bus();
    word_data_bus = new Bus();
    a_bus = new Bus();
    b_bus = new Bus();
    c_bus = new Bus();
    alu_bus = new Bus();
    mir_bus = new ObjectBus();

    // Control lines
    control_store_cl = new IntControlLine();
    memory_cl = new ControlLine();
    alu_cl = new ControlLine();
    shifter_cl = new ControlLine();
    decoder_cl = new IntControlLine();
    o_mbr_cl = new IntControlLine();
    o_addr_cl = new IntControlLine();
    o_jmpc_cl = new ControlLine();
    mpc_cl = new IntControlLine();
    jam_cl = new ControlLine();
    high_bit_cl = new ControlLine();
    n_cl = new ControlLine();
    z_cl = new ControlLine();
    
    mar_store_cl = new ControlLine();
    mar_put_cl = new ControlLine();
    mdr_store_cl = new ControlLine();
    mdr_put_cl = new ControlLine();
    mdr_mem_cl = new ControlLine();
    pc_store_cl = new ControlLine();
    pc_put_cl = new ControlLine();
    mbr_store_cl = new ControlLine();
    mbr_put_cl = new ControlLine();
    mbru_put_cl = new ControlLine();
    sp_store_cl = new ControlLine();
    sp_put_cl = new ControlLine();
    lv_store_cl = new ControlLine();
    lv_put_cl = new ControlLine();
    cpp_store_cl = new ControlLine();
    cpp_put_cl = new ControlLine();
    tos_store_cl = new ControlLine();
    tos_put_cl = new ControlLine();
    opc_store_cl = new ControlLine();
    opc_put_cl = new ControlLine();
    h_store_cl = new ControlLine();
    h_put_cl = new ControlLine();
    h_put_cl.setValue(true_array);

    // Next, create components and registers

  // Registers
    mar = new MAR(c_bus, word_address_bus, mar_store_cl, memory_cl);
    sp = new Register(c_bus, b_bus, "SP", sp_store_cl, sp_put_cl);
    lv = new Register(c_bus, b_bus, "LV", lv_store_cl, lv_put_cl);
    cpp = new Register(c_bus, b_bus, "CPP", cpp_store_cl, cpp_put_cl);
  
    tos = new Register(c_bus, b_bus, "TOS", tos_store_cl, tos_put_cl);
    opc = new Register(c_bus, b_bus, "OPC", opc_store_cl, opc_put_cl);
    h = new Register(c_bus, a_bus, "H", h_store_cl, h_put_cl);
    pc = new PC(c_bus, b_bus, byte_address_bus, pc_store_cl, pc_put_cl, memory_cl);
    old_pc = new PC(c_bus, b_bus, byte_address_bus, pc_store_cl, pc_put_cl, memory_cl);
    mdr = new MDR(c_bus, b_bus, word_data_bus, mdr_store_cl, mdr_put_cl, mdr_mem_cl, memory_cl);
    mbr = new MBR(byte_data_bus, b_bus, mbr_store_cl, mbr_put_cl, mbru_put_cl, o_mbr_cl);
    mir = new MIR(o_addr_cl, o_jmpc_cl, jam_cl, shifter_cl, alu_cl, mar_store_cl, mdr_store_cl,
		  pc_store_cl, sp_store_cl, lv_store_cl, cpp_store_cl, tos_store_cl, opc_store_cl, 
		  h_store_cl, memory_cl, decoder_cl, mir_bus);

    // Components
    control_store = new ControlStore(control_store_cl, mir_bus);
    mpc = new MPC(mpc_cl, high_bit_cl, control_store_cl, control_store, next_micro);
    main_memory = new MainMemory(byte_address_bus, word_address_bus, 
				 byte_data_bus, word_data_bus, memory_cl, mbr_store_cl, mdr_mem_cl, mdr);
    alu = new ALU(a_bus, b_bus, alu_bus, alu_cl, n_cl, z_cl);
    shifter = new Shifter(alu_bus, c_bus, shifter_cl);
    decoder = new Decoder(decoder_cl, mdr_put_cl, pc_put_cl, mbr_put_cl, mbru_put_cl,
			  sp_put_cl, lv_put_cl, cpp_put_cl, tos_put_cl, opc_put_cl);
    o = new O(o_addr_cl, o_mbr_cl, o_jmpc_cl, mpc_cl);
    high_bit = new HighBit(n_cl, z_cl, jam_cl, high_bit_cl);
    
    for (int j = 1; j < 4; j++) for (int i = 0; i < MEM_MAX_S; i++) Pointers[j][i] = 1;
    for (int i = 0; i < MEM_MAX_S; i++) Pointers[0][i] = 0;
    
    Pointers[1][sp.value] = 0;
    Pointers[2][lv.value] = 0;
    Pointers[3][cpp.value] = 0;
    if (flag) Pointers[4][0] = -1;
       else Pointers[4][0] = 1;
    
    initializeFrame();

  }

  //*********************************************************************************************

  private void clearControlLines() {
    mbr_store_cl.setValue(null_array);
    mdr_mem_cl.setValue(null_array);
    memory_cl.setValue(null_array);
    alu_cl.setValue(null_array);
    shifter_cl.setValue(null_array);
    o_jmpc_cl.setValue(null_array);
    jam_cl.setValue(null_array);
    high_bit_cl.setValue(null_array);
    n_cl.setValue(null_array);
    z_cl.setValue(null_array);
    mar_store_cl.setValue(null_array);
    mar_put_cl.setValue(null_array); 
    mdr_store_cl.setValue(null_array);
    mdr_put_cl.setValue(null_array);
    pc_store_cl.setValue(null_array);
    pc_put_cl.setValue(null_array);
    mbr_put_cl.setValue(null_array); 
    mbru_put_cl.setValue(null_array);
    sp_store_cl.setValue(null_array);
    sp_put_cl.setValue(null_array);
    lv_store_cl.setValue(null_array);
    lv_put_cl.setValue(null_array); 
    cpp_store_cl.setValue(null_array);
    cpp_put_cl.setValue(null_array); 
    tos_store_cl.setValue(null_array);
    tos_put_cl.setValue(null_array);
    opc_store_cl.setValue(null_array);
    opc_put_cl.setValue(null_array);
    h_store_cl.setValue(null_array);



  }


  //************************************************************************************************


  public void run() {
    if (memory) mM_frame.removeSelectPc();
    if (control_memory) cs_frame.removeSelectMpc();
    run = true;
    halt = false;
    run_button.setEnabled(false);
    microStep_button.setEnabled(false);
    macroStep_button.setEnabled(false);
    reset_button.setEnabled(false);
    modify_button.setEnabled(false);
    stop_button.setEnabled(true);
    run_thread = new RunThread(this);
    run_thread.start();
  
  
  }

  public void stop() {
    run = false;
    if (halt)
      run_button.setEnabled(false);
    else
      run_button.setEnabled(true);
    modify_button.setEnabled(true);
    microStep_button.setEnabled(true);
    macroStep_button.setEnabled(true);
    reset_button.setEnabled(true);
    stop_button.setEnabled(false);
    refresh();
    if (memory) mM_frame.selectPc(pc.getValue());
    if (control_memory) cs_frame.selectMpc(mpc.getValue());
  }

  public void microStep() {
    modify_button.setEnabled(false);
    cycle();
    if (control_memory) cs_frame.selectMpc(mpc.getValue());
    modify_button.setEnabled(true);
  }

  public void macroStep() {
    modify_button.setEnabled(false);
    do{
      if (mpc.getValue() == 0xFF) break;
      cycle();
    }while (mpc.getValue()!= 0x2);
    
    if (memory) mM_frame.selectPc(pc.getValue());		
    modify_button.setEnabled(true);
  }


  public void reset() {
    //Breakpoint_Vector.removeAllElements();
    if (mp_loaded) {
      halt = false;
      if (debug)
        DebugFrame.text.setText("");
      stdout.setText("");
      cycle_count = 0;
      control_store_cl.setValue(0);
      mir.reset();
      clearControlLines();
      mpc.forceValue(0);
      pc.forceValue(-1);
      mbr.forceValue(0);
      mar.forceValue(0);
      mdr.forceValue(0);
      opc.forceValue(0);
      tos.forceValue(0);
      h.forceValue(0);
      sp.forceValue(SP);
      lv.forceValue(LV);
      cpp.forceValue(CPP);
      key_buffer.removeAllElements();
      run_button.setEnabled(true);
      //modify_button.setEnabled(false);
      microStep_button.setEnabled(true);
      macroStep_button.setEnabled(true);
      reset_button.setEnabled(true);
      stop_button.setEnabled(false);
      main_memory.reset();
	  
      control_store_cl.setValue(0);
      decoder_cl.setValue(0);
      o_mbr_cl.setValue(0); 
      o_addr_cl.setValue(0); 
      mpc_cl.setValue(0); 
  
      byte_address_bus.setValue(0);
      word_address_bus.setValue(0);
      byte_data_bus.setValue(0);
      word_data_bus.setValue(0); 
      a_bus.setValue(0);
      b_bus.setValue(0);
      c_bus.setValue(0);
      alu_bus.setValue(0);

      for (int j = 1; j < 4; j++) for (int i = 0; i < MEM_MAX_S; i++) Pointers[j][i] = 1;
      for (int i = 0; i < MEM_MAX_S; i++) Pointers[0][i] = 0;
    	   Pointers[1][sp.value] = 0;
    	   Pointers[2][lv.value] = 0;
    	   Pointers[3][cpp.value] = 0;
      allocation_level=0;
      LV_0=LV;
	

      if (memory) mM_frame.reset();
      if (control_memory) cs_frame.reset();
      //if (Breakpoint) Breakpoint_Vector.removeAllElements();
      if (stack){
//      	  	Stack_Frame.dispose();
//      	  	Stack_Frame = new StackFrame(main_memory, Pointers);
		Stack_Frame.updatePointer(Pointers);
		}      
    }
    else {
      run_button.setEnabled(false);
      microStep_button.setEnabled(false);
      macroStep_button.setEnabled(false);
      reset_button.setEnabled(false);
      stop_button.setEnabled(false);
   }
   

  }

  public void cycle() {

    int sp_value_old, lv_value_old,cpp_value_old;
    
    
    cycle_count++;
    sp_value_old = sp.value; 
    lv_value_old = lv.value;
    cpp_value_old = cpp.value; 

    if (debug)
      debug_frame.text.append("-----------------Start cycle " + cycle_count + "----------------------\n");
    clearControlLines();
    // Drive next microinstruction onto control lines
    control_store.poke();
    mir.poke();

    // Data path cycle
    decoder.poke();
    mdr.put();
    pc.put();
    mbr.put();
    sp.put();
    lv.put();
    cpp.put();
    tos.put();
    opc.put();
    h.put();
    alu.poke();
    shifter.poke();

    mar.store();
    mdr.store();
    pc.store();
    sp.store();
    lv.store();
    cpp.store();
    tos.store();
    opc.store();
    h.store();

    pc.mem();
    mar.mem();
    mdr.mem();
    main_memory.poke();
    
   
    if (Pointers[4][0] == 1)
    {
	    if ((lv.value<sp.value)&&(lv.value>lv_value_old)) allocation_level++;
   	 if (lv.value<lv_value_old) allocation_level--;
   	 if (lv.value==LV_0) allocation_level=0;
   	 if ((lv_value_old==LV_0) && (lv_value_old!=lv.value)) allocation_level=1;
	
	    Pointers[2][lv_value_old] = 1; //Gestione del puntatore a lv
	    Pointers[2][lv.value] = 0;
	
	    Pointers[1][sp_value_old] = 1; //Gestione del puntatore a SP
	    Pointers[1][sp.value] = 0; 
	    
	    Pointers[3][cpp_value_old] = 1; //Gestione del puntatore a cpp
	    Pointers[3][cpp.value] = 0;
	
	  
	    	Pointers[0][lv_value_old] = 0; //Gestione dei livelli di allocazione per LV
	    	Pointers[0][lv.value] = allocation_level;
	
	    	Pointers[0][sp_value_old] = 0; //Gestione dei livelli di allocazione per SP
	    	Pointers[0][sp.value] = allocation_level;
	    
	
	    if (lv.value<sp.value)
		for(int r=lv.value; r<=sp.value; r++){
	  	Pointers[0][r]=allocation_level; 
	    	}
	    if (lv.value==sp.value)
		for(int r=lv.value; r<=sp_value_old; r++){
	  	Pointers[0][r]=0; 
	    	}
    }

    if (stack) Stack_Frame.updatePointer(Pointers);
    if (update) mM_frame.update(address); 
    if (update_Stack) Stack_Frame.update(address_Stack);

    mbr.store();

    // Control path cycle
    high_bit.poke();
    o.poke();
    mpc.poke();
    if (!run)
      refresh();
    if (Breakpoint)
    {
    if (pc.getValue() != old_pc.getValue()) old_pc.setValue(-10);
    if (pc.getValue() != old_pc.getValue()){ 
//    String pc_string = new String();

       if (Breakpoint_Vector.contains(String.valueOf(pc.getValue())))


       {
          stop();
          //ErrorDialog err = new ErrorDialog("Program interrupted" , "Reached breakpoint: PC = " + Integer.toHexString(pc.getValue()));
	  old_pc.setValue(pc.getValue());
       }
    }

	}
    if (control_memory) cs_frame.selectMpc(mpc.getValue());
    if (memory) mM_frame.selectPc(pc.getValue());
    if(code_editor!=null) code_editor.debug(pc.getValue());
  }

  public static void halt() {
    run = false;
    halt = true;
    stdout.append("\nEnd of run.\n");
  }

  public void refresh() {
    mdr.refresh();
    pc.refresh();
    mbr.refresh();
    sp.refresh();
    lv.refresh();
    cpp.refresh();
    tos.refresh();
    opc.refresh();
    h.refresh();
    mar.refresh();
  }

  public void setControlStore(Mic1Instruction instructions[]) {
    control_store.setStore(instructions);
  }

  private void setMemory(byte[] bytes) {
    main_memory.setMemory(bytes);
  }

  private void programDialog() {
    FileDialog fd = new FileDialog(this, "Load Macroprogram", FileDialog.LOAD);
    fd.setFile("*.ijvm");
    fd.setVisible(true);
    // fd.paintAll(fd.getGraphics());
    if (fd.getFile() != null) {
      loadProgram(fd.getDirectory() + fd.getFile());
    }
  }

  public void loadProgram(String filename) {
    ErrorDialog err = null;
    String ext = "ijvm";
    try {
      IJVMLoader loader = new IJVMLoader(filename);
      if (loader.isValid() == null) {
        setMemory(loader.getProgram());
	  //if ( memory ) mM_frame.setTable(main_memory);
	  //if (stack == 1) Stack_Frame.updatePointer(Pointers); 
        reset();
      }
      else
        err = new ErrorDialog("Error loading program", loader.isValid());
	
	MNELoader mne_loader = new MNELoader(filename);
	if ( mne_loader.isValid() == null ) {
		mnemonic = mne_loader.getMnemonic();
		constant = mne_loader.getConstant();
		labels = mne_loader.getLabels();
		SP=SP_ORIGINAL+mne_loader.number_variables_main();
		Pointers[1][sp.value] = 1;
		sp.forceValue(SP);
		Pointers[1][sp.value] = 0;
	}
  	else
        err = new ErrorDialog("Error loading mnemonic informations", mne_loader.isValid());
    }
    catch (FileNotFoundException fnfe) {
      	err = new ErrorDialog("Error opening program", "File not found: " + filename);
    }
    catch (IOException ioe) {
    	err = new ErrorDialog("Error loading macroprogram","Exception ioe");
    }

    //if (Breakpoint) Breakpoint_Vector.removeAllElements(); //BREAKPOINT E' TRUE SOLO QUANDO CI SONO DEI BREAKPOINT CHE VANNO OVVIAMENTE ELIMINATI DAL VECTOR ALL'ATTO DEL CARICAMENTO DI UN NUOVO MACROPROGRAMMA

    if (memory){
      mM_frame.dispose();
      mM_frame = new MainMemoryFrame(main_memory,mnemonic,constant,labels,Breakpoint_Vector);
    }
    if (stack){
      Stack_Frame.dispose();
      Stack_Frame = new StackFrame(main_memory,Pointers);
      Stack_Frame.updatePointer(Pointers);
      Stack_Frame.reset();

    }
  }
   
  private void microprogramDialog() {
    FileDialog fd = new FileDialog(this, "Load Microprogram", FileDialog.LOAD);
    fd.setFile("*.mic1");
    fd.setVisible(true);
    // fd.paintAll(fd.getGraphics());
    if (fd.getFile() != null) {
      prefs.put("last_microprogram", fd.getDirectory() + fd.getFile());
      loadMicroprogram(fd.getDirectory() + fd.getFile());
    } 
  }
  
  public void loadMicroprogram(String filename) {
    ErrorDialog err = null;
    Mic1Instruction microprogram[] = new Mic1Instruction[612];
    try {
      InputStream in = new FileInputStream(filename);
      boolean eof = false;
      if (in.read() != mic1_magic1 ||
          in.read() != mic1_magic2 ||
          in.read() != mic1_magic3 ||
          in.read() != mic1_magic4) {
        err = new ErrorDialog("Error opening file", 
          "Error loading Microprogram: invalid file format: " + filename);
        mp_loaded = false;
      }
      else {
        int i = 0;
        while (!eof) {
	  microprogram[i] = new Mic1Instruction();
	  eof = (microprogram[i].read(in) == -1);
	  i++;
        }
        setControlStore(microprogram);
        mp_loaded = true;
      }
    }
    catch (FileNotFoundException fnfe) {
      err = new ErrorDialog("Error opening file", 
        "Error loading Microprogram: file not found: " + filename);
      mp_loaded = false;
    }
    catch (IOException ioe) { 
      err = new ErrorDialog("Error reading file",
        "Error loading Microprogram: error reading file: " + filename);
      mp_loaded = false;
    }
    reset();
	
    if (control_memory){
	cs_frame.dispose();
      cs_frame = new ControlStoreFrame(control_store);
    }

    //if (Breakpoint) Breakpoint_Vector.removeAllElements(); //BREAKPOINT E' TRUE SOLO QUANDO CI SONO DEI BREAKPOINT CHE VANNO OVVIAMENTE ELIMINATI DAL VECTOR ALL'ATTO DEL CARICAMENTO DI UN NUOVO MACROPROGRAMMA

    if (memory){
      mM_frame.dispose();
      mM_frame = new MainMemoryFrame(main_memory,mnemonic,constant,labels,Breakpoint_Vector);
    }
    if (stack){
      Stack_Frame.dispose();
      Stack_Frame = new StackFrame(main_memory,Pointers);
    }

  }



  public boolean handleEvent(Event event) {
    switch (event.id) {
    case Event.ACTION_EVENT :
      if (event.target instanceof MenuItem) {
	  if (((String)event.arg).equals("Load Microprogram"))
	    microprogramDialog();
	  if (((String)event.arg).equals("Load Macroprogram"))
	    programDialog();
	  if (((String)event.arg).equals("Compile MAL code")){
		  if(mic1Compiler == null || !mic1Compiler.isOn()){
			  mic1Compiler = new Mic1compiler();
		  }
	  }
	  if (((String)event.arg).equals("Compile IJVM code")){
		  if(ijvmCompiler == null || !ijvmCompiler.isOn()){
			  ijvmCompiler = new IJVMcompiler(); 
		  }
	  }
	  if (((String)event.arg).equals("Insert Breakpoint"))
	    {IB_frame = new InsertBreakpointFrame(Breakpoint_Vector, BK_List);
	     Breakpoint = true;
	  } 

	  if (((String)event.arg).equals("Delete Breakpoint")){
	    DB_frame = new DeleteBreakpointFrame(Breakpoint_Vector, BK_List);
	    if (Breakpoint_Vector.isEmpty()) Breakpoint = false;
	  }  

	  if (((String)event.arg).equals("List of Breakpoint")){
	    if (Breakpoint_Vector.isEmpty()) 
			{BK_List.List_of_BK.setText("There are no valid Breakpoints now !!!");
	    		BK_List.repaint();
			}
	    BK_List.setVisible(true);

	  }  

	  if (((String)event.arg).equals("Debug")) {
	    if(!debug){
	    	debug_frame = new DebugFrame();
	    	debug = true;
	    }
	  }

	  if (((String)event.arg).equals("Control Store")) {
	    if (!control_memory){
	    	cs_frame = new ControlStoreFrame(control_store);
	    	control_memory = true;
	    }
	  }
	  
	  if (((String)event.arg).equals("Memory")) {
	    if(!stack){
	    	Stack_Frame = new StackFrame(main_memory, Pointers);
	    	Stack_Frame.updatePointer(Pointers);
	    	stack = true; 
	     }
	  }
	  if (((String)event.arg).equals("About")) {
		  if(ab == null || !ab.isOn()){
		    About ab = new About();
		  }	 
	  }
      if (((String)event.arg).equals("Method Area")) {
	   if(!memory){	
	    	mM_frame = new MainMemoryFrame(main_memory,mnemonic,constant,labels,Breakpoint_Vector);
	    	mM_frame.update(address); 
	   	memory = true;
	    }
	  }										//FINE											  

        if (((String)event.arg).equals("IJVM EditorEditor")) {
          if(!editor){
            code_editor = new IJVMEditor(this::loadProgram);
            code_editor.setBreakPointVector(Breakpoint_Vector);
            editor = true;
          }
        }

        if (((String)event.arg).equals("Exit")) {
	    this.dispatchEvent(new WindowEvent(mic1sim.this, WindowEvent.WINDOW_CLOSED));
	    System.exit(0);
	  }
      }
      else if (event.target == run_button)
	  run();
      else if (event.target == stop_button)
	  stop();
      else if (event.target == microStep_button)
	  microStep();
      else if (event.target == macroStep_button)
	  macroStep();
      else if (event.target == reset_button){
	  reset();
    	  }

      else if (event.target == modify_button)
	  Modify = new ModifyRegisterFrame(s);
      break;
    case Event.KEY_ACTION:
    case Event.KEY_PRESS :
      key_buffer.addElement(new Character((char)event.key));
      break;
    case Event.WINDOW_DESTROY:
      System.exit(0);
      break;
    }
    return super.handleEvent(event);
  }


  private void initializeFrame() {
    setLayout(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.insets = new Insets(2,5,1,5);

    c.weightx = 1.0; c.weighty = 0.0;

    c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.gridheight = 1;
    c.gridwidth=2; constrain(new Label("Registers"),c);
    c.gridwidth=1;
    c.gridy=1; constrain(new Label("MAR"),c);
    c.gridy=2; constrain(new Label("MDR"),c);
    c.gridy=3; constrain(new Label("PC"),c);
    c.gridy=4; constrain(new Label("MBR"),c);
    c.gridy=5; constrain(new Label("SP"),c);
    c.gridy=6; constrain(new Label("LV"),c);
    c.gridy=7; constrain(new Label("CPP"),c);
    c.gridy=8; constrain(new Label("TOS"),c);
    c.gridy=9; constrain(new Label("OPC"),c);
    c.gridy=10; constrain(new Label("H"),c);
    c.gridx=1;
    c.gridy=1; constrain(mar,c);
    c.gridy=2; constrain(mdr,c);
    c.gridy=3; constrain(pc,c);
    c.gridy=4; constrain(mbr,c);
    c.gridy=5; constrain(sp,c);
    c.gridy=6; constrain(lv,c);
    c.gridy=7; constrain(cpp,c);
    c.gridy=8; constrain(tos,c);
    c.gridy=9; constrain(opc,c);
    c.gridy=10; constrain(h,c);

    c.gridx=2; c.gridy=0; c.gridwidth=2;
    constrain(new Label("Components"),c);
    c.gridwidth=1;
    c.gridy=1; constrain(new Label("Microinstruction"),c);
    c.gridy=2; constrain(new Label("NextMicroinstruction"),c);
    c.gridy=3; constrain(new Label("MPC"),c);
    c.gridy=4; constrain(new Label("ALU"),c);
    c.gridwidth=2;
    c.gridy=5; constrain(new Label("Standard out"),c);

    c.gridwidth=1; c.gridx=3;
    c.gridy=1; constrain(mir,c);
    c.gridy=2; constrain(next_micro,c);
    c.gridy=3; constrain(mpc,c);
    c.gridy=4; constrain(alu,c);
    c.gridwidth=2;
    c.gridx=2;
    c.gridheight=5;
    c.gridy=6; constrain(stdout,c);

    Panel button_panel = new Panel();
    button_panel.setLayout(new GridLayout(1,4,5,5));

    run_button = new Button("Run");
    run_button.setEnabled(false);
    modify_button = new Button("Modify register");
    stop_button = new Button("Stop");
    stop_button.setEnabled(false);
    microStep_button = new Button("Micro Step");
    microStep_button.setEnabled(false);
    macroStep_button = new Button("Macro Step");
    macroStep_button.setEnabled(false);
    reset_button = new Button("Reset");
    reset_button.setEnabled(false);

    button_panel.add(run_button);
    button_panel.add(stop_button);
    button_panel.add(microStep_button);
    button_panel.add(macroStep_button);
    button_panel.add(modify_button); 
    button_panel.add(reset_button);

    c.gridx = 0; c.gridy = 11; c.gridwidth = 5; c.gridheight = 1;
    c.weightx = 1.0;
    c.weighty = 0.0;
    constrain(button_panel, c);

    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });


    setVisible(true);
    setSize(getPreferredSize());
    paintAll(getGraphics());

    if(!stack){
      Stack_Frame = new StackFrame(main_memory, Pointers);
      Stack_Frame.updatePointer(Pointers);
      stack = true;
    }
    if(!memory){
      mM_frame = new MainMemoryFrame(main_memory,mnemonic,constant,labels,Breakpoint_Vector);
      mM_frame.update(address);
      memory = true;
    }
    if (!control_memory){
      cs_frame = new ControlStoreFrame(control_store);
      control_memory = true;
    }
    if(!editor){
      code_editor = new IJVMEditor(this::loadProgram);
      code_editor.setBreakPointVector(Breakpoint_Vector);
      editor = true;
    }

    String lastMicroprogram = prefs.get("last_microprogram", null);
    if(lastMicroprogram!=null)
      loadMicroprogram(lastMicroprogram);

  }

  private void constrain(Component component, GridBagConstraints constraints) {
    ((GridBagLayout)getLayout()).setConstraints(component, constraints);
    add(component);
  }

  private static void bad_option() {
    System.out.println("Usage: java mic1sim <file.mic1> <file.ijvm> <-n>");
    System.exit(0);
  }  

  public static void main(String args[]) {
    stdout.setEditable(false);
    if (args.length == 1)
      if (args[0].charAt(0)!='-')s = new mic1sim(args[0], false);
      	else if (args[0].length() == 1)bad_option(); else if(args[0].charAt(1)!='n')bad_option(); else s = new mic1sim(true);
    else if (args.length == 2)
      if (args[1].charAt(0)!='-'){s = new mic1sim(args[0], args[1], false);} 
      	else if (args[1].length() ==1)bad_option(); else if(args[1].charAt(1)!='n')bad_option(); else s = new mic1sim(args[0],true);
    else if (args.length == 3)
      if (args[2].charAt(0)!='-')bad_option(); 	
    	else if (args[2].length() ==1)bad_option(); else if(args[2].charAt(1)!='n')bad_option(); else s = new mic1sim(args[0],args[1],true);
     else s = new mic1sim(false);
  }


}

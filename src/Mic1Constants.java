/*
*
*  Mic1Constants.java
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


/**
* This is an interface that has any shared constants, such as 
* magic numbers for binary files, initial addresses of SP,CPP,LV, etc.
*
* @author 
*   Dan Stone (<a href="mailto:dans@ontko.com"><i>dans@ontko.com</i></a>),
*   Ray Ontko & Co,
*   Richmond, Indiana, US
*
*   Modification History
*
*	Name             		Date       	Comment
*	---------------- 		---------- 	----------------------------------------
*	Dan Stone        			 	Created
*	Claudio Bertoncello	1999.12 	added magic number for .mne files and the size of memory showed by MainMemoryFrame
*	Simone Alciati		2001.5		modified the original value of any variables (LV and SP)
*/

public interface Mic1Constants {

	/** Four byte "magic number" for .mic1 binary files */
	public static final int
			mic1_magic1 = 0x12, mic1_magic2 = 0x34,
			mic1_magic3 = 0x56, mic1_magic4 = 0x78;

	/** Four byte "magic number" for .ijvm binary files */
	public static final int
			magic1 = 0x1D, magic2 = 0xEA,
			magic3 = 0xDF, magic4 = 0xAD;

	/** Four byte "magic number" for .mne files */
	public static final int
			mne_magic1 = (byte) 0x2E, mne_magic2 = (byte) 0x4F,
			mne_magic3 = (byte) 0x6D, mne_magic4 = (byte) 0x7C;

	/** Word address of the constant pool pointer */
	public static final int CPP = 0x4000;

	/** Byte address of the constant pool pointer */
	public static final int CPP_B = 0x10000;

	/** Word address of the stack pointer */
	public static final int SP_ORIGINAL = 0x8000;

	/** Byte address of the stack pointer */
	public static final int SP_B = 0x20000;

	/** Word address of the local variable frame */
	public static final int LV = 0x8000; //It was 0xc000 now is equals to SP

	/** Word address of the local variable frame */
	public int LV_B = SP_B; //It was 0x30000

	/** Size in bytes of main memory */
	public static final int MEM_MAX = 0x40000;

	/** Size in bytes of Stack */
	public static final int MEM_MAX_S = 0x10000;

	/** Size in micro instructions of control store */
	public static final int INSTR_COUNT = 512;

	/* Size of main memory showed */
	public static final int MEM_SHOWED = 512;

}

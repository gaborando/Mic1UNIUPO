/**
* Simbol.java
*
* struct (string,number) for label & constant
*
* @author 
*   Claudio Bertoncello (<a href="mailto:cle@edu-al.unipmn.it"><i>cle@edu-al.unipmn.it</i></a>),
*   U.P.O.
*   Alessandria Italy
*/

public class Simbol{

	private String name = null;
	private int  address;

	public Simbol(String name,int address){
		this.name = name;
		this.address = address;
	}

	public String getName(){
		return name;
	}

	public int getAddress(){
		return address;
	}
}
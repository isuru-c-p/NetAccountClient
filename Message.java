import java.io.*;

class Message implements Serializable{
	private String s;


	public Message( String s ){
		this.s = s;
	}

	public String toString(){
		return s;
	}
}

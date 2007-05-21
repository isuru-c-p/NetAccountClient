import java.io.*;

class Message implements Serializable{
	private string s;


	public Message( String s ){
		this.s = s;
	}

	public String toString(){
		return s;
	}
}

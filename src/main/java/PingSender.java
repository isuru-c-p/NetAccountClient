import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import nz.ac.auckland.cs.des.*;

public class PingSender extends Thread {
	private DatagramSocket s = null;
	private int Auth_Ref = -1;
	private int outtoken = 0;
	private String Host;
	private InetAddress Host_IPAddr;
	private int Port;
	private Key_schedule schedule = null;  //set up encryption key to the users old passwd
	private int Sequence_Number = 0;
	private volatile boolean stop = false;
	private volatile int outstandingPings = 0;
	private NetLoginGUI netLogingui=null;
	private NetLoginCMD netLogincmd=null;

	public PingSender( String The_Host, int The_Port, NetLoginGUI netLogin ) throws IOException {
		try {
			s = new DatagramSocket(); 	// Allocate a datgram socket
		}
		catch ( Exception e ) {
			throw new IOException( "Error creating DatagramSocket: " + e );
		}
		Host = The_Host;
		Port = The_Port;
		Host_IPAddr = InetAddress.getByName( Host );
		this.netLogingui = netLogin;
		this.netLogincmd = null;
	}
	
	public PingSender( String The_Host, int The_Port, NetLoginCMD netLogin ) throws IOException {
		try {
			s = new DatagramSocket(); 	// Allocate a datgram socket
		}
		catch ( Exception e ) {
			throw new IOException( "Error creating DatagramSocket: " + e );
		}
		Host = The_Host;
		Port = The_Port;
		Host_IPAddr = InetAddress.getByName( Host );
		this.netLogincmd = netLogin;
		this.netLogingui = null;
	}

	public void prepare( Key_schedule schedule, int Auth_Ref,
                       int outtoken, int Sequence_Number ){
		this.schedule = schedule;
		this.Auth_Ref = Auth_Ref;
		this.outtoken = outtoken;
		this.Sequence_Number = Sequence_Number;
	}

	public DatagramSocket getSocket() {
		return s;
	}

	public void stopPinging() {
		DatagramPacket sendPacket = null;
		desDataOutputStream packit = new desDataOutputStream( 128 );
		desDataOutputStream des_out = new desDataOutputStream( 128 );
		byte EncryptedOutBuffer[];
		byte messageBytes[];

		try{
			// Tell gate were disconnecting
			des_out.writeInt( outtoken ); 
			des_out.writeInt( Sequence_Number + 10000 );

			EncryptedOutBuffer = des_out.des_encrypt( schedule );

			packit.writeInt( Auth_Ref );
			packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
			messageBytes = packit.toByteArray();
			sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr , Port );

			s.send( sendPacket );
		} catch( Exception e){ }

		stop = true;
		interrupt();
	}

	public int getOutstandingPings() {
		return outstandingPings;
	}

	public void zeroOutstandingPings() {
		outstandingPings = 0;
	}

	public void sendMessage( String user, String message ){
		DatagramPacket sendPacket = null;
		desDataOutputStream packit = new desDataOutputStream( 8192 );
		byte messageBytes[];
	

		try {
			packit.writeInt( -1 );					// -1 tells Pingd this is a message
			packit.writeBytes( "senduser " + user + " " + message + " " );
			messageBytes = packit.toByteArray();
			sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr, Port );
			s.send( sendPacket );
		} catch ( IOException e ) {
			System.out.println( "PingSender: Error sending message" );
		}
	}

	public synchronized void run( ) {
		DatagramPacket sendPacket = null;
		desDataOutputStream packit = new desDataOutputStream( 128 );
		desDataOutputStream des_out = new desDataOutputStream( 128 );
		byte EncryptedOutBuffer[];
		byte messageBytes[];
		int bad = 0;

		setPriority( Thread.MAX_PRIORITY / 4 );
		while ( !stop && outstandingPings < 5 && bad < 10 ) {
			try {
				des_out.writeInt( outtoken );   		//Can throw IOException
				des_out.writeInt( Sequence_Number );   //Can throw IOException

				EncryptedOutBuffer = des_out.des_encrypt( schedule );  	//encrypt buffer

				//These can throw IOException
				packit.writeInt( Auth_Ref );
				packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
				messageBytes = packit.toByteArray();
				sendPacket = new DatagramPacket( messageBytes, messageBytes.length, Host_IPAddr , Port );

				s.send( sendPacket );

				Sequence_Number++;
				outstandingPings++;
				bad = 0;
	
			} catch ( IOException e ) {
				System.out.println( "PingSender: Error sending ping packet" );
				bad++; 		// Ignore it at least 10 times in a row
			}

			des_out.reset();  //zero the buffers so we can reuse them.
			packit.reset();
			EncryptedOutBuffer = null;  //free the memory for these.
			messageBytes = null;
			sendPacket = null;

			// Sleep for 10 seconds
			try {
				sleep( 10000 );  //time to sleep in 1/1000 of a second
			} catch ( InterruptedException e ) {
				// stopPinging wants us to stop.
			}
			
		} //end of while
		
		try {
			s.close();
		} catch ( Exception e ) {
			System.err.println( "Error closing socket: " + e );
		}
		if (netLogingui!=null) netLogingui.update( 0, false, false );
		if (netLogincmd!=null) netLogincmd.update( 0, false, false );
	}
}

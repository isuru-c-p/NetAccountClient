import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import nz.ac.auckland.cs.des.*;

public class PingRespHandler extends Thread {
	private NetLogin netLogin;
	//Response ping packet Commands
	private final int ACK		= 1;	
	private final int NACK		= 0; 	//will cause a shutdown.
	private int inToken			= 0;
	private DatagramSocket s	= null;
	private Key_schedule schedule		= null; //set up encryption key to the users passwd
	private volatile boolean loop		= true;
    private int Next_Sequence_Number_Expected = 0;
	private PingSender pinger;

	public PingRespHandler( NetLogin netLogin, PingSender pinger ) throws IOException {
		this.netLogin = netLogin;
		this.pinger = pinger;
		s = new DatagramSocket(); //Dynamic allocation of the Port Number 
		s.setSoTimeout( 500 );	// set timeout to half a second
	}
	
	/*
	 * Use supplied socket instead of makeing one with a dynamically
	 * allocated port, when using this send a responce port of 0, and 
	 * Robs new ping daemon will just send ping responces to the same
	 * port it recieves them on.
	 */
	public PingRespHandler( NetLogin netLogin, PingSender pinger,
				DatagramSocket socket ) throws IOException {
		this.netLogin = netLogin;
		this.pinger = pinger;
		s = socket; //Use same socket we use for sending pings
		s.setSoTimeout( 500 );	// set timeout to half a second
	}

	public void prepare( int randomIn, int squenceNum, Key_schedule sched ){
		inToken = randomIn;
		Next_Sequence_Number_Expected = squenceNum;	
		schedule = sched;
	}

	public int getLocalPort(){
		return s.getLocalPort();
	}

	public void end(){
		loop = false;
		interrupt();
	}

	public synchronized void run( ){
		byte recvBytes[] = new byte[ 8192 ];
		DatagramPacket incomingPacket = new DatagramPacket( recvBytes, recvBytes.length );
		desDataInputStream des_in;
		byte message[];
		int random_returned;
		int Seq_number_returned;
		int packet_length;
		int IP_usage;
		int Command;
		int message_length;
		int bad = 0;
		int OnPlan = 0;
			
		setPriority( Thread.MAX_PRIORITY / 4 );
		while( loop && bad < 10 && pinger.getOutstandingPings() < 5 ){
			incomingPacket = new DatagramPacket( recvBytes, recvBytes.length );

			try{
				s.receive( incomingPacket );
			} catch( InterruptedIOException e ){
				// end() interrupted is and wants us to stop or socket timeout
				continue;
			} catch( IOException e ) {
				System.out.println( "Error receiving: " + e );
				bad++;
				continue;
			}
			
			packet_length = incomingPacket.getLength();
			if( packet_length < 4 * 6) {
				System.out.println( "Short packet"  );
				bad++;
				continue;
			}
			
			des_in = new desDataInputStream( incomingPacket.getData() , 0, packet_length, schedule);

		    try {
				random_returned = des_in.readInt();
				if( inToken != random_returned ) {	// Other end doesn't agree on the current passwd
					System.out.println( "Other end doesn't agree on the current passwd" );
					bad++;
					continue; //packet could have been mashed.
				}
				
				Seq_number_returned = des_in.readInt();
				
				if( Next_Sequence_Number_Expected > Seq_number_returned ) {	// Probably a delayed packet
					System.out.println( "Ping responce sequence numbers out" );
					continue;
				}
				
				Next_Sequence_Number_Expected  = Seq_number_returned + 1;  //Catch up.
				
				
				
				/* from client version 3, These will be replaced by display user Internet Plan and monthly usage*/
				//IP_balance = des_in.readInt();
				IP_usage = des_in.readInt();
				
				Command	 = des_in.readInt();
				
				//OnPeak = des_in.readInt();
				OnPlan=des_in.readInt();
				
				message_length = des_in.readInt();
				message = new byte[ message_length ];
				des_in.read( message );
				
				//netLogin.update( IP_balance, ( OnPeak & 0x01 ) == 0x01, true, new String( message ) );
				netLogin.updateV3(IP_usage,OnPlan,true,new String(message));
				
				bad = 0; //start error trapping again.
				pinger.zeroOutstandingPings();

				if( Command == NACK ) {
					end(); //kill own thread
				}
			} catch( IOException e ) {
				System.out.println( "ping recv: IOException" );
				bad++;
		    }
		}
		if( pinger.getOutstandingPings() < 5 ){
			System.err.println( "Max outstanding pings reached, disconnecting" );
		}
		netLogin.update( 0, false, false );
	}
}

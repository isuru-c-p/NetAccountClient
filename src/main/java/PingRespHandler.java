import java.io.*;
import java.net.*;
import nz.ac.auckland.cs.des.*;

public class PingRespHandler extends Thread {
	
	private PingListener netLogin;

	// Response ping packet Commands
	private final int ACK = 1;
	private final int NACK = 0; // will cause a shutdown.
	private int inToken = 0;
	private DatagramSocket socket = null;
	private Key_schedule schedule = null; // set up encryption key to the users passwd
	private volatile boolean loop = true;
    private int nextSequenceNumberExpected = 0;
	private PingSender pinger;

	public PingRespHandler(PingListener netLogin, PingSender pinger) throws IOException {
		this.netLogin = netLogin;
		this.pinger = pinger;
		socket = new DatagramSocket(); //Dynamic allocation of the Port Number
		socket.setSoTimeout( 500 ); // set timeout to half a second
	}

	/*
	 * Use supplied socket instead of making one with a dynamically
	 * allocated port, when using this send a response port of 0, and
	 * Robs new ping daemon will just send ping responses to the same
	 * port it receives them on.
	 */
	public PingRespHandler(PingListener netLogin, PingSender pinger, DatagramSocket socket) throws IOException {
		this.netLogin = netLogin;
		this.pinger = pinger;
		this.socket = socket; // Use same socket we use for sending pings
		this.socket.setSoTimeout(500); // set timeout to half a second
	}

	public void prepare(int randomIn, int sequenceNum, Key_schedule sched){
		inToken = randomIn;
		nextSequenceNumberExpected = sequenceNum;
		schedule = sched;
	}

	public int getLocalPort(){
		return socket.getLocalPort();
	}

	public void end(){
		loop = false;
		interrupt();
	}

	public synchronized void run(){
		byte recvBytes[] = new byte[ 8192 ];
		DatagramPacket incomingPacket;
		desDataInputStream des_in;
		byte message[];
		int random_returned;
		int Seq_number_returned;
		int packet_length;
		int ipUsage;
		int command;
		int message_length;
		int bad = 0;
		int onPlan;
			
		setPriority( Thread.MAX_PRIORITY / 4 );
		while( loop && bad < 10 && pinger.getOutstandingPings() < 5 ){
			
			incomingPacket = new DatagramPacket( recvBytes, recvBytes.length );
			try{
				socket.receive( incomingPacket );
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
				
				if( nextSequenceNumberExpected > Seq_number_returned ) {	// Probably a delayed packet
					System.out.println( "Ping responce sequence numbers out" );
					continue;
				}
				
				nextSequenceNumberExpected = Seq_number_returned + 1; //Catch up.
				
				ipUsage = des_in.readInt();
				command = des_in.readInt();
				onPlan = des_in.readInt();
				
				message_length = des_in.readInt();
				message = new byte[message_length];
				des_in.read(message);

				netLogin.update(ipUsage, onPlan, new String(message));

				bad = 0; //start error trapping again.
				pinger.zeroOutstandingPings();
				
				if( command == NACK ) {
					end(); //kill own thread
				}
			} catch( Exception e ) {
				System.out.println( "ping recv: Exception:"+e );
				bad++;
		    }
		}
		if( pinger.getOutstandingPings() < 5 ){
			System.err.println( "Max outstanding pings reached, disconnecting" );
		}
		
		netLogin.disconnected();
	}
}

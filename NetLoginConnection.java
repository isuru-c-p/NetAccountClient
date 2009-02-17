import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import nz.ac.auckland.cs.des.*;

public class NetLoginConnection {
	
	//final String AUTHD_SERVER	= "jason2.netac.auckland.ac.nz";
	final String AUTHD_SERVER	= "gate-dev.ec.auckland.ac.nz";
	
	final int AUTHD_PORT		= 312	; 	// The port that we are awaiting authd from.
	final int PINGD_PORT		= 443	; 	// The port that we are awaiting pings from.

	//Packet Types
	final int AUTH_REQ_PACKET				= 1;
	final int AUTH_REQ_RESPONSE_PACKET		= 2;
	final int AUTH_CONFIRM_PACKET			= 3;
	final int AUTH_CONFIRM_RESPONSE_PACKET	= 4;
	final int NETGUARDIAN_JAVA_CLIENT		= 12;
	final int NETGUARDIAN_JAVA_MCLIENT		= 34;

	//client commands
	final int CMD_NULL							= 0; 	//do nothing
	final int CMD_LAST_CMD						= 1; 	//breaks cmd loop
	final int CMD_REGISTER						= 2; 	//register a client
	final int CMD_GET_USER_BALANCES_NO_BLOCK	= 3;
	final int CMD_GET_USER_BALANCES_DO_BLOCK	= 4;
	final int CMD_BAN_USER						= 5;

	//Response ping packet Commands
	final int ACK		= 1;
	final int NACK		= 0;  	//will cause a shutdown.
	final int VERSION	= 0;

	//From bookdefines.h
	final int UNAMESIZ	= 9;
	final int PASSWDSIZ	= 17;

	final int BUFSIZ	= 1024;
	byte InBuffer[]		= new byte[ BUFSIZ ];

	Random r = new Random();
	int random1 = 0;
	int random2 = 0;
	int Sequence_Number = 0;

	Key_schedule schedule = null;				//set up encryption key to the users passwd
	int	clienttype = NETGUARDIAN_JAVA_CLIENT; 	//We are a multiuser client today
	int cmd_data_length	= 2; 					//Ping Ports size (sizeof( short ))
	short	cmd_data = 0; 						//Port we want pings responses on
	
	/* client version will control ping response messages:
	 * when client version <3, quota-based Internet usage without user Internet plan
	 * when client version >=3, usage-based Internet usage with displaying user plan
	 */
	int	clientversion = 3;	         
	
	int	Client_Rel_Version;
	int	clientcommand = CMD_GET_USER_BALANCES_NO_BLOCK;

	SPP_Packet NetGuardian_stream = null;

	int Auth_Ref; 	//Reference to quote when we ping
	int IPBalance; 	//Our IP Balance
	int Response_Port;  //Our Port. the Server sends Ping responses to it

	PingSender pinger = null;
	PingRespHandler ping_receiver = null;
	boolean useStaticPingPort = false;
	String username;

	int OnPeak; 					//True of False. Used in ping packets & Statusd. in net byte order
	int localUnitCost; 				//c/MBytes of data for NZ traffic
	int intlOffPeakRate; 			//c/MBytes of data for international traffic
	int intlOnPeakRate; 			//c/MBytes of data for international traffic
	int start_Peak; 				//TIME OF DAY IN MINUTES
	int endPeak; 					//TIME OF DAY IN MINUTES
	int lastModDate; 				//DATESTAMP FROM THE CHARGES FILE
	
	NetLoginGUI parentgui=null;
	NetLoginCMD parentcmd=null;

	public NetLoginConnection(NetLoginCMD parentcmd)
	{
		this.parentcmd=parentcmd;
	}
	public NetLoginConnection( NetLoginGUI parentgui ){
		this.parentgui = parentgui;
	}

	public void logincmdline( String username, String password ) throws IOException{
		logincmdline( AUTHD_SERVER, username, password );
	}	

	public void logincmdline( String server, String username, String password ) throws IOException{
		
		pinger = new PingSender( server, PINGD_PORT, parentcmd );
		if( useStaticPingPort ){
			ping_receiver = new PingRespHandler( parentcmd, pinger, pinger.getSocket() );
			Response_Port = 0;
		} else {
			ping_receiver = new PingRespHandler( parentcmd, pinger );
			Response_Port = ping_receiver.getLocalPort();
		}
		
		authenticate( server, username, password );
		pinger.prepare( schedule, Auth_Ref, random2 + 2, Sequence_Number );
		ping_receiver.prepare( random1 + 3, Sequence_Number, schedule );
		ping_receiver.start();
		pinger.start();
	}
	
	public void login( String username, String password ) throws IOException{
		login( AUTHD_SERVER, username, password );
	}	

	public void login( String server, String username, String password ) throws IOException{
	
		this.username = username;
		pinger = new PingSender( server, PINGD_PORT, parentgui );
		if( useStaticPingPort ){
			ping_receiver = new PingRespHandler( parentgui, pinger, pinger.getSocket() );
			Response_Port = 0;
		} else {
			ping_receiver = new PingRespHandler( parentgui, pinger );
			Response_Port = ping_receiver.getLocalPort();
		}
		authenticate( server, username, password );
		pinger.prepare( schedule, Auth_Ref, random2 + 2, Sequence_Number );
		ping_receiver.prepare( random1 + 3, Sequence_Number, schedule );
		ping_receiver.start();
		pinger.start();
		parentgui.update( IPBalance, ( OnPeak & 0x01 ) == 0x01, true  );
	}

	public void setUseStaticPingPort( boolean b ){
		useStaticPingPort = b;
	}

	public void logout(){
		if( pinger != null )
			pinger.stopPinging();
		if( ping_receiver != null )
			ping_receiver.end();
	}

	public void sendMessage( String user, String message ){
		if( pinger != null )
			pinger.sendMessage( user, username +": "+ message );
	}

	private void authenticate( String server, String loginS, String PasswdS ) throws IOException, UnknownHostException {
		try {
			NetGuardian_stream = new SPP_Packet( server, AUTHD_PORT );
			schedule = new Key_schedule( PasswdS );
			sendPacket_1( loginS );
			RespPacket_1();

			SendSecondPacket();
			ReadSecondResponsePacket();
		} catch ( UnknownHostException e ) {
			throw e;
		} catch ( IOException e ) {
			throw e;
		} finally {
			NetGuardian_stream.close();
			NetGuardian_stream = null;
		}
	}

	private void sendPacket_1( String loginS ) throws IOException {
		desDataOutputStream packit = new desDataOutputStream( 128 );
		desDataOutputStream des_out = new desDataOutputStream( 128 );
		byte EncryptedOutBuffer[];

		random1 = r.nextInt();			// get a random number for using as a token
		des_out.writeInt( random1 );
		EncryptedOutBuffer = des_out.des_encrypt( schedule );  	//encrypt buffer

		//These can throw IOException
		packit.writeInt( clienttype );
		packit.writeInt( clientversion );
		packit.writeBytes( loginS, UNAMESIZ ); 	//truncates or pads so always UNAMESIZ
		packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
		NetGuardian_stream.SendPacket( AUTH_REQ_PACKET, VERSION, packit.toByteArray() );
	}

	private void RespPacket_1() throws IOException {
		desDataInputStream des_in;
		DataInputStream	unencrypted_data_input_Stream = null;
		int random_returned;
		ReadResult PacketHeader;
		int client_URL_Length;
		byte URL[];
		String error_msg;

		PacketHeader = NetGuardian_stream.ReadPacket( AUTH_REQ_RESPONSE_PACKET, VERSION, InBuffer );
		switch ( PacketHeader.Result ) {
			case SPP_Packet.RSLT_BAD_PACKET_TYPE:
				throw new IOException( "Unexpected PacketType " + PacketHeader.Packet_type +
						" expected " + AUTH_REQ_RESPONSE_PACKET );
			case SPP_Packet.REMOTE_RSLT_BAD_VERSION:
				error_msg = "Client Version incorrect";
				unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream(
							InBuffer, 0, NetGuardian_stream.Last_Read_length ) );
				try {
					Client_Rel_Version = unencrypted_data_input_Stream.readInt();
					if ( Client_Rel_Version > 0 ) {
						error_msg += "\rThe latest version is " + Client_Rel_Version;
						try {
							client_URL_Length = unencrypted_data_input_Stream.readInt();
							if ( client_URL_Length != 0 ) {
								URL = new byte[ client_URL_Length ];
								unencrypted_data_input_Stream.read( URL );
								error_msg += "\rURL " + URL;
							}
						} catch ( Exception e ) {} //ignore
					}

				} catch ( Exception e ) {} //ignore

				throw new IOException( error_msg );
			case SPP_Packet.REMOTE_RSLT_ACCESS_RESTRICTION:
				throw new IOException( "Access Denied from this Network or Host" );
			case SPP_Packet.REMOTE_RSLT_ACCESS_RESTRICTION_BAN:
				throw new IOException( "User Banned for using this host/network" );
			case SPP_Packet.RSLT_OK:
				break;
			default:
				throw new IOException( "Protocol Error " + PacketHeader.Result );
		}

		PacketHeader = null;

		if( NetGuardian_stream.Last_Read_length < /*C_Block.size() + 4 * 4*/ 20 ) 
			throw new IOException("RespPacket_1: AUTH_REQ_RESPONSE_PACKET too short, " +
					NetGuardian_stream.Last_Read_length + " bytes " + (C_Block.size() * 4) );

		unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream( InBuffer, 0, 4 ) );
		Client_Rel_Version = unencrypted_data_input_Stream.readInt();

		des_in = new desDataInputStream( InBuffer, 4, NetGuardian_stream.Last_Read_length, schedule );

		random_returned = des_in.readInt();
		if ( random1 + 1 != random_returned ) {	// Other end doesn't agree on the current passwd
			throw new IOException( "Incorrect password" );
			//throw new IOException( "RespPacket_1: Random Keys don't match " +
			//		uHex.toHex( random1 + 1 ) + " != " + uHex.toHex( random_returned ) );
		}

		random2 = des_in.readInt();   //We need to send this one back in the next packet
		schedule = new Key_schedule( des_in.readC_Block() );  //Get the new C_Block key and make a schedule
	}

	private void SendSecondPacket() throws IOException {
		desDataOutputStream des_out = new desDataOutputStream( 128 );
		byte EncryptedOutBuffer[];

		cmd_data = ( short ) Response_Port; 	//Port we want pings responses on
		des_out.writeInt( random2 + 1 );   		//Can throw IOException
		des_out.writeInt( clientcommand );   	//Can throw IOException
		des_out.writeInt( cmd_data_length ); 	//Can throw IOException
		des_out.writeShort( cmd_data );			//Can throw IOException

		EncryptedOutBuffer = des_out.des_encrypt( schedule );  	//encrypt buffer
		NetGuardian_stream.SendPacket( AUTH_CONFIRM_PACKET, VERSION, EncryptedOutBuffer );
	}

	private void ReadSecondResponsePacket() throws IOException {
		desDataInputStream des_in;
		int random_returned;
		int ack;
		int cmd_result_data_length;
		DataInputStream	unencrypted_data_input_Stream = null;
		ReadResult PacketHeader = null;

		// Process final ack packet to ensure all went well
		PacketHeader = NetGuardian_stream.ReadPacket( AUTH_CONFIRM_RESPONSE_PACKET, VERSION, InBuffer );
		switch ( PacketHeader.Result ) {
			case SPP_Packet.RSLT_BAD_PACKET_TYPE:
				throw new IOException( "Unexpected PacketType " + PacketHeader.Packet_type +
						" expected " + AUTH_REQ_RESPONSE_PACKET );
			case SPP_Packet.RSLT_OK:
				break;
			default:
				throw new IOException( "Protocol Error " + PacketHeader.Result );
		}

		if ( NetGuardian_stream.Last_Read_length < ( 10 * 4 ) )
			throw new IOException( "ReadSecondResponsePacket: AUTH_CONFIRM_RESPONSE_PACKET too short" );
		//too short for random2, ack and string

		unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream( InBuffer, 0, 28 ) );
		OnPeak = unencrypted_data_input_Stream.readInt();
		localUnitCost = unencrypted_data_input_Stream.readInt();
		intlOffPeakRate = unencrypted_data_input_Stream.readInt();
		intlOnPeakRate = unencrypted_data_input_Stream.readInt();
		start_Peak = unencrypted_data_input_Stream.readInt();
		endPeak = unencrypted_data_input_Stream.readInt();
		lastModDate = unencrypted_data_input_Stream.readInt();


		des_in = new desDataInputStream( InBuffer , 28, NetGuardian_stream.Last_Read_length, schedule );

		random_returned = des_in.readInt();
		if ( random1 + 2 != random_returned ) {	// Other end doesn't agree on the current passwd
			throw new IOException( "Incorrect password" );
		}

		ack = des_in.readInt();
		cmd_result_data_length = des_in.readInt();

		if( ack != Errors.CMD_RSLT_OK && ack != Errors.CMD_RSLT_WOULD_BLOCK )  //Error
			throw new IOException( "got Nack on authentication " + Errors.error_messages[ ack ] );

		if( ack == Errors.CMD_RSLT_WOULD_BLOCK )
			throw new IOException( "User record is locked. Unable to read IP balance" );

		if( cmd_result_data_length < 2 * 4 )
			throw new IOException( "Cmd result buffer too small" );
		else {
			Auth_Ref = des_in.readInt();
			IPBalance = des_in.readInt();
		}
	}
}

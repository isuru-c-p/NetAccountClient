package nz.ac.auckland.netlogin;

import nz.ac.auckland.cs.des.C_Block;
import nz.ac.auckland.cs.des.Key_schedule;
import nz.ac.auckland.cs.des.desDataInputStream;
import nz.ac.auckland.cs.des.desDataOutputStream;
import nz.ac.auckland.netlogin.*;
import nz.ac.auckland.netlogin.negotiation.Authenticator;
import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import nz.ac.auckland.netlogin.negotiation.password.PasswordAuthenticator;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class NetLoginConnection {
	
	public static final String AUTHD_SERVER = "gate.ec.auckland.ac.nz";
	
	final int AUTHD_PORT = 312; // The port that we are awaiting authd from.
	final int PINGD_PORT = 443; // The port that we are awaiting pings from.

	//Packet Types
	final int AUTH_REQ_PACKET				= 1;
	final int AUTH_REQ_RESPONSE_PACKET		= 2;
	final int AUTH_CONFIRM_PACKET			= 3;
	final int AUTH_CONFIRM_RESPONSE_PACKET	= 4;
	final int NETGUARDIAN_JAVA_CLIENT		= 12;
	final int NETGUARDIAN_JAVA_MCLIENT		= 34;

	//client commands
	final int CMD_NULL							= 0; //do nothing
	final int CMD_LAST_CMD						= 1; //breaks cmd loop
	final int CMD_REGISTER						= 2; //register a client
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

	int clientNonce;
	int serverNonce;
	int Sequence_Number = 0;

	Key_schedule schedule = null;				//set up encryption key to the users passwd
	int	clienttype = NETGUARDIAN_JAVA_CLIENT; 	//We are a multiuser client today
	int cmd_data_length	= 2; 					//Ping Ports size (sizeof( short ))
	short cmd_data = 0; 						//Port we want pings responses on
	
	/* client version will control ping response messages:
	 * when client version <3, quota-based Internet usage without user Internet plan
	 * when client version >=3, usage-based Internet usage with displaying user plan
	 */
	int	clientversion = 3;	         
	
	int	Client_Rel_Version;
	int	clientcommand = CMD_GET_USER_BALANCES_NO_BLOCK;

	SPP_Packet NetGuardian_stream = null;

	int Auth_Ref; 	//Reference to quote when we ping
	int IPUsage; 	//Our IP Balance
	int Response_Port;  //Our Port. the Server sends Ping responses to it

	PingSender pinger = null;
	PingRespHandler ping_receiver = null;
	boolean useStaticPingPort = false;
	String username;

	int onPlan; 					//True of False. Used in ping packets & Statusd. in net byte order
	int localUnitCost; 				//c/MBytes of data for NZ traffic
	int intlOffPeakRate; 			//c/MBytes of data for international traffic
	int intlOnPeakRate; 			//c/MBytes of data for international traffic
	int start_Peak; 				//TIME OF DAY IN MINUTES
	int endPeak; 					//TIME OF DAY IN MINUTES
	int lastModDate; 				//DATESTAMP FROM THE CHARGES FILE

	private Authenticator authenticator = new PasswordAuthenticator();

	private PingListener netLogin;

	public NetLoginConnection( PingListener netLogin ){
		this.netLogin = netLogin;
	}

	public void login(String server, CredentialsCallback callback) throws IOException {
		pinger = new PingSender( server, PINGD_PORT, netLogin );
		if( useStaticPingPort ){
			ping_receiver = new PingRespHandler( netLogin, pinger, pinger.getSocket() );
			Response_Port = 0;
		} else {
			ping_receiver = new PingRespHandler( netLogin, pinger );
			Response_Port = ping_receiver.getLocalPort();
		}

		authenticate( server, callback );
		
		pinger.prepare( schedule, Auth_Ref, serverNonce + 2, Sequence_Number );
		ping_receiver.prepare( clientNonce + 3, Sequence_Number, schedule );
		ping_receiver.start();
		pinger.start();
		
		netLogin.connected(IPUsage, onPlan);
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

	private void authenticate(String server, CredentialsCallback callback) throws IOException {
		try {
			NetGuardian_stream = new SPP_Packet( server, AUTHD_PORT );

			sendPacket_1(callback);
			RespPacket_1();

			SendSecondPacket();
			ReadSecondResponsePacket();

		} catch (IOException e) {
			throw e;
		} catch (LoginException e) {
			throw new IOException(e);
		} finally {
			NetGuardian_stream.close();
			NetGuardian_stream = null;
		}
	}

	private void sendPacket_1(CredentialsCallback callback) throws IOException, LoginException {
		Authenticator.AuthenticationRequest request = authenticator.startAuthentication(callback);
		this.username = request.getUsername();

		desDataOutputStream packet = new desDataOutputStream(128);
		packet.writeInt(clienttype);
		packet.writeInt(clientversion);
		packet.writeBytes(request.getUsername(), UNAMESIZ); // truncates or pads so always UNAMESIZ
		packet.write(request.getPayload());

		NetGuardian_stream.SendPacket(AUTH_REQ_PACKET, VERSION, packet.toByteArray());
	}

	private void RespPacket_1() throws IOException, LoginException {
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

		byte[] payload = new byte[NetGuardian_stream.Last_Read_length - 4];
		System.arraycopy(InBuffer, 4, payload, 0, payload.length);

		Authenticator.LoginComplete session = authenticator.validateResponse(payload);

		clientNonce = session.getClientNonce();
		serverNonce = session.getServerNonce();
		schedule = new Key_schedule(session.getSessionKey());
	}

	private void SendSecondPacket() throws IOException {
		desDataOutputStream des_out = new desDataOutputStream( 128 );
		byte EncryptedOutBuffer[];

		cmd_data = ( short ) Response_Port; 	//Port we want pings responses on
		des_out.writeInt( serverNonce + 1 );   		//Can throw IOException
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
		//too short for serverNonce, ack and string

		unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream( InBuffer, 0, 28 ) );
		onPlan = unencrypted_data_input_Stream.readInt();
		localUnitCost = unencrypted_data_input_Stream.readInt();
		intlOffPeakRate = unencrypted_data_input_Stream.readInt();
		intlOnPeakRate = unencrypted_data_input_Stream.readInt();
		start_Peak = unencrypted_data_input_Stream.readInt();
		endPeak = unencrypted_data_input_Stream.readInt();
		lastModDate = unencrypted_data_input_Stream.readInt();


		des_in = new desDataInputStream( InBuffer , 28, NetGuardian_stream.Last_Read_length, schedule );

		random_returned = des_in.readInt();
		if ( clientNonce + 2 != random_returned ) {	// Other end doesn't agree on the current passwd
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
			IPUsage = des_in.readInt();
		}
	}
}

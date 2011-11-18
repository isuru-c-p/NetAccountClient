import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import nz.ac.auckland.cs.des.*;

public class PasswordChanger {
    
	// passwdd packet types
	final int PWD_VERSION		= 1;
	final int PWD_CHANGE_PKT	= 1;
	final int PWD_CHK_PKT		= 2;
	final int PWD_CHK_VERIFY_PKT= 3;
	final int PWD_ACK_PKT		= 4;
	final int PWD_USR_CHG_PKT	= 5;
	final int PWD_ERR_SILLYPW	= 4001;		// this is a passwdlib error

	final int BUFSIZ		= 1024;
	final int UNAMESIZ		= 9;
	final int PASSWORDSIZE	= 17;
	public String errorString = "";

	public boolean changePassword( String username, String oldPassword, String newPassword ){
		try{
			Random rnd = new Random();
			int rnd1 = rnd.nextInt();
			int rnd2;
			int rnd1Returned;
			int ack;
			byte pwdBuffer[] = new byte[ BUFSIZ ];
			byte EncryptedOutBuffer[];
			SPP_Packet pwStream	= new SPP_Packet( "data.ec.auckland.ac.nz", 302 );
			Key_schedule schedule = new Key_schedule( oldPassword );
			desDataOutputStream packit = new desDataOutputStream( 128 );
			desDataOutputStream des_out = new desDataOutputStream( 128 );
			desDataInputStream des_in;
			DataInputStream	unencrypted_data_input_Stream = null;
			ReadResult packetHeader;
	
			// send PWD_CHANGE_PKT
			des_out.writeInt( rnd1 );
			EncryptedOutBuffer = des_out.des_encrypt( schedule );
			packit.writeBytes( username, UNAMESIZ ); 	//truncates or pads so always UNAMESIZ
			packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
			pwStream.SendPacket( PWD_CHANGE_PKT, PWD_VERSION, packit.toByteArray() );

			// read PWD_CHK_PKT
			packetHeader = pwStream.ReadPacket( PWD_CHK_PKT, PWD_VERSION, pwdBuffer );
			if( pwStream.Last_Read_length < 16 ){
				errorString = "Communication error reading PWD_CHK_PKT";
				return false;
			}
			des_in = new desDataInputStream( pwdBuffer, schedule );
	
			rnd1Returned = des_in.readInt();
			if( rnd1 + 1 != rnd1Returned ) {	// Other end doesn't agree on the current passwd
				errorString = "Your password is incorrect";
				return false;
			}
			rnd2 = des_in.readInt();
			schedule = new Key_schedule( des_in.readC_Block() ); // new schedule

			// send PWD_CHK_VERIFY_PKT
			des_out = new desDataOutputStream( 128 );
			packit = new desDataOutputStream( 128 );
			des_out.writeInt( rnd2 + 1 );
			des_out.writeBytes( newPassword, PASSWORDSIZE );
			EncryptedOutBuffer = des_out.des_encrypt( schedule );
			packit.write( EncryptedOutBuffer, 0, EncryptedOutBuffer.length );
			pwStream.SendPacket( PWD_CHK_VERIFY_PKT, PWD_VERSION, packit.toByteArray() );

			// read PWD_ACK_PKT
			packetHeader = pwStream.ReadPacket( PWD_ACK_PKT, PWD_VERSION, pwdBuffer );
			if( pwStream.Last_Read_length < 8 ){
				errorString = "Communication error reading PWD_ACK_PKT";
				return false;
			}
			des_in = new desDataInputStream( pwdBuffer, schedule );
	
			rnd1Returned = des_in.readInt();
			if( rnd1 + 2 != rnd1Returned ) {
				errorString = "Communication error, rnd1s dont match";
				return false;
			}
			ack = des_in.readInt();

			switch( ack ) {
				case PWD_ERR_SILLYPW:
					errorString = "Your new password is too easy to guess";
					break;
				case 0:
					errorString = "Password changed";
					return true;
				default:
					errorString = "An unknown error occured";
			}
		} catch( Exception e ) {
			errorString = "Communication error";
		}
		return false;
	}

	public static void main( String[] args ){
		PasswordChanger pwc = new PasswordChanger();

		if( args.length < 3 ){
			System.err.println( "usage: java PasswordChanger <upi> <old_pw> <new_pw>" );
			System.exit( 1 );
		}
		pwc.changePassword( args[ 0 ], args[ 1 ], args[ 2 ] );
		System.err.println( pwc.errorString );
	}
}

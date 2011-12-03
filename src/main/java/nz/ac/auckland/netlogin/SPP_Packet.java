package nz.ac.auckland.netlogin;

import java.io.*;
import java.net.*;

public class SPP_Packet {

	static final int RSLT_OK							=	 0;
	static final int RSLT_COMMUNICATION_FAILUE			=	-1;
	static final int RSLT_BAD_VERSION					=	-2;
	static final int RSLT_PROTOCOL_FAILURE				=	-3;
	static final int RSLT_BAD_PACKET_TYPE				=	-4;

	static final int REMOTE_RSLT_BAD_VERSION			=	-100; //sent in the version field as a response
	static final int REMOTE_RSLT_COMMUNICATION_FAILUE 	=	-101; //sent in the version field as a response
	static final int REMOTE_RSLT_ACCESS_RESTRICTION		=	-102; //sent in the version field as a response
	static final int REMOTE_RSLT_ACCESS_RESTRICTION_BAN	=	-103; //sent in the version field as a response
	static final int REMOTE_RSLT_PROTOCOL_FAILURE		=	-104; //sent in the version field as a response

	private int connectTimeout = 3000;
	Socket passwdSocket = null;
	DataOutputStream os = null;
	DataInputStream is = null;
	int lastReadLength = 0;

	// create the socket and connect to the host.
    public SPP_Packet(String host, int port) throws IOException {
		passwdSocket = new Socket();
		passwdSocket.connect(new InetSocketAddress(host, port), connectTimeout);
		os = new DataOutputStream(new BufferedOutputStream(passwdSocket.getOutputStream()));
		is = new DataInputStream(new BufferedInputStream(passwdSocket.getInputStream()));
    }

	// Send Buffer to the distant host
	public void SendPacket(int PacketType, int version, byte buffer[]) throws IOException {
    	try { os.writeInt(buffer.length + 8);} //Data field Length + 8 bytes for the packettype and version
    	 catch (IOException e) { throw new IOException("SendPacket: 1"+ e.getMessage());	}	
    	try { os.writeInt(PacketType);	}		//Packet Typw
    	 catch (IOException e) { throw new IOException("SendPacket: 2"+ e.getMessage());	}	
    	try { os.writeInt(version);	}			//Version of Data packet
    	 catch (IOException e) { throw new IOException("SendPacket: 3"+ e.getMessage());	}	
    	try { os.write(buffer, 0, buffer.length);}	//Data
    	 catch (IOException e) { throw new IOException("SendPacket: 4"+ e.getMessage());	}	
	    try { os.flush();}							//Send it on its way.
     	 catch (IOException e) { throw new IOException("SendPacket: 5"+ e.getMessage());	}	
   }

	// Read a buffer from the distant host
    public ReadResult ReadPacket(int PacketType, int version, byte buffer[]) throws IOException {

		int i = 0;
		int got = 0;
		int RPacketType;
		int Rversion;
		int Result = RSLT_OK;
    
		lastReadLength = is.readInt() - 8;	//Read the data length field + the 8 bytes for the packettype and version
		if(lastReadLength < 0)
			throw new IOException("ReadPacket 1: data length " + lastReadLength + " < 0");

		if(lastReadLength > buffer.length)
			throw new IOException("ReadPacket 2: data length " + lastReadLength + " > buffer length");
			
		RPacketType = is.readInt();		//Read the Pack type Field
		if(RPacketType != PacketType) {
			Result = RSLT_BAD_PACKET_TYPE;
		}

		Rversion = is.readInt();		//Read the Version field
		
		if(Result == RSLT_OK && Rversion != version) {
			Result = Rversion;
		}
			
		while(i < lastReadLength && got != -1) { //we loop here until we get all the data
			try {
    			got = is.read(buffer, i, lastReadLength -i);
    		} catch(IOException e) {
    			throw new IOException("ReadPacket 5: " + e.getMessage());
    		}
    		i += got;
    	}
	    
	    return new ReadResult(Result, RPacketType, Rversion );
    }
    
    public void close() throws IOException {
	    if(passwdSocket != null) {
		    passwdSocket.close();
			passwdSocket = null;
		}
		os = null;
		is = null;
    }
    
	public void finalize() throws Throwable {
		try {
			close();
		} catch (Exception e) {
		    System.err.println("Failed to close connection: " + e.getMessage());
		}
		super.finalize();
	}
		    
}

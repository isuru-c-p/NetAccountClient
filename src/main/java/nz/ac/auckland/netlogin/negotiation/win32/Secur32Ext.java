package nz.ac.auckland.netlogin.negotiation.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Additional Secur32.dll functions not implemented in com.sun.jna.platform.win32.Secur32
 */
public interface Secur32Ext extends StdCallLibrary {

	Secur32Ext INSTANCE = (Secur32Ext) Native.loadLibrary(
			"Secur32", Secur32Ext.class, W32APIOptions.UNICODE_OPTIONS);

    public static final int SECBUFFER_STREAM = 10;

	// SECURITY_STATUS SEC_ENTRY EncryptMessage(PCtxtHandle phContext, ULONG fQOP, PSecBufferDesc pMessage, ULONG MessageSeqNo);
	public int EncryptMessage(Sspi.CtxtHandle phContext, NativeLong fQOP, SecBufferDesc2 pMessage, NativeLong messageSeqNo);

	// SECURITY_STATUS SEC_ENTRY DecryptMessage(PCtxtHandle phContext, PSecBufferDesc pMessage, ULONG MessageSeqNo, PULONG pfQOP);
	public int DecryptMessage(Sspi.CtxtHandle phContext, SecBufferDesc2 pMessage, NativeLong messageSeqNo, NativeLongByReference fQOP);

	/**
	 * Derived from Sspi.SecBufferDesc to allow a variable number of buffers.
	 */
	public static class SecBufferDesc2 extends Structure {

	    public NativeLong ulVersion;
	    public NativeLong cBuffers;
	    public Sspi.SecBuffer.ByReference[] pBuffers;

	    public SecBufferDesc2(Sspi.SecBuffer.ByReference... pBuffers) {
	    	ulVersion = new NativeLong(Sspi.SECBUFFER_VERSION);
	    	cBuffers = new NativeLong(pBuffers.length);
	    	this.pBuffers = pBuffers;
	    	allocateMemory();
	    }

	}

}

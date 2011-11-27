package nz.ac.auckland.netlogin.negotiation.win32;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * Additional Secur32.dll functions not implemented in com.sun.jna.platform.win32.Secur32
 */
public interface Secur32Ext {

	Secur32Ext INSTANCE = (Secur32Ext) Native.loadLibrary(
			"Secur32", Secur32Ext.class, W32APIOptions.UNICODE_OPTIONS);

	// SECURITY_STATUS SEC_ENTRY EncryptMessage(PCtxtHandle phContext, ULONG fQOP, PSecBufferDesc pMessage, ULONG MessageSeqNo);
	public int EncryptMessage(Sspi.CtxtHandle phContext, NativeLong fQOP, Sspi.SecBufferDesc pMessage, NativeLong messageSeqNo);

	// SECURITY_STATUS SEC_ENTRY DecryptMessage(PCtxtHandle phContext, PSecBufferDesc pMessage, ULONG MessageSeqNo, PULONG pfQOP);
	public int DecryptMessage(Sspi.CtxtHandle phContext, Sspi.SecBufferDesc pMessage, NativeLong messageSeqNo, NativeLongByReference fQOP);

}

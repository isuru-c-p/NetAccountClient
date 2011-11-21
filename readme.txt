Communications

packet 1: client -> server
AUTH_REQ_PACKET
+ client type :: int
+ client version :: int
+ upi :: byte[]
+ encrypted payload using new schedule based on password
	+ clientNonce :: int

packet 2: server -> client
AUTH_REQ_RESPONSE_PACKET
+ client rel version :: int
+ encrypted payload continuing schedule based on password
	+ clientNonce + 1 :: int
	+ serverNonce :: int
	+ session :: C_Block

packet 3: client -> server
AUTH_CONFIRM_PACKET
+ encrypted payload using new schedule based on session
	+ serverNonce + 1 :: int
	+ client command :: int = 3 / CMD_GET_USER_BALANCES_NO_BLOCK
	+ client command data length :: int = 2
	+ client command data :: short = ping port

packet 4: server -> client
AUTH_CONFIRM_RESPONSE_PACKET
+ onPlan :: int
+ localUnitCost :: int
+ intlOffPeakRate :: int
+ intlOnPeakRate :: int
+ startPeak :: int
+ endPeak :: int
+ lastModDate :: int
+ encrypted payload continuing schedule based on session
	+ clientNonce + 2 :: int
	+ ack :: int
	+ command data length :: int
	+ auth ref :: int
	+ ip usage :: int


Creating a SPN

Create account
+ full name = fqdn
+ account name = fqdn
+ pre-2000 name = hostname

On the Domain Controller, and run this from a UAC command prompt:
setspn.exe -U -S netlogin/gate-test.ec.auckland.ac.nz uoatest\gate-test


Reference

SSPI and GSSAPI interoperability
http://msdn.microsoft.com/en-us/library/ms995352.aspx
http://msdn.microsoft.com/en-us/library/windows/desktop/aa380496(v=vs.85).aspx

API (Secur32)
http://msdn.microsoft.com/en-us/library/windows/desktop/aa378261(v=vs.85).aspx
https://github.com/twall/jna/blob/master/contrib/platform/src/com/sun/jna/platform/win32/Secur32.java

Using JNA with Secur32
http://code.dblock.org/jna-acquirecredentialshandle-initializesecuritycontext-and-acceptsecuritycontext-establishing-an-authenticated-connection
https://wafle.svn.codeplex.com/svn/trunk/Source/JNAWindowsAuthProvider/src/waffle/windows/auth/impl/WindowsAuthProviderImpl.java
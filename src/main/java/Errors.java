public class Errors {

	// result codes from command requests
	public static final int CMD_RSLT_OK = 1; //Command successful.
	public static final int CMD_RSLT_INCOMPLETE = 2; //Command still in progress. (a hack)
	public static final int CMD_RSLT_WOULD_BLOCK = 10;
	public static final int CMD_RSLT_NOT_AUTHORISED = 11;
	public static final int CMD_RSLT_UNKNOWN_COMMAND = 12;
	public static final int CMD_RSLT_UNKNOWN_CLIENT = 13;
	public static final int CMD_RSLT_USER_BANNED = 14;
	public static final int CMD_RSLT_USER_WAS_BANNED = 15;
	public static final int CMD_RSLT_AUTH_FAILURE = 16;
	public static final int CMD_RSLT_COMMUNICATION_FAILURE = 17;
	public static final int CMD_RSLT_MALLOC_ERROR = 18;
	public static final int CMD_RSLT_IDENTD_FAILURE = 19;
	public static final int CMD_RSLT_USER_INVALID = 20;
	public static final int CMD_RSLT_RLOCK_FAILURE = 21;
	public static final int CMD_RSLT_WLOCK_FAILURE = 22;
	public static final int CMD_RSLT_IPAAddEntry_FAILED = 23;

	public static final String[] error_messages = {
		"", //0
		"OK", //Command successful.
		"INCOMPLETE", //Command still in progress. (a hack)
		"", //3
		"", //4
		"", //5
		"", //6
		"", //7
		"", //8
		"", //9
		"WOULD_BLOCK",
		"NOT_AUTHORISED",
		"UNKNOWN_COMMAND",
		"UNKNOWN_CLIENT",
		"USER_BANNED",
		"USER_WAS_BANNED",
		"AUTH_FAILURE",
		"COMMUNICATION_FAILUE",
		"MALLOC_ERROR",
		"IDENTD_FAILUE",
		"USER_INVALID",
		"RLOCK_FAILURE",
		"WLOCK_FAILURE",
		"IPAAddEntry_FAILED"
	};
}

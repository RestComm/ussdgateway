package org.mobicents.ussdgateway.xml;

public interface XMLTags {
	
	
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static final String XML_LT = "<";
	public static final String XML_GT = ">";
	public static final String XML_FWD_SLASH = "/";

	// Top Node
	public static final String REQUEST = "request";
	public static final String RESPONSE = "response";
	public static final String ABORT = "abort";

	public static final String INVOKE_ID = "invokeId";
	public static final String USSD_CODING = "ussdCoding";
	public static final String USSD_STRING = "ussdString";
	public static final String MSISDN = "msisdn";

	public static final String LAST_RESULT = "lastResult";
	public static final String END = "end";
	
	public static final String USER_SPECIFIC_REASON = "userSpecificReason";
	public static final String USER_RESOURCE_LIMITATION = "userResourceLimitation";
	public static final String RESOURCE_UNAVAILABLE = "resourceUnavailable";
		public static final String SHORT_TERM_RESOURCE_LIMITATION = "shortTermResourceLimitation";
		public static final String LONG_TERM_RESOURCE_LIMITATION = "longTermResourceLimitation";
	
	public static final String APP_PROCEDURE_CANCELLATION = "applicationProcedureCancellation"; 
		public static final String HANDOVER_CANCELLATION = "handoverCancellation";
		public static final String RADIO_CHANNEL_RELEASE = "radioChannelRelease";
		public static final String NETWORK_PATH_RELEASE = "networkPathRelease";
		public static final String CALL_RELEASE = "callRelease";
		public static final String ASSOCIATED_PROC_FAILURE = "associatedProcedureFailure";
		public static final String TANDEM_DIALOGUE_RELEASE = "tandemDialogueRelease";
		public static final String REMOTE_OPERATION_FAILURE = "remoteOperationsFailure";		
}

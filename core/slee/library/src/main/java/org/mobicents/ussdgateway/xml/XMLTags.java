/**
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.ussdgateway.xml;

/**
 * 
 * @author amit bhayani
 *
 */
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

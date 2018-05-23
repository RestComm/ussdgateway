/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.applications.ussd.examples.sip;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.mobicents.ussdgateway.AnyExt;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.SipUssdMessage;

import java.text.ParseException;
import java.util.*;

/**
 * @author Amit Bhayani
 * 
 */
public class SipServerTest implements SipListener {

	private static final String CONTENT_TYPE = "application";
	private static final String CONTENT_SUB_TYPE = "vnd.3gpp.ussd+xml";

	// If you want to try TCP transport change the following to
	private String transport = "udp";
	private String peerHostPort = "127.0.0.1:5060";

	private String fromName = "BigGuy";
	private String fromSipAddress = "here.com";
	private String fromDisplayName = "The Master Blaster";

	private String toSipAddress = "there.com";
	private String toUser = "919960639901";
	private String toDisplayName = "919960639901";

	private static SipProvider sipProvider;

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;

	private ContactHeader contactHeader;

	private ListeningPoint udpListeningPoint;

	private ClientTransaction inviteTid;

	private Dialog dialog;

	private boolean byeTaskRunning;

	private EventsSerializeFactory eventsSerializeFactory;

	/**
	 * 
	 */
	public SipServerTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SipServerTest().init();
	}

	class ByeTask extends TimerTask {
		Dialog dialog;

		public ByeTask(Dialog dialog) {
			this.dialog = dialog;
		}

		public void run() {
			try {
				Request byeRequest = this.dialog.createRequest(Request.BYE);
				ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
				dialog.sendRequest(ct);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}

		}
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod() + " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE)) {
			processBye(request, serverTransactionId);
		} else if (request.getMethod().equals(Request.INFO)) {
			processInfo(requestReceivedEvent, serverTransactionId);
		} else {
			try {
				serverTransactionId.sendResponse(messageFactory.createResponse(202, request));
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void sendUnstructuredSSNotify(RequestEvent requestEvent) throws Exception {
		//requestSentCout++;
		SipUssdMessage simMsg = new SipUssdMessage("Thank you your balance is XX", "en");
		simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSNotify_Request));
		
		byte[] rawContent = this.eventsSerializeFactory.serializeSipUssdMessage(simMsg);
		String content = new String(rawContent);

		SipProvider provider = (SipProvider) requestEvent.getSource();
		Request infoRequest = dialog.createRequest(Request.INFO);

		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);
		infoRequest.setContent(content, contentTypeHeader);

		ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
		infoRequest.setContentLength(contentLengthHeader);

		ClientTransaction ct = provider.getNewClientTransaction(infoRequest);
		dialog.sendRequest(ct);
	}
	
	public void processInfo(RequestEvent requestEvent, ServerTransaction serverTransactionId) {

		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();

		try {
			System.out.println("SipServerTest: got an INFO sending INFO again");

			javax.sip.header.ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request
					.getHeader(ContentTypeHeader.NAME);

			System.out.println("ContentTypeHeader " + contentTypeHeader);

			if (contentTypeHeader == null) {
				// TODO This is error
			}

			String contentType = contentTypeHeader.getContentType();
			String contentSubType = contentTypeHeader.getContentSubType();

			if (contentType == null || !contentType.equals("application") || contentSubType == null
					|| contentSubType.equals("vnd.3gpp.ussd+xml")) {
				// TODO this is also error
			}

			byte[] content = request.getRawContent();
			System.out.println("Content " + new String(content));

			SipUssdMessage originalMessage = this.eventsSerializeFactory.deserializeSipUssdMessage(content);
			System.out.println("Deserialized SipUssdMessage : " + originalMessage);
			
			// System.out.println("shootme: " + request);
			Response response = messageFactory.createResponse(Response.OK, request);
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			dialog = st.getDialog();

			st.sendResponse(response);
			
			AnyExt anyExt = originalMessage.getAnyExt();
			if(anyExt != null){
				MAPMessageType mapMessageType = anyExt.getMapMessageType();
				if(mapMessageType == MAPMessageType.unstructuredSSNotify_Response){
					//This is Response lets just send BYE
					//Timer will expire in 4 sec and will automatically send BYE
					
					return;
					
				}
			}

			this.sendUnstructuredSSNotify(requestEvent);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processBye(Request request, ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	// Save the created ACK request, to respond to retransmitted 2xx
	private Request ackRequest;

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		System.out.println("Response received : Status Code = " + response.getStatusCode() + " " + cseq);

		if (tid == null) {

			// RFC3261: MUST respond to every 2xx
			if (ackRequest != null && dialog != null) {
				System.out.println("re-sending ACK");
				try {
					dialog.sendAck(ackRequest);
				} catch (SipException se) {
					se.printStackTrace();
				}
			}
			return;
		}
		// If the caller is supposed to send the bye
		// if (examples.simplecallsetup.Shootme.callerSendsBye &&
		// !byeTaskRunning) {
		if (!byeTaskRunning) {
			byeTaskRunning = true;
			new Timer().schedule(new ByeTask(dialog), 4000);
		}
		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					System.out.println("Dialog after 200 OK  " + dialog);
					System.out.println("Dialog State after 200 OK  " + dialog.getState());
					ackRequest = dialog.createAck(((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber());
					System.out.println("Sending ACK");
					dialog.sendAck(ackRequest);

					// JvB: test REFER, reported bug in tag handling
					// dialog.sendRequest( sipProvider.getNewClientTransaction(
					// dialog.createRequest("REFER") ));

				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the
						// dialog.
						System.out.println("Sending BYE -- cancel went in too late !!");
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);

					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out");
	}

	public void sendCancel() {
		try {
			System.out.println("Sending cancel");
			Request cancelRequest = inviteTid.createCancel();
			ClientTransaction cancelTid = sipProvider.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() {

		eventsSerializeFactory = new EventsSerializeFactory();

		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();

		// String peerHostPort = "230.0.0.1:5070";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/" + transport);
		// If you want to use UDP then uncomment this.
		properties.setProperty("javax.sip.STACK_NAME", "shootist");

		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootistdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootistlog.txt");

		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
		// Set to 0 (or NONE) in your production code for max speed.
		// You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug +
		// traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			udpListeningPoint = sipStack.createListeningPoint("127.0.0.1", 5070, transport);
			System.out.println("listeningPoint = " + udpListeningPoint);
			sipProvider = sipStack.createSipProvider(udpListeningPoint);
			System.out.println("SipProvider = " + sipProvider);
			SipServerTest listener = this;
			sipProvider.addSipListener(listener);

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress = addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI = addressFactory.createSipURI(toUser, peerHostPort);

			// Create ViaHeaders

			ArrayList viaHeaders = new ArrayList();
			String ipAddress = udpListeningPoint.getIPAddress();
			ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress, sipProvider.getListeningPoint(transport)
					.getPort(), transport, null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader,
					fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers
			String host = "127.0.0.1";

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(udpListeningPoint.getPort());
			contactUrl.setLrParam();

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);

			SipUssdMessage simMsg = new SipUssdMessage("Press 1 for SMS and 2 for Call", "en");
			byte[] data = eventsSerializeFactory.serializeSipUssdMessage(simMsg);
			String content = new String(data);

			byte[] contents = content.getBytes();

			request.setContent(contents, contentTypeHeader);
			// You can add as many extension headers as you
			// want.

			Header callInfoHeader = headerFactory.createHeader("Call-Info", "<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);

			// Create the client transaction.
			inviteTid = sipProvider.getNewClientTransaction(request);

			System.out.println("inviteTid = " + inviteTid);

			// send the request out.

			inviteTid.sendRequest();

			dialog = inviteTid.getDialog();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for " + exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());

	}

	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");

	}

}

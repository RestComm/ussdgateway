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

import java.text.ParseException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.SipUssdMessage;

/**
 * Simple SIP Client that receives INVITE from USSD Gateway and responds
 * accordingly
 * 
 * @author Amit Bhayani
 * 
 */
public class SipClientTest implements SipListener {

	private final Logger logger = Logger.getLogger(SipClientTest.class.getName());

	private static final String CONTENT_TYPE = "application";
	private static final String CONTENT_SUB_TYPE = "vnd.3gpp.ussd+xml";

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;

	private static final String myAddress = "127.0.0.1";

	private static final int myPort = 5065;

	protected ServerTransaction inviteTid;

	private Response okResponse;

	private Request inviteRequest;

	private Dialog dialog;

	public static final boolean callerSendsBye = true;

	private volatile int requestSentCout = 0;

	private EventsSerializeFactory eventsSerializeFactory;

	class MyTimerTask extends TimerTask {
		SipClientTest shootme;

		public MyTimerTask(SipClientTest shootme) {
			this.shootme = shootme;

		}

		public void run() {
			shootme.sendInviteOK();
		}

	}

	protected static final String usageString = "java " + "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}

	/**
	 * 
	 */
	public SipClientTest() {

	}

	public void processRequest(RequestEvent requestEvent) {

		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod() + " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.INFO)) {
			processInfo(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.CANCEL)) {
			processCancel(requestEvent, serverTransactionId);
		} else {
			try {
				serverTransactionId.sendResponse(messageFactory.createResponse(202, request));

				// send one back
				SipProvider prov = (SipProvider) requestEvent.getSource();
				Request refer = requestEvent.getDialog().createRequest("REFER");
				requestEvent.getDialog().sendRequest(prov.getNewClientTransaction(refer));

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

	public void processResponse(ResponseEvent responseEvent) {
	}

	/**
	 * Process the ACK request. Send the bye and complete the call flow.
	 */
	public void processAck(RequestEvent requestEvent, ServerTransaction serverTransaction) {
		try {
			System.out.println("shootme: got an ACK! ");
			System.out.println("Dialog State = " + dialog.getState());
			SipProvider provider = (SipProvider) requestEvent.getSource();
			if (!callerSendsBye) {
				Request byeRequest = dialog.createRequest(Request.BYE);
				ClientTransaction ct = provider.getNewClientTransaction(byeRequest);
				dialog.sendRequest(ct);

				return;
			}

			if (requestSentCout == 0) {
				// Lets send Unstructured SS Request to other side
				sendUnstructuredSSRequest(requestEvent);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void sendUnstructuredSSRequest(RequestEvent requestEvent) throws Exception {
		requestSentCout++;
		SipUssdMessage simMsg = new SipUssdMessage("Press 1 to know your airtime and 2 to know your SMS balance", "en");
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

	public void processInfo(RequestEvent requestEvent, ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();

		requestSentCout = 0;
		try {
			System.out.println("shootme: got an INFO Send BYE");

			// send back ACK
			ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
			Response response = messageFactory.createResponse(Response.OK, request);
			serverTransactionId.sendResponse(response);

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

			byte[] rawContent = request.getRawContent();
			System.out.println("Content " + new String(rawContent));

			SipUssdMessage originalMessage = this.eventsSerializeFactory.deserializeSipUssdMessage(rawContent);
			System.out.println("Deserialized SipUssdMessage : " + originalMessage);

			SipUssdMessage simMsg = new SipUssdMessage("Thank You!", "en");
			rawContent = this.eventsSerializeFactory.serializeSipUssdMessage(simMsg);
			String content = new String(rawContent);

			Request byeRequest = dialog.createRequest(Request.BYE);

			contentTypeHeader = headerFactory.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);
			byeRequest.setContent(content, contentTypeHeader);

			ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
			byeRequest.setContentLength(contentLengthHeader);

			ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
			dialog.sendRequest(ct);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	/**
	 * Process the invite request.
	 */
	public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {

		requestSentCout = 0;

		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme: got an Invite sending Trying");

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
			Response response = messageFactory.createResponse(Response.RINGING, request);
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			dialog = st.getDialog();

			st.sendResponse(response);

			this.okResponse = messageFactory.createResponse(Response.OK, request);
			Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory.createContactHeader(address);
			response.addHeader(contactHeader);
			ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			okResponse.addHeader(contactHeader);
			this.inviteTid = st;
			// Defer sending the OK to simulate the phone ringing.
			// Answered in 1 second ( this guy is fast at taking calls)
			this.inviteRequest = request;

			new Timer().schedule(new MyTimerTask(this), 1000);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	private void sendInviteOK() {
		try {
			if (inviteTid.getState() != TransactionState.COMPLETED) {
				System.out.println("shootme: Dialog state before 200: " + inviteTid.getDialog().getState());
				inviteTid.sendResponse(okResponse);
				System.out.println("shootme: Dialog state after 200: " + inviteTid.getDialog().getState());
			}
		} catch (SipException ex) {
			ex.printStackTrace();
		} catch (InvalidArgumentException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Process the bye request.
	 */
	public void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {

		requestSentCout = 0;

		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		Dialog dialog = requestEvent.getDialog();
		System.out.println("local party = " + dialog.getLocalParty());
		try {
			System.out.println("shootme:  got a bye sending OK.");
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processCancel(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme:  got a cancel.");
			if (serverTransactionId == null) {
				System.out.println("shootme:  null tid.");
				return;
			}
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			if (dialog.getState() != DialogState.CONFIRMED) {
				response = messageFactory.createResponse(Response.REQUEST_TERMINATED, inviteRequest);
				inviteTid.sendResponse(response);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println("dialogState = " + transaction.getDialog().getState());
		System.out.println("Transaction Time out");
	}

	public void init() {

		eventsSerializeFactory = new EventsSerializeFactory();

		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootmedebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootmelog.txt");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1", myPort, "udp");

			SipClientTest listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}

	}

	public static void main(String args[]) {
		new SipClientTest().init();
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException");

	}

	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		if (transactionTerminatedEvent.isServerTransaction())
			System.out.println("Transaction terminated event recieved"
					+ transactionTerminatedEvent.getServerTransaction());
		else
			System.out.println("Transaction terminated " + transactionTerminatedEvent.getClientTransaction());

	}

	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog terminated event recieved");
		Dialog d = dialogTerminatedEvent.getDialog();
		System.out.println("Local Party = " + d.getLocalParty());

	}

}

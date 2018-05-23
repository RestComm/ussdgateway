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

package org.mobicents.ussdgateway.slee.sip;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sdp.SdpFactory;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.SbbContext;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.util.FastList;
import net.java.slee.resource.sip.DialogActivity;
import net.java.slee.resource.sip.SipActivityContextInterfaceFactory;
import net.java.slee.resource.sip.SleeSipProvider;

import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.mobicents.ussdgateway.AnyExt;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.SipUssdErrorCode;
import org.mobicents.ussdgateway.SipUssdMessage;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.ChildSbb;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;

/**
 * 
 * @author amit bhayani
 * @author sergey vetyutnev
 * 
 */
public abstract class SipClientSbb extends ChildSbb {

	// Get the transport
	private final String TRANSPORT = "udp";

	public static final String FROM_DISPLAY_NAME = "MobicentsUSSDGateway";

	private static final String CONTENT_TYPE = "application";
	private static final String CONTENT_SUB_TYPE = "vnd.3gpp.ussd+xml";

	private static final String RECV_INFO_HEADER_NAME = "Recv-Info";
	private static final String RECV_INFO_HEADER_VALUE = "g.3gpp.ussd";
	private static final String ACCEPT_HEADER_VALUE = "application/sdp; application/3gpp-ims+xml; application/vnd.3gpp.ussd+xml";

	protected String ipAddress;
	protected int port;

	// /////////////////
	// SIP RA Stuff //
	// /////////////////

	private static final ResourceAdaptorTypeID sipRATypeID = new ResourceAdaptorTypeID("JAIN SIP", "javax.sip", "1.2");
	private static final String sipRALink = "SipRA";
	protected SleeSipProvider provider;

	protected AddressFactory addressFactory;
	protected HeaderFactory headerFactory;
	protected SdpFactory sdpFactory;
	protected MessageFactory messageFactory;
	protected SipActivityContextInterfaceFactory sipActConIntFac;

	/** Creates a new instance of CallSbb */
	public SipClientSbb() {
		super("SipClientSbb");
	}

	// //////////////////////
	// SIP Event handlers //
	// //////////////////////

	public void onTryingRespEvent(ResponseEvent event, ActivityContextInterface aci) {
		if (this.logger.isFineEnabled()) {
			this.logger.fine("Received the TRYING Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}
	}

	public void onProvisionalRespEvent(ResponseEvent event, ActivityContextInterface aci) {
		if (this.logger.isFineEnabled()) {
			this.logger.fine("Received the PROVISIONAL Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}
	}

	public void onSuccessRespEvent(ResponseEvent event, ActivityContextInterface aci) {

		if (this.logger.isFineEnabled()) {
			this.logger.fine("Received the SUCCESS Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}
		
		try {

			Response response = event.getResponse();
			Dialog dialog = event.getDialog();
			int status = response.getStatusCode();

			if (logger.isFineEnabled()) {
				logger.fine("Received success response event " + status);
			}
			CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

			if (cseq.getMethod().equals(Request.INVITE)) {
				// Send ACK
				Request ack = dialog.createAck(dialog.getLocalSeqNumber());
				dialog.sendAck(ack);
			}
		} catch (Exception e) {
			logger.severe("Error while processing 2xx \n" + event, e);

			this.sendServerErrorMessage();
            this.abortSipDialog();
            this.updateDialogFailureStat();
			this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_FAILURE);
			return;
		}
	}

	public void onGlobalFailureRespEvent(ResponseEvent event, ActivityContextInterface aci) {
		if (this.logger.isSevereEnabled()) {
			this.logger.severe("Received GLOBAL_FAILURE Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}

		this.sendServerErrorMessage();
		abortSipDialog();
        this.updateDialogFailureStat();
		this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);
		return;
	}

	public void onClientErrorRespEvent(ResponseEvent event, ActivityContextInterface ac) {
		if (this.logger.isSevereEnabled()) {
			this.logger.severe("Received CLIENT_ERROR Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}

		this.sendServerErrorMessage();
		abortSipDialog();
        this.updateDialogFailureStat();
		this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);
	}

	public void onServerErrorRespEvent(ResponseEvent event, ActivityContextInterface ac) {
		if (this.logger.isSevereEnabled()) {
			this.logger.severe("Received SERVER_ERROR Response " + event.getResponse().getStatusCode()
					+ " For SIP Dialog Id " + this.getDialogActivityId());
		}

		this.sendServerErrorMessage();
		abortSipDialog();
        this.updateDialogFailureStat();
		this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);
	}

	public void onRedirectRespEvent(ResponseEvent event, ActivityContextInterface aci) {
		logger.severe("Received onRedirectRespEvent " + event);

		// TODO what to do?
	}

	public void onInfoReqEvent(RequestEvent event, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			logger.info("Received onInfoReqEvent " + event);
		}

		this.cancelTimer();

		Request request = event.getRequest();

		try {
			// send back ACK
			ServerTransaction serverTransactionId = event.getServerTransaction();
			Response response = this.messageFactory.createResponse(Response.OK, request);
			serverTransactionId.sendResponse(response);

			// TODO test content-type header

			byte[] rawContent = request.getRawContent();

			if (logger.isInfoEnabled()) {
				logger.info("Payload " + rawContent);
			}

			if (rawContent == null || rawContent.length <= 0) {
                logger.severe("Received INFO but USSD Payload is null \n" + event);

                this.sendServerErrorMessage();

                SipUssdMessage sipMsg = new SipUssdMessage(SipUssdErrorCode.unexpectedDataValue);
                abortSipDialog(sipMsg);

				this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
				return;
			}

			EventsSerializeFactory factory = this.getEventsSerializeFactory();
			SipUssdMessage sipUssdMessage = factory.deserializeSipUssdMessage(rawContent);

			if (sipUssdMessage == null) {
				logger.severe("Received INFO but couldn't deserialize to SipUssdMessage. SipUssdMessage is null \n"
						+ event);

				this.sendServerErrorMessage();

				SipUssdMessage sipMsg = new SipUssdMessage(SipUssdErrorCode.unexpectedDataValue);
				this.sendBye(sipMsg);

	            this.updateDialogFailureStat();
				this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
				return;
			}

            MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();
            if (mapDialogSupplementary == null) {
                logger.warning("Error while processing INFO event: no MAP dialog is found, terminating of SIP dialog");
                abortSipDialog();
                this.updateDialogFailureStat();
                this.createCDRRecord(RecordStatus.FAILED_DIALOG_TIMEOUT);
                return;
            }

            if (sipUssdMessage.isSuccessMessage()) {
				CBSDataCodingScheme cbsDataCodingScheme = sipUssdMessage.getCBSDataCodingScheme();
				USSDStringImpl ussdStr = new USSDStringImpl(sipUssdMessage.getUssdString(), cbsDataCodingScheme, null);

				mapDialogSupplementary.addUnstructuredSSRequest(cbsDataCodingScheme, ussdStr, null, null);
                super.ussdStatAggregator.updateUssdRequestOperations();
                super.ussdStatAggregator.updateMessagesSent();
                super.ussdStatAggregator.updateMessagesAll();

				mapDialogSupplementary.send();
			} else {
				this.sendServerErrorMessage();

				SipUssdMessage sipMsg = new SipUssdMessage(SipUssdErrorCode.unexpectedDataValue);
                this.sendBye(sipMsg);

                this.updateDialogFailureStat();
                this.createCDRRecord(RecordStatus.ABORT_APP);
				return;
			}

		} catch (Exception e) {
			logger.severe("Error while processing INFO event", e);

			this.sendServerErrorMessage();
			abortSipDialog();
	        this.updateDialogFailureStat();
			this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
		}

	}

	public void onCallTerminated(RequestEvent evt, ActivityContextInterface aci) {

		if (this.logger.isFineEnabled()) {
			this.logger.fine("Received BYE Request For SIP Dialog Id " + this.getDialogActivityId());
		}
		
		this.cancelTimer();

		ServerTransaction tx = evt.getServerTransaction();
		Request request = evt.getRequest();

		try {
			Response response = messageFactory.createResponse(Response.OK, request);
			tx.sendResponse(response);
		} catch (Exception e) {
			logger.severe("Error while sending OK to received BYE \n" + evt, e);
		}

		try {

			// TODO test content-type header

			byte[] rawContent = request.getRawContent();

            if (logger.isInfoEnabled()) {
                logger.info("Payload " + rawContent);
            }

			if (rawContent == null || rawContent.length <= 0) {
                logger.severe("Received BYE but USSD Payload is null \n" + evt);

                this.sendServerErrorMessage();
                this.updateDialogFailureStat();
                this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
                return;
			}

			EventsSerializeFactory factory = this.getEventsSerializeFactory();
			SipUssdMessage sipUssdMessage = factory.deserializeSipUssdMessage(rawContent);

			if (sipUssdMessage == null) {
				logger.severe("Received BYE but couldn't deserialize to SipUssdMessage. SipUssdMessage is null \n"
						+ evt);
				this.sendServerErrorMessage();
                this.updateDialogFailureStat();
				this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
				return;
			}

			MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();

			if (sipUssdMessage.isSuccessMessage()) {
				CBSDataCodingScheme cbsDataCodingScheme = sipUssdMessage.getCBSDataCodingScheme();
				USSDStringImpl ussdStr = new USSDStringImpl(sipUssdMessage.getUssdString(), cbsDataCodingScheme, null);

				mapDialogSupplementary.addProcessUnstructuredSSResponse(this.getProcessUnstructuredSSRequestInvokeId(),
						cbsDataCodingScheme, ussdStr);

				mapDialogSupplementary.close(false);
				
				this.createCDRRecord(RecordStatus.SUCCESS);
			} else {
				MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
				abort.setUserSpecificReason();
				mapDialogSupplementary.abort(abort);
                this.updateDialogFailureStat();
				this.createCDRRecord(RecordStatus.ABORT_APP);
			}

		} catch (Exception e) {
			logger.severe("Error while processing BYE event \n" + evt, e);
			this.sendServerErrorMessage();
            this.updateDialogFailureStat();
			this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
		}

	}

	@Override
	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);

		try {

			// initialize SIP API
			this.sipActConIntFac = (SipActivityContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(sipRATypeID);
			this.provider = (SleeSipProvider) super.sbbContext.getResourceAdaptorInterface(sipRATypeID, sipRALink);
			// this.mapServiceFactory = this.mapProvider.getMapServiceFactory();

			this.ipAddress = this.provider.getListeningPoint(TRANSPORT).getIPAddress();
			this.port = this.provider.getListeningPoint(TRANSPORT).getPort();

			addressFactory = provider.getAddressFactory();
			headerFactory = provider.getHeaderFactory();
			sdpFactory = SdpFactory.getInstance();
			messageFactory = provider.getMessageFactory();
		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	// //////////////////////////////
	// Child abstract implemented //
	// //////////////////////////////

    @Override
    protected void updateDialogFailureStat() {
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPullFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
    }

	@Override
	protected void sendUssdData(XmlMAPDialog xmlMAPDialog) throws Exception {

		MessageType messageType = xmlMAPDialog.getTCAPMessageType();

		switch (messageType) {
		case Begin:
			// Initiate SIP Dialog
			// Create INVITE Request
			Request inviteReq = this.buildInvite(getCall(), xmlMAPDialog);

			this.logger.info("Trying to send INVITE message:\n" + inviteReq);

			// get new Client Tx
			ClientTransaction inviteCt = this.provider.getNewClientTransaction(inviteReq);

			// TODO : Should it attach to Client Tx?
			// Attach this SBB to Client Tx Activity
			ActivityContextInterface calleeAci = this.sipActConIntFac.getActivityContextInterface(inviteCt);
			calleeAci.attach(this.sbbContext.getSbbLocalObject());

			// Get Dialog
			Dialog dialog = inviteCt.getDialog();
			if (dialog != null) {
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Obtained dialog from ClientTransaction : automatic dialog support on");
				}
			} else {
				// Automatic dialog support turned off
				dialog = this.provider.getNewDialog(inviteCt);
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Obtained dialog for INVITE request to callee with getNewDialog");
				}
			}

			// Attach this SBB to Dialog Activity
			ActivityContextInterface calleeDialogAci = this.sipActConIntFac
					.getActivityContextInterface((DialogActivity) dialog);
			calleeDialogAci.attach(this.sbbContext.getSbbLocalObject());

			dialog.terminateOnBye(true);

			// Send Request
			inviteCt.sendRequest();
			break;
		case Continue:
			UnstructuredSSResponse unstructuredSSResponse = (UnstructuredSSResponse) xmlMAPDialog.getMAPMessages()
					.getFirst();

			SipUssdMessage simMsg = new SipUssdMessage(unstructuredSSResponse.getDataCodingScheme(),
					unstructuredSSResponse.getUSSDString());
			simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSRequest_Response));
			
			byte[] data = this.getEventsSerializeFactory().serializeSipUssdMessage(simMsg);
			String content = new String(data);

			DialogActivity sipDialogActivity = this.getDialog();
			Request infoRequest = sipDialogActivity.createRequest(Request.INFO);
			
			// Create the Via header and add to an array list
			ListeningPoint listeningPoint = provider.getListeningPoints()[0];
			ViaHeader viaHeader = headerFactory.createViaHeader(listeningPoint.getIPAddress(), listeningPoint.getPort(),
					listeningPoint.getTransport(), null);
			viaHeader.setRPort();
			
			infoRequest.setHeader(viaHeader);

			ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);
			infoRequest.setContent(content, contentTypeHeader);

			ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
			infoRequest.setContentLength(contentLengthHeader);

			ClientTransaction infoCt = provider.getNewClientTransaction(infoRequest);

			// TODO : Should it attach to Client Tx?
			// Attach this SBB to Client Tx Activity
			ActivityContextInterface infoRequestAci = this.sipActConIntFac.getActivityContextInterface(infoCt);
			infoRequestAci.attach(this.sbbContext.getSbbLocalObject());

			sipDialogActivity.sendRequest(infoCt);

			break;
		case Abort:
			abortSipDialog();
			break;
		case End:
			// End can never come from Mobile side
			// TODO may be this is error?
			logger.severe("Received unexpected MessageType.End " + xmlMAPDialog);
            abortSipDialog();
			break;
		case Unknown:
			// Unknown can never come from Mobile side
			// TODO may be this is error?
			logger.severe("Received unexpected MessageType.Unknown " + xmlMAPDialog);
			break;
		default:
			logger.severe("Received unidentified MessageType " + xmlMAPDialog);
			break;
		}
	}

	@Override
	protected boolean checkProtocolConnection() {
		return getDialog() != null;
	}
	
	private String generateTag() {
		String tag = new Integer((int) (Math.random() * 10000)).toString();
		return tag;
	}

	private ContactHeader createLocalContactHeader(String fromMsisdnStr) throws ParseException {
		SipURI contactURI = this.addressFactory.createSipURI(fromMsisdnStr, this.ipAddress);
		contactURI.setPort(this.port);
		Address contactAddress = this.addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(FROM_DISPLAY_NAME);
		ContactHeader contactHeader = this.headerFactory.createContactHeader(contactAddress);
		return contactHeader;
	}

	protected Request buildInvite(ScRoutingRule call, XmlMAPDialog xmlMAPDialog) throws Exception {

		FastList<MAPMessage> mList = xmlMAPDialog.getMAPMessages();
		MAPMessage mes = mList.getFirst();
		if (mes == null) {
			throw new NullPointerException("No USSD message to send to SIP.");
		}
		if (mes.getMessageType() != MAPMessageType.processUnstructuredSSRequest_Request) {
			throw new Exception("Bad USSD message type to send to SIP: " + mes.getMessageType());
		}
		ProcessUnstructuredSSRequest pmes = (ProcessUnstructuredSSRequest) mes;
		
		ISDNAddressString fromMsisdn = pmes.getMSISDNAddressString();
		
		
		String fromMsisdnStr = null;
		if(fromMsisdn != null){
			fromMsisdnStr = fromMsisdn.getAddress();
		} else {
			AddressString fromAddressString = xmlMAPDialog.getReceivedOrigReference();
			fromMsisdnStr = fromAddressString.getAddress();
		}
		
		
		USSDString ussd = pmes.getUSSDString();
		CBSDataCodingScheme dcs = pmes.getDataCodingScheme();
		String reqMessage = ussd.getString(null);
		int ind1 = reqMessage.indexOf("#");
		String destString;
		if (ind1 > 0) {
			destString = reqMessage.substring(0, ind1 + 1);
		} else {
			destString = reqMessage;
		}
		destString = destString.replaceAll("#", "%23");

		SipURI toSipUri = addressFactory.createSipURI(destString, call.getSipProxy()); // user,
																						// host

		Address toAddress = this.addressFactory.createAddress(toSipUri);
		// !!!!: TODO: changing of To and SIP-URI to special USSD form

		// To header:
		ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

		// Locale x;

		// From Header:
		ListeningPoint listeningPoint = provider.getListeningPoints()[0];

		SipURI fromAddressUri = addressFactory.createSipURI(fromMsisdnStr, listeningPoint.getIPAddress() + ":"
				+ listeningPoint.getPort());

		javax.sip.address.Address fromAddress = addressFactory.createAddress(fromAddressUri);

		FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, generateTag());

		// Set the sequence number for the invite
		CSeqHeader cseqHeader = headerFactory.createCSeqHeader(1l, Request.INVITE);

		// Create the Via header and add to an array list
		List<ViaHeader> viaHeadersList = new ArrayList<ViaHeader>(1);

		ViaHeader viaHeader = headerFactory.createViaHeader(listeningPoint.getIPAddress(), listeningPoint.getPort(),
				listeningPoint.getTransport(), null);
		viaHeader.setRPort();
		viaHeadersList.add(viaHeader);

		MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

		CallIdHeader callIdHeader = this.provider.getNewCallId();

		Request request = this.messageFactory.createRequest(toSipUri, Request.INVITE, callIdHeader, cseqHeader,
				fromHeader, toHeader, viaHeadersList, maxForwardsHeader);

		ContactHeader contactHeader = createLocalContactHeader(fromMsisdnStr);
		request.setHeader(contactHeader);

		// Set Content
		SipUssdMessage simMsg = new SipUssdMessage(pmes.getDataCodingScheme(), pmes.getUSSDString());
		simMsg.setAnyExt(new AnyExt(MAPMessageType.processUnstructuredSSRequest_Request));
		
		byte[] data = this.getEventsSerializeFactory().serializeSipUssdMessage(simMsg);
		String content = new String(data);

		// Set Content
		ContentTypeHeader contentTypeHeader = this.headerFactory
				.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);
		request.setContent(content, contentTypeHeader);

		ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
		request.setContentLength(contentLengthHeader);

		return request;
	}

	private DialogActivity getDialog() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			if (aci.getActivity() instanceof DialogActivity) {
				return (DialogActivity) aci.getActivity();
			}
		}

		return null;
	}

	private String getDialogActivityId() {
		DialogActivity act = this.getDialog();
		if (act != null)
			return act.getDialogId();
		else
			return null;
	}

	private void sendBye(SipUssdMessage simMsg) {
		Dialog sipDialog = this.getDialog();

		if (sipDialog == null) {
			// Most probably the Dialog between the GW and application died!
			return;
		}

		if (sipDialog.getState() == DialogState.CONFIRMED) {
			// TODO : Confirm BYE is to be sent only for Confirmed Dialog

			try {
				Request byeRequest = sipDialog.createRequest(Request.BYE);

				byte[] data = this.getEventsSerializeFactory().serializeSipUssdMessage(simMsg);
				String content = new String(data);

				ContentTypeHeader contentTypeHeader = this.headerFactory.createContentTypeHeader(CONTENT_TYPE,
						CONTENT_SUB_TYPE);

				byeRequest.setContent(content, contentTypeHeader);

				ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
				byeRequest.setContentLength(contentLengthHeader);

				ClientTransaction ct = this.provider.getNewClientTransaction(byeRequest);
				ActivityContextInterface calleeAci = this.sipActConIntFac.getActivityContextInterface(ct);
				calleeAci.attach(this.sbbContext.getSbbLocalObject());
				sipDialog.sendRequest(ct);
			} catch (Exception e) {
				this.logger.severe("Error while sending BYE Request ", e);
			}
		} else {
			logger.warning("Trying to send BYE for Dialog that is not CONFIRMED. Will delete SIP Dialog \n" + sipDialog);
			this.terminateProtocolConnection();
		}
	}

    private void abortSipDialog() {
        SipUssdMessage sipMsg = new SipUssdMessage(SipUssdErrorCode.errorUnspecified);
        abortSipDialog(sipMsg);
    }

    private void abortSipDialog(SipUssdMessage sipMsg) {
        this.sendBye(sipMsg);
    }

    @Override
    protected void terminateProtocolConnection() {
      Dialog sipDialog = this.getDialog();

      if (sipDialog == null) {
          // Most probably the Dialog between the GW and application died!
          return;
      }
      sipDialog.delete();
    }

    protected boolean isSip() {
        return true;
    }
}

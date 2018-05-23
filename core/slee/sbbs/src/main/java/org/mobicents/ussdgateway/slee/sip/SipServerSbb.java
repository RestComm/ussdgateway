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

import javax.sdp.SdpFactory;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.EventContext;
import javax.slee.SLEEException;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.xml.stream.XMLStreamException;
import net.java.slee.resource.sip.DialogActivity;
import net.java.slee.resource.sip.SipActivityContextInterfaceFactory;
import net.java.slee.resource.sip.SleeSipProvider;

import org.joda.time.DateTime;
import org.restcomm.protocols.ss7.indicator.NatureOfAddress;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.dialog.MAPDialogState;
import org.restcomm.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.restcomm.protocols.ss7.map.api.dialog.ProcedureCancellationReason;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorCode;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingLevel;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingPattern;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.restcomm.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.restcomm.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.sccp.impl.parameter.ParameterFactoryImpl;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.ParameterFactory;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.mobicents.slee.ChildRelationExt;
import org.restcomm.slee.resource.map.MAPContextInterfaceFactory;
import org.restcomm.slee.resource.map.events.DialogAccept;
import org.restcomm.slee.resource.map.events.DialogProviderAbort;
import org.restcomm.slee.resource.map.events.DialogReject;
import org.restcomm.slee.resource.map.events.DialogRelease;
import org.restcomm.slee.resource.map.events.DialogTimeout;
import org.restcomm.slee.resource.map.events.DialogUserAbort;
import org.restcomm.slee.resource.map.events.ErrorComponent;
import org.restcomm.slee.resource.map.events.InvokeTimeout;
import org.restcomm.slee.resource.map.events.MAPEvent;
import org.restcomm.slee.resource.map.events.RejectComponent;
import org.mobicents.ussdgateway.AnyExt;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.SipUssdErrorCode;
import org.mobicents.ussdgateway.SipUssdMessage;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.UssdStatAggregator;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.slee.ChildServerSbb;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;
import org.mobicents.ussdgateway.slee.cdr.USSDType;
import org.mobicents.ussdgateway.slee.sri.SriChild;
import org.mobicents.ussdgateway.slee.sri.SriParent;
import org.mobicents.ussdgateway.slee.sri.SriSbbLocalObject;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 * 
 */
public abstract class SipServerSbb extends ChildServerSbb implements SriParent {

	private static final String CONTENT_TYPE = "application";
	private static final String CONTENT_SUB_TYPE = "vnd.3gpp.ussd+xml";

	// /////////////////
	// SIP RA Stuff //
	// /////////////////

	private static final ResourceAdaptorTypeID sipRATypeID = new ResourceAdaptorTypeID("JAIN SIP", "javax.sip", "1.2");
	private static final String sipRALink = "SipRA";

	private static final AlertingPattern ALERTING_PATTERN = new AlertingPatternImpl(AlertingLevel.Level1);

	private static ContactHeader contactHeader;
	private static EventsSerializeFactory eventsSerializeFactory = null;

	protected SleeSipProvider sipProvider;

	protected AddressFactory addressFactory;
	protected HeaderFactory headerFactory;
	protected SdpFactory sdpFactory;
	protected MessageFactory messageFactory;
	protected SipActivityContextInterfaceFactory sipActConIntFac;

	protected ParameterFactory sccpParameterFact;

	/**
	 * 
	 */
	public SipServerSbb() {
		super("SipServerSbb");
	}

	@Override
	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);

		try {
			// initialize SIP API
			this.sipActConIntFac = (SipActivityContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(sipRATypeID);
			this.sipProvider = (SleeSipProvider) super.sbbContext.getResourceAdaptorInterface(sipRATypeID, sipRALink);
			// this.mapServiceFactory = this.mapProvider.getMapServiceFactory();

			super.mapAcif = (MAPContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(mapRATypeID);
			super.mapProvider = (MAPProvider) super.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			super.mapParameterFactory = super.mapProvider.getMAPParameterFactory();
            super.ussdStatAggregator = UssdStatAggregator.getInstance();

			addressFactory = sipProvider.getAddressFactory();
			headerFactory = sipProvider.getHeaderFactory();
			sdpFactory = SdpFactory.getInstance();
			messageFactory = sipProvider.getMessageFactory();

			this.ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
            this.sccpParameterFact = new ParameterFactoryImpl();

            this.timerFacility = this.sbbContext.getTimerFacility();
		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	// -------------------------------------------------------------
	// MAP RA events
	// -------------------------------------------------------------

	public void onProcessUnstructuredSSResponse(ProcessUnstructuredSSResponse event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received ProcessUnstructuredSSResponse " + event);
		try {
			SipUssdMessage simMsg = new SipUssdMessage(event.getDataCodingScheme(), event.getUSSDString());
			simMsg.setAnyExt(new AnyExt(MAPMessageType.processUnstructuredSSRequest_Response));

			this.processReceivedMAPEvent((MAPEvent) event, simMsg);
		} catch (Exception e) {
			logger.severe("Error while trying to handle ProcessUnstructuredSSResponse \n" + event, e);
		}

	}

	public void onUnstructuredSSRequest(UnstructuredSSRequest event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSRequest " + event);

		try {
			SipUssdMessage simMsg = new SipUssdMessage(event.getDataCodingScheme(), event.getUSSDString());
			simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSRequest_Request));

			this.processReceivedMAPEvent((MAPEvent) event, simMsg);
		} catch (Exception e) {
			logger.severe("Error while trying to handle UnstructuredSSRequest \n" + event, e);
		}

	}

	public void onUnstructuredSSNotifyRequest(UnstructuredSSNotifyRequest event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSNotifyRequest " + event);

		try {
			SipUssdMessage simMsg = new SipUssdMessage(event.getDataCodingScheme(), event.getUSSDString());
			simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSNotify_Request));

			this.processReceivedMAPEvent((MAPEvent) event, simMsg);
		} catch (Exception e) {
			logger.severe("Error while trying to handle UnstructuredSSNotifyRequest \n" + event, e);
		}

	}

	public void onUnstructuredSSNotifyResponse(UnstructuredSSNotifyResponse event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSNotifyResponse " + event);

        try {
            if (this.getFinalMessageSent()) {
                // we have sent a final NITIFY message and already closed an application part. We just close TCAL dialog now
                event.getMAPDialog().close(false);
                return;
            }
        } catch (Exception e) {
            logger.severe("Error while trying to send a final TC-END\n", e);
        }

        try {
            // TODO Hardcoded
            SipUssdMessage simMsg = new SipUssdMessage("en", "");
            simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSNotify_Response));

            this.processReceivedMAPEvent((MAPEvent) event, simMsg);
        } catch (Exception e) {
            logger.severe("Error while trying to handle UnstructuredSSNotifyResponse \n" + event, e);
        }
	}

	public void onUnstructuredSSResponse(UnstructuredSSResponse event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSResponse " + event);

        super.ussdStatAggregator.updateMessagesRecieved();
        super.ussdStatAggregator.updateMessagesAll();

        ChargeInterface cdrInterface = this.getCDRChargeInterface();
        USSDCDRState state = cdrInterface.getState();

        try {
            if (state.isInitialized()) {
                String ussdString = event.getUSSDString().getString(null);
                if (state.getUssdString() == null) {
                    state.setUssdString(ussdString);
                } else {
                    state.setUssdString(state.getUssdString() + USSDCDRState.USSD_STRING_SEPARATOR + ussdString);
                }
            }
        } catch (Exception e) {
            logger.warning("Exception when setting of UssdString CDR parameter in HttpServerSbb" + e.getMessage(), e);
        }

		try {
			SipUssdMessage simMsg = new SipUssdMessage(event.getDataCodingScheme(), event.getUSSDString());
			simMsg.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSRequest_Response));

			this.processReceivedMAPEvent((MAPEvent) event, simMsg);

			this.setTimer(aci);
		} catch (Exception e) {
			logger.severe("Error while trying to handle UnstructuredSSResponse \n" + event, e);
		}
	}

	public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {

		if (super.logger.isWarningEnabled())
			super.logger.warning("Invoke timeout received:" + evt);

		try {
			// If User is taking too long to respond, lets Abort the Dialog
            MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
            abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);

		} catch (MAPException e1) {
			logger.severe("Error while trying to send Abort MAP Dialog", e1);
		}

		this.abortSipDialog();

        super.ussdStatAggregator.updateMapInvokeTimeouts();

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();

        this.createCDRRecord(RecordStatus.FAILED_INVOKE_TIMEOUT);
	}

	public void onErrorComponent(ErrorComponent event, ActivityContextInterface aci) {
        if (super.logger.isInfoEnabled())
            super.logger.info("Error component received:" + event);

        this.abortSipDialog();		

        switch ((int) (long) (event.getMAPErrorMessage().getErrorCode())) {
        case MAPErrorCode.absentSubscriber:
        case MAPErrorCode.absentSubscriberSM:
            super.ussdStatAggregator.updateMapErrorAbsentSubscribers();
            this.createCDRRecord(RecordStatus.FAILED_ABSENT_SUBSCRIBER);
            break;
        case MAPErrorCode.illegalSubscriber:
            super.ussdStatAggregator.updateMapErrorComponentOther();
            this.createCDRRecord(RecordStatus.FAILED_ILLEGAL_SUBSCRIBER);
            break;
        case MAPErrorCode.ussdBusy:
            super.ussdStatAggregator.updateMapErrorUssdBusy();
            this.createCDRRecord(RecordStatus.FAILED_USSD_BUSY);
            break;
        default:
            super.ussdStatAggregator.updateMapErrorComponentOther();
            this.createCDRRecord(RecordStatus.FAILED_MAP_ERROR_COMPONENT);
            break;
        }

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onRejectComponent(RejectComponent event, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("Reject component received:" + event);

        try {
            MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
            abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);
        } catch (MAPException e1) {
            logger.severe("Error while trying to send Abort MAP Dialog", e1);
        }

        this.abortSipDialog();
        
        this.createCDRRecord(RecordStatus.FAILED_MAP_REJECT_COMPONENT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onDialogAccept(DialogAccept evt, ActivityContextInterface aci) {
		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();

        // CDR
        ChargeInterface cdrInterface = this.getCDRChargeInterface();
        USSDCDRState state = cdrInterface.getState();
        state.setRemoteDialogId(evt.getMAPDialog().getRemoteDialogId());

		if (xmlMAPDialog.getEmptyDialogHandshake() != null && xmlMAPDialog.getEmptyDialogHandshake()) {
			MAPDialogSupplementary mapDialog = (MAPDialogSupplementary) evt.getMAPDialog();
			try {
				this.pushInitialMapPayload(mapDialog);
			} catch (MAPException e) {
				super.logger.severe("Failed to send USSD Request onDialogAccept!", e);
				if (mapDialog != null) {
					mapDialog.release();
				}

                this.abortSipDialog();

				this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);

		        super.ussdStatAggregator.updateDialogsAllFailed();
		        super.ussdStatAggregator.updateDialogsPushFailed();
		        super.ussdStatAggregator.updateDialogsSipFailed();

				return;
			}

		}

        super.ussdStatAggregator.updateDialogsAllEstablished();
        super.ussdStatAggregator.updateDialogsPushEstablished();
        super.ussdStatAggregator.updateDialogsSipEstablished();
	}

	public void onDialogReject(DialogReject evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("Dialog reject received: " + evt);

		this.abortSipDialog();

		this.createCDRRecord(RecordStatus.FAILED_DIALOG_REJECTED);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("User abort received: " + evt);

        this.abortSipDialog();

        this.createCDRRecord(RecordStatus.FAILED_DIALOG_USER_ABORT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("Provider abort received: " + evt);

		this.abortSipDialog();
		
		this.createCDRRecord(RecordStatus.FAILED_PROVIDER_ABORT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onDialogTimeout(DialogTimeout evt, ActivityContextInterface aci) {
		// Assuming InvokeTimeout was called before so no need to take any
		// action here.
		if (super.logger.isWarningEnabled())
			super.logger.warning("Dialog timeout received: " + evt);

		MAPDialog mapDialog = evt.getMAPDialog();
        mapDialog.keepAlive();
        MAPUserAbortChoice mapUserAbortChoice = this.mapParameterFactory.createMAPUserAbortChoice();
        mapUserAbortChoice.setProcedureCancellationReason(ProcedureCancellationReason.callRelease);
        try {
            mapDialog.abort(mapUserAbortChoice);
        } catch (Exception e) {
            super.logger.severe("Exception when sending of : abort in SipServerSbb" + e.toString(), e);
        }

		this.abortSipDialog();

        super.ussdStatAggregator.updateMapDialogTimeouts();
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();

        this.createCDRRecord(RecordStatus.FAILED_DIALOG_TIMEOUT);
	}

    public void onDialogRelease(DialogRelease evt, ActivityContextInterface aci) {
        super.ussdStatAggregator.removeDialogsInProcess();
    }

	// -------------------------------------------------------------
	// SIP RA events
	// -------------------------------------------------------------

	public void onInvite(javax.sip.RequestEvent event, ActivityContextInterface aci, EventContext eventContext) {

		if (logger.isInfoEnabled()) {
			logger.info("Received INVITE \n" + event);
		}

		final ServerTransaction serverTransaction = event.getServerTransaction();
		final Request request = event.getRequest();

		try {
			// send "trying" response
			Response response = messageFactory.createResponse(Response.TRYING, request);
			serverTransaction.sendResponse(response);

			// get local object
			final SbbLocalObject sbbLocalObject = this.sbbContext.getSbbLocalObject();
			// detach from the server tx activity
			aci.detach(sbbLocalObject);

			// Create the dialogs representing the incoming call legs.
			final DialogActivity incomingDialog = (DialogActivity) sipProvider.getNewDialog(event
					.getServerTransaction());

			// Attach to Dialog Activity
			final ActivityContextInterface incomingDialogACI = sipActConIntFac
					.getActivityContextInterface(incomingDialog);
			incomingDialogACI.attach(sbbLocalObject);

			// TODO : process the payload

			byte[] rawContent = request.getRawContent();

			if (logger.isInfoEnabled()) {
				logger.info("Payload " + new String(rawContent));
			}

			if (rawContent == null || rawContent.length <= 0) {
				throw new Exception("Received INVITE but USSD Payload is null \n" + event);

			}

			EventsSerializeFactory factory = this.getEventsSerializeFactory();
			SipUssdMessage sipUssdMessage = factory.deserializeSipUssdMessage(rawContent);

			if (sipUssdMessage == null) {
				throw new Exception(
						"Received INVITE but couldn't deserialize to SipUssdMessage. SipUssdMessage is null \n" + event);
			}

			if (sipUssdMessage.isSuccessMessage()) {
				// Find TO number
				ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
				SipURI sipUri = (SipURI) toHeader.getAddress().getURI();
				String toUser = sipUri.getUser();

				// Set MSISDN
				ISDNAddressString msisdn = new ISDNAddressStringImpl(AddressNature.international_number,
						NumberingPlan.ISDN, toUser);
				this.setMsisdnCMP(msisdn);

				USSDString ussdString = new USSDStringImpl(sipUssdMessage.getUssdString(),
						sipUssdMessage.getCBSDataCodingScheme(), null);

				MAPMessageType messagetype = MAPMessageType.unstructuredSSRequest_Request;

				if (sipUssdMessage.getAnyExt() != null) {
					messagetype = sipUssdMessage.getAnyExt().getMapMessageType();
				}

				MAPMessage mapMessage = null;

                String serviceCode = ussdString.getString(null);

                switch (messagetype) {
                case unstructuredSSRequest_Request:
                    mapMessage = new UnstructuredSSRequestImpl(sipUssdMessage.getCBSDataCodingScheme(), ussdString, ALERTING_PATTERN, msisdn);
                    super.ussdStatAggregator.updateUssdRequestOperations();
                    super.ussdStatAggregator.updateMessagesSent();
                    super.ussdStatAggregator.updateMessagesAll();
                    break;
                case unstructuredSSNotify_Request:
                    mapMessage = new UnstructuredSSNotifyRequestImpl(sipUssdMessage.getCBSDataCodingScheme(), ussdString, ALERTING_PATTERN, msisdn);
                    super.ussdStatAggregator.updateUssdNotifyOperations();
                    super.ussdStatAggregator.updateMessagesSent();
                    super.ussdStatAggregator.updateMessagesAll();
                    break;
                default:
                    throw new Exception("SipUssdMessage MAPMessageType is not recognized \n" + sipUssdMessage);
                }

				XmlMAPDialog xmlMAPDialog = new XmlMAPDialog();
				xmlMAPDialog = new XmlMAPDialog(null, null, null, 0l, 0l, null, null);
				xmlMAPDialog.setTCAPMessageType(MessageType.Begin);

				xmlMAPDialog.addMAPMessage(mapMessage);

				this.setXmlMAPDialog(xmlMAPDialog);

                // CDR
                ChargeInterface cdrInterface = this.getCDRChargeInterface();
                USSDCDRState state = cdrInterface.getState();
                if (!state.isInitialized()) {
                    state.init(null, serviceCode, null, null, msisdn, null, null);
                    state.setDialogStartTime(DateTime.now());
                    state.setUssdType(USSDType.PUSH);
                    cdrInterface.setState(state);

                    // attach, in case impl wants to use more of dialog.
                    SbbLocalObject sbbLO = (SbbLocalObject) cdrInterface;
                    aci.attach(sbbLO);
                }

                super.ussdStatAggregator.addDialogsInProcess();

				getSRI().performSRIQuery(msisdn.getAddress(), xmlMAPDialog);
			} else {
				throw new Exception("Received INVITE but SipUssdMessage is carrying error. SipUssdMessage is null \n"
						+ event);
			}

			// send 180
			response = messageFactory.createResponse(Response.RINGING, event.getRequest());
			serverTransaction.sendResponse(response);

			// send 200 ok
			response = messageFactory.createResponse(Response.OK, event.getRequest());
			response.addHeader(getContactHeader());
			serverTransaction.sendResponse(response);
		} catch (Throwable e) {
			logger.severe("Failed to process incoming INVITE.", e);

			try {
				Response response = messageFactory.createResponse(Response.SERVICE_UNAVAILABLE, request);
				serverTransaction.sendResponse(response);
			} catch (Exception e1) {
				logger.severe("Error while trying to send SERVICE_UNAVAILABLE response to received INVITE", e1);
			}

			super.ussdStatAggregator.updateDialogsAllFailed();
	        super.ussdStatAggregator.updateDialogsPushFailed();
	        super.ussdStatAggregator.updateDialogsSipFailed();
		}
	}

	public void onTransactionTimeout(javax.sip.TimeoutEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received onTransactionTimeout \n" + event);

	}

	public void onDialogAck(javax.sip.RequestEvent event, ActivityContextInterface aci, EventContext eventContext) {
		if (logger.isInfoEnabled()) {
			logger.info("Received Dialog ACK \n" + event);
		}

	}

	public void onDialogBye(javax.sip.RequestEvent event, ActivityContextInterface aci, EventContext eventContext) {
		if (logger.isInfoEnabled()) {
			logger.info("Received Dialog BYE \n" + event);
		}

		this.cancelTimer();

		Request request = event.getRequest();
		try {
			// send back ACK
			ServerTransaction serverTransactionId = event.getServerTransaction();
			Response response = this.messageFactory.createResponse(Response.OK, request);
			serverTransactionId.sendResponse(response);

			// Since its BYE, we End MAP Dialog
			MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();

            // we need to check if MAP Dialog is alive
            if (mapDialogSupplementary == null || mapDialogSupplementary.getState() == MAPDialogState.EXPUNGED) {
                this.abortSipDialog();
                return;
            }
            if (mapDialogSupplementary != null
                    && (mapDialogSupplementary.getState() == MAPDialogState.ACTIVE || mapDialogSupplementary.getState() == MAPDialogState.INITIAL_RECEIVED)) {
                mapDialogSupplementary.close(false);
            }

		} catch (Exception e) {
            logger.severe("Error while processing BYE event", e);

            try {
                MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
                abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
                this.abortMapDialog(abortChoice);
            } catch (MAPException e1) {
                logger.severe("Error while trying to send abort to HTTP App and abort MAPDialog", e1);
            }

            super.ussdStatAggregator.updateDialogsAllFailed();
            super.ussdStatAggregator.updateDialogsPushFailed();
            super.ussdStatAggregator.updateDialogsSipFailed();
            this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_FAILURE);
            return;
		}

        this.createCDRRecord(RecordStatus.SUCCESS);
	}

	public void onDialogInfo(javax.sip.RequestEvent event, ActivityContextInterface aci, EventContext eventContext) {
		if (logger.isInfoEnabled()) {
			logger.info("Received INFO \n" + event);
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
				logger.info("Payload " + new String(rawContent));
			}

			if (rawContent == null || rawContent.length <= 0) {
                throw new Exception("Received INFO but USSD Payload is null \n" + event);
			}

			EventsSerializeFactory factory = this.getEventsSerializeFactory();
			SipUssdMessage sipUssdMessage = factory.deserializeSipUssdMessage(rawContent);

            if (sipUssdMessage == null) {
                throw new Exception("Received INFO but couldn't deserialize to SipUssdMessage. SipUssdMessage is null \n" + event);
			}

			if (sipUssdMessage.isSuccessMessage()) {

				MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();

				CBSDataCodingScheme cbsDataCodingScheme = sipUssdMessage.getCBSDataCodingScheme();
				USSDStringImpl ussdStr = new USSDStringImpl(sipUssdMessage.getUssdString(), cbsDataCodingScheme, null);

				MAPMessageType messagetype = MAPMessageType.unstructuredSSRequest_Request;

				if (sipUssdMessage.getAnyExt() != null) {
					messagetype = sipUssdMessage.getAnyExt().getMapMessageType();
				}

				switch (messagetype) {
                case unstructuredSSRequest_Request:
                    mapDialogSupplementary.addUnstructuredSSRequest(cbsDataCodingScheme, ussdStr, null, null);
                    super.ussdStatAggregator.updateUssdRequestOperations();
                    super.ussdStatAggregator.updateMessagesSent();
                    super.ussdStatAggregator.updateMessagesAll();
                    break;
                case unstructuredSSNotify_Request:
                    mapDialogSupplementary.addUnstructuredSSNotifyRequest(cbsDataCodingScheme, ussdStr, null, null);
                    super.ussdStatAggregator.updateUssdNotifyOperations();
                    super.ussdStatAggregator.updateMessagesSent();
                    super.ussdStatAggregator.updateMessagesAll();
                    break;
                default:
                    throw new Exception("SipUssdMessage MAPMessageType is not recognized \n" + sipUssdMessage);
				}

				mapDialogSupplementary.send();

			} else {
                throw new Exception("Received INFO but SipUssdMessage is carrying error. SipUssdMessage is null \n" + event);
            }

		} catch (Exception e) {
			logger.severe("Error while processing INFO event", e);

            this.abortSipDialog();

			try {
                MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
                abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

				this.abortMapDialog(abortChoice);
			} catch (MAPException e1) {
				logger.severe("Error while trying to send abort to HTTP App and abort MAPDialog", e1);
			}

			super.ussdStatAggregator.updateDialogsAllFailed();
	        super.ussdStatAggregator.updateDialogsPushFailed();
	        super.ussdStatAggregator.updateDialogsSipFailed();

            this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
		}
	}

	public void onSipDialogTimeout(net.java.slee.resource.sip.DialogTimeoutEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received onSipDialogTimeout \n" + event);

		try {
			MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
			abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);
		} catch (Exception e) {
			logger.severe("Error while trying to Abort MAP Dialog", e);
		}

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();

        this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);
	}

	public void onResponseRedirect(javax.sip.ResponseEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received SIP onResponseRedirect \n" + event);
		// TODO What to do?
	}

	public void onResponseClientError(javax.sip.ResponseEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received SIP onResponseClientError \n" + event);

		try {
			MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
			abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);
		} catch (Exception e) {
			logger.severe("Error while trying to Abort MAP Dialog", e);
		}

        this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onResponseSuccess(javax.sip.ResponseEvent event, ActivityContextInterface aci, EventContext eventContext) {
		// Ok all good
	}

	public void onResponseServerError(javax.sip.ResponseEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received SIP onResponseServerError \n" + event);

		try {
			MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
			abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
			abortMapDialog(abortChoice);
		} catch (Exception e) {
			logger.severe("Error while trying to Abort MAP Dialog", e);
		}

        this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	public void onResponseGlobalFailure(javax.sip.ResponseEvent event, ActivityContextInterface aci,
			EventContext eventContext) {
		logger.severe("Received SIP onResponseGlobalFailure \n" + event);

		try {
			MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
			abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
			abortMapDialog(abortChoice);
		} catch (Exception e) {
			logger.severe("Error while trying to Abort MAP Dialog", e);
		}

        this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_ERROR);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.sri.SriParent#onSRIResult(org.mobicents
	 * .ussdgateway.slee.sri.SriSbbLocalObject,
	 * org.restcomm.protocols.ss7.map.api.primitives.IMSI,
	 * org.restcomm.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI)
	 */
	@Override
	public void onSRIResult(SriSbbLocalObject sriSbb, IMSI imsi, LocationInfoWithLMSI locationInfo) {
		if (super.logger.isFineEnabled())
			super.logger.fine("received SRI result");

		this.setLocationInfoCMP(locationInfo);
		this.setImsiCMP(imsi);
		// TODO: org/dest refs may be null?

		// this is GW sccp address
		final SccpAddress origAddress = getUssdGwSccpAddress();

		// this must be provided by client, number of operator of something
		final AddressString origReference = getUssdGwReference();

		// this is VLR/MSC address
		final SccpAddress destAddress = getMSCSccpAddress();

		// Table of 29.002 7.3/2
		final AddressString destReference = getTargetReference();

        // CDR
        ChargeInterface cdrInterface = this.getCDRChargeInterface();
        USSDCDRState state = cdrInterface.getState();
        state.setOrigReference(origReference);
        state.setLocalAddress(origAddress);
        state.setDestReference(destReference);
        state.setRemoteAddress(destAddress);

		MAPDialogSupplementary mapDialog = null;

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();

		try {
			if (super.logger.isFineEnabled()) {
				super.logger
						.fine("Creating dialog for, origAddress '" + origAddress + "', origReference '" + origReference
								+ "', destAddress '" + destAddress + "', destReference '" + destReference + "'");
				super.logger.fine("Map context '" + getUSSDMAPApplicationContext() + "'");
			}

			mapDialog = this.mapProvider.getMAPServiceSupplementary().createNewDialog(getUSSDMAPApplicationContext(),
					origAddress, origReference, destAddress, destReference);
			mapDialog.setReturnMessageOnError(xmlMAPDialog.getReturnMessageOnError());

			ActivityContextInterface mapDialogAci = super.mapAcif.getActivityContextInterface(mapDialog);
			mapDialogAci.attach(super.sbbContext.getSbbLocalObject());

            state.setLocalDialogId(mapDialog.getLocalDialogId());

			if (xmlMAPDialog.getEmptyDialogHandshake() != null && xmlMAPDialog.getEmptyDialogHandshake()) {
				// Lets do handshake only
				mapDialog.send();
			} else {
				pushInitialMapPayload(mapDialog);
			}
		} catch (Exception e) {
			if (logger.isSevereEnabled())
				super.logger.severe("Failed to send USSD notify!", e);
            if (mapDialog != null) {
                mapDialog.release();
            } else {
                super.ussdStatAggregator.removeDialogsInProcess();
            }

            this.abortSipDialog();				

	        super.ussdStatAggregator.updateDialogsAllFailed();
	        super.ussdStatAggregator.updateDialogsPushFailed();
	        super.ussdStatAggregator.updateDialogsSipFailed();

            this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.sri.SriParent#onError(org.mobicents.
	 * ussdgateway.XmlMAPDialog)
	 */
	@Override
	public void onSriError(XmlMAPDialog xmlMAPDialog, RecordStatus recordStatus) {
        this.abortSipDialog();

        this.createCDRRecord(recordStatus);

        super.ussdStatAggregator.removeDialogsInProcess();
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
	}

	// -------------------------------------------------------------
	// SLEE: CMPs
	// -------------------------------------------------------------
	public abstract void setXmlMAPDialog(XmlMAPDialog dialog);

	public abstract XmlMAPDialog getXmlMAPDialog();

	public abstract void setLocationInfoCMP(LocationInfoWithLMSI li);

	public abstract LocationInfoWithLMSI getLocationInfoCMP();

	public abstract void setImsiCMP(IMSI imsi);

	public abstract IMSI getImsiCMP();

	public abstract void setMsisdnCMP(ISDNAddressString msisdn);

	public abstract ISDNAddressString getMsisdnCMP();

	// address CMP stuff

	public abstract void setMaxMAPApplicationContextVersionCMP(MAPApplicationContextVersion v);

	public abstract MAPApplicationContextVersion getMaxMAPApplicationContextVersionCMP();

	public abstract void setMAPApplicationContextCMP(MAPApplicationContext ctx);

	public abstract MAPApplicationContext getMAPApplicationContextCMP();

	public abstract void setUssdGwAddressCMP(AddressString gwAddress);

	public abstract AddressString getUssdGwAddressCMP();

	public abstract void setUssdGwSCCPAddressCMP(SccpAddress gwSccpAddress);

	public abstract SccpAddress getUssdGwSCCPAddressCMP();

	// -------------------------------------------------------------
	// SLEE: Child relation
	// -------------------------------------------------------------

	public SriChild getSRI() throws TransactionRequiredLocalException, SLEEException, CreateException {
		ChildRelationExt childRelationExt = getSriSbbChildRelation();
		if (childRelationExt.size() == 0) {
			return (SriChild) childRelationExt.create();
		} else {
			return (SriChild) childRelationExt.get(ChildRelationExt.DEFAULT_CHILD_NAME);
		}
	}

	public abstract ChildRelationExt getSriSbbChildRelation();

	/**
	 * Private
	 */

	private ContactHeader getContactHeader() throws ParseException {
		if (contactHeader == null) {
			final ListeningPoint listeningPoint = sipProvider.getListeningPoint("udp");
			final javax.sip.address.SipURI sipURI = addressFactory.createSipURI(null, listeningPoint.getIPAddress());
			sipURI.setPort(listeningPoint.getPort());
			sipURI.setTransportParam(listeningPoint.getTransport());
			contactHeader = headerFactory.createContactHeader(addressFactory.createAddress(sipURI));
		}
		return contactHeader;
	}

	private EventsSerializeFactory getEventsSerializeFactory() throws XMLStreamException {
		if (eventsSerializeFactory == null) {
			eventsSerializeFactory = new EventsSerializeFactory();
		}
		return eventsSerializeFactory;
	}

    /**
     * MAP related procedures
     */

	private SccpAddress getUssdGwSccpAddress() {
		SccpAddress address = this.getUssdGwSCCPAddressCMP();
		if (address == null) {
            GlobalTitle gt = sccpParameterFact.createGlobalTitle(ussdPropertiesManagement.getUssdGt(), 0,
                    org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, null, NatureOfAddress.INTERNATIONAL);
            address = sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, ussdPropertiesManagement.getUssdSsn());
			this.setUssdGwSCCPAddressCMP(address);
		}
		return address;
	}

	private AddressString getUssdGwReference() {
        AddressString address = this.getUssdGwAddressCMP();
        if (address == null) {
            address = this.mapParameterFactory.createAddressString(AddressNature.international_number,
                    org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, ussdPropertiesManagement.getUssdGt());
            this.setUssdGwAddressCMP(address);
        }
        return address;
	}

	private SccpAddress getMSCSccpAddress() {

		// TODO : use the networkNodeNumber also to derive if its
		// International / ISDN?
        GlobalTitle gt = sccpParameterFact.createGlobalTitle(getLocationInfoCMP().getNetworkNodeNumber().getAddress(), 0,
                org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, null, NatureOfAddress.INTERNATIONAL);
        return sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, ussdPropertiesManagement.getMscSsn());
	}

	private AddressString getTargetReference() {
		return this.mapParameterFactory.createAddressString(AddressNature.international_number,
				org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.land_mobile, getImsiCMP().getData());
	}

	private MAPApplicationContext getUSSDMAPApplicationContext() throws MAPException {
		MAPApplicationContext ctx = this.getMAPApplicationContextCMP();
		if (ctx == null) {
            ctx = MAPApplicationContext.getInstance(MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);
			this.setMAPApplicationContextCMP(ctx);
		}
		return ctx;
	}

	/**
	 * Sending first MAP request payload to SS7 network
	 *
	 * @param dialog
	 * @throws MAPException
	 */
	private void pushInitialMapPayload(MAPDialogSupplementary dialog) throws MAPException {
		if (super.logger.isFinestEnabled())
			super.logger.finest("Sending of initial Map payload.");

		XmlMAPDialog xmlMapDialog = this.getXmlMAPDialog();

		this.processXmlMAPDialog(xmlMapDialog, dialog);
        dialog.send();
	}

    private void abortMapDialog(MAPUserAbortChoice abortChoice) throws MAPException {
        MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();
        if (mapDialogSupplementary != null) {
            if (mapDialogSupplementary.getState() == MAPDialogState.ACTIVE || mapDialogSupplementary.getState() == MAPDialogState.INITIAL_RECEIVED) {
                mapDialogSupplementary.abort(abortChoice);
            } else {
                mapDialogSupplementary.release();
            }
        }
    }

    /**
     * SIP related procedures
     */
    private void processReceivedMAPEvent(MAPEvent event, SipUssdMessage simMsg) throws Exception {
        MessageType messageType = event.getMAPDialog().getTCAPMessageType();

        if (messageType == MessageType.Continue) {
            this.sendSipInfo(simMsg);

        } else {
            this.sendSipBye(simMsg);
        }
    }
    
    protected void abortSipDialog() {
        SipUssdMessage simMsg = new SipUssdMessage(SipUssdErrorCode.errorUnspecified);
        this.sendSipBye(simMsg);
    }

	private void sendSipInfo(SipUssdMessage simMsg) throws Exception {
        DialogActivity sipDialogActivity = this.getSipDialog();
        if (sipDialogActivity == null) {
            // Most probably the Dialog between the GW and application died!
            throw new Exception("No SIP DialogActivity when sending SIP Info");
        }

        byte[] data = this.getEventsSerializeFactory().serializeSipUssdMessage(simMsg);
		String content = new String(data);
		Request infoRequest = sipDialogActivity.createRequest(Request.INFO);

		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(CONTENT_TYPE, CONTENT_SUB_TYPE);
		infoRequest.setContent(content, contentTypeHeader);

		ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
		infoRequest.setContentLength(contentLengthHeader);

		ClientTransaction infoCt = sipProvider.getNewClientTransaction(infoRequest);

		// TODO : Should it attach to Client Tx?
		// Attach this SBB to Client Tx Activity
		ActivityContextInterface infoRequestAci = this.sipActConIntFac.getActivityContextInterface(infoCt);
		infoRequestAci.attach(this.sbbContext.getSbbLocalObject());

		sipDialogActivity.sendRequest(infoCt);
	}

	private void sendSipBye(SipUssdMessage simMsg) {
		Dialog sipDialog = this.getSipDialog();

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

				ClientTransaction ct = this.sipProvider.getNewClientTransaction(byeRequest);
				ActivityContextInterface calleeAci = this.sipActConIntFac.getActivityContextInterface(ct);
				calleeAci.attach(this.sbbContext.getSbbLocalObject());
				sipDialog.sendRequest(ct);
			} catch (Exception e) {
				this.logger.severe("Error while sending BYE Request ", e);
			}
		} else {
			logger.warning("Trying to send BYE for Dialog that is not CONFIRMED. Will delete SIP Dialog \n" + sipDialog);
			this.terminateSipProtocolConnection(sipDialog);
		}
	}

	private DialogActivity getSipDialog() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			if (aci.getActivity() instanceof DialogActivity) {
				return (DialogActivity) aci.getActivity();
			}
		}

		return null;
	}

	protected void terminateSipProtocolConnection(Dialog sipDialog) {
		if (sipDialog == null) {
			// Most probably the Dialog between the GW and application died!
			return;
		}
		sipDialog.delete();
	}

    protected void terminateProtocolConnection() {
        Dialog sipDialog = this.getSipDialog();

        if (sipDialog == null) {
            // Most probably the Dialog between the GW and application died!
            return;
        }
        sipDialog.delete();
    }

    protected void updateDialogFailureStat() {
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsSipFailed();
    }

    protected boolean isSip() {
        return true;
    }

}

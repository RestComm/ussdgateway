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

package org.mobicents.ussdgateway.slee.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.EventContext;
import javax.slee.SLEEException;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.util.FastList;
import javolution.xml.stream.XMLStreamException;
import net.java.slee.resource.http.HttpServletRaActivityContextInterfaceFactory;
import net.java.slee.resource.http.HttpServletRaSbbInterface;
import net.java.slee.resource.http.HttpSessionActivity;
import net.java.slee.resource.http.events.HttpServletRequestEvent;

import org.joda.time.DateTime;
import org.restcomm.protocols.ss7.indicator.NatureOfAddress;
import org.restcomm.protocols.ss7.indicator.NumberingPlan;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.dialog.MAPDialogState;
import org.restcomm.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.restcomm.protocols.ss7.map.api.dialog.ProcedureCancellationReason;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorCode;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
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
import org.mobicents.ussdgateway.EventsSerializeFactory;
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
 * HTTP Server SBB. This SBB serves as entry point for network initiated Push.
 * 
 * @author baranowb
 * @author sergey vetyutnev
 * 
 */
public abstract class HttpServerSbb extends ChildServerSbb implements SriParent {

	// -------------------------------------------------------------
	// statics
	// -------------------------------------------------------------
	private static final int EVENT_SUSPEND_TIMEOUT = 1000 * 60 * 3;
	private static final String CONTENT_MAIN_TYPE = "text";
	private static final String CONTENT_SUB_TYPE = "xml";
	private static final String CONTENT_TYPE = CONTENT_MAIN_TYPE + "/" + CONTENT_SUB_TYPE;
	// -------------------------------------------------------------
	// SS7 stuff:
	// -------------------------------------------------------------

	// -------------------------------------------------------------
	// USSD GW stuff
	// -------------------------------------------------------------
	private EventsSerializeFactory eventsSerializeFactory = null;

	protected ParameterFactory sccpParameterFact;

	public HttpServerSbb() {
		super("HttpServerSbb");
		// TODO Auto-generated constructor stub
	}

	// -------------------------------------------------------------
	// HTTP Server RA events
	// -------------------------------------------------------------
	public void onPost(HttpServletRequestEvent event, ActivityContextInterface aci, EventContext eventContext) {
        if (super.logger.isFineEnabled())
			super.logger.fine("Received POST");

		if (this.getEventContextCMP() != null) {
			// TODO: send error
			if (super.logger.isSevereEnabled())
				super.logger.severe("Detected previous event context: " + getEventContextCMP());
			return;
		}

		this.cancelTimer();

		boolean success = false;
		try {
			eventContext.suspendDelivery(EVENT_SUSPEND_TIMEOUT);
			this.setEventContextCMP(eventContext);

			// this is new dialog, we need to send SRI
			final XmlMAPDialog xmlMAPDialog = deserializeDialog(event);
			this.setXmlMAPDialog(xmlMAPDialog);

			final FastList<MAPMessage> mapMessages = xmlMAPDialog.getMAPMessages();

			ISDNAddressString msisdn = null;

			String serviceCode = null;

			// This is initial request, if its not NTFY, we need session
			if (mapMessages != null) {
				for (FastList.Node<MAPMessage> n = mapMessages.head(), end = mapMessages.tail(); (n = n.getNext()) != end;) {
					final MAPMessage rawMessage = n.getValue();
					final MAPMessageType type = rawMessage.getMessageType();

					if (logger.isFinestEnabled())
						logger.finest("Dialog message type: " + type);

					switch (type) {
					case unstructuredSSRequest_Request:
						final UnstructuredSSRequest ussRequest = (UnstructuredSSRequest) rawMessage;
						msisdn = ussRequest.getMSISDNAddressString();
						serviceCode = ussRequest.getUSSDString().getString(null);
	                    super.ussdStatAggregator.updateUssdRequestOperations();
                        super.ussdStatAggregator.updateMessagesSent();
                        super.ussdStatAggregator.updateMessagesAll();
						break;
					case unstructuredSSNotify_Request:
						final UnstructuredSSNotifyRequest ntfyReq = (UnstructuredSSNotifyRequest) rawMessage;
						msisdn = ntfyReq.getMSISDNAddressString();
						serviceCode = ntfyReq.getUSSDString().getString(null);
	                    super.ussdStatAggregator.updateUssdNotifyOperations();
                        super.ussdStatAggregator.updateMessagesSent();
                        super.ussdStatAggregator.updateMessagesAll();
						break;
					case processUnstructuredSSRequest_Request:
						final ProcessUnstructuredSSRequest processUnstrSSReq = (ProcessUnstructuredSSRequest) rawMessage;
						msisdn = processUnstrSSReq.getMSISDNAddressString();
						serviceCode = processUnstrSSReq.getUSSDString().getString(null);
	                    super.ussdStatAggregator.updateProcessUssdRequestOperations();
						break;
					}
				}// for
			}
            if (msisdn == null) {
                throw new Exception("MSISDN in a received initial HTTP PUSH request is null");
            }

			this.setMsisdnCMP(msisdn);

			if (logger.isFinestEnabled())
				logger.finest("Creating session activity.");

			HttpSession httpSession = event.getRequest().getSession(true);

			HttpSessionActivity httpSessionActivity = super.httpServletProvider.getHttpSessionActivity(httpSession);
			ActivityContextInterface httpSessionActivityContextInterface = httpServletRaActivityContextInterfaceFactory
					.getActivityContextInterface(httpSessionActivity);
			httpSessionActivityContextInterface.attach(super.sbbContext.getSbbLocalObject());

//			if (!xmlMAPDialog.isRedirectRequest()) {

            // Query HLR
            if (logger.isFinestEnabled())
                logger.finest("Triggering SRI routine.");

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

            getSRI().performSRIQuery(msisdn.getAddress(), xmlMAPDialog);

            super.ussdStatAggregator.addDialogsInProcess();

			success = true;

		} catch (Exception e) {
			super.logger.severe("Error while processing received HTTP POST request", e);
		} finally {
			if (!success) {
				try {
					MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
					abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

					XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
					if (xmlMAPDialog != null) {
						xmlMAPDialog.reset();
						xmlMAPDialog.abort(abortChoice);
						xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

						this.abortHttpDialog(xmlMAPDialog);
					} else {
			            EventContext httpEventContext = this.resumeHttpEventContext();
			            if (httpEventContext != null) {
			                HttpServletRequestEvent httpRequest = (HttpServletRequestEvent) httpEventContext.getEvent();
			                HttpServletResponse response = httpRequest.getResponse();
			                response.setStatus(HttpServletResponse.SC_OK);
			            }
					}

					MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();
					if (mapDialogSupplementary != null) {
						mapDialogSupplementary.abort(abortChoice);
					}
				} catch (MAPException e) {
					logger.severe("Error while trying to abort MAPDialog", e);
				}

	            super.ussdStatAggregator.updateDialogsAllFailed();
	            super.ussdStatAggregator.updateDialogsPushFailed();
	            super.ussdStatAggregator.updateDialogsHttpFailed();
			}
		}
	}

	public void onSessionPost(HttpServletRequestEvent event, ActivityContextInterface aci, EventContext eventContext) {
        if (super.logger.isFineEnabled())
			super.logger.fine("Received insession POST");

		if (this.getEventContextCMP() != null) {
			if (super.logger.isSevereEnabled())
				super.logger.severe("Detected previous event context: " + getEventContextCMP());
			// TODO: send error
			return;
		}

		this.cancelTimer();

        eventContext.suspendDelivery(EVENT_SUSPEND_TIMEOUT);
		this.setEventContextCMP(eventContext);
		// if this is new dialog, we need to send SRI
		boolean success = false;
		try {
            XmlMAPDialog xmlMAPDialog = deserializeDialog(event);

            FastList<MAPMessage> mapMessages = xmlMAPDialog.getMAPMessages();
            if (mapMessages != null) {
                for (FastList.Node<MAPMessage> n = mapMessages.head(), end = mapMessages.tail(); (n = n.getNext()) != end;) {
                    final MAPMessage rawMessage = n.getValue();
                    final MAPMessageType messagetype = rawMessage.getMessageType();

                    switch (messagetype) {
                    case unstructuredSSRequest_Request:
                        super.ussdStatAggregator.updateUssdRequestOperations();
                        super.ussdStatAggregator.updateMessagesSent();
                        super.ussdStatAggregator.updateMessagesAll();
                        break;
                    case unstructuredSSNotify_Request:
                        super.ussdStatAggregator.updateUssdNotifyOperations();
                        super.ussdStatAggregator.updateMessagesSent();
                        super.ussdStatAggregator.updateMessagesAll();
                        break;
                    }
                }
            }

            this.setXmlMAPDialog(xmlMAPDialog);
            pushToDevice();
            success = true;

            Boolean prearrangedEnd = xmlMAPDialog.getPrearrangedEnd();
            if (prearrangedEnd != null) {
                this.createCDRRecord(RecordStatus.SUCCESS);
            }
            MAPUserAbortChoice capUserAbortReason = xmlMAPDialog.getMAPUserAbortChoice();
            if (capUserAbortReason != null) {
                this.createCDRRecord(RecordStatus.ABORT_APP);
            }

            // TODO: exceptions
		} catch (IOException e) {
			if (super.logger.isSevereEnabled())
				super.logger.severe("", e);
		} catch (XMLStreamException e) {
			if (super.logger.isSevereEnabled())
				super.logger.severe("", e);
		} catch (MAPException e) {
			if (super.logger.isSevereEnabled())
				super.logger.severe("", e);
		} finally {
			if (!success) {
				try {
					XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
					xmlMAPDialog.reset();

					MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
					abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

					xmlMAPDialog.abort(abortChoice);
					xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

					this.abortHttpDialog(xmlMAPDialog);

					MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();
					if (mapDialogSupplementary != null) {
						mapDialogSupplementary.abort(abortChoice);
					}
				} catch (MAPException e) {
					logger.severe("Error while trying to send abort to HTTP App and abort MAPDialog", e);
				}

	            super.ussdStatAggregator.updateDialogsAllFailed();
	            super.ussdStatAggregator.updateDialogsPushFailed();
	            super.ussdStatAggregator.updateDialogsHttpFailed();

                this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
			}// if
		}
	}

	// -------------------------------------------------------------
	// HTTP Server RA events - forbidden
	// -------------------------------------------------------------
	public void onHead(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.HEAD");
	}

	public void onGet(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.GET");
	}

	public void onPut(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.PUT");
	}

	public void onDelete(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.DELETE");
	}

	public void onOptions(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.OPTIONS");
	}

	public void onTrace(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "request.TRACE");
	}

	public void onSeassionHead(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.HEAD");
	}

	public void onSessionGet(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.GET");
	}

	public void onSessionPut(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.PUT");
	}

	public void onSessionDelete(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.DELETE");
	}

	public void onSessionOptions(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.OPTIONS");
	}

	public void onSessionTrace(HttpServletRequestEvent event, ActivityContextInterface aci) {

		respondOnBadHTTPMethod(event, "session.TRACE");
	}

	// -------------------------------------------------------------
	// MAP RA events
	// -------------------------------------------------------------

	public void onProcessUnstructuredSSResponse(ProcessUnstructuredSSResponse event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received ProcessUnstructuredSSResponse " + event);

		this.processReceivedMAPEvent((MAPEvent) event);

	}

	public void onUnstructuredSSRequest(UnstructuredSSRequest event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSRequest " + event);

		this.processReceivedMAPEvent((MAPEvent) event);

	}

	public void onUnstructuredSSNotifyRequest(UnstructuredSSNotifyRequest event, ActivityContextInterface aci,
			EventContext eventContext) {
		if (logger.isFineEnabled())
			logger.fine("Received UnstructuredSSNotifyRequest " + event);

		this.processReceivedMAPEvent((MAPEvent) event);

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

		this.processReceivedMAPEvent((MAPEvent) event);
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

		this.processReceivedMAPEvent((MAPEvent) event);

		this.setTimer(aci);
	}

	public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {

		if (super.logger.isWarningEnabled())
			super.logger.warning("Invoke timeout received:" + evt);

        try {
            MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
            abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);
        } catch (MAPException e1) {
            logger.severe("Error while trying to send Abort MAP Dialog", e1);
        }

        try {
            XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
            xmlMAPDialog.reset();
            xmlMAPDialog.setInvokeTimedOut(evt.getInvokeId());
            xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

            this.abortHttpDialog(xmlMAPDialog);
        } catch (Exception e) {
            logger.severe("Error while trying to send DialogTimeout to App", e);
        }

        super.ussdStatAggregator.updateMapInvokeTimeouts();

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();

        this.createCDRRecord(RecordStatus.FAILED_INVOKE_TIMEOUT);
	}

	public void onErrorComponent(ErrorComponent event, ActivityContextInterface aci) {
		if (super.logger.isInfoEnabled())
			super.logger.info("Error component received:" + event);

        try {
            MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
            abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);
        } catch (MAPException e1) {
            logger.severe("Error while trying to send Abort MAP Dialog", e1);
        }

        try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.sendErrorComponent(event.getInvokeId(), event.getMAPErrorMessage());
			xmlMAPDialog.setTCAPMessageType(event.getMAPDialog().getTCAPMessageType());

			this.abortHttpDialog(xmlMAPDialog);
		} catch (MAPException e) {
			logger.severe("Error while trying to send ErrorComponent to HTTP App", e);
		}

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
        super.ussdStatAggregator.updateDialogsHttpFailed();
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


        try {
            XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
            xmlMAPDialog.reset();
            xmlMAPDialog.sendRejectComponent(event.getInvokeId(), event.getProblem());
            xmlMAPDialog.setTCAPMessageType(event.getMAPDialog().getTCAPMessageType());

            this.abortHttpDialog(xmlMAPDialog);
        } catch (MAPException e) {
            logger.severe("Error while trying to send ErrorComponent to HTTP App", e);
        }
        
        this.createCDRRecord(RecordStatus.FAILED_MAP_REJECT_COMPONENT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
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
				this.pushToDevice();
			} catch (MAPException e) {
				super.logger.severe("Failed to send USSD Request onDialogAccept!", e);
				if (mapDialog != null) {
					mapDialog.release();
				}

				try {

					MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
					abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

					xmlMAPDialog = this.getXmlMAPDialog();
					xmlMAPDialog.reset();
					xmlMAPDialog.abort(abortChoice);
					xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

					this.abortHttpDialog(xmlMAPDialog);

					this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);

	                super.ussdStatAggregator.updateDialogsAllFailed();
	                super.ussdStatAggregator.updateDialogsPushFailed();
	                super.ussdStatAggregator.updateDialogsHttpFailed();

	                return;
				} catch (Exception e1) {
					logger.severe("Error while trying to send abort to HTTP App", e1);
				}
			}

		}

        super.ussdStatAggregator.updateDialogsAllEstablished();
        super.ussdStatAggregator.updateDialogsPushEstablished();
        super.ussdStatAggregator.updateDialogsHttpEstablished();
	}

	public void onDialogReject(DialogReject evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("Dialog reject received: " + evt);

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setMapRefuseReason(evt.getRefuseReason());
		xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());
		this.abortHttpDialog(xmlMAPDialog);

		this.createCDRRecord(RecordStatus.FAILED_DIALOG_REJECTED);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("User abort received: " + evt);
		try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.abort(evt.getUserReason());
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());
			this.abortHttpDialog(xmlMAPDialog);
		} catch (MAPException e) {
			logger.severe("Error wile trying to send back MAPUserAbortChoice to HTTP App", e);
		}

		this.createCDRRecord(RecordStatus.FAILED_DIALOG_USER_ABORT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("Provider abort received: " + evt);

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setMapAbortProviderReason(evt.getAbortProviderReason());
		xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

		this.abortHttpDialog(xmlMAPDialog);

		this.createCDRRecord(RecordStatus.FAILED_PROVIDER_ABORT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
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
            super.logger.severe("Exception when sending of : abort in HttpServerSbb" + e.toString(), e);
        }

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setDialogTimedOut(true);
		if (evt.getMAPDialog().getTCAPMessageType() != null) {
			// TODO : MAP RA is setting MessageType to null if Dialog is sent
			// and remote side never responded, fix this in RA
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());
		}

        try {
            this.abortHttpDialog(xmlMAPDialog);
        } catch (Exception e) {
            super.logger.severe("Exception when sending of : abortHttpDialog in HttpServerSbb" + e.toString(), e);
        }

        super.ussdStatAggregator.updateMapDialogTimeouts();
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();

        this.createCDRRecord(RecordStatus.FAILED_DIALOG_TIMEOUT);
	}

    public void onDialogRelease(DialogRelease evt, ActivityContextInterface aci) {
        super.ussdStatAggregator.removeDialogsInProcess();
    }

	// -------------------------------------------------------------
	// SLEE: SriParent
	// -------------------------------------------------------------
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
		// NOTE: this may require ATI, if MSC and VLR are not integrated, as SRI
		// returns MSC address
		// (despite joyful diagrams on the web :) )
		// ATI(IMSI)-> HLR address

		if (super.logger.isFineEnabled())
			super.logger.fine("received SRI result");

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		this.setLocationInfoCMP(locationInfo);
		this.setImsiCMP(imsi);
		// TODO: org/dest refs may be null?

		// this is GW sccp address
		final SccpAddress origAddress = getUssdGwSccpAddress(xmlMAPDialog.getNetworkId());

		// this must be provided by client, number of operator of something
		final AddressString origReference = getUssdGwReference(xmlMAPDialog.getNetworkId());

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
			mapDialog.setNetworkId(xmlMAPDialog.getNetworkId());
			ActivityContextInterface mapDialogAci = super.mapAcif.getActivityContextInterface(mapDialog);
			mapDialogAci.attach(super.sbbContext.getSbbLocalObject());

			state.setLocalDialogId(mapDialog.getLocalDialogId());

			if (xmlMAPDialog.getEmptyDialogHandshake() != null && xmlMAPDialog.getEmptyDialogHandshake()) {
				// Lets do handshake only
				mapDialog.send();
			} else {
				pushToDevice(mapDialog);
			}
		} catch (Exception e) {
			if (logger.isSevereEnabled())
				super.logger.severe("Failed to send USSD notify!", e);
			if (mapDialog != null) {
				mapDialog.release();
            } else {
                super.ussdStatAggregator.removeDialogsInProcess();
			}

			try {

				MAPUserAbortChoiceImpl abortChoice = new MAPUserAbortChoiceImpl();
				abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

				xmlMAPDialog.reset();
				xmlMAPDialog.abort(abortChoice);
				xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

				this.abortHttpDialog(xmlMAPDialog);
			} catch (Exception e1) {
				logger.severe("Error while trying to send abort to HTTP App", e1);
			}

			this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);

            super.ussdStatAggregator.updateDialogsAllFailed();
            super.ussdStatAggregator.updateDialogsPushFailed();
            super.ussdStatAggregator.updateDialogsHttpFailed();
		}
	}

	@Override
	public void onSriError(XmlMAPDialog xmlMAPDialog, RecordStatus recordStatus) {
		this.abortHttpDialog(xmlMAPDialog);

		this.createCDRRecord(recordStatus);

        super.ussdStatAggregator.removeDialogsInProcess();
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
	}

	// -------------------------------------------------------------
	// SLEE: callbacks
	// -------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.USSDBaseSbb#setSbbContext(javax.slee.
	 * SbbContext)
	 */
	@Override
	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);
		super.logger = sbbContext.getTracer("HTTP-Server-" + getClass().getName());
		try {
			super.mapAcif = (MAPContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(mapRATypeID);
			super.mapProvider = (MAPProvider) super.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			super.mapParameterFactory = super.mapProvider.getMAPParameterFactory();
            super.ussdStatAggregator = UssdStatAggregator.getInstance();

            if (httpServerRATypeID != null)
                httpServerRATypeID = new ResourceAdaptorTypeID("HttpServletResourceAdaptorType", "org.restcomm", "1.0");
            try {
                super.httpServletRaActivityContextInterfaceFactory = (HttpServletRaActivityContextInterfaceFactory) super.sbbContext
                        .getActivityContextInterfaceFactory(httpServerRATypeID);
            } catch (Exception e) {
                httpServerRATypeID = new ResourceAdaptorTypeID("HttpServletResourceAdaptorType", "org.mobicents", "1.0");
                logger.info("Trying to use HttpServletResourceAdaptorType - org.mobicents");
                super.httpServletRaActivityContextInterfaceFactory = (HttpServletRaActivityContextInterfaceFactory) super.sbbContext
                        .getActivityContextInterfaceFactory(httpServerRATypeID);
            }
			super.httpServletProvider = (HttpServletRaSbbInterface) super.sbbContext.getResourceAdaptorInterface(
					httpServerRATypeID, httpServerRaLink);
			this.ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
			this.sccpParameterFact = new ParameterFactoryImpl();

			this.timerFacility = this.sbbContext.getTimerFacility();
		} catch (Exception ne) {
			if (logger.isSevereEnabled())
				super.logger.severe("Could not set SBB context:", ne);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.USSDBaseSbb#unsetSbbContext()
	 */
	@Override
	public void unsetSbbContext() {
		super.unsetSbbContext();
		this.ussdPropertiesManagement = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.USSDBaseSbb#sbbCreate()
	 */
	@Override
	public void sbbCreate() throws CreateException {
		// TODO Auto-generated method stub
		super.sbbCreate();
		this.setMaxMAPApplicationContextVersionCMP(MAPApplicationContextVersion
				.getInstance(this.ussdPropertiesManagement.getMaxMapVersion()));
	}

	// -------------------------------------------------------------
	// SLEE: CMPs
	// -------------------------------------------------------------
	public abstract void setXmlMAPDialog(XmlMAPDialog dialog);

	public abstract XmlMAPDialog getXmlMAPDialog();

	public abstract void setEventContextCMP(EventContext eventContext);

	public abstract EventContext getEventContextCMP();

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

	// -------------------------------------------------------------
	// private stuff
	// -------------------------------------------------------------

    private EventsSerializeFactory getEventsSerializeFactory() throws XMLStreamException {
        if (this.eventsSerializeFactory == null) {
            this.eventsSerializeFactory = new EventsSerializeFactory();
        }
        return this.eventsSerializeFactory;
    }

    /**
     * @param event
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    private XmlMAPDialog deserializeDialog(HttpServletRequestEvent event) throws IOException, XMLStreamException {

        HttpServletRequest request = event.getRequest();
        if (!request.getContentType().equals(CONTENT_TYPE)) {
            throw new IOException("Wrong content type '" + request.getContentType() + "', should be '" + CONTENT_TYPE
                    + "'");
        }

        request.setCharacterEncoding("UTF-8");
        BufferedReader is = request.getReader();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        String line;
        Charset charset = Charset.forName("UTF-8");
        while ((line = is.readLine()) != null) {

            bos.write(line.getBytes(charset));
        }
        if (super.logger.isFinestEnabled()) {
            super.logger.info("Deserializing:" + request.getContentType() + ":" + request.getCharacterEncoding());
            super.logger.info(new String(bos.toByteArray()));
        }

        XmlMAPDialog d = getEventsSerializeFactory().deserialize(bos.toByteArray());

        return d;
    }

	/*
	 * HTTP procedures
	 */

    private void abortHttpDialog(XmlMAPDialog xmlMAPDialog) {
        this.setXmlMAPDialog(xmlMAPDialog);

        EventContext httpEventContext = getEventContextCMP();
        if (httpEventContext == null) {
            if (super.logger.isWarningEnabled()) {
                super.logger.warning("When HTTP Dialog aborting no pending HTTP request is found");
                return;
            }
        }

        sendHttpResponse();
        this.endHttpSessionActivity();
    }

	/**
	 * @param event
	 * @param string
	 */
	private void respondOnBadHTTPMethod(HttpServletRequestEvent event, String method) {
		// 405, method not allowed
		if (super.logger.isWarningEnabled())
			super.logger.warning("Received wrong HTTP method  '" + method + "'.");

		HttpServletResponse response = event.getResponse();
		response.setStatus(405);
		response.setContentType("text/plain");
		PrintWriter w = null;
		try {
			w = response.getWriter();
			w.print("HTTP." + method + " is not supported");
			w.flush();
			response.flushBuffer();
		} catch (IOException e) {
			super.logger.severe("", e);
		}
	}

    /**
     * 
     */
    private EventContext resumeHttpEventContext() {
        EventContext httpEventContext = getEventContextCMP();

        if (httpEventContext == null) {
            logger.severe("No HTTP event context, can not resume ");
            return null;
        }

        httpEventContext.resumeDelivery();
        return httpEventContext;
    }

    private void sendHttpResponse() {
        try {
            if (super.logger.isFineEnabled())
                super.logger.fine("About to send HTTP response.");

            XmlMAPDialog dialog = getXmlMAPDialog();
            byte[] data = getEventsSerializeFactory().serialize(dialog);

            if (super.logger.isFineEnabled()) {
                super.logger.fine("Sending HTTP Response Payload = \n" + new String(data));
            }

            EventContext httpEventContext = this.resumeHttpEventContext();

            if (httpEventContext == null) {
                // TODO: terminate dialog?
                logger.severe("No HTTP event context, can not deliver response for MapXmlDialog: " + dialog);
                return;
            }

            HttpServletRequestEvent httpRequest = (HttpServletRequestEvent) httpEventContext.getEvent();
            HttpServletResponse response = httpRequest.getResponse();
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                response.getOutputStream().write(data);
                response.getOutputStream().flush();
            } catch (NullPointerException npe) {
                super.logger
                        .warning(
                                "Probably HTTPResponse already sent by HTTP-Servlet-RA. Increase HTTP_REQUEST_TIMEOUT in deploy-config.xml of RA to be greater than TCAP Dialog timeout",
                                npe);
            }

        } catch (XMLStreamException xmle) {
            super.logger.severe("Failed to serialize dialog", xmle);
        } catch (IOException e) {
            super.logger.severe("Failed to send answer!", e);
        }

    }

    private void endHttpSessionActivity() {
        HttpSessionActivity httpSessionActivity = this.getHttpSessionActivity();
        if (httpSessionActivity != null) {
            httpSessionActivity.endActivity();
        }
    }

    private HttpSessionActivity getHttpSessionActivity() {
        ActivityContextInterface[] acis = this.sbbContext.getActivities();
        for (ActivityContextInterface aci : acis) {
            Object activity = aci.getActivity();
            if (activity instanceof HttpSessionActivity) {
                return (HttpSessionActivity) activity;
            }
        }
        return null;
    }

    private void pushToDevice() throws MAPException {
        MAPDialogSupplementary dialog = this.getMAPDialog();
        if (dialog == null) {
            throw new MAPException("Underlying MAP Dialog is null");
        }
        this.pushToDevice(dialog);
    }

    /**
     * @throws MAPException
     * 
     */
    private void pushToDevice(MAPDialogSupplementary dialog) throws MAPException {
        if (super.logger.isFinestEnabled())
            super.logger.finest("Pushingng to device.");

        XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();

        MAPUserAbortChoice capUserAbortReason = xmlMAPDialog.getMAPUserAbortChoice();
        if (capUserAbortReason != null) {
            dialog.abort(capUserAbortReason);
            EventContext httpEventContext = this.resumeHttpEventContext();
            if (httpEventContext != null) {
                HttpServletRequestEvent httpRequest = (HttpServletRequestEvent) httpEventContext.getEvent();
                HttpServletResponse response = httpRequest.getResponse();
                response.setStatus(HttpServletResponse.SC_OK);
            }
            this.endHttpSessionActivity();
            return;
        }

        Boolean prearrangedEnd = xmlMAPDialog.getPrearrangedEnd();

        this.processXmlMAPDialog(xmlMAPDialog, dialog);

        if (prearrangedEnd != null) {
            dialog.close(prearrangedEnd);

            // If prearrangedEnd is not null means, no more MAP messages are
            // expected. Lets clear HTTP resources
            EventContext httpEventContext = this.resumeHttpEventContext();
            if (httpEventContext != null) {
                HttpServletRequestEvent httpRequest = (HttpServletRequestEvent) httpEventContext.getEvent();
                HttpServletResponse response = httpRequest.getResponse();
                response.setStatus(HttpServletResponse.SC_OK);
            }
            this.endHttpSessionActivity();
        } else {
            dialog.send();
        }
    }

    private void processReceivedMAPEvent(MAPEvent event) {
        XmlMAPDialog dialog = this.getXmlMAPDialog();
        dialog.reset();
        dialog.addMAPMessage(event.getWrappedEvent());

        MessageType messageType = event.getMAPDialog().getTCAPMessageType();
        dialog.setTCAPMessageType(messageType);
        setXmlMAPDialog(dialog);
        sendHttpResponse();

        if (messageType == MessageType.End) {
            // If MAP Dialog is end, lets kill HTTP Session Activity too
            this.endHttpSessionActivity();
        }
    }


	/*
	 * MAP procedures
	 */

	private MAPApplicationContext getUSSDMAPApplicationContext() throws MAPException {
		MAPApplicationContext ctx = this.getMAPApplicationContextCMP();
		if (ctx == null) {
			ctx = MAPApplicationContext.getInstance(MAPApplicationContextName.networkUnstructuredSsContext,
					MAPApplicationContextVersion.version2);
			if (ctx == null) {
				throw new MAPException("Not suitable context: "
						+ MAPApplicationContextName.networkUnstructuredSsContext + " for "
						+ this.getMaxMAPApplicationContextVersionCMP());
			}
			this.setMAPApplicationContextCMP(ctx);
		}
		return ctx;
	}

	private AddressString getUssdGwReference(int networkId) {
		AddressString address = this.getUssdGwAddressCMP();
		if (address == null) {
			address = this.mapParameterFactory.createAddressString(AddressNature.international_number,
					// TODO: getUssdGWGt seems wrrong here?
					org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
					ussdPropertiesManagement.getUssdGt(networkId));
			this.setUssdGwAddressCMP(address);
		}
		return address;
	}

	private AddressString getTargetReference() {
		return this.mapParameterFactory.createAddressString(AddressNature.international_number,
				org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.land_mobile, getImsiCMP().getData());
	}

	private SccpAddress getUssdGwSccpAddress(int networkId) {
		SccpAddress address = this.getUssdGwSCCPAddressCMP();
		if (address == null) {
			GlobalTitle gt = sccpParameterFact.createGlobalTitle(ussdPropertiesManagement.getUssdGt(networkId), 0,
					NumberingPlan.ISDN_TELEPHONY, null, NatureOfAddress.INTERNATIONAL);
			address = sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0,
					ussdPropertiesManagement.getUssdSsn());

			this.setUssdGwSCCPAddressCMP(address);
		}
		return address;
	}

	private SccpAddress getMSCSccpAddress() {

		// TODO : use the networkNodeNumber also to derive if its
		// International / ISDN?
		GlobalTitle gt = sccpParameterFact.createGlobalTitle(getLocationInfoCMP().getNetworkNodeNumber().getAddress(),
				0, NumberingPlan.ISDN_TELEPHONY, null, NatureOfAddress.INTERNATIONAL);
		return sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0,
				ussdPropertiesManagement.getMscSsn());
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

    protected void terminateProtocolConnection() {
    }

    protected void updateDialogFailureStat() {
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
    }

    protected boolean isSip() {
        return false;
    }

}

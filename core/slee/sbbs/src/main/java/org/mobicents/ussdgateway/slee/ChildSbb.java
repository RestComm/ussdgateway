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

package org.mobicents.ussdgateway.slee;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.SLEEException;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerID;
import javax.slee.facilities.TimerOptions;

import javolution.xml.stream.XMLStreamException;

import org.joda.time.DateTime;
import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.dialog.MAPDialogState;
import org.restcomm.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.restcomm.protocols.ss7.map.api.dialog.ProcedureCancellationReason;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.mobicents.slee.ChildRelationExt;
import org.restcomm.slee.resource.map.MAPContextInterfaceFactory;
import org.restcomm.slee.resource.map.events.DialogAccept;
import org.restcomm.slee.resource.map.events.DialogClose;
import org.restcomm.slee.resource.map.events.DialogDelimiter;
import org.restcomm.slee.resource.map.events.DialogNotice;
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
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * 
 * @author amit bhayani
 * @author baranowb
 */
public abstract class ChildSbb extends USSDBaseSbb implements ChildInterface {

	private EventsSerializeFactory eventsSerializeFactory = null;

	private TimerFacility timerFacility = null;

	protected static final UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();

	public ChildSbb(String loggerName) {
		super(loggerName);
	}

	/**
	 * Timer event
	 */
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {

		if (super.logger.isWarningEnabled()) {
			super.logger.warning(String.format(
					"Application didn't revert in %d milliseconds for PULL case. Sending back dialogtimeouterrmssg for MAPDialog %s",
					ussdPropertiesManagement.getDialogTimeout(), this.getMAPDialog()));
		}

        try {

            String errorMssg = ussdPropertiesManagement.getDialogTimeoutErrorMessage();
            this.sendErrorMessage(errorMssg);

            if (isSip()) { // sending error message only in SIP case
                XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
                xmlMAPDialog.reset();
                xmlMAPDialog.setDialogTimedOut(true);

                // TODO : Hardcoding MessageType to Abort
                xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

                this.sendUssdData(xmlMAPDialog);
            }
        } catch (Exception e) {
            logger.severe("Error while sending an error message to a peer " + e.getMessage(), e);
        }

        this.terminateProtocolConnection();

        this.ussdStatAggregator.updateAppTimeouts();
        this.updateDialogFailureStat();

        this.createCDRRecord(RecordStatus.FAILED_APP_TIMEOUT);
	}

    protected abstract boolean isSip();
    public abstract boolean getFinalMessageSent();
    public abstract void setFinalMessageSent(boolean val);

	// //////////////////////
	// MAP Stuff handlers //
	// //////////////////////

	/**
	 * MAP USSD Event Handlers
	 */

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest evt, ActivityContextInterface aci) {
		try {

			if (this.logger.isFineEnabled()) {
				this.logger.fine("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION for MAPDialog "
						+ evt.getMAPDialog());
			}

			this.setProcessUnstructuredSSRequestInvokeId(evt.getInvokeId());

			XmlMAPDialog dialog = this.getXmlMAPDialog();
			dialog.addMAPMessage(((MAPEvent) evt).getWrappedEvent());
			ChargeInterface cdrInterface = this.getCDRChargeInterface();
			USSDCDRState state = cdrInterface.getState();
			if (!state.isInitialized()) {
				String serviceCode = evt.getUSSDString().getString(null);
				// serviceCode = serviceCode.substring(serviceCode.indexOf("*")
				// + 1, serviceCode.indexOf("#"));
				state.init(dialog.getLocalDialogId(), serviceCode, dialog.getReceivedDestReference(), dialog
						.getReceivedOrigReference(), evt.getMSISDNAddressString(),
						evt.getMAPDialog().getLocalAddress(), evt.getMAPDialog().getRemoteAddress());
				state.setDialogStartTime(DateTime.now());
				state.setRemoteDialogId(evt.getMAPDialog().getRemoteDialogId());
				cdrInterface.setState(state);

				// attach, in case impl wants to use more of dialog.
				SbbLocalObject sbbLO = (SbbLocalObject) cdrInterface;
				aci.attach(sbbLO);
			}
			this.sendUssdData(dialog);

			// Set timer last
			this.setTimer(aci);
		} catch (Exception e) {
			// TODO remove MAPDialog from logger once actual event in MAP stack
			// has it embedded
			logger.severe(String.format(
					"Exception while processing PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION = %s MAPDialog = %s", evt,
					evt.getMAPDialog()), e);

			this.sendServerErrorMessage();

            this.terminateProtocolConnection();

			this.updateDialogFailureStat();

			this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);
		}
	}

	public void onUnstructuredSSResponse(UnstructuredSSResponse evt, ActivityContextInterface aci) {
		try {

			if (this.logger.isFineEnabled()) {
				this.logger.fine("Received UNSTRUCTURED_SS_RESPONSE_INDICATION for MAPDialog " + evt.getMAPDialog());
			}

			super.ussdStatAggregator.updateMessagesRecieved();
            super.ussdStatAggregator.updateMessagesAll();

			XmlMAPDialog dialog = this.getXmlMAPDialog();
			dialog.reset();
			String userObject = this.getUserObject();
			if(userObject!=null){
				dialog.setUserObject(userObject);
			}
			dialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());
			dialog.addMAPMessage(((MAPEvent) evt).getWrappedEvent());
			EventsSerializeFactory factory = this.getEventsSerializeFactory();

            ChargeInterface cdrInterface = this.getCDRChargeInterface();
            USSDCDRState state = cdrInterface.getState();

            if(state.isInitialized()){
                String ussdString = evt.getUSSDString().getString(null);
                if (state.getUssdString() == null) {
                    state.setUssdString(ussdString);
                } else {
                    state.setUssdString(state.getUssdString() + USSDCDRState.USSD_STRING_SEPARATOR + ussdString);
                }
            }

			this.sendUssdData(dialog);

			// Set timer last
			this.setTimer(aci);
		} catch (Exception e) {
			logger.severe(String.format(
					"Exception while processing UNSTRUCTURED_SS_RESPONSE_INDICATION = %s MAPDialog = %s", evt,
					evt.getMAPDialog()), e);

            this.sendServerErrorMessage();

			this.terminateProtocolConnection();

            this.updateDialogFailureStat();

			this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);
		}
	}

	/**
	 * MAP Dialog Event Handlers
	 */

	public void onDialogDelimiter(DialogDelimiter evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx :  onDialogDelimiter " + evt);
		}
	}

	public void onDialogAccept(DialogAccept evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx :  onDialogAccept " + evt);
		}
	}

	public void onDialogReject(DialogReject evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("DialogRejected " + evt);
		}

		try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.setMapRefuseReason(evt.getRefuseReason());
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

			this.sendUssdData(xmlMAPDialog);
		} catch (Exception e) {
			logger.severe("Error while trying to send DialogReject to App", e);
		}

		this.terminateProtocolConnection();

        this.updateDialogFailureStat();

		this.createCDRRecord(RecordStatus.FAILED_DIALOG_REJECTED);
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx : DialogUserAbort " + evt);
		}

		try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.abort(evt.getUserReason());
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

			this.sendUssdData(xmlMAPDialog);
		} catch (Exception e) {
			logger.severe("Error while trying to send DialogUserAbort to App", e);
		}

        this.terminateProtocolConnection();

        this.updateDialogFailureStat();

		this.createCDRRecord(RecordStatus.FAILED_DIALOG_USER_ABORT);
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx : DialogProviderAbort " + evt);
		}

		try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.setMapAbortProviderReason(evt.getAbortProviderReason());
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

			this.sendUssdData(xmlMAPDialog);
		} catch (Exception e) {
			logger.severe("Error while trying to send DialogProviderAbort to App", e);
		}

		this.terminateProtocolConnection();

        this.updateDialogFailureStat();

		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.createCDRRecord(RecordStatus.FAILED_PROVIDER_ABORT);
	}

	public void onDialogClose(DialogClose evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx : DialogClosed " + evt);
		}
		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();

        this.updateDialogFailureStat();

        this.createCDRRecord(RecordStatus.FAILED_SYSTEM_FAILURE);
	}

	public void onDialogNotice(DialogNotice evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx : onDialogNotice " + evt);
		}
	}

	public void onDialogTimeout(DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx : onDialogTimeout " + evt);
		}

		try {
		    MAPDialog mapDialog = evt.getMAPDialog();
		    mapDialog.keepAlive();
		    MAPUserAbortChoice mapUserAbortChoice = this.mapParameterFactory.createMAPUserAbortChoice();
		    mapUserAbortChoice.setProcedureCancellationReason(ProcedureCancellationReason.callRelease);
		    mapDialog.abort(mapUserAbortChoice);

			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.setDialogTimedOut(true);
			
			//TODO : Hardcoding MessageType to Abort
			xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

			this.sendUssdData(xmlMAPDialog);
		} catch (Exception e) {
			logger.severe("Error while trying to send DialogTimeout to App", e);
		}

		// TODO : Should send any xml content?
        if (this.isSip())
            this.terminateProtocolConnection();
        else {
            this.setFinalMessageSent(true);
        }

        this.ussdStatAggregator.updateMapDialogTimeouts();
        this.updateDialogFailureStat();
		this.createCDRRecord(RecordStatus.FAILED_DIALOG_TIMEOUT);
	}

	/**
	 * MAP Component Event Handler
	 */
	public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  InvokeTimeout" + evt);
		}

		try {
			//If User is taking too long to respond, lets Abort the Dialog
            MAPUserAbortChoice abortChoice = new MAPUserAbortChoiceImpl();
            abortChoice.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);
            this.abortMapDialog(abortChoice);

			try {
				XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
				xmlMAPDialog.reset();
				xmlMAPDialog.setInvokeTimedOut(evt.getInvokeId());
				xmlMAPDialog.setTCAPMessageType(MessageType.Abort);

				this.sendUssdData(xmlMAPDialog);
			} catch (Exception e) {
				logger.severe("Error while trying to send DialogTimeout to App", e);
			}
		} catch (MAPException e1) {
			logger.severe("Error while trying to send Abort MAP Dialog", e1);
		}

        this.terminateProtocolConnection();

        this.ussdStatAggregator.updateMapInvokeTimeouts();
        this.updateDialogFailureStat();

		this.createCDRRecord(RecordStatus.FAILED_INVOKE_TIMEOUT);
	}

	public void onErrorComponent(ErrorComponent event, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  ErrorComponent" + event);
		}

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

            this.sendUssdData(xmlMAPDialog);
        } catch (Exception e) {
            logger.severe("Error while trying to send ErrorComponent to HTTP App", e);
        }

        this.terminateProtocolConnection();

        if (event.getMAPDialog().getTCAPMessageType() != MessageType.End)
            this.updateDialogFailureStat();
        super.ussdStatAggregator.updateMapErrorComponentOther();

        this.createCDRRecord(RecordStatus.FAILED_MAP_ERROR_COMPONENT);
	}

	public void onRejectComponent(RejectComponent event, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  RejectComponent" + event);
		}

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

            this.sendUssdData(xmlMAPDialog);
        } catch (Exception e) {
            logger.severe("Error while trying to send ErrorComponent to HTTP App", e);
        }
        
        this.createCDRRecord(RecordStatus.FAILED_MAP_REJECT_COMPONENT);

        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPushFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
	}

    public void onDialogRelease(DialogRelease evt, ActivityContextInterface aci) {
        super.ussdStatAggregator.removeDialogsInProcess();
    }

	// //////////////////////////
	// Abstract child methods //
	// //////////////////////////
    /**
     * Termiantes specific protocol connection if any exists.
     */
    protected abstract void terminateProtocolConnection();

    /**
     * Termiantes specific protocol connection if any exists.
     */
    protected abstract void updateDialogFailureStat();

	/**
	 * Creates connection to other side via specific protocol if one does not
	 * exist and sends request
	 * 
	 * @param xmlRequest
	 * @throws Exception
	 */
	protected abstract void sendUssdData(XmlMAPDialog xmlMAPDialog /* byte[] data*/ ) throws Exception;

	/**
	 * Checks if there is specific protocol connection alive.
	 * 
	 * @return
	 */
	protected abstract boolean checkProtocolConnection();

	protected void sendErrorMessage(MAPDialogSupplementary mapDialogSupplementary, String errorMssg) {
        if (errorMssg != null) {
            if (errorMssg.length() > 160)
                errorMssg = errorMssg.substring(0, 160);
        } else {
            errorMssg = "sendError";
        }

		try {
			USSDString ussdString = mapParameterFactory.createUSSDString(errorMssg);

			// TODO this is in-correct. The CBSDataCodingScheme must be
			// configurable or from original request?
			CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(0x0f);
			mapDialogSupplementary.addProcessUnstructuredSSResponse(this.getProcessUnstructuredSSRequestInvokeId(),
					cbsDataCodingScheme, ussdString);
			mapDialogSupplementary.close(false);
		} catch (Exception e) {
			logger.severe("Exception while trying to send MAP ErrorMessage", e);
		}

	}

    private void abortMapDialog(MAPUserAbortChoice abortChoice) throws MAPException {
        MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();
        if (mapDialogSupplementary != null
                && (mapDialogSupplementary.getState() == MAPDialogState.ACTIVE || mapDialogSupplementary.getState() == MAPDialogState.INITIAL_RECEIVED)) {
            mapDialogSupplementary.abort(abortChoice);
        }
    }

	protected void sendErrorMessage(String errorMssg) {
		MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();
		this.sendErrorMessage(mapDialogSupplementary, errorMssg);
	}

	protected void sendServerErrorMessage() {
		String errorMssg = ussdPropertiesManagement.getServerErrorMessage();
		this.sendErrorMessage(errorMssg);
	}

	protected void createCDRRecord(RecordStatus recordStatus) {
		try {
			this.getCDRChargeInterface().createRecord(recordStatus);
		} catch (Exception e) {
			logger.severe("Error while trying to create CDR Record", e);
		}
	}

	// ///////////////////
	// Charge interface //
	// ///////////////////

	private static final String CHARGER = "CHARGER";

	public abstract ChildRelationExt getCDRInterfaceChildRelation();

    public abstract ChildRelationExt getCDRPlainInterfaceChildRelation();

	public ChargeInterface getCDRChargeInterface() {
        UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
        ChildRelationExt childExt;
        if (ussdPropertiesManagement.getCdrLoggingTo() == UssdPropertiesManagement.CdrLoggedType.Textfile) {
            childExt = getCDRPlainInterfaceChildRelation();
        } else {
            childExt = getCDRInterfaceChildRelation();
        }

		ChargeInterface child = (ChargeInterface) childExt.get(CHARGER);
		if (child == null) {
			try {
				child = (ChargeInterface) childExt.create(CHARGER);
			} catch (TransactionRequiredLocalException e) {
				logger.severe("TransactionRequiredLocalException when creating CDR child", e);
			} catch (IllegalArgumentException e) {
				logger.severe("IllegalArgumentException when creating CDR child", e);
			} catch (NullPointerException e) {
				logger.severe("NullPointerException when creating CDR child", e);
			} catch (SLEEException e) {
				logger.severe("SLEEException when creating CDR child", e);
			} catch (CreateException e) {
				logger.severe("CreateException when creating CDR child", e);
			}
		}

		return child;
	}

	// /////////////////
	// Sbb callbacks //
	// /////////////////
	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);
		this.logger = sbbContext.getTracer("USSD-Child-" + getClass().getName());

		try {

			super.mapAcif = (MAPContextInterfaceFactory) this.sbbContext
					.getActivityContextInterfaceFactory(mapRATypeID);
			super.mapProvider = (MAPProvider) this.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			super.mapParameterFactory = this.mapProvider.getMAPParameterFactory();
			super.ussdStatAggregator = UssdStatAggregator.getInstance();

			this.timerFacility = this.sbbContext.getTimerFacility();

		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	// ///////
	// CMP //
	// ///////

	public abstract void setCall(ScRoutingRule call);

	public abstract ScRoutingRule getCall();

	public abstract void setXmlMAPDialog(XmlMAPDialog dialog);

	public abstract XmlMAPDialog getXmlMAPDialog();

	public abstract void setCDRState(USSDCDRState dialog);

	public abstract USSDCDRState getCDRState();

	// 'timerID' CMP field setter
	public abstract void setTimerID(TimerID value);

	// 'timerID' CMP field getter
	public abstract TimerID getTimerID();

	public abstract void setProcessUnstructuredSSRequestInvokeId(long processUnstructuredSSRequestInvokeId);

	// 'timerID' CMP field getter
	public abstract long getProcessUnstructuredSSRequestInvokeId();

	// userObject
	public abstract void setUserObject(String userObject);

	public abstract String getUserObject();

	// //////////////////
	// SBB LO methods //
	// //////////////////

	public void setCallFact(ScRoutingRule call) {
		this.setCall(call);
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
//	 * recordGenerationSucessed
//	 * (org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent.RecordType)
//	 */
//	@Override
//	public void recordGenerationSucessed() {
//		if (this.logger.isFineEnabled()) {
//			this.logger.fine("Generated CDR for Status: " + getCDRChargeInterface().getState());
//		}
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
//	 * recordGenerationFailed(java.lang.String)
//	 */
//	@Override
//	public void recordGenerationFailed(String message) {
//		if (this.logger.isSevereEnabled()) {
//			this.logger.severe("Failed to generate CDR! Message: '" + message + "'");
//			this.logger.severe("Status: " + getCDRChargeInterface().getState());
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
//	 * recordGenerationFailed(java.lang.String, java.lang.Throwable)
//	 */
//	@Override
//	public void recordGenerationFailed(String message, Throwable t) {
//		if (this.logger.isSevereEnabled()) {
//			this.logger.severe("Failed to generate CDR! Message: '" + message + "'", t);
//			this.logger.severe("Status: " + getCDRChargeInterface().getState());
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#initFailed(java
//	 * .lang.String, java.lang.Throwable)
//	 */
//	@Override
//	public void initFailed(String message, Throwable t) {
//		if (this.logger.isSevereEnabled()) {
//			this.logger.severe("Failed to initializee CDR Database! Message: '" + message + "'", t);
//		}
//
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#initSuccessed()
//	 */
//	@Override
//	public void initSuccessed() {
//		if (this.logger.isFineEnabled()) {
//			this.logger.fine("CDR Database has been initialized!");
//		}
//
//	}

	// ///////////////////////////////////////////////
	// protected child stuff, to be used in parent //
	// ///////////////////////////////////////////////

	protected EventsSerializeFactory getEventsSerializeFactory() throws XMLStreamException {
		if (this.eventsSerializeFactory == null) {
			this.eventsSerializeFactory = new EventsSerializeFactory();
		}
		return this.eventsSerializeFactory;
	}

	protected void cancelTimer() {
		try {
			TimerID timerID = this.getTimerID();
			if (timerID != null) {
				this.timerFacility.cancelTimer(timerID);
			}
		} catch (Exception e) {
			logger.severe("Could not cancel Timer", e);
		}
	}

	private void setTimer(ActivityContextInterface ac) {
		TimerOptions options = new TimerOptions();
		long waitingTime = ussdPropertiesManagement.getDialogTimeout();
		// Set the timer on ACI
		TimerID timerID = this.timerFacility.setTimer(ac, null, System.currentTimeMillis() + waitingTime, options);
		this.setTimerID(timerID);
	}
}

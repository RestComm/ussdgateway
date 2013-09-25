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
package org.mobicents.ussdgateway.slee;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.SLEEException;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.TransactionRequiredLocalException;
import javax.slee.facilities.TimerEvent;
import javax.slee.facilities.TimerFacility;
import javax.slee.facilities.TimerID;
import javax.slee.facilities.TimerOptions;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.mobicents.slee.ChildRelationExt;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.map.MAPContextInterfaceFactory;
import org.mobicents.slee.resource.map.events.DialogAccept;
import org.mobicents.slee.resource.map.events.DialogClose;
import org.mobicents.slee.resource.map.events.DialogDelimiter;
import org.mobicents.slee.resource.map.events.DialogNotice;
import org.mobicents.slee.resource.map.events.DialogProviderAbort;
import org.mobicents.slee.resource.map.events.DialogReject;
import org.mobicents.slee.resource.map.events.DialogTimeout;
import org.mobicents.slee.resource.map.events.DialogUserAbort;
import org.mobicents.slee.resource.map.events.ErrorComponent;
import org.mobicents.slee.resource.map.events.InvokeTimeout;
import org.mobicents.slee.resource.map.events.MAPEvent;
import org.mobicents.slee.resource.map.events.RejectComponent;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.DialogType;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.cdr.AbortType;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;
import org.mobicents.ussdgateway.slee.cdr.TimeoutType;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * 
 * @author amit bhayani
 * 
 */
public abstract class ChildSbb implements Sbb, ChildInterface, ChargeInterfaceParent {

	protected SbbContextExt sbbContext;

	// /////////////////
	// MAP RA Stuff //
	// /////////////////

	protected static final ResourceAdaptorTypeID mapRATypeID = new ResourceAdaptorTypeID("MAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String mapRaLink = "MAPRA";

	protected MAPContextInterfaceFactory mapAcif;
	protected MAPProvider mapProvider;
	protected MAPParameterFactory mapParameterFactory;

	protected Tracer logger;

	private EventsSerializeFactory eventsSerializeFactory = null;

	private TimerFacility timerFacility = null;

	private static final UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();

	/**
	 * Timer event
	 */
	public void onTimerEvent(TimerEvent event, ActivityContextInterface aci) {

		if (this.logger.isWarningEnabled()) {
			this.logger.warning(String.format(
					"Application didn't revert in %d milliseconds. Sending back dialogtimeouterrmssg",
					ussdPropertiesManagement.getDialogTimeout()));
		}

		try {
			MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) aci.getActivity();
			String errorMssg = ussdPropertiesManagement.getDialogTimeoutErrorMessage();
			this.sendErrorMessage(mapDialogSupplementary, errorMssg);
		} catch (Exception e) {
			logger.severe("Error while trying to send Dialog timeout error message to user", e);
		} finally {
			// this.getCDRChargeInterface().createTerminateRecord();
			// TODO CDR?
		}

		this.terminateProtocolConnection();
	}

	// //////////////////////
	// MAP Stuff handlers //
	// //////////////////////

	/**
	 * MAP USSD Event Handlers
	 */

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest evt, ActivityContextInterface aci) {
		try {

			if (this.logger.isFineEnabled()) {
				this.logger.fine("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION for MAP Dialog Id "
						+ evt.getMAPDialog().getLocalDialogId());
			}

			this.setProcessUnstructuredSSRequestInvokeId(evt.getInvokeId());
			this.setTimer(aci);

			Dialog dialog = this.getDialog();
			dialog.setMAPMessage(((MAPEvent) evt).getWrappedEvent());
			ChargeInterface cdrInterface = this.getCDRChargeInterface();
			USSDCDRState state = cdrInterface.getState();
			if (!state.isInitialized()) {
				String serviceCode = evt.getUSSDString().getString(null);
				serviceCode = serviceCode.substring(serviceCode.indexOf("*") + 1, serviceCode.indexOf("#"));
				state.init(dialog.getId(), serviceCode, dialog.getDestReference(), dialog.getOrigReference(), evt
						.getMSISDNAddressString(), evt.getMAPDialog().getLocalAddress(), evt.getMAPDialog()
						.getRemoteAddress());
				cdrInterface.setState(state);
				cdrInterface.createInitRecord();
				// attach, in case impl wants to use more of dialog.
				SbbLocalObject sbbLO = (SbbLocalObject) cdrInterface;
				aci.attach(sbbLO);
			} else {
				// use this, since getDialog.type is not changed now
				cdrInterface.createContinueRecord();
			}
			byte[] data = this.getEventsSerializeFactory().serialize(dialog);
			this.sendUssdData(data);
		} catch (Exception e) {
			logger.severe(
					String.format("Exception while processing PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION = %s", evt), e);
			// TODO Abort DIalog?
		}
	}

	public void onUnstructuredSSResponse(UnstructuredSSResponse evt, ActivityContextInterface aci) {
		try {

			if (this.logger.isFineEnabled()) {
				this.logger.fine("Received UNSTRUCTURED_SS_RESPONSE_INDICATION for MAP Dialog Id "
						+ evt.getMAPDialog().getLocalDialogId());
			}

			this.setTimer(aci);

			Dialog dialog = new Dialog(DialogType.CONTINUE, evt.getMAPDialog().getLocalDialogId(), null, null);
			dialog.setMAPMessage(((MAPEvent) evt).getWrappedEvent());
			this.getCDRChargeInterface().createContinueRecord();
			EventsSerializeFactory factory = this.getEventsSerializeFactory();
			byte[] data = factory.serialize(dialog);

			this.sendUssdData(data);
		} catch (Exception e) {
			logger.severe(String.format("Exception while processing UNSTRUCTURED_SS_RESPONSE_INDICATION = %s", evt), e);
			// TODO Abort DIalog?
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
		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx : DialogUserAbort " + evt);
		}

		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx : DialogProviderAbort " + evt);
		}
		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogClose(DialogClose evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx : DialogClosed " + evt);
		}
		// TODO: CDR, how this should be covered?
		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogNotice(DialogNotice evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("Rx : onDialogNotice" + evt);
		}
	}

	public void onDialogTimeout(DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx : DialogTimeout" + evt);
		}
		try {
			// TODO : Should send any xml content?
			this.terminateProtocolConnection();
		} finally {
			getCDRChargeInterface().createTimeoutRecord(TimeoutType.DialogTimeout);
		}
	}

	/**
	 * MAP Component Event Handler
	 */
	public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  InvokeTimeout" + evt);
		}
		// TODO End both the activities?
	}

	public void onErrorComponent(ErrorComponent evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  ErrorComponent" + evt);
		}
		// TODO End both the activities?
	}

//	public void onProviderErrorComponent(ProviderErrorComponent evt, ActivityContextInterface aci) {
//		if (logger.isWarningEnabled()) {
//			this.logger.warning("Rx :  ProviderErrorComponent" + evt);
//		}
//		
//		// TODO End both the activities?
//	}

	public void onRejectComponent(RejectComponent evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  RejectComponent" + evt);
		}
		// TODO End both the activities?
	}

	// //////////////////////////
	// Abstract child methods //
	// //////////////////////////
	/**
	 * Termiantes specific protocol connection if any exists.
	 */
	protected abstract void terminateProtocolConnection();

	/**
	 * Creates connection to other side via specific protocol if one does not
	 * exist and sends request
	 * 
	 * @param xmlRequest
	 * @throws Exception
	 */
	protected abstract void sendUssdData(byte[] data) throws Exception;

	/**
	 * Checks if there is specific protocol connection alive.
	 * 
	 * @return
	 */
	protected abstract boolean checkProtocolConnection();

	protected void sendErrorMessage(MAPDialogSupplementary mapDialogSupplementary, String errorMssg)
			throws MAPException {
		USSDString ussdString = mapParameterFactory.createUSSDString(errorMssg);
		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(0x0f);
		mapDialogSupplementary.addProcessUnstructuredSSResponse(this.getProcessUnstructuredSSRequestInvokeId(),
				cbsDataCodingScheme, ussdString);
		mapDialogSupplementary.close(false);

	}

	// ///////////////////
	// Charge interface //
	// ///////////////////

	private static final String CHARGER = "CHARGER";

	public abstract ChildRelationExt getCDRInterfaceChildRelation();

	public ChargeInterface getCDRChargeInterface() {
		ChildRelationExt childExt = getCDRInterfaceChildRelation();
		ChargeInterface child = (ChargeInterface) childExt.get(CHARGER);
		if (child == null) {
			try {
				child = (ChargeInterface) childExt.create(CHARGER);
			} catch (TransactionRequiredLocalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SLEEException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CreateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return child;
	}

	// /////////////////
	// Sbb callbacks //
	// /////////////////
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer("USSD-CHILD-" + getClass().getName());

		try {

			this.mapAcif = (MAPContextInterfaceFactory) this.sbbContext.getActivityContextInterfaceFactory(mapRATypeID);
			this.mapProvider = (MAPProvider) this.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			this.mapParameterFactory = this.mapProvider.getMAPParameterFactory();

			this.timerFacility = this.sbbContext.getTimerFacility();

		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
		this.logger = null;
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbRemove() {
	}

	public void sbbExceptionThrown(Exception exception, Object object, ActivityContextInterface activityContextInterface) {
	}

	public void sbbRolledBack(RolledBackContext rolledBackContext) {
	}

	// ///////
	// CMP //
	// ///////

	public abstract void setCall(ScRoutingRule call);

	public abstract ScRoutingRule getCall();

	public abstract void setDialog(Dialog dialog);

	public abstract Dialog getDialog();

	public abstract void setCDRState(USSDCDRState dialog);

	public abstract USSDCDRState getCDRState();

	// 'timerID' CMP field setter
	public abstract void setTimerID(TimerID value);

	// 'timerID' CMP field getter
	public abstract TimerID getTimerID();

	public abstract void setProcessUnstructuredSSRequestInvokeId(long processUnstructuredSSRequestInvokeId);

	// 'timerID' CMP field getter
	public abstract long getProcessUnstructuredSSRequestInvokeId();

	// //////////////////
	// SBB LO methods //
	// //////////////////

	public void setCallFact(ScRoutingRule call) {
		this.setCall(call);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
	 * recordGenerationSucessed
	 * (org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent.RecordType)
	 */
	@Override
	public void recordGenerationSucessed(RecordType type) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
	 * recordGenerationFailed
	 * (org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent.RecordType,
	 * java.lang.String)
	 */
	@Override
	public void recordGenerationFailed(RecordType type, String message) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#
	 * recordGenerationFailed
	 * (org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent.RecordType,
	 * java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void recordGenerationFailed(RecordType type, String message, Throwable t) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#initFailed(java
	 * .lang.String, java.lang.Throwable)
	 */
	@Override
	public void initFailed(String message, Throwable t) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent#initSuccessed()
	 */
	@Override
	public void initSuccessed() {
		// TODO Auto-generated method stub

	}

	// ///////////////////////////////////////////////
	// protected child stuff, to be used in parent //
	// ///////////////////////////////////////////////

	protected void abort(MAPDialog mapDialog) throws MAPException {
		// TODO get the reason

		MAPUserAbortChoice mapUserAbortChoice = this.mapParameterFactory.createMAPUserAbortChoice();
		// As of now hardcoded
		mapUserAbortChoice.setUserSpecificReason();
		try {
			mapDialog.abort(mapUserAbortChoice);
		} finally {
			this.getCDRChargeInterface().createAbortRecord(AbortType.UserSpecificReason);
		}

	}

	protected MAPDialogSupplementary getMAPDialog() {
		MAPDialogSupplementary mapDialog = null;

		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			Object activity = aci.getActivity();
			if (activity instanceof MAPDialogSupplementary) {
				return (MAPDialogSupplementary) activity;
			}
		}

		return mapDialog;
	}

	protected EventsSerializeFactory getEventsSerializeFactory() throws XMLStreamException {
		if (this.eventsSerializeFactory == null) {
			this.eventsSerializeFactory = new EventsSerializeFactory();
		}
		return this.eventsSerializeFactory;
	}

	protected void addUnstructuredSSRequest(UnstructuredSSRequest unstructuredSSRequestIndication) throws MAPException {
		MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();

		if (mapDialogSupplementary == null) {
			// TODO:
			return;
		}

		mapDialogSupplementary.addUnstructuredSSRequest(unstructuredSSRequestIndication.getDataCodingScheme(),
				unstructuredSSRequestIndication.getUSSDString(), null, null);
		try {
			mapDialogSupplementary.send();
		} finally {
			this.getCDRChargeInterface().createContinueRecord();
		}
		// TODO : Check if the Dialog Type is CONTINUE or END?
	}

	protected void addProcessUnstructuredSSResponse(
			ProcessUnstructuredSSResponse processUnstructuredSSResponseIndication) throws MAPException {
		MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();
		if (mapDialogSupplementary == null) {
			// TODO:
			return;
		}

		mapDialogSupplementary.addProcessUnstructuredSSResponse(processUnstructuredSSResponseIndication.getInvokeId(),
				processUnstructuredSSResponseIndication.getDataCodingScheme(),
				processUnstructuredSSResponseIndication.getUSSDString());
		try {
			mapDialogSupplementary.close(false);
		} finally {
			this.getCDRChargeInterface().createTerminateRecord();
		}
		// TODO : Check if the Dialog Type is CONTINUE or END?
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

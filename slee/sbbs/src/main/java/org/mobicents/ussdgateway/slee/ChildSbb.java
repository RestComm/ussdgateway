package org.mobicents.ussdgateway.slee;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponseIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponseIndication;
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
import org.mobicents.slee.resource.map.events.ProviderErrorComponent;
import org.mobicents.slee.resource.map.events.RejectComponent;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.DialogType;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.rules.Call;

public abstract class ChildSbb implements Sbb, ChildInterface {

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

	// //////////////////////
	// MAP Stuff handlers //
	// //////////////////////

	/**
	 * MAP USSD Event Handlers
	 */

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequestIndication evt, ActivityContextInterface aci) {
		try {
			if (this.checkProtocolConnection()) {
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION for MAP Dialog Id "
							+ evt.getMAPDialog().getDialogId() + ", with active session, terminating both.");
				}
				try {
					this.abort(evt.getMAPDialog());
				} catch (MAPException e) {
					this.logger.severe("Error while aborting MAPDialog ", e);
				}
				terminateProtocolConnection();
			} else {
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION for MAP Dialog Id "
							+ evt.getMAPDialog().getDialogId());
				}

				Dialog dialog = this.getDialog();
				dialog.setProcessUnstructuredSSRequest(evt);

				byte[] data = this.getEventsSerializeFactory().serialize(dialog);
				this.sendUssdData(data);
			}
		} catch (Exception e) {
			logger.severe(
					String.format("Exception while processing PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION = %s", evt), e);
			// TODO Abort DIalog?
		}
	}

	public void onUnstructuredSSResponse(UnstructuredSSResponseIndication evt, ActivityContextInterface aci) {
		try {
			if (!this.checkProtocolConnection()) {
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Received UNSTRUCTURED_SS_RESPONSE_INDICATION for MAP Dialog Id "
							+ evt.getMAPDialog().getDialogId() + ", without active session, terminating both.");
				}
				MAPUserAbortChoice mapUsrAbrtCho = this.mapParameterFactory.createMAPUserAbortChoice();
				mapUsrAbrtCho.setUserSpecificReason();

				try {
					evt.getMAPDialog().abort(mapUsrAbrtCho);
				} catch (MAPException e) {
					this.logger.severe("Error while aborting MAPDialog ", e);
				}
				terminateProtocolConnection();
			} else {
				if (this.logger.isFineEnabled()) {
					this.logger.fine("Received UNSTRUCTURED_SS_RESPONSE_INDICATION for MAP Dialog Id "
							+ evt.getMAPDialog().getDialogId());
				}

				Dialog dialog = new Dialog(DialogType.CONTINUE, evt.getMAPDialog().getDialogId(), null, null);
				dialog.setUnstructuredSSResponse(evt);

				EventsSerializeFactory factory = this.getEventsSerializeFactory();
				byte[] data = factory.serialize(dialog);

				this.sendUssdData(data);
			}
		} catch (Exception e) {
			logger.severe(String.format("Exception while processing UNSTRUCTURED_SS_RESPONSE_INDICATION = %s", evt), e);
			// TODO Abort DIalog?
		}
	}

	/**
	 * MAP Dialog Event Handlers
	 */

	public void onDialogDelimiter(DialogDelimiter evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogDelimiter" + evt);
		}
	}

	public void onDialogAccept(DialogAccept evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogAccept" + evt);
		}
	}

	public void onDialogReject(DialogReject evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogReject" + evt);
		}

		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogUserAbort" + evt);
		}

		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogProviderAbort" + evt);
		}

		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogClose(DialogClose evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogClose" + evt);
		}

		// TODO : Should we add any xml content?
		this.terminateProtocolConnection();
	}

	public void onDialogNotice(DialogNotice evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  onDialogNotice" + evt);
		}
	}

	public void onDialogTimeout(DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  DialogTimeout" + evt);
		}
	}

	/**
	 * MAP Component Event Handler
	 */
	public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  InvokeTimeout" + evt);
		}
	}

	public void onErrorComponent(ErrorComponent evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  ErrorComponent" + evt);
		}
	}

	public void onProviderErrorComponent(ProviderErrorComponent evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  ProviderErrorComponent" + evt);
		}
	}

	public void onRejectComponent(RejectComponent evt, ActivityContextInterface aci) {
		if (logger.isInfoEnabled()) {
			this.logger.info("Rx :  RejectComponent" + evt);
		}
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

	public abstract void setCall(Call call);

	public abstract Call getCall();

	public abstract void setDialog(Dialog dialog);

	public abstract Dialog getDialog();

	// //////////////////
	// SBB LO methods //
	// //////////////////

	public void setCallFact(Call call) {
		this.setCall(call);
	}

	// ///////////////////////////////////////////////
	// protected child stuff, to be used in parent //
	// ///////////////////////////////////////////////

	protected void abort(MAPDialog mapDialog) throws MAPException {
		// TODO get the reason
		MAPUserAbortChoice mapUserAbortChoice = this.mapParameterFactory.createMAPUserAbortChoice();
		// As of now hardcoded
		mapUserAbortChoice.setUserSpecificReason();
		mapDialog.abort(mapUserAbortChoice);
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

	protected void addUnstructuredSSRequest(UnstructuredSSRequestIndication unstructuredSSRequestIndication)
			throws MAPException {
		MAPDialogSupplementary mapDialogSupplementary = (MAPDialogSupplementary) this.getMAPDialog();

		if (mapDialogSupplementary == null) {
			// TODO:
			return;
		}

		mapDialogSupplementary.addUnstructuredSSRequest(unstructuredSSRequestIndication.getUSSDDataCodingScheme(),
				unstructuredSSRequestIndication.getUSSDString(), null, null);
		mapDialogSupplementary.send();
		// TODO : Check if the Dialog Type is CONTINUE or END?
	}

	protected void addProcessUnstructuredSSResponse(
			ProcessUnstructuredSSResponseIndication processUnstructuredSSResponseIndication) throws MAPException {
		MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();
		if (mapDialogSupplementary == null) {
			// TODO:
			return;
		}

		mapDialogSupplementary.addProcessUnstructuredSSResponse(processUnstructuredSSResponseIndication.getInvokeId(),
				processUnstructuredSSResponseIndication.getUSSDDataCodingScheme(),
				processUnstructuredSSResponseIndication.getUSSDString());
		mapDialogSupplementary.close(false);
		// TODO : Check if the Dialog Type is CONTINUE or END?
	}
}

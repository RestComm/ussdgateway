package org.mobicents.ussdgateway.slee;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.slee.ActivityContextInterface;
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.StatelessKnowledgeSession;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequestIndication;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.map.MAPContextInterfaceFactory;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.DialogType;
import org.mobicents.ussdgateway.rules.Call;

/**
 * 
 * @author amit bhayani
 */
public abstract class ParentSbb implements Sbb {

	protected SbbContextExt sbbContext;

	private Tracer logger;

	private KnowledgeBase kbase;
	private KnowledgeAgent kagent;

	protected MAPContextInterfaceFactory mapAcif;
	protected MAPProvider mapProvider;
	protected MAPParameterFactory mapParameterFactory;

	protected static final ResourceAdaptorTypeID mapRATypeID = new ResourceAdaptorTypeID("MAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String mapRaLink = "MAPRA";

	/** Creates a new instance of CallSbb */
	public ParentSbb() {
	}

	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer("USSD-Parent");
		try {
			Context ctx = (Context) new InitialContext();
			kagent = (KnowledgeAgent) ctx.lookup("java:/mobicents/ussdgateway/rulesservice");

			this.mapAcif = (MAPContextInterfaceFactory) this.sbbContext.getActivityContextInterfaceFactory(mapRATypeID);
			this.mapProvider = (MAPProvider) this.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			this.mapParameterFactory = this.mapProvider.getMAPParameterFactory();
		} catch (Exception ne) {
			logger.severe("Could not set SBB context:", ne);
		}
	}

	public void onDialogRequest(org.mobicents.slee.resource.map.events.DialogRequest evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("New MAP Dialog. Received event MAPOpenInfo " + evt);
		}

		Dialog dialog = new Dialog(DialogType.BEGIN, evt.getMAPDialog().getDialogId(), evt.getDestReference(),
				evt.getOrigReference());

		this.setDialog(dialog);
	}

	public void onDialogTimeout(org.mobicents.slee.resource.map.events.DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  onDialogTimeout" + evt);
		}
	}

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequestIndication evt, ActivityContextInterface aci) {

		try {

			USSDString ussdStrObj = evt.getUSSDString();
			String ussdStr = ussdStrObj.getString();

			Call call = new Call(ussdStr);

			if (this.logger.isFineEnabled()) {
				this.logger.fine(String.format("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION=%s", evt));
			}

			StatelessKnowledgeSession statelessksession = this.kbase.newStatelessKnowledgeSession();

			statelessksession.execute(call);

			if (this.logger.isFineEnabled()) {
				this.logger.fine(String.format("Call=%s", call));
			}

			if (call.isHttp()) {
				// Create child of Http SBB and call local method

				ChildRelation relation = this.getHttpClientSbb();
				ChildSbbLocalObject child = (ChildSbbLocalObject) relation.create();
				child.setCallFact(call);
				child.setDialog(this.getDialog());
				forwardEvent(child, aci);
			} else if (call.isSmpp()) {
				this.logger
						.warning(String
								.format("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION=%s and rule for routing is Call=%s. SMPP is not yet supported. Aborting MAP Dialog",
										evt, call));

				// Create child of SMPP SBB and call local method
				// TODO :

				this.abort(evt.getMAPDialog());
			} else {
				// TODO : Decline? Or Read Database and give back answer
				this.logger
						.warning(String
								.format("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION=%s and rule for routing is Call=%s. Aborting Dialog",
										evt, call));

			}
		} catch (Exception e) {
			logger.severe("Unexpected error: ", e);
		}

	}

	private void forwardEvent(SbbLocalObject child, ActivityContextInterface aci) {
		try {
			aci.attach(child);
			aci.detach(sbbContext.getSbbLocalObject());
		} catch (Exception e) {
			logger.severe("Unexpected error: ", e);
		}
	}

	protected void abort(MAPDialog mapDialog) throws MAPException {
		// TODO get the reason
		MAPUserAbortChoice mapUserAbortChoice = this.mapParameterFactory.createMAPUserAbortChoice();
		// As of now hardcoded
		mapUserAbortChoice.setUserSpecificReason();
		mapDialog.abort(mapUserAbortChoice);
	}

	public abstract ChildRelation getHttpClientSbb();

	public void unsetSbbContext() {
		this.sbbContext = null;
		this.logger = null;
	}

	public void sbbCreate() throws CreateException {
		kbase = kagent.getKnowledgeBase();
		if (this.logger.isFineEnabled()) {
			this.logger.fine("Created KnowledgeBase");
		}
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

	/**
	 * CMP
	 */
	public abstract void setDialog(Dialog dialog);

	public abstract Dialog getDialog();

}

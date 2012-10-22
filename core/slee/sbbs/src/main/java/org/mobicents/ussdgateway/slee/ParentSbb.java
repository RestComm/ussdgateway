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
import javax.slee.ChildRelation;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.map.MAPContextInterfaceFactory;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.DialogType;
import org.mobicents.ussdgateway.ShortCodeRoutingRuleManagement;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.rules.ScRoutingRule;

/**
 * 
 * @author amit bhayani
 */
public abstract class ParentSbb implements Sbb {

	protected SbbContextExt sbbContext;

	private Tracer logger;

	protected MAPContextInterfaceFactory mapAcif;
	protected MAPProvider mapProvider;
	protected MAPParameterFactory mapParameterFactory;

	protected static final ResourceAdaptorTypeID mapRATypeID = new ResourceAdaptorTypeID("MAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String mapRaLink = "MAPRA";

	private static final ShortCodeRoutingRuleManagement shortCodeRoutingRuleManagement = ShortCodeRoutingRuleManagement
			.getInstance();

	private static final UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();

	/** Creates a new instance of CallSbb */
	public ParentSbb() {
	}

	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer("USSD-Parent");
		try {
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

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest evt, ActivityContextInterface aci) {

		try {

			USSDString ussdStrObj = evt.getUSSDString();
			String shortCode = ussdStrObj.getString(null);

			if (this.logger.isFineEnabled()) {
				this.logger.fine(String.format("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION=%s", evt));
			}

			ScRoutingRule call = shortCodeRoutingRuleManagement.getScRoutingRule(shortCode);

			if (call == null) {

				if (this.logger.isWarningEnabled()) {
					this.logger.warning(String.format("No routing rule configured for short code=%s", shortCode));
				}
				this.sendError(evt, ussdPropertiesManagement.getNoRoutingRuleConfiguredMessage());
			} else {
				// Create child of Http SBB and call local method

				ChildRelation relation = this.getHttpClientSbb();
				ChildSbbLocalObject child = (ChildSbbLocalObject) relation.create();
				child.setCallFact(call);
				child.setDialog(this.getDialog());
				forwardEvent(child, aci);
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

	protected void sendError(ProcessUnstructuredSSRequest request, String errorMssg) throws MAPException {
		MAPDialogSupplementary mapDialogSupplementary = request.getMAPDialog();
		USSDString ussdString = mapParameterFactory.createUSSDString(errorMssg);
		mapDialogSupplementary.addProcessUnstructuredSSResponse(request.getInvokeId(),
				request.getDataCodingScheme(), ussdString);
		try {
			mapDialogSupplementary.close(false);
		} finally {
			// this.getCDRChargeInterface().createTerminateRecord();
			// TODO CDR?
		}
	}

	public abstract ChildRelation getHttpClientSbb();

	public void unsetSbbContext() {
		this.sbbContext = null;
		this.logger = null;
	}

	public void sbbCreate() throws CreateException {
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

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
import javax.slee.ChildRelation;
import javax.slee.SbbContext;
import javax.slee.SbbLocalObject;

import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.slee.resource.map.MAPContextInterfaceFactory;
import org.mobicents.ussdgateway.ShortCodeRoutingRuleManagement;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.UssdStatAggregator;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.rules.ScRoutingRuleType;

/**
 * 
 * @author amit bhayani
 */
public abstract class ParentSbb extends USSDBaseSbb {

	private static final ShortCodeRoutingRuleManagement shortCodeRoutingRuleManagement = ShortCodeRoutingRuleManagement
			.getInstance();

	private static final UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();

	/** Creates a new instance of CallSbb */
	public ParentSbb() {
		super("ParentSbb");
	}

	public void onDialogRequest(org.restcomm.slee.resource.map.events.DialogRequest evt, ActivityContextInterface aci) {
		if (logger.isFineEnabled()) {
			this.logger.fine("New MAP Dialog. Received event MAPOpenInfo " + evt);
		}

		MAPDialog mapDialog = evt.getMAPDialog();

		XmlMAPDialog dialog = new XmlMAPDialog(mapDialog.getApplicationContext(), mapDialog.getLocalAddress(),
				mapDialog.getRemoteAddress(), mapDialog.getLocalDialogId(), mapDialog.getRemoteDialogId(),
				evt.getDestReference(), evt.getOrigReference());
		dialog.setReturnMessageOnError(mapDialog.getReturnMessageOnError());
		dialog.setTCAPMessageType(mapDialog.getTCAPMessageType());
		dialog.setNetworkId(mapDialog.getNetworkId());

		this.setDialog(dialog);
	}

	public void onDialogTimeout(org.restcomm.slee.resource.map.events.DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled()) {
			this.logger.warning("Rx :  onDialogTimeout" + evt);
		}
	}

	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest evt, ActivityContextInterface aci) {

		try {
	        if (!this.checkMaxActivityCount(ussdPropertiesManagement.getMaxActivityCount())) {
                if (this.logger.isWarningEnabled()) {
                    this.logger
                            .warning(String
                                    .format("ProcessUnstructuredSSRequest is received and rejected because of too many active dialogs, more then maxActivityCount=%d",
                                            ussdPropertiesManagement.getMaxActivityCount()));
                }
                this.sendError(evt, ussdPropertiesManagement.getServerOverloadedMessage());

                super.ussdStatAggregator.updateDialogsAllFailed();
                super.ussdStatAggregator.updateDialogsPullFailed();
                return;
	        }

	        USSDString ussdStrObj = evt.getUSSDString();
			String shortCode = ussdStrObj.getString(null);

			if (this.logger.isFineEnabled()) {
				this.logger.fine(String.format("Received PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION=%s", evt));
			}
			int networkId = evt.getMAPDialog().getNetworkId();
			ScRoutingRule call = shortCodeRoutingRuleManagement.getScRoutingRule(shortCode, networkId);

			if (call == null) {

				if (this.logger.isWarningEnabled()) {
					this.logger.warning(String.format("No routing rule configured for short code=%s and network id=%d", shortCode, networkId));
				}
				this.sendError(evt, ussdPropertiesManagement.getNoRoutingRuleConfiguredMessage());

                super.ussdStatAggregator.updateDialogsAllFailed();
                super.ussdStatAggregator.updateDialogsPullFailed();
                super.ussdStatAggregator.updateUssdPullNoRoutingRule();
			} else {

		        super.ussdStatAggregator.addDialogsInProcess();

		        super.ussdStatAggregator.updateDialogsAllEstablished();
                super.ussdStatAggregator.updateDialogsPullEstablished();
                super.ussdStatAggregator.updateProcessUssdRequestOperations();

                super.ussdStatAggregator.updateRequestsPerUssdCode(call.getShortCode());

                super.ussdStatAggregator.updateMessagesRecieved();
                super.ussdStatAggregator.updateMessagesAll();

                if (call.getRuleType() == ScRoutingRuleType.HTTP) {
	                super.ussdStatAggregator.updateDialogsHttpEstablished();

					// Create child of Http SBB and call local method
					ChildRelation relation = this.getHttpClientSbb();
					ChildSbbLocalObject child = (ChildSbbLocalObject) relation.create();
					child.setCallFact(call);
					child.setXmlMAPDialog(this.getDialog());
					forwardEvent(child, aci);
				} else {
                    super.ussdStatAggregator.updateDialogsSipEstablished();

					// Create child of Sip SBB and call local method
					ChildRelation relation = this.getSipSbb();
					ChildSbbLocalObject child = (ChildSbbLocalObject) relation.create();
					child.setCallFact(call);
					child.setXmlMAPDialog(this.getDialog());
					forwardEvent(child, aci);
				}
			}

		} catch (Throwable e) {
			logger.severe("Unexpected error: ", e);
			// TODO: isolate try+catch per if/else
			// TODO:CDR
			// TODO: terminater dialog
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
        if (errorMssg != null) {
            if (errorMssg.length() > 160)
                errorMssg = errorMssg.substring(0, 160);
        } else {
            errorMssg = "sendError";
        }

		MAPDialogSupplementary mapDialogSupplementary = request.getMAPDialog();
		USSDString ussdString = mapParameterFactory.createUSSDString(errorMssg);
        CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(0x0f);
		mapDialogSupplementary.addProcessUnstructuredSSResponse(request.getInvokeId(), cbsDataCodingScheme,
				ussdString);
		try {
			mapDialogSupplementary.close(false);
		} finally {
			// this.getCDRChargeInterface().createTerminateRecord();
			// TODO CDR?
		}
	}

	public abstract ChildRelation getHttpClientSbb();

	public abstract ChildRelation getSipSbb();

	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);
		// overwrite logger
		this.logger = sbbContext.getTracer("USSD-Parent-" + getClass().getName());
		try {
			super.mapAcif = (MAPContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(mapRATypeID);
			super.mapProvider = (MAPProvider) super.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			super.mapParameterFactory = super.mapProvider.getMAPParameterFactory();
            super.ussdStatAggregator = UssdStatAggregator.getInstance();
		} catch (Exception ne) {
			super.logger.severe("Could not set SBB context:", ne);
		}
	}

	/**
	 * CMP
	 */
	public abstract void setDialog(XmlMAPDialog dialog);

	public abstract XmlMAPDialog getDialog();

}

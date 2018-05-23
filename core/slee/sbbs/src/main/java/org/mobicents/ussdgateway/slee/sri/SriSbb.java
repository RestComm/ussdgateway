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

package org.mobicents.ussdgateway.slee.sri;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.SbbContext;

import org.restcomm.protocols.ss7.indicator.NatureOfAddress;
import org.restcomm.protocols.ss7.indicator.NumberingPlan;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.dialog.MAPRefuseReason;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorCode;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.restcomm.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.restcomm.protocols.ss7.sccp.impl.parameter.ParameterFactoryImpl;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.ParameterFactory;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.restcomm.protocols.ss7.tcap.asn.ApplicationContextName;
import org.restcomm.protocols.ss7.tcap.asn.comp.Problem;
import org.restcomm.slee.resource.map.MAPContextInterfaceFactory;
import org.restcomm.slee.resource.map.events.DialogClose;
import org.restcomm.slee.resource.map.events.DialogDelimiter;
import org.restcomm.slee.resource.map.events.DialogProviderAbort;
import org.restcomm.slee.resource.map.events.DialogReject;
import org.restcomm.slee.resource.map.events.DialogTimeout;
import org.restcomm.slee.resource.map.events.DialogUserAbort;
import org.restcomm.slee.resource.map.events.ErrorComponent;
import org.restcomm.slee.resource.map.events.InvokeTimeout;
import org.restcomm.slee.resource.map.events.RejectComponent;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.UssdPropertiesManagementMBean;
import org.mobicents.ussdgateway.UssdStatAggregator;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.slee.USSDBaseSbb;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;

/**
 * SRI lookup SBB
 * 
 * @author baranowb
 * @author sergey vetyutnev
 * 
 */
public abstract class SriSbb extends USSDBaseSbb implements SriChild {

	// -------------------------------------------------------------
	// Helper fields, to avoid initialization... over and over and over
	// -------------------------------------------------------------
	protected UssdPropertiesManagementMBean ussdPropertiesManagement = null;

	protected ParameterFactory sccpParameterFact;

	public SriSbb() {
		super("SriSbb");
	}

	// -------------------------------------------------------------
	// SBB LO
	// -------------------------------------------------------------
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.sri.SriChild#performSRILookup(java.lang
	 * .String)
	 */
	@Override
	public void performSRIQuery(String msisdn, XmlMAPDialog xmlMapDialog) throws Exception {
		if (super.logger.isFineEnabled())
			super.logger.fine("Perform SRI on '" + msisdn + "'");

		this.setXmlMAPDialog(xmlMapDialog);
        this.setMsisdnCMP(msisdn);

        this.performSRIQuery(msisdn, getSriMAPApplicationContext());
	}

	public void performSRIQuery(String msisdn, MAPApplicationContext desiredContext) throws MAPException {
		if (super.logger.isFineEnabled())
			super.logger.fine("Perform SRI on '" + msisdn + "', '" + desiredContext + "'");

		MAPDialogSms mapDialogSms = null;
		int networkId = this.getXmlMAPDialog().getNetworkId();
		try {
			String hlrAddress = msisdn;
            if (ussdPropertiesManagement.getHrHlrGt() != null && ussdPropertiesManagement.getHrHlrGt().length() > 0
                    && !ussdPropertiesManagement.getHrHlrGt().equals("-1")) {
                hlrAddress = ussdPropertiesManagement.getHrHlrGt();
			}
			SccpAddress destAddress = this.convertAddressFieldToSCCPAddress(hlrAddress);

			if (super.logger.isFinestEnabled()) {
				super.logger.finest("Creating dialog Context '" + desiredContext + "',origAddress '"
						+ this.getUssdGwSccpAddress(networkId) + "', destAddress '" + destAddress + "'");
			}

			mapDialogSms = this.mapProvider.getMAPServiceSms().createNewDialog(desiredContext,
					this.getUssdGwSccpAddress(networkId), null, destAddress, null);
			mapDialogSms.setNetworkId(networkId);

			mapDialogSms.addSendRoutingInfoForSMRequest(this.getCalledPartyISDNAddressString(msisdn), true,
					this.getUssdGwAddress(networkId), null, false, null, null, null, false, null, false, false, null, null);

			// 2. Create the ACI and attach this SBB
			ActivityContextInterface sriDialogACI = this.mapAcif.getActivityContextInterface(mapDialogSms);
			sriDialogACI.attach(this.sbbContext.getSbbLocalObject());
			// 3. Finally send the request
			mapDialogSms.send();
		} catch (MAPException e) {
			if (logger.isSevereEnabled())
				logger.severe("Error while trying to send SendRoutingInfoForSMRequest", e);
			// something horrible, release MAPDialog and free resources

			if (mapDialogSms != null) {
				mapDialogSms.release();
			}

			throw e;
		}
	}

    // -------------------------------------------------------------
    // MAP event handlers: SRI
    // -------------------------------------------------------------

    /**
     * Received response for SRI sent earlier
     * 
     * @param evt
     * @param aci
     */
    public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse evt, ActivityContextInterface aci) {
        if (this.logger.isFineEnabled()) {
            this.logger.fine("Received SEND_ROUTING_INFO_FOR_SM_RESPONSE = " + evt + " Dialog=" + evt.getMAPDialog());
        }

        this.setSendRoutingInfoForSMResponse(evt);
    }

	// -------------------------------------------------------------
	// MAP event handlers: regular
	// -------------------------------------------------------------

    public void onDialogDelimiter(DialogDelimiter evt, ActivityContextInterface aci) {
        try {
            this.onSriFullResponse(evt.getMAPDialog().getTCAPMessageType(), aci);
            evt.getMAPDialog().close(false);
        } catch (Throwable e1) {
            logger.severe("Exception in SriSbb.onDialogDelimiter when fetching records and issuing events: " + e1.getMessage(), e1);
        }
    }

    public void onDialogClose(DialogClose evt, ActivityContextInterface aci) {
        try {
            this.onSriFullResponse(evt.getMAPDialog().getTCAPMessageType(), aci);
        } catch (Throwable e1) {
            logger.severe("Exception in SriSbb.onDialogClose when fetching records and issuing events: " + e1.getMessage(), e1);
        }
    }

    private void onSriFullResponse(MessageType messageType, ActivityContextInterface aci) {
        SendRoutingInfoForSMResponse evt = this.getSendRoutingInfoForSMResponse();

        if (evt != null) {
            detachFromCurrentDialog(aci);

            SriSbbLocalObject local = (SriSbbLocalObject) super.sbbContext.getSbbLocalObject();
            SriParent parent = (SriParent) local.getParent();
            parent.onSRIResult(local, evt.getIMSI(), evt.getLocationInfoWithLMSI());
        } else {
            XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
            xmlMAPDialog.reset();
            MAPErrorMessage errorComponent = this.getErrorComponent();
            RecordStatus recordStatus = RecordStatus.FAILED_SYSTEM_FAILURE;
            try {
                if (errorComponent != null) {
                    xmlMAPDialog.sendErrorComponent(this.getErrorInvokeId(), errorComponent);

                    switch ((int) (long) (errorComponent.getErrorCode())) {
                    case MAPErrorCode.absentSubscriber:
                    case MAPErrorCode.absentSubscriberSM:
                        super.ussdStatAggregator.updateMapErrorAbsentSubscribers();
                        recordStatus = RecordStatus.SRI_ABSENT_SUBSCRIBER;
                        break;
                    case MAPErrorCode.callBarred:
                        super.ussdStatAggregator.updateMapErrorCallBarred();
                        recordStatus = RecordStatus.SRI_CALL_BARRED;
                        break;
                    case MAPErrorCode.teleserviceNotProvisioned:
                        super.ussdStatAggregator.updateMapErrorTeleserviceNotProvisioned();
                        recordStatus = RecordStatus.SRI_TELESERVICE_NOT_PROVISIONED;
                        break;
                    case MAPErrorCode.unknownSubscriber:
                        super.ussdStatAggregator.updateMapErrorUnknownSubscriber();
                        recordStatus = RecordStatus.SRI_UNKNOWN_SUBSCRIBER;
                        break;
                    default:
                        super.ussdStatAggregator.updateMapErrorComponentOther();
                        recordStatus = RecordStatus.SRI_MAP_ERROR_COMPONENT;
                        break;
                    }
                }
                Problem rejectProblem = this.getRejectProblem();
                if (rejectProblem != null) {
                    xmlMAPDialog.sendRejectComponent(this.getRejectInvokeId(), rejectProblem);
                    recordStatus = RecordStatus.SRI_MAP_REJECT_COMPONENT;
                }
            } catch (MAPException e) {
                // can not be Exception here
            }
            xmlMAPDialog.setTCAPMessageType(messageType);

            sendErrorToParent(xmlMAPDialog, recordStatus);
        }
    }

    public void onInvokeTimeout(InvokeTimeout evt, ActivityContextInterface aci) {
		// TODO:
		logger.warning("Invoke timeout received:" + evt);
		// this.sendErrorToParent();
	}

	public void onErrorComponent(ErrorComponent event, ActivityContextInterface aci) {
        if (logger.isWarningEnabled())
            logger.warning("Error component received:" + event);

        this.setErrorComponent(event.getMAPErrorMessage());
        this.setErrorInvokeId(event.getInvokeId());
	}

	public void onRejectComponent(RejectComponent event, ActivityContextInterface aci) {
        if (logger.isWarningEnabled())
            logger.warning("Reject component received:" + event);

        this.setRejectProblem(event.getProblem());
        this.setRejectInvokeId(event.getInvokeId());
	}

	public void onDialogReject(DialogReject evt, ActivityContextInterface aci) {

		MAPRefuseReason mapRefuseReason = evt.getRefuseReason();
		if (super.logger.isFineEnabled())
			super.logger.fine("Dialog rejected because '" + mapRefuseReason + "'");

		// If ACN not supported, lets use the new one suggested
		if (mapRefuseReason == MAPRefuseReason.ApplicationContextNotSupported) {
			detachFromCurrentDialog(aci);

			// Now send new SRI with supported ACN
			ApplicationContextName tcapApplicationContextName = evt.getAlternativeApplicationContext();
			MAPApplicationContext supportedMAPApplicationContext = MAPApplicationContext
					.getInstance(tcapApplicationContextName.getOid());

			try {
				this.performSRIQuery(getMsisdnCMP(), supportedMAPApplicationContext);
				return;
			} catch (MAPException e) {
				logger.severe("Error while trying to performSRIQuery", e);

			}
		}

		super.logger.severe("Dialog Rejected " + evt);

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setMapRefuseReason(evt.getRefuseReason());
		xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

        this.sendErrorToParent(xmlMAPDialog, RecordStatus.SRI_DIALOG_REJECTED);
	}

	public void onDialogUserAbort(DialogUserAbort evt, ActivityContextInterface aci) {
		if (super.logger.isWarningEnabled())
			super.logger.warning("User abort received: " + evt);
		try {
			XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
			xmlMAPDialog.reset();
			xmlMAPDialog.abort(evt.getUserReason());
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

            this.sendErrorToParent(xmlMAPDialog, RecordStatus.SRI_DIALOG_USER_ABORT);
		} catch (MAPException e) {
			logger.severe("Error while trying to send back MAPUserAbortChoice to HTTP App", e);
		}
	}

	public void onDialogProviderAbort(DialogProviderAbort evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled())
			logger.warning("Provider abort received: " + evt);

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setMapAbortProviderReason(evt.getAbortProviderReason());
		xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());

        this.sendErrorToParent(xmlMAPDialog, RecordStatus.SRI_PROVIDER_ABORT);
	}

	public void onDialogTimeout(DialogTimeout evt, ActivityContextInterface aci) {
		if (logger.isWarningEnabled())
			logger.warning("DialogTimeout received: " + evt);

		XmlMAPDialog xmlMAPDialog = this.getXmlMAPDialog();
		xmlMAPDialog.reset();
		xmlMAPDialog.setDialogTimedOut(true);
		if (evt.getMAPDialog().getTCAPMessageType() != null) {
			// TODO : MAP RA is setting MessaegType to null if Dialog is sent
			// and remote side never responded, fix this in RA
			xmlMAPDialog.setTCAPMessageType(evt.getMAPDialog().getTCAPMessageType());
		}

        super.ussdStatAggregator.updateMapDialogTimeouts();

		this.sendErrorToParent(xmlMAPDialog, RecordStatus.SRI_DIALOG_TIMEOUT);
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
		super.logger = sbbContext.getTracer("SRI-" + getClass().getName());
		try {
			super.mapAcif = (MAPContextInterfaceFactory) super.sbbContext
					.getActivityContextInterfaceFactory(mapRATypeID);
			super.mapProvider = (MAPProvider) super.sbbContext.getResourceAdaptorInterface(mapRATypeID, mapRaLink);
			super.mapParameterFactory = super.mapProvider.getMAPParameterFactory();
            super.ussdStatAggregator = UssdStatAggregator.getInstance();

			this.ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
			this.sccpParameterFact = new ParameterFactoryImpl();
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
	}

	// -------------------------------------------------------------
	// SLEE: CMPs
	// -------------------------------------------------------------
	public abstract void setXmlMAPDialog(XmlMAPDialog dialog);

	public abstract XmlMAPDialog getXmlMAPDialog();

	public abstract void setMsisdnCMP(String msisdn);

	public abstract String getMsisdnCMP();

	// address CMP stuff

	public abstract void setMAPApplicationContextCMP(MAPApplicationContext ctx);

	public abstract MAPApplicationContext getMAPApplicationContextCMP();

	public abstract void setUssdGwAddressCMP(AddressString gwAddress);

	public abstract AddressString getUssdGwAddressCMP();

	public abstract void setUssdGwSCCPAddressCMP(SccpAddress gwSccpAddress);

	public abstract SccpAddress getUssdGwSCCPAddressCMP();

    public abstract void setSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse sendRoutingInfoForSMResponse);

    public abstract SendRoutingInfoForSMResponse getSendRoutingInfoForSMResponse();

    public abstract void setErrorComponent(MAPErrorMessage errorComponent);

    public abstract MAPErrorMessage getErrorComponent();

    public abstract void setErrorInvokeId(long errorInvokeId);

    public abstract long getErrorInvokeId();

    public abstract void setRejectProblem(Problem rejectProblem);

    public abstract Problem getRejectProblem();

    public abstract void setRejectInvokeId(long rejectInvokeId);

    public abstract long getRejectInvokeId();

	// -------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------

	protected ISDNAddressString getCalledPartyISDNAddressString(String destinationAddress) {
		return super.mapParameterFactory.createISDNAddressString(AddressNature.international_number,
				org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.ISDN, destinationAddress);
	}

	protected SccpAddress convertAddressFieldToSCCPAddress(String address) {
		GlobalTitle gt = sccpParameterFact.createGlobalTitle(address, 0, NumberingPlan.ISDN_TELEPHONY, null,
				NatureOfAddress.INTERNATIONAL);
		return sccpParameterFact.createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0,
				ussdPropertiesManagement.getHlrSsn());
	}

	protected MAPApplicationContext getSriMAPApplicationContext() throws MAPException {
		MAPApplicationContext ctx = this.getMAPApplicationContextCMP();
		if (ctx == null) {
            ctx = MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgGatewayContext,
                    MAPApplicationContextVersion.getInstance(this.ussdPropertiesManagement.getMaxMapVersion()));
            if (ctx == null) {
                throw new MAPException("Not suitable context: " + MAPApplicationContextName.shortMsgGatewayContext + " for "
                        + this.ussdPropertiesManagement.getMaxMapVersion());
            }

			this.setMAPApplicationContextCMP(ctx);
		}
		return ctx;
	}

	protected AddressString getUssdGwAddress(int networkId) {
		AddressString address = this.getUssdGwAddressCMP();
		if (address == null) {
			address = this.mapParameterFactory.createAddressString(AddressNature.international_number,
					// TODO: getUssdGWGt seems wrong here?
					org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan.ISDN,
					ussdPropertiesManagement.getUssdGt(networkId));
			this.setUssdGwAddressCMP(address);
		}
		return address;
	}

	protected SccpAddress getUssdGwSccpAddress(int networkId) {
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

	protected void detachFromCurrentDialog(ActivityContextInterface aci) {
		aci.detach(super.sbbContext.getSbbLocalObject());
		MAPDialogSms mapDialogSms = (MAPDialogSms) aci.getActivity();
		mapDialogSms.release();
	}

    private void sendErrorToParent(XmlMAPDialog xmlMAPDialog, RecordStatus recordStatus) {
        xmlMAPDialog.setSriPart(true);
        SriSbbLocalObject local = (SriSbbLocalObject) super.sbbContext.getSbbLocalObject();
        SriParent parent = (SriParent) local.getParent();
        parent.onSriError(xmlMAPDialog, recordStatus);
    }
}

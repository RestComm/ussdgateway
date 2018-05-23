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

package org.mobicents.ussd.ihub;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPDialogListener;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.dialog.MAPAbortProviderReason;
import org.restcomm.protocols.ss7.map.api.dialog.MAPAbortSource;
import org.restcomm.protocols.ss7.map.api.dialog.MAPNoticeProblemDiagnostic;
import org.restcomm.protocols.ss7.map.api.dialog.MAPRefuseReason;
import org.restcomm.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.restcomm.protocols.ss7.map.api.errors.AbsentSubscriberDiagnosticSM;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageFactory;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.sms.AlertServiceCentreRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.AlertServiceCentreResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.ForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.InformServiceCentreRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;
import org.restcomm.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.restcomm.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;
import org.restcomm.protocols.ss7.map.api.service.sms.MoForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.MoForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.MtForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.MtForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.NoteSubscriberPresentRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReadyForSMRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReadyForSMResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.SmsSignalInfo;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ActivateSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ActivateSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.DeactivateSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.DeactivateSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.EraseSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.EraseSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.GetPasswordRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.GetPasswordResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.InterrogateSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.InterrogateSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPServiceSupplementaryListener;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.RegisterPasswordRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.RegisterPasswordResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.RegisterSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.RegisterSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.smstpdu.AddressField;
import org.restcomm.protocols.ss7.map.api.smstpdu.SmsSubmitTpdu;
import org.restcomm.protocols.ss7.map.api.smstpdu.SmsTpdu;
import org.restcomm.protocols.ss7.map.api.smstpdu.SmsTpduType;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.protocols.ss7.map.primitives.IMSIImpl;
import org.restcomm.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.map.service.sms.LocationInfoWithLMSIImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.tcap.asn.ApplicationContextName;
import org.restcomm.protocols.ss7.tcap.asn.comp.Problem;

/**
 * 
 * 
 * @author Amit Bhayani
 * 
 */
public class MAPListener implements MAPDialogListener, MAPServiceSmsListener, MAPServiceSupplementaryListener {

	private static final Logger logger = Logger.getLogger(MAPListener.class);

	private MAPSimulator iHubManagement = null;

	private final AtomicLong mapMessagesReceivedCounter = new AtomicLong(0);
	private long currentMapMessageCount = 0;

	private final MAPErrorMessageFactory mAPErrorMessageFactory;

	private long processUnstSSReqInvokeId = 0l;

	protected MAPListener(MAPSimulator iHubManagement) {
		this.iHubManagement = iHubManagement;
		this.mAPErrorMessageFactory = this.iHubManagement.getMapProvider().getMAPErrorMessageFactory();
	}

	/**
	 * Dialog Listener
	 */

	@Override
	public void onDialogAccept(MAPDialog arg0, MAPExtensionContainer arg1) {
		logger.info("onDialogAccept " + arg0);
	}

	@Override
	public void onDialogClose(MAPDialog arg0) {
		logger.info("onDialogClose " + arg0);
	}

	@Override
	public void onDialogDelimiter(MAPDialog arg0) {
		logger.info("onDialogDelimiter " + arg0);
	}

	@Override
	public void onDialogNotice(MAPDialog arg0, MAPNoticeProblemDiagnostic arg1) {
		logger.warn("onDialogNotice " + arg0);
	}

	@Override
	public void onDialogProviderAbort(MAPDialog arg0, MAPAbortProviderReason arg1, MAPAbortSource arg2,
			MAPExtensionContainer arg3) {
		logger.error("onDialogProviderAbort " + arg0);
	}

	@Override
	public void onDialogReject(MAPDialog mapDialog, MAPRefuseReason refuseReason,
			ApplicationContextName alternativeApplicationContext, MAPExtensionContainer extensionContainer) {
		logger.error("onDialogReject " + mapDialog);
	}

	@Override
	public void onDialogRelease(MAPDialog arg0) {
		logger.info("onDialogRelease " + arg0);
	}

	@Override
	public void onDialogRequest(MAPDialog arg0, AddressString arg1, AddressString arg2, MAPExtensionContainer arg3) {
		logger.info("onDialogRequest " + arg0);
		this.currentMapMessageCount = this.mapMessagesReceivedCounter.incrementAndGet();
	}

	@Override
	public void onDialogRequestEricsson(MAPDialog arg0, AddressString arg1, AddressString arg2, AddressString arg3,
			AddressString arg4) {
		logger.info("onDialogRequestEricsson " + arg0);
	}

	@Override
	public void onDialogTimeout(MAPDialog arg0) {
		logger.error("onDialogTimeout " + arg0);
	}

	@Override
	public void onDialogUserAbort(MAPDialog arg0, MAPUserAbortChoice arg1, MAPExtensionContainer arg2) {
		logger.error("onDialogUserAbort " + arg0);
	}

	/**
	 * Component Listener
	 */

	@Override
	public void onErrorComponent(MAPDialog arg0, Long arg1, MAPErrorMessage arg2) {
		logger.error("onErrorComponent " + arg0);
	}

	@Override
	public void onInvokeTimeout(MAPDialog arg0, Long arg1) {
		logger.error("onInvokeTimeout " + arg0);
	}

	@Override
	public void onMAPMessage(MAPMessage arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRejectComponent(MAPDialog arg0, Long arg1, Problem arg2, boolean isLocalOriginated) {
		logger.error("onRejectComponent " + arg0);
	}

	/**
	 * SMS Listener
	 */

	@Override
	public void onAlertServiceCentreRequest(AlertServiceCentreRequest arg0) {
		logger.info("onAlertServiceCentreRequest " + arg0);
	}

	@Override
	public void onAlertServiceCentreResponse(AlertServiceCentreResponse arg0) {
		logger.info("onAlertServiceCentreResponse " + arg0);
	}

	@Override
	public void onForwardShortMessageRequest(ForwardShortMessageRequest event) {
		if (logger.isInfoEnabled()) {
			logger.info("Rx : onForwardShortMessageRequest=" + event);
		}

		// Lets first close the Dialog
		MAPDialogSms mapDialogSms = event.getMAPDialog();

		if (this.currentMapMessageCount % 7 == 0) {
			// Send back AbsentSubscriber for every 7th MtSMS
			try {
				MAPErrorMessage mapErrorMessage = mAPErrorMessageFactory.createMAPErrorMessageAbsentSubscriberSM(
						AbsentSubscriberDiagnosticSM.IMSIDetached, null, null);
				mapDialogSms.sendErrorComponent(event.getInvokeId(), mapErrorMessage);
				mapDialogSms.close(false);
			} catch (MAPException e) {
				logger.error("Error while sending MAPErrorMessageAbsentSubscriberSM ", e);
			}
		} else {

			try {
				mapDialogSms.addForwardShortMessageResponse(event.getInvokeId());
				mapDialogSms.close(false);
			} catch (MAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onForwardShortMessageResponse(ForwardShortMessageResponse arg0) {
		logger.info("onForwardShortMessageResponse " + arg0);
	}

	@Override
	public void onInformServiceCentreRequest(InformServiceCentreRequest arg0) {
		logger.info("onInformServiceCentreRequest " + arg0);
	}

	@Override
	public void onMoForwardShortMessageRequest(MoForwardShortMessageRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Rx : MoForwardShortMessageRequestIndication=" + request);
		}

		MAPDialogSms dialog = request.getMAPDialog();

		try {
			// TODO Should we add PENDING SMS TPDU here itself?
			dialog.addMoForwardShortMessageResponse(request.getInvokeId(), null, null);
			dialog.close(false);
		} catch (MAPException e) {
			logger.error("Error while sending MoForwardShortMessageResponse ", e);
		}

		try {
			SmsSignalInfo smsSignalInfo = request.getSM_RP_UI();
			SmsTpdu smsTpdu = smsSignalInfo.decodeTpdu(true);

			if (smsTpdu.getSmsTpduType() != SmsTpduType.SMS_SUBMIT) {
				// TODO : Error, we should always receive SMS_SUBMIT for
				// MoForwardShortMessageRequestIndication
				logger.error("Rx : MoForwardShortMessageRequestIndication, but SmsTpduType is not SMS_SUBMIT. SmsTpdu="
						+ smsTpdu);
				return;
			}

			SmsSubmitTpdu smsSubmitTpdu = (SmsSubmitTpdu) smsTpdu;
			AddressField destinationAddress = smsSubmitTpdu.getDestinationAddress();

			// TODO Normalize

		} catch (MAPException e1) {
			logger.error("Error while decoding SmsSignalInfo ", e1);
		}
	}

	@Override
	public void onMoForwardShortMessageResponse(MoForwardShortMessageResponse arg0) {
		logger.info("onMoForwardShortMessageResponse " + arg0);
	}

	@Override
	public void onMtForwardShortMessageRequest(MtForwardShortMessageRequest event) {
		if (logger.isInfoEnabled()) {
			logger.info("Rx : onMtForwardShortMessageIndication=" + event);
		}

		// Lets first close the Dialog
		MAPDialogSms mapDialogSms = event.getMAPDialog();

		if (this.currentMapMessageCount % 7 == 0) {
			// Send back AbsentSubscriber for every 7th MtSMS
			try {
				MAPErrorMessage mapErrorMessage = mAPErrorMessageFactory.createMAPErrorMessageAbsentSubscriberSM(
						AbsentSubscriberDiagnosticSM.IMSIDetached, null, null);
				mapDialogSms.sendErrorComponent(event.getInvokeId(), mapErrorMessage);
				mapDialogSms.close(false);
			} catch (MAPException e) {
				logger.error("Error while sending MAPErrorMessageAbsentSubscriberSM ", e);
			}
		} else {

			try {
				mapDialogSms.addMtForwardShortMessageResponse(event.getInvokeId(), null, null);
				mapDialogSms.close(false);
			} catch (MAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onMtForwardShortMessageResponse(MtForwardShortMessageResponse arg0) {
		logger.info("onMtForwardShortMessageResponse " + arg0);
	}

	@Override
	public void onReportSMDeliveryStatusRequest(ReportSMDeliveryStatusRequest arg0) {
		logger.info("onReportSMDeliveryStatusRequest " + arg0);
	}

	@Override
	public void onReportSMDeliveryStatusResponse(ReportSMDeliveryStatusResponse arg0) {
		logger.info("onReportSMDeliveryStatusResponse " + arg0);
	}

	@Override
	public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest event) {
		if (logger.isInfoEnabled()) {
			logger.info("Rx : SendRoutingInfoForSMRequestIndication=" + event);
		}

		MAPDialogSms mapDialogSms = event.getMAPDialog();

//		try {
//			MAPUserAbortChoice mapUserAbortChoice = new MAPUserAbortChoiceImpl();
//			mapUserAbortChoice.setUserSpecificReason();
//
//			mapDialogSms.abort(mapUserAbortChoice);
//		} catch (MAPException e) {
//			e.printStackTrace();
//		}

		IMSI imsi = new IMSIImpl("410035001692061");
		ISDNAddressString nnn = new ISDNAddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"923330052001");

		LocationInfoWithLMSI li = new LocationInfoWithLMSIImpl(nnn, null, null, false, null);

		try {
			mapDialogSms.addSendRoutingInfoForSMResponse(event.getInvokeId(), imsi, li, null, null, null);
			mapDialogSms.close(false);
		} catch (MAPException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse arg0) {
		logger.info("onSendRoutingInfoForSMResponse " + arg0);
	}

	/**
	 * USSD Listener methods
	 */

	@Override
	public void onProcessUnstructuredSSRequest(ProcessUnstructuredSSRequest req) {
		logger.info("onProcessUnstructuredSSRequest " + req);

		this.processUnstSSReqInvokeId = req.getInvokeId();

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		USSDStringImpl ussdStr = new USSDStringImpl(
				"USSD String : Hello World\n 1. Balance\n 2. Texts Remaining".getBytes(), cbsDataCodingScheme);
		UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(cbsDataCodingScheme,
				ussdStr, null, null);

		MAPDialogSupplementary mapDialog = req.getMAPDialog();
		try {
			mapDialog.addUnstructuredSSRequest(cbsDataCodingScheme, ussdStr, null, null);
			mapDialog.send();
		} catch (MAPException e) {
			logger.error("Error while trying to send UnstructuredSSRequest to remote", e);
		}

	}

	@Override
	public void onProcessUnstructuredSSResponse(ProcessUnstructuredSSResponse arg0) {
		logger.info("onProcessUnstructuredSSResponse " + arg0);
	}

	@Override
	public void onUnstructuredSSNotifyRequest(UnstructuredSSNotifyRequest event) {
		if (logger.isInfoEnabled()) {
			logger.info("Rx : onUnstructuredSSNotifyRequest=" + event);
		}

		MAPDialogSupplementary mapDialogSupplementary = event.getMAPDialog();
		try {
			mapDialogSupplementary.addUnstructuredSSNotifyResponse(event.getInvokeId());
			mapDialogSupplementary.send();
		} catch (MAPException e) {
			logger.error("Error while trying to send the UnstructuredSSNotifyResponse for Dialog"
					+ mapDialogSupplementary, e);
		}

	}

	@Override
	public void onUnstructuredSSNotifyResponse(UnstructuredSSNotifyResponse event) {
		logger.info("onUnstructuredSSNotifyResponse " + event);
	}

	@Override
	public void onUnstructuredSSRequest(UnstructuredSSRequest event) {
		if (logger.isInfoEnabled()) {
			logger.info("Rx : onUnstructuredSSRequest=" + event);
		}

		MAPDialogSupplementary mapDialogSupplementary = event.getMAPDialog();
		try {
			USSDString ussdStr = new USSDStringImpl("1", event.getDataCodingScheme(), null);

			mapDialogSupplementary.addUnstructuredSSResponse(event.getInvokeId(), event.getDataCodingScheme(), ussdStr);
			mapDialogSupplementary.send();
		} catch (MAPException e) {
			logger.error("Error while trying to send UnstructuredSSResponse", e);
		}
	}

	@Override
	public void onUnstructuredSSResponse(UnstructuredSSResponse evt) {
		logger.info("onUnstructuredSSResponse " + evt);
		try {
			USSDString ussdStr = new USSDStringImpl("Thank You!", evt.getDataCodingScheme(), null);

			MAPDialogSupplementary mapDialogSupplementary = evt.getMAPDialog();
			mapDialogSupplementary.addProcessUnstructuredSSResponse(this.processUnstSSReqInvokeId,
					evt.getDataCodingScheme(), ussdStr);
			mapDialogSupplementary.close(false);
		} catch (MAPException e) {
			logger.error("Error while trying to send ProcessUnstructuredSSResponse", e);
		}

	}

    @Override
    public void onRegisterSSRequest(RegisterSSRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRegisterSSResponse(RegisterSSResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEraseSSRequest(EraseSSRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEraseSSResponse(EraseSSResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivateSSRequest(ActivateSSRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onActivateSSResponse(ActivateSSResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDeactivateSSRequest(DeactivateSSRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDeactivateSSResponse(DeactivateSSResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onInterrogateSSRequest(InterrogateSSRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onInterrogateSSResponse(InterrogateSSResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGetPasswordRequest(GetPasswordRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onGetPasswordResponse(GetPasswordResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRegisterPasswordRequest(RegisterPasswordRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onRegisterPasswordResponse(RegisterPasswordResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onReadyForSMRequest(ReadyForSMRequest request) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onReadyForSMResponse(ReadyForSMResponse response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onNoteSubscriberPresentRequest(NoteSubscriberPresentRequest request) {
        // TODO Auto-generated method stub
        
    }
}

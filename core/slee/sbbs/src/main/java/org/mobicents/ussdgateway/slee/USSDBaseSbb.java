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
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.util.FastList;
import net.java.client.slee.resource.http.HttpClientActivityContextInterfaceFactory;
import net.java.client.slee.resource.http.HttpClientResourceAdaptorSbbInterface;
import net.java.slee.resource.http.HttpServletRaActivityContextInterfaceFactory;
import net.java.slee.resource.http.HttpServletRaSbbInterface;

import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPParameterFactory;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.mobicents.slee.SbbContextExt;
import org.restcomm.slee.resource.jdbc.JdbcActivityContextInterfaceFactory;
import org.restcomm.slee.resource.jdbc.JdbcResourceAdaptorSbbInterface;
import org.restcomm.slee.resource.map.MAPContextInterfaceFactory;
import org.mobicents.ussdgateway.UssdStatAggregator;
import org.mobicents.ussdgateway.XmlMAPDialog;

/**
 * Simple base SBB class. Its meant to have all the basics for GW SBBS(SLEE
 * basics, RA IDs, RA link, RA interfaces) . Other SBBs should extend it. This
 * class puts some compile time deps on other classes, however its easier to
 * maintain everything in one place.
 * 
 * SBB which extends this class should initialize RA references it wants to use(
 * by providing extension to setSbbContext method for instance)
 * 
 * @author baranowb
 * 
 */
public class USSDBaseSbb implements Sbb {
	
	private final String loggerName;

	// -------------------------------------------------------------
	// SLEE STUFF
	// -------------------------------------------------------------
	protected SbbContextExt sbbContext;

	protected Tracer logger;

	// -------------------------------------------------------------
	// MAP RA STUFF
	// -------------------------------------------------------------
	protected static final ResourceAdaptorTypeID mapRATypeID = new ResourceAdaptorTypeID("MAPResourceAdaptorType",
			"org.mobicents", "2.0");
	protected static final String mapRaLink = "MAPRA";
	protected MAPContextInterfaceFactory mapAcif;
	protected MAPProvider mapProvider;
	protected MAPParameterFactory mapParameterFactory;

	// -------------------------------------------------------------
	// JDBC RA STUFF
	// -------------------------------------------------------------
	protected static final ResourceAdaptorTypeID JDBC_RESOURCE_ADAPTOR_ID = JdbcResourceAdaptorSbbInterface.RATYPE_ID;
	protected static final String JDBC_RA_LINK = "JDBCRA";
	protected JdbcResourceAdaptorSbbInterface jdbcRA;
	protected JdbcActivityContextInterfaceFactory jdbcACIF;

	// -------------------------------------------------------------
	// HTTP Client RA STUFF
	// -------------------------------------------------------------
	protected static ResourceAdaptorTypeID httpClientRATypeID;
	protected static final String httpClientRaLink = "HttpClientResourceAdaptor";

	protected HttpClientActivityContextInterfaceFactory httpClientActivityContextInterfaceFactory;
	protected HttpClientResourceAdaptorSbbInterface httpClientProvider;

	// -------------------------------------------------------------
	// HTTP Server RA STUFF
	// -------------------------------------------------------------
	protected static ResourceAdaptorTypeID httpServerRATypeID;
	protected static final String httpServerRaLink = "HttpServletRA";

	protected HttpServletRaSbbInterface httpServletProvider;
	protected HttpServletRaActivityContextInterfaceFactory httpServletRaActivityContextInterfaceFactory;

	public USSDBaseSbb(String loggerName) {
		super();
		this.loggerName = loggerName;
	}

    // -------------------------------------------------------------
    // Statistics STUFF
    // -------------------------------------------------------------
    protected UssdStatAggregator ussdStatAggregator;

	// -------------------------------------------------------------
	// SLEE minimal STUFF
	// -------------------------------------------------------------
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = (SbbContextExt) sbbContext;
		this.logger = sbbContext.getTracer(this.loggerName);
	}

	public void unsetSbbContext() {
		// clean RAs
		this.mapAcif = null;
		this.mapProvider = null;
        this.mapParameterFactory = null;
        this.ussdStatAggregator = null;

		this.jdbcRA = null;
		this.jdbcACIF = null;

		this.httpClientActivityContextInterfaceFactory = null;
		this.httpClientProvider = null;

		this.httpServletProvider = null;
		this.httpServletRaActivityContextInterfaceFactory = null;

		// clean SLEE
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

	protected void processXmlMAPDialog(XmlMAPDialog xmlMAPDialog, MAPDialogSupplementary mapDialog)
			throws MAPException {
        FastList<MAPMessage> mapMessages = xmlMAPDialog.getMAPMessages();
        if (mapMessages != null) {
            for (FastList.Node<MAPMessage> n = mapMessages.head(), end = mapMessages.tail(); (n = n.getNext()) != end;) {
                Long invokeId = this.processMAPMessageFromApplication(n.getValue(), mapDialog, xmlMAPDialog.getCustomInvokeTimeOut());
            }
        }
	}

	protected Long processMAPMessageFromApplication(MAPMessage mapMessage,
			MAPDialogSupplementary mapDialogSupplementary, Integer customInvokeTimeout) throws MAPException {
		switch (mapMessage.getMessageType()) {
		case unstructuredSSRequest_Request:
			UnstructuredSSRequest unstructuredSSRequest = (UnstructuredSSRequest) mapMessage;
			if (customInvokeTimeout != null) {
				return mapDialogSupplementary.addUnstructuredSSRequest(customInvokeTimeout,
						unstructuredSSRequest.getDataCodingScheme(), unstructuredSSRequest.getUSSDString(),
						unstructuredSSRequest.getAlertingPattern(), unstructuredSSRequest.getMSISDNAddressString());
			}
			return mapDialogSupplementary.addUnstructuredSSRequest(unstructuredSSRequest.getDataCodingScheme(),
					unstructuredSSRequest.getUSSDString(), unstructuredSSRequest.getAlertingPattern(),
					unstructuredSSRequest.getMSISDNAddressString());
		case unstructuredSSRequest_Response:
			UnstructuredSSResponse unstructuredSSResponse = (UnstructuredSSResponse) mapMessage;
			mapDialogSupplementary.addUnstructuredSSResponse(unstructuredSSResponse.getInvokeId(),
					unstructuredSSResponse.getDataCodingScheme(), unstructuredSSResponse.getUSSDString());
			break;

		case processUnstructuredSSRequest_Response:
			ProcessUnstructuredSSResponse processUnstructuredSSResponse = (ProcessUnstructuredSSResponse) mapMessage;
			mapDialogSupplementary.addProcessUnstructuredSSResponse(processUnstructuredSSResponse.getInvokeId(),
					processUnstructuredSSResponse.getDataCodingScheme(), processUnstructuredSSResponse.getUSSDString());
			return processUnstructuredSSResponse.getInvokeId();
		case unstructuredSSNotify_Request:
			// notify, this means dialog will end;
			final UnstructuredSSNotifyRequest ntfyRequest = (UnstructuredSSNotifyRequest) mapMessage;
			if (customInvokeTimeout != null) {
				return mapDialogSupplementary.addUnstructuredSSNotifyRequest(customInvokeTimeout,
						ntfyRequest.getDataCodingScheme(), ntfyRequest.getUSSDString(),
						ntfyRequest.getAlertingPattern(), ntfyRequest.getMSISDNAddressString());
			}
			return mapDialogSupplementary
					.addUnstructuredSSNotifyRequest(ntfyRequest.getDataCodingScheme(), ntfyRequest.getUSSDString(),
							ntfyRequest.getAlertingPattern(), ntfyRequest.getMSISDNAddressString());
		case unstructuredSSNotify_Response:
			// notify, this means dialog will end;
			final UnstructuredSSNotifyResponse ntfyResponse = (UnstructuredSSNotifyResponse) mapMessage;
			mapDialogSupplementary.addUnstructuredSSNotifyResponse(ntfyResponse.getInvokeId());
			break;
		case processUnstructuredSSRequest_Request:
			ProcessUnstructuredSSRequest processUnstructuredSSRequest = (ProcessUnstructuredSSRequest) mapMessage;
			if (customInvokeTimeout != null) {
				return mapDialogSupplementary.addProcessUnstructuredSSRequest(customInvokeTimeout,
						processUnstructuredSSRequest.getDataCodingScheme(),
						processUnstructuredSSRequest.getUSSDString(),
						processUnstructuredSSRequest.getAlertingPattern(),
						processUnstructuredSSRequest.getMSISDNAddressString());
			}
			return mapDialogSupplementary.addProcessUnstructuredSSRequest(
					processUnstructuredSSRequest.getDataCodingScheme(), processUnstructuredSSRequest.getUSSDString(),
					processUnstructuredSSRequest.getAlertingPattern(),
					processUnstructuredSSRequest.getMSISDNAddressString());

		}// switch

		return null;
	}

    protected boolean checkMaxActivityCount(int maxActivityCount) {
        if (maxActivityCount <= 0)
            return true;
        return mapProvider.getCurrentDialogsCount() < maxActivityCount;
    }

}

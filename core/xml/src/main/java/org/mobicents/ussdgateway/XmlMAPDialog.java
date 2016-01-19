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
package org.mobicents.ussdgateway;

import java.util.Map;

import javax.naming.OperationNotSupportedException;

import javolution.util.FastList;
import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.MAPMessageType;
import org.mobicents.protocols.ss7.map.api.MAPServiceBase;
import org.mobicents.protocols.ss7.map.api.dialog.MAPAbortProviderReason;
import org.mobicents.protocols.ss7.map.api.dialog.MAPDialogState;
import org.mobicents.protocols.ss7.map.api.dialog.MAPRefuseReason;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.dialog.ProcedureCancellationReason;
import org.mobicents.protocols.ss7.map.api.dialog.Reason;
import org.mobicents.protocols.ss7.map.api.dialog.ResourceUnavailableReason;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.mobicents.protocols.ss7.map.primitives.AddressStringImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSRequestImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyResponseImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSResponseImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResult;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResultLast;

/**
 * <p>
 * Represents the underlying {@link MAPDialogSupplementary}. Application can use
 * this instance of this class, set the supplementary {@link MAPMessage} and
 * pass it {@link EventsSerializeFactory} to serialize this Dialog to send
 * across the wire.
 * </p>
 * <p>
 * Application may also pass byte[] to {@link EventsSerializeFactory} and get
 * deserialized Dailog back
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class XmlMAPDialog implements MAPDialog, XMLSerializable {

	private static final String MAP_APPLN_CONTEXT = "appCntx";
	
	private static final String NETWORK_ID = "networkId";

	private static final String SCCP_LOCAL_ADD = "localAddress";
	private static final String SCCP_REMOTE_ADD = "remoteAddress";

	private static final String MAP_USER_ABORT_CHOICE = "mapUserAbortChoice";
	private static final String MAP_PROVIDER_ABORT_REASON = "mapAbortProviderReason";
	private static final String MAP_REFUSE_REASON = "mapRefuseReason";
    private static final String MAP_DIALOG_TIMEDOUT = "dialogTimedOut";
    private static final String MAP_SRI_PART = "sriPart";
	private static final String EMPTY_DIALOG_HANDSHAKE = "emptyDialogHandshake";
	private static final String MAP_INVOKE_TIMEDOUT = "invokeTimedOut";

	private static final String PRE_ARRANGED_END = "prearrangedEnd";

	private static final String RETURN_MSG_ON_ERR = "returnMessageOnError";

//	private static final String REDIRECT_REQUEST = "redirectRequest";

	private static final String MAP_MSGS_SIZE = "mapMessagesSize";

	private static final String LOCAL_ID = "localId";
	private static final String REMOTE_ID = "remoteId";

	private static final String INVOKE_WITHOUT_ANSWERS_ID = "invokeWithoutAnswerIds";
	private static final String CUSTOM_INVOKE_TIMEOUT = "customInvokeTimeout";

    private static final String ERROR_COMPONENTS = "errComponents";
    private static final String REJECT_COMPONENTS = "rejectComponents";

	private static final String COMMA_SEPARATOR = ",";
	private static final String UNDERSCORE_SEPARATOR = "_";

	private static final String DESTINATION_REFERENCE = "destinationReference";
	private static final String ORIGINATION_REFERENCE = "originationReference";

	private static final String MAPUSERABORTCHOICE_PROCEDCANC = "isProcedureCancellationReason";
	private static final String MAPUSERABORTCHOICE_RESORCUNAV = "isResourceUnavailableReason";
	private static final String MAPUSERABORTCHOICE_USERRESLMT = "isUserResourceLimitation";
	private static final String MAPUSERABORTCHOICE_USERSPECREA = "isUserSpecificReason";

	private static final String DIALOG_TYPE = "type";

	private static final String USER_OBJECT = "userObject";

	// Application Context of this Dialog
	protected MAPApplicationContext appCntx;

	protected SccpAddress localAddress;
	protected SccpAddress remoteAddress;

	private MAPUserAbortChoice mapUserAbortChoice = null;
	private MAPAbortProviderReason mapAbortProviderReason = null;
	private MAPRefuseReason mapRefuseReason = null;
	private Reason refuseReason = null;
	private Boolean prearrangedEnd = null;
	private Boolean dialogTimedOut = null;
	private Boolean emptyDialogHandshake = null;
    private Boolean sriPart = null;

	private Long localId;
	private Long remoteId;
	
	private int networkId;

	private boolean returnMessageOnError = true;

//	private boolean redirectRequest = false;

	private FastList<Long> processInvokeWithoutAnswerIds = new FastList<Long>();
	private FastList<MAPMessage> mapMessages = new FastList<MAPMessage>();

    private ErrorComponentMap errorComponents = new ErrorComponentMap();
    private RejectComponentMap rejectComponents = new RejectComponentMap();

	private MAPDialogState state = MAPDialogState.IDLE;

	private AddressString destReference;
	private AddressString origReference;

	private MessageType messageType = MessageType.Unknown;

	private Long invokeTimedOut = null;
	private Integer customInvokeTimeOut = null;

	private String userObject = null;

	public XmlMAPDialog() {
		super();
	}

	/**
	 * 
	 */
	public XmlMAPDialog(MAPApplicationContext appCntx, SccpAddress localAddress, SccpAddress remoteAddress,
			Long localId, Long remoteId, AddressString destReference, AddressString origReference) {
		this.appCntx = appCntx;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		this.localId = localId;
		this.remoteId = remoteId;

		this.destReference = destReference;
		this.origReference = origReference;
	}

	@Override
	public void abort(MAPUserAbortChoice mapUserAbortChoice) throws MAPException {
		this.mapUserAbortChoice = mapUserAbortChoice;
	}

	@Override
	public void addEricssonData(IMSI arg0, AddressString arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean cancelInvocation(Long arg0) throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

	@Override
	public void close(boolean prearrangedEnd) throws MAPException {
		this.prearrangedEnd = prearrangedEnd;
	}

	@Override
	public void closeDelayed(boolean arg0) throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

	@Override
	public MAPApplicationContext getApplicationContext() {
		return this.appCntx;
	}

	@Override
	public SccpAddress getLocalAddress() {
		return this.localAddress;
	}

	@Override
	public Long getLocalDialogId() {
		return this.localId;
	}

	@Override
	public int getMaxUserDataLength() {
		return 0;
	}

	@Override
	public int getMessageUserDataLengthOnClose(boolean arg0) throws MAPException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMessageUserDataLengthOnSend() throws MAPException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AddressString getReceivedDestReference() {
		return this.destReference;
	}

	@Override
	public MAPExtensionContainer getReceivedExtensionContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AddressString getReceivedOrigReference() {
		return this.origReference;
	}

	@Override
	public SccpAddress getRemoteAddress() {
		return this.remoteAddress;
	}

	@Override
	public Long getRemoteDialogId() {
		return this.remoteId;
	}

	@Override
	public boolean getReturnMessageOnError() {
		return this.returnMessageOnError;
	}

	@Override
	public MAPServiceBase getService() {
		return null;
	}

	@Override
	public MAPDialogState getState() {
		return this.state;
	}

	@Override
	public MessageType getTCAPMessageType() {
		return this.messageType;
	}

	@Override
	public Object getUserObject() {
		return this.userObject;
	}

	@Override
	public void keepAlive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void processInvokeWithoutAnswer(Long invokeId) {
		this.processInvokeWithoutAnswerIds.add(invokeId);
	}

	@Override
	public void refuse(Reason refuseReason) throws MAPException {
		this.refuseReason = refuseReason;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetInvokeTimer(Long arg0) throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

	@Override
	public void send() throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

	@Override
	public void sendDelayed() throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

	@Override
	public void sendErrorComponent(Long invokeId, MAPErrorMessage mapErrorMessage) throws MAPException {
		this.errorComponents.put(invokeId, mapErrorMessage);
	}

	@Override
	public void sendInvokeComponent(Invoke arg0) throws MAPException {
		throw new MAPException(new OperationNotSupportedException());
	}

    @Override
    public void sendRejectComponent(Long invokeId, Problem problem) throws MAPException {
        this.rejectComponents.put(invokeId, problem);
    }

	@Override
	public void sendReturnResultComponent(ReturnResult arg0) throws MAPException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendReturnResultLastComponent(ReturnResultLast arg0) throws MAPException {
		throw new MAPException(new OperationNotSupportedException());

	}

	@Override
	public void setExtentionContainer(MAPExtensionContainer arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocalAddress(SccpAddress origAddress) {
		this.localAddress = origAddress;
	}

	@Override
	public void setRemoteAddress(SccpAddress destAddress) {
		this.remoteAddress = destAddress;
	}

	@Override
	public void setReturnMessageOnError(boolean returnMessageOnError) {
		this.returnMessageOnError = returnMessageOnError;

	}

	@Override
	public void setUserObject(Object obj) {
		this.userObject = obj.toString();
	}
	
	@Override
	public int getNetworkId() {
		return this.networkId;
	}

	@Override
	public void setNetworkId(int networkId) {
        this.networkId = networkId;
	}

	/**
	 * Non MAPDialog methods
	 */

	public void addMAPMessage(MAPMessage mapMessage) {
		this.mapMessages.add(mapMessage);
	}

	public boolean removeMAPMessage(MAPMessage mapMessage) {
		return this.mapMessages.remove(mapMessage);
	}

	public FastList<MAPMessage> getMAPMessages() {
		return this.mapMessages;
	}

	public FastList<Long> getProcessInvokeWithoutAnswerIds() {
		return this.processInvokeWithoutAnswerIds;
	}

    public Map<Long, MAPErrorMessage> getErrorComponents() {
        return errorComponents.getErrorComponents();
    }

    public Map<Long, Problem> getRejectComponents() {
        return rejectComponents.getRejectComponents();
    }

	public MAPUserAbortChoice getMAPUserAbortChoice() {
		return this.mapUserAbortChoice;
	}

	public MAPAbortProviderReason getMapAbortProviderReason() {
		return mapAbortProviderReason;
	}

	public void setMapAbortProviderReason(MAPAbortProviderReason mapAbortProviderReason) {
		this.mapAbortProviderReason = mapAbortProviderReason;
	}

	public MAPRefuseReason getMapRefuseReason() {
		return mapRefuseReason;
	}

	public void setMapRefuseReason(MAPRefuseReason mapRefuseReason) {
		this.mapRefuseReason = mapRefuseReason;
	}

	public Boolean getDialogTimedOut() {
		return dialogTimedOut;
	}

	public void setDialogTimedOut(Boolean dialogTimedOut) {
		this.dialogTimedOut = dialogTimedOut;
	}

    public Boolean getSriPart() {
        return sriPart;
    }

    public void setSriPart(Boolean sriPart) {
        this.sriPart = sriPart;
    }

	public Boolean getPrearrangedEnd() {
		return this.prearrangedEnd;
	}

	public void setTCAPMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

//	public boolean isRedirectRequest() {
//		return redirectRequest;
//	}

	public Long getInvokeTimedOut() {
		return invokeTimedOut;
	}

	public void setInvokeTimedOut(Long invokeTimedOut) {
		this.invokeTimedOut = invokeTimedOut;
	}

//	public void setRedirectRequest(boolean redirectRequest) {
//		this.redirectRequest = redirectRequest;
//	}

	public Integer getCustomInvokeTimeOut() {
		return customInvokeTimeOut;
	}

	/**
	 * Set custom invoke time out for added MapMessages. If not set the default
	 * values will be used
	 * 
	 * @param customInvokeTimeOut
	 */
	public void setCustomInvokeTimeOut(Integer customInvokeTimeOut) {
		this.customInvokeTimeOut = customInvokeTimeOut;
	}

	public Boolean getEmptyDialogHandshake() {
		return emptyDialogHandshake;
	}

	/**
	 * A special parameter used only when Dialog is initiated by USSD Gw (Push
	 * or Proxy). USSD Gateway will first create empty Dialog and send Begin to
	 * remote end and only on Dialog accept from remote side it, will send
	 * payload.
	 * 
	 * @param emptyDialogHandshake
	 */
	public void setEmptyDialogHandshake(Boolean emptyDialogHandshake) {
		this.emptyDialogHandshake = emptyDialogHandshake;
	}

	public void reset() {
		this.mapMessages.clear();
		this.processInvokeWithoutAnswerIds.clear();
		this.errorComponents.clear();
		this.rejectComponents.clear();
	}

	@Override
	public String toString() {
		return "XmlMAPDialog [appCntx=" + appCntx + ", localAddress="
				+ localAddress + ", remoteAddress=" + remoteAddress
				+ ", mapUserAbortChoice=" + mapUserAbortChoice
				+ ", mapAbortProviderReason=" + mapAbortProviderReason
				+ ", mapRefuseReason=" + mapRefuseReason + ", refuseReason="
				+ refuseReason + ", prearrangedEnd=" + prearrangedEnd
				+ ", dialogTimedOut=" + dialogTimedOut
				+ ", emptyDialogHandshake=" + emptyDialogHandshake
				+ ", sriPart=" + sriPart + ", localId=" + localId
				+ ", remoteId=" + remoteId + ", networkId=" + networkId
				+ ", returnMessageOnError=" + returnMessageOnError
				+ ", processInvokeWithoutAnswerIds="
				+ processInvokeWithoutAnswerIds + ", mapMessages="
				+ mapMessages + ", errorComponents=" + errorComponents
				+ ", rejectComponents=" + rejectComponents + ", state=" + state
				+ ", destReference=" + destReference + ", origReference="
				+ origReference + ", messageType=" + messageType
				+ ", invokeTimedOut=" + invokeTimedOut
				+ ", customInvokeTimeOut=" + customInvokeTimeOut
				+ ", userObject=" + userObject + "]";
	}

	protected static String serializeMAPUserAbortChoice(MAPUserAbortChoice abort) {
		StringBuilder sb = new StringBuilder();

		if (abort.isUserSpecificReason()) {
			sb.append(MAPUSERABORTCHOICE_USERSPECREA);
			return sb.toString();
		}

		if (abort.isProcedureCancellationReason()) {
			sb.append(MAPUSERABORTCHOICE_PROCEDCANC).append(UNDERSCORE_SEPARATOR)
					.append(abort.getProcedureCancellationReason().name());
			return sb.toString();
		}

		if (abort.isResourceUnavailableReason()) {
			sb.append(MAPUSERABORTCHOICE_RESORCUNAV).append(UNDERSCORE_SEPARATOR)
					.append(abort.getResourceUnavailableReason().name());
			return sb.toString();
		}

		if (abort.isUserResourceLimitation()) {
			sb.append(MAPUSERABORTCHOICE_USERRESLMT);
			return sb.toString();
		}

		return null;
	}

	protected static MAPUserAbortChoice deserializeMAPUserAbortChoice(String str) {
		String[] appCtxBody = str.split(UNDERSCORE_SEPARATOR);

		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();

		if (appCtxBody[0].equals(MAPUSERABORTCHOICE_USERSPECREA)) {
			abort.setUserSpecificReason();
			return abort;
		}

		if (appCtxBody[0].equals(MAPUSERABORTCHOICE_USERRESLMT)) {
			abort.setUserResourceLimitation();
			return abort;
		}

		if (appCtxBody[0].equals(MAPUSERABORTCHOICE_PROCEDCANC)) {
			ProcedureCancellationReason procCanReasn = ProcedureCancellationReason.valueOf(appCtxBody[1]);
			abort.setProcedureCancellationReason(procCanReasn);
			return abort;
		}

		if (appCtxBody[0].equals(MAPUSERABORTCHOICE_RESORCUNAV)) {
			ResourceUnavailableReason resUnaReas = ResourceUnavailableReason.valueOf(appCtxBody[1]);
			abort.setResourceUnavailableReason(resUnaReas);
			return abort;
		}

		return null;
	}

	protected static String serializeMAPApplicationContext(MAPApplicationContext mapApplicationContext) {
		StringBuilder sb = new StringBuilder();
		sb.append(mapApplicationContext.getApplicationContextName().name()).append(UNDERSCORE_SEPARATOR)
				.append(mapApplicationContext.getApplicationContextVersion().name());
		return sb.toString();
	}

	protected static MAPApplicationContext deserializeMAPApplicationContext(String str) {
		String[] appCtxBody = str.split(UNDERSCORE_SEPARATOR);

		MAPApplicationContextName appCtxname = MAPApplicationContextName.valueOf(appCtxBody[0]);
		MAPApplicationContextVersion appCtxVer = MAPApplicationContextVersion.valueOf(appCtxBody[1]);

		return MAPApplicationContext.getInstance(appCtxname, appCtxVer);

	}

	protected static final XMLFormat<XmlMAPDialog> USSR_XML = new XMLFormat<XmlMAPDialog>(XmlMAPDialog.class) {

		public void write(XmlMAPDialog dialog, OutputElement xml) throws XMLStreamException {
			xml.setAttribute(DIALOG_TYPE, dialog.messageType.name());

			if (dialog.appCntx != null) {
				xml.setAttribute(MAP_APPLN_CONTEXT, serializeMAPApplicationContext(dialog.appCntx));
			}
			xml.setAttribute(NETWORK_ID, dialog.networkId);
			xml.setAttribute(LOCAL_ID, dialog.localId);
			xml.setAttribute(REMOTE_ID, dialog.remoteId);

			int size = dialog.processInvokeWithoutAnswerIds.size();
			if (size != 0) {
				StringBuffer sb = new StringBuffer();
				for (int count = 0; count < size; count++) {
					sb.append(dialog.processInvokeWithoutAnswerIds.get(count));
					if (count != (size - 1)) {
						sb.append(COMMA_SEPARATOR);
					}
				}

				xml.setAttribute(INVOKE_WITHOUT_ANSWERS_ID, sb.toString());
			}

			int mapMessagsSize = dialog.mapMessages.size();

			xml.setAttribute(MAP_MSGS_SIZE, mapMessagsSize);

			if (dialog.mapUserAbortChoice != null) {
				xml.setAttribute(MAP_USER_ABORT_CHOICE, serializeMAPUserAbortChoice(dialog.mapUserAbortChoice));
			}

			if (dialog.mapAbortProviderReason != null) {
				xml.setAttribute(MAP_PROVIDER_ABORT_REASON, dialog.mapAbortProviderReason.name());
			}

			if (dialog.mapRefuseReason != null) {
				xml.setAttribute(MAP_REFUSE_REASON, dialog.mapRefuseReason.name());
			}

            if (dialog.dialogTimedOut != null) {
                xml.setAttribute(MAP_DIALOG_TIMEDOUT, dialog.dialogTimedOut);
            }

            if (dialog.sriPart != null) {
                xml.setAttribute(MAP_SRI_PART, dialog.sriPart);
            }

			if (dialog.invokeTimedOut != null) {
				xml.setAttribute(MAP_INVOKE_TIMEDOUT, dialog.invokeTimedOut);
			}

			if (dialog.customInvokeTimeOut != null) {
				xml.setAttribute(CUSTOM_INVOKE_TIMEOUT, dialog.customInvokeTimeOut);
			}

			if (dialog.emptyDialogHandshake != null) {
				xml.setAttribute(EMPTY_DIALOG_HANDSHAKE, dialog.emptyDialogHandshake);
			}

			xml.setAttribute(PRE_ARRANGED_END, dialog.prearrangedEnd);

			xml.setAttribute(RETURN_MSG_ON_ERR, dialog.returnMessageOnError);

//			xml.setAttribute(REDIRECT_REQUEST, dialog.redirectRequest);

			if (dialog.userObject != null) {
				xml.setAttribute(USER_OBJECT, dialog.userObject.toString());
			}

			xml.add((SccpAddressImpl)dialog.localAddress, SCCP_LOCAL_ADD, SccpAddressImpl.class);
			xml.add((SccpAddressImpl)dialog.remoteAddress, SCCP_REMOTE_ADD, SccpAddressImpl.class);

			xml.add((AddressStringImpl) dialog.destReference, DESTINATION_REFERENCE, AddressStringImpl.class);
			xml.add((AddressStringImpl) dialog.origReference, ORIGINATION_REFERENCE, AddressStringImpl.class);

            if (dialog.errorComponents.size() > 0)
                xml.add(dialog.errorComponents, ERROR_COMPONENTS, ErrorComponentMap.class);
            if (dialog.rejectComponents.size() > 0)
                xml.add(dialog.rejectComponents, REJECT_COMPONENTS, RejectComponentMap.class);

			for (FastList.Node<MAPMessage> n = dialog.mapMessages.head(), end = dialog.mapMessages.tail(); (n = n
					.getNext()) != end;) {

				MAPMessage mapMessage = n.getValue();

				switch (mapMessage.getMessageType()) {
				case processUnstructuredSSRequest_Request:
					xml.add((ProcessUnstructuredSSRequestImpl) mapMessage,
							MAPMessageType.processUnstructuredSSRequest_Request.name(),
							ProcessUnstructuredSSRequestImpl.class);
					break;
				case processUnstructuredSSRequest_Response:
					xml.add((ProcessUnstructuredSSResponseImpl) mapMessage,
							MAPMessageType.processUnstructuredSSRequest_Response.name(),
							ProcessUnstructuredSSResponseImpl.class);
					break;

				case unstructuredSSRequest_Request:

					xml.add((UnstructuredSSRequestImpl) mapMessage,
							MAPMessageType.unstructuredSSRequest_Request.name(), UnstructuredSSRequestImpl.class);
					break;

				case unstructuredSSRequest_Response:

					xml.add((UnstructuredSSResponseImpl) mapMessage,
							MAPMessageType.unstructuredSSRequest_Response.name(), UnstructuredSSResponseImpl.class);
					break;

				case unstructuredSSNotify_Request:
					xml.add((UnstructuredSSNotifyRequestImpl) mapMessage,
							MAPMessageType.unstructuredSSNotify_Request.name(), UnstructuredSSNotifyRequestImpl.class);
					break;
				case unstructuredSSNotify_Response:
					xml.add((UnstructuredSSNotifyResponseImpl) mapMessage,
							MAPMessageType.unstructuredSSNotify_Response.name(), UnstructuredSSNotifyResponseImpl.class);
					break;
				default:
					break;
				}
			}

		}

		public void read(InputElement xml, XmlMAPDialog dialog) throws XMLStreamException {
			dialog.messageType = MessageType.valueOf(xml.getAttribute(DIALOG_TYPE, MessageType.Unknown.name()));

			String appCtxStr = xml.getAttribute(MAP_APPLN_CONTEXT, null);

			if (appCtxStr != null) {
				dialog.appCntx = deserializeMAPApplicationContext(appCtxStr);
			}
			dialog.networkId = xml.getAttribute(NETWORK_ID, 0);
			dialog.localId = xml.getAttribute(LOCAL_ID, 0l);
			dialog.remoteId = xml.getAttribute(REMOTE_ID, 0l);

			String sb = xml.getAttribute(INVOKE_WITHOUT_ANSWERS_ID, null);

			if (sb != null) {
				String[] longStrsArr = sb.split(COMMA_SEPARATOR);
				for (int count = 0; count < longStrsArr.length; count++) {
					dialog.processInvokeWithoutAnswer(Long.parseLong(longStrsArr[count]));
				}
			}

			int mapMssgsSize = xml.getAttribute(MAP_MSGS_SIZE, 0);

			String mapUsrAbrtChoiceStr = xml.getAttribute(MAP_USER_ABORT_CHOICE, null);

			if (mapUsrAbrtChoiceStr != null) {
				dialog.mapUserAbortChoice = deserializeMAPUserAbortChoice(mapUsrAbrtChoiceStr);
			}

			String mapAbortProviderReasonStr = xml.getAttribute(MAP_PROVIDER_ABORT_REASON, null);
			if (mapAbortProviderReasonStr != null) {
				dialog.mapAbortProviderReason = MAPAbortProviderReason.valueOf(mapAbortProviderReasonStr);
			}

			String mapRefuseReason = xml.getAttribute(MAP_REFUSE_REASON, null);
			if (mapRefuseReason != null) {
				dialog.mapRefuseReason = MAPRefuseReason.valueOf(mapRefuseReason);
			}

            String dialogTimedOutStr = xml.getAttribute(MAP_DIALOG_TIMEDOUT, null);
            if (dialogTimedOutStr != null) {
                dialog.dialogTimedOut = Boolean.parseBoolean(dialogTimedOutStr);
            }

            String sriPartStr = xml.getAttribute(MAP_SRI_PART, null);
            if (sriPartStr != null) {
                dialog.sriPart = Boolean.parseBoolean(sriPartStr);
            }

			String invokeTimedOutStr = xml.getAttribute(MAP_INVOKE_TIMEDOUT, null);
			if (invokeTimedOutStr != null) {
				dialog.invokeTimedOut = Long.parseLong(invokeTimedOutStr);
			}

			String customInvokeTimeoutStr = xml.getAttribute(CUSTOM_INVOKE_TIMEOUT, null);
			if (customInvokeTimeoutStr != null) {
				dialog.customInvokeTimeOut = Integer.parseInt(customInvokeTimeoutStr);
			}

			String emptyDialogHandshakeStr = xml.getAttribute(EMPTY_DIALOG_HANDSHAKE, null);
			if (emptyDialogHandshakeStr != null) {
				dialog.emptyDialogHandshake = Boolean.parseBoolean(emptyDialogHandshakeStr);
			}

			String preArrEndStr = xml.getAttribute(PRE_ARRANGED_END, null);
			if (preArrEndStr != null) {
				dialog.prearrangedEnd = Boolean.parseBoolean(preArrEndStr);
			}

			dialog.returnMessageOnError = xml.getAttribute(RETURN_MSG_ON_ERR, false);

//			dialog.redirectRequest = xml.getAttribute(REDIRECT_REQUEST, false);

			dialog.userObject = xml.getAttribute(USER_OBJECT, null);

			dialog.localAddress = xml.get(SCCP_LOCAL_ADD, SccpAddressImpl.class);
			dialog.remoteAddress = xml.get(SCCP_REMOTE_ADD, SccpAddressImpl.class);

			dialog.destReference = xml.get(DESTINATION_REFERENCE, AddressStringImpl.class);
			dialog.origReference = xml.get(ORIGINATION_REFERENCE, AddressStringImpl.class);

            ErrorComponentMap em = xml.get(ERROR_COMPONENTS, ErrorComponentMap.class);
            if (em != null)
                dialog.errorComponents = em;

            RejectComponentMap pm = xml.get(REJECT_COMPONENTS, RejectComponentMap.class);
            if (pm != null)
                dialog.rejectComponents = pm;

			for (int count = 0; count < mapMssgsSize; count++) {

				MAPMessage mapMessage = xml.get(MAPMessageType.processUnstructuredSSRequest_Request.name(),
						ProcessUnstructuredSSRequestImpl.class);

				if (mapMessage == null) {
					mapMessage = xml.get(MAPMessageType.processUnstructuredSSRequest_Response.name(),
							ProcessUnstructuredSSResponseImpl.class);
				}

				if (mapMessage == null) {
					mapMessage = xml.get(MAPMessageType.unstructuredSSRequest_Request.name(),
							UnstructuredSSRequestImpl.class);
				}

				if (mapMessage == null) {
					mapMessage = xml.get(MAPMessageType.unstructuredSSRequest_Response.name(),
							UnstructuredSSResponseImpl.class);
				}

				if (mapMessage == null) {
					mapMessage = xml.get(MAPMessageType.unstructuredSSNotify_Request.name(),
							UnstructuredSSNotifyRequestImpl.class);
				}

				if (mapMessage == null) {
					mapMessage = xml.get(MAPMessageType.unstructuredSSNotify_Response.name(),
							UnstructuredSSNotifyResponseImpl.class);
				}

				dialog.addMAPMessage(mapMessage);
			}
		}
	};

}
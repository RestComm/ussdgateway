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

package org.mobicents.applications.ussd.examples.http.push;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javolution.util.FastList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingLevel;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingPattern;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.restcomm.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.restcomm.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.XmlMAPDialog;

/**
 * @author baranowb
 * 
 */
public class HTTPPush implements HTTPPushMBean {

	private static final Logger logger = Logger.getLogger(HTTPPush.class);
	// default
	private String targetURI = "http://localhost:8080/mobicents";
	private String isdn = "1111";

	private EventsSerializeFactory serializer = new EventsSerializeFactory();
	private StringBuilder status = new StringBuilder();
	private static long ID = 0;

	// some defs
	private static final CBSDataCodingScheme CBS_CODDING_SCHEME = new CBSDataCodingSchemeImpl(15);
	private static final AlertingPattern ALERTING_PATTERN = new AlertingPatternImpl(AlertingLevel.Level1);
	private static final String ANSWER_NTFY = "2";

	// session
	private XmlMAPDialog dialog;
	private HttpContext context;

	// client
	private HttpClient httpClient;

	private CookieStore cookieStore = null;

	private final MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
			MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

	/**
     * 
     */
	public HTTPPush() {
		super();
		HttpParams params = new SyncBasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(schemeRegistry);
		threadSafeClientConnManager.setMaxTotal(1000);

		httpClient = new DefaultHttpClient(threadSafeClientConnManager, params);
	}

	@Override
	public void setTargetUri(String uri) {
		this.targetURI = uri;

	}

	@Override
	public String getTargetUri() {
		return this.targetURI;
	}

	@Override
	public String getIsdn() {
		return isdn;
	}

	@Override
	public void setIsdn(String isdn) {
		this.isdn = isdn;
	}

	@Override
	public void reset() {
		// TODO: try to end gracefully, ie send term or something
		this.dialog = null;
		this.status = new StringBuilder();
		this.context = null;
	}

	@Override
	public void sendRequest(String ussdRequest, boolean emptyDialogHandshake, int invokeTimeout, String userData) throws Exception {
		ISDNAddressString isdnAddressString = getISDNAddressString();
		USSDString ussdStr = getUSSDString(ussdRequest);
		UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(CBS_CODDING_SCHEME,
				ussdStr, ALERTING_PATTERN, isdnAddressString);

		// lets be a good citizen
		if (this.dialog == null) {
			this.establishSession();
			this.dialog = new XmlMAPDialog(appCtx, null, null, 0l, 0l, null, null);
			this.dialog.setCustomInvokeTimeOut(invokeTimeout);
			this.dialog.setEmptyDialogHandshake(emptyDialogHandshake);
			this.dialog.setTCAPMessageType(MessageType.Begin);
			addStatusEntry("Created dialog: '" + this.dialog);
		} else {
			this.dialog.reset();
			this.dialog.setTCAPMessageType(MessageType.Continue);
			addStatusEntry("Sending on existing dialog: '" + this.dialog);
		}

		if(userData != null){
			this.dialog.setUserObject(userData);
		}
		
		this.dialog.addMAPMessage(unstructuredSSRequestIndication);
		byte[] serialized = this.serializer.serialize(this.dialog);
		// send
		addStatusEntry("Sending UnstructuredSSRequest");
		HttpResponse response = send(serialized);
		handleResponse(response);
	}

	@Override
	public void sendNotify(String ussdRequest, boolean emptyDialogHandshake, int invokeTimeout, String userData) throws Exception {
		ISDNAddressString isdnAddressString = getISDNAddressString();
		USSDString ussdStr = getUSSDString(ussdRequest);
		UnstructuredSSNotifyRequest unstructuredSSNotifyRequest = new UnstructuredSSNotifyRequestImpl(
				CBS_CODDING_SCHEME, ussdStr, ALERTING_PATTERN, isdnAddressString);

		// lets be a good citizen
		if (this.dialog == null) {
			this.establishSession();
			this.dialog = new XmlMAPDialog(appCtx, null, null, 0l, 0l, null, null);
			this.dialog.setEmptyDialogHandshake(emptyDialogHandshake);
			this.dialog.setCustomInvokeTimeOut(invokeTimeout);
			this.dialog.setTCAPMessageType(MessageType.Begin);
			addStatusEntry("Created dialog: '" + this.dialog);
		} else {
			this.dialog.reset();
			this.dialog.setTCAPMessageType(MessageType.Continue);
			addStatusEntry("Sending on existing dialog: '" + this.dialog);
		}
		
		if(userData != null){
			this.dialog.setUserObject(userData);
		}
		
		this.dialog.addMAPMessage(unstructuredSSNotifyRequest);
		byte[] serialized = this.serializer.serialize(this.dialog);
		addStatusEntry("Sending UnstructuredSSNotifyRequest");
		HttpResponse response = send(serialized);
		handleResponse(response);
	}

	@Override
	public String getStatus() {
		return this.status.toString();
	}

	@Override
	public void abort() throws Exception {
		if (this.dialog == null) {
			throw new Exception("There is no Dialog established yet");
		}

		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setUserSpecificReason();

		this.dialog.reset();
		this.dialog.setTCAPMessageType(MessageType.Abort);
		this.dialog.abort(abort);

		addStatusEntry("Aborting existing dialog: '" + this.dialog);

		byte[] serialized = this.serializer.serialize(this.dialog);

		HttpResponse response = send(serialized);
		handleResponse(response);
	}

	@Override
	public void close() throws Exception {
		if (this.dialog == null) {
			throw new Exception("There is no Dialog established yet");
		}

		this.dialog.reset();
		this.dialog.setTCAPMessageType(MessageType.End);
		this.dialog.close(false);

		addStatusEntry("Closing existing dialog: '" + this.dialog);

		byte[] serialized = this.serializer.serialize(this.dialog);

		HttpResponse response = send(serialized);
		handleResponse(response);
	}

	protected synchronized long getNewID() {
		return ID++;
	}

	protected USSDString getUSSDString(String msg) throws MAPException {
		return new USSDStringImpl(msg, CBS_CODDING_SCHEME, null);
	}

	protected ISDNAddressString getISDNAddressString() {
		return new ISDNAddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN, isdn);
	}

	protected void addStatusEntry(String entry) {
		Date d = new Date();
		d.setTime(System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		sb.append(d.toString()).append(">").append(entry);
		sb.append("\n");
		sb.append("---------------------------------");
		logger.info(sb.toString());
		this.status.append(sb.toString().replaceAll("<", "-").replaceAll(">", "-"));
	}

	protected void establishSession() {
		this.context = new BasicHttpContext();
		CookieStore cookieStore = new BasicCookieStore();
		this.context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * @param serialized
	 * @return
	 */
	protected HttpResponse send(byte[] serialized) throws Exception {
		HttpUriRequest post = new HttpPost(this.targetURI);
		if (!(post instanceof HttpEntityEnclosingRequestBase)) {
			throw new IOException();
		}
		addStatusEntry("Sending:\n" + new String(serialized));
		ByteArrayEntity entity = new ByteArrayEntity(serialized);
		entity.setContentType("text/xml");
		entity.setContentEncoding(Charset.defaultCharset().toString());
		((HttpEntityEnclosingRequestBase) post).setEntity(entity);

		return this.httpClient.execute(post, this.context);
	}

	/**
	 * Handle response, this checks for choice and sends NTFY if required
	 * 
	 * @param response
	 * @throws Exception
	 */
	protected void handleResponse(HttpResponse response) throws Exception {
		if (response.getStatusLine().getStatusCode() != 200) {
			logger.error("Received bad answer: " + response.getStatusLine().getStatusCode() + " <> "
					+ response.getStatusLine().getReasonPhrase());
			return;
		}
		Header[] headers = response.getAllHeaders();
		for (Header h : headers) {
			addStatusEntry(h.getName() + " - " + h.getValue());
		}
		String content = EntityUtils.toString(response.getEntity());

		addStatusEntry("Received response: \n" + content);

		if (content == null || content.trim().equals("")) {
			// received empty response which is ok
			return;
		}

		XmlMAPDialog deserialized = this.serializer.deserialize(content.getBytes());
		this.dialog = deserialized;

		if (this.dialog.getTCAPMessageType() == MessageType.End
				|| this.dialog.getTCAPMessageType() == MessageType.Abort) {
			addStatusEntry("Dialog " + this.dialog.getTCAPMessageType().name());
			this.reset();
			return;
		}

		Boolean prearrangedEnd = dialog.getPrearrangedEnd();
		FastList<MAPMessage> capMessages = dialog.getMAPMessages();

		for (FastList.Node<MAPMessage> n = capMessages.head(), end = capMessages.tail(); (n = n.getNext()) != end;) {
			MAPMessage message = n.getValue();
			MAPMessageType type = message.getMessageType();
			switch (type) {
			case unstructuredSSRequest_Response:
				UnstructuredSSResponse unstructuredSSResponse = (UnstructuredSSResponse) message;
				USSDString x = unstructuredSSResponse.getUSSDString();
				// TODO: CBS ?
				String s = x.getString(Charset.defaultCharset());
				addStatusEntry("Received response: " + s);
				if (s.equals(ANSWER_NTFY)) {
					sendNotify("Bye bye", false, 20000, null);
				}
				break;
			case unstructuredSSNotify_Response:
				addStatusEntry("Received NTFY response.");
				break;
			default:
				break;
			}
		}

	}
}

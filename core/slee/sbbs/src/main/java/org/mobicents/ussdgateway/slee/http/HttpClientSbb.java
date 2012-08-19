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
package org.mobicents.ussdgateway.slee.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.slee.ActivityContextInterface;
import javax.slee.SbbContext;
import javax.slee.resource.ResourceAdaptorTypeID;

import net.java.client.slee.resource.http.HttpClientActivity;
import net.java.client.slee.resource.http.HttpClientActivityContextInterfaceFactory;
import net.java.client.slee.resource.http.HttpClientResourceAdaptorSbbInterface;
import net.java.client.slee.resource.http.event.ResponseEvent;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.ChildSbb;

/**
 * 
 * @author amit bhayani
 */
public abstract class HttpClientSbb extends ChildSbb {

	private static final String CONTENT_TYPE = "text";
	private static final String CONTENT_SUB_TYPE = "xml";

	private static final String ACCEPTED_CONTENT_TYPE = CONTENT_TYPE + "/" + CONTENT_SUB_TYPE;

	// /////////////////
	// HTTP RA Stuff //
	// /////////////////
	private static final ResourceAdaptorTypeID httpRATypeID = new ResourceAdaptorTypeID(
			"HttpClientResourceAdaptorType", "org.mobicents", "4.0");
	private static final String httpRaLink = "HttpClientResourceAdaptor";

	private HttpClientActivityContextInterfaceFactory httpClientActivityContextInterfaceFactory;
	private HttpClientResourceAdaptorSbbInterface httpProvider;

	/** Creates a new instance of CallSbb */
	public HttpClientSbb() {
	}

	// /////////////////
	// HTTP Handlers //
	// /////////////////

	public void onResponseEvent(ResponseEvent event, ActivityContextInterface aci) {

		HttpResponse response = event.getHttpResponse();
		HttpClientActivity httpClientActivity = ((HttpClientActivity) aci.getActivity());

		if (response == null) {
			// Error condition
			logger.severe("Exception received for HTTP request sent. See traces above");

			// TODO Abort Dialog or Send response back?
			try {
				this.abort(this.getMAPDialog());
				this.endHttpClientActivity(httpClientActivity);
			} catch (MAPException e) {
				logger.severe("Exception while trying to abort MAP Dialog", e);
			}
			return;
		}

		StatusLine statusLine = response.getStatusLine();

		int statusCode = statusLine.getStatusCode();

		switch (statusCode) {

		case 200:
			try {
				byte[] xmlContent = null;
				if (response.getEntity() != null) {
					xmlContent = getResultData(response.getEntity());
					if (logger.isFineEnabled()) {
						logger.fine("Received answer content: \n" + new String(xmlContent));
					}
					if (xmlContent == null || xmlContent.length <= 0) {
						// TODO Error Condition
						logger.severe("Received invalid payload from http server");
					}
					EventsSerializeFactory factory = this.getEventsSerializeFactory();
					Dialog dialog = factory.deserialize(xmlContent);

					if (dialog == null) {
						// TODO Error Condition
						logger.severe("Received Success Response but couldn't deserialize to Dialog. Dialog is null");
					}

					MAPMessage mapMessage = dialog.getMAPMessage();

					switch (mapMessage.getMessageType()) {
					case unstructuredSSRequest_Request:
						this.addUnstructuredSSRequest((UnstructuredSSRequest) mapMessage);
						break;
					case processUnstructuredSSRequest_Response:
						aci.detach(this.sbbContext.getSbbLocalObject());
						this.addProcessUnstructuredSSResponse((ProcessUnstructuredSSResponse) mapMessage);
						this.endHttpClientActivity(httpClientActivity);
						break;
					default:
						logger.severe("Received Success Response but unidentified response body");
						break;
					}

				} else {
					// TODO Error condition, take care
					logger.severe("Received Success Response but without response body");
				}

			} catch (Exception e) {
				// TODO Error condition, take care
				logger.severe("Error while processing 2xx", e);
			}
			break;

		default:
			// TODO Error condition, take care
			logger.severe(String.format("Received non 2xx Response=%s", response));
			break;
		}

	}

	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);
		try {
			this.httpClientActivityContextInterfaceFactory = (HttpClientActivityContextInterfaceFactory) this.sbbContext
					.getActivityContextInterfaceFactory(httpRATypeID);
			this.httpProvider = (HttpClientResourceAdaptorSbbInterface) this.sbbContext.getResourceAdaptorInterface(
					httpRATypeID, httpRaLink);

		} catch (Exception ne) {
			super.logger.severe("Could not set SBB context:", ne);
		}
	}

	public void unsetSbbContext() {
		this.sbbContext = null;
		super.unsetSbbContext();
	}

	private HttpClientActivity getHTTPClientActivity() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			Object activity = aci.getActivity();
			if (activity instanceof HttpClientActivity) {
				return (HttpClientActivity) activity;
			}
		}
		return null;
	}

	private void doPost(HttpClientActivity httpClientActivity, String url, byte[] content) {

		HttpPost uriRequest = createRequest(url, null, ACCEPTED_CONTENT_TYPE, null);

		// NOTE: here we assume that its text/xml utf8 encoded... bum.
		pushContent(uriRequest, ACCEPTED_CONTENT_TYPE, "utf-8", new ByteArrayInputStream(content));

		if (logger.isFineEnabled()) {
			logger.fine("Executing HttpPost=" + uriRequest);
		}
		httpClientActivity.execute(uriRequest, null);
	}

	private String encode(String url, Map<String, String> queryParameters) {
		// seriously...
		if (queryParameters == null && queryParameters.size() == 0)
			return url;

		url = url + "?";
		int encCount = 0;
		Iterator<Map.Entry<String, String>> vks = queryParameters.entrySet().iterator();
		while (vks.hasNext()) {

			Map.Entry<String, String> e = vks.next();
			url += e.getKey() + "=" + e.getValue();
			encCount++;
			if (encCount < queryParameters.size()) {
				url += "&";
			}

		}

		return url;
	}

	/**
	 * @param method
	 * @param restID
	 * @param uri
	 * @param queryParameters
	 * @param acceptedContent
	 * @param headers
	 * @return
	 */
	private HttpPost createRequest(String uri, Map<String, String> queryParameters, String acceptedContent,
			Header[] headers) {

		if (uri == null) {
			throw new NullPointerException("URI mst not be null.");
		}
		String requestURI = uri;
		if (queryParameters != null) {
			requestURI = encode(requestURI, queryParameters);
		}
		HttpPost request = new HttpPost(uri);

		if (acceptedContent != null) {
			BasicHeader acceptedContentHeader = new BasicHeader("Accept", acceptedContent);
			request.addHeader(acceptedContentHeader);
		}
		if (headers != null) {
			for (Header h : headers) {
				request.addHeader(h);
			}
		}

		return request;
	}

	private void pushContent(HttpUriRequest request, String contentType, String contentEncoding, InputStream content) {

		// TODO: check other preconditions?
		if (contentType != null && content != null && request instanceof HttpEntityEnclosingRequest) {
			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(content);
			if (contentEncoding != null)
				entity.setContentEncoding(contentEncoding);
			entity.setContentType(contentType);
			HttpEntityEnclosingRequest rr = (HttpEntityEnclosingRequest) request;
			rr.setEntity(entity);
		}

	}

	private byte[] getResultData(HttpEntity entity) throws IOException {
		return EntityUtils.toByteArray(entity);
	}

	// /////////////////////////
	// Client abstract stuff //
	// /////////////////////////

	@Override
	protected boolean checkProtocolConnection() {
		return this.getHTTPClientActivity() != null;
	}

	@Override
	protected void sendUssdData(byte[] data) throws Exception {
		HttpClientActivity httpClientActivity = this.getHTTPClientActivity();
		if (httpClientActivity == null) {

			httpClientActivity = this.httpProvider.createHttpClientActivity(false, null);
			// combo
			this.httpClientActivityContextInterfaceFactory.getActivityContextInterface(httpClientActivity).attach(
					this.sbbContext.getSbbLocalObject());
			logger.info("Created HTTP Activity '" + httpClientActivity + "' ");
		}

		ScRoutingRule call = this.getCall();

		// combo
		this.httpClientActivityContextInterfaceFactory.getActivityContextInterface(httpClientActivity).attach(
				this.sbbContext.getSbbLocalObject());
		String url = call.getRuleUrl();

		doPost(httpClientActivity, url, data);
	}

	@Override
	protected void terminateProtocolConnection() {
		HttpClientActivity httpClientActivity = this.getHTTPClientActivity();
		this.endHttpClientActivity(httpClientActivity);
	}

	private void endHttpClientActivity(HttpClientActivity httpClientActivity) {
		if (httpClientActivity != null) {
			httpClientActivity.endActivity();
		}
	}

}

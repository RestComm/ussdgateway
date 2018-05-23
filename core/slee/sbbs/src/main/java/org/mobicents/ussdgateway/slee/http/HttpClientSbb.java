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

package org.mobicents.ussdgateway.slee.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.slee.ActivityContextInterface;
import javax.slee.SbbContext;
import javax.slee.resource.ResourceAdaptorTypeID;

import javolution.util.FastList;
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
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.restcomm.protocols.ss7.map.api.service.supplementary.MAPDialogSupplementary;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.ChildSbb;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;

/**
 * 
 * @author amit bhayani
 * @author sergey vetyutnev
 */
public abstract class HttpClientSbb extends ChildSbb {

	private static final String CONTENT_TYPE = "text";
	private static final String CONTENT_SUB_TYPE = "xml";

	private static final String CONTENT_ENCODING = "utf-8";

	private static final String ACCEPTED_CONTENT_TYPE = CONTENT_TYPE + "/" + CONTENT_SUB_TYPE;

	/** Creates a new instance of CallSbb */
	public HttpClientSbb() {
		super("HttpClientSbb");
	}

	// -------------------------------------------------------------
	// HTTP Handlers
	// -------------------------------------------------------------

	public void onResponseEvent(ResponseEvent event, ActivityContextInterface aci) {

		this.cancelTimer();

		HttpResponse response = event.getHttpResponse();
		HttpClientActivity httpClientActivity = ((HttpClientActivity) aci.getActivity());

        if (this.getFinalMessageSent()) {
            // dialog was already terminated
            httpClientActivity.endActivity();
            return;
        }

		MAPDialogSupplementary mapDialogSupplementary = this.getMAPDialog();

        try {
            if (response == null) {
                // Error condition
                throw new Exception("Exception received for HTTP request sent. See traces above");
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
                            throw new Exception("Received invalid payload from http server (null or zero length)");
                        }

                        EventsSerializeFactory factory = this.getEventsSerializeFactory();
                        XmlMAPDialog dialog = factory.deserialize(xmlContent);

                        if (dialog == null) {
                            throw new Exception("Received Success HTTPResponse but couldn't deserialize to Dialog. Dialog is null");
                        }

                        Object userObject = dialog.getUserObject();
                        if (userObject != null) {
                            this.setUserObject(userObject.toString());
                        }

                        MAPUserAbortChoice mapUserAbortChoice = dialog.getMAPUserAbortChoice();
                        if (mapUserAbortChoice != null) {
                            MAPDialogSupplementary mapDialog = this.getMAPDialog();
                            mapDialog.abort(mapUserAbortChoice);
                            this.endHttpClientActivity(httpClientActivity);
                            this.updateDialogFailureStat();
                            this.createCDRRecord(RecordStatus.ABORT_APP);
                            return;
                        }

                        Boolean prearrangedEnd = dialog.getPrearrangedEnd();

                        FastList<MAPMessage> mapMessages = dialog.getMAPMessages();
                        if (mapMessages != null) {
                            for (FastList.Node<MAPMessage> n = mapMessages.head(), end = mapMessages.tail(); (n = n.getNext()) != end;) {
                                switch (n.getValue().getMessageType()) {
                                case unstructuredSSRequest_Request:
                                    super.ussdStatAggregator.updateUssdRequestOperations();
                                    super.ussdStatAggregator.updateMessagesSent();
                                    super.ussdStatAggregator.updateMessagesAll();
                                    break;
                                case unstructuredSSNotify_Request:
                                    super.ussdStatAggregator.updateUssdNotifyOperations();
                                    super.ussdStatAggregator.updateMessagesSent();
                                    super.ussdStatAggregator.updateMessagesAll();
                                    break;
                                case processUnstructuredSSRequest_Request:
                                    super.ussdStatAggregator.updateProcessUssdRequestOperations();
                                    break;
                                case processUnstructuredSSRequest_Response:
                                    super.ussdStatAggregator.updateMessagesSent();
                                    super.ussdStatAggregator.updateMessagesAll();
                                    break;
                                }
                            }
                        }

                        this.processXmlMAPDialog(dialog, mapDialogSupplementary);

                        if (prearrangedEnd != null) {
                            mapDialogSupplementary.close(prearrangedEnd);
                            this.endHttpClientActivity(httpClientActivity);
                            this.createCDRRecord(RecordStatus.SUCCESS);
                        } else {
                            mapDialogSupplementary.send();
                        }

                    } else {
                        throw new Exception("Received Success Response but without response body");
                    }

                } catch (Exception e) {
                    throw new Exception("Error while processing 2xx", e);
                }
                break;

            default:
                // TODO Error condition, take care
                logger.severe(String.format("Received non 2xx Response=%s", response));
                this.sendServerErrorMessage();
                this.endHttpClientActivity(httpClientActivity);
                this.updateDialogFailureStat();
                this.createCDRRecord(RecordStatus.FAILED_TRANSPORT_FAILURE);

                break;
            }
        } catch (Throwable e) {
            logger.severe("Error while processing RESPONSE event", e);

            this.sendServerErrorMessage();
            this.endHttpClientActivity(httpClientActivity);
            this.updateDialogFailureStat();
            this.createCDRRecord(RecordStatus.FAILED_CORRUPTED_MESSAGE);
        }

	}

	private ActivityContextInterface getHttpClientActivityContextInterface() {
		ActivityContextInterface[] acis = this.sbbContext.getActivities();
		for (ActivityContextInterface aci : acis) {
			Object activity = aci.getActivity();
			if (activity instanceof HttpClientActivity) {
				return aci;
			}
		}
		return null;
	}

	private HttpClientActivity getHTTPClientActivity() {
		ActivityContextInterface aci = this.getHttpClientActivityContextInterface();
		if (aci != null) {
			Object activity = aci.getActivity();
			return (HttpClientActivity) activity;
		}
		return null;
	}

	private void doPost(HttpClientActivity httpClientActivity, String url, byte[] content) {

		HttpPost uriRequest = createRequest(url, null, ACCEPTED_CONTENT_TYPE, null);

		// NOTE: here we assume that its text/xml utf8 encoded... bum.
		pushContent(uriRequest, ACCEPTED_CONTENT_TYPE, CONTENT_ENCODING, content);

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

	private void pushContent(HttpUriRequest request, String contentType, String contentEncoding, byte[] content) {

		// TODO: check other preconditions?
		if (contentType != null && content != null && request instanceof HttpEntityEnclosingRequest) {
			BasicHttpEntity entity = new BasicHttpEntity();
			entity.setContent(new ByteArrayInputStream(content));
			entity.setContentLength(content.length);
			entity.setChunked(false);
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

	// -------------------------------------------------------------
	// Client abstract stuff
	// -------------------------------------------------------------

	@Override
	protected boolean checkProtocolConnection() {
		return this.getHTTPClientActivity() != null;
	}

	@Override
	protected void sendUssdData(XmlMAPDialog xmlMAPDialog /* byte[] data */) throws Exception {

		String userData = this.getUserObject();
        if (userData != null) {
            xmlMAPDialog.setUserObject(userData);
        }

		byte[] data = this.getEventsSerializeFactory().serialize(xmlMAPDialog);

		HttpClientActivity httpClientActivity = this.getHTTPClientActivity();

		if (httpClientActivity == null) {

			httpClientActivity = this.httpClientProvider.createHttpClientActivity(false, null);
			// combo
			this.httpClientActivityContextInterfaceFactory.getActivityContextInterface(httpClientActivity).attach(
					this.sbbContext.getSbbLocalObject());

			if (logger.isFineEnabled()) {
				logger.fine("Created HTTP Activity '" + httpClientActivity.getSessionId() + "' for MAPDialog "
						+ this.getMAPDialog());
			}
		}

		ScRoutingRule call = this.getCall();
		String url = call.getRuleUrl();

		doPost(httpClientActivity, url, data);
	}

    @Override
    protected void updateDialogFailureStat() {
        super.ussdStatAggregator.updateDialogsAllFailed();
        super.ussdStatAggregator.updateDialogsPullFailed();
        super.ussdStatAggregator.updateDialogsHttpFailed();
    }

	@Override
	protected void terminateProtocolConnection() {
		HttpClientActivity httpClientActivity = this.getHTTPClientActivity();
		this.endHttpClientActivity(httpClientActivity);
	}

	private void endHttpClientActivity(HttpClientActivity httpClientActivity) {
		if (httpClientActivity != null) {

			try {
				httpClientActivity.endActivity();
			} catch (Exception e) {
				logger.severe(String.format("Error while trying to end HttpClientActivity = %s for MAPDialog = %s",
						httpClientActivity.getSessionId(), this.getMAPDialog()));
			}
		}
	}

	// -------------------------------------------------------------
	// SLEE STUFF
	// -------------------------------------------------------------

	public void setSbbContext(SbbContext sbbContext) {
		super.setSbbContext(sbbContext);
		try {
            if (httpClientRATypeID == null)
                httpClientRATypeID = new ResourceAdaptorTypeID("HttpClientResourceAdaptorType", "org.restcomm", "4.0");
            try {
                super.httpClientActivityContextInterfaceFactory = (HttpClientActivityContextInterfaceFactory) super.sbbContext
                        .getActivityContextInterfaceFactory(httpClientRATypeID);
            } catch (Exception e) {
                httpClientRATypeID = new ResourceAdaptorTypeID("HttpClientResourceAdaptorType", "org.mobicents", "4.0");
                logger.info("Trying to use HttpClientResourceAdaptorType - org.mobicents");
                super.httpClientActivityContextInterfaceFactory = (HttpClientActivityContextInterfaceFactory) super.sbbContext
                        .getActivityContextInterfaceFactory(httpClientRATypeID);
            }
			super.httpClientProvider = (HttpClientResourceAdaptorSbbInterface) super.sbbContext
					.getResourceAdaptorInterface(httpClientRATypeID, httpClientRaLink);

		} catch (Exception ne) {
			super.logger.severe("Could not set SBB context:", ne);
		}
	}

    protected boolean isSip() {
        return false;
    }
}

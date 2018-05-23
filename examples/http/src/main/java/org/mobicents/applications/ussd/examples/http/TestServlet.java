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

package org.mobicents.applications.ussd.examples.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingCategory;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.protocols.ss7.map.primitives.AddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.restcomm.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSResponseImpl;
import org.restcomm.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.mobicents.ussdgateway.EventsSerializeFactory;
import org.mobicents.ussdgateway.XmlMAPDialog;

/**
 * 
 * @author amit bhayani
 * 
 */
public class TestServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(TestServlet.class);

	private EventsSerializeFactory factory = null;

	@Override
	public void init() {
		factory = new EventsSerializeFactory();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<h1>Hello USSD Demo Get</h1>");
		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		USSDStringImpl ussdStr = new USSDStringImpl(
				"USSD String : Hello World\n 1. Balance\n 2. Texts Remaining".getBytes(), cbsDataCodingScheme);
		UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(cbsDataCodingScheme,
				ussdStr, null, null);

		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		ISDNAddressStringImpl isdnAddress = new ISDNAddressStringImpl(AddressNature.international_number,
				NumberingPlan.ISDN, "79273605819");
		AlertingPatternImpl alertingPattern = new AlertingPatternImpl(AlertingCategory.Category3);
		ProcessUnstructuredSSRequestImpl processUnstructuredSSRequestIndication = new ProcessUnstructuredSSRequestImpl(
				cbsDataCodingScheme, ussdStr, alertingPattern, isdnAddress);

		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		XmlMAPDialog copy = new XmlMAPDialog(appCtx, orgAddress, dstAddress, 12l, 13l, destReference, origReference);
		copy.addMAPMessage(unstructuredSSRequestIndication);

		try {
			byte[] data = factory.serialize(copy);
			System.out.println(Arrays.toString(data));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

		out.println("</body>");
		out.println("</html>");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletInputStream is = request.getInputStream();
		try {
			XmlMAPDialog original = factory.deserialize(is);
			HttpSession session = request.getSession(true);
			if (logger.isInfoEnabled()) {
				logger.info("doPost. HttpSession=" + session.getId() + " Dialog = " + original);
			}

			USSDString ussdStr = null;
			byte[] data = null;

			final FastList<MAPMessage> capMessages = original.getMAPMessages();

			MessageType messageType = original.getTCAPMessageType();

			// This is initial request, if its not NTFY, we need session
			for (FastList.Node<MAPMessage> n = capMessages.head(), end = capMessages.tail(); (n = n.getNext()) != end;) {
				final MAPMessage rawMessage = n.getValue();
				final MAPMessageType type = rawMessage.getMessageType();

				switch (messageType) {
				case Begin:
					switch (type) {
					case processUnstructuredSSRequest_Request:
						ProcessUnstructuredSSRequest processUnstructuredSSRequest = (ProcessUnstructuredSSRequest) rawMessage;
						CBSDataCodingScheme cbsDataCodingScheme = processUnstructuredSSRequest.getDataCodingScheme();
						if (logger.isInfoEnabled()) {
							logger.info("Received ProcessUnstructuredSSRequestIndication USSD String="
									+ processUnstructuredSSRequest.getUSSDString().getString(null));
						}
						session.setAttribute("ProcessUnstructuredSSRequest_InvokeId",
								processUnstructuredSSRequest.getInvokeId());

						// You business logic here and finally send back
						// response
						
						//Urdu
//						CBSDataCodingScheme cbsDataCodingSchemeUrdu = new  CBSDataCodingSchemeImpl(72);
//						ussdStr = new USSDStringImpl("\u062C\u0645\u064A\u0639 \u0627\u0644\u0645\u0633\u062A\u0639\u0645\u0644\u064A\u0646 \u0627\u0644\u0622\u062E\u0631\u064A\u0646 \u062A\u0645 \u0625\u0636\u0627\u0641\u062A\u0647\u0645",
//								cbsDataCodingSchemeUrdu, null);
//						UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(
//								cbsDataCodingSchemeUrdu, ussdStr, null, null);

						//English
						ussdStr = new USSDStringImpl("USSD String : Hello World\n 1. Balance\n 2. Texts Remaining",
								cbsDataCodingScheme, null);
						UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(
								cbsDataCodingScheme, ussdStr, null, null);

						original.reset();
						original.setUserObject("Session Id : "+session.getId());
						original.setTCAPMessageType(MessageType.Continue);
						original.setCustomInvokeTimeOut(25000); //Custom Invoke Timeout to 25sec
						original.addMAPMessage(unstructuredSSRequestIndication);

						data = factory.serialize(original);

						response.getOutputStream().write(data);
						response.flushBuffer();

						break;
					default:
						// This is error. If its begin it should be only Process
						// Unstructured SS Request
						logger.error("Received Dialog BEGIN but message is not ProcessUnstructuredSSRequestIndication. Message="
								+ rawMessage);
						break;
					}

					break;
				case Continue:
					switch (type) {
					case unstructuredSSRequest_Response:
						UnstructuredSSResponse unstructuredSSResponse = (UnstructuredSSResponseImpl) rawMessage;

						CBSDataCodingScheme cbsDataCodingScheme = unstructuredSSResponse.getDataCodingScheme();

						long invokeId = (Long) session.getAttribute("ProcessUnstructuredSSRequest_InvokeId");

						USSDString ussdStringObj = unstructuredSSResponse.getUSSDString();
						String ussdString = null;
						if (ussdStringObj != null) {
							ussdString = ussdStringObj.getString(null);
						}

						logger.info("Received UnstructuredSSResponse USSD String=" + ussdString + " HttpSession="
								+ session.getId() + " invokeId=" + invokeId);

						cbsDataCodingScheme = new CBSDataCodingSchemeImpl(0x0f);
						ussdStr = new USSDStringImpl("Thank You!", null, null);
						ProcessUnstructuredSSResponse processUnstructuredSSResponse = new ProcessUnstructuredSSResponseImpl(
								cbsDataCodingScheme, ussdStr);
						processUnstructuredSSResponse.setInvokeId(invokeId);

						original.reset();
						original.setTCAPMessageType(MessageType.End);
						original.addMAPMessage(processUnstructuredSSResponse);
						original.close(false);

						data = factory.serialize(original);

						response.getOutputStream().write(data);
						response.flushBuffer();

						try {
							session.invalidate();
						} catch (Exception e) {
							session.invalidate();
							logger.error("Error while invalidating HttpSession=" + session.getId());
						}
						break;
					default:
						// This is error. If its begin it should be only Process
						// Unstructured SS Request
						logger.error("Received Dialog CONTINUE but message is not UnstructuredSSResponseIndication. Message="
								+ rawMessage);
						break;
					}

					break;

				case Abort:
					// The Dialog is aborted, lets do cleaning here

					try {
						session.invalidate();
					} catch (Exception e) {
						session.invalidate();
						logger.error("Error while invalidating HttpSession=" + session.getId());
					}
					break;
				}

			}

		} catch (XMLStreamException e) {
			logger.error("Error while processing received XML", e);
		} catch (MAPException e) {
			e.printStackTrace();
		}

	}
}

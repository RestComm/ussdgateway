package org.mobicents.applications.ussd.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequest;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.mobicents.protocols.ss7.map.primitives.USSDStringImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSResponseImpl;
import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.DialogType;
import org.mobicents.ussdgateway.EventsSerializeFactory;

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
		try {
			factory = new EventsSerializeFactory();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<h1>Hello USSD Demo Get</h1>");

		USSDStringImpl ussdStr = new USSDStringImpl("USSD String : Hello World\n 1. Balance\n 2. Texts Remaining", null);
		UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl((byte) 0x0f, ussdStr,
				null, null);

		Dialog copy = new Dialog(DialogType.CONTINUE, 1l, null, null, unstructuredSSRequestIndication);

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
			Dialog original = factory.deserialize(is);
			HttpSession session = request.getSession(true);
			if (logger.isInfoEnabled()) {
				logger.info("doPost. HttpSession=" + session.getId() + " Dialog = " + original);
			}

			USSDString ussdStr = null;
			byte[] data = null;
			MAPMessage mapMessage = original.getMAPMessage();
			switch (original.getType()) {
			case BEGIN:
				switch (mapMessage.getMessageType()) {
				case processUnstructuredSSRequest_Request:
					ProcessUnstructuredSSRequest processUnstructuredSSRequest = (ProcessUnstructuredSSRequest) mapMessage;
					if (logger.isInfoEnabled()) {
						logger.info("Received ProcessUnstructuredSSRequestIndication USSD String="
								+ processUnstructuredSSRequest.getUSSDString().getString());
						session.setAttribute("ProcessUnstructuredSSRequest_InvokeId",
								processUnstructuredSSRequest.getInvokeId());
					}

					ussdStr = new USSDStringImpl("USSD String : Hello World\n 1. Balance\n 2. Texts Remaining", null);
					UnstructuredSSRequest unstructuredSSRequestIndication = new UnstructuredSSRequestImpl((byte) 0x0f,
							ussdStr, null, null);

					Dialog copy = new Dialog(DialogType.CONTINUE, original.getId(), null, null,
							unstructuredSSRequestIndication);

					data = factory.serialize(copy);

					response.getOutputStream().write(data);
					response.flushBuffer();

					break;
				default:
					// This is error. If its begin it should be only Process
					// Unstructured SS Request
					logger.error("Received Dialog BEGIN but message is not ProcessUnstructuredSSRequestIndication. Message="
							+ mapMessage);
					break;
				}

				break;
			case CONTINUE:
				switch (mapMessage.getMessageType()) {
				case unstructuredSSRequest_Response:
					UnstructuredSSResponse unstructuredSSResponse = (UnstructuredSSResponseImpl) mapMessage;

					long invokeId = (Long) session.getAttribute("ProcessUnstructuredSSRequest_InvokeId");

					logger.info("Received UnstructuredSSResponse USSD String="
							+ unstructuredSSResponse.getUSSDString().getString() + " HttpSession=" + session.getId()
							+ " invokeId=" + invokeId);

					ussdStr = new USSDStringImpl("Thank You!", null);
					ProcessUnstructuredSSResponseImpl processUnstructuredSSResponseIndication = new ProcessUnstructuredSSResponseImpl(
							(byte) 0x0f, ussdStr);
					processUnstructuredSSResponseIndication.setInvokeId(invokeId);

					Dialog copy1 = new Dialog(DialogType.END, original.getId(), processUnstructuredSSResponseIndication);

					data = factory.serialize(copy1);

					response.getOutputStream().write(data);
					response.flushBuffer();

					try {

					} catch (Exception e) {
						session.invalidate();
						logger.error("Error while invalidating HttpSession=" + session.getId());
					}
					break;
				default:
					// This is error. If its begin it should be only Process
					// Unstructured SS Request
					logger.error("Received Dialog CONTINUE but message is not UnstructuredSSResponseIndication. Message="
							+ mapMessage);
					break;
				}

				break;

			}

		} catch (XMLStreamException e) {
			logger.error("Error while processing received XML", e);
		}

	}
}

package org.mobicents.ussdgateway;

import java.io.Serializable;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponseIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponseIndication;
import org.mobicents.protocols.ss7.map.primitives.AddressStringImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSResponseIndicationImpl;

/**
 * @author amit bhayani
 * 
 */
public class Dialog implements Serializable {
	private static final String DESTINATION_REFERENCE = "destinationReference";
	private static final String ORIGINATION_REFERENCE = "originationReference";
	private static final String PROCESS_UNSTRUCTURED_SS_REQUEST = "processUnstructuredSSRequest";
	private static final String PROCESS_UNSTRUCTURED_SS_RESPONSE = "processUnstructuredSSResponse";
	private static final String UNSTRUCTURED_SS_REQUEST = "unstructuredSSRequest";
	private static final String UNSTRUCTURED_SS_RESPONSE = "unstructuredSSResponse";
	private static final String UNSTRUCTURED_SS_NOTIFY_REQUEST = "unstructuredSSNotifyRequest";

	// MAP−OpenInfo
	// TODO : DO we add IMSI and vlrNo for EMAP?
	private AddressString destReference;
	private AddressString origReference;

	private ProcessUnstructuredSSRequestIndication processUnstructuredSSRequest = null;
	private UnstructuredSSRequestIndication unstructuredSSRequest = null;
	private ProcessUnstructuredSSResponseIndication processUnstructuredSSResponse = null;
	private UnstructuredSSResponseIndication unstructuredSSResponse = null;
	private UnstructuredSSNotifyRequestIndication unstructuredSSNotifyRequest = null;
	private DialogType type = null;
	private Long id;

	public Dialog() {

	}

	public Dialog(DialogType dialogType, Long id, AddressString destReference, AddressString origReference) {
		this.type = dialogType;
		this.id = id;
		this.destReference = destReference;
		this.origReference = origReference;
	}

	public Dialog(DialogType dialogType, Long id, AddressString destReference, AddressString origReference,
			UnstructuredSSNotifyRequestIndication unstructuredSSNotify) {
		this(dialogType, id, destReference, origReference);
		this.unstructuredSSNotifyRequest = unstructuredSSNotify;
	}

	public Dialog(DialogType dialogType, Long id, AddressString destReference, AddressString origReference,
			ProcessUnstructuredSSRequestIndication processUnstructuredSSRequest) {
		this(dialogType, id, destReference, origReference);
		this.processUnstructuredSSRequest = processUnstructuredSSRequest;
	}

	public Dialog(DialogType dialogType, Long id, ProcessUnstructuredSSResponseIndication processUnstructuredSSResponse) {
		this(dialogType, id, null, null);
		this.processUnstructuredSSResponse = processUnstructuredSSResponse;
	}

	public Dialog(DialogType dialogType, Long id, AddressString destReference, AddressString origReference,
			UnstructuredSSRequestIndication unstructuredSSRequest) {
		this(dialogType, id, destReference, origReference);
		this.unstructuredSSRequest = unstructuredSSRequest;
	}

	public Dialog(DialogType dialogType, Long id, UnstructuredSSResponseIndication unstructuredSSResponse) {
		this(dialogType, id, null, null);
		this.unstructuredSSResponse = unstructuredSSResponse;
	}

	public AddressString getDestReference() {
		return destReference;
	}

	public AddressString getOrigReference() {
		return origReference;
	}

	public ProcessUnstructuredSSRequestIndication getProcessUnstructuredSSRequest() {
		return processUnstructuredSSRequest;
	}

	public void setProcessUnstructuredSSRequest(ProcessUnstructuredSSRequestIndication processUnstructuredSSRequest) {
		this.processUnstructuredSSRequest = processUnstructuredSSRequest;
	}

	public ProcessUnstructuredSSResponseIndication getProcessUnstructuredSSResponse() {
		return processUnstructuredSSResponse;
	}

	public void setProcessUnstructuredSSResponse(ProcessUnstructuredSSResponseIndication processUnstructuredSSResponse) {
		this.processUnstructuredSSResponse = processUnstructuredSSResponse;
	}

	public UnstructuredSSRequestIndication getUnstructuredSSRequest() {
		return unstructuredSSRequest;
	}

	public void setUnstructuredSSRequest(UnstructuredSSRequestIndication unstructuredSSRequest) {
		this.unstructuredSSRequest = unstructuredSSRequest;
	}

	public UnstructuredSSResponseIndication getUnstructuredSSResponse() {
		return unstructuredSSResponse;
	}

	public void setUnstructuredSSResponse(UnstructuredSSResponseIndication unstructuredSSResponse) {
		this.unstructuredSSResponse = unstructuredSSResponse;
	}

	public UnstructuredSSNotifyRequestIndication getUnstructuredSSNotifyRequest() {
		return unstructuredSSNotifyRequest;
	}

	public void setUnstructuredSSNotifyRequest(UnstructuredSSNotifyRequestIndication unstructuredSSNotifyRequest) {
		this.unstructuredSSNotifyRequest = unstructuredSSNotifyRequest;
	}

	public DialogType getType() {
		return type;
	}

	public Long getId() {
		return id;
	}

	protected static final XMLFormat<Dialog> USSR_XML = new XMLFormat<Dialog>(Dialog.class) {
		public void write(Dialog dialog, OutputElement xml) throws XMLStreamException {
			xml.setAttribute("type", dialog.type != null ? dialog.type.getType() : null);
			xml.setAttribute("id", dialog.id);
			xml.add((AddressStringImpl) dialog.destReference, DESTINATION_REFERENCE, AddressStringImpl.class);
			xml.add((AddressStringImpl) dialog.origReference, ORIGINATION_REFERENCE, AddressStringImpl.class);

			xml.add((ProcessUnstructuredSSRequestIndicationImpl) dialog.processUnstructuredSSRequest,
					PROCESS_UNSTRUCTURED_SS_REQUEST, ProcessUnstructuredSSRequestIndicationImpl.class);

			xml.add((ProcessUnstructuredSSResponseIndicationImpl) dialog.processUnstructuredSSResponse,
					PROCESS_UNSTRUCTURED_SS_RESPONSE, ProcessUnstructuredSSResponseIndicationImpl.class);

			xml.add((UnstructuredSSRequestIndicationImpl) dialog.unstructuredSSRequest, UNSTRUCTURED_SS_REQUEST,
					UnstructuredSSRequestIndicationImpl.class);

			xml.add((UnstructuredSSResponseIndicationImpl) dialog.unstructuredSSResponse, UNSTRUCTURED_SS_RESPONSE,
					UnstructuredSSResponseIndicationImpl.class);

			xml.add((UnstructuredSSNotifyRequestIndicationImpl) dialog.unstructuredSSNotifyRequest,
					UNSTRUCTURED_SS_NOTIFY_REQUEST, UnstructuredSSNotifyRequestIndicationImpl.class);

		}

		public void read(InputElement xml, Dialog dialog) throws XMLStreamException {
			dialog.type = DialogType.getInstance(xml.getAttribute("type", null));
			dialog.id = xml.getAttribute("id", 0l);
			dialog.destReference = xml.get(DESTINATION_REFERENCE, AddressStringImpl.class);
			dialog.origReference = xml.get(ORIGINATION_REFERENCE, AddressStringImpl.class);
			dialog.processUnstructuredSSRequest = xml.get(PROCESS_UNSTRUCTURED_SS_REQUEST,
					ProcessUnstructuredSSRequestIndicationImpl.class);

			dialog.processUnstructuredSSResponse = xml.get(PROCESS_UNSTRUCTURED_SS_RESPONSE,
					ProcessUnstructuredSSResponseIndicationImpl.class);

			dialog.unstructuredSSRequest = xml.get(UNSTRUCTURED_SS_REQUEST, UnstructuredSSRequestIndicationImpl.class);

			dialog.unstructuredSSResponse = xml.get(UNSTRUCTURED_SS_RESPONSE,
					UnstructuredSSResponseIndicationImpl.class);

			dialog.unstructuredSSNotifyRequest = xml.get(UNSTRUCTURED_SS_NOTIFY_REQUEST,
					UnstructuredSSNotifyRequestIndicationImpl.class);
		}
	};

	@Override
	public String toString() {
		return "Dialog [destReference=" + destReference + ", origReference=" + origReference + ", type=" + type
				+ ", id=" + id + ", processUnstructuredSSRequest=" + processUnstructuredSSRequest
				+ ", unstructuredSSRequest=" + unstructuredSSRequest + ", processUnstructuredSSResponse="
				+ processUnstructuredSSResponse + ", unstructuredSSResponse=" + unstructuredSSResponse
				+ ", unstructuredSSNotifyRequest=" + unstructuredSSNotifyRequest + "]";
	}

}
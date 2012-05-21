package org.mobicents.ussdgateway;

import java.io.Serializable;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
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

	private MAPMessage mapMessage = null;
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
			MAPMessage mapMessage) {
		this(dialogType, id, destReference, origReference);
		this.mapMessage = mapMessage;
	}

	public Dialog(DialogType dialogType, Long id, MAPMessage mapMessage) {
		this(dialogType, id, null, null);
		this.mapMessage = mapMessage;
	}

	public AddressString getDestReference() {
		return destReference;
	}

	public AddressString getOrigReference() {
		return origReference;
	}

	public MAPMessage getMAPMessage() {
		return this.mapMessage;
	}

	public void setMAPMessage(MAPMessage mapMessage) {
		this.mapMessage = mapMessage;
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

			MAPMessage mapMessage = dialog.getMAPMessage();

			if (mapMessage == null) {
				return;
			}

			switch (mapMessage.getMessageType()) {
			case processUnstructuredSSRequest_Request:
				xml.add((ProcessUnstructuredSSRequestIndicationImpl) mapMessage, PROCESS_UNSTRUCTURED_SS_REQUEST,
						ProcessUnstructuredSSRequestIndicationImpl.class);
				break;
			case processUnstructuredSSRequest_Response:
				xml.add((ProcessUnstructuredSSResponseIndicationImpl) mapMessage, PROCESS_UNSTRUCTURED_SS_RESPONSE,
						ProcessUnstructuredSSResponseIndicationImpl.class);
				break;

			case unstructuredSSRequest_Request:

				xml.add((UnstructuredSSRequestIndicationImpl) mapMessage, UNSTRUCTURED_SS_REQUEST,
						UnstructuredSSRequestIndicationImpl.class);
				break;

			case unstructuredSSRequest_Response:

				xml.add((UnstructuredSSResponseIndicationImpl) mapMessage, UNSTRUCTURED_SS_RESPONSE,
						UnstructuredSSResponseIndicationImpl.class);
				break;

			case unstructuredSSNotify_Request:

				xml.add((UnstructuredSSNotifyRequestIndicationImpl) mapMessage, UNSTRUCTURED_SS_NOTIFY_REQUEST,
						UnstructuredSSNotifyRequestIndicationImpl.class);
				break;
			default:
				break;
			}

		}

		public void read(InputElement xml, Dialog dialog) throws XMLStreamException {
			dialog.type = DialogType.getInstance(xml.getAttribute("type", null));
			dialog.id = xml.getAttribute("id", 0l);
			dialog.destReference = xml.get(DESTINATION_REFERENCE, AddressStringImpl.class);
			dialog.origReference = xml.get(ORIGINATION_REFERENCE, AddressStringImpl.class);

			MAPMessage mapMessage = xml.get(PROCESS_UNSTRUCTURED_SS_REQUEST,
					ProcessUnstructuredSSRequestIndicationImpl.class);

			if (mapMessage == null) {
				mapMessage = xml.get(PROCESS_UNSTRUCTURED_SS_RESPONSE,
						ProcessUnstructuredSSResponseIndicationImpl.class);
			}

			if (mapMessage == null) {
				mapMessage = xml.get(UNSTRUCTURED_SS_REQUEST, UnstructuredSSRequestIndicationImpl.class);
			}

			if (mapMessage == null) {
				mapMessage = xml.get(UNSTRUCTURED_SS_RESPONSE, UnstructuredSSResponseIndicationImpl.class);
			}

			if (mapMessage == null) {
				mapMessage = xml.get(UNSTRUCTURED_SS_NOTIFY_REQUEST, UnstructuredSSNotifyRequestIndicationImpl.class);
			}

			dialog.setMAPMessage(mapMessage);
		}
	};

	@Override
	public String toString() {
		return "Dialog [destReference=" + destReference + ", origReference=" + origReference + ", mapMessage="
				+ mapMessage + ", type=" + type + ", id=" + id + "]";
	}

}
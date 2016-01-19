/**
 * 
 */
package org.mobicents.ussdgateway;

import javolution.text.CharArray;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPMessageType;

/**
 * @author Amit Bhayani
 * 
 */
public class MessageType {

	public static final String MESSAGE_TYPE = "message-type";

	private MAPMessageType mapMessageType;

	/**
	 * 
	 */
	public MessageType() {
		// TODO Auto-generated constructor stub
	}

	public MessageType(MAPMessageType mapMessageType) {
		this.mapMessageType = mapMessageType;
	}

	public MAPMessageType getType() {
		return mapMessageType;
	}

	public void setType(MAPMessageType mapMessageType) {
		this.mapMessageType = mapMessageType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapMessageType == null) ? 0 : mapMessageType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageType other = (MessageType) obj;
		if (mapMessageType != other.mapMessageType)
			return false;
		return true;
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<MessageType> MAP_MESSAGE_TYPE_XML = new XMLFormat<MessageType>(MessageType.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml, MessageType anyExt) throws XMLStreamException {

			CharArray mapMessageTypeStr = xml.getText();
			if (mapMessageTypeStr != null) {
				anyExt.mapMessageType = MAPMessageType.valueOf(mapMessageTypeStr.toString());
			}

		}

		@Override
		public void write(MessageType anyExt, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
			if (anyExt.mapMessageType != null)
				xml.addText(anyExt.mapMessageType.name());

		}
	};

}

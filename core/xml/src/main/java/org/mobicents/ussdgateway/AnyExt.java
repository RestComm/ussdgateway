/**
 * 
 */
package org.mobicents.ussdgateway;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPMessageType;

/**
 * @author Amit Bhayani
 * 
 */
public class AnyExt implements XMLSerializable {

	public static final String MESSAGE_TYPE = "message-type";

	private MessageType messageType;

	/**
	 * 
	 */
	public AnyExt() {
		// TODO Auto-generated constructor stub
	}

	public AnyExt(MAPMessageType mapMessageType) {
		this.messageType = new MessageType(mapMessageType);
	}

	public MAPMessageType getMapMessageType() {
		if (this.messageType != null) {
			return messageType.getType();
		}

		return null;
	}

	public void setMapMessageType(MAPMessageType mapMessageType) {
		if (this.messageType == null) {
			this.messageType = new MessageType();
		}

		this.messageType.setType(mapMessageType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
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
		AnyExt other = (AnyExt) obj;
		if (messageType == null) {
			if (other.messageType != null)
				return false;
		} else if (!messageType.equals(other.messageType))
			return false;
		return true;
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<AnyExt> ANY_EXT_XML = new XMLFormat<AnyExt>(AnyExt.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml, AnyExt anyExt) throws XMLStreamException {
			anyExt.messageType = xml.get(MESSAGE_TYPE, MessageType.class);

		}

		@Override
		public void write(AnyExt anyExt, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
			if (anyExt.messageType != null)
				xml.add(anyExt.messageType, MESSAGE_TYPE, MessageType.class);

		}
	};

}

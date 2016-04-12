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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageAbsentSubscriberImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageAbsentSubscriberSMImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageBusySubscriberImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageCUGRejectImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageCallBarredImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageExtensionContainerImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageFacilityNotSupImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageParameterlessImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessagePositionMethodFailureImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessagePwRegistrationFailureImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageRoamingNotAllowedImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageSMDeliveryFailureImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageSsErrorStatusImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageSsIncompatibilityImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageSubscriberBusyForMtSmsImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageSystemFailureImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageUnauthorizedLCSClientImpl;
import org.mobicents.protocols.ss7.map.errors.MAPErrorMessageUnknownSubscriberImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0001Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0010Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0011Impl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0001;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0010;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0011;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle0100;

/**
 * <p>
 * Factory Object used to serialize/de-serialize the {@link XmlMAPDialog}
 * objects
 * </p>
 * 
 * @author amit bhayani
 * 
 */
public class EventsSerializeFactory {

	private static final String DIALOG = "dialog";
	private static final String TYPE = "type";
	private static final String TAB = "\t";

	final XMLBinding binding = new XMLBinding();

    private Charset charset = Charset.forName("UTF-8");

	public EventsSerializeFactory() {
		//MAPErrorMessage classes
		binding.setAlias(MAPErrorMessageExtensionContainerImpl.class, ErrorComponentMap.MAP_ERROR_EXT_CONTAINER);
		binding.setAlias(MAPErrorMessageSMDeliveryFailureImpl.class, ErrorComponentMap.MAP_ERROR_SM_DEL_FAILURE);
		binding.setAlias(MAPErrorMessageAbsentSubscriberSMImpl.class, ErrorComponentMap.MAP_ERROR_ABSENT_SUBS_SM);
		binding.setAlias(MAPErrorMessageSystemFailureImpl.class, ErrorComponentMap.MAP_ERROR_SYSTEM_FAILURE);
		binding.setAlias(MAPErrorMessageCallBarredImpl.class, ErrorComponentMap.MAP_ERROR_CALL_BARRED);
		binding.setAlias(MAPErrorMessageFacilityNotSupImpl.class, ErrorComponentMap.MAP_ERROR_FACILITY_NOT_SUPPORTED);
		binding.setAlias(MAPErrorMessageUnknownSubscriberImpl.class, ErrorComponentMap.MAP_ERROR_UNKNOWN_SUBS);
		binding.setAlias(MAPErrorMessageSubscriberBusyForMtSmsImpl.class, ErrorComponentMap.MAP_ERROR_SUBS_BUSY_FOR_MT_SMS);
		binding.setAlias(MAPErrorMessageAbsentSubscriberImpl.class, ErrorComponentMap.MAP_ERROR_ABSENT_SUBS);
		binding.setAlias(MAPErrorMessageUnauthorizedLCSClientImpl.class, ErrorComponentMap.MAP_ERROR_UNAUTHORIZED_LCS_CLIENT);
		binding.setAlias(MAPErrorMessagePositionMethodFailureImpl.class, ErrorComponentMap.MAP_ERROR_POSITION_METHOD_FAIL);
		binding.setAlias(MAPErrorMessageBusySubscriberImpl.class, ErrorComponentMap.MAP_ERROR_BUSY_SUBS);
		binding.setAlias(MAPErrorMessageCUGRejectImpl.class, ErrorComponentMap.MAP_ERROR_CUG_REJECT);
		binding.setAlias(MAPErrorMessageRoamingNotAllowedImpl.class, ErrorComponentMap.MAP_ERROR_ROAMING_NOT_ALLOWED);
		binding.setAlias(MAPErrorMessageSsErrorStatusImpl.class, ErrorComponentMap.MAP_ERROR_SS_ERROR_STATUS);
		binding.setAlias(MAPErrorMessageSsIncompatibilityImpl.class, ErrorComponentMap.MAP_ERROR_SS_INCOMPATIBILITY);
		binding.setAlias(MAPErrorMessagePwRegistrationFailureImpl.class, ErrorComponentMap.MAP_ERROR_PW_REGS_FAIL);
		binding.setAlias(MAPErrorMessageParameterlessImpl.class, ErrorComponentMap.MAP_ERROR_PARAM_LESS);
		
		//SCCP Gt classes
		binding.setAlias(GlobalTitle0001Impl.class, GlobalTitle0001.class.getSimpleName());
		binding.setAlias(GlobalTitle0010Impl.class, GlobalTitle0010.class.getSimpleName());
		binding.setAlias(GlobalTitle0011Impl.class, GlobalTitle0011.class.getSimpleName());
		binding.setAlias(GlobalTitle0100Impl.class, GlobalTitle0100.class.getSimpleName());
		
		binding.setAlias(XmlMAPDialog.class, DIALOG);
		
		binding.setClassAttribute(TYPE);
	}

	/**
	 * Serialize passed {@link XmlMAPDialog} object
	 * 
	 * @param dialog
	 * @return serialized byte array
	 * @throws XMLStreamException
	 *             Exception if serialization fails
	 */
	public byte[] serialize(XmlMAPDialog dialog) throws XMLStreamException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XMLObjectWriter writer = XMLObjectWriter.newInstance(baos);

		try {

			writer.setBinding(binding);
			writer.setIndentation(TAB);

			writer.write(dialog, DIALOG, XmlMAPDialog.class);
			writer.flush();
			byte[] data = baos.toByteArray();

			return data;
		} finally {
			writer.close();
		}
	}

	/**
	 * Serialize passed {@link SipUssdMessage} object
	 * 
	 * @param dialog
	 * @return serialized byte array
	 * @throws XMLStreamException
	 *             Exception if serialization fails
	 */
	public byte[] serializeSipUssdMessage(SipUssdMessage sipUssdMessage) throws XMLStreamException {

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final XMLObjectWriter writer = XMLObjectWriter.newInstance(baos);

		try {

			writer.setBinding(binding);
			writer.setIndentation(TAB);

			writer.write(sipUssdMessage, SipUssdMessage.USSD_DATA, SipUssdMessage.class);
			writer.flush();
			byte[] data = baos.toByteArray();

			return data;
		} finally {
			writer.close();
		}
	}

	public SipUssdMessage deserializeSipUssdMessage(byte[] data) throws XMLStreamException {

        // this is a workaround because of javolution xml lib does not parse properly hex values like
        // &#xa; and we need to replace them to value like &#10;
        // TODO: we need remove it after we switch to javolution 6.1 lib
        String s1 = new String(data, charset);
        String s2 = s1.replaceAll("&#xa;", "&#10;");
        String s3 = s2.replaceAll("&#XA;", "&#10;");
        String s4 = s3.replaceAll("&#xd;", "&#13;");
        String sn = s4.replaceAll("&#XD;", "&#13;");
        byte[] data2 = sn.getBytes(charset);

	    final ByteArrayInputStream bais = new ByteArrayInputStream(data2);
		final XMLObjectReader reader = XMLObjectReader.newInstance(bais);
		try {
			reader.setBinding(binding);
			SipUssdMessage sipUssdMessage = reader.read(SipUssdMessage.USSD_DATA, SipUssdMessage.class);
			return sipUssdMessage;
		} finally {
			reader.close();
		}
	}

	public SipUssdMessage deserializeSipUssdMessage(InputStream is) throws XMLStreamException {
		final XMLObjectReader reader = XMLObjectReader.newInstance(is);
		try {
			reader.setBinding(binding);
			SipUssdMessage sipUssdMessage = reader.read(SipUssdMessage.USSD_DATA, SipUssdMessage.class);
			return sipUssdMessage;
		} finally {
			reader.close();
		}
	}

	/**
	 * De-serialize the byte[] into {@link XmlMAPDialog} object
	 * 
	 * @param data
	 * @return de-serialized Dialog Object
	 * @throws XMLStreamException
	 *             Exception if de-serialization fails
	 */
	public XmlMAPDialog deserialize(byte[] data) throws XMLStreamException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(data);
		final XMLObjectReader reader = XMLObjectReader.newInstance(bais);
		try {
			reader.setBinding(binding);
			XmlMAPDialog dialog = reader.read(DIALOG, XmlMAPDialog.class);
			return dialog;
		} finally {
			reader.close();
		}
	}

	/**
	 * De-serialize passed {@link InputStream} into {@link XmlMAPDialog} object
	 * 
	 * @param is
	 * @return de-serialized Dialog Object
	 * @throws XMLStreamException
	 *             Exception if de-serialization fails
	 */
	public XmlMAPDialog deserialize(InputStream is) throws XMLStreamException {
		final XMLObjectReader reader = XMLObjectReader.newInstance(is);
		try {
			reader.setBinding(binding);
			XmlMAPDialog dialog = reader.read(DIALOG, XmlMAPDialog.class);
			return dialog;
		} finally {
			reader.close();
		}
	}
}

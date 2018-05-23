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

package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;

import org.restcomm.protocols.ss7.map.api.MAPMessageType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class MessageTypeTest {

	private static final String TAB = "\t";
	private static final String TYPE = "type";

	final XMLBinding binding = new XMLBinding();

	/**
	 * 
	 */
	public MessageTypeTest() {
		binding.setClassAttribute(TYPE);
	}

	@BeforeClass
	public void setUpClass() throws Exception {
	}

	@AfterClass
	public void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

	@Test(groups = { "Sip" })
	public void testXMLSerialize() throws Exception {
		MessageType anyExt = new MessageType(MAPMessageType.unstructuredSSRequest_Request);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLObjectWriter writer = XMLObjectWriter.newInstance(baos);

		writer.setBinding(binding);
		writer.setIndentation(TAB);

		writer.write(anyExt, MessageType.MESSAGE_TYPE, MessageType.class);
		writer.flush();
		byte[] data = baos.toByteArray();

		writer.close();

		System.out.println(new String(data));

		final ByteArrayInputStream bais = new ByteArrayInputStream(data);
		final XMLObjectReader reader = XMLObjectReader.newInstance(bais);
		reader.setBinding(binding);
		MessageType copy = reader.read(MessageType.MESSAGE_TYPE, MessageType.class);

		reader.close();
		
		assertEquals(copy, anyExt);

	}

}

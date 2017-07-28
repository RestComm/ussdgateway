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

package org.mobicents.ussdgateway.rules;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class ScRoutingRuleTest {

	final XMLBinding binding = new XMLBinding();
	private static final String TAB = "\t";

	/**
	 * 
	 */
	public ScRoutingRuleTest() {
		// TODO Auto-generated constructor stub
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

	@Test
	public void testSerialization() throws XMLStreamException {
		ScRoutingRule scRule = new ScRoutingRule();
		scRule.setRuleType(ScRoutingRuleType.SIP);
		scRule.setShortCode("*123#");
		scRule.setSipProxy("127.0.0.1:5060");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLObjectWriter writer = XMLObjectWriter.newInstance(baos);

		writer.setBinding(binding);
		writer.setIndentation(TAB);

		writer.write(scRule, "ScRoutingRule", ScRoutingRule.class);
		writer.flush();
		byte[] data = baos.toByteArray();

		System.out.println(new String(data));

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		XMLObjectReader reader = XMLObjectReader.newInstance(bais);
		reader.setBinding(binding);
		ScRoutingRule copy = reader.read("ScRoutingRule", ScRoutingRule.class);

		assertEquals(copy, scRule);

		scRule = new ScRoutingRule();
		scRule.setRuleType(ScRoutingRuleType.HTTP);
		scRule.setShortCode("*123#");
		scRule.setRuleUrl("http://localhost:8080/ussddemo/test");

		baos = new ByteArrayOutputStream();
		writer = XMLObjectWriter.newInstance(baos);

		writer.setBinding(binding);
		writer.setIndentation(TAB);

		writer.write(scRule, "ScRoutingRule", ScRoutingRule.class);
		writer.flush();
		data = baos.toByteArray();

		System.out.println(new String(data));

		bais = new ByteArrayInputStream(data);
		reader = XMLObjectReader.newInstance(bais);
		reader.setBinding(binding);
		copy = reader.read("ScRoutingRule", ScRoutingRule.class);

		assertEquals(copy, scRule);

	}

}

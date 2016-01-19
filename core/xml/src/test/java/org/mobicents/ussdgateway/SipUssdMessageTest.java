/*
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.mobicents.protocols.ss7.map.MAPProviderImpl;
import org.mobicents.protocols.ss7.map.api.MAPMessageType;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSDataCodingGroup;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSNationalLanguage;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorCode;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.smstpdu.CharacterSet;
import org.mobicents.protocols.ss7.map.api.smstpdu.DataCodingSchemaMessageClass;
import org.mobicents.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.mobicents.protocols.ss7.map.primitives.USSDStringImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class SipUssdMessageTest {

	private static String ussdM = "*1000#";

	private EventsSerializeFactory factory = null;

	@BeforeClass
	public void setUpClass() throws Exception {
		factory = new EventsSerializeFactory();
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
	public void testCreatingXMLSerialize() throws Exception {
		CBSDataCodingScheme dcs = new CBSDataCodingSchemeImpl(15);
		USSDString ussd = new USSDStringImpl(ussdM, dcs, null);
		SipUssdMessage mes = new SipUssdMessage(dcs, ussd);

		assertTrue(mes.isSuccessMessage());
		assertNull(mes.getErrorCode());
		assertEquals(mes.getLanguage(), "en");
		assertEquals(mes.getUssdString(), ussdM);

		CBSDataCodingScheme dcs2 = new CBSDataCodingSchemeImpl(CBSDataCodingGroup.GeneralGsm7, CharacterSet.GSM7,
				CBSNationalLanguage.Italian, DataCodingSchemaMessageClass.Class0, false);
		ussd = new USSDStringImpl(ussdM, dcs2, null);
		mes = new SipUssdMessage(dcs2, ussd);

		assertTrue(mes.isSuccessMessage());
		assertNull(mes.getErrorCode());
		assertEquals(mes.getLanguage(), "it");
		assertEquals(mes.getUssdString(), ussdM);

		CBSDataCodingScheme dcs3 = new CBSDataCodingSchemeImpl(72);
		ussd = new USSDStringImpl(ussdM, dcs3, null);
		mes = new SipUssdMessage(dcs3, ussd);

		assertTrue(mes.isSuccessMessage());
		assertNull(mes.getErrorCode());
		assertNull(mes.getLanguage());
		assertEquals(mes.getUssdString(), ussdM);
	}

	@Test(groups = { "Sip" })
	public void testXMLSerialize() throws Exception {
		SipUssdMessage original = new SipUssdMessage("*123#", "en");

		byte[] rawData = factory.serializeSipUssdMessage(original);

		String serializedEvent = new String(rawData);

		System.out.println(serializedEvent);

		SipUssdMessage copy = factory.deserializeSipUssdMessage(rawData);

		assertEquals(copy.getErrorCodeNumeric(), original.getErrorCodeNumeric());
		assertEquals(copy.getUssdString(), original.getUssdString());
		assertEquals(copy.getLanguage(), original.getLanguage());

		// 2nd Test
		original = new SipUssdMessage("*123#1111#", null);

		// Writes the area to a file.
		rawData = factory.serializeSipUssdMessage(original);
		serializedEvent = new String(rawData);

		System.out.println(serializedEvent);

		copy = factory.deserializeSipUssdMessage(rawData);

		assertEquals(copy.getErrorCodeNumeric(), original.getErrorCodeNumeric());
		assertEquals(copy.getUssdString(), original.getUssdString());
		assertEquals(copy.getLanguage(), original.getLanguage());

		// 3rd Test
		original = new SipUssdMessage("Hello World. Press 1 for Hi and 2 for Hello", "en");

		rawData = factory.serializeSipUssdMessage(original);
		serializedEvent = new String(rawData);

		System.out.println(serializedEvent);

		copy = factory.deserializeSipUssdMessage(rawData);

		assertEquals(copy.getErrorCodeNumeric(), original.getErrorCodeNumeric());
		assertEquals(copy.getUssdString(), original.getUssdString());
		assertEquals(copy.getLanguage(), original.getLanguage());
		assertEquals(copy.getCBSDataCodingScheme(), original.getCBSDataCodingScheme());

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		assertEquals(copy.getCBSDataCodingScheme(), cbsDataCodingScheme);
		assertEquals(copy.getUssdString(), "Hello World. Press 1 for Hi and 2 for Hello");

		// 4th Test
		original = new SipUssdMessage(2);

		// Writes the area to a file.
		rawData = factory.serializeSipUssdMessage(original);
		serializedEvent = new String(rawData);

		System.out.println(serializedEvent);

		copy = factory.deserializeSipUssdMessage(rawData);

		assertEquals(copy.getErrorCodeNumeric(), original.getErrorCodeNumeric());
		assertEquals(copy.getUssdString(), original.getUssdString());
		assertEquals(copy.getLanguage(), original.getLanguage());

		// 5th Test
		original = new SipUssdMessage("Hello World. Press 1 for Hi and 2 for Hello", "en");
		original.setAnyExt(new AnyExt(MAPMessageType.unstructuredSSRequest_Request));

		rawData = factory.serializeSipUssdMessage(original);
		serializedEvent = new String(rawData);

		System.out.println(serializedEvent);

		copy = factory.deserializeSipUssdMessage(rawData);

		assertEquals(copy.getErrorCodeNumeric(), original.getErrorCodeNumeric());
		assertEquals(copy.getUssdString(), original.getUssdString());
		assertEquals(copy.getLanguage(), original.getLanguage());
		assertEquals(copy.getCBSDataCodingScheme(), original.getCBSDataCodingScheme());
		assertEquals(copy.getAnyExt(), original.getAnyExt());
	}

	@Test(groups = { "Sip" })
	public void testOutput() throws Exception {
		MAPProvider mapProvider = new MAPProviderImpl("MAPStack", null);

		SipUssdMessage mes = new SipUssdMessage(ussdM, "en");
		USSDString ussd = mes.getUSSDString(mapProvider);
		CBSDataCodingScheme dcs = mes.getCBSDataCodingScheme();
		MAPErrorMessage err = mes.getMAPErrorMessage(mapProvider);

		assertEquals(ussd.getString(null), ussdM);
		assertEquals(dcs.getCode(), 15);
		assertNull(err);

		mes = new SipUssdMessage(ussdM, "ru");
		ussd = mes.getUSSDString(mapProvider);
		dcs = mes.getCBSDataCodingScheme();
		err = mes.getMAPErrorMessage(mapProvider);

		assertEquals(ussd.getString(null), ussdM);
		assertEquals(dcs.getCode(), 72);
		assertNull(err);

		mes = new SipUssdMessage(ussdM, null);
		ussd = mes.getUSSDString(mapProvider);
		dcs = mes.getCBSDataCodingScheme();
		err = mes.getMAPErrorMessage(mapProvider);

		assertEquals(ussd.getString(null), ussdM);
		assertEquals(dcs.getCode(), 15);
		assertNull(err);

		mes = new SipUssdMessage(SipUssdErrorCode.languageAlphabitNotSupported);
		ussd = mes.getUSSDString(mapProvider);
		dcs = mes.getCBSDataCodingScheme();
		err = mes.getMAPErrorMessage(mapProvider);

		assertNull(ussd);
		assertNull(dcs);
		assertTrue(err.isEmExtensionContainer());
		assertEquals((int) (long) err.getErrorCode(), MAPErrorCode.unknownAlphabet);
	}

}

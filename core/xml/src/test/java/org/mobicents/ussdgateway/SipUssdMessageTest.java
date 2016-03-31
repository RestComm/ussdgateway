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

	
	
    @Test(groups = { "Sip" })
    public void testXMLSerialize2() throws Exception {
        byte[] bt = new byte[] { 0x3c, 0x3f, 0x78, 0x6d, 0x6c, 0x20, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x3d, 0x27,
                0x31, 0x2e, 0x30, 0x27, 0x20, 0x65, 0x6e, 0x63, 0x6f, 0x64, 0x69, 0x6e, 0x67, 0x3d, 0x27, 0x55, 0x54, 0x46,
                0x2d, 0x38, 0x27, 0x3f, 0x3e, 0x0a, 0x3c, 0x75, 0x73, 0x73, 0x64, 0x2d, 0x64, 0x61, 0x74, 0x61, 0x3e, 0x0a,
                0x3c, 0x6c, 0x61, 0x6e, 0x67, 0x75, 0x61, 0x67, 0x65, 0x20, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x3d, 0x22, 0x61,
                0x72, 0x22, 0x2f, 0x3e, 0x0a, 0x3c, 0x75, 0x73, 0x73, 0x64, 0x2d, 0x73, 0x74, 0x72, 0x69, 0x6e, 0x67, 0x20,
                0x76, 0x61, 0x6c, 0x75, 0x65, 0x3d, 0x22, (byte) 0xd8, (byte) 0xa7, (byte) 0xd8, (byte) 0xae, (byte) 0xd8,
                (byte) 0xaa, (byte) 0xd8, (byte) 0xb1, 0x20, (byte) 0xd9, (byte) 0x85, (byte) 0xd9, (byte) 0x86, 0x20,
                (byte) 0xd8, (byte) 0xa7, (byte) 0xd9, (byte) 0x84, (byte) 0xd8, (byte) 0xaa, (byte) 0xd8, (byte) 0xa7,
                (byte) 0xd9, (byte) 0x84, (byte) 0xd9, (byte) 0x8a, 0x26, 0x23, 0x78, 0x61, 0x3b, 0x31, 0x20, (byte) 0xd8,
                (byte) 0xa7, (byte) 0xd9, (byte) 0x84, (byte) 0xd8, (byte) 0xac, (byte) 0xd9, (byte) 0x88, 0x26, 0x23, 0x78,
                0x61, 0x3b, 0x32, 0x20, (byte) 0xd8, (byte) 0xa7, (byte) 0xd9, (byte) 0x84, (byte) 0xd8, (byte) 0xb1,
                (byte) 0xd8, (byte) 0xb5, (byte) 0xd9, (byte) 0x8a, (byte) 0xd8, (byte) 0xaf, 0x22, 0x2f, 0x3e, 0x0a, 0x3c,
                0x61, 0x6e, 0x79, 0x45, 0x78, 0x74, 0x3e, 0x0a, 0x3c, 0x6d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x2d, 0x74,
                0x79, 0x70, 0x65, 0x3e, 0x75, 0x6e, 0x73, 0x74, 0x72, 0x75, 0x63, 0x74, 0x75, 0x72, 0x65, 0x64, 0x53, 0x53,
                0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x5f, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x3c, 0x2f, 0x6d,
                0x65, 0x73, 0x73, 0x61, 0x67, 0x65, 0x2d, 0x74, 0x79, 0x70, 0x65, 0x3e, 0x0a, 0x3c, 0x2f, 0x61, 0x6e, 0x79,
                0x45, 0x78, 0x74, 0x3e, 0x0a, 0x3c, 0x2f, 0x75, 0x73, 0x73, 0x64, 0x2d, 0x64, 0x61, 0x74, 0x61, 0x3e };
//        String templ = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ussd-data><language value=\"en\"/><ussd-string value=\"AAAA&#13;BBB\"/></ussd-data>";
//        String templ = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ussd-data><language value=\"en\"/><ussd-string value=\"AAAA&#10;BBB\"/></ussd-data>";
        String templ = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><ussd-data><language value=\"en\"/><ussd-string value=\"AAAA&#xa;BBB\"/></ussd-data>";

//        SipUssdMessage copy = factory.deserializeSipUssdMessage(bt);
        SipUssdMessage copy = factory.deserializeSipUssdMessage(templ.getBytes());

        char[] ch = copy.getUssdString().toCharArray();
        int[] ii1 = new int[ch.length];
        for (int i = 0; i < ch.length; i++) {
            ii1[i] = ch[i];
        }

        int ggg = 0;
        ggg++;
    }
    
    @Test(groups = { "Sip" })
    public void testXMLSerialize3() throws Exception {
        MAPProvider mapProvider = new MAPProviderImpl("MAPStack", null);

        SipUssdMessage mes = new SipUssdMessage("AAAA\rBBB", "en");
        byte[] rawData = factory.serializeSipUssdMessage(mes);
        String s1 = new String(rawData);
    }
}

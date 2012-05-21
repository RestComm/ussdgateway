package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.AlertingCategory;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponseIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequestIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponseIndication;
import org.mobicents.protocols.ss7.map.primitives.AddressStringImpl;
import org.mobicents.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.mobicents.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.mobicents.protocols.ss7.map.primitives.USSDStringImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSRequestIndicationImpl;
import org.mobicents.protocols.ss7.map.service.supplementary.UnstructuredSSResponseIndicationImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DialogTest {

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

	@Test
	public void testProcessUnstructuredSSRequestSerialization() throws XMLStreamException {

		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		ISDNAddressStringImpl isdnAddress = new ISDNAddressStringImpl(AddressNature.international_number,
				NumberingPlan.ISDN, "79273605819");
		AlertingPatternImpl alertingPattern = new AlertingPatternImpl(AlertingCategory.Category3);
		USSDString ussdStr = new USSDStringImpl("*234#", null);
		ProcessUnstructuredSSRequestIndication processUnstructuredSSRequestIndication = new ProcessUnstructuredSSRequestIndicationImpl(
				(byte) 0x0f, ussdStr, alertingPattern, isdnAddress);

		Dialog original = new Dialog(DialogType.BEGIN, 1234l, destReference, origReference,
				processUnstructuredSSRequestIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getType(), original.getType());
		assertEquals(copy.getId(), original.getId());
		ProcessUnstructuredSSRequestIndication copyUSSR = (ProcessUnstructuredSSRequestIndication) copy.getMAPMessage();
		ProcessUnstructuredSSRequestIndication originalUSSR = (ProcessUnstructuredSSRequestIndication) original
				.getMAPMessage();
		assertEquals(copyUSSR.getUSSDString(), originalUSSR.getUSSDString());

	}

	@Test
	public void testProcessUnstructuredSSResponseSerialization() throws XMLStreamException {

		USSDString ussdStr = new USSDStringImpl("Thank You!", null);
		ProcessUnstructuredSSResponseIndicationImpl processUnstructuredSSResponseIndication = new ProcessUnstructuredSSResponseIndicationImpl(
				(byte) 0x0f, ussdStr);

		Dialog original = new Dialog(DialogType.END, 1234l, processUnstructuredSSResponseIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getType(), original.getType());
		assertEquals(copy.getId(), original.getId());
		ProcessUnstructuredSSResponseIndication copyUSSR = (ProcessUnstructuredSSResponseIndication) copy
				.getMAPMessage();
		ProcessUnstructuredSSResponseIndication originalUSSR = (ProcessUnstructuredSSResponseIndication) original
				.getMAPMessage();
		assertEquals(copyUSSR.getUSSDString(), originalUSSR.getUSSDString());

	}

	@Test
	public void testUnstructuredSSRequestSerialization() throws XMLStreamException {
		USSDString ussdStr = new USSDStringImpl("USSD String : Hello World\n 1. Balance\n 2. Texts Remaining", null);
		UnstructuredSSRequestIndicationImpl unstructuredSSRequestIndication = new UnstructuredSSRequestIndicationImpl(
				(byte) 0x0f, ussdStr, null, null);

		Dialog original = new Dialog(DialogType.CONTINUE, 1234l, null, null, unstructuredSSRequestIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getType(), original.getType());
		assertEquals(copy.getId(), original.getId());

	}

	@Test
	public void testUnstructuredSSResponseSerialization() throws XMLStreamException {

		USSDString ussdStr = new USSDStringImpl("1", null);
		UnstructuredSSResponseIndicationImpl unstructuredSSResponseIndication = new UnstructuredSSResponseIndicationImpl(
				(byte) 0x0f, ussdStr);

		Dialog original = new Dialog(DialogType.CONTINUE, 1234l, unstructuredSSResponseIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getType(), original.getType());
		assertEquals(copy.getId(), original.getId());
		UnstructuredSSResponseIndication copyUSSR = (UnstructuredSSResponseIndication) copy.getMAPMessage();
		UnstructuredSSResponseIndication originalUSSR = (UnstructuredSSResponseIndication) original.getMAPMessage();
		assertEquals(copyUSSR.getUSSDString(), originalUSSR.getUSSDString());

	}

	@Test
	public void testUnstructuredSSNotifyRequestIndicationSerialization() throws XMLStreamException {

		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		USSDString ussdStr = new USSDStringImpl(
				"Your new balance is 34.38 AFN and expires on 30.07.2012. Cost of last event was 0.50 AFN.", null);
		UnstructuredSSNotifyRequestIndicationImpl unstructuredSSNotifyRequestIndication = new UnstructuredSSNotifyRequestIndicationImpl(
				(byte) 0x0f, ussdStr, null, null);

		Dialog original = new Dialog(DialogType.BEGIN, 1234l, destReference, origReference,
				unstructuredSSNotifyRequestIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getType(), original.getType());
		assertEquals(copy.getId(), original.getId());
		UnstructuredSSNotifyRequestIndication copyUSSR = (UnstructuredSSNotifyRequestIndication) copy.getMAPMessage();
		UnstructuredSSNotifyRequestIndication originalUSSR = (UnstructuredSSNotifyRequestIndication) original
				.getMAPMessage();
		assertEquals(copyUSSR.getUSSDString(), originalUSSR.getUSSDString());

	}

	@Test
	public void testDialogAbortSerialization() throws XMLStreamException {
		Dialog original = new Dialog(DialogType.ABORT, 1234l, null, null);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		Dialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);
		
		assertEquals(copy.getType(), original.getType());
	}

}

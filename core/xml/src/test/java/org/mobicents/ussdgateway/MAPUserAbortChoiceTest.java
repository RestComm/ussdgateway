/**
 * 
 */
package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.dialog.ProcedureCancellationReason;
import org.mobicents.protocols.ss7.map.api.dialog.ResourceUnavailableReason;
import org.mobicents.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class MAPUserAbortChoiceTest {

	/**
	 * 
	 */
	public MAPUserAbortChoiceTest() {
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
	public void testProcedureCancellationReasonSerialization() throws XMLStreamException {
		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setProcedureCancellationReason(ProcedureCancellationReason.associatedProcedureFailure);

		String s = XmlMAPDialog.serializeMAPUserAbortChoice(abort);

		System.out.println(s);

		MAPUserAbortChoice duplicate = XmlMAPDialog.deserializeMAPUserAbortChoice(s);

		assertEquals(abort.getProcedureCancellationReason(), duplicate.getProcedureCancellationReason());

	}

	@Test
	public void testResourceUnavailableReason() throws XMLStreamException {
		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setResourceUnavailableReason(ResourceUnavailableReason.shortTermResourceLimitation);

		String s = XmlMAPDialog.serializeMAPUserAbortChoice(abort);

		System.out.println(s);

		MAPUserAbortChoice duplicate = XmlMAPDialog.deserializeMAPUserAbortChoice(s);

		assertEquals(abort.getResourceUnavailableReason(), duplicate.getResourceUnavailableReason());

	}

	@Test
	public void testUserResourceLimitation() throws XMLStreamException {
		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setUserResourceLimitation();

		String s = XmlMAPDialog.serializeMAPUserAbortChoice(abort);

		System.out.println(s);

		MAPUserAbortChoice duplicate = XmlMAPDialog.deserializeMAPUserAbortChoice(s);

		assertEquals(abort.isUserResourceLimitation(), duplicate.isUserResourceLimitation());

	}

	@Test
	public void testUserSpecificReason() throws XMLStreamException {
		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setUserSpecificReason();

		String s = XmlMAPDialog.serializeMAPUserAbortChoice(abort);

		System.out.println(s);

		MAPUserAbortChoice duplicate = XmlMAPDialog.deserializeMAPUserAbortChoice(s);

		assertEquals(abort.isUserSpecificReason(), duplicate.isUserSpecificReason());

	}
}

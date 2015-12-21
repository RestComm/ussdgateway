/**
 * 
 */
package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;

import org.mobicents.protocols.ss7.map.api.MAPMessageType;
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

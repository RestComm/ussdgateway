/**
 * 
 */
package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;

import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class MAPApplicationContextTest {

	/**
	 * 
	 */
	public MAPApplicationContextTest() {
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
		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		String s = XmlMAPDialog.serializeMAPApplicationContext(appCtx);

		System.out.println(s);

		assertEquals(appCtx, XmlMAPDialog.deserializeMAPApplicationContext(s));
	}

}

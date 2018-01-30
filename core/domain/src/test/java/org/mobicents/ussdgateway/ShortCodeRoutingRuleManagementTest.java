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

import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.rules.ScRoutingRuleType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Amit Bhayani
 * 
 */
public class ShortCodeRoutingRuleManagementTest {

	private ShortCodeRoutingRuleManagement scrm;

	/**
	 * 
	 */
	public ShortCodeRoutingRuleManagementTest() {
		// TODO Auto-generated constructor stub
	}

	@BeforeClass
	public void setUpClass() throws Exception {
	}

	@AfterClass
	public void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUp() throws Exception {
		scrm = ShortCodeRoutingRuleManagement.getInstance("scrm_test");
		scrm.start();
		scrm.removeAllResourses();
	}

	@AfterMethod
	public void tearDown() throws Exception {
		scrm.stop();
	}

	@Test
	public void testSerialization() throws Exception {

		ScRoutingRule sr1 = scrm.createScRoutingRule("*123#", "http://localhost:8080/ussddemo/test", true);
		ScRoutingRule sr2 = scrm.createScRoutingRule("*456#", ScRoutingRuleType.SIP.toString(), "127.0.0.1:5060", true,0);

		scrm.stop();

		scrm.start();

		ScRoutingRule sr1Copy = scrm.getScRoutingRule("*123#",0);
		ScRoutingRule sr2Copy = scrm.getScRoutingRule("*456#",0);

		assertEquals(sr1Copy, sr1);
		assertEquals(sr2Copy, sr2);
	}

}

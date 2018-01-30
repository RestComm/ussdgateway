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

import java.util.List;

import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.rules.ScRoutingRuleType;

/**
 * @author amit bhayani
 * 
 */
public interface ShortCodeRoutingRuleManagementMBean {
	List<ScRoutingRule> getScRoutingRuleList();

	/**
	 * Finds the {@link ScRoutingRule} that match's with passed shortCode and networkId
	 * @param shortCode short code for which rule is to be found. 
	 * @param networkId networkId for which rule is to be found.
	 * @return
	 */
	ScRoutingRule getScRoutingRule(String shortCode, int networkId);

	/**
	 * Creates new HTTP based routing rule
	 * 
	 * @param shortCode
	 *            the ussd short code for this rule
	 * @param url
	 *            the HTTP URL where HTTP POST request should be forwarded if
	 *            rule match's
	 * @param exactMatch
	 *            if received dial string should be exactly matched with
	 *            shortCode or should just begin with shortCode
	 * @return the {@link ScRoutingRule} instance
	 * 
	 * @Deprecated use
	 *             {@link ShortCodeRoutingRuleManagementMBean#createScRoutingRule(String, ScRoutingRuleType, String, String, boolean)}
	 *             instead
	 * @throws Exception
	 */
	@Deprecated
	ScRoutingRule createScRoutingRule(String shortCode, String url, boolean exactMatch) throws Exception;

	ScRoutingRule createScRoutingRule(String shortCode, String routingRuleType, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception; // ScRoutingRuleType
	
	ScRoutingRule modifyScRoutingRule(String shortCode, String routingRuleType, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception;	

	ScRoutingRule deleteScRoutingRule(String shortCode, int networkId) throws Exception;
}

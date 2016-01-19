/**
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

	ScRoutingRule createScRoutingRule(String shortCode, ScRoutingRuleType routingRuleType, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception;
	
	ScRoutingRule modifyScRoutingRule(String shortCode, ScRoutingRuleType routingRuleType, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception;	

	ScRoutingRule deleteScRoutingRule(String shortCode, int networkId) throws Exception;
}

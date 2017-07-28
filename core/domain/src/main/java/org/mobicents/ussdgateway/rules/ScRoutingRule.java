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

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.ussdgateway.UssdOAMMessages;

/**
 * Acts as Fact for Rules
 * 
 * @author amit bhayani
 * 
 */
public class ScRoutingRule implements XMLSerializable {
	private static final String RULE_TYPE = "ruleType";
	private static final String SHORT_CODE = "shortcode";
	private static final String NETWORK_ID = "networkid";
	private static final String RULE_URL = "ruleurl";
	private static final String EXACT_MATCH = "exactmatch";
	private static final String SIP_PROXY = "sipProxy";

	// type of media that
	private ScRoutingRuleType ruleType = ScRoutingRuleType.SIP;

	// Initial string, its like #123*
	private String shortCode;
	private int networkId = 0;

	// to be used with other protocols
	private String ruleUrl;
	private String sipProxy;

	private boolean exactMatch = true;

	public ScRoutingRule() {

	}

	public ScRoutingRuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(ScRoutingRuleType ruleType) {
		this.ruleType = ruleType;
	}

	public ScRoutingRule(String ussdString) {
		this.shortCode = ussdString;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public String getRuleUrl() {
		return ruleUrl;
	}

	public void setRuleUrl(String ruleUrl) {
		this.ruleUrl = ruleUrl;
	}

	public String getSipProxy() {
		return sipProxy;
	}

	public void setSipProxy(String sipProxy) {
		this.sipProxy = sipProxy;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		this.show(sb);
		return sb.toString();
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<ScRoutingRule> ESME_XML = new XMLFormat<ScRoutingRule>(ScRoutingRule.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml, ScRoutingRule esme) throws XMLStreamException {
			String ruleTypeStr = xml.getAttribute(RULE_TYPE, null);

			if (ruleTypeStr == null) {
				esme.ruleType = ScRoutingRuleType.HTTP;
			} else {
				esme.ruleType = ScRoutingRuleType.valueOf(ruleTypeStr);
			}
			esme.shortCode = xml.getAttribute(SHORT_CODE, null);
			esme.networkId = xml.getAttribute(NETWORK_ID, 0);
			esme.ruleUrl = xml.getAttribute(RULE_URL, null);
			esme.sipProxy = xml.getAttribute(SIP_PROXY, null);
			esme.exactMatch = xml.getAttribute(EXACT_MATCH, true);
		}

		@Override
		public void write(ScRoutingRule esme, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
			xml.setAttribute(RULE_TYPE, esme.ruleType.name());
			xml.setAttribute(SHORT_CODE, esme.shortCode);
			xml.setAttribute(NETWORK_ID, esme.networkId);
			xml.setAttribute(RULE_URL, esme.ruleUrl);
			xml.setAttribute(SIP_PROXY, esme.sipProxy);
			xml.setAttribute(EXACT_MATCH, esme.exactMatch);
		}
	};

	public void show(StringBuffer sb) {
		sb.append(UssdOAMMessages.SHOW_SC).append(this.shortCode).append(UssdOAMMessages.SHOW_NETWORK_ID).append(this.networkId)
				.append(UssdOAMMessages.SHOW_RULE_TYPE)
				.append(this.ruleType).append(UssdOAMMessages.SHOW_URL).append(this.ruleUrl)
				.append(UssdOAMMessages.SHOW_SIP_PROXY).append(this.sipProxy).append(UssdOAMMessages.SHOW_EXACT_MATCH)
				.append(this.isExactMatch());

		sb.append(UssdOAMMessages.NEW_LINE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + networkId;
		result = prime * result
				+ ((shortCode == null) ? 0 : shortCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScRoutingRule other = (ScRoutingRule) obj;
		if (networkId != other.networkId)
			return false;
		if (shortCode == null) {
			if (other.shortCode != null)
				return false;
		} else if (!shortCode.equals(other.shortCode))
			return false;
		return true;
	}
}

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
package org.mobicents.ussdgateway.rules;

import org.mobicents.ussdgateway.UssdOAMMessages;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

/**
 * Acts as Fact for Rules
 * 
 * @author amit bhayani
 * 
 */
public class ScRoutingRule implements XMLSerializable {
	private static final String SHORT_CODE = "shortcode";
	private static final String RULE_URL = "ruleurl";

	// Initial string, its like #123*
	private String shortCode;

	// to be used with other protocols
	private String ruleUrl;

	public ScRoutingRule(String ussdString) {
		this.shortCode = ussdString;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getRuleUrl() {
		return ruleUrl;
	}

	public void setRuleUrl(String ruleUrl) {
		this.ruleUrl = ruleUrl;
	}

	@Override
	public String toString() {
		return "Call [ussdString=" + shortCode + ", ruleUrl=" + ruleUrl + "]";
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<ScRoutingRule> ESME_XML = new XMLFormat<ScRoutingRule>(ScRoutingRule.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml, ScRoutingRule esme) throws XMLStreamException {
			esme.shortCode = xml.getAttribute(SHORT_CODE, "");
			esme.ruleUrl = xml.getAttribute(RULE_URL, "");
		}

		@Override
		public void write(ScRoutingRule esme, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
			xml.setAttribute(SHORT_CODE, esme.shortCode);
			xml.setAttribute(RULE_URL, esme.ruleUrl);
		}
	};

	public void show(StringBuffer sb) {
		sb.append(UssdOAMMessages.SHOW_SC).append(this.shortCode).append(UssdOAMMessages.SHOW_URL).append(this.ruleUrl);

		sb.append(UssdOAMMessages.NEW_LINE);
	}

}

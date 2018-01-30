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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.rules.ScRoutingRuleType;

/**
 * @author amit bhayani
 * 
 */
public class ShortCodeRoutingRuleManagement implements ShortCodeRoutingRuleManagementMBean {

	private static final Logger logger = Logger.getLogger(ShortCodeRoutingRuleManagement.class);

	private static final String SC_ROUTING_RULE_LIST = "scroutingrulelist";

	private static final String TAB_INDENT = "\t";
	private static final String CLASS_ATTRIBUTE = "type";
	private static final XMLBinding binding = new XMLBinding();
	private static final String PERSIST_FILE_NAME = "scroutingrule.xml";

	private String name;

	private String persistDir = null;

	protected FastList<ScRoutingRule> scRoutingRuleList = new FastList<ScRoutingRule>();

	private final TextBuilder persistFile = TextBuilder.newInstance();

	private static ShortCodeRoutingRuleManagement instance;

	private ShortCodeRoutingRuleManagement(String name) {
		this.name = name;
		binding.setClassAttribute(CLASS_ATTRIBUTE);
		binding.setAlias(ScRoutingRule.class, "scroutingrule");
	}

	protected static ShortCodeRoutingRuleManagement getInstance(String name) {
		if (instance == null) {
			instance = new ShortCodeRoutingRuleManagement(name);
		}
		return instance;
	}

	public static ShortCodeRoutingRuleManagement getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	public String getPersistDir() {
		return persistDir;
	}

	public void setPersistDir(String persistDir) {
		this.persistDir = persistDir;
	}

	@Override
	public List<ScRoutingRule> getScRoutingRuleList() {
		return this.scRoutingRuleList.unmodifiable();
	}

	@Override
	public ScRoutingRule getScRoutingRule(String shortCode, int networkId) {
		for (FastList.Node<ScRoutingRule> n = this.scRoutingRuleList.head(), end = this.scRoutingRuleList.tail(); (n = n
				.getNext()) != end;) {
			ScRoutingRule rule = n.getValue();

			if (rule.isExactMatch()) {
				if (rule.getShortCode().equals(shortCode) && (rule.getNetworkId() == networkId)) {
					return rule;
				}
			} else {
				if (shortCode.startsWith(rule.getShortCode()) && (rule.getNetworkId() == networkId)) {
					return rule;
				}
			}
		}
		return null;
	}

	@Override
	public ScRoutingRule createScRoutingRule(String shortCode, String routingRuleTypeStr, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception {
		if (shortCode == null || shortCode.equals("")) {
			throw new Exception(UssdOAMMessages.INVALID_SC);
		}

		ScRoutingRule rule = this.getScRoutingRule(shortCode, networkId);
		if (rule != null) {
			throw new Exception(UssdOAMMessages.CREATE_SC_RULE_FAIL_ALREADY_EXIST);
		}

		ScRoutingRuleType routingRuleType = ScRoutingRuleType.valueOf(routingRuleTypeStr);
		if (routingRuleType == null) {
			throw new Exception(UssdOAMMessages.NULL_RULE_TYPE);
		}

		if (urlOrsipProxy == null || urlOrsipProxy.equals("")) {
			throw new Exception(UssdOAMMessages.INVALID_ROUTING_RULE_URL);
		}

		if (routingRuleType == ScRoutingRuleType.HTTP) {

			rule = new ScRoutingRule(shortCode);
			rule.setRuleType(routingRuleType);
			rule.setRuleUrl(urlOrsipProxy);
			rule.setExactMatch(exactMatch);
			rule.setNetworkId(networkId);
			this.scRoutingRuleList.add(rule);

			this.store();

			return rule;
		} else {
			// TODO : Parse the sipProxy for ip:port for validity

			rule = new ScRoutingRule(shortCode);
			rule.setSipProxy(urlOrsipProxy);
			rule.setExactMatch(exactMatch);
			rule.setRuleType(routingRuleType);
			rule.setNetworkId(networkId);
			this.scRoutingRuleList.add(rule);

			this.store();

			return rule;
		}
	}
	
	@Override
	public ScRoutingRule modifyScRoutingRule(String shortCode, String routingRuleTypeStr, String urlOrsipProxy,
			boolean exactMatch, int networkId) throws Exception {
		if (shortCode == null || shortCode.equals("")) {
			throw new Exception(UssdOAMMessages.INVALID_SC);
		}

		ScRoutingRule rule = this.getScRoutingRule(shortCode, networkId);
		if (rule == null) {
			throw new Exception(String.format(UssdOAMMessages.DELETE_SC_RULE_FAILED_NO_SC_RULE_FOUND, shortCode, networkId));
		}

        ScRoutingRuleType routingRuleType = ScRoutingRuleType.valueOf(routingRuleTypeStr);
		if (routingRuleType == null) {
			throw new Exception(UssdOAMMessages.NULL_RULE_TYPE);
		}

		if (urlOrsipProxy == null || urlOrsipProxy.equals("")) {
			throw new Exception(UssdOAMMessages.INVALID_ROUTING_RULE_URL);
		}

		if (routingRuleType == ScRoutingRuleType.HTTP) {
			rule.setRuleType(routingRuleType);
			rule.setRuleUrl(urlOrsipProxy);
			rule.setExactMatch(exactMatch);
			this.store();

			return rule;
		} else {
			// TODO : Parse the sipProxy for ip:port for validity
			rule.setSipProxy(urlOrsipProxy);
			rule.setExactMatch(exactMatch);
			rule.setRuleType(routingRuleType);
			this.store();

			return rule;
		}
	}	

	@Override
	@Deprecated
	public ScRoutingRule createScRoutingRule(String shortCode, String url, boolean exactMatch) throws Exception {
		return this.createScRoutingRule(shortCode, ScRoutingRuleType.HTTP.toString(), url, exactMatch, 0);
	}

	@Override
	public ScRoutingRule deleteScRoutingRule(String shortCode, int networkId) throws Exception {
		if (shortCode == null || shortCode.equals("")) {
			throw new Exception(UssdOAMMessages.INVALID_SC);
		}

		ScRoutingRule rule = this.getScRoutingRule(shortCode, networkId);
		if (rule == null) {
			throw new Exception(String.format(UssdOAMMessages.DELETE_SC_RULE_FAILED_NO_SC_RULE_FOUND, shortCode, networkId));
		}

		this.scRoutingRuleList.remove(rule);

		this.store();

		return rule;
	}

	public void start() throws Exception {

		this.persistFile.clear();

		if (persistDir != null) {
			this.persistFile.append(persistDir).append(File.separator).append(this.name).append("_")
					.append(PERSIST_FILE_NAME);
		} else {
			persistFile
					.append(System.getProperty(UssdManagement.USSD_PERSIST_DIR_KEY,
							System.getProperty(UssdManagement.USER_DIR_KEY))).append(File.separator).append(this.name)
					.append("_").append(PERSIST_FILE_NAME);
		}

		logger.info(String.format("Loading short code routig rule configuration from %s", persistFile.toString()));

		try {
			this.load();
		} catch (FileNotFoundException e) {
			logger.warn(String.format("Failed to load the short code routig rule configuration file. \n%s",
					e.getMessage()));
		}

	}

	public void stop() throws Exception {
		this.store();
	}

	public void removeAllResourses() throws Exception {
		this.scRoutingRuleList.clear();
		this.store();
	}

	/**
	 * Persist
	 */
	public void store() {

		// TODO : Should we keep reference to Objects rather than recreating
		// everytime?
		try {
			XMLObjectWriter writer = XMLObjectWriter.newInstance(new FileOutputStream(persistFile.toString()));
			writer.setBinding(binding);
			// Enables cross-references.
			// writer.setReferenceResolver(new XMLReferenceResolver());
			writer.setIndentation(TAB_INDENT);
			writer.write(this.scRoutingRuleList, SC_ROUTING_RULE_LIST, FastList.class);

			writer.close();
		} catch (Exception e) {
			logger.error("Error while persisting the Rule state in file", e);
		}
	}

	/**
	 * Load and create LinkSets and Link from persisted file
	 * 
	 * @throws Exception
	 */
	public void load() throws FileNotFoundException {

		XMLObjectReader reader = null;
		try {
			reader = XMLObjectReader.newInstance(new FileInputStream(persistFile.toString()));

			reader.setBinding(binding);
			this.scRoutingRuleList = reader.read(SC_ROUTING_RULE_LIST, FastList.class);

			reader.close();
		} catch (XMLStreamException ex) {
			// this.logger.info(
			// "Error while re-creating Linksets from persisted file", ex);
		}
	}

}

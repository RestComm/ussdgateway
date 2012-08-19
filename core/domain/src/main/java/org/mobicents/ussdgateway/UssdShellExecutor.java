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

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.mobicents.ss7.management.console.ShellExecutor;
import org.mobicents.ussdgateway.rules.ScRoutingRule;

/**
 * @author amit bhayani
 * 
 */
public class UssdShellExecutor implements ShellExecutor {

	private static final Logger logger = Logger.getLogger(UssdShellExecutor.class);

	private UssdManagement ussdManagement;
	private UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
	private ShortCodeRoutingRuleManagement shortCodeRoutingRuleManagement = ShortCodeRoutingRuleManagement
			.getInstance();

	/**
	 * 
	 */
	public UssdShellExecutor() {
		// TODO Auto-generated constructor stub
	}

	public UssdManagement getUssdManagement() {
		return ussdManagement;
	}

	public void setUssdManagement(UssdManagement ussdManagement) {
		this.ussdManagement = ussdManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.ShellExecutor#execute(java.lang.
	 * String[])
	 */
	@Override
	public String execute(String[] commands) {

		try {
			if (commands.length < 2) {
				return UssdOAMMessages.INVALID_COMMAND;
			}
			String command = commands[1];

			if (command.equals("scrule")) {
				return this.manageScRule(commands);
			} else if (command.equals("set")) {
				return this.manageSet(commands);
			} else if (command.equals("get")) {
				return this.manageGet(commands);
			}
			return UssdOAMMessages.INVALID_COMMAND;
		} catch (Exception e) {
			logger.error(String.format("Error while executing comand %s", Arrays.toString(commands)), e);
			return e.getMessage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.ShellExecutor#handles(java.lang.
	 * String)
	 */
	@Override
	public boolean handles(String command) {
		return "ussd".equals(command);
	}

	private String manageScRule(String[] commands) throws Exception {
		String command = commands[2];
		if (command.equals("create")) {
			return this.createScRule(commands);
		} else if (command.equals("delete")) {
			return this.deleteScRule(commands);
		} else if (command.equals("show")) {
			return this.showScRule();
		}
		return UssdOAMMessages.INVALID_COMMAND;
	}

	/**
	 * Command is ussd scrule create <short-code> <url>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String createScRule(String[] commands) throws Exception {
		if (commands.length < 5) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String shortCode = commands[3];
		String url = commands[4];

		shortCodeRoutingRuleManagement.createScRoutingRule(shortCode, url);

		return String.format(UssdOAMMessages.CREATE_SC_RULE_SUCCESSFULL, shortCode);
	}

	/**
	 * Command is ussd scrule delete <short-code>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String deleteScRule(String[] commands) throws Exception {
		if (commands.length != 4) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String shortCode = commands[3];

		shortCodeRoutingRuleManagement.deleteScRoutingRule(shortCode);

		return String.format(UssdOAMMessages.DELETE_SC_RULE_SUCCESSFUL, shortCode);
	}

	/**
	 * Command is ussd scrule show
	 * 
	 * @return
	 */
	private String showScRule() {
		List<ScRoutingRule> esmes = this.shortCodeRoutingRuleManagement.getScRoutingRuleList();
		if (esmes.size() == 0) {
			return UssdOAMMessages.NO_SC_RULE_DEFINED_YET;
		}
		StringBuffer sb = new StringBuffer();
		for (ScRoutingRule scRule : esmes) {
			sb.append(UssdOAMMessages.NEW_LINE);
			scRule.show(sb);
		}
		return sb.toString();
	}

	private String manageSet(String[] options) throws Exception {
		if (options.length < 4) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String parName = options[2].toLowerCase();
		if (parName.equals(UssdPropertiesManagement.NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE)) {
			ussdPropertiesManagement.setNoRoutingRuleConfiguredMessage(this.formFullMessage(options, 3));
		} else if (parName.equals(UssdPropertiesManagement.DIALOG_TIMEOUT_ERROR_MESSAGE)) {
			ussdPropertiesManagement.setDialogTimeoutErrorMessage(this.formFullMessage(options, 3));
		} else if (parName.equals(UssdPropertiesManagement.SERVER_ERROR_MESSAGE)) {
			ussdPropertiesManagement.setServerErrorMessage(this.formFullMessage(options, 3));
		}

		return UssdOAMMessages.PARAMETER_SUCCESSFULLY_SET;
	}

	private String formFullMessage(String[] options, int fromIndex) {
		StringBuffer sb = new StringBuffer();
		for (int count = fromIndex; count < options.length; count++) {
			sb.append(options[count]);
			if (count != (options.length - 1)) {
				sb.append(UssdOAMMessages.SPACE);
			}
		}
		return sb.toString();
	}

	private String manageGet(String[] options) throws Exception {
		if (options.length == 3) {
			String parName = options[2].toLowerCase();

			StringBuilder sb = new StringBuilder();
			sb.append(options[2]);
			sb.append(" = ");
			if (parName.equals(UssdPropertiesManagement.NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE)) {
				sb.append(ussdPropertiesManagement.getNoRoutingRuleConfiguredMessage());
			} else if (parName.equals(UssdPropertiesManagement.DIALOG_TIMEOUT_ERROR_MESSAGE)) {
				sb.append(ussdPropertiesManagement.getDialogTimeoutErrorMessage());
			} else if (parName.equals(UssdPropertiesManagement.SERVER_ERROR_MESSAGE)) {
				sb.append(ussdPropertiesManagement.getServerErrorMessage());
			} else {
				return UssdOAMMessages.INVALID_COMMAND;
			}

			return sb.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(UssdPropertiesManagement.NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE + " = ");
			sb.append(ussdPropertiesManagement.getNoRoutingRuleConfiguredMessage());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.DIALOG_TIMEOUT_ERROR_MESSAGE + " = ");
			sb.append(ussdPropertiesManagement.getDialogTimeoutErrorMessage());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.SERVER_ERROR_MESSAGE + " = ");
			sb.append(ussdPropertiesManagement.getServerErrorMessage());
			sb.append("\n");

			return sb.toString();
		}
	}
}

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

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.restcomm.ss7.management.console.ShellExecutor;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.rules.ScRoutingRuleType;

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
	 * org.restcomm.ss7.management.console.ShellExecutor#execute(java.lang.
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
	 * org.restcomm.ss7.management.console.ShellExecutor#handles(java.lang.
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
		} else if (command.equals("modify")) {
			return this.modifyScRule(commands);
		} else if (command.equals("delete")) {
			return this.deleteScRule(commands);
		} else if (command.equals("show")) {
			return this.showScRule(commands);
		}
		return UssdOAMMessages.INVALID_COMMAND;
	}

	/**
	 * <p>
	 * Command is ussd scrule create <short-code> <url> <true/false> <HTTP/SIP> <networkId>
	 * </p>
	 * <p>
	 * By default its assumed that routing rule is HTTP. However if you want to
	 * set SIP user below command using SIP. If its HTTP, the second parameter
	 * will be considered as http-url. networkId is not mandatory and by default 
	 * is 0. Exact match is not mandatory and by default is true.
	 * </p>
	 * <p>
	 * ussd scrule create <short-code> <sip-proxy> <true/false> <SIP|HTTP> <networkId>
	 * </p>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String createScRule(String[] commands) throws Exception {
		if (commands.length < 5 || commands.length > 8) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String shortCode = commands[3];
		String url = commands[4];

		String ruleType = ScRoutingRuleType.HTTP.toString();
		boolean exactmatch = true;
		int networkId = 0;

		if (commands.length > 5) {
			String args6 = commands[5];
			if (args6.equals("true") || args6.equals("false")) {
				exactmatch = Boolean.parseBoolean(args6);
			} else if (args6.equals("HTTP") || args6.equals("SIP")) {
				ruleType = args6;
			} else {
				networkId = Integer.parseInt(args6);
			}
		}

		if (commands.length > 6) {
			String args7 = commands[6];
			if (args7.equals("true") || args7.equals("false")) {
				exactmatch = Boolean.parseBoolean(args7);
			} else if (args7.equals("HTTP") || args7.equals("SIP")) {
				ruleType = args7;
			} else {
				networkId = Integer.parseInt(args7);
			}
		}
		
		if (commands.length > 7) {
			String args8 = commands[7];
			if (args8.equals("true") || args8.equals("false")) {
				exactmatch = Boolean.parseBoolean(args8);
			} else if (args8.equals("HTTP") || args8.equals("SIP")) {
				ruleType = args8;
			} else {
				networkId = Integer.parseInt(args8);
			}
		}

		if (ruleType == null) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		shortCodeRoutingRuleManagement.createScRoutingRule(shortCode, ruleType, url, exactmatch, networkId);

		return String.format(UssdOAMMessages.CREATE_SC_RULE_SUCCESSFULL, shortCode, networkId);
	}

	/**
	 * <p>
	 * Command is ussd scrule modify <short-code> <url> <true/false> <HTTP/SIP> <networkId>
	 * </p>
	 * <p>
	 * By default its assumed that routing rule is HTTP. However if you want to
	 * set SIP user below command using SIP. If its HTTP, the second parameter
	 * will be considered as http-url. networkId is not mandatory and by default 
	 * is 0. Exact match is not mandatory and by default is true.
	 * </p>
	 * <p>
	 * ussd scrule modify <short-code> <sip-proxy> <true/false> <SIP|HTTP> <networkId>
	 * </p>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String modifyScRule(String[] commands) throws Exception {
		if (commands.length < 5 || commands.length > 8) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String shortCode = commands[3];
		String url = commands[4];

		String ruleType = ScRoutingRuleType.HTTP.toString();
		boolean exactmatch = true;
		int networkId = 0;

		if (commands.length > 5) {
			String args6 = commands[5];
			if (args6.equals("true") || args6.equals("false")) {
				exactmatch = Boolean.parseBoolean(args6);
			} else if (args6.equals("HTTP") || args6.equals("SIP")) {
				ruleType = args6;
			} else {
				networkId = Integer.parseInt(args6);
			}
		}

		if (commands.length > 6) {
			String args7 = commands[6];
			if (args7.equals("true") || args7.equals("false")) {
				exactmatch = Boolean.parseBoolean(args7);
			} else if (args7.equals("HTTP") || args7.equals("SIP")) {
				ruleType = args7;
			} else {
				networkId = Integer.parseInt(args7);
			}
		}
		
		if (commands.length > 7) {
			String args8 = commands[7];
			if (args8.equals("true") || args8.equals("false")) {
				exactmatch = Boolean.parseBoolean(args8);
			} else if (args8.equals("HTTP") || args8.equals("SIP")) {
				ruleType = args8;
			} else {
				networkId = Integer.parseInt(args8);
			}
		}

		if (ruleType == null) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		shortCodeRoutingRuleManagement.modifyScRoutingRule(shortCode, ruleType, url, exactmatch, networkId);

		return String.format(UssdOAMMessages.MODIFY_SC_RULE_SUCCESSFULL, shortCode, networkId);
	}	
	
	

	/**
	 * Command is ussd scrule delete <short-code> <networkId>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String deleteScRule(String[] commands) throws Exception {
		if (commands.length < 4 || commands.length > 5) {
			return UssdOAMMessages.INVALID_COMMAND;
		}

		String shortCode = commands[3];
		int networkId = 0;
		
		if (commands.length > 4) {
			String args6 = commands[4];
			networkId = Integer.parseInt(args6);
		}
		
		shortCodeRoutingRuleManagement.deleteScRoutingRule(shortCode, networkId);

		return String.format(UssdOAMMessages.DELETE_SC_RULE_SUCCESSFUL, shortCode, networkId);
	}

	/**
	 * Command is ussd scrule show
	 * 
	 * @return
	 */
	private String showScRule(String[] commands) {
        List<ScRoutingRule> rules = this.shortCodeRoutingRuleManagement.getScRoutingRuleList();
        if (rules.size() == 0) {
            return UssdOAMMessages.NO_SC_RULE_DEFINED_YET;
        }

        StringBuffer sb = new StringBuffer();
        if (commands.length >= 4) {
            String shortCode = commands[3];
            int networkId = 0;
            
            if (commands.length > 4) {
                String args6 = commands[4];
                networkId = Integer.parseInt(args6);
            }

            ScRoutingRule scRule = shortCodeRoutingRuleManagement.getScRoutingRule(shortCode, networkId);
            if (scRule == null) {
                return UssdOAMMessages.INVALID_SC;
            } else {
                sb.append(UssdOAMMessages.NEW_LINE);
                scRule.show(sb);
            }
        } else {
            for (ScRoutingRule scRule : rules) {
                sb.append(UssdOAMMessages.NEW_LINE);
                scRule.show(sb);
            }
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
        } else if (parName.equals(UssdPropertiesManagement.SERVER_OVERLOADED_MESSAGE)) {
            ussdPropertiesManagement.setServerOverloadedMessage(this.formFullMessage(options, 3));
		} else if (parName.equals(UssdPropertiesManagement.SERVER_ERROR_MESSAGE)) {
			ussdPropertiesManagement.setServerErrorMessage(this.formFullMessage(options, 3));
		} else if (parName.equals(UssdPropertiesManagement.DIALOG_TIMEOUT)) {
			ussdPropertiesManagement.setDialogTimeout(Long.parseLong(options[3]));
		} else if (parName.equals(UssdPropertiesManagement.USSD_GT)) {
		    String gt = options[3];
            if (options.length >= 6 && options[4].equals("networkid")) {
                int val = Integer.parseInt(options[5]);
                ussdPropertiesManagement.setUssdGt(val, gt);
            } else {
                ussdPropertiesManagement.setUssdGt(gt);
            }
        } else if (parName.equals(UssdPropertiesManagement.MAX_ACTIVITY_COUNT)) {
            ussdPropertiesManagement.setMaxActivityCount(Integer.parseInt(options[3]));
        } else if (parName.equals(UssdPropertiesManagement.USSD_SSN)) {
            ussdPropertiesManagement.setUssdSsn(Integer.parseInt(options[3]));
		} else if (parName.equals(UssdPropertiesManagement.HLR_SSN)) {
			ussdPropertiesManagement.setHlrSsn(Integer.parseInt(options[3]));
		} else if (parName.equals(UssdPropertiesManagement.MSC_SSN)) {
			ussdPropertiesManagement.setMscSsn(Integer.parseInt(options[3]));
		} else if (parName.equals(UssdPropertiesManagement.MAX_MAP_VERSION)) {
			ussdPropertiesManagement.setMaxMapVersion(Integer.parseInt(options[3]));
		} else if (parName.equals(UssdPropertiesManagement.HR_HLR_GT)) {
			if (options[3].equals("null")) {
				System.err.println("setting null");
				ussdPropertiesManagement.setHrHlrGt(null);
			} else {
				System.err.println("its not null");
				ussdPropertiesManagement.setHrHlrGt(options[3]);
			}
		} else if (parName.equals(UssdPropertiesManagement.CDR_LOGGING_TO)) {
            String cdrLoggedType = options[3];
            ussdPropertiesManagement.setCdrLoggingToStr(cdrLoggedType);
        } else if (parName.equals("cdrseparator")) {
            String cdrSeparator = options[3];
            ussdPropertiesManagement.setCdrSeparator(cdrSeparator);

        } else {
			return UssdOAMMessages.INVALID_COMMAND;
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
            } else if (parName.equals(UssdPropertiesManagement.SERVER_OVERLOADED_MESSAGE)) {
                sb.append(ussdPropertiesManagement.getServerOverloadedMessage());
			} else if (parName.equals(UssdPropertiesManagement.SERVER_ERROR_MESSAGE)) {
				sb.append(ussdPropertiesManagement.getServerErrorMessage());
			} else if (parName.equals(UssdPropertiesManagement.DIALOG_TIMEOUT)) {
				sb.append(ussdPropertiesManagement.getDialogTimeout());
			} else if (parName.equals(UssdPropertiesManagement.USSD_GT)) {
			    sb.append("networkId=0 - GT=");
                sb.append(ussdPropertiesManagement.getUssdGt());
                for (Integer key : ussdPropertiesManagement.getNetworkIdVsUssdGwGt().keySet()) {
                    sb.append("\nnetworkId=");
                    sb.append(key);
                    sb.append(" - GT=");
                    sb.append(ussdPropertiesManagement.getNetworkIdVsUssdGwGt().get(key));
                }
            } else if (parName.equals(UssdPropertiesManagement.MAX_ACTIVITY_COUNT)) {
                sb.append(ussdPropertiesManagement.getMaxActivityCount());
			} else if (parName.equals(UssdPropertiesManagement.USSD_SSN)) {
				sb.append(ussdPropertiesManagement.getUssdSsn());
			} else if (parName.equals(UssdPropertiesManagement.HLR_SSN)) {
				sb.append(ussdPropertiesManagement.getHlrSsn());
			} else if (parName.equals(UssdPropertiesManagement.MSC_SSN)) {
				sb.append(ussdPropertiesManagement.getMscSsn());
			} else if (parName.equals(UssdPropertiesManagement.MAX_MAP_VERSION)) {
				sb.append(ussdPropertiesManagement.getMaxMapVersion());
			} else if (parName.equals(UssdPropertiesManagement.HR_HLR_GT)) {
				sb.append(ussdPropertiesManagement.getHrHlrGt());
            } else if (parName.equals(UssdPropertiesManagement.CDR_LOGGING_TO)) {
                sb.append(ussdPropertiesManagement.getCdrLoggingTo());
            } else if (parName.equals("cdrseparator")) {
                sb.append(ussdPropertiesManagement.getCdrSeparator());

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

            sb.append(UssdPropertiesManagement.SERVER_OVERLOADED_MESSAGE + " = ");
            sb.append(ussdPropertiesManagement.getServerOverloadedMessage());
            sb.append("\n");

			sb.append(UssdPropertiesManagement.DIALOG_TIMEOUT + " = ");
			sb.append(ussdPropertiesManagement.getDialogTimeout());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.SERVER_ERROR_MESSAGE + " = ");
			sb.append(ussdPropertiesManagement.getServerErrorMessage());
			sb.append("\n");

			sb.append("networkId=0 - GT=");
            sb.append(ussdPropertiesManagement.getUssdGt());
            for (Integer key : ussdPropertiesManagement.getNetworkIdVsUssdGwGt().keySet()) {
                sb.append("\nnetworkId=");
                sb.append(key);
                sb.append(" - GT=");
                sb.append(ussdPropertiesManagement.getNetworkIdVsUssdGwGt().get(key));
            }
			sb.append("\n");

            sb.append(UssdPropertiesManagement.MAX_ACTIVITY_COUNT + " = ");
            sb.append(ussdPropertiesManagement.getMaxActivityCount());
            sb.append("\n");

            sb.append(UssdPropertiesManagement.USSD_SSN + " = ");
            sb.append(ussdPropertiesManagement.getUssdSsn());
            sb.append("\n");

			sb.append(UssdPropertiesManagement.HLR_SSN + " = ");
			sb.append(ussdPropertiesManagement.getHlrSsn());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.MSC_SSN + " = ");
			sb.append(ussdPropertiesManagement.getMscSsn());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.MAX_MAP_VERSION + " = ");
			sb.append(ussdPropertiesManagement.getMaxMapVersion());
			sb.append("\n");

			sb.append(UssdPropertiesManagement.HR_HLR_GT + " = ");
			sb.append(ussdPropertiesManagement.getHrHlrGt());
			sb.append("\n");

            sb.append(UssdPropertiesManagement.CDR_LOGGING_TO + " = ");
            sb.append(ussdPropertiesManagement.getCdrLoggingTo());
            sb.append("\n");

            sb.append("cdrseparator = ");
            sb.append(ussdPropertiesManagement.getCdrSeparator());
            sb.append("\n");

			return sb.toString();
		}
	}
}

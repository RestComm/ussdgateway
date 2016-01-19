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

/**
 * 
 * @author amit bhayani
 * 
 */
public interface UssdOAMMessages {
	
	/**
	 * Generic constants
	 */
	public static final String TAB = "        ";
	
	public static final String SPACE = " ";
	
	public static final String NEW_LINE = "\n";

	public static final String COMMA = ",";
	
	

	public static final String INVALID_COMMAND = "Invalid Command";

	public static final String CREATE_SC_RULE_FAIL_ALREADY_EXIST = "Creation of short code routing rule failed. Rule already exist";

	public static final String INVALID_SC = "Invalid short code and networkid pair";

	public static final String NULL_RULE_TYPE = "Rule type cannot be null";

	public static final String INVALID_ROUTING_RULE_URL = "Invalid routing rule URL";
	
	public static final String INVALID_SIP_PROXY = "Invalid SIP Proxy";

	public static final String DELETE_SC_RULE_FAILED_NO_SC_RULE_FOUND = "No short code routing rule found for short code=%s network id=%d";

	public static final String CREATE_SC_RULE_SUCCESSFULL = "Successfully created routing rule for short code=%s network id=%d";
	
	public static final String MODIFY_SC_RULE_SUCCESSFULL = "Successfully modified routing rule for short code=%s network id=%d";

	public static final String DELETE_SC_RULE_SUCCESSFUL = "Successfully deleted routing rule for short code=%s network id=%d";
	
	public static final String NO_SC_RULE_DEFINED_YET = "No short code routing rule defined yet";
	
	public static final String PARAMETER_SUCCESSFULLY_SET = "Parameter has been successfully set";
	
	
	public static final String SHOW_RULE_TYPE = " Rule Type=";
	
	public static final String SHOW_NETWORK_ID = " Network Id=";
	
	public static final String SHOW_SIP_PROXY = " To SIP Proxy=";
	
	public static final String SHOW_SC = "Short Code=";
	
	public static final String SHOW_URL = " URL=";
	
	public static final String SHOW_EXACT_MATCH = " Exact Match=";

}

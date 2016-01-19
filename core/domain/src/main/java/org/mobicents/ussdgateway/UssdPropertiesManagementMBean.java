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

import java.util.Map;

import org.mobicents.ussdgateway.UssdPropertiesManagement.CdrLoggedType;

/**
 * @author amit bhayani
 * @author baranowb
 */
public interface UssdPropertiesManagementMBean {

	public String getNoRoutingRuleConfiguredMessage();

	public void setNoRoutingRuleConfiguredMessage(String noRoutingRuleConfiguredMessage);

	public String getServerErrorMessage();

	public void setServerErrorMessage(String serverErrorMessage);

	public String getDialogTimeoutErrorMessage();

	public void setDialogTimeoutErrorMessage(String dialogTimeoutErrorMessage);

	public long getDialogTimeout();

	public void setDialogTimeout(long dialogTimeout);
	
	public String getUssdGt();

    public void setUssdGt(String serviceCenterGt);
    
    public String getUssdGt(int networkId);
    
    public void setUssdGt(int networkId, String serviceCenterGt);
    
    public Map<Integer, String> getNetworkIdVsUssdGwGt();

    public int getUssdSsn();

    public void setUssdSsn(int serviceCenterSsn);

    public int getHlrSsn();
    
    public void setHlrSsn(int ssn);

    public int getMaxMapVersion();

    public void setMaxMapVersion(int maxMapVersion);
    
    public int getMscSsn();

    public void setMscSsn(int mscSsn);

    public CdrLoggedType getCdrLoggingTo();

    public void setCdrLoggingTo(CdrLoggedType cdrLogging);

    public void setHrHlrGt(String hrHlrGt);

    public String getHrHlrGt();
    
}

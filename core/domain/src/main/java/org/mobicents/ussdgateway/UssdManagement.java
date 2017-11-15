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

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.log4j.Logger;

/**
 * @author amit bhayani
 * 
 */
public class UssdManagement implements UssdManagementMBean {
	private static final Logger logger = Logger.getLogger(UssdManagement.class);

	public static final String JMX_DOMAIN = "org.mobicents.ussdgateway";

	protected static final String USSD_PERSIST_DIR_KEY = "ussd.persist.dir";
	protected static final String USER_DIR_KEY = "user.dir";

	private String persistDir = null;
	private final String name;

	private UssdPropertiesManagement ussdPropertiesManagement = null;
	private ShortCodeRoutingRuleManagement shortCodeRoutingRuleManagement = null;

	private MBeanServer mbeanServer = null;

	private static UssdManagement instance = null;
	
	private boolean isStarted = false;
	
	public UssdManagement(String name) {
		this.name = name;
	}

	public static UssdManagement getInstance(String name) {
		if (instance == null) {
			instance = new UssdManagement(name);
		}
		return instance;
	}

	public static UssdManagement getInstance() {
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

	public boolean isStarted() {
		return isStarted;
	}
	
	public void start() throws Exception {
        UssdStatAggregator.getInstance().clearDialogsInProcess();

	    this.ussdPropertiesManagement = UssdPropertiesManagement.getInstance(this.name);
		this.ussdPropertiesManagement.setPersistDir(this.persistDir);
		this.ussdPropertiesManagement.start();

		this.shortCodeRoutingRuleManagement = ShortCodeRoutingRuleManagement.getInstance(this.name);
		this.shortCodeRoutingRuleManagement.setPersistDir(this.persistDir);
		this.shortCodeRoutingRuleManagement.start();

		// Register the MBeans
		boolean servFound = false;
        String agentId = "jboss";
        List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
        if (servers != null && servers.size() > 0) {
            for (MBeanServer server : servers) {
                String defaultDomain = server.getDefaultDomain();

                if (defaultDomain != null && defaultDomain.equals(agentId)) {
                    mbeanServer = server;
                    servFound = true;
                    logger.info(String.format("Found MBeanServer matching for agentId=%s", agentId));
                } else {
                    logger.warn(String.format("Found non-matching MBeanServer with default domian = %s", defaultDomain));
                }
            }
        }

        if (!servFound) {
            this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
        }            
		
		ObjectName ussdPropObjNname = new ObjectName(UssdManagement.JMX_DOMAIN + ":name=UssdPropertiesManagement");
		StandardMBean ussdPropMxBean = new StandardMBean(this.ussdPropertiesManagement,
				UssdPropertiesManagementMBean.class, true);
		this.mbeanServer.registerMBean(ussdPropMxBean, ussdPropObjNname);

		ObjectName ussdScRuleObjNname = new ObjectName(UssdManagement.JMX_DOMAIN
				+ ":name=ShortCodeRoutingRuleManagement");
		StandardMBean ussdScRuleMxBean = new StandardMBean(this.shortCodeRoutingRuleManagement,
				ShortCodeRoutingRuleManagementMBean.class, true);
		this.mbeanServer.registerMBean(ussdScRuleMxBean, ussdScRuleObjNname);

		this.isStarted = true;
		
		logger.info("Started UssdManagement");
	}

	public void stop() throws Exception {
		this.ussdPropertiesManagement.stop();

		if (this.mbeanServer != null) {

			ObjectName ussdPropObjNname = new ObjectName(UssdManagement.JMX_DOMAIN + ":name=UssdPropertiesManagement");
			this.mbeanServer.unregisterMBean(ussdPropObjNname);

			ObjectName ussdScRuleObjNname = new ObjectName(UssdManagement.JMX_DOMAIN
					+ ":name=ShortCodeRoutingRuleManagement");
			this.mbeanServer.unregisterMBean(ussdScRuleObjNname);
		}
	}
}

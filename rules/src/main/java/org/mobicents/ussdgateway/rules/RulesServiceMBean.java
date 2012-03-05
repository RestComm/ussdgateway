package org.mobicents.ussdgateway.rules;

import javax.naming.NamingException;

/**
 * 
 * @author amit bhayani
 *
 */
public interface RulesServiceMBean  extends org.jboss.system.ServiceMBean {
	
	String getJndiName();
    void setJndiName(String jndiName) throws NamingException;
    
    void startService() throws Exception;
    void stopService() throws Exception;

    


}

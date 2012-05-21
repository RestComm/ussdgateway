package org.mobicents.ussdgateway.rules;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.NonSerializableFactory;

/**
 * MBean Service to bind the {@link org.drools.agent.KnowledgeAgent} to JNDI
 * tree such that SBB can do lookup and create a new {@link KnowledgeBase}
 * 
 * @author amit bhayani
 * 
 */
public class RulesService extends ServiceMBeanSupport implements
		RulesServiceMBean {

	private String jndiName = "java:/mobicents/ussdgateway/rulesservice";

	private static final Logger logger = Logger.getLogger(RulesService.class);

	private static final String APP_HOME = System.getProperty("jboss.server.home.url")
			+ "deploy/mobicents-ussd-gateway/";

	private static final String CHANGESET_FILE_PATH = APP_HOME
			+ "config/USSDGatewayChangeSet.xml";

	private static final String PROPERTY = "jboss_server_home_url";
	private static final String SYSTEM_PROPERTY = "jboss.server.home.url";
	
	
	private KnowledgeAgent kagent;

	public RulesService() {

	}

	public void create() {

	}

	public void start() {

	}

	public void stop() {

	}

	public void destroy() {

	}

	public String getJndiName() {
		return this.jndiName;
	}

	public void setJndiName(String jndiName) throws NamingException {
		String oldName = this.jndiName;
		this.jndiName = jndiName;
		if (getState() == STARTED) {
			unbind(oldName);
			try {
				rebind();
			} catch (Exception e) {
				NamingException ne = new NamingException(
						"Failed to update jndiName");
				ne.setRootCause(e);
				throw ne;
			}
		}

	}

	public void startService() throws Exception {

		setupRule();

		rebind();

		this.logger.info("Started Rules Service");
	}

	public void stopService() throws Exception {
		unbind(jndiName);
	}

	private void rebind() throws NamingException {
		InitialContext rootCtx = new InitialContext();
		// Get the parent context into which we are to bind
		Name fullName = rootCtx.getNameParser("").parse(jndiName);
		System.out.println("fullName=" + fullName);
		Name parentName = fullName;
		if (fullName.size() > 1)
			parentName = fullName.getPrefix(fullName.size() - 1);
		else
			parentName = new CompositeName();
		Context parentCtx = createContext(rootCtx, parentName);
		Name atomName = fullName.getSuffix(fullName.size() - 1);
		String atom = atomName.get(0);
		NonSerializableFactory.rebind(parentCtx, atom, kagent);
	}

	private void unbind(String jndiName) {
		try {
			Context rootCtx = (Context) new InitialContext();
			rootCtx.unbind(jndiName);
			NonSerializableFactory.unbind(jndiName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private static Context createContext(Context rootContext, Name name)
			throws NamingException {
		Context subctx = rootContext;
		for (int n = 0; n < name.size(); n++) {
			String atom = name.get(n);
			try {
				Object obj = subctx.lookup(atom);
				subctx = (Context) obj;
			} catch (NamingException e) { // No binding exists, create a
				// subcontext
				subctx = subctx.createSubcontext(atom);
			}
		}

		return subctx;
	}

	private void setupRule() throws Exception {
		//drools dont expand properties.... lol
		//File changeSetFile=new File(CHANGESET_FILE_PATH);
		File changeSetFile=new File(CHANGESET_FILE_PATH.replaceFirst("file:", ""));
		
		FileInputStream fis = new FileInputStream(changeSetFile);
		StringBuffer sb = new StringBuffer();
		//TODO: this is a quick fix, make it work properly
		while(fis.available()>0)
		{
			byte[] data = new byte[fis.available()];
			fis.read(data);
			sb.append(new String(data));
		}
		fis.close();
		
		String fileAsString = sb.toString();
		if(fileAsString.contains(PROPERTY))
		{

			//need to expand pproperty.
			fileAsString = fileAsString.replace(PROPERTY, System.getProperty(SYSTEM_PROPERTY));
			
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(fileAsString.getBytes());
		Resource resource = ResourceFactory.newInputStreamResource(bais);
	
		bais.close();
		

		kagent = KnowledgeAgentFactory.newKnowledgeAgent("UssdGatewayAgent");
		kagent.applyChangeSet(resource);

		ResourceFactory.getResourceChangeNotifierService().start();
		ResourceFactory.getResourceChangeScannerService().start();

	}

}

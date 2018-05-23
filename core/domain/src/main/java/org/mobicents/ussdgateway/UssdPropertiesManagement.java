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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.restcomm.protocols.ss7.map.primitives.ArrayListSerializingBase;

/**
 * @author amit bhayani
 * 
 */
public class UssdPropertiesManagement implements UssdPropertiesManagementMBean {

	private static final Logger logger = Logger.getLogger(UssdPropertiesManagement.class);

    protected static final String NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE = "noroutingruleconfigerrmssg";
    protected static final String SERVER_OVERLOADED_MESSAGE = "serveroverloadedmsg";
	protected static final String SERVER_ERROR_MESSAGE = "servererrmssg";
	protected static final String DIALOG_TIMEOUT_ERROR_MESSAGE = "dialogtimeouterrmssg";

	protected static final String DIALOG_TIMEOUT = "dialogtimeout";
	
	private static final String USSD_GT_LIST = "ussdgtlist";
	protected static final String USSD_GT = "ussdgt";
	protected static final String USSD_SSN = "ussdssn";
	protected static final String HLR_SSN = "hlrssn";
	protected static final String MSC_SSN = "mscssn";
	protected static final String MAX_MAP_VERSION = "maxmapv";
    protected static final String HR_HLR_GT = "hrhlrgt";
    protected static final String CDR_LOGGING_TO = "cdrloggingto";
    protected static final String MAX_ACTIVITY_COUNT = "maxactivitycount";
    protected static final String CDR_SEPARATOR = "cdrSeparator";

	private static final String TAB_INDENT = "\t";
	private static final String CLASS_ATTRIBUTE = "type";
	private static final XMLBinding binding = new XMLBinding();
	private static final String PERSIST_FILE_NAME = "ussdproperties.xml";

	private static UssdPropertiesManagement instance;

	private final String name;

	private String persistDir = null;

	private final TextBuilder persistFile = TextBuilder.newInstance();

    private String noRoutingRuleConfiguredMessage = "Not valid short code. Please dial valid short code.";
    private String serverOverloadedMessage = "Server is overloaded. Please try later";
    private String serverErrorMessage = "Server error, please try again after sometime";
    private String dialogTimeoutErrorMessage = "Request timeout please try again after sometime.";

    private String ussdGwGt = "00000000";
    private FastMap<Integer, String> networkIdVsUssdGwGt = new FastMap<Integer, String>();
    private int ussdGwSsn = 8;
    private int hlrSsn = 6;
    private int mscSsn = 8;
    private int maxMapVersion = 3;
	/**
	 * Dialog time out in milliseconds. Once HTTP request is sent, it expects
	 * back response in dialogTimeout milli seconds.
	 */
	private long dialogTimeout = 25000;

    // if !=null and !=""
    // this address will be inserted as CalledPartyAddress SCCP into all SRI
    // outgoing requests
    private String hrHlrGt = null;

    private CdrLoggedType cdrLoggingTo = CdrLoggedType.Textfile;
    // Separator between fields in a CDR text file
    private String cdrSeparator = ":";

    // max count of TCAP Dialogs that are possible at the same time
    private int maxActivityCount = 5000;

	private UssdPropertiesManagement(String name) {
		this.name = name;
		binding.setClassAttribute(CLASS_ATTRIBUTE);
	}

	protected static UssdPropertiesManagement getInstance(String name) {
		if (instance == null) {
			instance = new UssdPropertiesManagement(name);
		}
		return instance;
	}

	public static UssdPropertiesManagement getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

    @Override
    public String getNoRoutingRuleConfiguredMessage() {
        return this.noRoutingRuleConfiguredMessage;
    }

    @Override
    public void setNoRoutingRuleConfiguredMessage(String noRoutingRuleConfiguredMessage) {
        this.noRoutingRuleConfiguredMessage = noRoutingRuleConfiguredMessage;
        this.store();
    }

    @Override
    public String getServerOverloadedMessage() {
        return this.serverOverloadedMessage;
    }

    @Override
    public void setServerOverloadedMessage(String serverOverloadedMessage) {
        this.serverOverloadedMessage = serverOverloadedMessage;
        this.store();
    }

	@Override
	public String getServerErrorMessage() {
		return this.serverErrorMessage;
	}

	@Override
	public void setServerErrorMessage(String serverErrorMessage) {
		this.serverErrorMessage = serverErrorMessage;
		this.store();
	}

	@Override
	public String getDialogTimeoutErrorMessage() {
		return this.dialogTimeoutErrorMessage;
	}

	@Override
	public void setDialogTimeoutErrorMessage(String dialogTimeoutErrorMessage) {
		this.dialogTimeoutErrorMessage = dialogTimeoutErrorMessage;
		this.store();
	}

	@Override
	public long getDialogTimeout() {
		return dialogTimeout;
	}

	@Override
	public void setDialogTimeout(long dialogTimeout) {
		this.dialogTimeout = dialogTimeout;
		this.store();
	}
	
	public String getPersistDir() {
        return persistDir;
    }

    public void setPersistDir(String persistDir) {
        this.persistDir = persistDir;
    }

    @Override
    public String getUssdGt() {
        return this.ussdGwGt;
    }

    @Override
    public void setUssdGt(String serviceCenterGt) {
        this.setUssdGt(0, serviceCenterGt);
    }
    
	@Override
	public String getUssdGt(int networkId) {
		 String res = this.networkIdVsUssdGwGt.get(networkId);
	        if (res != null)
	            return res;
	        else
	            return this.ussdGwGt;
	}
	
	@Override
    public Map<Integer, String> getNetworkIdVsUssdGwGt() {
        return this.networkIdVsUssdGwGt;
    }

	@Override
	public void setUssdGt(int networkId, String serviceCenterGt) {
		 if (networkId == 0) {
	            this.ussdGwGt = serviceCenterGt;
	        } else {
	            if (serviceCenterGt == null || serviceCenterGt.equals("") || serviceCenterGt.equals("0")) {
	                this.networkIdVsUssdGwGt.remove(networkId);
	            } else {
	                this.networkIdVsUssdGwGt.put(networkId, serviceCenterGt);
	            }
	        }

	        this.store();
	}

    public int getUssdSsn() {
        return ussdGwSsn;
    }

    public void setUssdSsn(int serviceCenterSsn) {
        this.ussdGwSsn = serviceCenterSsn;
        this.store();
    }

    public int getHlrSsn() {
        return hlrSsn;
    }

    public void setHlrSsn(int hlrSsn) {
        this.hlrSsn = hlrSsn;
        this.store();
    }

    public int getMaxMapVersion() {
        return maxMapVersion;
    }

    public void setMaxMapVersion(int maxMapVersion) {
        this.maxMapVersion = maxMapVersion;
        this.store();
    }

    public String getHrHlrGt() {
        return hrHlrGt;
    }

    public void setHrHlrGt(String hrHlrNumber) {
        this.hrHlrGt = hrHlrNumber;
        this.store();
    }

    public int getMscSsn() {
        return mscSsn;
    }

    public void setMscSsn(int mscSsn) {
        this.mscSsn = mscSsn;
        this.store();
    }

    public CdrLoggedType getCdrLoggingTo() {
        return cdrLoggingTo;
    }

    public String getCdrLoggingToStr() {
        if (cdrLoggingTo != null)
            return cdrLoggingTo.toString();
        else
            return null;
    }

    public void setCdrLoggingTo(CdrLoggedType cdrLoggingTo) {
        this.cdrLoggingTo = cdrLoggingTo;
        this.store();
    }

    public void setCdrLoggingToStr(String cdrLoggingTo) {
        this.cdrLoggingTo = CdrLoggedType.valueOf(cdrLoggingTo);
        this.store();
    }

    @Override
    public String getCdrSeparator() {
        return cdrSeparator;
    }

    @Override
    public void setCdrSeparator(String cdrSeparator) {
        if (cdrSeparator != null && cdrSeparator.length() > 0) {
            this.cdrSeparator = cdrSeparator;
            this.store();
        }
    }

    @Override
    public int getMaxActivityCount() {
        return maxActivityCount;
    }

    @Override
    public void setMaxActivityCount(int maxActivityCount) {
        this.maxActivityCount = maxActivityCount;
        this.store();
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

		logger.info(String.format("Loading USSD Properties from %s", persistFile.toString()));

		try {
			this.load();
		} catch (FileNotFoundException e) {
			logger.warn(String.format("Failed to load the USSD configuration file. \n%s", e.getMessage()));
		}

	}

	public void stop() throws Exception {
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
			
            if (networkIdVsUssdGwGt.size() > 0) {
                ArrayList<UssdGwGtNetworkIdElement> al = new ArrayList<UssdGwGtNetworkIdElement>();
                for (Entry<Integer, String> val : networkIdVsUssdGwGt.entrySet()) {
                    UssdGwGtNetworkIdElement el = new UssdGwGtNetworkIdElement();
                    el.networkId = val.getKey();
                    el.ussdGwGt = val.getValue();
                    al.add(el);
                }
                UssdPropertiesManagement_ussdGwGtNetworkId al2 = new UssdPropertiesManagement_ussdGwGtNetworkId(al);
                writer.write(al2, USSD_GT_LIST, UssdPropertiesManagement_ussdGwGtNetworkId.class);
            }

            writer.write(this.noRoutingRuleConfiguredMessage, NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE, String.class);
            writer.write(this.serverOverloadedMessage, SERVER_OVERLOADED_MESSAGE, String.class);
			writer.write(this.serverErrorMessage, SERVER_ERROR_MESSAGE, String.class);
			writer.write(this.dialogTimeoutErrorMessage, DIALOG_TIMEOUT_ERROR_MESSAGE, String.class);
			writer.write(this.dialogTimeout, DIALOG_TIMEOUT, Long.class);
            writer.write(this.hrHlrGt, HR_HLR_GT, String.class);
            writer.write(this.cdrLoggingTo.toString(), CDR_LOGGING_TO, String.class);
            writer.write(this.cdrSeparator, CDR_SEPARATOR, String.class);

            writer.write(this.maxActivityCount, MAX_ACTIVITY_COUNT, Integer.class);

			writer.write(this.ussdGwGt, USSD_GT, String.class);
            writer.write(this.ussdGwSsn, USSD_SSN, Integer.class);
            writer.write(this.hlrSsn, HLR_SSN, Integer.class);
            writer.write(this.mscSsn, MSC_SSN, Integer.class);
            writer.write(this.maxMapVersion, MAX_MAP_VERSION, Integer.class);
			
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
            
            UssdPropertiesManagement_ussdGwGtNetworkId al = reader.read(USSD_GT_LIST, UssdPropertiesManagement_ussdGwGtNetworkId.class);
            networkIdVsUssdGwGt.clear();
            if (al != null) {
                for (UssdGwGtNetworkIdElement elem : al.getData()) {
                    networkIdVsUssdGwGt.put(elem.networkId, elem.ussdGwGt);
                }
            }            

            String s1 = reader.read(NO_ROUTING_RULE_CONFIGURED_ERROR_MESSAGE, String.class);
            if (s1 != null)
                this.noRoutingRuleConfiguredMessage = s1;
            s1 = reader.read(SERVER_OVERLOADED_MESSAGE, String.class);
            if (s1 != null)
                this.serverOverloadedMessage = s1;
            s1 = reader.read(SERVER_ERROR_MESSAGE, String.class);
            if (s1 != null)
                this.serverErrorMessage = s1;
            s1 = reader.read(DIALOG_TIMEOUT_ERROR_MESSAGE, String.class);
            if (s1 != null)
                this.dialogTimeoutErrorMessage = s1;

			this.dialogTimeout = reader.read(DIALOG_TIMEOUT, Long.class);

            String vals = reader.read(HR_HLR_GT, String.class);
            if (vals != null)
                this.hrHlrGt = vals;
            vals = reader.read("hrHlrGt", String.class);
            if (vals != null)
                this.hrHlrGt = vals;
            vals = reader.read("hrhlrnumber", String.class);
            if (vals != null)
                this.hrHlrGt = vals;
            vals = reader.read(CDR_LOGGING_TO, String.class);
            if (vals != null)
                this.cdrLoggingTo = Enum.valueOf(CdrLoggedType.class, vals);
            vals = reader.read("cdrLoggingTo", String.class);
            if (vals != null)
                this.cdrLoggingTo = Enum.valueOf(CdrLoggedType.class, vals);
            vals = reader.read(CDR_SEPARATOR, String.class);
            if (vals != null && vals.length() > 0)
                this.cdrSeparator = vals;

            Integer val = reader.read(MAX_ACTIVITY_COUNT, Integer.class);
            if (val != null)
                this.maxActivityCount = val;

			this.ussdGwGt = reader.read(USSD_GT, String.class);
            this.ussdGwSsn = reader.read(USSD_SSN, Integer.class);
            this.hlrSsn = reader.read(HLR_SSN, Integer.class);
            this.mscSsn = reader.read(MSC_SSN, Integer.class);
            this.maxMapVersion = reader.read(MAX_MAP_VERSION, Integer.class);
			reader.close();
		} catch (XMLStreamException ex) {
			// this.logger.info(
			// "Error while re-creating Linksets from persisted file", ex);
		}
	}

    public enum CdrLoggedType {
        Database, Textfile,
    }
    
    public static class UssdPropertiesManagement_ussdGwGtNetworkId extends ArrayListSerializingBase<UssdGwGtNetworkIdElement> {

        public UssdPropertiesManagement_ussdGwGtNetworkId() {
            super(USSD_GT_LIST, UssdGwGtNetworkIdElement.class);
        }

        public UssdPropertiesManagement_ussdGwGtNetworkId(ArrayList<UssdGwGtNetworkIdElement> data) {
            super(USSD_GT_LIST, UssdGwGtNetworkIdElement.class, data);
        }

    }

}

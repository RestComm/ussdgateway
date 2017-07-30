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

package org.mobicents.ussdgateway.slee.test;

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.HeaderFactoryImpl;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import javax.slee.facilities.TimerID;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.ussdgateway.XmlMAPDialog;
import org.mobicents.ussdgateway.rules.ScRoutingRule;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;
import org.mobicents.ussdgateway.slee.sip.SipClientSbb;

/**
 * 
 * @author sergey vetyutnev
 *
 */
public class SipSbbProxy extends SipClientSbb {
	
	private static final String FROM_USER = "1111";

    protected Request buildInvite(ScRoutingRule call, XmlMAPDialog xmlMAPDialog) throws Exception {
        return super.buildInvite(call, xmlMAPDialog);
    }

    public SipSbbProxy() throws Exception {
//        SipResourceAdaptor ra = new SipResourceAdaptor();
//
//        ResourceAdaptorContext raContext = new ResourceAdaptorContextProxy();
//        ra.setResourceAdaptorContext(raContext);
//
//        ConfigProperties properties = new ConfigProperties();
//        Property property = new ConfigProperties.Property("javax.sip.PORT", "java.lang.Integer", 6080);
//        properties.addProperty(property);
//        Property property2 = new ConfigProperties.Property("javax.sip.IP_ADDRESS", "java.lang.String", "127.0.0.1");
//        properties.addProperty(property2);
//        Property property3 = new ConfigProperties.Property("org.mobicents.ha.javax.sip.BALANCERS", "java.lang.String", "");
//        properties.addProperty(property3);
//        Property property4 = new ConfigProperties.Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String", "");
//        properties.addProperty(property4);
//        Property property5 = new ConfigProperties.Property("javax.sip.TRANSPORT", "java.lang.String", "UDP"); // "transport,transport2"
//        properties.addProperty(property5);
//        Property property6 = new ConfigProperties.Property("org.mobicents.javax.sip.LOOSE_DIALOG_VALIDATION", "java.lang.Boolean", false);
//        properties.addProperty(property6);
//
//        ra.raConfigure(properties);
//        ra.raActive();
//
//        addressFactory = null;

        SipProviderImpl x;
        
        provider = new SipResourceAdaptorProxy();

        this.addressFactory = new AddressFactoryImpl();
        this.headerFactory = new HeaderFactoryImpl();

        this.ipAddress = "127.0.0.1";
        this.port = 6082;
        
        SipURI fromSipUri = addressFactory.createSipURI(FROM_USER, ipAddress);

        Address fromAddress = addressFactory.createAddress(fromSipUri);
//        fromAddress.setDisplayName(FROM_DISPLAY_NAME);
    }


    @Override
    public ChildRelationExt getCDRInterfaceChildRelation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCall(ScRoutingRule call) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ScRoutingRule getCall() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setXmlMAPDialog(XmlMAPDialog dialog) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public XmlMAPDialog getXmlMAPDialog() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCDRState(USSDCDRState dialog) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public USSDCDRState getCDRState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTimerID(TimerID value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TimerID getTimerID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProcessUnstructuredSSRequestInvokeId(long processUnstructuredSSRequestInvokeId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getProcessUnstructuredSSRequestInvokeId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setUserObject(String userObject) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getUserObject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChildRelationExt getCDRPlainInterfaceChildRelation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getFinalMessageSent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setFinalMessageSent(boolean val) {
        // TODO Auto-generated method stub
        
    }

}

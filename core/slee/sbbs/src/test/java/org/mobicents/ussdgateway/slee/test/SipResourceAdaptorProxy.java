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

import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.header.CallID;

import java.util.TooManyListenersException;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionUnavailableException;
import javax.sip.TransportAlreadySupportedException;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import net.java.slee.resource.sip.CancelRequestEvent;
import net.java.slee.resource.sip.DialogActivity;

public class SipResourceAdaptorProxy implements net.java.slee.resource.sip.SleeSipProvider {
	
	int[] listneningpoints = new int[]{5060};
	
	ListeningPoint listeningPoint = null;

    public SipResourceAdaptorProxy() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
    public void addSipListener(SipListener sipListener) throws TooManyListenersException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeSipListener(SipListener sipListener) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SipStack getSipStack() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListeningPoint getListeningPoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListeningPoint[] getListeningPoints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException, TransportAlreadySupportedException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ListeningPoint getListeningPoint(String transport) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeListeningPoint(ListeningPoint listeningPoint) throws ObjectInUseException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public CallIdHeader getNewCallId() {
        String callId = Utils.getInstance().generateCallIdentifier(this.getListeningPoint()
                .getIPAddress());
        CallID callid = new CallID();
        try {
            callid.setCallId(callId);
        } catch (java.text.ParseException ex) {
        }
        return callid;
    }

    @Override
    public ClientTransaction getNewClientTransaction(Request request) throws TransactionUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServerTransaction getNewServerTransaction(Request request) throws TransactionAlreadyExistsException, TransactionUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendRequest(Request request) throws SipException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sendResponse(Response response) throws SipException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Dialog getNewDialog(Transaction transaction) throws SipException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAutomaticDialogSupportEnabled(boolean flag) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AddressFactory getAddressFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HeaderFactory getHeaderFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageFactory getMessageFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DialogActivity getNewDialog(Address from, Address to) throws SipException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DialogActivity getNewDialog(DialogActivity incomingDialog, boolean useSameCallId) throws SipException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLocalSipURI(SipURI uri) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isLocalHostname(String host) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SipURI getLocalSipURI(String transport) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ViaHeader getLocalVia(String transport, String branch) throws TransportNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DialogActivity forwardForkedResponse(ServerTransaction origServerTransaction, Response response) throws SipException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean acceptCancel(CancelRequestEvent cancelEvent, boolean isProxy) {
        // TODO Auto-generated method stub
        return false;
    }

}

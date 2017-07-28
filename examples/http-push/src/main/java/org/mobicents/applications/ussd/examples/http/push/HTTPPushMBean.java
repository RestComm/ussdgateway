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

package org.mobicents.applications.ussd.examples.http.push;

/**
 * Simple MBean interface. This MBean is front end of simple example for ussd
 * push via HTTP.
 * 
 * @author baranowb
 * @author Amit Bhayani
 * 
 */
public interface HTTPPushMBean {

	/**
	 * The URI where HTTP Post request is to be submitted. This should point the
	 * USSD Gateway. Basically http://USSD-IP:8080/mobicents
	 * 
	 * @param uri
	 */
	public void setTargetUri(String uri);

	/**
	 * Get the URI pointing to USSD Gateway for push
	 * 
	 * @return
	 */
	public String getTargetUri();

	/**
	 * Set the MSISDN where USSD Push is to be sent
	 * 
	 * @param isdn
	 */
	public void setIsdn(String isdn);

	/**
	 * Get the MSISDN where USSD request is to be pushed
	 * 
	 * @return
	 */
	public String getIsdn();

	/**
	 * Reset( remove local dialog ) in case something goes wrong
	 */
	public void reset();

	/**
	 * Starts dialog if not already started. Sends Unstructured Request. It can
	 * be sent multiple times in the same dialog
	 * 
	 * @param ussdRequest
	 *            The actual USSD String request
	 * @param emptyDialogHandshake
	 *            If true, USSD Gateway will first establish Dialog by doing
	 *            handshake before sending USSD request. If false the USSD
	 *            request will be added in Dialog begin message
	 * @param invokeTimeout
	 *            Time in milliseconds USSD gateway will wait for user to
	 *            respond, if user doesn't respond back within specified time,
	 *            USSD Gateway will abort the dialog and send back Abort error
	 *            to HTTP App
	 * @param userData
	 * 			  User Data to be sent with every request to USSD Gateway which will be
	 * 			  returned back with response from USSD Gw. This is just in case if 
	 * 			  application wants to keep some data at Dialog level, for example MSISDN
	 * 
	 * 			              
	 * @throws Exception
	 */
	public void sendRequest(String ussdRequest, boolean emptyDialogHandshake, int invokeTimeout, String userData) throws Exception;

	/**
	 * Starts dialog if not already started. Sends Notify Request. It can be
	 * sent multiple times in the same dialog
	 * 
	 * @param ussdRequest
	 *            The actual USSD String request
	 * @param emptyDialogHandshake
	 *            If true, USSD Gateway will first establish Dialog by doing
	 *            handshake before sending USSD request. If false the USSD
	 *            request will be added in Dialog begin message
	 * @param invokeTimeout
	 *            Time in milliseconds USSD gateway will wait for user to
	 *            respond, if user doesn't respond back within specified time,
	 *            USSD Gateway will abort the dialog and send back Abort error
	 *            to HTTP App
	 * @param userData
	 * 			  User Data to be sent with every request to USSD Gateway which will be
	 * 			  returned back with response from USSD Gw. This is just in case if 
	 * 			  application wants to keep some data at Dialog level, for example MSISDN            
	 * @throws Exception
	 */
	public void sendNotify(String ussdRequest, boolean emptyDialogHandshake, int invokeTimeout, String userData) throws Exception;

	/**
	 * USER Abort the underlying MAP Dialog
	 * 
	 * @throws Exception
	 */
	public void abort() throws Exception;

	/**
	 * Close the underlying MAP Dialog. This will send TCAP End to peer
	 * 
	 * @throws Exeption
	 */
	public void close() throws Exception;

	/**
	 * Return current status of service - what has been sent, what has been
	 * received etc.
	 * 
	 * @return
	 */
	public String getStatus();
}

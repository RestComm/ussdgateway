/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2012, Telestax Inc and individual contributors
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

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * @author abhayani
 *
 */
public class UssdGwGtNetworkIdElement {
    private static final String NETWORK_ID = "networkId";
    private static final String USSD_GW__GT = "ussdGwGt";

    public int networkId;
    public String ussdGwGt;

    /**
     * XML Serialization/Deserialization
     */
    protected static final XMLFormat<UssdGwGtNetworkIdElement> SERVICE_CENTER_GT_NETWORKID_XML = new XMLFormat<UssdGwGtNetworkIdElement>(
            UssdGwGtNetworkIdElement.class) {

        @Override
        public void read(javolution.xml.XMLFormat.InputElement xml, UssdGwGtNetworkIdElement elem) throws XMLStreamException {
            elem.networkId = xml.getAttribute(NETWORK_ID, 0);
            elem.ussdGwGt = xml.getAttribute(USSD_GW__GT, "0");
        }

        @Override
        public void write(UssdGwGtNetworkIdElement elem, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
            xml.setAttribute(NETWORK_ID, elem.networkId);
            if (elem.ussdGwGt != null) {
                xml.setAttribute(USSD_GW__GT, elem.ussdGwGt);
            }
        }
    };

}

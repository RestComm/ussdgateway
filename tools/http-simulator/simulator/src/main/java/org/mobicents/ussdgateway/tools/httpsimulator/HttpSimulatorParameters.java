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

package org.mobicents.ussdgateway.tools.httpsimulator;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class HttpSimulatorParameters {

    private int listerningPort = 8049;
    private String callingHost = "localhost";
    private int callingPort = 8080;
    private String url = "/restcomm";

    public int getListeningPort() {
        return listerningPort;
    }

    public void setListeningPort(int listerningPort) {
        this.listerningPort = listerningPort;
    }

    public String getCallingHost() {
        return callingHost;
    }

    public void setCallingHost(String callingHost) {
        this.callingHost = callingHost;
    }

    public int getCallingPort() {
        return callingPort;
    }

    public void setCallingPort(int callingPort) {
        this.callingPort = callingPort;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

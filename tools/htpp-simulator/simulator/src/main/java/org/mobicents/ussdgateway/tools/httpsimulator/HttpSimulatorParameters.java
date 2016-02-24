/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * TeleStax and individual contributors
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

    public int getListerningPort() {
        return listerningPort;
    }

    public void setListerningPort(int listerningPort) {
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

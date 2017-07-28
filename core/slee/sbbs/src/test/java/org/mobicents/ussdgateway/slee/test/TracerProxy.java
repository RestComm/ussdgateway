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

import javax.slee.facilities.FacilityException;
import javax.slee.facilities.TraceLevel;
import javax.slee.facilities.Tracer;

public class TracerProxy implements Tracer {

    @Override
    public void config(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void config(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fine(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fine(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finer(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finer(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finest(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finest(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getParentTracerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TraceLevel getTraceLevel() throws FacilityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTracerName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void info(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void info(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isConfigEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFineEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFinerEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFinestEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInfoEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSevereEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTraceable(TraceLevel arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isWarningEnabled() throws FacilityException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void severe(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void severe(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void trace(TraceLevel arg0, String arg1) throws NullPointerException, IllegalArgumentException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void trace(TraceLevel arg0, String arg1, Throwable arg2) throws NullPointerException, IllegalArgumentException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void warning(String arg0) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void warning(String arg0, Throwable arg1) throws NullPointerException, FacilityException {
        // TODO Auto-generated method stub
        
    }

}

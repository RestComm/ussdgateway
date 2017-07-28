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

import java.util.Timer;

import javax.slee.SLEEException;
import javax.slee.ServiceID;
import javax.slee.facilities.AlarmFacility;
import javax.slee.facilities.EventLookupFacility;
import javax.slee.facilities.ServiceLookupFacility;
import javax.slee.facilities.Tracer;
import javax.slee.profile.ProfileTable;
import javax.slee.profile.UnrecognizedProfileTableNameException;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.ResourceAdaptorID;
import javax.slee.resource.ResourceAdaptorTypeID;
import javax.slee.resource.SleeEndpoint;
import javax.slee.transaction.SleeTransactionManager;
import javax.slee.usage.NoUsageParametersInterfaceDefinedException;
import javax.slee.usage.UnrecognizedUsageParameterSetNameException;

public class ResourceAdaptorContextProxy implements ResourceAdaptorContext {


    @Override
    public Tracer getTracer(String arg0) throws NullPointerException, IllegalArgumentException, SLEEException {
        return new TracerProxy();
    }

    @Override
    public String getEntityName() {
        return "SipRA";
    }


    @Override
    public AlarmFacility getAlarmFacility() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getDefaultUsageParameterSet() throws NoUsageParametersInterfaceDefinedException, SLEEException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventLookupFacility getEventLookupFacility() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceID getInvokingService() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProfileTable getProfileTable(String arg0) throws NullPointerException, UnrecognizedProfileTableNameException, SLEEException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceAdaptorID getResourceAdaptor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceAdaptorTypeID[] getResourceAdaptorTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceLookupFacility getServiceLookupFacility() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SleeEndpoint getSleeEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SleeTransactionManager getSleeTransactionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Timer getTimer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getUsageParameterSet(String arg0) throws NullPointerException, NoUsageParametersInterfaceDefinedException,
            UnrecognizedUsageParameterSetNameException, SLEEException {
        // TODO Auto-generated method stub
        return null;
    }

}

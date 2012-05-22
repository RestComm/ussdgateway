/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.mobicents.ussdgateway.slee.cdr.jdbc.task;

import javax.slee.facilities.Tracer;

import org.mobicents.slee.resource.jdbc.task.simple.SimpleJdbcTask;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * @author baranowb
 * 
 */
public abstract class CDRTaskBase extends SimpleJdbcTask {

    protected final Tracer tracer;
    protected final USSDCDRState state;
    /**CDRCreateTask
     * @param tracer
     */
    public CDRTaskBase(final Tracer tracer, final USSDCDRState state) {
        super();
        this.tracer = tracer;
        this.state = state;
        
        //now decompose to basics
    }

    public abstract void callParentOnFailure(ChargeInterfaceParent parent, String message, Throwable t);

    public abstract void callParentOnSuccess(ChargeInterfaceParent parent);
}

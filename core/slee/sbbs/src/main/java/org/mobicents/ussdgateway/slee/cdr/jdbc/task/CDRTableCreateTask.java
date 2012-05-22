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

import java.sql.Statement;

import javax.slee.facilities.Tracer;

import org.mobicents.slee.resource.jdbc.task.JdbcTaskContext;
import org.mobicents.ussdgateway.slee.cdr.CDRCreateException;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;

/**
 * @author baranowb
 * 
 */
public class CDRTableCreateTask extends CDRTaskBase {

    private final boolean reset;

    /**
     * @param reset
     */
    public CDRTableCreateTask(final Tracer tracer,final boolean reset) {
        super(tracer, null);
        this.reset = reset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnFailure(org.mobicents.ussdgateway.slee.cdr.
     * ChargeInterfaceParent, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void callParentOnFailure(final ChargeInterfaceParent parent, final String message, final Throwable t) {
        if(parent!=null)
            parent.initFailed(message, t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnSuccess(org.mobicents.ussdgateway.slee.cdr.
     * ChargeInterfaceParent)
     */
    @Override
    public void callParentOnSuccess(ChargeInterfaceParent parent) {
        if(parent!=null)
            parent.initSuccessed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.slee.resource.jdbc.task.simple.SimpleJdbcTask#executeSimple(org.mobicents.slee.resource.jdbc.task.
     * JdbcTaskContext)
     */
    @Override
    public Object executeSimple(JdbcTaskContext ctx) {
        try {
            Statement statement = ctx.getConnection().createStatement();
            //TODO: this may be required to run as TX?
            if(reset){
                statement.execute(Schema._QUERY_DROP);
            }
            System.err.println("QUERRY: 000000 "+Schema._QUERY_CREATE);
            statement.execute(Schema._QUERY_CREATE);
        } catch (Exception e) {
            super.tracer.severe("Failed at execute!", e);
            throw new CDRCreateException(e);
        }
        return this;
    }

}

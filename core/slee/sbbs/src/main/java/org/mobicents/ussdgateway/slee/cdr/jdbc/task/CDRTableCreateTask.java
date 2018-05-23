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

package org.mobicents.ussdgateway.slee.cdr.jdbc.task;

import java.sql.Statement;

import javax.slee.facilities.Tracer;

import org.restcomm.slee.resource.jdbc.task.JdbcTaskContext;
import org.mobicents.ussdgateway.slee.cdr.CDRCreateException;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

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
    public void callParentOnFailure(USSDCDRState state, final String message, final Throwable t) {
        this.tracer.severe("Failed to initializee CDR Database! Message: '" + message + "'", t);

//        if(parent!=null)
//            parent.initFailed(message, t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnSuccess(org.mobicents.ussdgateway.slee.cdr.
     * ChargeInterfaceParent)
     */
    @Override
    public void callParentOnSuccess(USSDCDRState state) {
        if (this.tracer.isFineEnabled()) {
            this.tracer.fine("CDR Database has been initialized!");
        }

//        if(parent!=null)
//            parent.initSuccessed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.restcomm.slee.resource.jdbc.task.simple.SimpleJdbcTask#executeSimple(org.restcomm.slee.resource.jdbc.task.
     * JdbcTaskContext)
     */
    @Override
    public Object executeSimple(JdbcTaskContext ctx) {
        try {
            Statement statement = ctx.getConnection().createStatement();
            //TODO: this may be required to run as TX?
            if(reset){
                statement.execute(Schema._QUERY_DROP);
                if(tracer.isFineEnabled()){
                    tracer.fine("Dropping DB: "+Schema._QUERY_DROP);
                }
                if(tracer.isFineEnabled()){
                    tracer.fine("Creating DB: "+Schema._QUERY_CREATE);
                }
                statement.execute(Schema._QUERY_CREATE);
            }else{
                //TODO:how to manage multiple schema updates/schema versioning?
                boolean res;
                res = statement.execute(Schema._QUERY_CHECK_VERSION_0_0_1);

                if(!res){
                    statement.execute(Schema._QUERY_ALTER_0_0_1);
                }
                //FIXME: better to put the updates into a table/list
                res = statement.execute(Schema._QUERY_CHECK_VERSION_0_0_2);

                if(!res){
                    statement.execute(Schema._QUERY_ALTER_0_0_2);
                }
            }
        } catch (Exception e) {
            super.tracer.severe("Failed at execute!", e);
            throw new CDRCreateException(e);
        }
        return this;
    }

}

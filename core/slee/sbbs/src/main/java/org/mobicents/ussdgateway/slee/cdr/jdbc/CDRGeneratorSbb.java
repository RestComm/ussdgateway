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

package org.mobicents.ussdgateway.slee.cdr.jdbc;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ResourceAdaptorTypeID;

import org.mobicents.protocols.ss7.tcap.api.tc.dialog.events.AbortReason;
import org.mobicents.slee.SbbContextExt;
import org.mobicents.slee.resource.jdbc.JdbcActivity;
import org.mobicents.slee.resource.jdbc.JdbcActivityContextInterfaceFactory;
import org.mobicents.slee.resource.jdbc.JdbcResourceAdaptorSbbInterface;
import org.mobicents.slee.resource.jdbc.event.JdbcTaskExecutionThrowableEvent;
import org.mobicents.slee.resource.jdbc.task.simple.SimpleJdbcTaskResultEvent;
import org.mobicents.ussdgateway.slee.cdr.AbortType;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;
import org.mobicents.ussdgateway.slee.cdr.TimeoutType;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRAbortTask;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRCreateTask;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRTableCreateTask;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRTaskBase;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRTerminateTask;
import org.mobicents.ussdgateway.slee.cdr.jdbc.task.CDRTimeoutTask;

/**
 * @author baranowb
 * 
 */
public abstract class CDRGeneratorSbb implements Sbb, ChargeInterface {

    // --------------- JDBC RA essentials ---------------
    private static final ResourceAdaptorTypeID JDBC_RESOURCE_ADAPTOR_ID = JdbcResourceAdaptorSbbInterface.RATYPE_ID;
    private static final String JDBC_RA_LINK = "JDBCRA";
    private JdbcResourceAdaptorSbbInterface jdbcRA;
    private JdbcActivityContextInterfaceFactory jdbcACIF;
    // --------------- SBB essentials ---------------
    private SbbContextExt sbbContextExt;
    private Tracer tracer;

    // --------------- ChargeInterface methods ---------------

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#init(boolean)
     */
    @Override
    public void init(final boolean reset) {
        CDRTableCreateTask task = new CDRTableCreateTask(this.tracer, reset);
        executeTask(task);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createInitRecord()
     */
    @Override
    public void createInitRecord() {
        CDRCreateTask task = new CDRCreateTask(this.tracer, getUSSDCDRState());
        executeTask(task);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createContinueRecord()
     */
    @Override
    public void createContinueRecord() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createTerminateRecord()
     */
    @Override
    public void createTerminateRecord() {
        CDRTerminateTask task = new CDRTerminateTask(this.tracer, getUSSDCDRState());
        executeTask(task);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createAbortRecord(java.lang.String)
     */
    @Override
    public void createAbortRecord(AbortType abortChoice) {
        CDRAbortTask task = new CDRAbortTask(this.tracer, getUSSDCDRState(), abortChoice);
        executeTask(task);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createTimeoutRecord(java.lang.String)
     */
    @Override
    public void createTimeoutRecord(TimeoutType timeoutReason) {
        CDRTimeoutTask task = new CDRTimeoutTask(this.tracer, getUSSDCDRState(), timeoutReason);
        executeTask(task);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#setState(org.mobicents.ussdgateway.slee.cdr.USSDCDRState)
     */
    @Override
    public void setState(USSDCDRState state) {
        this.setUSSDCDRState(state);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#getState()
     */
    @Override
    public USSDCDRState getState() {
        return this.getUSSDCDRState();
    }

    // --------------- JDBC event handlers ---------------
    /**
     * Simple method to create JDBC activity and execute given task.
     * 
     * @param queryJDBCTask
     */
    private void executeTask(CDRTaskBase jdbcTask) {
        JdbcActivity jdbcActivity = jdbcRA.createActivity();
        ActivityContextInterface jdbcACI = jdbcACIF.getActivityContextInterface(jdbcActivity);
        jdbcACI.attach(sbbContextExt.getSbbLocalObject());
        jdbcActivity.execute(jdbcTask);
    }

    // CMPs

    public abstract USSDCDRState getUSSDCDRState();

    public abstract void setUSSDCDRState(USSDCDRState state);

    // events

    /**
     * Event handler for {@link JdbcTaskExecutionThrowableEvent}.
     * 
     * @param event
     * @param aci
     */
    public void onJdbcTaskExecutionThrowableEvent(JdbcTaskExecutionThrowableEvent event, ActivityContextInterface aci) {
        if (tracer.isWarningEnabled()) {
            tracer.warning("Received a JdbcTaskExecutionThrowableEvent, as result of executed task " + event.getTask(),
                    event.getThrowable());
        }

        // end jdbc activity
        final JdbcActivity activity = (JdbcActivity) aci.getActivity();
        activity.endActivity();
        // call back parent
        final ChargeInterfaceParent parent = (ChargeInterfaceParent) sbbContextExt.getSbbLocalObject().getParent();
        final CDRTaskBase jdbcTask = (CDRTaskBase) event.getTask();
        jdbcTask.callParentOnFailure(parent, null, event.getThrowable());

    }

    public void onSimpleJdbcTaskResultEvent(SimpleJdbcTaskResultEvent event, ActivityContextInterface aci) {

        if (tracer.isFineEnabled()) {
            tracer.fine("Received a SimpleJdbcTaskResultEvent, as result of executed task " + event.getTask());
        }

        // end jdbc activity
        final JdbcActivity activity = (JdbcActivity) aci.getActivity();
        activity.endActivity();
        // call back parent
        final ChargeInterfaceParent parent = (ChargeInterfaceParent) sbbContextExt.getSbbLocalObject().getParent();
        final CDRTaskBase jdbcTask = (CDRTaskBase) event.getTask();
        jdbcTask.callParentOnSuccess(parent);

    }

    public void onStartServiceEvent(javax.slee.serviceactivity.ServiceStartedEvent event, ActivityContextInterface aci) {
        try {
            InitialContext ctx = new InitialContext();
            Boolean val = (Boolean) ctx.lookup("java:comp/env/reset");
            this.init(val);
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // --------------- SBB callbacks ---------------

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbActivate()
     */
    @Override
    public void sbbActivate() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbCreate()
     */
    @Override
    public void sbbCreate() throws CreateException {
        this.setUSSDCDRState(new USSDCDRState());

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbExceptionThrown(java.lang.Exception, java.lang.Object, javax.slee.ActivityContextInterface)
     */
    @Override
    public void sbbExceptionThrown(Exception arg0, Object arg1, ActivityContextInterface arg2) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbLoad()
     */
    @Override
    public void sbbLoad() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbPassivate()
     */
    @Override
    public void sbbPassivate() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbPostCreate()
     */
    @Override
    public void sbbPostCreate() throws CreateException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbRemove()
     */
    @Override
    public void sbbRemove() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbRolledBack(javax.slee.RolledBackContext)
     */
    @Override
    public void sbbRolledBack(RolledBackContext arg0) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#sbbStore()
     */
    @Override
    public void sbbStore() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#setSbbContext(javax.slee.SbbContext)
     */
    @Override
    public void setSbbContext(SbbContext ctx) {
        this.sbbContextExt = (SbbContextExt) ctx;
        this.tracer = this.sbbContextExt.getTracer(TRACER_NAME);
        this.jdbcRA = (JdbcResourceAdaptorSbbInterface) this.sbbContextExt.getResourceAdaptorInterface(
                JDBC_RESOURCE_ADAPTOR_ID, JDBC_RA_LINK);
        this.jdbcACIF = (JdbcActivityContextInterfaceFactory) this.sbbContextExt
                .getActivityContextInterfaceFactory(JDBC_RESOURCE_ADAPTOR_ID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#unsetSbbContext()
     */
    @Override
    public void unsetSbbContext() {

        this.sbbContextExt = null;
        this.tracer = null;
        this.jdbcRA = null;
        this.jdbcACIF = null;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.mobicents.slee.SbbExt#sbbConfigurationUpdate(org.mobicents.slee.ConfigProperties)
    // */
    // @Override
    // public void sbbConfigurationUpdate(ConfigProperties arg0) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.mobicents.slee.SbbExt#sbbConfigure(org.mobicents.slee.ConfigProperties)
    // */
    // @Override
    // public void sbbConfigure(ConfigProperties arg0) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.mobicents.slee.SbbExt#sbbUnconfigure()
    // */
    // @Override
    // public void sbbUnconfigure() {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.mobicents.slee.SbbExt#sbbVerifyConfiguration(org.mobicents.slee.ConfigProperties)
    // */
    // @Override
    // public void sbbVerifyConfiguration(ConfigProperties arg0) throws InvalidConfigurationException {
    // // TODO Auto-generated method stub
    //
    // }

}

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

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.slee.facilities.Tracer;

import org.mobicents.protocols.ss7.indicator.AddressIndicator;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.slee.resource.jdbc.task.JdbcTaskContext;
import org.mobicents.ussdgateway.slee.cdr.CDRCreateException;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent.RecordType;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * @author baranowb
 * 
 */
public class CDRTerminateTask extends CDRTaskBase {

    private static final String TERMINATE = "Success";
    /**
     * @param callerID
     * @param callingID
     * @param dialogId
     */
    public CDRTerminateTask(final Tracer tracer, USSDCDRState state) {
        super(tracer,state);

     
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnFailure(org.mobicents.ussdgateway.slee.cdr.
     * ChargeInterfaceParent, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void callParentOnFailure(final ChargeInterfaceParent parent, final String message, final Throwable t) {
        parent.recordGenerationFailed(RecordType.TERMINATE, message, t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnSuccess(org.mobicents.ussdgateway.slee.cdr.
     * ChargeInterfaceParent)
     */
    @Override
    public void callParentOnSuccess(ChargeInterfaceParent parent) {
        parent.recordGenerationSucessed(RecordType.TERMINATE);
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
            PreparedStatement preparedStatement = ctx.getConnection().prepareStatement(Schema._QUERY_INSERT);
            Timestamp tstamp = new Timestamp(System.currentTimeMillis());
            SccpAddress localAddress = super.state.getLocalAddress();
            AddressIndicator addressIndicator = localAddress.getAddressIndicator();
//            _COLUMN_L_SPC+","+
            if(addressIndicator.pcPresent()){
                preparedStatement.setInt(1, localAddress.getSignalingPointCode());
            }else {
                preparedStatement.setNull(1, Types.SMALLINT);
            }
                
//            _COLUMN_L_SSN+","+
            if(addressIndicator.ssnPresent()){
                preparedStatement.setByte(2, (byte) localAddress.getSubsystemNumber());
            }else{
                preparedStatement.setNull(2, Types.TINYINT);
            }
//            _COLUMN_L_RI+","+
            if(addressIndicator.getRoutingIndicator()!=null){
                preparedStatement.setByte(3, (byte) addressIndicator.getRoutingIndicator().getIndicator());
            }else {
                preparedStatement.setNull(3,Types.TINYINT);
            }
                
//            _COLUMN_L_GT_I+","+
            GlobalTitle gt = localAddress.getGlobalTitle();
            if(gt!=null && gt.getIndicator() != null){
                preparedStatement.setByte(4, (byte) gt.getIndicator().getValue());
            }else{
                preparedStatement.setNull(4, Types.TINYINT);
            }
//            _COLUMN_L_GT_DIGITS+","+
            if(gt!=null && gt.getDigits() != null){
                preparedStatement.setString(5, gt.getDigits());
            }else{
                preparedStatement.setNull(5, Types.VARCHAR);
            }
            SccpAddress remoteAddress = super.state.getRemoteAddress();
            addressIndicator = remoteAddress.getAddressIndicator();
//          _COLUMN_R_SPC+","+
          if(addressIndicator.pcPresent()){
              preparedStatement.setInt(6, remoteAddress.getSignalingPointCode());
          }else {
              preparedStatement.setNull(6, Types.SMALLINT);
          }
              
//          _COLUMN_R_SSN+","+
          if(addressIndicator.ssnPresent()){
              preparedStatement.setByte(7, (byte) remoteAddress.getSubsystemNumber());
          }else{
              preparedStatement.setNull(7, Types.TINYINT);
          }
//          _COLUMN_R_RI+","+
          if(addressIndicator.getRoutingIndicator()!=null){
              preparedStatement.setByte(8, (byte) addressIndicator.getRoutingIndicator().getIndicator());
          }else {
              preparedStatement.setNull(8,Types.TINYINT);
          }
              
//          _COLUMN_R_GT_I+","+
          gt = remoteAddress.getGlobalTitle();
          if(gt!=null && gt.getIndicator() != null){
              preparedStatement.setByte(9, (byte) gt.getIndicator().getValue());
          }else{
              preparedStatement.setNull(9, Types.TINYINT);
          }
//          _COLUMN_R_GT_DIGITS+","+
          if(gt!=null && gt.getDigits() != null){
              preparedStatement.setString(10, gt.getDigits());
          }else{
              preparedStatement.setNull(10, Types.VARCHAR);
          }
//            _COLUMN_SERVICE_CODE+","+
          preparedStatement.setString(11, super.state.getServiceCode());

          AddressString addressString = super.state.getOrigReference();
          if(addressString != null){
//            _COLUMN_OR_NATURE+","+
//            _COLUMN_OR_PLAN+","+
//            _COLUMN_OR_DIGITS+","+
              preparedStatement.setByte(12, (byte) addressString.getAddressNature().getIndicator());
              preparedStatement.setByte(13, (byte) addressString.getNumberingPlan().getIndicator());
              preparedStatement.setString(14, addressString.getAddress());
              
          }else{
              preparedStatement.setNull(12, Types.TINYINT);
              preparedStatement.setNull(13, Types.TINYINT);
              preparedStatement.setNull(14, Types.VARCHAR);
              
          }
          addressString = super.state.getDestReference();

          if(addressString != null){
//            _COLUMN_DE_NATURE+","+
//            _COLUMN_DE_PLAN+","+
//            _COLUMN_DE_DIGITS+","+
              preparedStatement.setByte(15, (byte) addressString.getAddressNature().getIndicator());
              preparedStatement.setByte(16, (byte) addressString.getNumberingPlan().getIndicator());
              preparedStatement.setString(17, addressString.getAddress());
              
          }else{
              preparedStatement.setNull(15, Types.TINYINT);
              preparedStatement.setNull(16, Types.TINYINT);
              preparedStatement.setNull(17, Types.VARCHAR);
              
          }
          
          addressString= super.state.getISDNString();
          if(addressString != null){
//            _COLUMN_ISDN_NATURE+","+
//            _COLUMN_ISDN_PLAN+","+
//            _COLUMN_ISDN_DIGITS+","+
              preparedStatement.setByte(18, (byte) addressString.getAddressNature().getIndicator());
              preparedStatement.setByte(19, (byte) addressString.getNumberingPlan().getIndicator());
              preparedStatement.setString(20, addressString.getAddress());
              
          }else{
              preparedStatement.setNull(18, Types.TINYINT);
              preparedStatement.setNull(19, Types.TINYINT);
              preparedStatement.setNull(20, Types.VARCHAR);
              
          }
          
          addressString= super.state.getEriVlrNo();
          if(addressString != null){
//            _COLUMN_VLR_NATURE+","+
//            _COLUMN_VLR_PLAN+","+
//            _COLUMN_VLR_DIGITS+","+
              preparedStatement.setByte(21, (byte) addressString.getAddressNature().getIndicator());
              preparedStatement.setByte(22, (byte) addressString.getNumberingPlan().getIndicator());
              preparedStatement.setString(23, addressString.getAddress());
              
          }else{
              preparedStatement.setNull(21, Types.TINYINT);
              preparedStatement.setNull(22, Types.TINYINT);
              preparedStatement.setNull(23, Types.VARCHAR);
              
          }

          IMSI imsi = super.state.getEriImsi();
          if(imsi != null){
//            _COLUMN_IMSI+","+
              preparedStatement.setString(24, imsi.getData());
          }else{
              preparedStatement.setNull(24, Types.VARCHAR);
          }
              
//            _COLUMN_DIALOG_ID+","+
          preparedStatement.setLong(25, super.state.getDialogId());
//            _COLUMN_TSTAMP+","+
          preparedStatement.setTimestamp(26, tstamp);
//            _COLUMN_TERMINATE_REASON+ <-- 'Success'
          preparedStatement.setString(27,TERMINATE);
          preparedStatement.setString(28,super.state.getId());
            preparedStatement.execute();
        } catch (Exception e) {
            super.tracer.severe("Failed at execute!", e);
            throw new CDRCreateException(e);
        }
        return this;
    }
}

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

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;

import javax.slee.facilities.Tracer;

import org.restcomm.slee.resource.jdbc.task.JdbcTaskContext;
import org.restcomm.slee.resource.jdbc.task.simple.SimpleJdbcTask;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;
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

    public abstract void callParentOnFailure(USSDCDRState state, String message, Throwable t);

    public abstract void callParentOnSuccess(USSDCDRState state);
    
    protected void markRecordCorrupted(JdbcTaskContext ctx){
        try {
            PreparedStatement preparedStatement = ctx.getConnection().prepareStatement(Schema._QUERY_INSERT);
            Timestamp tstamp = new Timestamp(System.currentTimeMillis());

//            _COLUMN_L_SPC+","+
          preparedStatement.setNull(1, Types.SMALLINT);
//            _COLUMN_L_SSN+","+
          preparedStatement.setNull(2, Types.TINYINT);
//            _COLUMN_L_RI+","+
          preparedStatement.setNull(3,Types.TINYINT);              
//            _COLUMN_L_GT_I+","+
          preparedStatement.setNull(4, Types.TINYINT);
           
//            _COLUMN_L_GT_DIGITS+","+
          preparedStatement.setNull(5, Types.VARCHAR);

//          _COLUMN_R_SPC+","+
          preparedStatement.setNull(6, Types.SMALLINT);
             
//          _COLUMN_R_SSN+","+
          preparedStatement.setNull(7, Types.TINYINT);

//          _COLUMN_R_RI+","+
          preparedStatement.setNull(8,Types.TINYINT);  
//          _COLUMN_R_GT_I+","+
          preparedStatement.setNull(9, Types.TINYINT);
//          _COLUMN_R_GT_DIGITS+","+
          preparedStatement.setNull(10, Types.VARCHAR);
//            _COLUMN_SERVICE_CODE+","+
          preparedStatement.setNull(11, Types.VARCHAR);
//            _COLUMN_OR_NATURE+","+
//            _COLUMN_OR_PLAN+","+
//            _COLUMN_OR_DIGITS+","+

          preparedStatement.setNull(12, Types.TINYINT);
          preparedStatement.setNull(13, Types.TINYINT);
          preparedStatement.setNull(14, Types.VARCHAR);
//            _COLUMN_DE_NATURE+","+
//            _COLUMN_DE_PLAN+","+
//            _COLUMN_DE_DIGITS+","+
          preparedStatement.setNull(15, Types.TINYINT);
          preparedStatement.setNull(16, Types.TINYINT);
          preparedStatement.setNull(17, Types.VARCHAR);
//            _COLUMN_ISDN_NATURE+","+
//            _COLUMN_ISDN_PLAN+","+
//            _COLUMN_ISDN_DIGITS+","+
          preparedStatement.setNull(18, Types.TINYINT);
          preparedStatement.setNull(19, Types.TINYINT);
          preparedStatement.setNull(20, Types.VARCHAR);
//            _COLUMN_VLR_NATURE+","+
//            _COLUMN_VLR_PLAN+","+
//            _COLUMN_VLR_DIGITS+","+
          preparedStatement.setNull(21, Types.TINYINT);
          preparedStatement.setNull(22, Types.TINYINT);
          preparedStatement.setNull(23, Types.VARCHAR);
//            _COLUMN_IMSI+","+
          preparedStatement.setNull(24, Types.VARCHAR);    
//            _COLUMN_LOCAL_DIALOG_ID+","+
//          preparedStatement.setLong(25, this.state.getLocalDialogId());
          if (this.state.getLocalDialogId() != null) {
              preparedStatement.setLong(25, this.state.getLocalDialogId());
          } else {
              preparedStatement.setNull(25, Types.BIGINT);
          }
          //REMOTE dialog??
          if (this.state.getRemoteDialogId() != null) {
              preparedStatement.setLong(26, this.state.getRemoteDialogId());
          } else {
              preparedStatement.setNull(26, Types.BIGINT);
          }

          //_COLUMN_DIALOG_DURATION
          preparedStatement.setNull(27, Types.BIGINT);

          //_COLUMN_USSD_STRING
          preparedStatement.setNull(28, Types.VARCHAR);

//            _COLUMN_TSTAMP+","+
          preparedStatement.setTimestamp(29, tstamp);
//        _COLUMN_STATUS+ <-- null - create record
          preparedStatement.setString(30,RecordStatus.FAILED_CORRUPTED_MESSAGE.toString());
//        _COLUMN_ID
          preparedStatement.setString(31,this.state.getId());
          preparedStatement.execute();
        } catch (Exception e) {
            this.tracer.severe("Failed at execute!", e);
            //throw new CDRCreateException(e);
        }
    }
}

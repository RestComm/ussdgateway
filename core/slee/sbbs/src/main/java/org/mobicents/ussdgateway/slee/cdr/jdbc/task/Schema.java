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

/**
 * @author baranowb
 * 
 */
final class Schema {

    /**
     * 
     */
    private Schema() {
        super();
        // TODO Auto-generated constructor stub
    }

    //ID | L_SPC | L_SSN | L_RI | L_GT_I | L_GT_DIGITS
    //   | R_SPC | R_SSN | R_RI | R_GT_I | R_GT_DIGITS
    //   | SERVICE_CODE 
    //   | OR_NATURE | OR_PLAN | OR_DIGITS 
    //   | DE_NATURE | DE_PLAN | DE_DIGITS 
    //   | ISDN_NATURE | ISDN_PLAN | ISDN_DIGITS 
    //   | VLR_NATURE | VLR_PLAN | VLR_DIGITS 
    //   | IMSI | LOCAL_DIALOG_ID | REMOTE_DIALOG_ID | DIALOG_DURATION
    //   | USSD_STRING
    //   | TSTAMP | STATUS
    // lets define some statics for table and queries in one place.
    
    public static final String _TABLE_NAME = "USSD_GW_CDRS";
    public static final String _COLUMN_ID = "ID";
    public static final String _COLUMN_L_SPC = "L_SPC";
    public static final String _COLUMN_L_SSN = "L_SSN";
    public static final String _COLUMN_L_RI = "L_RI";
    public static final String _COLUMN_L_GT_I = "L_GT_I";
    public static final String _COLUMN_L_GT_DIGITS = "L_GT_DIGITS";
    public static final String _COLUMN_R_SPC = "R_SPC";
    public static final String _COLUMN_R_SSN = "R_SSN";
    public static final String _COLUMN_R_RI = "R_RI";
    public static final String _COLUMN_R_GT_I = "R_GT_I";
    public static final String _COLUMN_R_GT_DIGITS = "R_GT_DIGITS";
    public static final String _COLUMN_SERVICE_CODE = "SERVICE_CODE";
    public static final String _COLUMN_OR_NATURE = "OR_NATURE";
    public static final String _COLUMN_OR_PLAN = "OR_PLAN";
    public static final String _COLUMN_OR_DIGITS = "OR_DIGITS";
    public static final String _COLUMN_DE_NATURE = "DE_NATURE";
    public static final String _COLUMN_DE_PLAN = "DE_PLAN";
    public static final String _COLUMN_DE_DIGITS = "DE_DIGITS";
    public static final String _COLUMN_ISDN_NATURE = "ISDN_NATURE";
    public static final String _COLUMN_ISDN_PLAN = "ISDN_PLAN";
    public static final String _COLUMN_ISDN_DIGITS = "ISDN_DIGITS";
    public static final String _COLUMN_VLR_NATURE = "VLR_NATURE";
    public static final String _COLUMN_VLR_PLAN = "VLR_PLAN";
    public static final String _COLUMN_VLR_DIGITS = "VLR_DIGITS";
    public static final String _COLUMN_IMSI = "IMSI";
    public static final String _COLUMN_LOCAL_DIALOG_ID = "LOCAL_DIALOG_ID";
    public static final String _COLUMN_REMOTE_DIALOG_ID = "REMOTE_DIALOG_ID";
    public static final String _COLUMN_DIALOG_DURATION = "DIALOG_DURATION";
    public static final String _COLUMN_USSD_STRING = "USSD_STRING";
    public static final String _COLUMN_TSTAMP = "TSTAMP";
    public static final String _COLUMN_STATUS = "STATUS";
    public static final String _COLUMN_TYPE = "TYPE";


    

    //TODO: baranowb: we need to check types if its enough for us.
    public static final String _TYPE_COLUMN_ID = "VARCHAR(150)";
    public static final String _TYPE_COLUMN_L_SPC = "INT";
    public static final String _TYPE_COLUMN_L_SSN = "SMALLINT";
    public static final String _TYPE_COLUMN_L_RI = "SMALLINT";
    public static final String _TYPE_COLUMN_L_GT_I = "SMALLINT";
    public static final String _TYPE_COLUMN_L_GT_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_R_SPC = "INT";
    public static final String _TYPE_COLUMN_R_SSN = "SMALLINT";
    public static final String _TYPE_COLUMN_R_RI = "SMALLINT";
    public static final String _TYPE_COLUMN_R_GT_I = "SMALLINT";
    public static final String _TYPE_COLUMN_R_GT_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_SERVICE_CODE = "VARCHAR(50)";
    public static final String _TYPE_COLUMN_OR_NATURE = "SMALLINT";
    public static final String _TYPE_COLUMN_OR_PLAN = "SMALLINT";
    public static final String _TYPE_COLUMN_OR_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_DE_NATURE = "SMALLINT";
    public static final String _TYPE_COLUMN_DE_PLAN = "SMALLINT";
    public static final String _TYPE_COLUMN_DE_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_ISDN_NATURE = "SMALLINT";
    public static final String _TYPE_COLUMN_ISDN_PLAN = "SMALLINT";
    public static final String _TYPE_COLUMN_ISDN_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_VLR_NATURE = "SMALLINT";
    public static final String _TYPE_COLUMN_VLR_PLAN = "SMALLINT";
    public static final String _TYPE_COLUMN_VLR_DIGITS = "VARCHAR(18)";
    public static final String _TYPE_COLUMN_IMSI = "VARCHAR(100)";
    //THIS should be ENUM ?
    public static final String _TYPE_COLUMN_STATUS = "VARCHAR(30)";
    public static final String _TYPE_COLUMN_TYPE = "VARCHAR(30)";
    public static final String _TYPE_COLUMN_TSTAMP = "TIMESTAMP";
    public static final String _TYPE_COLUMN_LOCAL_DIALOG_ID = "BIGINT";
    public static final String _TYPE_COLUMN_REMOTE_DIALOG_ID = "BIGINT";
    public static final String _TYPE_COLUMN_DIALOG_DURATION = "BIGINT";
    //FIXME: what size should the column be?
    public static final String _TYPE_COLUMN_USSD_STRING = "VARCHAR(255)";


    // SQL queries.
    // drop table
    public static final String _QUERY_DROP = "DROP TABLE IF EXISTS " + _TABLE_NAME + ";";
    
    //NOTE: should TSTAMP be also managed by DB ?
    // create table, declare DB managed ID, since caller_id+callee_id+dialog_id may repeate itself.
    public static final String _QUERY_CREATE = "CREATE TABLE  " + _TABLE_NAME
            + " (" + _COLUMN_ID     + " "+_TYPE_COLUMN_ID+" NOT NULL, "
            + _COLUMN_L_SPC         + " "+_TYPE_COLUMN_L_SPC+", "
            + _COLUMN_L_SSN         + " "+_TYPE_COLUMN_L_SSN+", "
            + _COLUMN_L_RI          + " "+_TYPE_COLUMN_L_RI+", "
            + _COLUMN_L_GT_I        + " "+_TYPE_COLUMN_L_GT_I+", "
            + _COLUMN_L_GT_DIGITS   + " "+_TYPE_COLUMN_L_GT_DIGITS+", "
            + _COLUMN_R_SPC         + " "+_TYPE_COLUMN_R_SPC+", "
            + _COLUMN_R_SSN         + " "+_TYPE_COLUMN_R_SSN+", "
            + _COLUMN_R_RI          + " "+_TYPE_COLUMN_R_RI+", "
            + _COLUMN_R_GT_I        + " "+_TYPE_COLUMN_R_GT_I+", "
            + _COLUMN_R_GT_DIGITS   + " "+_TYPE_COLUMN_R_GT_DIGITS+", "
            + _COLUMN_SERVICE_CODE  + " "+_TYPE_COLUMN_SERVICE_CODE+", "
            + _COLUMN_OR_NATURE     + " "+_TYPE_COLUMN_OR_NATURE+", "
            + _COLUMN_OR_PLAN       + " "+_TYPE_COLUMN_OR_PLAN+", "
            + _COLUMN_OR_DIGITS     + " "+_TYPE_COLUMN_OR_DIGITS+", "
            + _COLUMN_DE_NATURE + " "+_TYPE_COLUMN_DE_NATURE+", "
            + _COLUMN_DE_PLAN + " "+_TYPE_COLUMN_DE_PLAN+", "
            + _COLUMN_DE_DIGITS + " "+_TYPE_COLUMN_DE_DIGITS+", "
            + _COLUMN_ISDN_NATURE + " "+_TYPE_COLUMN_ISDN_NATURE+", "
            + _COLUMN_ISDN_PLAN + " "+_TYPE_COLUMN_ISDN_PLAN+", "
            + _COLUMN_ISDN_DIGITS + " "+_TYPE_COLUMN_ISDN_DIGITS+", "
            + _COLUMN_VLR_NATURE + " "+_TYPE_COLUMN_VLR_NATURE+", "
            + _COLUMN_VLR_PLAN + " "+_TYPE_COLUMN_VLR_PLAN+", "
            + _COLUMN_VLR_DIGITS + " "+_TYPE_COLUMN_VLR_DIGITS+", "
            + _COLUMN_IMSI + " "+_TYPE_COLUMN_IMSI+", "
            + _COLUMN_STATUS + " "+_TYPE_COLUMN_STATUS+"  NOT NULL , "
            + _COLUMN_TYPE + " "+_TYPE_COLUMN_TYPE+"  NOT NULL , "
            + _COLUMN_TSTAMP + " "+_TYPE_COLUMN_TSTAMP+"  NOT NULL , "
            + _COLUMN_LOCAL_DIALOG_ID + " "+_TYPE_COLUMN_LOCAL_DIALOG_ID +", " 
            + _COLUMN_REMOTE_DIALOG_ID + " "+_TYPE_COLUMN_REMOTE_DIALOG_ID +", "
            + _COLUMN_DIALOG_DURATION + " "+_TYPE_COLUMN_DIALOG_DURATION +","
            + _COLUMN_USSD_STRING + " "+_TYPE_COLUMN_USSD_STRING
             + ", PRIMARY KEY(" + _COLUMN_ID + ","+_COLUMN_TSTAMP+")" + ");";

    public static final String _QUERY_CHECK_VERSION_0_0_1 = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS "
            + " WHERE TABLE_NAME = "+_TABLE_NAME
            + " AND COLUMN_NAME = "+_COLUMN_DIALOG_DURATION;
    public static final String _QUERY_CHECK_VERSION_0_0_2 = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS "
            + " WHERE TABLE_NAME = "+_TABLE_NAME
            + " AND COLUMN_NAME = "+_COLUMN_USSD_STRING;

    public static final String _QUERY_ALTER_0_0_1 = "ALTER TABLE "+_TABLE_NAME
            + " ADD COLUMN "+_COLUMN_DIALOG_DURATION + " " + _TYPE_COLUMN_DIALOG_DURATION;
    public static final String _QUERY_ALTER_0_0_2 = "ALTER TABLE "+_TABLE_NAME
            + " ADD COLUMN "+_COLUMN_USSD_STRING + " " + _TYPE_COLUMN_USSD_STRING;

    public static final String _QUERY_INSERT ="INSERT INTO "+_TABLE_NAME
            +" ( " +
                _COLUMN_L_SPC+","+
                _COLUMN_L_SSN+","+
                _COLUMN_L_RI+","+
                _COLUMN_L_GT_I+","+
                _COLUMN_L_GT_DIGITS+","+
                _COLUMN_R_SPC+","+
                _COLUMN_R_SSN+","+
                _COLUMN_R_RI+","+
                _COLUMN_R_GT_I+","+
                _COLUMN_R_GT_DIGITS+","+
                _COLUMN_SERVICE_CODE+","+
                _COLUMN_OR_NATURE+","+
                _COLUMN_OR_PLAN+","+
                _COLUMN_OR_DIGITS+","+
                _COLUMN_DE_NATURE+","+
                _COLUMN_DE_PLAN+","+
                _COLUMN_DE_DIGITS+","+
                _COLUMN_ISDN_NATURE+","+
                _COLUMN_ISDN_PLAN+","+
                _COLUMN_ISDN_DIGITS+","+
                _COLUMN_VLR_NATURE+","+
                _COLUMN_VLR_PLAN+","+
                _COLUMN_VLR_DIGITS+","+
                _COLUMN_IMSI+","+
                _COLUMN_LOCAL_DIALOG_ID+","+
                _COLUMN_REMOTE_DIALOG_ID+","+
                _COLUMN_DIALOG_DURATION+","+
                _COLUMN_USSD_STRING+","+
                _COLUMN_TSTAMP+","+
                _COLUMN_STATUS+","+
                _COLUMN_TYPE+","+
                _COLUMN_ID+
             ") " +
                //ehhhh....
             "VALUES (?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?," +
                     "?,?,?,?);";

    public static void main(String[] args){
        System.out.println(_QUERY_CREATE);
    }
}

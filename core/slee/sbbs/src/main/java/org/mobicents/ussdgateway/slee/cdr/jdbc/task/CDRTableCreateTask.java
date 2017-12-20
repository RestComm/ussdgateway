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

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import javax.slee.facilities.Tracer;

import org.mobicents.slee.resource.jdbc.task.JdbcTaskContext;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.slee.cdr.CDRCreateException;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;

/**
 * @author baranowb
 * 
 */
public class CDRTableCreateTask extends CDRTaskBase {

    protected static final UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();

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
                if(tracer.isFineEnabled()){
                    tracer.fine("Dropping DB: "+Schema._QUERY_DROP);
                }
                if(tracer.isFineEnabled()){
                    tracer.fine("Creating DB: "+Schema._QUERY_CREATE);
                }
                statement.execute(Schema._QUERY_CREATE);
            }else{
                int src = 0;
                int dest = 0;
                checkUpgradePath(statement, src, dest);
                String backupFilename = "preupgrade_" + Schema.upgrades[src] + "_to_" + Schema.upgrades[dest] + ".bak";
                String backupDir = ussdPropertiesManagement.getDbBackupDir();
                String dbLogin = ussdPropertiesManagement.getDbLogin();
                String dbPassword = ussdPropertiesManagement.getDbPassword();
                String dbName = ussdPropertiesManagement.getDbSchemaName();
                // check flag
                if(ussdPropertiesManagement.isDbBackup()) {
                    backupDatabase(backupDir + "/" + backupFilename, dbLogin, dbPassword, dbName);
                }
                upgradeDatabase(statement, src, dest);
            }
        } catch (Exception e) {
            super.tracer.severe("Failed at execute!", e);
            throw new CDRCreateException(e);
        }
        return this;
    }

    private void upgradeDatabase(Statement statement, int src, int dest)
            throws SQLException {
        for (int i = src; i < dest; ++i) {
            // FIXME: do savepoint
            // FIXME: actually check and handle failure
            statement.execute(Schema.upgradeModifications[i]);
        }
    }

    private void checkUpgradePath(Statement statement, int src, int dest)
            throws SQLException {
        for (int i = 0; i < Schema.upgradeChecks.length; ++i) {
            // if check failed, means that the current db version is older than
            // the current modification
            // if check was successful, the db is already at the current loop
            // iteration version
            if (!statement.execute(Schema.upgradeChecks[i])) {
                src = i;
                break;
            }
        }
        // TODO: will we ever want to do partial upgrade?
        dest = Schema.upgradeChecks.length;
    }

    // TODO: ideally we should split up the backup to schema and data
    // TODO: currently this is mysql specific, we should create spi and have
    // various implementations, mysql, hsqldb, etc
    private void backupDatabase(String backupPath, String dbLogin,
            String dbPassword, String dbName) throws IOException {
        Runtime.getRuntime().exec(
                "mysqldump -u " + dbLogin + " -p" + dbPassword + " " + dbName
                        + " > " + backupPath);
    }
}

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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import javax.slee.facilities.Tracer;

import org.restcomm.protocols.ss7.indicator.AddressIndicator;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.slee.resource.jdbc.task.JdbcTaskContext;
import org.mobicents.ussdgateway.slee.cdr.CDRCreateException;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * @author baranowb
 * 
 */
public class CDRCreateTask extends CDRTaskBase {

	/**
	 * @param callerID
	 * @param callingID
	 * @param localDialogId
	 */
	public CDRCreateTask(final Tracer tracer, final USSDCDRState state) {
		super(tracer, state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnFailure
	 * (org.mobicents.ussdgateway.slee.cdr. ChargeInterfaceParent,
	 * java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void callParentOnFailure(USSDCDRState state, final String message, final Throwable t) {
        this.tracer.severe("Failed to generate CDR! Message: '" + message + "'");
        this.tracer.severe("Status: " + state);

//		if (parent != null)
//			parent.recordGenerationFailed(message, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ussdgateway.slee.cdr.jdbc.CDRBaseTask#callParentOnSuccess
	 * (org.mobicents.ussdgateway.slee.cdr. ChargeInterfaceParent)
	 */
	@Override
	public void callParentOnSuccess(USSDCDRState state) {
        if (this.tracer.isFineEnabled()) {
            this.tracer.fine("Generated CDR for Status: " + state);
        }

//        getCDRChargeInterface().getState()

//	    if (parent != null)
//			parent.recordGenerationSucessed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.restcomm.slee.resource.jdbc.task.simple.SimpleJdbcTask#executeSimple
	 * (org.restcomm.slee.resource.jdbc.task. JdbcTaskContext)
	 */
	@Override
	public Object executeSimple(JdbcTaskContext ctx) {
		try {
			PreparedStatement preparedStatement = ctx.getConnection().prepareStatement(Schema._QUERY_INSERT);
			Timestamp tstamp = new Timestamp(System.currentTimeMillis());
			SccpAddress localAddress = super.state.getLocalAddress();
			AddressIndicator addressIndicator = null;
			GlobalTitle gt = null;

			if (localAddress == null) {
				preparedStatement.setNull(1, Types.SMALLINT);
				preparedStatement.setNull(2, Types.TINYINT);
				preparedStatement.setNull(3, Types.TINYINT);
				preparedStatement.setNull(4, Types.TINYINT);
				preparedStatement.setNull(5, Types.VARCHAR);
			} else {

				addressIndicator = localAddress.getAddressIndicator();
				// _COLUMN_L_SPC+","+
				if (addressIndicator.isPCPresent()) {
					preparedStatement.setInt(1, localAddress.getSignalingPointCode());
				} else {
					preparedStatement.setNull(1, Types.SMALLINT);
				}

				// _COLUMN_L_SSN+","+
				if (addressIndicator.isSSNPresent()) {
					preparedStatement.setByte(2, (byte) localAddress.getSubsystemNumber());
				} else {
					preparedStatement.setNull(2, Types.TINYINT);
				}
				// _COLUMN_L_RI+","+
				if (addressIndicator.getRoutingIndicator() != null) {
					preparedStatement.setByte(3, (byte) addressIndicator.getRoutingIndicator().getValue());
				} else {
					preparedStatement.setNull(3, Types.TINYINT);
				}

				// _COLUMN_L_GT_I+","+
				gt = localAddress.getGlobalTitle();
				if (gt != null && gt.getGlobalTitleIndicator() != null) {
					preparedStatement.setByte(4, (byte) gt.getGlobalTitleIndicator().getValue());
				} else {
					preparedStatement.setNull(4, Types.TINYINT);
				}
				// _COLUMN_L_GT_DIGITS+","+
				if (gt != null && gt.getDigits() != null) {
					preparedStatement.setString(5, gt.getDigits());
				} else {
					preparedStatement.setNull(5, Types.VARCHAR);
				}
			}

			SccpAddress remoteAddress = super.state.getRemoteAddress();

			if (remoteAddress == null) {
				preparedStatement.setNull(6, Types.SMALLINT);
				preparedStatement.setNull(7, Types.TINYINT);
				preparedStatement.setNull(8, Types.TINYINT);
				preparedStatement.setNull(9, Types.TINYINT);
				preparedStatement.setNull(10, Types.VARCHAR);
			} else {

				addressIndicator = remoteAddress.getAddressIndicator();
				// _COLUMN_R_SPC+","+
				if (addressIndicator.isPCPresent()) {
					preparedStatement.setInt(6, remoteAddress.getSignalingPointCode());
				} else {
					preparedStatement.setNull(6, Types.SMALLINT);
				}

				// _COLUMN_R_SSN+","+
				if (addressIndicator.isSSNPresent()) {
					preparedStatement.setByte(7, (byte) remoteAddress.getSubsystemNumber());
				} else {
					preparedStatement.setNull(7, Types.TINYINT);
				}
				// _COLUMN_R_RI+","+
				if (addressIndicator.getRoutingIndicator() != null) {
					preparedStatement.setByte(8, (byte) addressIndicator.getRoutingIndicator().getValue());
				} else {
					preparedStatement.setNull(8, Types.TINYINT);
				}

				// _COLUMN_R_GT_I+","+
				gt = remoteAddress.getGlobalTitle();
				if (gt != null && gt.getGlobalTitleIndicator() != null) {
					preparedStatement.setByte(9, (byte) gt.getGlobalTitleIndicator().getValue());
				} else {
					preparedStatement.setNull(9, Types.TINYINT);
				}
				// _COLUMN_R_GT_DIGITS+","+
				if (gt != null && gt.getDigits() != null) {
					preparedStatement.setString(10, gt.getDigits());
				} else {
					preparedStatement.setNull(10, Types.VARCHAR);
				}
			}
			// _COLUMN_SERVICE_CODE+","+
			preparedStatement.setString(11, super.state.getServiceCode());

			AddressString addressString = super.state.getOrigReference();
			if (addressString != null) {
				// _COLUMN_OR_NATURE+","+
				// _COLUMN_OR_PLAN+","+
				// _COLUMN_OR_DIGITS+","+
				preparedStatement.setByte(12, (byte) addressString.getAddressNature().getIndicator());
				preparedStatement.setByte(13, (byte) addressString.getNumberingPlan().getIndicator());
				preparedStatement.setString(14, addressString.getAddress());

			} else {
				preparedStatement.setNull(12, Types.TINYINT);
				preparedStatement.setNull(13, Types.TINYINT);
				preparedStatement.setNull(14, Types.VARCHAR);

			}
			addressString = super.state.getDestReference();

			if (addressString != null) {
				// _COLUMN_DE_NATURE+","+
				// _COLUMN_DE_PLAN+","+
				// _COLUMN_DE_DIGITS+","+
				preparedStatement.setByte(15, (byte) addressString.getAddressNature().getIndicator());
				preparedStatement.setByte(16, (byte) addressString.getNumberingPlan().getIndicator());
				preparedStatement.setString(17, addressString.getAddress());

			} else {
				preparedStatement.setNull(15, Types.TINYINT);
				preparedStatement.setNull(16, Types.TINYINT);
				preparedStatement.setNull(17, Types.VARCHAR);

			}

			ISDNAddressString isdnAddressString = super.state.getISDNAddressString();
			if (isdnAddressString != null) {
				// _COLUMN_ISDN_NATURE+","+
				// _COLUMN_ISDN_PLAN+","+
				// _COLUMN_ISDN_DIGITS+","+
				preparedStatement.setByte(18, (byte) isdnAddressString.getAddressNature().getIndicator());
				preparedStatement.setByte(19, (byte) isdnAddressString.getNumberingPlan().getIndicator());
				preparedStatement.setString(20, isdnAddressString.getAddress());

			} else {
				preparedStatement.setNull(18, Types.TINYINT);
				preparedStatement.setNull(19, Types.TINYINT);
				preparedStatement.setNull(20, Types.VARCHAR);

			}

			addressString = super.state.getEriVlrNo();
			if (addressString != null) {
				// _COLUMN_VLR_NATURE+","+
				// _COLUMN_VLR_PLAN+","+
				// _COLUMN_VLR_DIGITS+","+
				preparedStatement.setByte(21, (byte) addressString.getAddressNature().getIndicator());
				preparedStatement.setByte(22, (byte) addressString.getNumberingPlan().getIndicator());
				preparedStatement.setString(23, addressString.getAddress());

			} else {
				preparedStatement.setNull(21, Types.TINYINT);
				preparedStatement.setNull(22, Types.TINYINT);
				preparedStatement.setNull(23, Types.VARCHAR);

			}

			IMSI imsi = super.state.getEriImsi();
			if (imsi != null) {
				// _COLUMN_IMSI+","+
				preparedStatement.setString(24, imsi.getData());
			} else {
				preparedStatement.setNull(24, Types.VARCHAR);
			}

			// _COLUMN_LOCAL_DIALOG_ID+","+
            if (super.state.getLocalDialogId() != null) {
                preparedStatement.setLong(25, super.state.getLocalDialogId());
            } else {
                preparedStatement.setNull(25, Types.BIGINT);
            }

			if (super.state.getRemoteDialogId() != null) {
				preparedStatement.setLong(26, super.state.getRemoteDialogId());
			} else {
				preparedStatement.setNull(26, Types.BIGINT);
			}

            //_COLUMN_DIALOG_DURATION
            Long dialogDuration = state.getDialogDuration();
            if(dialogDuration != null){
                //TODO: output as millis or?
                preparedStatement.setLong(27, dialogDuration);
            }else{
                preparedStatement.setNull(27, Types.BIGINT);
            }

            //_COLUMN_USSD_STRING
            String ussdString = state.getUssdString();
            if(ussdString != null && !ussdString.isEmpty()){
                preparedStatement.setString(28, ussdString);
            }else{
                preparedStatement.setNull(28, Types.VARCHAR);
            }

			// _COLUMN_TSTAMP+","+
			preparedStatement.setTimestamp(29, tstamp);
			// _COLUMN_TERMINATD+ <-- null - create record
			preparedStatement.setString(30, super.state.getRecordStatus().toString());

			preparedStatement.setString(31, super.state.getUssdType().toString());
			// _COLUMN_ID
			preparedStatement.setString(32, super.state.getId());
			preparedStatement.execute();

		} catch (SQLException e) {
			super.tracer.severe("Failed at execute!", e);
			throw new CDRCreateException(e);
		} catch (RuntimeException re) {
			super.tracer.severe("Failed at execute!", re);
			markRecordCorrupted(ctx);
		}
		return this;
	}
}

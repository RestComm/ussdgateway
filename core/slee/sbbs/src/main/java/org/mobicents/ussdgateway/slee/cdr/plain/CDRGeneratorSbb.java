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

package org.mobicents.ussdgateway.slee.cdr.plain;

import java.sql.Timestamp;

import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.SbbContext;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restcomm.protocols.ss7.indicator.AddressIndicator;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.UssdPropertiesManagementMBean;
import org.mobicents.ussdgateway.slee.USSDBaseSbb;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;
import org.mobicents.ussdgateway.slee.cdr.USSDCDRState;

/**
 * @author baranowb
 * 
 */
public abstract class CDRGeneratorSbb extends USSDBaseSbb implements ChargeInterface {
	
	private static final Logger cdrTracer = Logger.getLogger(CDRGeneratorSbb.class);
	
	private static final String CDR_GENERATED_TO = "Textfile";

	protected UssdPropertiesManagementMBean ussdPropertiesManagement = null;


    public CDRGeneratorSbb() {
		super("CDRGeneratorSbb");
		// TODO Auto-generated constructor stub
	}

	// --------------- SLEE Stuff ----------------------------
    // --------------- ChargeInterface methods ---------------
    /*
     * (non-Javadoc)
     * 
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#init(boolean)
     */
    @Override
    public void init(final boolean reset) {
        //noop
    	super.logger.info("Setting CDR_GENERATED_TO to "+CDR_GENERATED_TO);
    }

    /* (non-Javadoc)
     * @see org.mobicents.ussdgateway.slee.cdr.ChargeInterface#createRecord(org.mobicents.ussdgateway.slee.cdr.Status)
     */
    @Override
    public void createRecord(RecordStatus outcome) {
        USSDCDRState state = getState();

        if(state.isGenerated()){
            super.logger.severe("");
        }else{
            if(super.logger.isFineEnabled()){
                super.logger.fine("Generating record, status '"+outcome+"' for '"+state+"'");
            }
            DateTime startTime = state.getDialogStartTime();
            if(startTime!=null){
                DateTime endTime = DateTime.now();
                Long duration = endTime.getMillis() - startTime.getMillis();
                state.setDialogEndTime(endTime);
                state.setDialogDuration(duration);
            }
            state.setRecordStatus(outcome);
            state.setGenerated(true);
            this.setState(state);
            String data = toString(state);
            //TODO: set to fine so by default it wont spew console.
            this.cdrTracer.debug(data);
        }
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
    // CMPs

    public abstract USSDCDRState getUSSDCDRState();

    public abstract void setUSSDCDRState(USSDCDRState state);

    public void onStartServiceEvent(javax.slee.serviceactivity.ServiceStartedEvent event, ActivityContextInterface aci) {
    	this.init(true);
    }

    // --------------- SBB callbacks ---------------

   
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
     * @see javax.slee.Sbb#setSbbContext(javax.slee.SbbContext)
     */
    @Override
    public void setSbbContext(SbbContext ctx) {
        super.setSbbContext(ctx);
        super.logger = super.sbbContext.getTracer(TRACER_NAME);
        this.ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.slee.Sbb#unsetSbbContext()
     */
    @Override
    public void unsetSbbContext() {
        super.unsetSbbContext();
    }

    // -------- helper methods
    /**
     * @param state
     * @return
     */
    protected String toString(USSDCDRState state) {
        String SEPARATOR = ussdPropertiesManagement.getCdrSeparator();

        //StringBuilder is faster than StringBuffer
        final StringBuilder sb = new StringBuilder();
        final Timestamp tstamp = new Timestamp(System.currentTimeMillis());
        SccpAddress localAddress = state.getLocalAddress();
        if (localAddress != null) {
            AddressIndicator addressIndicator = localAddress.getAddressIndicator();
            // _COLUMN_L_SPC+","+
            if (addressIndicator.isPCPresent()) {
                sb.append(localAddress.getSignalingPointCode()).append(SEPARATOR);
            } else {
                sb.append(SEPARATOR);
            }

            // _COLUMN_L_SSN+","+
            if (addressIndicator.isSSNPresent()) {
                sb.append((byte) localAddress.getSubsystemNumber()).append(SEPARATOR);
            } else {
                sb.append(SEPARATOR);
            }
            // _COLUMN_L_RI+","+
            if (addressIndicator.getRoutingIndicator() != null) {
                sb.append((byte) addressIndicator.getRoutingIndicator().getValue()).append(SEPARATOR);
            } else {
                sb.append(SEPARATOR);
            }

            // _COLUMN_L_GT_I+","+
            GlobalTitle gt = localAddress.getGlobalTitle();
            if (gt != null && gt.getGlobalTitleIndicator() != null) {
                sb.append((byte) gt.getGlobalTitleIndicator().getValue()).append(SEPARATOR);
            } else {
                sb.append(SEPARATOR);
            }
            // _COLUMN_L_GT_DIGITS+","+
            if (gt != null && gt.getDigits() != null) {
                sb.append(gt.getDigits()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }
        }

        SccpAddress remoteAddress = state.getRemoteAddress();
        if (remoteAddress != null) {
            AddressIndicator addressIndicator = remoteAddress.getAddressIndicator();
            // _COLUMN_R_SPC+","+
            if (addressIndicator.isPCPresent()) {
                sb.append(remoteAddress.getSignalingPointCode()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }

            // _COLUMN_R_SSN+","+
            if (addressIndicator.isSSNPresent()) {
                sb.append((byte) remoteAddress.getSubsystemNumber()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }
            // _COLUMN_R_RI+","+
            if (addressIndicator.getRoutingIndicator() != null) {
                sb.append((byte) addressIndicator.getRoutingIndicator().getValue()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }

            // _COLUMN_R_GT_I+","+
            GlobalTitle gt = remoteAddress.getGlobalTitle();
            if (gt != null && gt.getGlobalTitleIndicator() != null) {
                sb.append((byte) gt.getGlobalTitleIndicator().getValue()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }
            // _COLUMN_R_GT_DIGITS+","+
            if (gt != null && gt.getDigits() != null) {
                sb.append(gt.getDigits()).append(SEPARATOR);
                ;
            } else {
                sb.append(SEPARATOR);
            }
        }

        //        _COLUMN_SERVICE_CODE+","+
      sb.append(state.getServiceCode()).append(SEPARATOR);

      AddressString addressString = state.getOrigReference();
      if(addressString != null){
//        _COLUMN_OR_NATURE+","+
//        _COLUMN_OR_PLAN+","+
//        _COLUMN_OR_DIGITS+","+
          sb.append((byte) addressString.getAddressNature().getIndicator()).append(SEPARATOR);
          sb.append((byte) addressString.getNumberingPlan().getIndicator()).append(SEPARATOR);
          sb.append(addressString.getAddress()).append(SEPARATOR);
          
      }else{
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          
      }
      addressString = state.getDestReference();

      if(addressString != null){
//        _COLUMN_DE_NATURE+","+
//        _COLUMN_DE_PLAN+","+
//        _COLUMN_DE_DIGITS+","+
          sb.append((byte) addressString.getAddressNature().getIndicator()).append(SEPARATOR);
          sb.append((byte) addressString.getNumberingPlan().getIndicator()).append(SEPARATOR);
          sb.append(addressString.getAddress()).append(SEPARATOR);
          
      }else{
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          
      }
      
      ISDNAddressString isdnAddressString= state.getISDNAddressString();
      if(isdnAddressString != null){
//        _COLUMN_ISDN_NATURE+","+
//        _COLUMN_ISDN_PLAN+","+
//        _COLUMN_ISDN_DIGITS+","+
          sb.append((byte) isdnAddressString.getAddressNature().getIndicator()).append(SEPARATOR);
          sb.append((byte) isdnAddressString.getNumberingPlan().getIndicator()).append(SEPARATOR);
          sb.append(isdnAddressString.getAddress()).append(SEPARATOR);
          
      }else{
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          
      }
      
      addressString= state.getEriVlrNo();
      if(addressString != null){
//        _COLUMN_VLR_NATURE+","+
//        _COLUMN_VLR_PLAN+","+
//        _COLUMN_VLR_DIGITS+","+
          sb.append((byte) addressString.getAddressNature().getIndicator()).append(SEPARATOR);
          sb.append((byte) addressString.getNumberingPlan().getIndicator()).append(SEPARATOR);
          sb.append(addressString.getAddress()).append(SEPARATOR);
          
      }else{
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          sb.append(SEPARATOR);
          
      }

      IMSI imsi = state.getEriImsi();
      if(imsi != null){
//        _COLUMN_IMSI+","+
          sb.append(imsi.getData()).append(SEPARATOR);
      }else{
          sb.append(SEPARATOR);
      }

        // _COLUMN_STATUS+ <-- null - create record
        sb.append(state.getRecordStatus().toString()).append(SEPARATOR);
        // _COLUMN_TYPE
        sb.append(state.getUssdType().toString()).append(SEPARATOR);
        // _COLUMN_TSTAMP+","+
        sb.append(tstamp).append(SEPARATOR);

        // _COLUMN_LOCAL_DIALOG_ID+","+
        sb.append(state.getLocalDialogId()).append(SEPARATOR);
        // _COLUMN_REMOTE_DIALOG_ID+","+
        sb.append(state.getRemoteDialogId()).append(SEPARATOR);
        Long dialogDuration = state.getDialogDuration();
        if (dialogDuration != null) {
            // _COLUMN_DIALOG_DURATION
            // TODO: output as millis or?
            sb.append(dialogDuration).append(SEPARATOR);
        } else {
            sb.append(SEPARATOR);
        }
        // _COLUMN_USSD_STRING
        String ussdString = state.getUssdString();
        if (ussdString != null && !ussdString.isEmpty()) {
            sb.append(ussdString).append(SEPARATOR);
        } else {
            sb.append(SEPARATOR);
        }

        // _COLUMN_ID
        sb.append(state.getId());

        return sb.toString();
    }
}

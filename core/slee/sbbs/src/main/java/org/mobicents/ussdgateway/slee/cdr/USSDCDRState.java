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

package org.mobicents.ussdgateway.slee.cdr;

import java.io.Serializable;

import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;

/**
 * Represents state associated with ongoing dialog required for proper CDR generation. Data which should be used for CDR is
 * spread across many objects. So we need object which can be used to store them in one, conveniant place.
 * 
 * @author baranowb
 * 
 */
public class USSDCDRState implements Serializable {

    // TODO: AddressString and IMSI hashCode + equals
    protected boolean initiated;
    // map has those... optional ech.
    protected AddressString origReference, destReference;
    // ericcsson bs...
    protected IMSI eriImsi;
    protected AddressString eriVlrNo;
    // USSD req stuff, its not present in answer.
    protected ISDNAddressString ISDNString;
    protected SccpAddress localAddress;
    protected SccpAddress remoteAddress;

    protected String serviceCode;

    protected Long dialogId;

    protected String id;
    /**
     * @return the initiated
     */
    public boolean isInitiated() {
        return initiated;
    }

    /**
     * @param initiated the initiated to set
     */
    public void setInitiated(boolean initiated) {
        this.initiated = initiated;
    }

    /**
     * @return the origReference
     */
    public AddressString getOrigReference() {
        return origReference;
    }

    /**
     * @param origReference the origReference to set
     */
    public void setOrigReference(AddressString origReference) {
        this.origReference = origReference;
    }

    /**
     * @return the destReference
     */
    public AddressString getDestReference() {
        return destReference;
    }

    /**
     * @param destReference the destReference to set
     */
    public void setDestReference(AddressString destReference) {
        this.destReference = destReference;
    }

    /**
     * @return the eriImsi
     */
    public IMSI getEriImsi() {
        return eriImsi;
    }

    /**
     * @param eriImsi the eriImsi to set
     */
    public void setEriImsi(IMSI eriImsi) {
        this.eriImsi = eriImsi;
    }

    /**
     * @return the eriVlrNo
     */
    public AddressString getEriVlrNo() {
        return eriVlrNo;
    }

    /**
     * @param eriVlrNo the eriVlrNo to set
     */
    public void setEriVlrNo(AddressString eriVlrNo) {
        this.eriVlrNo = eriVlrNo;
    }

    /**
     * @return the iSDNString
     */
    public ISDNAddressString getISDNString() {
        return ISDNString;
    }

    /**
     * @param iSDNString the iSDNString to set
     */
    public void setISDNString(ISDNAddressString iSDNString) {
        ISDNString = iSDNString;
    }

    /**
     * @return the localAddress
     */
    public SccpAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * @param localAddress the localAddress to set
     */
    public void setLocalAddress(SccpAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * @return the remoteAddress
     */
    public SccpAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * @param remoteAddress the remoteAddress to set
     */
    public void setRemoteAddress(SccpAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * @return the serviceCode
     */
    public String getServiceCode() {
        return serviceCode;
    }

    /**
     * @param serviceCode the serviceCode to set
     */
    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    /**
     * @return the dialogId
     */
    public Long getDialogId() {
        return dialogId;
    }

    /**
     * @param dialogId the dialogId to set
     */
    public void setDialogId(Long dialogId) {
        this.dialogId = dialogId;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    public void init(final Long dialogId, final String serviceCode, final AddressString destRef, final AddressString origRef,
            final ISDNAddressString isdnAddressString, final SccpAddress localAddress, final SccpAddress remoteAddress) {
        this.initiated = true;
        this.destReference = destRef;
        this.origReference = origRef;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.serviceCode = serviceCode;
        this.dialogId = dialogId;
        this.id = dialogId+""+System.currentTimeMillis();
    }

    public boolean isInitialized() {
        return this.initiated;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ISDNString == null) ? 0 : ISDNString.hashCode());
        result = prime * result + ((destReference == null) ? 0 : destReference.hashCode());
        result = prime * result + ((dialogId == null) ? 0 : dialogId.hashCode());
        result = prime * result + ((eriImsi == null) ? 0 : eriImsi.hashCode());
        result = prime * result + ((eriVlrNo == null) ? 0 : eriVlrNo.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (initiated ? 1231 : 1237);
        result = prime * result + ((localAddress == null) ? 0 : localAddress.hashCode());
        result = prime * result + ((origReference == null) ? 0 : origReference.hashCode());
        result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
        result = prime * result + ((serviceCode == null) ? 0 : serviceCode.hashCode());
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        USSDCDRState other = (USSDCDRState) obj;
        if (ISDNString == null) {
            if (other.ISDNString != null)
                return false;
        } else if (!ISDNString.equals(other.ISDNString))
            return false;
        if (destReference == null) {
            if (other.destReference != null)
                return false;
        } else if (!destReference.equals(other.destReference))
            return false;
        if (dialogId == null) {
            if (other.dialogId != null)
                return false;
        } else if (!dialogId.equals(other.dialogId))
            return false;
        if (eriImsi == null) {
            if (other.eriImsi != null)
                return false;
        } else if (!eriImsi.equals(other.eriImsi))
            return false;
        if (eriVlrNo == null) {
            if (other.eriVlrNo != null)
                return false;
        } else if (!eriVlrNo.equals(other.eriVlrNo))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (initiated != other.initiated)
            return false;
        if (localAddress == null) {
            if (other.localAddress != null)
                return false;
        } else if (!localAddress.equals(other.localAddress))
            return false;
        if (origReference == null) {
            if (other.origReference != null)
                return false;
        } else if (!origReference.equals(other.origReference))
            return false;
        if (remoteAddress == null) {
            if (other.remoteAddress != null)
                return false;
        } else if (!remoteAddress.equals(other.remoteAddress))
            return false;
        if (serviceCode == null) {
            if (other.serviceCode != null)
                return false;
        } else if (!serviceCode.equals(other.serviceCode))
            return false;
        return true;
    }
}

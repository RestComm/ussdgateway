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

package org.mobicents.ussdgateway.slee.cdr;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.DateTime;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.IMSI;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;

/**
 * Represents state associated with ongoing dialog required for proper CDR
 * generation. Data which should be used for CDR is spread across many objects.
 * So we need object which can be used to store them in one, conveniant place.
 * 
 * @author baranowb
 * 
 */
public class USSDCDRState implements Serializable {

    public static final String USSD_STRING_SEPARATOR = ",";

	// TODO: AddressString and IMSI hashCode + equals
	protected boolean initiated;
	protected boolean generated;
	// map has those... optional ech.
	protected AddressString origReference, destReference;
	// ericcsson bs...
	protected IMSI eriImsi;
	protected AddressString eriVlrNo;
	// USSD req stuff, its not present in answer.
	protected ISDNAddressString isdnAddressString;
	protected SccpAddress localAddress;
	protected SccpAddress remoteAddress;

	protected String serviceCode;

	protected Long localDialogId;
	protected Long remoteDialogId;

	//NB: once we update to JDK8, we should revert to using standard java.time package
    protected DateTime dialogStartTime;
    protected DateTime dialogEndTime;
    protected Long dialogDuration;

	protected String id;

	protected RecordStatus recordStatus;

	private USSDType ussdType = USSDType.PULL;

    protected String ussdString;

	/**
	 * @return the destReference
	 */
	public AddressString getDestReference() {
		return destReference;
	}

	/**
	 * @return the localDialogId
	 */
	public Long getLocalDialogId() {
		return localDialogId;
	}

	/**
	 * @return the eriImsi
	 */
	public IMSI getEriImsi() {
		return eriImsi;
	}

	/**
	 * @return the eriVlrNo
	 */
	public AddressString getEriVlrNo() {
		return eriVlrNo;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the iSDNString
	 */
	public ISDNAddressString getISDNAddressString() {
		return isdnAddressString;
	}

	/**
	 * @return the localAddress
	 */
	public SccpAddress getLocalAddress() {
		return localAddress;
	}

	/**
	 * @return the origReference
	 */
	public AddressString getOrigReference() {
		return origReference;
	}

	/**
	 * @return the remoteAddress
	 */
	public SccpAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @return the serviceCode
	 */
	public String getServiceCode() {
		return serviceCode;
	}

	public void init(final Long dialogId, final String serviceCode, final AddressString destRef,
			final AddressString origRef, final ISDNAddressString isdnAddressString, final SccpAddress localAddress,
			final SccpAddress remoteAddress) {
		this.initiated = true;
		this.destReference = destRef;
		this.origReference = origRef;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		this.serviceCode = serviceCode;
		this.localDialogId = dialogId;
		this.isdnAddressString = isdnAddressString;
		// This should be enough to be unique
		this.id = UUID.randomUUID().toString();

        this.dialogStartTime = null;
        this.dialogEndTime = null;
        this.dialogDuration =null;
	}

	/**
	 * @return the generated
	 */
	public boolean isGenerated() {
		return generated;
	}

	public boolean isInitialized() {
		return this.initiated;
	}

	/**
	 * @return the initiated
	 */
	public boolean isInitiated() {
		return initiated;
	}

	/**
	 * @param destReference
	 *            the destReference to set
	 */
	public void setDestReference(AddressString destReference) {
		this.destReference = destReference;
	}

	/**
	 * @param localDialogId
	 *            the localDialogId to set
	 */
	public void setLocalDialogId(Long dialogId) {
		this.localDialogId = dialogId;
	}

	public Long getRemoteDialogId() {
		return this.remoteDialogId;
	}

	public void setRemoteDialogId(Long remoteDialogId) {
		this.remoteDialogId = remoteDialogId;
	}

	public void setDialogStartTime(DateTime startTime) {
		this.dialogStartTime = startTime;
	}
	public DateTime getDialogStartTime() {
		return this.dialogStartTime;
	}

	public void setDialogEndTime(DateTime endTime) {
		this.dialogEndTime = endTime;
	}

	public DateTime getDialogEndTime() {
		return this.dialogEndTime;
	}

	public void setDialogDuration(Long duration) {
		this.dialogDuration = duration;
	}
	public Long getDialogDuration() {
		return this.dialogDuration;
	}

	/**
	 * @param eriImsi
	 *            the eriImsi to set
	 */
	public void setEriImsi(IMSI eriImsi) {
		this.eriImsi = eriImsi;
	}

	/**
	 * @param eriVlrNo
	 *            the eriVlrNo to set
	 */
	public void setEriVlrNo(AddressString eriVlrNo) {
		this.eriVlrNo = eriVlrNo;
	}

	/**
	 * @param generated
	 *            the generated to set
	 */
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param initiated
	 *            the initiated to set
	 */
	public void setInitiated(boolean initiated) {
		this.initiated = initiated;
	}

	/**
	 * @param iSDNString
	 *            the iSDNString to set
	 */
	public void setISDNAddressString(ISDNAddressString iSDNString) {
		isdnAddressString = iSDNString;
	}

	/**
	 * @param localAddress
	 *            the localAddress to set
	 */
	public void setLocalAddress(SccpAddress localAddress) {
		this.localAddress = localAddress;
	}

	/**
	 * @param origReference
	 *            the origReference to set
	 */
	public void setOrigReference(AddressString origReference) {
		this.origReference = origReference;
	}

	/**
	 * @param remoteAddress
	 *            the remoteAddress to set
	 */
	public void setRemoteAddress(SccpAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * @param serviceCode
	 *            the serviceCode to set
	 */
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	/**
	 * @return the recordStatus
	 */
	public RecordStatus getRecordStatus() {
		return recordStatus;
	}

	/**
	 * @param recordStatus
	 *            the recordStatus to set
	 */
	public void setRecordStatus(RecordStatus recordStatus) {
		this.recordStatus = recordStatus;
	}

	public USSDType getUssdType() {
		return ussdType;
	}

	public void setUssdType(USSDType ussdType) {
		this.ussdType = ussdType;
	}

    public String getUssdString() {
        return this.ussdString;
    }

    public void setUssdString(String ussdString) {
        this.ussdString = ussdString;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isdnAddressString == null) ? 0 : isdnAddressString.hashCode());
		result = prime * result + ((destReference == null) ? 0 : destReference.hashCode());
		result = prime * result + ((localDialogId == null) ? 0 : localDialogId.hashCode());
		result = prime * result + ((remoteDialogId == null) ? 0 : remoteDialogId.hashCode());
		result = prime * result + ((eriImsi == null) ? 0 : eriImsi.hashCode());
		result = prime * result + ((eriVlrNo == null) ? 0 : eriVlrNo.hashCode());
		result = prime * result + (generated ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (initiated ? 1231 : 1237);
		result = prime * result + ((localAddress == null) ? 0 : localAddress.hashCode());
		result = prime * result + ((origReference == null) ? 0 : origReference.hashCode());
		result = prime * result + ((recordStatus == null) ? 0 : recordStatus.hashCode());
		result = prime * result + ((remoteAddress == null) ? 0 : remoteAddress.hashCode());
		result = prime * result + ((serviceCode == null) ? 0 : serviceCode.hashCode());
		result = prime * result + ussdType.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		if (isdnAddressString == null) {
			if (other.isdnAddressString != null)
				return false;
		} else if (!isdnAddressString.equals(other.isdnAddressString))
			return false;
		if (destReference == null) {
			if (other.destReference != null)
				return false;
		} else if (!destReference.equals(other.destReference))
			return false;
		if (localDialogId == null) {
			if (other.localDialogId != null)
				return false;
		} else if (!localDialogId.equals(other.localDialogId))
			return false;

		if (remoteDialogId == null) {
			if (other.remoteDialogId != null)
				return false;
		} else if (!remoteDialogId.equals(other.remoteDialogId))
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
		if (generated != other.generated)
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
		if (recordStatus != other.recordStatus)
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

		if (ussdType != other.ussdType)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "USSDCDRState [initiated=" + initiated + ", generated=" + generated + ", origReference=" + origReference
				+ ", destReference=" + destReference + ", eriImsi=" + eriImsi + ", eriVlrNo=" + eriVlrNo
				+ ", ISDNString=" + isdnAddressString + ", localAddress=" + localAddress + ", remoteAddress="
				+ remoteAddress + ", serviceCode=" + serviceCode + ", localDialogId=" + localDialogId
				+ ", remoteDialogId=" + remoteDialogId + ", id=" + id + ", recordStatus=" + recordStatus
				+ ", ussdType=" + ussdType + " ]@" + super.hashCode();
	}

}

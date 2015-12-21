/**
 * 
 */
package org.mobicents.ussdgateway.slee;

import javax.slee.CreateException;
import javax.slee.SLEEException;
import javax.slee.TransactionRequiredLocalException;

import org.mobicents.slee.ChildRelationExt;
import org.mobicents.ussdgateway.UssdPropertiesManagement;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterface;
import org.mobicents.ussdgateway.slee.cdr.ChargeInterfaceParent;
import org.mobicents.ussdgateway.slee.cdr.RecordStatus;

/**
 * @author Amit Bhayani
 * 
 */
public abstract class ChildServerSbb extends USSDBaseSbb implements ChargeInterfaceParent {

	public ChildServerSbb(String loggerName) {
		super(loggerName);
	}

	@Override
	public void recordGenerationSucessed() {
		if (this.logger.isFineEnabled()) {
			this.logger.fine("Generated CDR for Status: " + getCDRChargeInterface().getState());
		}
	}

	@Override
	public void recordGenerationFailed(String message) {
		this.logger.severe("Failed to generate CDR! Message: '" + message + "'");
		this.logger.severe("Status: " + getCDRChargeInterface().getState());
	}

	@Override
	public void recordGenerationFailed(String message, Throwable t) {
		this.logger.severe("Failed to generate CDR! Message: '" + message + "'", t);
		this.logger.severe("Status: " + getCDRChargeInterface().getState());
	}

	@Override
	public void initFailed(String message, Throwable t) {
		this.logger.severe("Failed to initializee CDR Database! Message: '" + message + "'", t);
	}

	@Override
	public void initSuccessed() {
		if (this.logger.isFineEnabled()) {
			this.logger.fine("CDR Database has been initialized!");
		}
	}

	// ///////////////////
	// Charge interface //
	// ///////////////////

	private static final String CHARGER = "CHARGER";

    public abstract ChildRelationExt getCDRInterfaceChildRelation();

    public abstract ChildRelationExt getCDRPlainInterfaceChildRelation();

	public ChargeInterface getCDRChargeInterface() {
        UssdPropertiesManagement ussdPropertiesManagement = UssdPropertiesManagement.getInstance();
        ChildRelationExt childExt;
        if (ussdPropertiesManagement.getCdrLoggingTo() == UssdPropertiesManagement.CdrLoggedType.Textfile) {
            childExt = getCDRPlainInterfaceChildRelation();
        } else {
            childExt = getCDRInterfaceChildRelation();
        }

		ChargeInterface child = (ChargeInterface) childExt.get(CHARGER);
		if (child == null) {
			try {
				child = (ChargeInterface) childExt.create(CHARGER);
			} catch (TransactionRequiredLocalException e) {
				logger.severe("TransactionRequiredLocalException when creating CDR child", e);
			} catch (IllegalArgumentException e) {
				logger.severe("IllegalArgumentException when creating CDR child", e);
			} catch (NullPointerException e) {
				logger.severe("NullPointerException when creating CDR child", e);
			} catch (SLEEException e) {
				logger.severe("SLEEException when creating CDR child", e);
			} catch (CreateException e) {
				logger.severe("CreateException when creating CDR child", e);
			}
		}

		return child;
	}
	
	protected void createCDRRecord(RecordStatus recordStatus) {
		try {
			this.getCDRChargeInterface().createRecord(recordStatus);
		} catch (Exception e) {
			logger.severe("Error while trying to create CDR Record", e);
		}
	}

}

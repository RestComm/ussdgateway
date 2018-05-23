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

package org.mobicents.ussd.ihub;

import org.restcomm.protocols.ss7.map.MAPStackImpl;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.sccp.impl.SccpStackImpl;

/**
 * 
 * @author Amit Bhayani
 *
 */
public class MAPSimulator {
	
	// MAP
	private MAPStackImpl mapStack;
	private MAPProvider mapProvider;

	// SCCP
	private SccpStackImpl sccpStack;

	// SSn
	private int ssn;

	private MAPListener mapListener = null;

	public MAPSimulator() {

	}

	public SccpStackImpl getSccpStack() {
		return sccpStack;
	}

	public void setSccpStack(SccpStackImpl sccpStack) {
		this.sccpStack = sccpStack;
	}

	public int getSsn() {
		return ssn;
	}

	public void setSsn(int ssn) {
		this.ssn = ssn;
	}

	public void start() throws Exception {
		// Create MAP Stack and register listener
		this.mapStack = new MAPStackImpl("MAPStack", this.sccpStack.getSccpProvider(), this.getSsn());
		this.mapProvider = this.mapStack.getMAPProvider();

		this.mapListener = new MAPListener(this);

		this.mapProvider.addMAPDialogListener(this.mapListener);
		
		this.mapProvider.getMAPServiceSupplementary().addMAPServiceListener(this.mapListener);
		
		this.mapProvider.getMAPServiceSms().addMAPServiceListener(this.mapListener);

		this.mapProvider.getMAPServiceSms().acivate();
		this.mapProvider.getMAPServiceSupplementary().acivate();

		this.mapStack.start();

	}

	public void stop() {
		this.mapStack.stop();
	}

	protected MAPProvider getMapProvider() {
		return mapProvider;
	}

}

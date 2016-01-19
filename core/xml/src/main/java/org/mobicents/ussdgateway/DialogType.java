/**
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.mobicents.ussdgateway;


/**
 * 
 * @author amit bhayani
 *
 */
public enum DialogType {
	BEGIN("BEGIN"), CONTINUE("CONTINUE"), END("END"), ABORT("ABORT");

	private static final String BEGIN_STATE = "BEGIN";
	private static final String CONTINUE_STATE = "CONTINUE";
	private static final String END_STATE = "END";
	private static final String ABORT_STATE = "ABORT";

	private final String type;

	private DialogType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public static DialogType getInstance(String type) {
		if (BEGIN_STATE.equals(type)) {
			return BEGIN;
		} else if (CONTINUE_STATE.equals(type)) {
			return CONTINUE;
		} else if (END_STATE.equals(type)) {
			return END;
		} else if (ABORT_STATE.equals(type)) {
			return ABORT;
		}

		return null;
	}
}

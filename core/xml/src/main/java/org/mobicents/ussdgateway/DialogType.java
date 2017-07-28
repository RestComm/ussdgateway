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

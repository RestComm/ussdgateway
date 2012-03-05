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

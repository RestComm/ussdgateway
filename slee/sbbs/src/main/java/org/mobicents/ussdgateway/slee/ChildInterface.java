package org.mobicents.ussdgateway.slee;

import org.mobicents.ussdgateway.Dialog;
import org.mobicents.ussdgateway.rules.Call;

public interface ChildInterface {
	public void setCallFact(Call call);
	
	public void setDialog(Dialog dialog);
}

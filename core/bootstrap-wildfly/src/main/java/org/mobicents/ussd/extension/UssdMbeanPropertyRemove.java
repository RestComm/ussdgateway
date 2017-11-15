package org.mobicents.ussd.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

class UssdMbeanPropertyRemove extends AbstractRemoveStepHandler {

    public static final UssdMbeanPropertyRemove INSTANCE = new UssdMbeanPropertyRemove();

    private UssdMbeanPropertyRemove() {
    }
}
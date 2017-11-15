package org.mobicents.ussd.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

class UssdMbeanRemove extends AbstractRemoveStepHandler {

    static final UssdMbeanRemove INSTANCE = new UssdMbeanRemove();

    private UssdMbeanRemove() {
    }
}
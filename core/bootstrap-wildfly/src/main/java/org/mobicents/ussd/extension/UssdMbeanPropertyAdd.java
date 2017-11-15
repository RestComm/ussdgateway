package org.mobicents.ussd.extension;

import static org.mobicents.ussd.extension.UssdMbeanPropertyDefinition.PROPERTY_ATTRIBUTES;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.dmr.ModelNode;

class UssdMbeanPropertyAdd extends AbstractAddStepHandler {

    public static final UssdMbeanPropertyAdd INSTANCE = new UssdMbeanPropertyAdd();

    private UssdMbeanPropertyAdd() {
    }

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        UssdMbeanPropertyDefinition.NAME_ATTR.validateAndSet(operation, model);
        for (SimpleAttributeDefinition def : PROPERTY_ATTRIBUTES) {
            def.validateAndSet(operation, model);
        }
    }
}
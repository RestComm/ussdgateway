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

import java.util.Map;

import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageAbsentSubscriber;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageAbsentSubscriberSM;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageBusySubscriber;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageCUGReject;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageCallBarred;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageExtensionContainer;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageFacilityNotSup;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageParameterless;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessagePositionMethodFailure;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessagePwRegistrationFailure;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageRoamingNotAllowed;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageSMDeliveryFailure;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageSsErrorStatus;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageSsIncompatibility;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageSubscriberBusyForMtSms;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageSystemFailure;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageUnauthorizedLCSClient;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageUnknownSubscriber;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * @author Amit Bhayani
 * @author sergey vetyutnev
 *
 */
public class ErrorComponentMap {
	
	public static final String MAP_ERROR_EXT_CONTAINER = MAPErrorMessageExtensionContainer.class.getSimpleName();
	public static final String MAP_ERROR_SM_DEL_FAILURE = MAPErrorMessageSMDeliveryFailure.class.getSimpleName();
	public static final String MAP_ERROR_ABSENT_SUBS_SM = MAPErrorMessageAbsentSubscriberSM.class.getSimpleName();
	public static final String MAP_ERROR_SYSTEM_FAILURE = MAPErrorMessageSystemFailure.class.getSimpleName();
	public static final String MAP_ERROR_CALL_BARRED = MAPErrorMessageCallBarred.class.getSimpleName();
	public static final String MAP_ERROR_FACILITY_NOT_SUPPORTED = MAPErrorMessageFacilityNotSup.class.getSimpleName();
	public static final String MAP_ERROR_UNKNOWN_SUBS = MAPErrorMessageUnknownSubscriber.class.getSimpleName();
	public static final String MAP_ERROR_SUBS_BUSY_FOR_MT_SMS = MAPErrorMessageSubscriberBusyForMtSms.class.getSimpleName();
	public static final String MAP_ERROR_ABSENT_SUBS = MAPErrorMessageAbsentSubscriber.class.getSimpleName();
	public static final String MAP_ERROR_UNAUTHORIZED_LCS_CLIENT = MAPErrorMessageUnauthorizedLCSClient.class.getSimpleName();
	public static final String MAP_ERROR_POSITION_METHOD_FAIL = MAPErrorMessagePositionMethodFailure.class.getSimpleName();
	public static final String MAP_ERROR_BUSY_SUBS = MAPErrorMessageBusySubscriber.class.getSimpleName();
	public static final String MAP_ERROR_CUG_REJECT = MAPErrorMessageCUGReject.class.getSimpleName();
	public static final String MAP_ERROR_ROAMING_NOT_ALLOWED = MAPErrorMessageRoamingNotAllowed.class.getSimpleName();
	public static final String MAP_ERROR_SS_ERROR_STATUS = MAPErrorMessageSsErrorStatus.class.getSimpleName();
	public static final String MAP_ERROR_SS_INCOMPATIBILITY = MAPErrorMessageSsIncompatibility.class.getSimpleName();
	public static final String MAP_ERROR_PW_REGS_FAIL = MAPErrorMessagePwRegistrationFailure.class.getSimpleName();
	public static final String MAP_ERROR_PARAM_LESS = MAPErrorMessageParameterless.class.getSimpleName();

    private static final String INVOKE_ID = "invokeId";
    private static final String ERROR_COMPONENT = "errorComponent";

    private FastMap<Long, MAPErrorMessage> data = new FastMap<Long, MAPErrorMessage>();

    public void put(Long invokeId, MAPErrorMessage mapErrorMessage) {
        this.data.put(invokeId, mapErrorMessage);
    }

    public void clear() {
        this.data.clear();
    }

    public int size() {
        return this.data.size();
    }

    public Map<Long, MAPErrorMessage> getErrorComponents() {
        return data.unmodifiable();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ErrorComponentMap=[");

        int i1 = 0;
        for (Entry<Long, MAPErrorMessage> n = data.head(), end = data.tail(); (n = n.getNext()) != end;) {
            Long id = n.getKey();
            MAPErrorMessage mapErrorMessage = n.getValue();

            if (i1 == 0)
                i1 = 1;
            else
                sb.append(", ");
            sb.append("invokeId=");
            sb.append(id);
            sb.append(", mapErrorMessage=");
            sb.append(mapErrorMessage);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * XML Serialization/Deserialization
     */
    protected static final XMLFormat<ErrorComponentMap> ERROR_COMPONENT_MAP_XML = new XMLFormat<ErrorComponentMap>(
            ErrorComponentMap.class) {

        @Override
        public void read(javolution.xml.XMLFormat.InputElement xml, ErrorComponentMap data) throws XMLStreamException {
            data.data.clear();

            while (xml.hasNext()) {
                Long id = xml.get(INVOKE_ID, Long.class);
                Object pe = xml.get(ERROR_COMPONENT);
                MAPErrorMessage mapErrorMessage = (MAPErrorMessage) pe;
                data.data.put(id, mapErrorMessage);
            }
        }

        @Override
        public void write(ErrorComponentMap data, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
            if (data.data.size() > 0) {
                for (Entry<Long, MAPErrorMessage> n = data.data.head(), end = data.data.tail(); (n = n.getNext()) != end;) {
                    Long id = n.getKey();
                    MAPErrorMessage mapErrorMessage = n.getValue();

                    xml.add(id, INVOKE_ID, Long.class);
                    xml.add(mapErrorMessage, ERROR_COMPONENT);
                }
            }
        }
    };
}

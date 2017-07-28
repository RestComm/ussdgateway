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

package org.mobicents.ussdgateway.slee.cdr;

/**
 * @author baranowb
 * @author sergey vetyutnev
 *
 */
public enum RecordStatus {
    SUCCESS,
    FAILED_INVOKE_TIMEOUT,
    FAILED_DIALOG_TIMEOUT,
    FAILED_APP_TIMEOUT,
    FAILED_CORRUPTED_MESSAGE,
    FAILED_TRANSPORT_ERROR, 
    FAILED_TRANSPORT_FAILURE, 
    FAILED_PROVIDER_ABORT, 
    FAILED_DIALOG_USER_ABORT,
    FAILED_DIALOG_REJECTED,
    FAILED_SYSTEM_FAILURE,
    FAILED_ABSENT_SUBSCRIBER,
    FAILED_ILLEGAL_SUBSCRIBER,
    FAILED_USSD_BUSY,
    FAILED_MAP_ERROR_COMPONENT,
    FAILED_MAP_REJECT_COMPONENT,
    ABORT_APP,
    SRI_DIALOG_REJECTED,
    SRI_PROVIDER_ABORT, 
    SRI_DIALOG_USER_ABORT,
    SRI_DIALOG_TIMEOUT,
    SRI_MAP_REJECT_COMPONENT,
    SRI_ABSENT_SUBSCRIBER,
    SRI_CALL_BARRED,
    SRI_TELESERVICE_NOT_PROVISIONED,
    SRI_UNKNOWN_SUBSCRIBER,
    SRI_MAP_ERROR_COMPONENT,
    ;
}

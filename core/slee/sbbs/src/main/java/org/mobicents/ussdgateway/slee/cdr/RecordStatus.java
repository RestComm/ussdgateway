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

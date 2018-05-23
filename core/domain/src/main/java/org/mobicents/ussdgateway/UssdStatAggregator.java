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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.restcomm.protocols.ss7.statistics.StatDataCollectionImpl;
import org.restcomm.protocols.ss7.statistics.api.LongValue;
import org.restcomm.protocols.ss7.statistics.api.StatDataCollection;
import org.restcomm.protocols.ss7.statistics.api.StatDataCollectorType;
import org.restcomm.protocols.ss7.statistics.api.StatResult;

import com.codahale.metrics.Counter;

/**
*
* @author sergey vetyutnev
*
*/
public class UssdStatAggregator {

    private static String MIN_DIALOGS_IN_PROCESS = "MinDialogsInProcess";
    private static String MAX_DIALOGS_IN_PROCESS = "MaxDialogsInProcess";

    private static String REQUESTS_PER_USSD_CODE = "RequestsPerUssdCode";

    private final static UssdStatAggregator instance = new UssdStatAggregator();
    private StatCollector statCollector = new StatCollector();
    private UUID sessionId = UUID.randomUUID();

    private Counter counterDialogs;
    private Counter counterMessages;

    public static UssdStatAggregator getInstance() {
        return instance;
    }

    public void setCounterDialogs(Counter counterDialogs) {
        this.counterDialogs = counterDialogs;
    }

    public void setCounterMessages(Counter counterMessages) {
        this.counterMessages = counterMessages;
    }

    public void reset() {
        statCollector = new StatCollector();
        sessionId = UUID.randomUUID();
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void addDialogsInProcess() {
        long newVal = this.statCollector.dialogsCnt.addAndGet(1);
        updateMinDialogsInProcess(newVal);
        updateMaxDialogsInProcess(newVal);
    }

    public void removeDialogsInProcess() {
        long newVal = this.statCollector.dialogsCnt.addAndGet(-1);
        updateMinDialogsInProcess(newVal);
        updateMaxDialogsInProcess(newVal);
    }

    public void clearDialogsInProcess() {
        this.statCollector.dialogsCnt.set(0);
        updateMinDialogsInProcess(0);
        updateMaxDialogsInProcess(0);
    }

    public Long getMinDialogsInProcess(String compainName) {
        StatResult res = this.statCollector.statDataCollection.restartAndGet(MIN_DIALOGS_IN_PROCESS, compainName);
        this.statCollector.statDataCollection.updateData(MIN_DIALOGS_IN_PROCESS, this.statCollector.dialogsCnt.get());
        if (res != null)
            return res.getLongValue();
        else
            return null;
    }

    private void updateMinDialogsInProcess(long newVal) {
        this.statCollector.statDataCollection.updateData(MIN_DIALOGS_IN_PROCESS, newVal);
    }

    public Long getMaxDialogsInProcess(String compainName) {
        StatResult res = this.statCollector.statDataCollection.restartAndGet(MAX_DIALOGS_IN_PROCESS, compainName);
        this.statCollector.statDataCollection.updateData(MAX_DIALOGS_IN_PROCESS, this.statCollector.dialogsCnt.get());
        if (res != null)
            return res.getLongValue();
        else
            return null;
    }

    private void updateMaxDialogsInProcess(long newVal) {
        this.statCollector.statDataCollection.updateData(MAX_DIALOGS_IN_PROCESS, newVal);
    }

    public Long getCurrentDialogsInProcess(String compainName) {
        return statCollector.dialogsCnt.get();
    }

    public long getDialogsAllEstablished() {
        return statCollector.dialogsAllEstablished.get();
    }

    public void updateDialogsAllEstablished() {
        statCollector.dialogsAllEstablished.addAndGet(1);

        if (counterDialogs != null)
            counterDialogs.inc();
    }

    public long getDialogsAllFailed() {
        return statCollector.dialogsAllFailed.get();
    }

    public void updateDialogsAllFailed() {
        statCollector.dialogsAllFailed.addAndGet(1);
    }

    public long getDialogsPullEstablished() {
        return statCollector.dialogsPullEstablished.get();
    }

    public void updateDialogsPullEstablished() {
        statCollector.dialogsPullEstablished.addAndGet(1);
    }

    public long getDialogsPullFailed() {
        return statCollector.dialogsPullFailed.get();
    }

    public void updateDialogsPullFailed() {
        statCollector.dialogsPullFailed.addAndGet(1);
    }

    public long getDialogsPushEstablished() {
        return statCollector.dialogsPushEstablished.get();
    }

    public void updateDialogsPushEstablished() {
        statCollector.dialogsPushEstablished.addAndGet(1);
    }

    public long getDialogsPushFailed() {
        return statCollector.dialogsPushFailed.get();
    }

    public void updateDialogsPushFailed() {
        statCollector.dialogsPushFailed.addAndGet(1);
    }

    public long getDialogsHttpEstablished() {
        return statCollector.dialogsHttpEstablished.get();
    }

    public void updateDialogsHttpEstablished() {
        statCollector.dialogsHttpEstablished.addAndGet(1);
    }

    public long getDialogsHttpFailed() {
        return statCollector.dialogsHttpFailed.get();
    }

    public void updateDialogsHttpFailed() {
        statCollector.dialogsHttpFailed.addAndGet(1);
    }

    public long getDialogsSipEstablished() {
        return statCollector.dialogsSipEstablished.get();
    }

    public void updateDialogsSipEstablished() {
        statCollector.dialogsSipEstablished.addAndGet(1);
    }

    public long getDialogsSipFailed() {
        return statCollector.dialogsSipFailed.get();
    }

    public void updateDialogsSipFailed() {
        statCollector.dialogsSipFailed.addAndGet(1);
    }

    public long getMessagesRecieved() {
        return statCollector.messagesRecieved.get();
    }

    public void updateMessagesRecieved() {
        statCollector.messagesRecieved.addAndGet(1);
    }

    public long getMessagesSent() {
        return statCollector.messagesSent.get();
    }

    public void updateMessagesSent() {
        statCollector.messagesSent.addAndGet(1);
    }

    public long getMessagesAll() {
        return statCollector.messagesAll.get();
    }

    public long getMessagesAllCumulative() {
        return statCollector.messagesAll.get();
    }

    public void updateMessagesAll() {
        statCollector.messagesAll.addAndGet(1);

        if (counterMessages != null)
            counterMessages.inc();
    }

    public long getDialogsAllEstablishedCumulative() {
        return statCollector.dialogsAllEstablished.get();
    }

    public long getDialogsAllFailedCumulative() {
        return statCollector.dialogsAllFailed.get();
    }


    public long getProcessUssdRequestOperations() {
        return statCollector.processUssdRequestOperations.get();
    }

    public void updateProcessUssdRequestOperations() {
        statCollector.processUssdRequestOperations.addAndGet(1);
    }

    public long getUssdRequestOperations() {
        return statCollector.ussdRequestOperations.get();
    }

    public void updateUssdRequestOperations() {
        statCollector.ussdRequestOperations.addAndGet(1);
    }

    public long getUssdNotifyOperations() {
        return statCollector.ussdNotifyOperations.get();
    }

    public void updateUssdNotifyOperations() {
        statCollector.ussdNotifyOperations.addAndGet(1);
    }

    public long getUssdPullNoRoutingRule() {
        return statCollector.ussdPullNoRoutingRule.get();
    }

    public void updateUssdPullNoRoutingRule() {
        statCollector.ussdPullNoRoutingRule.addAndGet(1);
    }

    public long getMapErrorAbsentSubscribers() {
        return statCollector.mapErrorAbsentSubscribers.get();
    }

    public void updateMapErrorAbsentSubscribers() {
        statCollector.mapErrorAbsentSubscribers.addAndGet(1);
    }

    public long getMapErrorCallBarred() {
        return statCollector.mapErrorCallBarred.get();
    }

    public void updateMapErrorCallBarred() {
        statCollector.mapErrorCallBarred.addAndGet(1);
    }

    public long getMapErrorTeleserviceNotProvisioned() {
        return statCollector.mapErrorTeleserviceNotProvisioned.get();
    }

    public void updateMapErrorTeleserviceNotProvisioned() {
        statCollector.mapErrorTeleserviceNotProvisioned.addAndGet(1);
    }

    public long getMapErrorUnknownSubscriber() {
        return statCollector.mapErrorUnknownSubscriber.get();
    }

    public void updateMapErrorUnknownSubscriber() {
        statCollector.mapErrorUnknownSubscriber.addAndGet(1);
    }

    public long getMapErrorUssdBusy() {
        return statCollector.mapErrorUssdBusy.get();
    }

    public void updateMapErrorUssdBusy() {
        statCollector.mapErrorUssdBusy.addAndGet(1);
    }

    public long getMapErrorComponentOther() {
        return statCollector.mapErrorComponentOther.get();
    }

    public void updateMapErrorComponentOther() {
        statCollector.mapErrorComponentOther.addAndGet(1);
    }

    public long getMapDialogTimeouts() {
        return statCollector.mapDialogTimeouts.get();
    }

    public void updateMapDialogTimeouts() {
        statCollector.mapDialogTimeouts.addAndGet(1);
    }

    public long getMapInvokeTimeouts() {
        return statCollector.mapInvokeTimeouts.get();
    }

    public void updateMapInvokeTimeouts() {
        statCollector.mapInvokeTimeouts.addAndGet(1);
    }

    public long getAppTimeouts() {
        return statCollector.appTimeouts.get();
    }

    public void updateAppTimeouts() {
        statCollector.appTimeouts.addAndGet(1);
    }

    public Map<String, LongValue> getRequestsPerUssdCode(String compainName) {
        StatResult res = statCollector.statDataCollection.restartAndGet(REQUESTS_PER_USSD_CODE, compainName);
//        statCollector.statDataCollection.updateData(REQUESTS_PER_USSD_CODE, provider.getCurrentDialogsCount());
        if (res != null)
            return res.getStringLongValue();
        else
            return null;
    }

    public void updateRequestsPerUssdCode(String name) {
        statCollector.statDataCollection.updateData(REQUESTS_PER_USSD_CODE, name);
    }

    private class StatCollector {
        private StatDataCollection statDataCollection = new StatDataCollectionImpl();

        private AtomicLong dialogsCnt = new AtomicLong();

        private AtomicLong dialogsAllEstablished = new AtomicLong();
        private AtomicLong dialogsAllFailed = new AtomicLong();
        private AtomicLong dialogsPullEstablished = new AtomicLong();
        private AtomicLong dialogsPullFailed = new AtomicLong();
        private AtomicLong dialogsPushEstablished = new AtomicLong();
        private AtomicLong dialogsPushFailed = new AtomicLong();
        private AtomicLong dialogsHttpEstablished = new AtomicLong();
        private AtomicLong dialogsHttpFailed = new AtomicLong();
        private AtomicLong dialogsSipEstablished = new AtomicLong();
        private AtomicLong dialogsSipFailed = new AtomicLong();

        private AtomicLong messagesRecieved = new AtomicLong();
        private AtomicLong messagesSent = new AtomicLong();
        private AtomicLong messagesAll = new AtomicLong();

        private AtomicLong processUssdRequestOperations = new AtomicLong();
        private AtomicLong ussdRequestOperations = new AtomicLong();
        private AtomicLong ussdNotifyOperations = new AtomicLong();
        private AtomicLong ussdPullNoRoutingRule = new AtomicLong();
        private AtomicLong mapErrorAbsentSubscribers = new AtomicLong();
        private AtomicLong mapErrorCallBarred = new AtomicLong();
        private AtomicLong mapErrorTeleserviceNotProvisioned = new AtomicLong();
        private AtomicLong mapErrorUnknownSubscriber = new AtomicLong();
        private AtomicLong mapErrorUssdBusy = new AtomicLong();
        private AtomicLong mapErrorComponentOther = new AtomicLong();
        private AtomicLong mapDialogTimeouts = new AtomicLong();
        private AtomicLong mapInvokeTimeouts = new AtomicLong();
        private AtomicLong appTimeouts = new AtomicLong();

        public StatCollector() {
            this.statDataCollection.registerStatCounterCollector(MIN_DIALOGS_IN_PROCESS, StatDataCollectorType.MIN);
            this.statDataCollection.registerStatCounterCollector(MAX_DIALOGS_IN_PROCESS, StatDataCollectorType.MAX);

            this.statDataCollection.registerStatCounterCollector(REQUESTS_PER_USSD_CODE, StatDataCollectorType.StringLongMap);
        }
    }

}

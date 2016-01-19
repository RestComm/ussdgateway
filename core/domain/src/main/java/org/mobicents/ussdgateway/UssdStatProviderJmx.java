/*
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

package org.mobicents.ussdgateway;

import java.util.Date;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.log4j.Logger;
import org.mobicents.protocols.ss7.statistics.api.LongValue;

import com.telscale.protocols.ss7.oam.common.jmx.MBeanHost;
import com.telscale.protocols.ss7.oam.common.jmx.MBeanType;
import com.telscale.protocols.ss7.oam.common.jmxss7.Ss7Layer;
import com.telscale.protocols.ss7.oam.common.statistics.ComplexValueImpl;
import com.telscale.protocols.ss7.oam.common.statistics.CounterDefImpl;
import com.telscale.protocols.ss7.oam.common.statistics.CounterDefSetImpl;
import com.telscale.protocols.ss7.oam.common.statistics.SourceValueCounterImpl;
import com.telscale.protocols.ss7.oam.common.statistics.SourceValueObjectImpl;
import com.telscale.protocols.ss7.oam.common.statistics.SourceValueSetImpl;
import com.telscale.protocols.ss7.oam.common.statistics.api.ComplexValue;
import com.telscale.protocols.ss7.oam.common.statistics.api.CounterDef;
import com.telscale.protocols.ss7.oam.common.statistics.api.CounterDefSet;
import com.telscale.protocols.ss7.oam.common.statistics.api.CounterMediator;
import com.telscale.protocols.ss7.oam.common.statistics.api.CounterType;
import com.telscale.protocols.ss7.oam.common.statistics.api.SourceValueSet;

/**
*
* @author sergey vetyutnev
*
*/
public class UssdStatProviderJmx implements UssdStatProviderJmxMBean, CounterMediator {

    protected final Logger logger;

    private final MBeanHost ss7Management;
    private final UssdStatAggregator ussdStatAggregator = UssdStatAggregator.getInstance();

    private FastMap<String, CounterDefSet> lstCounters = new FastMap<String, CounterDefSet>();

    public UssdStatProviderJmx(MBeanHost ss7Management) {
        this.ss7Management = ss7Management;

        this.logger = Logger.getLogger(UssdStatProviderJmx.class.getCanonicalName() + "-" + getName());
    }

    /**
     * methods - bean life-cycle
     */

    public void start() throws Exception {
        logger.info("Starting ...");

        setupCounterList();

        this.ss7Management.registerMBean(Ss7Layer.USSD_GW, UssdManagementType.MANAGEMENT, this.getName(), this);

        logger.info("Started ...");
    }

    public void stop() {
        logger.info("Stopping ...");
        logger.info("Stopped ...");
    }

    public String getName() {
        return "USSD";
    }

    private void setupCounterList() {
        FastMap<String, CounterDefSet> lst = new FastMap<String, CounterDefSet>();

        CounterDefSetImpl cds = new CounterDefSetImpl(this.getCounterMediatorName() + "-Main");
        lst.put(cds.getName(), cds);

        CounterDef cd = new CounterDefImpl(CounterType.Minimal, "MinDialogsInProcess", "A min count of dialogs that are in progress during a period");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Maximal, "MaxDialogsInProcess", "A max count of dialogs that are in progress during a period");
        cds.addCounterDef(cd);

        cd = new CounterDefImpl(CounterType.Summary, "DialogsAllEstablished", "Dialogs successfully established all");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsAllFailed", "Dialogs failed at establishing or established phases all");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsPullEstablished", "Dialogs successfully established - pull case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsPullFailed", "Dialogs failed at establishing or established phases - pull case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsPushEstablished", "Dialogs successfully established - push case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsPushFailed", "Dialogs failed at establishing or established phases - push case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsHttpEstablished", "Dialogs successfully established - Http case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsHttpFailed", "Dialogs failed at establishing or established phases - Http case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsSipEstablished", "Dialogs successfully established - Sip case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "DialogsSipFailed", "Dialogs failed at establishing or established phases - Sip case");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "DialogsAllEstablishedCumulative", "Dialogs successfully established all cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "DialogsAllFailedCumulative", "Dialogs failed at establishing or established phases all cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Average, "DialogsAllEstablishedPerSec", "Dialogs successfully established all per second");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Average, "DialogsAllFailedPerSec", "Dialogs failed at establishing or established phases all per second");
        cds.addCounterDef(cd);

        cd = new CounterDefImpl(CounterType.Summary, "ProcessUssdRequestOperations", "ProcessUssdRequest operations count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "ProcessUssdRequestOperationsCumulative", "ProcessUssdRequest operations count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "UssdRequestOperations", "UssdRequest operations count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdRequestOperationsCumulative", "UssdRequest operations count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "UssdNotifyOperations", "UssdNotify operations count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdNotifyOperationsCumulative", "UssdNotify operations count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "UssdPullNoRoutingRule", "Ussd pull requests with no configured routing rule count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "UssdPullNoRoutingRuleCumulative", "Ussd pull requests with no configured routing rule count cumulative");
        cds.addCounterDef(cd);

        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorAbsentSubscribers", "AbsentSubscribers MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorAbsentSubscribersCumulative", "AbsentSubscribers MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorCallBarred", "CallBarred MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorCallBarredCumulative", "CallBarred MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorTeleserviceNotProvisioned", "TeleserviceNotProvisioned MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorTeleserviceNotProvisionedCumulative", "TeleserviceNotProvisioned MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorUnknownSubscriber", "UnknownSubscriber MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorUnknownSubscriberCumulative", "UnknownSubscriber MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorUssdBusy", "UssdBusy MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorUssdBusyCumulative", "UssdBusy MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapErrorComponentOther", "ComponentOther MAP errors count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapErrorComponentOtherCumulative", "ComponentOther MAP errors count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapDialogTimeouts", "MAP DialogTimeouts count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapDialogTimeoutsCumulative", "MAP DialogTimeouts count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "MapInvokeTimeouts", "MAP InvokeTimeouts count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "MapInvokeTimeoutsCumulative", "MAP InvokeTimeouts count cumulative");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary, "AppTimeouts", "Application Timeouts count");
        cds.addCounterDef(cd);
        cd = new CounterDefImpl(CounterType.Summary_Cumulative, "AppTimeoutsCumulative", "Application Timeouts count cumulative");
        cds.addCounterDef(cd);

        cd = new CounterDefImpl(CounterType.ComplexValue, "RequestsPerUssdCode", "USSD PULL requests count per USSD code");
        cds.addCounterDef(cd);

        lstCounters = lst;
    }

    @Override
    public CounterDefSet getCounterDefSet(String counterDefSetName) {
        return lstCounters.get(counterDefSetName);
    }

    @Override
    public String[] getCounterDefSetList() {
        String[] res = new String[lstCounters.size()];
        lstCounters.keySet().toArray(res);
        return res;
    }

    @Override
    public String getCounterMediatorName() {
        return "USSD GW-" + this.getName();
    }

    @Override
    public SourceValueSet getSourceValueSet(String counterDefSetName, String campaignName, int durationInSeconds) {

        if (durationInSeconds >= 60)
            logger.info("getSourceValueSet() - starting - campaignName=" + campaignName);
        else
            logger.debug("getSourceValueSet() - starting - campaignName=" + campaignName);

        long curTimeSeconds = new Date().getTime() / 1000;

        SourceValueSetImpl svs;
        try {
            String[] csl = this.getCounterDefSetList();
            if (!csl[0].equals(counterDefSetName))
                return null;

            svs = new SourceValueSetImpl(ussdStatAggregator.getSessionId());

            CounterDefSet cds = getCounterDefSet(counterDefSetName);
            for (CounterDef cd : cds.getCounterDefs()) {
                SourceValueCounterImpl scs = new SourceValueCounterImpl(cd);

                SourceValueObjectImpl svo = null;
                if (cd.getCounterName().equals("MinDialogsInProcess")) {
                    Long res = ussdStatAggregator.getMinDialogsInProcess(campaignName);
                    if (res != null)
                        svo = new SourceValueObjectImpl(this.getName(), res);
                } else if (cd.getCounterName().equals("MaxDialogsInProcess")) {
                    Long res = ussdStatAggregator.getMaxDialogsInProcess(campaignName);
                    if (res != null)
                        svo = new SourceValueObjectImpl(this.getName(), res);

                } else if (cd.getCounterName().equals("DialogsAllEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsAllEstablished());
                } else if (cd.getCounterName().equals("DialogsAllFailed")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsAllFailed());
                } else if (cd.getCounterName().equals("DialogsPullEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsPullEstablished());
                } else if (cd.getCounterName().equals("DialogsPullFailed")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsPullFailed());
                } else if (cd.getCounterName().equals("DialogsPushEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsPushEstablished());
                } else if (cd.getCounterName().equals("DialogsPushFailed")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsPushFailed());
                } else if (cd.getCounterName().equals("DialogsHttpEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsHttpEstablished());
                } else if (cd.getCounterName().equals("DialogsHttpFailed")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsHttpFailed());
                } else if (cd.getCounterName().equals("DialogsSipEstablished")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsSipEstablished());
                } else if (cd.getCounterName().equals("DialogsSipFailed")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsSipFailed());
                } else if (cd.getCounterName().equals("DialogsAllEstablishedCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsAllEstablishedCumulative());
                } else if (cd.getCounterName().equals("DialogsAllFailedCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getDialogsAllFailedCumulative());

                } else if (cd.getCounterName().equals("DialogsAllEstablishedPerSec")) {
                    long cnt = ussdStatAggregator.getDialogsAllEstablished();
                    svo = new SourceValueObjectImpl(this.getName(), 0);
                    svo.setValueA(cnt);
                    svo.setValueB(curTimeSeconds);
                } else if (cd.getCounterName().equals("DialogsAllFailedPerSec")) {
                    long cnt = ussdStatAggregator.getDialogsAllFailed();
                    svo = new SourceValueObjectImpl(this.getName(), 0);
                    svo.setValueA(cnt);
                    svo.setValueB(curTimeSeconds);


                } else if (cd.getCounterName().equals("ProcessUssdRequestOperations")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getProcessUssdRequestOperations());
                } else if (cd.getCounterName().equals("ProcessUssdRequestOperationsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getProcessUssdRequestOperations());
                } else if (cd.getCounterName().equals("UssdRequestOperations")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdRequestOperations());
                } else if (cd.getCounterName().equals("UssdRequestOperationsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdRequestOperations());
                } else if (cd.getCounterName().equals("UssdNotifyOperations")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdNotifyOperations());
                } else if (cd.getCounterName().equals("UssdNotifyOperationsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdNotifyOperations());
                } else if (cd.getCounterName().equals("UssdPullNoRoutingRule")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdPullNoRoutingRule());
                } else if (cd.getCounterName().equals("UssdPullNoRoutingRuleCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getUssdPullNoRoutingRule());

                } else if (cd.getCounterName().equals("MapErrorAbsentSubscribers")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorAbsentSubscribers());
                } else if (cd.getCounterName().equals("MapErrorAbsentSubscribersCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorAbsentSubscribers());
                } else if (cd.getCounterName().equals("MapErrorCallBarred")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorCallBarred());
                } else if (cd.getCounterName().equals("MapErrorCallBarredCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorCallBarred());
                } else if (cd.getCounterName().equals("MapErrorTeleserviceNotProvisioned")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorTeleserviceNotProvisioned());
                } else if (cd.getCounterName().equals("MapErrorTeleserviceNotProvisionedCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorTeleserviceNotProvisioned());
                } else if (cd.getCounterName().equals("MapErrorUnknownSubscriber")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorUnknownSubscriber());
                } else if (cd.getCounterName().equals("MapErrorUnknownSubscriberCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorUnknownSubscriber());
                } else if (cd.getCounterName().equals("MapErrorUssdBusy")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorUssdBusy());
                } else if (cd.getCounterName().equals("MapErrorUssdBusyCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorUssdBusy());
                } else if (cd.getCounterName().equals("MapErrorComponentOther")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorComponentOther());
                } else if (cd.getCounterName().equals("MapErrorComponentOtherCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapErrorComponentOther());
                } else if (cd.getCounterName().equals("MapDialogTimeouts")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapDialogTimeouts());
                } else if (cd.getCounterName().equals("MapDialogTimeoutsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapDialogTimeouts());
                } else if (cd.getCounterName().equals("MapInvokeTimeouts")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapInvokeTimeouts());
                } else if (cd.getCounterName().equals("MapInvokeTimeoutsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getMapInvokeTimeouts());
                } else if (cd.getCounterName().equals("AppTimeouts")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getAppTimeouts());
                } else if (cd.getCounterName().equals("AppTimeoutsCumulative")) {
                    svo = new SourceValueObjectImpl(this.getName(), ussdStatAggregator.getAppTimeouts());

                } else if (cd.getCounterName().equals("RequestsPerUssdCode")) {
                    svo = createComplexValue(ussdStatAggregator.getRequestsPerUssdCode(campaignName));

                }

                if (svo != null)
                    scs.addObject(svo);

                svs.addCounter(scs);
            }
        } catch (Throwable e) {
            logger.info("Exception when getSourceValueSet() - campaignName=" + campaignName + " - " + e.getMessage(), e);
            return null;
        }

        if (durationInSeconds >= 60)
            logger.info("getSourceValueSet() - return value - campaignName=" + campaignName);
        else
            logger.debug("getSourceValueSet() - return value - campaignName=" + campaignName);

        return svs;
    }

    private SourceValueObjectImpl createComplexValue(Map<String, LongValue> vv) {
        SourceValueObjectImpl svo = null;
        if (vv != null) {
            svo = new SourceValueObjectImpl(this.getName(), 0);
            ComplexValue[] vvv = new ComplexValue[vv.size()];
            int i1 = 0;
            for (String s : vv.keySet()) {
                LongValue lv = vv.get(s);
                vvv[i1++] = new ComplexValueImpl(s, lv.getValue());
            }
            svo.setComplexValue(vvv);
        }
        return svo;
    }


    public enum UssdManagementType implements MBeanType {
        MANAGEMENT("Management");

        private final String name;

        public static final String NAME_MANAGEMENT = "Management";

        private UssdManagementType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static UssdManagementType getInstance(String name) {
            if (NAME_MANAGEMENT.equals(name)) {
                return MANAGEMENT;
            }

            return null;
        }
    }

}


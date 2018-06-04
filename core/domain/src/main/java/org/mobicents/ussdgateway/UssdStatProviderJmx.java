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

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.applications.ussd.bootstrap.Version;
import org.restcomm.commons.statistics.reporter.RestcommStatsReporter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

/**
*
* @author sergey vetyutnev
*
*/
public class UssdStatProviderJmx implements UssdStatProviderJmxMBean {

    protected final Logger logger;

    private final UssdStatAggregator ussdStatAggregator = UssdStatAggregator.getInstance();

    protected static final String DEFAULT_STATISTICS_SERVER = "https://statistics.restcomm.com/rest/";

    private RestcommStatsReporter statsReporter = RestcommStatsReporter.getRestcommStatsReporter();
    private MetricRegistry metrics = RestcommStatsReporter.getMetricRegistry();
    private Counter counterDialogs = metrics.counter("ussd_dialogs");
    private Counter counterMessages = metrics.counter("messages");

    public UssdStatProviderJmx() {
        this.logger = Logger.getLogger(UssdStatProviderJmx.class.getCanonicalName() + "-" + getName());
    }

    /**
     * methods - bean life-cycle
     */

    public void start() throws Exception {
        logger.info("UssdStatProviderJmx Starting ...");

        String statisticsServer = Version.instance.getStatisticsServer();
        if (statisticsServer == null || !statisticsServer.contains("http")) {
            statisticsServer = DEFAULT_STATISTICS_SERVER;
        }
        // define remote server address (optionally)
        statsReporter.setRemoteServer(statisticsServer);

        String projectName = System.getProperty("RestcommProjectName", Version.instance.getShortName());
        String projectType = System.getProperty("RestcommProjectType", Version.instance.getProjectType());
        String projectVersion = System.getProperty("RestcommProjectVersion", Version.instance.getProjectVersion());
        logger.info("Restcomm Stats starting: " + projectName + " " + projectType + " " + projectVersion + " "
                + statisticsServer);
        statsReporter.setProjectName(projectName);
        statsReporter.setProjectType(projectType);
        statsReporter.setVersion(projectVersion);
        statsReporter.start(86400, TimeUnit.SECONDS);

        ussdStatAggregator.setCounterDialogs(counterDialogs);
        ussdStatAggregator.setCounterMessages(counterMessages);

        logger.info("UssdStatProviderJmx Started ...");
    }

    public void stop() {
        logger.info("UssdStatProviderJmx Stopping ...");

        statsReporter.stop();

        logger.info("UssdStatProviderJmx Stopped ...");
    }

    public String getName() {
        return "USSD";
    }


    public enum UssdManagementType {
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


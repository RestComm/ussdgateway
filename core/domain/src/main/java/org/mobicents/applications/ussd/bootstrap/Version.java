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

package org.mobicents.applications.ussd.bootstrap;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Version class reads the version.properties packaged with for
 * run time display of Version
 *
 * @author amit.bhayani
 * 
 */
public final class Version {

	/**
	 * The single instance.
	 */
	public final static Version instance = new Version();

	/**
	 * The version properties.
	 */
	private Properties props;

	/**
	 * Do not allow direct public construction.
	 */
	private Version() {
		props = loadProperties();
	}

	/**
	 * Returns an unmodifiable map of version properties.
	 * 
	 * @return
	 */
	public Map getProperties() {
		return Collections.unmodifiableMap(props);
	}

	/**
	 * Returns the value for the given property name.
	 * 
	 * @param name -
	 *            The name of the property.
	 * @return The property value or null if the property is not set.
	 */
	public String getProperty(final String name) {
		return props.getProperty(name);
	}

    public String getProjectName() {
        String version = instance.getProperty("name");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getProjectType() {
        String version = instance.getProperty("project.type");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getProjectVersion() {
        String version = instance.getProperty("version");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getStatisticsServer() {
        String version = instance.getProperty("statistics.server");
        if (version != null) {
            return version;
        } else {
            return "https://statistics.restcomm.com/rest/";
        }
    }

    public String getShortName() {
        String version = instance.getProperty("short.name");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

	/**
	 * Returns the version information as a string.
	 * 
	 * @return Basic information as a string.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("Mobicents USSD Gateway Server: ");
		boolean first = true;
		for (Object key : props.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(key).append('=').append(props.get(key));
		}
		return sb.toString();
	}

	/**
	 * Load the version properties from a resource.
	 */
	private Properties loadProperties() {

		props = new Properties();

		try {
			InputStream in = Version.class
					.getResourceAsStream("version.properties");
			props.load(in);
			in.close();
		} catch (Exception e) {
			throw new Error("Missing version.properties");
		}

		return props;
	}

}

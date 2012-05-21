package org.mobicents.ussdgateway.rules;

import java.io.Serializable;

/**
 * Acts as Fact for Rules
 * 
 * @author amit bhayani
 * 
 */
public class Call implements Serializable {
	// Initial string, its like #123*
	private String ussdString;

	private boolean isHttp;
	private boolean isSmpp;

	// to be used with other protocols
	private String genericUrl;

	public Call(String ussdString) {
		this.ussdString = ussdString;
	}

	public String getUssdString() {
		return ussdString;
	}

	public boolean isHttp() {
		return isHttp;
	}

	public void setHttp(boolean isHttp) {
		this.isHttp = isHttp;
	}

	public boolean isSmpp() {
		return isSmpp;
	}

	public void setSmpp(boolean isSmpp) {
		this.isSmpp = isSmpp;
	}

	/**
	 * @return the genericUrl
	 */
	public String getGenericUrl() {
		return genericUrl;
	}

	/**
	 * @param genericUrl
	 *            the genericUrl to set
	 */
	public void setGenericUrl(String genericUrl) {
		this.genericUrl = genericUrl;
	}

	@Override
	public String toString() {
		return "Call [ussdString=" + ussdString + ", isHttp=" + isHttp + ", isSmpp=" + isSmpp + ", genericUrl="
				+ genericUrl + "]";
	}
	
}

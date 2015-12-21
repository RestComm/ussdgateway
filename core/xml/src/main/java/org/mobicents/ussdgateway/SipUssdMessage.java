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

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSDataCodingGroup;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.mobicents.protocols.ss7.map.api.datacoding.CBSNationalLanguage;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorCode;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.USSDString;
import org.mobicents.protocols.ss7.map.api.smstpdu.CharacterSet;
import org.mobicents.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;

/**
 * 
 * @author sergey vetyutnev
 * 
 */
public class SipUssdMessage implements XMLSerializable {
	public static final String USSD_DATA = "ussd-data";
	public static final String LANGUAGE = "language";
	public static final String USSD_STRING = "ussd-string";
	public static final String ERROR_CODE = "error-code";
	public static final String ANY_EXT = "anyExt";

	private String language;
	private String ussdString;
	private int errorCode;
	private AnyExt anyExt;

	public SipUssdMessage() {
	}

	public SipUssdMessage(String ussdString, String language) {
		this.ussdString = ussdString;
		this.language = language;
	}

	public SipUssdMessage(int errorCode) {
		this.errorCode = errorCode;
	}

	public SipUssdMessage(CBSDataCodingScheme dataCodingScheme, USSDString sourceUSSDString) throws MAPException {
		this.ussdString = sourceUSSDString.getString(null);

		if (dataCodingScheme.getNationalLanguageShiftTable() == null
				|| dataCodingScheme.getNationalLanguageShiftTable() == CBSNationalLanguage.LanguageUnspecified) {
			if (dataCodingScheme.getCharacterSet() == CharacterSet.GSM7)
				this.language = "en";
		} else {
			switch (dataCodingScheme.getNationalLanguageShiftTable()) {
			case German:
				this.language = "de";
				break;
			case English:
				this.language = "en";
				break;
			case Italian:
				this.language = "it";
				break;
			case French:
				this.language = "fr";
				break;
			case Spanish:
				this.language = "es";
				break;
			case Dutch:
				this.language = "nl";
				break;
			case Swedish:
				this.language = "sv";
				break;
			case Danish:
				this.language = "da";
				break;
			case Portuguese:
				this.language = "pt";
				break;
			case Finnish:
				this.language = "fi";
				break;
			case Norwegian:
				this.language = "nb";
				break;
			case Greek:
				this.language = "el";
				break;
			case Turkish:
				this.language = "tr";
				break;
			case Hungarian:
				this.language = "hu";
				break;
			case Polish:
				this.language = "pl";
				break;
			case Czech:
				this.language = "cs";
				break;
			case Hebrew:
				this.language = "he";
				break;
			case Arabic:
				this.language = "ar";
				break;
			case Russian:
				this.language = "ru";
				break;
			case Icelandic:
				this.language = "is";
				break;
			}
		}
	}

	public SipUssdMessage(SipUssdErrorCode sipUssdErrorCode) {
		if (sipUssdErrorCode != null)
			this.errorCode = sipUssdErrorCode.getCode();
	}

	public String getLanguage() {
		return language;
	}

	public String getUssdString() {
		return ussdString;
	}

	public int getErrorCodeNumeric() {
		return errorCode;
	}

	public SipUssdErrorCode getErrorCode() {
		return SipUssdErrorCode.getSipUssdErrorCode(errorCode);
	}

	public AnyExt getAnyExt() {
		return anyExt;
	}

	public void setAnyExt(AnyExt anyExt) {
		this.anyExt = anyExt;
	}

	public boolean isSuccessMessage() {
		if (this.errorCode == 0 && this.ussdString != null)
			return true;
		else
			return false;
	}

	public boolean isErrorMessage() {
		return !this.isSuccessMessage();
	}

	public static CBSDataCodingScheme getCBSDataCodingSchemeForLanguage(String language) {
		CBSDataCodingScheme res = null;

		if (language == null) {
			res = new CBSDataCodingSchemeImpl(CBSDataCodingGroup.GeneralGsm7, CharacterSet.GSM7, null, null, false);
		} else {
			if (language.equals("en") || language.equals("de")) {
				res = new CBSDataCodingSchemeImpl(CBSDataCodingGroup.GeneralGsm7, CharacterSet.GSM7, null, null, false);
			} else {
				res = new CBSDataCodingSchemeImpl(CBSDataCodingGroup.GeneralDataCodingIndication, CharacterSet.UCS2,
						null, null, false);
			}
		}

		return res;
	}

	public CBSDataCodingScheme getCBSDataCodingScheme() {
		if (!this.isSuccessMessage())
			return null;

		return getCBSDataCodingSchemeForLanguage(this.language);
	}

	public USSDString getUSSDString(MAPProvider mapProvider) throws MAPException {
		if (!this.isSuccessMessage())
			return null;

		USSDString res = mapProvider.getMAPParameterFactory().createUSSDString(this.ussdString,
				getCBSDataCodingScheme(), null);
		return res;
	}

	public MAPErrorMessage getMAPErrorMessage(MAPProvider mapProvider) {
		if (this.isSuccessMessage())
			return null;

		MAPErrorMessage res;
		switch (this.getErrorCode()) {
		case languageAlphabitNotSupported:
			res = mapProvider.getMAPErrorMessageFactory().createMAPErrorMessageExtensionContainer(
					(long) MAPErrorCode.unknownAlphabet, null);
			break;
		case unexpectedDataValue:
			res = mapProvider.getMAPErrorMessageFactory().createMAPErrorMessageExtensionContainer(
					(long) MAPErrorCode.unexpectedDataValue, null);
			break;
		default:
			res = mapProvider.getMAPErrorMessageFactory().createMAPErrorMessageSystemFailure(2, null, null, null);
			break;
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("SipUssdMessage [");

		if (this.ussdString != null) {
			sb.append("ussdString=");
			sb.append(this.ussdString);
			sb.append(",");
		}
		if (this.language != null) {
			sb.append("language=");
			sb.append(this.language);
			sb.append(",");
		}
		if (this.errorCode != 0) {
			sb.append("errorCode=");
			SipUssdErrorCode ec = SipUssdErrorCode.getSipUssdErrorCode(this.errorCode);
			if (ec != null)
				sb.append(ec);
			else
				sb.append(this.errorCode);
			sb.append(",");
		}

		sb.append("]");

		return sb.toString();
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<SipUssdMessage> SIP_USSD_MESSAGE_XML = new XMLFormat<SipUssdMessage>(
			SipUssdMessage.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml, SipUssdMessage ussdMessage)
				throws XMLStreamException {
			ussdMessage.language = xml.get(LANGUAGE, String.class);
			ussdMessage.ussdString = xml.get(USSD_STRING, String.class);
			Integer valI = xml.get(ERROR_CODE, Integer.class);
			if (valI != null)
				ussdMessage.errorCode = valI;

			ussdMessage.anyExt = xml.get(SipUssdMessage.ANY_EXT, AnyExt.class);
		}

		@Override
		public void write(SipUssdMessage ussdMessage, javolution.xml.XMLFormat.OutputElement xml)
				throws XMLStreamException {
			if (ussdMessage.language != null)
				xml.add(ussdMessage.language, LANGUAGE, String.class);
			if (ussdMessage.ussdString != null)
				xml.add(ussdMessage.ussdString, USSD_STRING, String.class);
			if (ussdMessage.errorCode != 0)
				xml.add(ussdMessage.errorCode, ERROR_CODE, Integer.class);
			if (ussdMessage.anyExt != null) {
				xml.add(ussdMessage.anyExt, SipUssdMessage.ANY_EXT, AnyExt.class);
			}
		}
	};

}

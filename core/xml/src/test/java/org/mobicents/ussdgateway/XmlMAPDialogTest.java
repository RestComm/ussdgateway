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

import static org.testng.Assert.*;
import javolution.xml.stream.XMLStreamException;

import org.restcomm.protocols.ss7.indicator.NatureOfAddress;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.datacoding.CBSDataCodingScheme;
import org.restcomm.protocols.ss7.map.api.dialog.MAPAbortProviderReason;
import org.restcomm.protocols.ss7.map.api.dialog.MAPRefuseReason;
import org.restcomm.protocols.ss7.map.api.errors.AbsentSubscriberDiagnosticSM;
import org.restcomm.protocols.ss7.map.api.errors.AbsentSubscriberReason;
import org.restcomm.protocols.ss7.map.api.errors.CallBarringCause;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorCode;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageAbsentSubscriber;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageAbsentSubscriberSM;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageBusySubscriber;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageCallBarred;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessageExtensionContainer;
import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.AlertingCategory;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.primitives.USSDString;
import org.restcomm.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyRequest;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSNotifyResponse;
import org.restcomm.protocols.ss7.map.api.service.supplementary.UnstructuredSSResponse;
import org.restcomm.protocols.ss7.map.datacoding.CBSDataCodingSchemeImpl;
import org.restcomm.protocols.ss7.map.dialog.MAPUserAbortChoiceImpl;
import org.restcomm.protocols.ss7.map.errors.MAPErrorMessageAbsentSubscriberImpl;
import org.restcomm.protocols.ss7.map.errors.MAPErrorMessageAbsentSubscriberSMImpl;
import org.restcomm.protocols.ss7.map.errors.MAPErrorMessageBusySubscriberImpl;
import org.restcomm.protocols.ss7.map.errors.MAPErrorMessageCallBarredImpl;
import org.restcomm.protocols.ss7.map.errors.MAPErrorMessageExtensionContainerImpl;
import org.restcomm.protocols.ss7.map.primitives.AddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.AlertingPatternImpl;
import org.restcomm.protocols.ss7.map.primitives.ISDNAddressStringImpl;
import org.restcomm.protocols.ss7.map.primitives.USSDStringImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.ProcessUnstructuredSSResponseImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSNotifyResponseImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSRequestImpl;
import org.restcomm.protocols.ss7.map.service.supplementary.UnstructuredSSResponseImpl;
import org.restcomm.protocols.ss7.sccp.impl.parameter.DefaultEncodingScheme;
import org.restcomm.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl;
import org.restcomm.protocols.ss7.sccp.impl.parameter.SccpAddressImpl;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.protocols.ss7.tcap.api.MessageType;
import org.restcomm.protocols.ss7.tcap.asn.ProblemImpl;
import org.restcomm.protocols.ss7.tcap.asn.comp.InvokeProblemType;
import org.restcomm.protocols.ss7.tcap.asn.comp.Problem;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class XmlMAPDialogTest {

	private EventsSerializeFactory factory = null;

	@BeforeClass
	public void setUpClass() throws Exception {
		factory = new EventsSerializeFactory();
	}

	@AfterClass
	public void tearDownClass() throws Exception {
	}

	@BeforeMethod
	public void setUp() {
	}

	@AfterMethod
	public void tearDown() {
	}

	@Test
	public void testProcessUnstructuredSSRequestSerialization() throws XMLStreamException, MAPException {
//		GlobalTitle gt = GlobalTitle.getInstance(0, org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, NatureOfAddress.INTERNATIONAL, "79023700299");
        GlobalTitle gt = new GlobalTitle0100Impl("79023700299", 0, DefaultEncodingScheme.INSTANCE,
                org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, NatureOfAddress.INTERNATIONAL);
        // final String digits,final int translationType, final EncodingScheme encodingScheme,final NumberingPlan numberingPlan, final NatureOfAddress natureOfAddress

		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, 146);
		
//		GlobalTitle gt1 = GlobalTitle.getInstance(0, org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, NatureOfAddress.INTERNATIONAL, "79023700111");
        GlobalTitle gt1 = new GlobalTitle0100Impl("79023700111", 0, DefaultEncodingScheme.INSTANCE,
                org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, NatureOfAddress.INTERNATIONAL);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt1, 0, 146);

		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		ISDNAddressStringImpl isdnAddress = new ISDNAddressStringImpl(AddressNature.international_number,
				NumberingPlan.ISDN, "79273605819");
		AlertingPatternImpl alertingPattern = new AlertingPatternImpl(AlertingCategory.Category3);
		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		USSDString ussdStr = new USSDStringImpl("*234#", cbsDataCodingScheme, null);
		ProcessUnstructuredSSRequestImpl processUnstructuredSSRequestIndication = new ProcessUnstructuredSSRequestImpl(
				cbsDataCodingScheme, ussdStr, alertingPattern, isdnAddress);

		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		XmlMAPDialog original = new XmlMAPDialog(appCtx, orgAddress, dstAddress, 12l, 13l, destReference, origReference);
		original.setTCAPMessageType(MessageType.Begin);
		original.setUserObject("123456789");
		original.addMAPMessage(processUnstructuredSSRequestIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

		ProcessUnstructuredSSRequestImpl copyUSSR = (ProcessUnstructuredSSRequestImpl) copy.getMAPMessages().get(0);
		ProcessUnstructuredSSRequestImpl originalUSSR = (ProcessUnstructuredSSRequestImpl) original.getMAPMessages()
				.get(0);
		assertEquals(copyUSSR.getUSSDString().getString(null), originalUSSR.getUSSDString().getString(null));

	}

	@Test
	public void testProcessUnstructuredSSResponseSerialization() throws XMLStreamException, MAPException {

		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		USSDString ussdStr = new USSDStringImpl("Thank You!", cbsDataCodingScheme, null);
		ProcessUnstructuredSSResponseImpl processUnstructuredSSResponseIndication = new ProcessUnstructuredSSResponseImpl(
				cbsDataCodingScheme, ussdStr);

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setTCAPMessageType(MessageType.Continue);
		original.addMAPMessage(processUnstructuredSSResponseIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

		ProcessUnstructuredSSResponse copyUSSR = (ProcessUnstructuredSSResponse) copy.getMAPMessages().get(0);
		ProcessUnstructuredSSResponse originalUSSR = (ProcessUnstructuredSSResponse) original.getMAPMessages().get(0);
		assertEquals(copyUSSR.getUSSDString().getString(null), originalUSSR.getUSSDString().getString(null));

	}

	@Test
	public void testUnstructuredSSRequestSerialization() throws XMLStreamException, MAPException {

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);

		USSDString ussdStr = new USSDStringImpl("USSD String : Hello World\n 1. Balance\n 2. Texts Remaining",
				cbsDataCodingScheme, null);
		UnstructuredSSRequestImpl unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(cbsDataCodingScheme,
				ussdStr, null, null);

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.addMAPMessage(unstructuredSSRequestIndication);
		original.setTCAPMessageType(MessageType.Continue);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

	}

	@Test
	public void testUnstructuredSSResponseSerialization() throws XMLStreamException, MAPException {

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		USSDString ussdStr = new USSDStringImpl("1", cbsDataCodingScheme, null);
		UnstructuredSSResponseImpl unstructuredSSResponseIndication = new UnstructuredSSResponseImpl(
				cbsDataCodingScheme, ussdStr);

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.addMAPMessage(unstructuredSSResponseIndication);
		original.setTCAPMessageType(MessageType.Continue);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());

		UnstructuredSSResponse copyUSSR = (UnstructuredSSResponse) copy.getMAPMessages().get(0);
		UnstructuredSSResponse originalUSSR = (UnstructuredSSResponse) original.getMAPMessages().get(0);
		assertEquals(copyUSSR.getUSSDString(), originalUSSR.getUSSDString());

	}

	@Test
	public void testUnstructuredSSResponseSerializationWithNullUSSD() throws XMLStreamException, MAPException {

		UnstructuredSSResponseImpl unstructuredSSResponseIndication = new UnstructuredSSResponseImpl();

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.addMAPMessage(unstructuredSSResponseIndication);
		original.setTCAPMessageType(MessageType.Continue);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

		UnstructuredSSResponse copyUSSR = (UnstructuredSSResponse) copy.getMAPMessages().get(0);
		assertNull(copyUSSR.getUSSDString());

	}

    @Test
    public void testUnstructuredSSRequestSerialization_Cyrillic() throws XMLStreamException, MAPException {

        CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(72);

        USSDString ussdStr = new USSDStringImpl("������� 1 ��� ������ ��� 2 ��� ������",
                cbsDataCodingScheme, null);
        UnstructuredSSRequestImpl unstructuredSSRequestIndication = new UnstructuredSSRequestImpl(cbsDataCodingScheme,
                ussdStr, null, null);

        XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
        original.addMAPMessage(unstructuredSSRequestIndication);
        original.setTCAPMessageType(MessageType.Continue);

        byte[] serializedEvent = this.factory.serialize(original);
        System.out.println(new String(serializedEvent));

        XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

        assertNotNull(copy);

        assertEquals(copy.getApplicationContext(), original.getApplicationContext());
        assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
        assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
        assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

    }

	@Test
	public void testUnstructuredSSNotifyRequestIndicationSerialization() throws XMLStreamException, MAPException {

		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		USSDString ussdStr = new USSDStringImpl(
				"Your new balance is 34.38 AFN and expires on 30.07.2012. Cost of last event was 0.50 AFN.",
				cbsDataCodingScheme, null);
		UnstructuredSSNotifyRequestImpl unstructuredSSNotifyRequestIndication = new UnstructuredSSNotifyRequestImpl(
				cbsDataCodingScheme, ussdStr, null, null);

		XmlMAPDialog original = new XmlMAPDialog(appCtx, orgAddress, dstAddress, 12l, 13l, destReference, origReference);
		original.addMAPMessage(unstructuredSSNotifyRequestIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

		UnstructuredSSNotifyRequest copyUSSR = (UnstructuredSSNotifyRequest) copy.getMAPMessages().get(0);
		UnstructuredSSNotifyRequest originalUSSR = (UnstructuredSSNotifyRequest) original.getMAPMessages().get(0);
		assertEquals(copyUSSR.getUSSDString().getString(null), originalUSSR.getUSSDString().getString(null));

	}
	
	@Test
	public void testUnstructuredSSNotifyResponseIndicationSerialization() throws XMLStreamException, MAPException {

		MAPApplicationContext appCtx = MAPApplicationContext.getInstance(
				MAPApplicationContextName.networkUnstructuredSsContext, MAPApplicationContextVersion.version2);

		SccpAddress orgAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 1, 8);
		SccpAddress dstAddress = new SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_DPC_AND_SSN, null, 2, 8);

		CBSDataCodingScheme cbsDataCodingScheme = new CBSDataCodingSchemeImpl(15);
		AddressString destReference = new AddressStringImpl(AddressNature.international_number,
				NumberingPlan.land_mobile, "204208300008002");
		AddressString origReference = new AddressStringImpl(AddressNature.international_number, NumberingPlan.ISDN,
				"31628968300");

		
		UnstructuredSSNotifyResponseImpl unstructuredSSNotifyResponseIndication = new UnstructuredSSNotifyResponseImpl();

		XmlMAPDialog original = new XmlMAPDialog(appCtx, orgAddress, dstAddress, 12l, 13l, destReference, origReference);
		original.addMAPMessage(unstructuredSSNotifyResponseIndication);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getApplicationContext(), original.getApplicationContext());
		assertEquals(copy.getLocalDialogId(), original.getLocalDialogId());
		assertEquals(copy.getProcessInvokeWithoutAnswerIds().size(), original.getProcessInvokeWithoutAnswerIds().size());
		assertEquals(copy.getMAPMessages().size(), original.getMAPMessages().size());

		UnstructuredSSNotifyResponse copyUSSR = (UnstructuredSSNotifyResponse) copy.getMAPMessages().get(0);
		UnstructuredSSNotifyResponse originalUSSR = (UnstructuredSSNotifyResponse) original.getMAPMessages().get(0);
		assertEquals(copyUSSR.getInvokeId(), originalUSSR.getInvokeId());

	}

	@Test
	public void testDialogMAPUserAbortChoice() throws XMLStreamException, MAPException {
		MAPUserAbortChoiceImpl abort = new MAPUserAbortChoiceImpl();
		abort.setUserSpecificReason();

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.abort(abort);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getMAPUserAbortChoice().isUserSpecificReason(), original.getMAPUserAbortChoice()
				.isUserSpecificReason());
	}

	@Test
	public void testDialogMAPAbortProviderReason() throws XMLStreamException, MAPException {

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setMapAbortProviderReason(MAPAbortProviderReason.SupportingDialogueTransactionReleased);

		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getMapAbortProviderReason(), original.getMapAbortProviderReason());
	}

    @Test
    public void testDialogMAPRefuseReason() throws XMLStreamException, MAPException {

        XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
        original.setMapRefuseReason(MAPRefuseReason.ApplicationContextNotSupported);

        byte[] serializedEvent = this.factory.serialize(original);
        System.out.println(new String(serializedEvent));

        XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

        assertNotNull(copy);

        assertEquals(copy.getMapRefuseReason(), original.getMapRefuseReason());
    }

    @Test
    public void testDialogErrorReject() throws XMLStreamException, MAPException {

        XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);

        long invokeId = 5;
        MAPErrorMessage errorMessage = new MAPErrorMessageAbsentSubscriberSMImpl(AbsentSubscriberDiagnosticSM.RoamingRestriction, null, null);
        original.sendErrorComponent(invokeId, errorMessage);
        long invokeId2 = 6;
        errorMessage = new MAPErrorMessageAbsentSubscriberImpl(null, AbsentSubscriberReason.purgedMS);
        original.sendErrorComponent(invokeId2, errorMessage);
        long invokeId3 = 7;
        errorMessage = new MAPErrorMessageBusySubscriberImpl(null, true, false);
        original.sendErrorComponent(invokeId3, errorMessage);
        long invokeId4 = 8;
        errorMessage = new MAPErrorMessageCallBarredImpl(3, CallBarringCause.operatorBarring, null, null);
        original.sendErrorComponent(invokeId4, errorMessage);
        long invokeId5 = 9;
        errorMessage = new MAPErrorMessageExtensionContainerImpl((Long) (long) MAPErrorCode.ussdBusy, null);
        original.sendErrorComponent(invokeId5, errorMessage);

        long invokeIdProb = 2;
        Problem problem = new ProblemImpl();
        problem.setInvokeProblemType(InvokeProblemType.DuplicateInvokeID);
        original.sendRejectComponent(invokeIdProb, problem);
        original.setSriPart(true);

        byte[] serializedEvent = this.factory.serialize(original);
        System.out.println(new String(serializedEvent));


        XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

        assertNotNull(copy);

        MAPErrorMessage errorMessageCopy = copy.getErrorComponents().get(invokeId);
        assertEquals(AbsentSubscriberDiagnosticSM.RoamingRestriction, ((MAPErrorMessageAbsentSubscriberSM) errorMessageCopy).getAbsentSubscriberDiagnosticSM());
        errorMessageCopy = copy.getErrorComponents().get(invokeId2);
        assertEquals(AbsentSubscriberReason.purgedMS, ((MAPErrorMessageAbsentSubscriber) errorMessageCopy).getAbsentSubscriberReason());
        errorMessageCopy = copy.getErrorComponents().get(invokeId3);
        assertTrue(((MAPErrorMessageBusySubscriber) errorMessageCopy).getCcbsPossible());
        assertFalse(((MAPErrorMessageBusySubscriber) errorMessageCopy).getCcbsBusy());
        errorMessageCopy = copy.getErrorComponents().get(invokeId4);
        assertEquals(CallBarringCause.operatorBarring, ((MAPErrorMessageCallBarred) errorMessageCopy).getCallBarringCause());
        assertEquals(3, ((MAPErrorMessageCallBarred) errorMessageCopy).getMapProtocolVersion());
        errorMessageCopy = copy.getErrorComponents().get(invokeId5);
        assertEquals(MAPErrorCode.ussdBusy, (long)(((MAPErrorMessageExtensionContainer) errorMessageCopy).getErrorCode()));

        Problem problemCopy = copy.getRejectComponents().get(invokeIdProb);
        assertEquals(problemCopy.getInvokeProblemType(), problem.getInvokeProblemType());
        assertEquals(copy.getSriPart(), original.getSriPart());
    }

	@Test
	public void testDialogTimedOut() throws XMLStreamException, MAPException {

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setDialogTimedOut(true);
		
		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getDialogTimedOut(), original.getDialogTimedOut());
	}	
	
	@Test
	public void testInvokeTimedOut() throws XMLStreamException, MAPException {

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setInvokeTimedOut(1l);
		
		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getInvokeTimedOut(), original.getInvokeTimedOut());
	}
	
	@Test
	public void testEmptyDialogHandshake() throws XMLStreamException, MAPException {

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setEmptyDialogHandshake(true);
		
		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertEquals(copy.getEmptyDialogHandshake(), original.getEmptyDialogHandshake());
	}
	
	
	@Test
	public void testReturnMessageOnError() throws XMLStreamException, MAPException {

		XmlMAPDialog original = new XmlMAPDialog(null, null, null, 12l, 13l, null, null);
		original.setReturnMessageOnError(true);
		
		byte[] serializedEvent = this.factory.serialize(original);
		System.out.println(new String(serializedEvent));

		XmlMAPDialog copy = this.factory.deserialize(serializedEvent);

		assertNotNull(copy);

		assertTrue(copy.getReturnMessageOnError());
	}	
	
	@Test
	public void testDeserializeUnstructuredSSRequest() throws Exception {
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><dialog appCntx=\"networkUnstructuredSsContext_version2\" localId=\"36\" mapMessagesSize=\"1\" prearrangedEnd=\"false\" redirectRequest=\"false\" remoteId=\"37\" returnMessageOnError=\"false\" type=\"End\"><localAddress pc=\"1\" ssn=\"8\"><ai value=\"67\" /></localAddress><remoteAddress pc=\"2\" ssn=\"8\"><ai value=\"67\" /></remoteAddress><destinationReference nai=\"international_number\" npi=\"land_mobile\" number=\"92333000789\" /><originationReference nai=\"international_number\" npi=\"land_mobile\" number=\"92300123456\" /><processUnstructuredSSRequest_Response dataCodingScheme=\"15\" invokeId=\"2\" string=\"We will shortly get back to you.\" /></dialog>";
		XmlMAPDialog copy = this.factory.deserialize(s.getBytes());
		
		System.out.println(" ******* SERILAIZED "+copy);
	}
    
//    @Test
//    public void testDeserialize2() throws Exception {
//        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
//                + "<dialog type=\"Begin\" mapMessagesSize=\"1\" redirectRequest=\"true\">\n"
//                + "<localAddress pc=\"0\" ssn=\"146\">\n"
//                + "<ai value=\"18\"/>\n"
//                + "<gt type=\"org.restcomm.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl\" tt=\"0\" es=\"0\" np=\"1\" nai=\"4\" digits=\"222222222\"/>\n"
//                + "</localAddress>\n"
//                + "<remoteAddress pc=\"0\" ssn=\"146\">\n"
//                + "<ai value=\"18\"/>\n"
//                + "<gt type=\"org.restcomm.protocols.ss7.sccp.impl.parameter.GlobalTitle0100Impl\" tt=\"0\" es=\"0\" np=\"1\" nai=\"4\" digits=\"3333333333\"/>\n"
//                + "</remoteAddress>\n"
//                + "<destinationReference number=\"33333300000000\" nai=\"international_number\" npi=\"land_mobile\"/>\n"
//                + "<originationReference number=\"22222000000\" nai=\"international_number\" npi=\"ISDN\"/>\n"
//                + "<unstructuredSSNotify_Request dataCodingScheme=\"15\" string=\"Your new balance is 34.38 AFN and expires on 30.07.2012. Cost of last event was 0.50 AFN.\">\n"
//                + "<msisdn nai=\"international_number\" npi=\"ISDN\" number=\"79273605819\"/>\n" + "</unstructuredSSNotify_Request>\n" + "</dialog>\n";
//        XmlMAPDialog copy = this.factory.deserialize(s.getBytes());
//        
//        System.out.println(" ******* SERILAIZED "+copy);
//    }

}

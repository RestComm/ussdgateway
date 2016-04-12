package org.mobicents.ussdgateway;

import static org.testng.Assert.assertEquals;

import java.nio.charset.Charset;

import org.testng.annotations.Test;

public class XmlNewLineTest {

    @Test
    public void testProcessUnstructuredSSRequestSerialization() throws Exception {
        Charset charset = Charset.forName("UTF-8");
        String menuText = "Menu:&#10;aaaa&#xa;bbbb";
        String menuDecodedText = "Menu:\naaaa\nbbbb";
        String s1 = "<ussd-data> <language value=\"ru\"/> <ussd-string value=\"" + menuText + "\"/> <anyExt> "
                + " <message-type>processUnstructuredSSRequest_Response</message-type> </anyExt> </ussd-data>";
        byte[] buf = s1.getBytes(charset);

        EventsSerializeFactory fact = new EventsSerializeFactory();
        SipUssdMessage message = fact.deserializeSipUssdMessage(buf);

        assertEquals(message.getUssdString(), menuDecodedText);
    }

}

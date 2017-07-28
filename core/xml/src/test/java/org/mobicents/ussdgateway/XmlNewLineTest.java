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

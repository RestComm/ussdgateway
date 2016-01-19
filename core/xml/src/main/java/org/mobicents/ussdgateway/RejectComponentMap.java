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

package org.mobicents.ussdgateway;

import java.util.Map;

import org.mobicents.protocols.ss7.tcap.asn.ProblemImpl;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * @author sergey vetyutnev
 *
 */
public class RejectComponentMap {

    private static final String INVOKE_ID = "invokeId";
    private static final String REJECT_COMPONENT = "rejectComponent";

    private FastMap<Long, Problem> data = new FastMap<Long, Problem>();

    public void put(Long invokeId, Problem problem) {
        this.data.put(invokeId, problem);
    }

    public void clear() {
        this.data.clear();
    }

    public int size() {
        return this.data.size();
    }

    public Map<Long, Problem> getRejectComponents() {
        return data.unmodifiable();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RejectComponentMap=[");

        int i1 = 0;
        for (Entry<Long, Problem> n = data.head(), end = data.tail(); (n = n.getNext()) != end;) {
            Long id = n.getKey();
            Problem problem = n.getValue();

            if (i1 == 0)
                i1 = 1;
            else
                sb.append(", ");
            sb.append("invokeId=");
            sb.append(id);
            sb.append(", problem=");
            sb.append(problem);
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * XML Serialization/Deserialization
     */
    protected static final XMLFormat<RejectComponentMap> REJECT_COMPONENT_MAP_XML = new XMLFormat<RejectComponentMap>(RejectComponentMap.class) {

        @Override
        public void read(javolution.xml.XMLFormat.InputElement xml, RejectComponentMap data) throws XMLStreamException {
            data.data.clear();

            while (xml.hasNext()) {
                Long id = xml.get(INVOKE_ID, Long.class);
                ProblemImpl problem = xml.get(REJECT_COMPONENT, ProblemImpl.class);
                data.data.put(id, problem);
            }
        }

        @Override
        public void write(RejectComponentMap data, javolution.xml.XMLFormat.OutputElement xml) throws XMLStreamException {
            if (data.data.size() > 0) {
                for (Entry<Long, Problem> n = data.data.head(), end = data.data.tail(); (n = n.getNext()) != end;) {
                    Long id = n.getKey();
                    ProblemImpl problem = (ProblemImpl) n.getValue();

                    xml.add(id, INVOKE_ID, Long.class);
                    xml.add(problem, REJECT_COMPONENT, ProblemImpl.class);
                }
            }
        }
    };

}

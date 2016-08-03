/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.demetra.xml.regression;

import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.regression.GenericTradingDaysVariables;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlGenericTradingDays.RNAME)
@XmlType(name = XmlGenericTradingDays.NAME)
public class XmlGenericTradingDays extends XmlModifiableRegressionVariable {

    static final String RNAME = "GenericTradingDays", NAME = RNAME + "Type";

    @XmlElement
    @XmlList
    public int[] DayClustering;

    @XmlAttribute
    public boolean contrasts;

    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlGenericTradingDays, GenericTradingDaysVariables> {

        @Override
        public Class<XmlGenericTradingDays> getXmlType() {
            return XmlGenericTradingDays.class;
        }

        @Override
        public Class<GenericTradingDaysVariables> getValueType() {
            return GenericTradingDaysVariables.class;
        }

        @Override
        public GenericTradingDaysVariables unmarshal(XmlGenericTradingDays v) throws Exception {
            ec.tstoolkit.timeseries.DayClustering clustering = ec.tstoolkit.timeseries.DayClustering.create(v.DayClustering);
            
            GenericTradingDays gtd;
            if (v.contrasts)
                gtd= GenericTradingDays.contrasts(clustering);
            else
                gtd=GenericTradingDays.normalized(clustering);
            return new GenericTradingDaysVariables(gtd);
        }

        @Override
        public XmlGenericTradingDays marshal(GenericTradingDaysVariables v) throws Exception {
            XmlGenericTradingDays xml = new XmlGenericTradingDays();
            xml.contrasts=v.getCore().getContrastGroup()>=0;
            xml.DayClustering=v.getCore().getClustering().getGroupsDefinition();
            return xml;
        }

    }
}

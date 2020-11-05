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
package demetra.toolkit.io.xml.legacy.regression;

import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
//@XmlRootElement(name = XmlGenericTradingDays.RNAME)
@XmlType(name = XmlGenericTradingDays.NAME)
public class XmlGenericTradingDays extends XmlModifiableRegressionVariable {

    static final String RNAME = "GenericTradingDays", NAME = RNAME + "Type";

    @XmlElement(name="DayClustering")
    @XmlList
    public int[] dayClustering;

    @XmlAttribute
    public boolean contrasts;

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlGenericTradingDays, GenericTradingDaysVariable> {

        @Override
        public Class<XmlGenericTradingDays> getXmlType() {
            return XmlGenericTradingDays.class;
        }

        @Override
        public Class<GenericTradingDaysVariable> getImplementationType() {
            return GenericTradingDaysVariable.class;
        }

        @Override
        public GenericTradingDaysVariable unmarshal(XmlGenericTradingDays v) throws Exception {
            DayClustering clustering = DayClustering.of(v.dayClustering);
            
            GenericTradingDays gtd;
            if (v.contrasts)
                gtd= GenericTradingDays.contrasts(clustering);
            else
                gtd=GenericTradingDays.normalized(clustering);
            return new GenericTradingDaysVariable(gtd);
        }

        @Override
        public XmlGenericTradingDays marshal(GenericTradingDaysVariable v) throws Exception {
            XmlGenericTradingDays xml = new XmlGenericTradingDays();
            xml.contrasts=v.isContrast();
            xml.dayClustering=v.getClustering().getGroupsDefinition();
            return xml;
        }

    }
}

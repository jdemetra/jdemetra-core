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
package ec.tss.xml.regression;

import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlGenericTradingDays.NAME)
@XmlType(name=XmlGenericTradingDays.NAME)
public class XmlGenericTradingDays extends XmlVariable{
    
    static final String RNAME="genericTradingDays", NAME=RNAME+"Type";

    static class Adapter implements ITsVariableAdapter<XmlGenericTradingDays, GregorianCalendarVariables> {

        @Override
        public Class<XmlGenericTradingDays> getXmlType() {
            return XmlGenericTradingDays.class;
        }

        @Override
        public Class<GregorianCalendarVariables> getValueType() {
            return GregorianCalendarVariables.class;
        }

        @Override
        public GregorianCalendarVariables decode(XmlGenericTradingDays v) throws Exception {
            return GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        }

        @Override
        public XmlGenericTradingDays encode(GregorianCalendarVariables v) throws Exception {
            return new XmlGenericTradingDays();
        }


    }
}

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

import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.ChangeOfRegime;
import ec.tstoolkit.timeseries.regression.ChangeOfRegimeType;
import ec.tstoolkit.timeseries.regression.TsVariableWindow;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlChangeOfRegime.NAME)
@XmlType(name = XmlChangeOfRegime.NAME)
public class XmlChangeOfRegime extends XmlTsModifier {

    static final String RNAME = "ChangeOfRegime", NAME = RNAME + "Type";

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day start;

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    public Day end;

    @ServiceProvider(service = ITsVariableAdapter.class)
    public static class Adapter implements ITsVariableAdapter<XmlChangeOfRegime, ChangeOfRegime> {

        @Override
        public Class<ChangeOfRegime> getValueType() {
            return ChangeOfRegime.class;
        }

        @Override
        public Class<XmlChangeOfRegime> getXmlType() {
            return XmlChangeOfRegime.class;
        }

        @Override
        public ChangeOfRegime decode(XmlChangeOfRegime v) throws Exception {
            ChangeOfRegimeType type;
            Day day;
            if (v.start != null) {
                type = ChangeOfRegimeType.ZeroStarted;
                day = v.start;
            } else {
                type = ChangeOfRegimeType.ZeroEnded;
                day = v.end;
            }
            return new ChangeOfRegime(TsVariableAdapters.getDefault().decode(v.core), type, day);
        }

        @Override
        public XmlChangeOfRegime encode(ChangeOfRegime v) throws Exception {
            XmlChangeOfRegime xml = new XmlChangeOfRegime();
            if (v.getRegime() == ChangeOfRegimeType.ZeroStarted) {
                xml.start = v.getDay();
            } else {
                xml.end = v.getDay();
            }
            xml.core = TsVariableAdapters.getDefault().encode(v.getVariable());
            return xml;
        }
    }
}

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

import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlAdditiveOutlier.RNAME)
@XmlType(name = XmlAdditiveOutlier.NAME)

public class XmlAdditiveOutlier extends XmlOutlier {

    static final String RNAME = "AdditiveOutlier", NAME = RNAME + "Type";

    @ServiceProvider(service = TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter<XmlAdditiveOutlier, AdditiveOutlier> {

        @Override
        public Class<AdditiveOutlier> getValueType() {
            return AdditiveOutlier.class;
        }

        @Override
        public Class<XmlAdditiveOutlier> getXmlType() {
            return XmlAdditiveOutlier.class;
        }

         @Override
        public AdditiveOutlier unmarshal(XmlAdditiveOutlier v) throws Exception {
            AdditiveOutlier o = new AdditiveOutlier(v.Position);
            return o;
        }

        @Override
        public XmlAdditiveOutlier marshal(AdditiveOutlier v) throws Exception {
            XmlAdditiveOutlier xml = new XmlAdditiveOutlier();
            xml.Position = v.getPosition();
            return xml;
        }

    }
}

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

import ec.tss.xml.XmlDayAdapter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.OutliersFactory;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlOutlier.RNAME)
@XmlType(name=XmlOutlier.NAME)
public class XmlOutlier extends XmlVariable {
    
    static final String RNAME="outlier", NAME=RNAME+"Type";

    static class Adapter implements ITsVariableAdapter<XmlOutlier, IOutlierVariable> {

        @Override
        public Class<IOutlierVariable> getValueType() {
            return IOutlierVariable.class;
        }

        @Override
        public Class<XmlOutlier> getXmlType() {
            return XmlOutlier.class;
        }

        @Override
        public IOutlierVariable decode(XmlOutlier v) throws Exception {
            OutlierDefinition odef = new OutlierDefinition(v.position, v.type, v.prespecified);
            return OutliersFactory.defaultFactory.make(odef, TsFrequency.Monthly);
        }

        @Override
        public XmlOutlier encode(IOutlierVariable v) throws Exception {
            XmlOutlier xml = new XmlOutlier();
            xml.position = v.getPosition().firstday();
            xml.type=v.getOutlierType();
            xml.prespecified=v.isPrespecified();
            return xml;
        }
    }

    @XmlElement
    @XmlJavaTypeAdapter(XmlDayAdapter.class)
    Day position;

    @XmlElement
    OutlierType type;
    
    @XmlAttribute
    boolean prespecified;

}

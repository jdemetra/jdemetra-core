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

import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.ITsVariable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AdditiveOutlierType")
public class XmlAdditiveOutlier extends XmlOutlier {

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public AdditiveOutlier unmarshal(XmlRegressionVariable x) {
            if (x instanceof XmlAdditiveOutlier) {
                XmlAdditiveOutlier xvar = (XmlAdditiveOutlier) x;
                AdditiveOutlier o = new AdditiveOutlier(xvar.position.atStartOfDay());
                return o;
            } else {
                return null;
            }
        }

        @Override
        public XmlAdditiveOutlier marshal(ITsVariable var) {
            if (var instanceof AdditiveOutlier) {
                AdditiveOutlier v = (AdditiveOutlier) var;
                XmlAdditiveOutlier xml = new XmlAdditiveOutlier();
                xml.position = v.getPosition().toLocalDate();
                return xml;
            } else {
                return null;
            }
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlAdditiveOutlier.class);
        }
    }
    
    public static final Adapter ADAPTER=new Adapter();
}

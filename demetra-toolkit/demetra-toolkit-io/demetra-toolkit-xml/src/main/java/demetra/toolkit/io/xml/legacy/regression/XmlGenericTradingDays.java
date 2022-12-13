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
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.ModifiedTsVariable;
import java.util.List;
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

    @XmlElement(name = "DayClustering")
    @XmlList
    public int[] dayClustering;

    @XmlAttribute
    public boolean contrasts;

    @ServiceProvider(TsVariableAdapter.class)
    public static class Adapter extends TsVariableAdapter {

        @Override
        public ITsVariable unmarshal(XmlRegressionVariable xvar) throws Exception {
            if (xvar instanceof XmlGenericTradingDays) {
                XmlGenericTradingDays v = (XmlGenericTradingDays) xvar;
                DayClustering clustering = DayClustering.of(v.dayClustering);

                GenericTradingDays gtd;
                if (v.contrasts) {
                    gtd = GenericTradingDays.contrasts(clustering);
                } else {
                    gtd = GenericTradingDays.raw(clustering);
                }
                GenericTradingDaysVariable td = new GenericTradingDaysVariable(gtd);
                if (v.getModifiersCount() == 0) {
                    return td;
                } else {
                    List<ModifiedTsVariable.Modifier> mds = TsModifierAdapters.unmarshal(v.getModifiers());
                    return ModifiedTsVariable.builder()
                            .variable(td)
                            .modifiers(mds)
                            .build();
                }
            } else {
                return null;
            }
        }

        @Override
        public XmlGenericTradingDays marshal(ITsVariable v) throws Exception {
            GenericTradingDaysVariable td = null;
            ModifiedTsVariable mv = null;
            if (v instanceof GenericTradingDaysVariable) {
                td = (GenericTradingDaysVariable) v;
                mv = null;
            } else if (v instanceof ModifiedTsVariable) {
                mv = (ModifiedTsVariable) v;
                if (mv.getVariable() instanceof GenericTradingDaysVariable) {
                    td = (GenericTradingDaysVariable) mv.getVariable();
                }
            }
            if (td == null)
                return null;

            XmlGenericTradingDays xml = new XmlGenericTradingDays();
            xml.contrasts = td.getVariableType()== GenericTradingDays.Type.CONTRAST;
            xml.dayClustering = td.getClustering().getGroupsDefinition();
            if (mv != null){
                List<XmlRegressionVariableModifier> xm = TsModifierAdapters.marshal(mv.getModifiers());
                xml.getModifiers().addAll(xm);
            }
            return xml;
        }

        @Override
        public void xmlClasses(List<Class> lclass) {
            lclass.add(XmlGenericTradingDays.class);
        }

    }
}

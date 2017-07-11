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

import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.Arrays2;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Jean Palate
 */
@XmlRootElement(name = XmlVariables.RNAME)
@XmlType(name = XmlVariables.NAME)
public class XmlVariables implements IXmlConverter<TsVariables> {

    static final String RNAME = "Variables", NAME = RNAME + "vType";
    @XmlElement(name = "Variable")
    private final List<XmlRegressionVariable> vars = new ArrayList<>();

    @Override
    public TsVariables create() {
        TsVariables nvars = new TsVariables();
        if (!vars.isEmpty()) {
            TsVariableAdapters adapters = TsVariableAdapters.getDefault();
            for (XmlRegressionVariable var : vars) {
                ITsVariable v = adapters.unmarshal(var);
                
             }
        }
        return nvars;
    }

    @Override
    public void copy(TsVariables t) {
        String[] n = t.getNames();
        if (Arrays2.isNullOrEmpty(n)) {
            return;
        }
        TsVariableAdapters adapters = TsVariableAdapters.getDefault();
        for (int i = 0; i < n.length; ++i) {
            ITsVariable v = t.get(n[i]);
            XmlRegressionVariable xv = adapters.marshal(v);
            vars.add(xv);
        }
    }

    /**
     * @return the vars
     */
    public List<XmlRegressionVariable> getVars() {
        return vars;
    }
}

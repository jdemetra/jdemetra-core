/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tstoolkit.timeseries.regression;

import data.Data;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class ITsModifierTest {

    public ITsModifierTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void demoTramoExpander() {
        TsData x = Data.X;
        TsDomain xdom = x.getDomain().extend(24, 60);

        TsVariableList vars = new TsVariableList();
        vars.add(new SeasonalDummies(TsFrequency.Monthly));
        vars.add(new TramoExpander(x, TramoSpecification.TRfull));
        // Series extended with 24 backcasts and 60 forecasts,
        // mixed with other variables
        Matrix matrix = vars.all().matrix(xdom);
        //System.out.println(matrix);
    }
}

class TramoExpander implements ITsModifier {

    private final ITsVariable var;
    private final PreprocessingModel model;

    static TsData of(ITsVariable v) {
        TsDomain dom = v.getDefinitionDomain();
        if (dom == null) {
            return null;
        }
        if (v.getDim() > 1) {
            return null;
        }
        DataBlock data = new DataBlock(dom.getLength());
        v.data(dom, Collections.singletonList(data), 0);
        return new TsData(dom.getStart(), data);
    }

    TramoExpander(ITsVariable var, TramoSpecification spec) {
        TsData s = of(var);
        if (s == null) {
            throw new IllegalArgumentException();
        }
        this.var = var;
        model = spec.build().process(s, new ModellingContext());
    }

    TramoExpander(TsData s, TramoSpecification spec) {
        var = new TsVariable(s);
        model = spec.build().process(s, new ModellingContext());
    }

    @Override
    public ITsVariable getVariable() {
        return var;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsData s = of(var);
        TsDomain vdom = s.getDomain();
        int nb = vdom.getStart().minus(domain.getStart());
        int nf = domain.getEnd().minus(vdom.getEnd());

        TsData b = null, f = null;
        if (nb > 0) {
            b = model.backcast(nb, false);
        }
        if (nf > 0) {
            f = model.forecast(nf, false);
        }
        s = s.fittoDomain(domain);
        if (b != null) {
            s = s.update(b);
        }
        if (f != null) {
            s = s.update(f);
        }
        data.get(0).copy(s);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return var.getDefinitionFrequency();
    }

    @Override
    public String getDescription() {
        return var.getDescription();
    }

    @Override
    public int getDim() {
        return 1;
    }

    @Override
    public String getItemDescription(int idx) {
        return var.getItemDescription(idx);
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getFrequency() == var.getDefinitionFrequency();
    }

}

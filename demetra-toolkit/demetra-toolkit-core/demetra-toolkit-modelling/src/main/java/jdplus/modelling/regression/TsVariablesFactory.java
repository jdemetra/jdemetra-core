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
package jdplus.modelling.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.TsVariables;
import jdplus.math.matrices.FastMatrix;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
class TsVariablesFactory implements RegressionVariableFactory<TsVariables> {

    static TsVariablesFactory FACTORY = new TsVariablesFactory();

    private TsVariablesFactory() {
    }

    @Override
    public boolean fill(TsVariables var, TsPeriod start, FastMatrix buffer) {
        int nvars = var.dim();
        for (int i = 0; i < nvars; ++i) {
            TsData v = var.getData(i);
            TsDomain curdom = v.getDomain();
            // position of the first data (in m_ts)
            int istart = curdom.getStartPeriod().until(start);
            // position of the last data (excluded)
            int n = buffer.getRowsCount();
            int iend = istart + n;

            // indexes in data //in buffer
            int jstart = 0, jend = n;
            // not enough data at the beginning
            if (istart < 0) {
                jstart = -istart;
                istart = 0;
            }
            // not enough data at the end
            //          if (iend > n) {
            if (iend > v.getValues().length()) {
                // iend = v.getValues().length();
                jend = v.getValues().length() - istart;
                iend = v.getValues().length();
            }
            buffer.column(i).range(jstart, jend).copy(v.getValues().range(istart, iend));
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>> boolean fill(TsVariables var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

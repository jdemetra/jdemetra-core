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

import demetra.timeseries.regression.PeriodicContrasts;
import jdplus.data.DataBlock;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesPeriod;

/**
 * The periodic contrasts are defined as follows:
 *
 * The contrasting period is by design the last period of the year. The
 * regression variables generated that way are linearly independent.
 *
 * @author Jean Palate
 */
public class PeriodicContrastsFactory implements RegressionVariableFactory<PeriodicContrasts> {

    public static Matrix matrix(PeriodicContrasts var, int length, int start) {
        int period = var.getPeriod();
        Matrix M = Matrix.make(length, period - 1);
        int lstart = period - start - 1;
        if (lstart < 0) {
            lstart += period;
        }
        for (int i = 0; i < period - 1; i++) {
            DataBlock x = M.column(i);
            int jstart = i - start;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
            DataBlock q = x.extract(lstart, -1, period);
            q.set(-1);
        }
        return M;
    }

    static PeriodicContrastsFactory FACTORY=new PeriodicContrastsFactory();

    private PeriodicContrastsFactory(){}

    @Override
    public boolean fill(PeriodicContrasts var, TsPeriod start, Matrix buffer) {
        int period = var.getPeriod();
        TsPeriod refPeriod = start.withDate(var.getReference());
        long del = start.getId() - refPeriod.getId();
        int pstart = (int) del % period;
        int lstart = period - pstart - 1;
        if (lstart < 0) {
            lstart += period;
        }
        for (int i = 0; i < period - 1; i++) {
            DataBlock x = buffer.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
            DataBlock q = x.extract(lstart, -1, period);
            q.set(-1);
        }
        return true;
    }

    @Override
    public <P extends TimeSeriesPeriod, D extends TimeSeriesDomain<P>>  boolean fill(PeriodicContrasts var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

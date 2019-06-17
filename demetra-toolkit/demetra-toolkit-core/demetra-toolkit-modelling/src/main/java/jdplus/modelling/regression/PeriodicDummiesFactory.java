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

import demetra.modelling.regression.PeriodicDummies;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.maths.matrices.FastMatrix;

/**
 * The periodic contrasts are defined as follows:
 *
 * The contrasting period is by design the last period of the year. The
 * regression variables generated that way are linearly independent.
 *
 * @author Jean Palate
 */
public class PeriodicDummiesFactory implements RegressionVariableFactory<PeriodicDummies> {

    public static CanonicalMatrix matrix(PeriodicDummies var, int length, int start) {
        int period = var.getPeriod();
        CanonicalMatrix m = CanonicalMatrix.make(length, period);
        int pstart = start % period;
        for (int i = 0; i < period; i++) {
            DataBlock x = m.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            x.extract(jstart, -1, period).set(1);
        }
        return m;
    }

    static PeriodicDummiesFactory FACTORY=new PeriodicDummiesFactory();

    private PeriodicDummiesFactory(){}

    @Override
    public boolean fill(PeriodicDummies var, TsPeriod start, FastMatrix buffer) {
        int period = var.getPeriod();
        TsPeriod refPeriod = start.withDate(var.getReference());
        long del = start.getId() - refPeriod.getId();
        int pstart = (int) del % period;
        for (int i = 0; i < period; i++) {
            DataBlock x = buffer.column(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += period;
            }
            DataBlock m = x.extract(jstart, -1, period);
            m.set(1);
        }
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(PeriodicDummies var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

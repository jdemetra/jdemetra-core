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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DiffConstant extends AbstractSingleTsVariable {

    /**
     *
     * @param ur
     * @param n
     * @return
     */
    public static double[] generateMeanEffect(BackFilter ur, int n) {
        Polynomial p = ur.getPolynomial();
        double[] m = new double[n];
        for (int i = ur.getDegree(); i < n; ++i) {
            double c = 1;
            for (int j = 1; j <= p.getDegree(); ++j) {
                if (p.get(j) != 0) {
                    c -= p.get(j) * m[i - j];
                }
            }
            m[i] = c;
        }
        return m;
    }
    private Day m_start;
    private BackFilter m_ur;

    /**
     *
     * @param ur
     * @param start
     */
    public DiffConstant(BackFilter ur, Day start) {
        m_start = start;
        m_ur = ur;
    }

    /**
     *
     * @param start
     * @param data
     */
    @Override
    public void data(TsPeriod start, DataBlock data) {
        TsPeriod s = new TsPeriod(start.getFrequency(), m_start);
        int del = start.minus(s);
        // raw implementation
        if (del < 0) {
            throw new TsException("Unexpected DConstant");
        }
        double[] g = generateMeanEffect(m_ur, del + data.getLength());
        data.copyFrom(g, del);
    }

    @Override
    public String getDescription(TsFrequency context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Polynomial trend (").append(m_ur.getDegree()).append(
                ')');
        return builder.toString();
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getLength() > m_ur.getDegree();
    }

    @Override
    public String getName() {
        return "trend";
    }

}

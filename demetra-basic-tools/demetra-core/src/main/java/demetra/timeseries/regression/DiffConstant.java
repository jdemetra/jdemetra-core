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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.data.Range;
import demetra.design.Development;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.Polynomial;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.util.List;
import demetra.timeseries.TimeSeriesDomain;


///**
// *
// * @author Jean Palate
// * @param <P>
// */
//@Development(status = Development.Status.Alpha)
//public class DiffConstant<D extends TsDomain<?>> implements ITsVariable<D> {
//
//    /**
//     *
//     * @param ur
//     * @param n
//     * @return
//     */
//    public static double[] generateMeanEffect(BackFilter ur, int n) {
//        Polynomial p = ur.asPolynomial();
//        double[] m = new double[n];
//        for (int i = p.getDegree(); i < n; ++i) {
//            double c = 1;
//            for (int j = 1; j <= p.getDegree(); ++j) {
//                if (p.get(j) != 0) {
//                    c -= p.get(j) * m[i - j];
//                }
//            }
//            m[i] = c;
//        }
//        return m;
//    }
//    private final LocalDateTime start;
//    private final BackFilter ur;
//
//    /**
//     *
//     * @param ur
//     * @param start
//     */
//    public DiffConstant(BackFilter ur, LocalDateTime start) {
//        this.start = start;
//        this.ur = ur;
//    }
//
//    /**
//     *
//     * @param start
//     * @param data
//     */
//    @Override
//    public void data(D domain, List<DataBlock> data) {
//        Range<LocalDateTime> s=domain.get(0);
//        TsPeriod s = new TsPeriod(start.getFrequency(), this.start);
//        int del = start.minus(s);
//        // raw implementation
//        if (del < 0) {
//            throw new TsException("Unexpected DConstant");
//        }
//        double[] g = generateMeanEffect(ur, del + data.getLength());
//        data.copyFrom(g, del);
//    }
//
//    @Override
//    public String getDescription(TsFrequency context) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Polynomial trend (").append(ur.getDegree()).append(
//                ')');
//        return builder.toString();
//    }
//
//    @Override
//    public boolean isSignificant(TsDomain domain) {
//        return domain.getLength() > ur.getDegree();
//    }
//
//    @Override
//    public String getName() {
//        return "trend";
//    }
//
//}

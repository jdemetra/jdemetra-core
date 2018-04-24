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
package demetra.tempdisagg.univariate.internal;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.data.normalizer.IDataNormalizer;
import demetra.design.Development;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
class DisaggregationData {

    /**
     * Y expanded to the high frequency (with missing values). hY corresponds to
     * hDom (domain of the set of the indicators). If necessary it is expanded
     * with missing values.
     */
    double[] hY;
    /**
     * Regression variables. Defined on the high level domain.
     */
    Matrix hX;
    /**
     * Regression variables transformed to match the aggregation mode
     * (cumulative variables). Defined on the high level domain.
     */
    Matrix hEX;
    /**
     * High level domain. The results correspond to that domain.
     */
    TsDomain hDom;
    /**
     * High level estimation domain. Corresponds to the domain taken into
     * account in the estimation procedure.
     */
    TsDomain hEDom;
    /**
     * Ratio between the high and the low frequencies (Conversion ratio)
     */
    int FrequencyRatio;
    /**
     * Scaling factor for hY
     */
    double yfactor;
    /**
     * Scaling factors for hX
     */
    double[] xfactor;

    /**
     *
     * @param normalizer
     */
    void scale(IDataNormalizer normalizer) {
        if (normalizer != null) {
            double yfactor = normalizer.normalize(DataBlock.ofInternal(hY));
        } else {
            yfactor = 1;
        }
        if (hX == null) {
            return;
        }

        int nx = hX.getColumnsCount();
        xfactor = new double[nx];

        if (normalizer != null) {
            DataBlockIterator cols = hX.columnsIterator();
            int i = 0;
            while (cols.hasNext()) {
                xfactor[i++] = normalizer.normalize(cols.next());
            }
        } else {
            for (int i = 0; i < xfactor.length; ++i) {
                xfactor[i] = 1;
            }
        }
    }
}

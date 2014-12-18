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

package ec.benchmarking;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IDataNormalizer;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author pcuser
 */
@Development(status = Development.Status.Alpha)
public class DisaggregationData {

    /**
     * Y expanded to the high frequency (with missing values). hY corresponds to
     * hDom (domain of the set of the indicators). If necessary it is expanded
     * with missing values.
     */
    public double[] hY;
    /**
     * Regression variables. Defined on the high level domain.
     */
    public Matrix hX;
    /**
     * Regression variables transformed to match the aggregation mode
     * (cumulative variables). Defined on the high level domain.
     */
    public Matrix hEX;
    /**
     * High level domain. The results correspond to that domain.
     */
    public TsDomain hDom;
    /**
     * High level estimation domain. Corresponds to the domain taken into 
     * account in the estimation procedure.
     */
    public TsDomain hEDom;
    /**
     * Ratio between the high and the low frequencies (Conversion ratio)
     */
    public int FrequencyRatio;
    /**
     * Low frequency (aggregated series)
     */
    public int lowFrequency;
    /**
     * High frequency (disaggregated series)
     */
    public int highFrequency;
    /**
     * Scaling factor for hY
     */
    public double yfactor;
    /**
     * Scaling factors for hX
     */
    public double[] xfactor;

    /**
     *
     */
    public DisaggregationData() {
    }

    /**
     *
     * @param normalizer
     */
    public void scale(IDataNormalizer normalizer) {
        if (normalizer != null && normalizer.process(new ReadDataBlock(hY))) {
            yfactor = normalizer.getFactor();
            hY = normalizer.getNormalizedData();
        } else {
            yfactor = 1;
        }
        if (hX == null) {
            return;
        }

        int nx = hX.getColumnsCount();
        xfactor = new double[nx];

        DataBlockIterator cols = hX.columns();
        DataBlock col = cols.getData();
        int i = 0;
        do {
            if (normalizer != null && normalizer.process(col)) {
                xfactor[i] = normalizer.getFactor();
                col.mul(xfactor[i]);
            } else {
                xfactor[i] = 1;
            }
            ++i;
        } while (cols.next());

    }
}

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

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractOutlierVariable extends AbstractSingleTsVariable
        implements IOutlierVariable {

    // / <summary>Povides the robust MAD estimation for an array of
    // values.</summary>
    /**
     *
     * @param data
     * @param medcorrection
     * @return
     */
    public static double mad(IReadDataBlock data, boolean medcorrection) {
        double[] e = new double[data.getLength()];
        data.copyTo(e, 0);
        Arrays.sort(e);
        int n = e.length;
        double median = 0;
        int n2 = n / 2;
        if (n2 * 2 == n) // n even
        {
            median = (e[n2 - 1] + e[n2]) / 2;
        } else {
            median = e[n2];
        }
        if (medcorrection) {
            for (int i = 0; i < n; ++i) {
                if (e[i] >= median) {
                    e[i] -= median;
                } else {
                    e[i] = median - e[i];
                }
            }
        }

        Arrays.sort(e);
        if (n2 * 2 == n) // n even
        {
            median = (e[n2 - 1] + e[n2]) / 2;
        } else {
            median = e[n2];
        }
        return 1.483 * median;
    }
    TsPeriod position;
    boolean prespecified;

    /**
     *
     * @param p
     */
    protected AbstractOutlierVariable(TsPeriod p) {
        position = p.clone();
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append(getOutlierType()).append(" (").append(position).append(
                ')');
        return builder.toString();
    }

    // / <summary>Position of the outlier</summary>
    @Override
    public TsPeriod getPosition() {
        return position;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isPrespecified() {
        return prespecified;
    }

    @Override
    public void setPrespecified(boolean value) {
        prespecified = value;
    }

}

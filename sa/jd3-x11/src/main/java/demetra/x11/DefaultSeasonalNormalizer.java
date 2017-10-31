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

package demetra.x11;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
class DefaultSeasonalNormalizer extends DefaultX11Algorithm implements
        ISeasonalNormalizer {

    private INormalizing tempNormalizer;

     /**
     *
     * @param option
     */
    public void setNormalizer(SeasonalFilterOption[] options) {
        /* hier müssen nun die stable von den nicht stable getrennt werden */
        /*Wenn ein stable drin ist und nicht alle dann muss es Complex werden*/
        if (options == null) {
            tempNormalizer = new DefaultNormalizingStrategie();
        } else if (options.length == 1) {
            tempNormalizer = new DefaultNormalizingStrategie();
        } else {
            for (int i = 0; i < options.length; ++i) {
                if (options[i] != null && options[i] == SeasonalFilterOption.Stable) {
                    tempNormalizer = new ComplexNormalizingStrategie(options);
                    break;
                }
            }
            if (tempNormalizer == null) {
                tempNormalizer = new DefaultNormalizingStrategie();
            }
        }
    }
    /**
     *
     * @param s
     * @param xdom
     * @return
     */
    @Override
    public TsData normalize(TsData s, TsDomain xdom) {
        /* Das folgende muss sich ändern damit der INormalizing verwendet wird */

        int freq = context.getFrequency();
        /**
         * SymmetricFilter f = TrendCycleFilterFactory.makeTrendFilter(freq);
         * IEndPointsProcessor iep = new CopyEndPoints(f.getLength() / 2);
         *
         * IFiltering n = new DefaultTrendFilteringStrategy(f, iep); TsData tmp
         * = n.process(s, s.getDomain());
         */
        TsData tmp = tempNormalizer.process(s, s.getDomain(), freq);

// CH:Chain of the TS
        TsData snorm = op(s, tmp);
// CH: insert the last missing values of the year befor 
        if (xdom == null) {
            return snorm;
        } else {
            TsData xsnorm = snorm.fittoDomain(xdom);
            int nf = s.getStart().minus(xsnorm.getStart());
            new CopyYearEndPoints(nf, freq).process(null, new DataBlock(xsnorm
                    .internalStorage()));
            return xsnorm;
        }
    }
}

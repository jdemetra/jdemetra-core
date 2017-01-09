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
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 * The seasonal dummies are in fact seasonal contrasts. The contrasting period
 * is by design the last period of the year. The regression variables generated
 * that way are linearly independent.
 *
 * @author Gianluca Caporello
 */
public class SeasonalDummies implements ITsVariable {

    TsFrequency freq_;

    public SeasonalDummies(TsFrequency freq) {
        freq_ = freq;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        int pstart = domain.getStart().getPosition();
        int ifreq = domain.getFrequency().intValue();
        int lstart = ifreq - pstart - 1;
        if (lstart < 0) {
            lstart += ifreq;
        }
        for (int i = 0; i < ifreq - 1; i++) {
            DataBlock x = data.get(i);
            int jstart = i - pstart;
            if (jstart < 0) {
                jstart += ifreq;
            }
            DataBlock m = x.extract(jstart, -1, ifreq);
            m.set(1);
            DataBlock q = x.extract(lstart, -1, ifreq);
            q.set(-1);
        }
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public TsFrequency getDefinitionFrequency() {
        return freq_;

    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription(TsFrequency context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Seasonal dummies");
        return builder.toString();
    }

    /**
     *
     * @return
     */
    @Override
    public int getDim() {
        return freq_.intValue() - 1;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Seasonal dummy [").append(idx + 1).append(']');
        return builder.toString();
    }

    /**
     *
     * @param domain
     * @return
     */
    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.getFrequency() != TsFrequency.Yearly;
    }

    @Override
    public String getName() {
        return "seas#" + getDim();
    }

}

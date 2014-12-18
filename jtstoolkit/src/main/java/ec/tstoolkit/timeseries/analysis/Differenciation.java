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


package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Kristof Bayens
 */
public class Differenciation {
    private TsData new_, old_;

    public enum DifferentiationType {
        Difference,
        Percentage
    }

    public Differenciation(TsData newTs, TsData oldTs) {
        new_ = newTs;
        old_ = oldTs;
    }

    public TsData getNew() {
        return new_;
    }

    public TsData getOld() {
        return old_;
    }

    public TsData getDifference(DifferentiationType type) {
        int startdiff = new_.getStart().minus(old_.getStart());
        if (startdiff > 0)
            new_ = new_.extend(startdiff, 0);
        else
            old_ = old_.extend(-startdiff, 0);
        int enddiff = new_.getEnd().minus(old_.getEnd());
        if (enddiff > 0)
            new_ = new_.extend(0, enddiff);
        else
            old_ = old_.extend(0, -enddiff);

        double[] data = new double[new_.getLength()];
        for (int i=0; i<new_.getLength(); i++) {
            switch (type) {
                case Difference: {
                    data[i] = new_.get(i) - old_.get(i);
                    break;
                }
                case Percentage: {
                    data[i] = ((new_.get(i) - old_.get(i)) / old_.get(i)) * 100;
                    break;
                }
            }
        }
        return new TsData(new_.getFrequency(), new_.getStart().getYear(), new_.getStart().getPosition(), data, true);
    }
}

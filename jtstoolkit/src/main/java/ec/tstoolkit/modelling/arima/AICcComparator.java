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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class AICcComparator implements IModelComparator {

    public AICcComparator() {
        aicDiff_ = 0;
    }

    /**
     * Preference for the reference model
     * It means that the alternative model is accepted if
     * aicc(ref) > aicc(alternative) - aiccdiff
     * @param aiccDiff
     */
    public AICcComparator(double aiccDiff) {
        aicDiff_ = aiccDiff;
    }
    private final double aicDiff_;

    @Override
    public int compare(ModelEstimation reference, ModelEstimation[] models) {
        int imin = -1;
        double aicc = 0;
        for (int i = 0; i < models.length; ++i) {
            if (models[i] != null) {
                double aiccCur = models[i].getStatistics().AICC;
                if (imin < 0 || aiccCur < aicc) {
                    aicc = aiccCur;
                    imin = i;
                }
            }
        }
        if (imin < 0) {
            return -1;
        }
        else if (reference == null) {
            return imin;
        }
        else {
            double aiccRef = reference.getStatistics().AICC;
            return aiccRef > aicc - aicDiff_ ? imin : -1;
        }
    }

    @Override
    public int compare(ModelEstimation reference, ModelEstimation alternative) {
        if (reference == null) {
            return 0;
        }
        else if (alternative == null) {
            return -1;
        }
        double aiccRef = reference.getStatistics().AICC;
        double aicc = alternative.getStatistics().AICC;
        return aiccRef > aicc - aicDiff_ ? 0 : -1;
    }
    
}

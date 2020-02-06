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

package jdplus.tramo;

import jdplus.data.DataBlock;
import demetra.math.Complex;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.regsarima.regular.ProcessingResult;
import jdplus.regsarima.regular.RegArimaModelling;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import jdplus.tramo.internal.DifferencingModule;
import demetra.data.DoubleSeq;
import jdplus.regarima.RegArimaEstimation;


/**
 *
 * @author Jean Palate
 */
class RegularUnderDifferencingTest extends ModelController {

    private static final double RTVAL = 1.6, IM = .01, MOD = .9;

    RegularUnderDifferencingTest() {
    }

    @Override
    ProcessingResult process(RegArimaModelling modelling, TramoProcessor.Context context) {
        ModelDescription desc = modelling.getDescription();
        RegArimaEstimation<SarimaModel> estimation = modelling.getEstimation();
        SarimaModel cur = desc.arima();
        SarimaOrders spec = cur.specification();
        if (spec.getD() == DifferencingModule.MAXD || spec.getP() == 0 || !desc.isMean()) {
            return ProcessingResult.Unchanged;
        }
        if (checkResiduals(estimation.getConcentratedLikelihood().e())) {
            return ProcessingResult.Unchanged;
        }
        if (!hasQuasiUnitRoots(cur)) {
            return ProcessingResult.Unchanged;
        }
        spec.setD(spec.getD() + 1);
        spec.setP(spec.getP() - 1);
        ModelDescription ndesc=ModelDescription.copyOf(desc);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        RegArimaModelling ncontext = RegArimaModelling.of(ndesc);
        if (!estimate(ncontext, false)) {
            return ProcessingResult.Failed;
        }
        else {
            transferInformation(ncontext, modelling);
            return ProcessingResult.Changed;
        }
    }

    private boolean checkResiduals(DoubleSeq e) {
        DataBlock res = DataBlock.of(e);
        double rm = res.sum(), rv = res.ssq();
        int n = res.length();
        rm /= n;
        rv = rv / n - rm * rm;
        double rstd = Math.sqrt(rv / n);
        double rtval = rm / rstd;
        return Math.abs(rtval) <= RTVAL;
    }

    private boolean hasQuasiUnitRoots(SarimaModel m) {
        Complex[] roots = m.getRegularAR().mirror().roots();
        for (int i = 0; i < roots.length; ++i) {
            if (roots[i].getRe() > 0 && Math.abs(roots[i].getIm()) <= IM && roots[i].abs() >= MOD) {
                return true;
            }
        }
        return false;
    }
}

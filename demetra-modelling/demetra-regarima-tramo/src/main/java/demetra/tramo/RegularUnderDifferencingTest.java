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

package demetra.tramo;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.Complex;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.ProcessingResult;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.tramo.internal.DifferencingModule;


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
        ModelEstimation estimation = modelling.getEstimation();
        SarimaModel cur = desc.arima();
        SarimaSpecification spec = cur.specification();
        if (spec.getD() == DifferencingModule.MAXD || spec.getP() == 0 || !desc.isEstimatedMean()) {
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
        RegArimaModelling ncontext = new RegArimaModelling();
        ModelDescription ndesc=new ModelDescription(desc);
        ndesc.setSpecification(spec);
        ndesc.setMean(false);
        ncontext.setDescription(ndesc);
        if (!estimate(ncontext, false)) {
            return ProcessingResult.Failed;
        }
        else {
            transferInformation(ncontext, modelling);
            return ProcessingResult.Changed;
        }
    }

    private boolean checkResiduals(DoubleSequence e) {
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

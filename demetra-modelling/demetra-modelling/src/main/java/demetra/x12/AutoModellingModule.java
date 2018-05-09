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
package demetra.x12;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.polynomials.UnitRoots;
import demetra.regarima.ami.IArmaModule;
import demetra.regarima.ami.IDifferencingModule;
import demetra.regarima.ami.ProcessingResult;
import demetra.regarima.RegArimaUtility;
import demetra.regarima.regular.ModelDescription;
import demetra.regarima.regular.ModelEstimation;
import demetra.regarima.regular.RegArimaContext;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.SarmaSpecification;

/**
 *
 * @author Jean Palate
 */
public class AutoModellingModule {

    private final IDifferencingModule diff;
    private final IArmaModule arma;

    public AutoModellingModule(final IDifferencingModule diff, final IArmaModule arma) {
        this.diff = diff;
        this.arma = arma;
    }

    public ProcessingResult process(RegArimaContext context) {
        ModelDescription desc = context.getDescription();
        ModelEstimation est = context.getEstimation();
        int freq = desc.getAnnualFrequency();
        if (est == null) {
            return ProcessingResult.Failed;
        }
        SarimaSpecification curspec = desc.getSpecification();
        SarimaSpecification nspec;
        boolean nmean = false;
        try {
            int[] periods = freq == 1 ? new int[]{1} : new int[]{1, freq};
            // get residuals
            DoubleSequence res = RegArimaUtility.linearizedData(desc.regarima(), est.getConcentratedLikelihood());
            int[] d = diff.process(res, periods, null);
            int nd = d[0], nbd = freq == 1 ? 0 : d[1];
            nmean = diff.isMeanCorrection();

            DoubleSequence dres;
            if (nd != 0 || nbd != 0) {
                BackFilter ur = new BackFilter(UnitRoots.D(nd).times(UnitRoots.D(nbd)));
                DataBlock tmp = DataBlock.make(res.length() - ur.getDegree());
                ur.apply(res, tmp);
                dres = tmp;
            } else {
                dres = res;
            }
//            if (nmean) {
//                dres.sub(dres.sum() / dres.getLength());
//            }

            SarmaSpecification rsltSpec = arma.process(dres, freq,
                    nd, nbd, context.isSeasonal());
            nspec = SarimaSpecification.of(rsltSpec, nd, nbd);
        } catch (RuntimeException err) {
            nspec = new SarimaSpecification(freq);
            nspec.airline(context.isSeasonal());
        }
        boolean changed = false;
        if (!curspec.equals(nspec)) {
            desc.setSpecification(nspec);
            changed = true;
        }
        if (nmean != desc.isEstimatedMean()) {
            desc.setMean(nmean);
            changed = true;
        }
        if (changed) {
            context.setEstimation(null);
            return ProcessingResult.Changed;
        } else {
            return ProcessingResult.Unchanged;
        }
    }

    static SarimaSpecification calcmaxspec(final int freq, final int inic, final int d,
            final int bd) {
        SarimaSpecification spec = new SarimaSpecification(freq);
        spec.setD(d);
        spec.setBd(bd);
        spec.setP(2);
        spec.setQ(2);
        if (freq > 1) {
//                    if (bd == 0) {
            spec.setBp(1);
//                    }
            spec.setBq(1);
        }
        return spec;
    }

}

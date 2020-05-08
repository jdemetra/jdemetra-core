/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.revisions.r;

import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.Coefficient;
import demetra.revisions.parametric.OlsTest;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.parametric.RevisionAnalysis;
import demetra.stats.TestResult;
import java.time.LocalDate;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Utility {
    
    public double theil(RegressionBasedAnalysis<LocalDate> analysis, int k){
        if (k>analysis.getRevisions().size())
            return Double.NaN;
        return analysis.getRevisions().get(k-1).getTheilCoefficient();
    }
    
    public double[] olsInformation(RegressionBasedAnalysis<LocalDate> analysis, int k){
        if (k>analysis.getRevisions().size())
            return null;
        RevisionAnalysis<LocalDate> cur = analysis.getRevisions().get(k-1);
        if (cur == null)
            return null;
        OlsTest reg = cur.getRegression();
        if (reg == null)
            return null;
        Coefficient b0 = reg.getIntercept();
        Coefficient b1 = reg.getSlope();
        TestResult jb = reg.getDiagnostics().getJarqueBera();
        TestResult bp = reg.getDiagnostics().getBreuschPagan();
        TestResult w = reg.getDiagnostics().getWhite();
        return new double[]{
            reg.getN(), reg.getR2(), 
            b0.getEstimate(), b0.getStdev(), b0.getTstat(), b0.getPvalue(),
            b1.getEstimate(), b1.getStdev(), b1.getTstat(), b1.getPvalue(),
            jb.getValue(), jb.getPvalue(),
            bp.getValue(), bp.getPvalue(),
            w.getValue(), w.getPvalue()
        };
    }

    public double[] biasInformation(RegressionBasedAnalysis<LocalDate> analysis, int k) {
        if (k>analysis.getRevisions().size())
            return null;
        RevisionAnalysis<LocalDate> cur = analysis.getRevisions().get(k-1);
        if (cur == null)
            return null;
        Bias bias = cur.getBias();
        if (bias == null) {
            return null;
        }
        return new double[]{
            bias.getN(), 
            bias.getMu(),
            bias.getSigma(),
            bias.getT(),
            bias.getTPvalue(),
            bias.getAr(),
            bias.getAdjustedSigma(),
            bias.getAdjustedT(),
            bias.getAdjustedTPvalue()};
    }
}

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

package demetra.stats.tests;

import demetra.data.DataBlock;
import demetra.dstats.F;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import demetra.linearmodel.LinearModel;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;
import demetra.maths.matrices.internal.HouseholderR;
import java.util.Arrays;
import java.util.List;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class Anova {

    private static final double EPS=1e-9;
    private final double rssq;
    private final int rdf;
    
    private final Row[] rows;

    public final class Row {

        Row(final int df, final double ssq) {
            this.df = df;
            this.ssq = ssq;
        }
        public final int df;
        public final double ssq;

        public double mssq() {
            return ssq / df;
        }

        public StatisticalTest ftest() {
            F f = new F(df, rdf);
            double val = ssq * rdf / (df * rssq);
            return new StatisticalTest(f, val, TestType.Upper, true);
        }
    }

    public Anova(LinearModel model, int[] groups) {
        HouseholderR qr=new HouseholderR();
        qr.setPrecision(EPS);
        qr.decompose(model.variables());
        ConcentratedLikelihoodWithMissing[] ll = nestedModelsEstimation(model.isMeanCorrection(), qr, model.getY(), groups);
        rssq=ll[groups.length].ssq();
        rdf=ll[groups.length].degreesOfFreedom();
        
        rows=new Row[groups.length];
        for (int i=0; i<rows.length; ++i){
            int df=ll[i].degreesOfFreedom()-ll[i+1].degreesOfFreedom();
            double dssq=ll[i].ssq()-ll[i+1].ssq();
            rows[i]=new Row(df, dssq);
        }
    }
    
    public int getResidualsDegeesOfFreedom(){
        return rdf;
    }

    public double getResidualsSsq(){
        return rssq;
    }
    
    public List<Row> getRows(){
        return Arrays.asList(rows);
    }

    private ConcentratedLikelihoodWithMissing likelihood(HouseholderR qr, DoubleSeq y, int nvars) {
        int rank = qr.rank(nvars);
        int n = qr.getRowsCount();
        DataBlock res = DataBlock.make(n - rank);
        DataBlock b = DataBlock.make(rank);
        qr.partialLeastSquares(y, b, res);
        double ssqerr = res.ssq();
        // initializing the results...
        return ConcentratedLikelihoodWithMissing.builder()
                .ndata(n)
                .ssqErr(ssqerr)
                .residuals(res)
                .coefficients(b)
                .rfactor(qr.r())
                .build();
     }

    private ConcentratedLikelihoodWithMissing[] nestedModelsEstimation(boolean mean, HouseholderR qr, DoubleSeq y, int[] groups) {
        ConcentratedLikelihoodWithMissing[] ll = new ConcentratedLikelihoodWithMissing[groups.length + 1];
        int n = mean ? 1 : 0;
        ll[0] = likelihood(qr, y, n);
        for (int i = 0; i < groups.length; ++i) {
            n += groups[i];
            ll[i + 1] = likelihood(qr, y, n);
        }
        return ll;
    }
}

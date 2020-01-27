/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jdplus.stats.tests;

import jdplus.likelihood.ConcentratedLikelihood;
import jdplus.linearmodel.LinearModel;
import demetra.data.DoubleSeq;
import jdplus.data.DataBlock;
import jdplus.dstats.F;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.HouseholderR;
import java.util.Arrays;
import java.util.List;

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
        ConcentratedLikelihood[] ll = nestedModelsEstimation(model.isMeanCorrection(), qr, model.getY(), groups);
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

    private ConcentratedLikelihood likelihood(HouseholderR qr, DoubleSeq y, int nvars) {
        int rank = qr.rank(nvars);
        int n = qr.getRowsCount();
        DataBlock res = DataBlock.make(n - rank);
        DataBlock b = DataBlock.make(rank);
        qr.partialLeastSquares(y, b, res);
        double ssqerr = res.ssq();
        // initializing the results...
        Matrix u = UpperTriangularMatrix.inverse(qr.r());
        Matrix bvar = SymmetricMatrix.UUt(u);
        return ConcentratedLikelihood.builder()
                .ndata(n)
                .ssqErr(ssqerr)
                .residuals(res)
                .coefficients(b)
                .unscaledCovariance(bvar)
                .build();
     }

    private ConcentratedLikelihood[] nestedModelsEstimation(boolean mean, HouseholderR qr, DoubleSeq y, int[] groups) {
        ConcentratedLikelihood[] ll = new ConcentratedLikelihood[groups.length + 1];
        int n = mean ? 1 : 0;
        ll[0] = likelihood(qr, y, n);
        for (int i = 0; i < groups.length; ++i) {
            n += groups[i];
            ll[i + 1] = likelihood(qr, y, n);
        }
        return ll;
    }
}

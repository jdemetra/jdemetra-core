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

package ec.tstoolkit.stats;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;
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
            F f = new F();
            f.setDFNum(df);
            f.setDFDenom(rdf);
            double val = ssq * rdf / (df * rssq);
            return new StatisticalTest(f, val, TestType.Upper, true);
        }
    }

    public Anova(RegModel model, int[] groups) {
        HouseholderR qr=new HouseholderR(false);
        qr.setEpsilon(EPS);
        qr.decompose(model.variables());
        ConcentratedLikelihood[] ll = nestedModelsEstimation(model.isMeanCorrection(), qr, model.getY(), groups);
        rssq=ll[groups.length].getSsqErr();
        rdf=ll[groups.length].getDegreesOfFreedom(true, 0);
        
        rows=new Row[groups.length];
        for (int i=0; i<rows.length; ++i){
            int df=ll[i].getDegreesOfFreedom(true, 0)-ll[i+1].getDegreesOfFreedom(true, 0);
            double dssq=ll[i].getSsqErr()-ll[i+1].getSsqErr();
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

    private ConcentratedLikelihood likelihood(HouseholderR qr, DataBlock y, int nvars) {
        int rank = qr.rank(nvars);
        int n = qr.getEquationsCount();
        DataBlock res = new DataBlock(n - rank);
        DataBlock b = new DataBlock(rank);
        qr.partialLeastSquares(y, b, res);
        ConcentratedLikelihood ll = new ConcentratedLikelihood();
        double ssqerr = res.ssq();
        Matrix u = UpperTriangularMatrix.inverse(qr.getR());

        // initializing the results...
        double sig = ssqerr / n;
        Matrix bvar = SymmetricMatrix.XXt(u);
        bvar.mul(sig);
        ll.set(ssqerr, 0, n);
        ll.setRes(res.getData());
        ll.setB(b.getData(), bvar, rank);
        return ll;
    }

    private ConcentratedLikelihood[] nestedModelsEstimation(boolean mean, HouseholderR qr, DataBlock y, int[] groups) {
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

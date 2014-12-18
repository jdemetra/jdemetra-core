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

package ec.satoolkit.diagnostics;

import ec.tstoolkit.timeseries.simplets.TsData;
import data.Data;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityTestTest {

    public SeasonalityTestTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void IBTest() {

        TsData m1 = Data.M1;
        TsData m2 = Data.M2;
        TsData m3 = Data.M3;

        int n = m1.getLength();
        int m = 3;
        
        // Computes the absolute de-trended series by applying hpfilter
        DataBlock t1 = hpfilter(m1, 1600);
        TsData c1 = m1.minus(new TsData(m1.getStart(), t1)).abs();
        DataBlock t2 = hpfilter(m2, 1600);
        TsData c2 = m2.minus(new TsData(m2.getStart(), t2)).abs();
        DataBlock t3 = hpfilter(m3, 1600);
        TsData c3 = m3.minus(new TsData(m3.getStart(), t3)).abs();

        // creates the stacked tot
        DataBlock tot = new DataBlock(n * m);
        tot.extract(0, n).copy(c1);
        tot.extract(n, n).copy(c2);
        tot.extract(2 * n, n).copy(c3);
        

        // creates the regression variables
        // month contains the seasonal dummies
        int ifreq = m1.getFrequency().intValue();
        Matrix month = new Matrix(m * n, ifreq);
        for (int j = 0; j < m; ++j) {
            SubMatrix Q = month.subMatrix(j * n, (j + 1) * n, 0, ifreq);
            for (int i = 0; i < ifreq; ++i) {
                Q.column(i).extract(i, -1, ifreq).set(1);
            }
        }

        // creates the series dummies in series
        Matrix series = new Matrix(m * n, m);
        for (int i = 0; i < m; ++i) {
            series.column(i).extract(i * n, n).set(1);
        }

        // create the years dummies in year
        int ny = 1+(m1.getLength()-1) / ifreq;

        Matrix year = new Matrix(m * n, ny);
        // complete the matrix
        for (int j = 0; j < m; ++j) {
            SubMatrix Z = year.subMatrix(j * n, (j + 1) * n, 0, ny);
            for (int i = 0; i < ny; ++i) {
                int i0 = i * ifreq;
                int i1 = Math.min(i0 + ifreq, n);
                Z.column(i).extract(i0, i1 - i0).set(1);
            }
        }

         // compute the regression variables and the linear model
        RegModel model = new RegModel();

        model.setY(tot);
        model.setMeanCorrection(true);
        
       Ols ols = new Ols();
        ols.process(model);
        double ssqErr0 = ols.getLikelihood().getSsqErr();

        // restricted model
        for (int i = 1; i < ifreq; ++i) {
            model.addX(month.column(i));
        }
        ols.process(model);
        for (int i = 1; i < ny; ++i) {
            model.addX(year.column(i));
        }
        double ssqErr1=ols.getLikelihood().getSsqErr();
        ols.process(model);
        double ssqErr2 = ols.getLikelihood().getSsqErr();

        // full model
        for (int i = 1; i < m; ++i) {
            model.addX(series.column(i));
        }

        ols.process(model);
        double ssqErr3 = ols.getLikelihood().getSsqErr();

        double sig=ols.getLikelihood().getSsqErr()/(m*n-ifreq-ny-m+2);
        double dssq1 = (ssqErr0 - ssqErr1) / (ifreq - 1)/sig;
        double dssq2 = (ssqErr1 - ssqErr2) / (ny -1)/sig;
        double dssq3 = (ssqErr2 - ssqErr3) / (m - 1)/sig;

        F fstat=new F();
        fstat.setDFNum(m - 1);
        fstat.setDFDenom(m*n-ifreq-ny-m+2);
        double prob=fstat.getProbability(dssq3, ProbabilityType.Upper);
        if (prob > .1)
            System.out.println("Direct");
        else
            System.out.println("Indirect");
    }

    public static DataBlock hpfilter(IReadDataBlock x, double lambda) {
        int n = x.getLength();
        Matrix M = Matrix.diff(n, 1, 2);
        // M=M*M' (cross-product)
        M = SymmetricMatrix.XtX(M);
        // M=M*lamda
        M.mul(lambda);
        // add 1 on the diagonal
        M.diagonal().add(1);
        DataBlock rslt = new DataBlock(x);
        // solve the linear system. 
        SymmetricMatrix.rsolve(M, rslt, false);
        return rslt;
    }
}

class HPFilter {

    private final double lambda_;
    private Matrix L;

    HPFilter(int n, double lambda) {
        this.lambda_ = lambda;
        // compute M
        Matrix M = Matrix.diff(n, 1, 2);
        // M=M*M' (cross-product)
        L = new Matrix(n, n + M.getRowsCount());
        M.mul(Math.sqrt(lambda));
        L.diagonal().set(1);
        L.subMatrix(0, n, n, L.getColumnsCount()).copy(M.subMatrix().transpose());
        // triangularize...
        ElementaryTransformations.givensTriangularize(L.subMatrix());
        L = new Matrix(L.subMatrix(0, n, 0, n));
    }

    boolean process(DataBlock x) {
        if (x.getLength() != L.getColumnsCount()) {
            return false;
        }
        LowerTriangularMatrix.rsolve(L, x);
        // L'X = year
        // X'L = year'
        LowerTriangularMatrix.lsolve(L, x);
        return true;
    }
}

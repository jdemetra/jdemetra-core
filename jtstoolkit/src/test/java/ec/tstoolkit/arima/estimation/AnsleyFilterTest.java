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

package ec.tstoolkit.arima.estimation;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.ElementaryTransformations;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.FastFilter;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.estimation.BurmanEstimatesC;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Jean Palate
 */
public class AnsleyFilterTest {

    public AnsleyFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testCholesky() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA4);
        ArimaModel tmodel = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComponent(0);
        SarimaModel model = rslt.get("preprocessing", PreprocessingModel.class).estimation.getArima();
        ArimaModel noise = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComplement(0);
        Polynomial ds = tmodel.getNonStationaryAR().getPolynomial();
        Polynomial dn = noise.getNonStationaryAR().getPolynomial();

        int n = Data.X.getLength();
        Matrix VS = new Matrix(n - ds.getDegree(), n - ds.getDegree());
        AutoCovarianceFunction acf = tmodel.stationaryTransformation().stationaryModel.getAutoCovarianceFunction();
        double[] cov = acf.values(VS.getRowsCount());
        for (int i = 1; i < VS.getColumnsCount(); ++i) {
            VS.subDiagonal(i).set(cov[i]);
            VS.subDiagonal(-i).set(cov[i]);
        }
        VS.diagonal().set(cov[0]);

        Matrix VN = new Matrix(n - dn.getDegree(), n - dn.getDegree());
        acf = noise.stationaryTransformation().stationaryModel.getAutoCovarianceFunction();
        cov = acf.values(VN.getRowsCount());
        for (int i = 1; i < VN.getColumnsCount(); ++i) {
            VN.subDiagonal(i).set(cov[i]);
            VN.subDiagonal(-i).set(cov[i]);
        }
        VN.diagonal().set(cov[0]);

        Matrix QS = SymmetricMatrix.inverse(VS);
        Matrix QN = SymmetricMatrix.inverse(VN);

        Matrix DS = new Matrix(n - ds.getDegree(), n);
        Matrix DN = new Matrix(n - dn.getDegree(), n);

        double[] c = ds.getCoefficients();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DS.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }
        c = dn.getCoefficients();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DN.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }

        Matrix AS = SymmetricMatrix.quadraticForm(QS, DS);
        Matrix AN = SymmetricMatrix.quadraticForm(QN, DN);

        Matrix W = SymmetricMatrix.inverse(AS.plus(AN));

    }

    //@Test
    public void demoWKFilter() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA3);
        ArimaModel tmodel = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComponent(0);
        SarimaModel model = rslt.get("preprocessing", PreprocessingModel.class).estimation.getArima();
        ArimaModel noise = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComplement(0);
        ArimaModel check = tmodel.plus(noise);
        long t0 = System.currentTimeMillis();
        for (int a = 0; a < 1000; ++a) {
            // differencing matrices
            int n = Data.X.getLength();
            Polynomial ds = tmodel.getNonStationaryAR().getPolynomial();
            Polynomial dn = noise.getNonStationaryAR().getPolynomial();

            Matrix DS = new Matrix(n - ds.getDegree(), n);
            Matrix DN = new Matrix(n - dn.getDegree(), n);

            double[] c = ds.getCoefficients();
            for (int j = 0; j < c.length; ++j) {
                DataBlock d = DS.subDiagonal(j);
                d.set(c[c.length - j - 1]);
            }
            c = dn.getCoefficients();
            for (int j = 0; j < c.length; ++j) {
                DataBlock d = DN.subDiagonal(j);
                d.set(c[c.length - j - 1]);
            }

            AnsleyFilter S = new AnsleyFilter();
            S.initialize((IArimaModel) tmodel.stationaryTransformation().stationaryModel, n - ds.getDegree());
            AnsleyFilter N = new AnsleyFilter();
            N.initialize((IArimaModel) noise.stationaryTransformation().stationaryModel, n - dn.getDegree());

            Matrix Q = new Matrix(n, 2 * n - ds.getDegree() - dn.getDegree());
            for (int i = 0; i < n; ++i) {
                S.filter(DS.column(i), Q.row(n - i - 1).range(0, n - ds.getDegree()));
            }
            //Q.subMatrix(0, n, 0, n - ds.getDegree()).mul(Math.sqrt(noise.getInnovationVariance() / tmodel.getInnovationVariance()));
            for (int i = 0; i < n; ++i) {
                N.filter(DN.column(i), Q.row(n - i - 1).drop(n - ds.getDegree(), 0));
            }
            double[] y = Data.X.internalStorage();
            DataBlock yd = new DataBlock(y.length - dn.getDegree());
            noise.getNonStationaryAR().filter(new DataBlock(y), yd);
            DataBlock yl = new DataBlock(yd.getLength());
            N.filter(yd, yl);
            // compute K'n x yl. Don't forget: Q is arranged in reverse order !
            // should be improved to take into account the structure of K
            double[] z = new double[n];
            for (int i = 0, j = n - 1; i < n; ++i, --j) {
                z[i] = Q.row(j).drop(n - ds.getDegree(), 0).dot(yl);
            }
            // triangularize by means of Givens rotations
            ElementaryTransformations.givensTriangularize(Q.subMatrix());
            Matrix L = new Matrix(Q.internalStorage(), n, n);
            LowerTriangularMatrix.rsolve(L, z);
            LowerTriangularMatrix.lsolve(L, z);
            if (a == 0) {
                for (int i = 0; i < z.length; ++i) {
                    System.out.println(z[i]);
                }
            }
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    //@Test
    public void demoBurman() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA3);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        ArimaModel tmodel = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComponent(0);
        SarimaModel model = rslt.get("preprocessing", PreprocessingModel.class).estimation.getArima();
        ArimaModel noise = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComplement(0);
        ArimaModel check = tmodel.plus(noise);
        long t0 = System.currentTimeMillis();
        for (int a = 0; a < 1000; ++a) {
            BurmanEstimatesC burman = new BurmanEstimatesC();
            burman.setUcarimaModel(ucm);
            burman.setData(Data.X);
            double[] z = burman.estimates(0, true);
            if (a == 0) {
                for (int i = 0; i < z.length; ++i) {
                    System.out.println(z[i]);
                }
            }

        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

    //@Test
    public void demoKF() {
        CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.X, TramoSeatsSpecification.RSA3);
        UcarimaModel ucm = rslt.get("decomposition", SeatsResults.class).getUcarimaModel();
        ArimaModel tmodel = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComponent(0);
        SarimaModel model = rslt.get("preprocessing", PreprocessingModel.class).estimation.getArima();
        ArimaModel noise = rslt.get("decomposition", SeatsResults.class).getUcarimaModel().getComplement(0);
        ArimaModel check = tmodel.plus(noise);
        long t0 = System.currentTimeMillis();
        for (int a = 0; a < 1000; ++a) {
            // add forecasts
            ec.tstoolkit.ssf.SsfData sdata = new ec.tstoolkit.ssf.SsfData(Data.X.internalStorage(), null);

            SsfUcarima ssf = new SsfUcarima(ucm);
            // compute KS
            DisturbanceSmoother smoother = new DisturbanceSmoother();
            smoother.setSsf(ssf);
            FastFilter<SsfUcarima> filter = new FastFilter<>();
            filter.setSsf(ssf);
            DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
            filter.process(sdata, frslts);

            smoother.process(new ec.tstoolkit.ssf.SsfData(Data.X.internalStorage(), null), frslts);
            ec.tstoolkit.ssf.SmoothingResults srslts = smoother.calcSmoothedStates();
            double[] z = srslts.component(0);
            if (a == 0) {
                for (int i = 0; i < z.length; ++i) {
                    System.out.println(z[i]);
                }
            }

        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}

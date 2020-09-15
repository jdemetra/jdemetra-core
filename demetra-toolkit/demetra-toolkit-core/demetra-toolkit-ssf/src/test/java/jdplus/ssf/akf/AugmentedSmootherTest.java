/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.ssf.akf;

import demetra.arima.SarimaOrders;
import demetra.data.Data;
import demetra.data.DoubleSeq;
import jdplus.arima.ssf.SsfArima;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.sarima.SarimaModel;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.ssf.SsfUcarima;
import static jdplus.ucarima.ssf.SsfUcarimaTest.ucmAirline;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class AugmentedSmootherTest {

    public AugmentedSmootherTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    public static void main(String[] arg) {
        SarimaOrders spec = SarimaOrders.m011(1);
        SarimaModel arima = SarimaModel.builder(spec)
                .theta(1, -.9)
                .build();
        Ssf ssf = Ssf.of(SsfArima.of(arima), SsfArima.defaultLoading());
        SsfData data = new SsfData(Data.NILE);
        AugmentedSmoother smoother = new AugmentedSmoother();
        smoother.setCalcVariances(true);
        DefaultSmoothingResults sd = DefaultSmoothingResults.full();
        sd.prepare(ssf.getStateDim(), 0, data.length());
        smoother.process(ssf, data, sd);
        double sig2=smoother.getFilteringResults().var();
        
        int n = data.length();
        for (int i = 0; i < n; ++i) {
//            System.out.print(sd.smoothation(i));
            System.out.print(sd.smoothation(i) / sd.smoothationVariance(i));
            System.out.print('\t');
            Matrix X = Matrix.make(n, 1);
            X.set(i, 0, 1);
            Ssf ssfx = RegSsf.ssf(ssf, X);
            DefaultSmoothingResults sr = DkToolkit.smooth(ssfx, data, false, true);
            double last = sr.a(0).getLast();
            System.out.print(last);
            System.out.print('\t');
            System.out.print(sd.smoothation(i)*sd.smoothation(i) / sd.smoothationVariance(i)/sig2);
            System.out.print('\t');
            DataBlock R = DataBlock.of(sd.R(i));
            Matrix Rvar = sd.RVariance(i).deepClone();
            SymmetricMatrix.lcholesky(Rvar, 1e-9);
            LowerTriangularMatrix.solveLx(Rvar, R, 1e-9);
            System.out.println(R.ssq()/sig2);
            
       }

    }
}

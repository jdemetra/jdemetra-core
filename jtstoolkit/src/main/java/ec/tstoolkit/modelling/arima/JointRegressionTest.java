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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.F;
import ec.tstoolkit.dstats.TestType;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.stats.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class JointRegressionTest implements IRegressionTest {

    private StatisticalTest ftest;
    private double eps_;

    public JointRegressionTest(final double eps) {
        eps_ = eps;
    }

    /**
     * Tests that all the specified variables are jointly 0.
     *
     * @param ll
     * @param nhp
     * @param ireg
     * @param nregs
     * @param info
     * @return
     */
    @Override
    public boolean accept(ConcentratedLikelihood ll, int nhp, int ireg, int nregs, InformationSet info) {
        final double f;
        if (nregs > 1) {
            Matrix bvar = new Matrix(ll.getBVar(nhp >= 0, nhp).subMatrix(ireg, ireg + nregs, ireg, ireg + nregs));
            SymmetricMatrix.lcholesky(bvar);
            double[] b = new double[bvar.getRowsCount()];
            System.arraycopy(ll.getB(), ireg, b, 0, nregs);
            LowerTriangularMatrix.rsolve(bvar, b);
            f = new DataBlock(b).ssq() / nregs;
        } else {
            double t = ll.getTStat(ireg, true, nhp);
            f = t * t;
        }
        F fdist = new F();
        fdist.setDFNum(nregs);
        fdist.setDFDenom(ll.getDegreesOfFreedom(nhp >= 0, nhp));
        ftest = new StatisticalTest(fdist, f, TestType.Upper, true);
        ftest.setSignificanceThreshold(eps_);
        return ftest.isSignificant();
    }

    public boolean accept(DiffuseConcentratedLikelihood ll, int nhp, int ireg, int nregs, InformationSet info) {
        final double f;
        if (nregs > 1) {
            Matrix bvar = new Matrix(ll.bvar(nhp >= 0, nhp).subMatrix(ireg, ireg + nregs, ireg, ireg + nregs));
            SymmetricMatrix.lcholesky(bvar);
            double[] b = new double[bvar.getRowsCount()];
            System.arraycopy(ll.getB(), ireg, b, 0, nregs);
            LowerTriangularMatrix.rsolve(bvar, b);
            f = new DataBlock(b).ssq() / nregs;
        } else {
            double t = ll.getTStat(ireg, true, nhp);
            f = t * t;
        }
        F fdist = new F();
        fdist.setDFNum(nregs);
        fdist.setDFDenom(ll.getDegreesOfFreedom(nhp >= 0, nhp));
        ftest = new StatisticalTest(fdist, f, TestType.Upper, true);
        ftest.setSignificanceThreshold(eps_);
        return ftest.isSignificant();
    }

    /**
     * Tests that all the specified variables are jointly 0.
     *
     * @param ll The concentrated likelihood
     * @param nhp The number of hyper parameters
     * @param ipos the positions of the tested variables
     * @param info InformationSet that could contain additional information. May
     * be null
     * @return true if we accept H0 (the coefficients of the considered
     * variables are 0).
     */
    public boolean accept(ConcentratedLikelihood ll, int nhp, int[] ipos, InformationSet info) {
        Matrix bvar = ll.getBVar(nhp >= 0, nhp).clone();
        bvar=Matrix.select(bvar.subMatrix(), ipos, ipos);
        SymmetricMatrix.lcholesky(bvar);
        double[] b = new double[bvar.getRowsCount()];
        for (int i = 0; i < ipos.length; ++i) {
            b[i] = ll.getB()[ipos[i]];
        }
        LowerTriangularMatrix.rsolve(bvar, b);
        double f = new DataBlock(b).ssq() / ipos.length;
        F fdist = new F();
        fdist.setDFNum(ipos.length);
        fdist.setDFDenom(ll.getDegreesOfFreedom(nhp >= 0, nhp));
        ftest = new StatisticalTest(fdist, f, TestType.Upper, true);
        ftest.setSignificanceThreshold(eps_);
        return ftest.isSignificant();
    }

    public StatisticalTest getTest() {
        return ftest;
    }
}

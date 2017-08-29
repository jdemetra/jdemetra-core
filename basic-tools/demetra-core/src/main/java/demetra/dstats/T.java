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
package demetra.dstats;

import demetra.dstats.internal.SpecialFunctions;
import demetra.dstats.internal.ProbInvFinder;
import demetra.dstats.internal.Utility;
import demetra.design.Development;
import demetra.random.IRandomNumberGenerator;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Release)
public class T implements IContinuousDistribution {
    
    private static final Normal N = new Normal();
    static final double[][] COEFF = new double[][]{
        new double[]{1.0, 1.0, 0.0, 0.0, 0.0},
        new double[]{3.0, 16.0, 5.0, 0.0, 0.0},
        new double[]{-15.0, 17.0, 19.0, 3.0, 0.0},
        new double[]{-945.0, -1920.0, 1482.0, 776.0, 79.0}};
    static final double[] DENOM = {4.0, 96.0, 384.0, 92160.0};
    static final int[] DEG = {2, 3, 4, 5};
    
    static double calcInitT(final double p, final double df) {
        double x = N.getProbabilityInverse(p, ProbabilityType.Lower);
        double xx = x * x;
        double sum = x;
        double denpow = 1.0;
        for (int i = 0; i < 4; i++) {
            double term = Utility.calcPoly(COEFF[i], DEG[i], xx) * x;
            denpow *= df;
            sum += term / (denpow * DENOM[i]);
        }
        
        return sum;
    }
    
    private final double df;
    private final AtomicReference<Chi2> chi2;

    /**
     * Default constructor; sets the degrees of freedom to 2
     * @param df
     */
    public T(final double df) {
        if (df < 0)
            throw new IllegalArgumentException("The degrees of freedom should be strictly positive");
        this.df = df;
        chi2 = new AtomicReference<>();
    }

    /**
     *
     * @return
     */
    public double getDegreesofFreedom() {
        return df;
    }
    
    @Override
    public double getDensity(final double x) {
        return SpecialFunctions.studentDensity(x, df);
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("T with ");
        sb.append(df);
        sb.append(" degrees of freedom");
        return sb.toString();
    }
    
    @Override
    public double getExpectation() {
        if (df <= 1)
            throw new DStatException(DStatException.ERR_UNDEFINED, "T");
        return 0.0;
    }
    
    @Override
    public double getVariance() {
        if (df <= 1) {
            throw new DStatException(DStatException.ERR_UNDEFINED, "T");
        } else if (df <= 2) {
            return Double.POSITIVE_INFINITY;
        } else {
            return (df) / (df - 2.0);
        }
    }

    @Override
    public double getLeftBound() {
        return Double.NEGATIVE_INFINITY;
    }
    
    @Override
    public double getProbability(final double x, final ProbabilityType pt) {
        if (pt == ProbabilityType.Point) {
            return 0;
        }
        if (x == 0) {
            return .5;
        }
        
        double res = SpecialFunctions.studentProbability(x, df);
        if (pt == ProbabilityType.Upper) {
            res = 1.0 - res;
        }
        
        return res;
    }
    
    @Override
    public double getProbabilityInverse(double p, final ProbabilityType pt) {
        if (pt == ProbabilityType.Upper) {
            p = 1.0 - p;
        }
        if (p < EPS_P || 1 - p < EPS_P) {
            throw new DStatException(DStatException.ERR_INV_SMALL);
        }
        if (Math.abs(p - .5) < EPS_P) {
            return 0;
        }
        double start = calcInitT(p, df);
        return ProbInvFinder.find(p, start, EPS_P, EPS_X, this);
    }
    
    @Override
    public double getRightBound() {
        return Double.POSITIVE_INFINITY;
    }
    
    
    @Override
    public BoundaryType hasLeftBound() {
        return BoundaryType.None;
    }
    
    @Override
    public BoundaryType hasRightBound() {
        return BoundaryType.None;
    }
    
    @Override
    public boolean isSymmetrical() {
        return true;
    }
    
    @Override
    public double random(IRandomNumberGenerator rng) {
        Chi2 cdenom = chi2.get();
        if (cdenom == null) {
            cdenom = new Chi2(df);
            chi2.set(cdenom);
        }
        return N.random(rng) / Math.sqrt(cdenom.random(rng) / df);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("T(");
        sb.append(df);
        sb.append(')');
        return sb.toString();
    }
}

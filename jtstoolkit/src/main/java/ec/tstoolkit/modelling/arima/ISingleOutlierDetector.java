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

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.TableOfBoolean;
import ec.tstoolkit.design.AlgorithmDefinition;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.regression.IOutlierFactory;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.DoubleList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Release)
@AlgorithmDefinition
public interface ISingleOutlierDetector<T extends IArimaModel> {

    /**
     *
     * @param nval
     * @return
     */
    public static double calcVA(int nval) {
        return calcVA(nval, 0.05);
    }

    /**
     *
     * @param nvals
     * @param alpha
     * @return
     */
    public static double calcVA(int nvals, double alpha) {
        Normal normal = new Normal();
        if (nvals == 1) {
            return normal.getProbabilityInverse(alpha / 2,
                    ProbabilityType.Upper);
        }
        double n = nvals;
        double[] y = new double[3];
        int[] x = new int[]{2, 100, 200};
        Matrix X = new Matrix(3, 3);

        for (int i = 0; i < 3; ++i) {
            X.set(i, 0, 1);
            X.set(i, 2, Math.sqrt(2 * Math.log(x[i])));
            X.set(i, 1, (Math.log(Math.log(x[i])) + Math.log(4 * Math.PI))
                    / (2 * X.get(i, 2)));
        }

        y[0] = normal.getProbabilityInverse((1 + Math.sqrt(1 - alpha)) / 2,
                ProbabilityType.Lower);
        for (int i = 1; i < 3; ++i) {
            y[i] = calcVAL(x[i], alpha);
        }
        // solve X b = y
        Householder qr = new Householder(false);
        qr.decompose(X);
        double[] b = qr.solve(y);

        double acv = Math.sqrt(2 * Math.log(n));
        double bcv = (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                / (2 * acv);
        return b[0] + b[1] * bcv + b[2] * acv;
    }

    public static double calcVAL(int nvals, double alpha) {
        if (nvals == 1) {
            return 1.96; // normal distribution
        }
        double n = nvals;
        double pmod = 2 - Math.sqrt(1 + alpha);
        double acv = Math.sqrt(2 * Math.log(n));
        double bcv = acv - (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                / (2 * acv);
        double xcv = -Math.log(-.5 * Math.log(pmod));
        return xcv / acv + bcv;
    }

    /**
     *
     * @param o
     * @param weight
     */
    void addOutlierFactory(IOutlierFactory o, double weight);

    /**
     *
     * @param e
     */
    public static double calcMAD(IReadDataBlock e, int centile) {
        int n = e.getLength();
        double[] a = new double[n];
        e.copyTo(a, 0);
        for (int i = 0; i < n; ++i) {
            a[i] = Math.abs(a[i]);
        }
        Arrays.sort(a);
        double m = 0;
        int nm = n * centile / 100;
        if (n % 2 == 0) // n even
        {
            m = (a[nm - 1] + a[nm]) / 2;
        } else {
            m = a[nm];
        }
        Normal normal = new Normal();
        double l = normal.getProbabilityInverse(0.5 + .005 * centile,
                ProbabilityType.Lower);
        return m / l;
    }

 void clearOutlierFactories();

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    double coeff(int pos, int outlier);
    /**
     *
     * @param pos
     * @param ioutlier
     */
    void exclude(int pos, int ioutlier);

    /**
     *
     * @param pos
     * @param ioutlier
     */
    void allow(int pos, int ioutlier);

    /**
     *
     * @param pos
     */
    void exclude(int[] pos);

    /**
     *
     * @param o
     */
    void exclude(IOutlierVariable o);
    /**
     *
     * @param o
     */
    void allow(IOutlierVariable o) ;
    /**
     *
     * @param outliers
     */
    
    default void exclude(IOutlierVariable[] outliers) {
        for (IOutlierVariable o : outliers) {
            exclude(o);
        }
    }


    /**
     *
     * @param pos
     * @param ioutlier
     */
    default void exclude(TsPeriod pos, int ioutlier) {
        int r = pos.minus(getDomain().getStart());
        if (r >= 0) {
            exclude(r, ioutlier);
        }
    }

    /**
     *
     * @return
     */
    TsDomain getDomain() ;

    /**
     *
     * @return
     */
    IOutlierVariable getMaxOutlier();

    /**
     *
     * @return
     */
    double getMaxTStat();

    /**
     *
     * @return
     */
    int getOutlierFactoriesCount() ;

    /**
     *
     * @param i
     * @return
     */
    IOutlierFactory getOutlierFactory(int i);

    /**
     *
     * @param model
     * @return
     */
    boolean process(RegArimaModel<T> model, TsDomain estimationdomain, TsDomain outliersdomain);

    /**
     *
     * @param pos
     * @param outlier
     * @return
     */
    double T(int pos, int outlier);
 
}

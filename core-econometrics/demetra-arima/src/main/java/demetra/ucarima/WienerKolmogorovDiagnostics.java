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
package demetra.ucarima;

import demetra.arima.ArimaModel;
import demetra.arima.BartlettApproximation;
import demetra.arima.Model;
import demetra.arima.StationaryTransformation;
import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.dstats.Normal;
import demetra.maths.matrices.Matrix;
import demetra.data.DoubleSequence;
import demetra.stats.AutoCovariances;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class WienerKolmogorovDiagnostics {

    private Matrix m_tac, m_eac, m_sdvar;
    private ArimaModel[] m_stcmp;
    private Model[] m_stest;
    private double[][] m_stdata;

    /**
     *
     * @param model
     * @param err Standard error of the model
     * @param data
     * @param cmps Position of the components in the decomposition model. When
     * the signal is considered, the position is positive; when the noise is
     * considered, the position is negative. Position is 1-based !!!
     * @return
     */
    public static WienerKolmogorovDiagnostics make(UcarimaModel model, double err, double[][] data, int[] cmps) {
        int n = cmps.length;
        if (data.length != n) {
            return null;
        }

        WienerKolmogorovDiagnostics diags = new WienerKolmogorovDiagnostics();
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(model);
        // creates stationary models
        double[][] stdata = new double[n][];
        ArimaModel[] stmodels = new ArimaModel[n];
        Model[] emodels = new Model[n];
        int ndata = data[0].length;
        for (int i = 0; i < n; ++i) {
            if (data[i] != null) {
                ArimaModel cur = null;
                boolean signal = cmps[i] > 0;
                int icmp = signal ? (cmps[i] - 1) : (-cmps[i] - 1);
                cur = model.getComponent(icmp);
                if (!cur.isNull()) {
                    if (!signal) {
                        cur = model.getComplement(icmp);
                    }
                    StationaryTransformation stmodel = cur.stationaryTransformation();
                    double[] curst = new double[ndata - stmodel.getUnitRoots().length() - 1];
                    DataBlock out = DataBlock.ofInternal(curst);
                    stmodel.getUnitRoots().apply(DataBlock.ofInternal(data[i]), out);
                    out.sub(out.sum() / out.length());
                    stdata[i] = curst;
                    emodels[i] = (Model) wk.finalStationaryEstimator(icmp, signal).getStationaryModel();
                    stmodels[i] = (ArimaModel) stmodel.getStationaryModel();
                }
            }
        }
        if (diags.test(emodels, err, stdata)) {
            diags.m_stcmp = stmodels;
            diags.m_stest = emodels;
            diags.m_stdata = stdata;
            return diags;
        } else {
            return null;
        }
    }

    private WienerKolmogorovDiagnostics() {
    }

    /**
     *
     * @param stmodels Stationary models
     * @param err
     * @param stdata Stationary data, corrected for the mean
     * @return
     */
    private boolean test(Model[] stmodels, double err, double[][] stdata) {
        try {
            int n = stmodels.length;
            if (n != stdata.length) {
                return false;
            }
            m_eac = Matrix.square(n);
            m_tac = Matrix.square(n);
            m_sdvar = Matrix.square(n);

            for (int i = 0; i < n; ++i) {
                if (stmodels[i] != null) {
                    double[] itmp = stdata[i];
                    double sdvar = BartlettApproximation.standardDeviationOfVariance(stmodels[i], itmp.length);

//                DescriptiveStatistics stats = new DescriptiveStatistics(new ReadDataBlock(itmp));
//                AutoCorrelations ac = new AutoCorrelations(stats);
//                ac.setCorrectedForMean(true);
//                double evar = stats.getVar() / (err * err);
                    double evar = AutoCovariances.varianceNoMissing(DoubleSequence.ofInternal(itmp), 0) / (err * err);
                    double var = stmodels[i].getAutoCovarianceFunction().get(0);
                    m_tac.set(i, i, var);
                    m_eac.set(i, i, evar);
                    m_sdvar.set(i, i, sdvar);

                    for (int j = i + 1; j < n; ++j) {
                        double[] jtmp = stdata[j];
                        if (jtmp != null) {
                            int ni = itmp.length, nj = jtmp.length;
                            int si = 0, sj = 0, nc = 0;
                            if (ni < nj) {
                                nc = ni;
                                sj = nj - ni;
                            } else {
                                nc = nj;
                                si = ni - nj;

                            }
                            BartlettApproximation.CrossCorrelation cbartlett = new BartlettApproximation.CrossCorrelation(stmodels[i], stmodels[j]);
                            double cov = cbartlett.get(0);
                            double sdcov = cbartlett.standardDeviation(0, nc);
                            DoubleSequence di = DoubleSequence.ofInternal(itmp, si, nc);
                            DoubleSequence dj = DoubleSequence.ofInternal(jtmp, sj, nc);
                            double vi = AutoCovariances.varianceNoMissing(di,0);
                            double vj = AutoCovariances.varianceNoMissing(dj,0);
                            double cvij = AutoCovariances.covarianceWithZeroMean(di, dj);
                            double ecov = cvij / Math.sqrt(vi * vj);
                            m_tac.set(i, j, cov);
                            m_tac.set(j, i, cov);
                            m_sdvar.set(i, j, sdcov);
                            m_sdvar.set(j, i, sdcov);
                            m_eac.set(i, j, ecov);
                            m_eac.set(j, i, ecov);
                        }
                    }
                }
            }

            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public double getPValue(int i) {
        if (m_stcmp[i] == null) {
            return Double.NaN;
        }
        Normal dist = new Normal();
        double z = Math.abs(m_tac.get(i, i) - m_eac.get(i, i)) / m_sdvar.get(i, i);
        return 1 - dist.getProbabilityForInterval(-z, z);
    }

    public double getPValue(int i, int j) {
        if (m_stcmp[i] == null || m_stcmp[j] == null) {
            return Double.NaN;
        }
        Normal dist = new Normal();
        double z = Math.abs(m_tac.get(i, j) - m_eac.get(i, j)) / m_sdvar.get(i, j);
        return 1 - dist.getProbabilityForInterval(-z, z);
    }

    public double getEstimatorVariance(int i) {
        if (m_stcmp[i] == null) {
            return Double.NaN;
        }
        return m_tac.get(i, i);
    }

    public double getEstimateVariance(int i) {
        if (m_stcmp[i] == null) {
            return Double.NaN;
        }
        return m_eac.get(i, i);
    }

    public double getEstimatorSDVariance(int i) {
        if (m_stcmp[i] == null) {
            return Double.NaN;
        }
        return m_sdvar.get(i, i);
    }

    public double getEstimatorCrossCorrelation(int i, int j) {
        if (m_stcmp[i] == null || m_stcmp[j] == null) {
            return Double.NaN;
        }
        return m_tac.get(i, j);
    }

    public double getEstimateCrossCorrelation(int i, int j) {
        if (m_stcmp[i] == null || m_stcmp[j] == null) {
            return Double.NaN;
        }
        return m_eac.get(i, j);
    }

    public double getEstimatorSDCrossCorrelation(int i, int j) {
        if (m_stcmp[i] == null || m_stcmp[j] == null) {
            return Double.NaN;
        }
        return m_sdvar.get(i, j);
    }

    public ArimaModel getStationaryComponentModel(int i) {
        return m_stcmp[i];
    }

    public Model getStationaryEstimatorModel(int i) {
        return m_stest[i];
    }

    public DoubleSequence getStationaryEstimate(int i) {
        return DoubleSequence.ofInternal(m_stdata[i]);
    }
}

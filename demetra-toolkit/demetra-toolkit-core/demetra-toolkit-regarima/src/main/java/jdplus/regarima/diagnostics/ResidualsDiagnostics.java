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
package jdplus.regarima.diagnostics;

import jdplus.data.analysis.Periodogram;
import demetra.processing.ProcQuality;
import jdplus.regarima.RegArimaUtility;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.stats.tests.NiidTests;
import java.util.ArrayList;
import java.util.List;
import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import java.util.Collections;
import demetra.processing.Diagnostics;

/**
 *
 * @author Jean Palate
 */
public class ResidualsDiagnostics implements Diagnostics {

    private double N0 = 0.1, N1 = .01;
    private double tdPeriodogram0 = 0.1, tdPeriodogram1 = 0.01, tdPeriodogram2 = 0.001;
    private double sPeriodogram0 = 0.1, sPeriodogram1 = 0.01, sPeriodogram2 = 0.001;
    private final List<String> warnings = new ArrayList<>();

    private NiidTests stats;
    private Periodogram periodogram;
    private int period;

    static ResidualsDiagnostics create(ResidualsDiagnosticsConfiguration config, RegSarimaModel regarima) {
        try {
            return new ResidualsDiagnostics(config, regarima);
        } catch (Exception ex) {
            return null;
        }
    }

    public ResidualsDiagnostics(ResidualsDiagnosticsConfiguration config, RegSarimaModel rslts) {
        sPeriodogram2 = config.getSevereThresholdeForSeasonalPeaks();
        sPeriodogram1 = config.getBadThresholdeForSeasonalPeaks();
        sPeriodogram0 = config.getUncertainThresholdeForSeasonalPeaks();

        tdPeriodogram2 = config.getSevereThresholdForTradingDaysPeak();
        tdPeriodogram1 = config.getBadThresholdForTradingDaysPeak();
        tdPeriodogram0 = config.getUncertainThresholdForTradingDaysPeak();

        N0 = config.getUncertainThresholdForNormality();
        N1 = config.getBadThresholdForNormality();

        testRegarima(rslts);
    }

    private boolean testRegarima(RegSarimaModel regarima) {
        try {
            
            DoubleSeq res = regarima.fullResiduals().getValues();
            period = regarima.getAnnualFrequency();
            stats = RegArimaDiagnostics.residualTests(res, period, regarima.freeArimaParametersCount());
            periodogram = Periodogram.of(res);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String getName() {
        return ResidualsDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return ResidualsDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        double pval;
        switch (test) {
            case ResidualsDiagnosticsFactory.NORMALITY:
                if (stats == null) {
                    return ProcQuality.Undefined;
                }
                StatisticalTest dht = stats.normalityTest();
                if (dht == null) {
                    return ProcQuality.Undefined;
                }
                pval = dht.getPvalue();
                if (pval > N0) {
                    return ProcQuality.Good;
                } else if (pval < N1) {
                    return ProcQuality.Bad;
                } else {
                    return ProcQuality.Uncertain;
                }
            case ResidualsDiagnosticsFactory.INDEPENDENCE:
                if (stats == null) {
                    return ProcQuality.Undefined;
                }
                StatisticalTest lbt = stats.ljungBox();
                if (lbt == null) {
                    return ProcQuality.Undefined;
                }
                pval = lbt.getPvalue();
                if (pval > N0) {
                    return ProcQuality.Good;
                } else if (pval < N1) {
                    return ProcQuality.Bad;
                } else {
                    return ProcQuality.Uncertain;
                }
            case ResidualsDiagnosticsFactory.TD_PEAK: {
                if (periodogram == null) {
                    return ProcQuality.Undefined;
                }
                double[] tdfreqs = Periodogram.getTradingDaysFrequencies(period);
                double[] p = periodogram.getP();
                double xmax = 0;
                double dstep = periodogram.getIntervalInRadians();
                for (int i = 0; i < tdfreqs.length; ++i) {
                    int i0 = (int) (tdfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                    xcur = p[i0 + 1];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
                if (pval < tdPeriodogram2) {
                    return ProcQuality.Severe;
                }
                if (pval < tdPeriodogram1) {
                    return ProcQuality.Bad;
                } else if (pval > tdPeriodogram0) {
                    return ProcQuality.Good;
                } else {
                    return ProcQuality.Uncertain;
                }
            }
            case ResidualsDiagnosticsFactory.S_PEAK: {
                if (periodogram == null) {
                    return ProcQuality.Undefined;
                }
                double[] seasfreqs = new double[(period - 1) / 2];
                // seas freq in radians...
                for (int i = 0; i < seasfreqs.length; ++i) {
                    seasfreqs[i] = (i + 1) * 2 * Math.PI / period;
                }

                double[] p = periodogram.getP();
                double xmax = 0;
                double dstep = periodogram.getIntervalInRadians();
                for (int i = 0; i < seasfreqs.length; ++i) {
                    int i0 = (int) (seasfreqs[i] / dstep);
                    double xcur = p[i0];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                    xcur = p[i0 + 1];
                    if (xcur > xmax) {
                        xmax = xcur;
                    }
                }
                pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
                if (pval < sPeriodogram2) {
                    return ProcQuality.Severe;
                }
                if (pval < sPeriodogram1) {
                    return ProcQuality.Bad;
                } else if (pval > sPeriodogram0) {
                    return ProcQuality.Good;
                } else {
                    return ProcQuality.Uncertain;
                }
            }
            default:
                break;
        }
        return ProcQuality.Undefined;
    }

    @Override
    public double getValue(String test) {
        try {
            double pval = 0;
            switch (test) {
                case ResidualsDiagnosticsFactory.NORMALITY:
                    if (stats != null) {
                        StatisticalTest dht = stats.normalityTest();
                        pval = dht.getPvalue();
                    }
                    break;
                case ResidualsDiagnosticsFactory.INDEPENDENCE:
                    if (stats != null) {
                        StatisticalTest lbt = stats.ljungBox();
                        pval = lbt.getPvalue();
                    }
                    break;
                case ResidualsDiagnosticsFactory.TD_PEAK:
                    if (periodogram != null) {
                        double[] tdfreqs = Periodogram.getTradingDaysFrequencies(period);
                        double[] p = periodogram.getP();
                        double xmax = 0;
                        double dstep = periodogram.getIntervalInRadians();
                        for (int i = 0; i < tdfreqs.length; ++i) {
                            int i0 = (int) (tdfreqs[i] / dstep);
                            double xcur = p[i0];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                            xcur = p[i0 + 1];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                        }
                        pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), tdfreqs.length);
                    }
                    break;
                case ResidualsDiagnosticsFactory.S_PEAK:
                    if (periodogram != null) {
                        double[] seasfreqs = new double[(period - 1) / 2];
                        // seas freq in radians...
                        for (int i = 0; i < seasfreqs.length; ++i) {
                            seasfreqs[i] = (i + 1) * 2 * Math.PI / period;
                        }

                        double[] p = periodogram.getP();
                        double xmax = 0;
                        double dstep = periodogram.getIntervalInRadians();
                        for (int i = 0; i < seasfreqs.length; ++i) {
                            int i0 = (int) (seasfreqs[i] / dstep);
                            double xcur = p[i0];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                            xcur = p[i0 + 1];
                            if (xcur > xmax) {
                                xmax = xcur;
                            }
                        }
                        pval = 1 - Math.pow(1 - Math.exp(-xmax * .5), seasfreqs.length);
                    }
                    break;
                default:
                    break;
            }
            return pval;
        } catch (Exception err) {
            return Double.NaN;
        }
    }

    @Override
    public List<String> getWarnings() {
        return Collections.emptyList();
    }

    public double getNIIDBound(ProcQuality quality) {
        switch (quality) {
            case Bad:
                return N1;
            case Uncertain:
                return N0;
            default:
                return Double.NaN;
        }
    }

    public double getTDPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return tdPeriodogram2;
            case Bad:
                return tdPeriodogram1;
            case Uncertain:
                return tdPeriodogram0;
            default:
                return Double.NaN;
        }
    }

    public double getSPeriodogram(ProcQuality quality) {
        switch (quality) {
            case Severe:
                return sPeriodogram2;
            case Bad:
                return sPeriodogram1;
            case Uncertain:
                return sPeriodogram0;
            default:
                return Double.NaN;
        }
    }
}

/*
 * Copyright 2014 National Bank of Belgium
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
package ec.tstoolkit.arima.special.mixedfrequencies;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationMapping;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.arima.LogForecasts;
import static ec.tstoolkit.modelling.arima.PreprocessingModel.outlierTypes;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.DiffConstant;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableList.ISelector;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
public class MixedFrequenciesModelEstimation implements IProcResults {

    private double logtransform;
    private final SarimaModel arima;
    private final Matrix X, J;
    private final ConcentratedLikelihood likelihood;
    private LikelihoodStatistics statistics;
    private NiidTests tests;
    private final Matrix pcov;
    private final TsVariableList regs;
    private final TsData h0, l0, he, le, si, esi;
    private final TsDomain edomain;
    private final DataType type;
    private final boolean log;

    public MixedFrequenciesModelEstimation(MixedFrequenciesMonitor monitor, DataType type, boolean log) {
        this.type = type;
        X = monitor.getX();
        J = monitor.getJ();
        regs = monitor.getRegression();
        arima = monitor.getArima();
        edomain = monitor.getEstimationDomain();
        h0 = monitor.getHighFreqInput();
        l0 = monitor.getLowFreqInput();
        he = monitor.getHighFreqData();
        le = monitor.getLowFreqData();
        likelihood = monitor.getLikelihood();
        pcov = monitor.getParametersCovariance();
        si = monitor.getInterpolatedSeries();
        esi = monitor.getInterpolationErrors();
        this.log = log;
    }

    public void setLogTransformation(double val) {
        this.logtransform = val;
    }

    public boolean isLog() {
        return log;
    }

    public TsData getHighFreqInput() {
        return h0;
    }

    public TsData getLowFreqInput() {
        return l0;
    }

    public TsData getHighFreqData() {
        return he;
    }

    public TsData getLowFreqData() {
        return le;
    }

    public TsData getInterpolatedSeries(boolean transformed) {
        if (log && !transformed) {
            return si.exp();
        } else if (!log && transformed) {
            return null;
        } else {
            return si;
        }
    }

    public TsData getInterpolationErrors(boolean transformed) {
        if (log && !transformed) {
            TsData esic = esi.clone();
            for (int i = 0; i < esic.getLength(); ++i) {
                esic.set(i, LogForecasts.expStdev(esi.get(i), si.get(i)));
            }
            return esic;
        } else if (!log && transformed) {
            return null;
        } else {
            return esi;
        }
    }

    public ConcentratedLikelihood getLikelihood() {
        return likelihood;
    }

    public LikelihoodStatistics getLikelihoodStatistics() {
        if (statistics == null) {
            int n = le.getObsCount() + he.getObsCount();
            statistics = LikelihoodStatistics.create(likelihood, n - arima.getDifferenceOrder(), arima.getParametersCount(), logtransform);
        }
        return statistics;
    }

    public Matrix getX() {
        return X;
    }

    public Matrix getJ() {
        return J;
    }

    public TsVariableList getRegression() {
        return regs;
    }

    public SarimaModel getArima() {
        return arima;
    }

    public Matrix getParametersCovariance() {
        return pcov;
    }

    private TsData op(TsData l, TsData r) {
        if (log) {
            return TsData.multiply(l, r);
        } else {
            return TsData.add(l, r);
        }
    }

    private TsData inv_op(TsData l, TsData r) {
        if (log) {
            return TsData.divide(l, r);
        } else {
            return TsData.subtract(l, r);
        }
    }

    public TsData linearizedSeries() {
        TsData sregs = regressionEffect(edomain);
        return TsData.subtract(si, sregs);
    }

    public TsData getHighFreqLinearizedSeries() {
        TsData sregs = regressionEffect(h0.getDomain());
        if (sregs != null && log) {
            sregs = sregs.exp();
        }
        return inv_op(h0, sregs);
    }

    public TsData getLowFreqLinearizedSeries() {
        TsDomain ldom = l0.getDomain().changeFrequency(h0.getFrequency(), true);
        TsData sregs = regressionEffect(ldom);
        if (sregs != null) {
            if (type == DataType.Stock) {
                sregs = sregs.changeFrequency(l0.getFrequency(), TsAggregationType.Last, true);
            } else {
                sregs = sregs.changeFrequency(l0.getFrequency(), TsAggregationType.Sum, true);
                if (log) {
                    sregs.apply(x -> x / edomain.getFrequency().ratio(l0.getFrequency()));
                }
            }
        }
        if (sregs != null && log) {
            sregs = sregs.exp();
        }
        return inv_op(l0, sregs);
    }

    public TsData regressionEffect(TsDomain domain) {
        if (likelihood == null) {
            return null;
        }
        double[] coeffs = likelihood.getB();
        if (coeffs == null) {
            return new TsData(domain, 0);
        } else {
            TsVariableSelection sel = regs.select(var -> !(var instanceof DiffConstant)); //To change body of generated methods, choose Tools | Templates.
            DataBlock sum = sel.sum(new DataBlock(coeffs, 0, coeffs.length, 1), domain);

            if (sum == null) {
                sum = new DataBlock(domain.getLength());
            }
            TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
            return rslt;
        }
    }

    // cmp is used in back transformation
    public <T extends ITsVariable> TsData regressionEffect(TsDomain domain, Class<T> tclass) {
        if (likelihood == null) {
            return null;
        }
        double[] coeffs = likelihood.getB();
        if (coeffs == null) {
            return new TsData(domain, 0);
        }
        TsVariableSelection sel = regs.selectCompatible(tclass);
        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }

        DataBlock sum = sel.sum(new DataBlock(coeffs, 0, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData regressionEffect(TsDomain domain, final ComponentType type) {
        return regressionEffect(domain, var -> !(var instanceof DiffConstant) && DeterministicComponent.getType(var) == type);
    }

    private TsDomain fdomain() {
        return new TsDomain(edomain.getEnd(), edomain.getFrequency().intValue());
    }

    private TsData regressionEffect(TsDomain domain, Predicate<ITsVariable> selector) {
        if (likelihood == null) {
            return null;
        }
        if (regs.isEmpty()) {
            return new TsData(domain, 0);
        }
        TsVariableSelection<ITsVariable> sel = regs.select(selector);
        if (sel.isEmpty()) {
            return new TsData(domain, 0);
        }
        double[] coeffs = likelihood.getB();

        DataBlock sum = sel.sum(new DataBlock(coeffs, 0, coeffs.length, 1), domain);

        if (sum == null) {
            sum = new DataBlock(domain.getLength());
        }
        TsData rslt = new TsData(domain.getStart(), sum.getData(), false);
        return rslt;
    }

    public TsData tradingDaysEffect(TsDomain domain) {
        return regressionEffect(domain, ICalendarVariable.class);
    }

    public TsData movingHolidaysEffect(TsDomain domain) {
        return regressionEffect(domain, IMovingHolidayVariable.class);
    }

    public TsData outliersEffect(TsDomain domain) {
        return regressionEffect(domain, IOutlierVariable.class);
    }

    public TsData outliersEffect(TsDomain domain, final ComponentType type) {
        if (likelihood == null) {
            return null;
        }
        if (type == ComponentType.Undefined) {
            return outliersEffect(domain);
        }
        OutlierType[] types = outlierTypes(type);
        TsData rslt = null;
        for (int i = 0; i < types.length; ++i) {
            TsVariableSelection sel = regs.select(types[i]);
            if (!sel.isEmpty()) {
                double[] coeffs = likelihood.getB();
                DataBlock sum = sel.sum(new DataBlock(coeffs, 0, coeffs.length, 1), domain);

                if (sum != null) {
                    rslt = TsData.add(rslt, new TsData(domain.getStart(), sum.getData(), false));
                }
            }
        }
        return rslt;

    }

    public List<TsData> regressors(TsDomain domain) {
        ArrayList<TsData> vregs = new ArrayList<>();
        List<DataBlock> data = regs.all().data(domain);
        for (DataBlock d : data) {
            double[] cur = new double[domain.getLength()];
            d.copyTo(cur, 0);
            vregs.add(new TsData(domain.getStart(), cur, false));
        }
        return vregs;
    }

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    @Override
    public Map<String, Class> getDictionary(boolean compact) {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, compact);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return MAPPING.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        return MAPPING.contains(id);
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    public static final String LIKELIHOOD = "likelihood",
            NP = "np",
            NEFFOBS = "neffectiveobs",
            ADJLVAL = "adjustedlogvalue",
            LVAL = "logvalue",
            SSQERR = "ssqerr",
            SER = "ser",
            SERML = "ser-ml",
            AIC = "aic",
            AICC = "aicc",
            BIC = "bic",
            BICC = "bicc",
            RESIDUALS = "residuals",
            RES_DATA = "res",
            RES_STDERR = "stderr",
            RES_MEAN = "mean",
            RES_SKEWNESS = "skewness",
            RES_KURTOSIS = "kurtosis",
            RES_DH = "dh",
            RES_LB = "lb",
            RES_LB2 = "lb2",
            RES_SEASLB = "seaslb",
            RES_BP = "bp",
            RES_BP2 = "bp2",
            RES_SEASBP = "seasbp",
            RES_UD_NUMBER = "nruns",
            RES_UD_LENGTH = "lruns",
            ARIMA = "arima",
            ARIMA_COVAR = "covar",
            ARIMA_MEAN = "mean",
            ARIMA_P = "p",
            ARIMA_D = "d",
            ARIMA_Q = "q",
            ARIMA_BP = "bp",
            ARIMA_BD = "bd",
            ARIMA_BQ = "bq",
            ARIMA_PHI = "phi", ARIMA_PHI1 = "phi(1)", ARIMA_PHI2 = "phi(2)", ARIMA_PHI3 = "phi(3)", ARIMA_PHI4 = "phi(4)",
            ARIMA_TH = "th", ARIMA_TH1 = "th(1)", ARIMA_TH2 = "th(2)", ARIMA_TH3 = "th(3)", ARIMA_TH4 = "th(4)",
            ARIMA_BPHI = "bphi", ARIMA_BPHI1 = "bphi(1)", ARIMA_BTH = "bth", ARIMA_BTH1 = "bth(1)",
            YC = "yc", YC_E = "yc_e", YC_L = "yc_orig", YC_L_E = "yc_orig_e";

    // MAPPING
    public static InformationMapping<MixedFrequenciesModelEstimation> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<MixedFrequenciesModelEstimation, T> extractor) {
        MAPPING.set(name, tclass, extractor);
    }

    public static <T> void setTsData(String name, Function<MixedFrequenciesModelEstimation, TsData> extractor) {
        MAPPING.set(name, extractor);
    }

    private static final InformationMapping<MixedFrequenciesModelEstimation> MAPPING = new InformationMapping<>(MixedFrequenciesModelEstimation.class);

    private static Parameter param(MixedFrequenciesModelEstimation source, String name, int lag) {
        SarimaModel arima = source.getArima();
        int pos = -1;
        switch (name) {
            case ARIMA_PHI:
                pos = arima.getPhiPosition(lag);
                break;
            case ARIMA_BPHI:
                pos = arima.getBPhiPosition(lag);
                break;
            case ARIMA_TH:
                pos = arima.getThetaPosition(lag);
                break;
            case ARIMA_BTH:
                pos = arima.getBThetaPosition(lag);
                break;
            default:
                break;
        }
        if (pos < 0) {
            return null;
        }
        double err = source.pcov == null ? 0 : Math.sqrt(source.pcov.get(pos, pos));
        Parameter p = new Parameter(arima.getParameter(pos), err == 0 ? ParameterType.Fixed : ParameterType.Estimated);
        p.setStde(err);
        return p;
    }

    static {
        // Series
        MAPPING.set(YC, source -> source.getInterpolatedSeries(false));
        MAPPING.set(YC_E, source -> source.getInterpolationErrors(false));
        MAPPING.set(YC_L, source -> source.getInterpolatedSeries(true));
        MAPPING.set(YC_L_E, source -> source.getInterpolationErrors(true));
        MAPPING.set(ModellingDictionary.Y_LIN, source -> {
            TsData s = source.linearizedSeries();
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.CAL, source -> {
            TsData s = source.regressionEffect(source.edomain, ComponentType.CalendarEffect);
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.DET, source -> {
            TsData s = source.regressionEffect(source.edomain);
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.TDE, source -> {
            TsData s = source.tradingDaysEffect(source.edomain);
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.EE, source -> {
            TsData s = source.movingHolidaysEffect(source.edomain);
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, source -> {
            TsData s = source.regressionEffect(source.fdomain(), ComponentType.CalendarEffect);
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, source -> {
            TsData s = source.regressionEffect(source.fdomain());
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, source -> {
            TsData s = source.tradingDaysEffect(source.fdomain());
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });
        MAPPING.set(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, source -> {
            TsData s = source.movingHolidaysEffect(source.fdomain());
            if (s == null) {
                return null;
            }
            if (source.log) {
                return s.exp();
            } else {
                return s;
            }
        });

        // Likelihood
        MAPPING.set(InformationSet.item(LIKELIHOOD, NEFFOBS), Integer.class,
                source -> source.statistics.effectiveObservationsCount);
        MAPPING.set(InformationSet.item(LIKELIHOOD, NP), Integer.class,
                source -> source.statistics.estimatedParametersCount);
        MAPPING.set(InformationSet.item(LIKELIHOOD, LVAL), Double.class,
                source -> source.statistics.logLikelihood);
        MAPPING.set(InformationSet.item(LIKELIHOOD, ADJLVAL), Double.class,
                source -> source.statistics.adjustedLogLikelihood);
        MAPPING.set(InformationSet.item(LIKELIHOOD, SSQERR), Double.class,
                source -> source.statistics.SsqErr);
        MAPPING.set(InformationSet.item(LIKELIHOOD, AIC), Double.class,
                source -> source.statistics.AIC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, AICC), Double.class,
                source -> source.statistics.AICC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, BIC), Double.class,
                source -> source.statistics.BIC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, BICC), Double.class,
                source -> source.statistics.BICC);
        // RESIDUALS
        MAPPING.set(InformationSet.item(RESIDUALS, SER), Double.class,
                source -> Math.sqrt(source.statistics.SsqErr
                        / (source.statistics.effectiveObservationsCount - source.statistics.estimatedParametersCount + 1)));
        MAPPING.set(InformationSet.item(RESIDUALS, SERML), Double.class,
                source -> Math.sqrt(source.statistics.SsqErr
                        / (source.statistics.effectiveObservationsCount)));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_DATA), double[].class,
                source -> source.likelihood.getResiduals());
        // ARIMA
        MAPPING.set(ARIMA, SarimaModel.class, source -> source.arima);
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_P), Integer.class,
                source -> source.arima.getRegularAROrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_D), Integer.class,
                source -> source.arima.getRegularDifferenceOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_Q), Integer.class,
                source -> source.arima.getRegularMAOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BP), Integer.class,
                source -> source.arima.getSeasonalAROrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BD), Integer.class,
                source -> source.arima.getSeasonalDifferenceOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BQ), Integer.class,
                source -> source.arima.getSeasonalMAOrder());

        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_PHI), 1, 5, Parameter.class,
                (source, i) -> param(source, ARIMA_PHI, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_BPHI), 1, 2, Parameter.class,
                (source, i) -> param(source, ARIMA_BPHI, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_TH), 1, 5, Parameter.class,
                (source, i) -> param(source, ARIMA_TH, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_BTH), 1, 2, Parameter.class,
                (source, i) -> param(source, ARIMA_BTH, i));

        MAPPING.set(InformationSet.item(ARIMA, ARIMA_COVAR), Matrix.class, source -> source.pcov);
    }
}

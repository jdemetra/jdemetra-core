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
import ec.tstoolkit.eco.CoefficientEstimation;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.modelling.UserVariable;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.LogForecasts;
import static ec.tstoolkit.modelling.arima.PreprocessingModel.outlierComponent;
import static ec.tstoolkit.modelling.arima.PreprocessingModel.outlierTypes;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.DataType;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.regression.DiffConstant;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsModifier;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableList.ISelector;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                    sregs.getValues().div(edomain.getFrequency().ratio(l0.getFrequency()));
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
            TsVariableSelection sel = regs.select(new ISelector() {
                @Override
                public boolean accept(ITsVariable var) {
                    return !(var instanceof DiffConstant); //To change body of generated methods, choose Tools | Templates.
                }
            });
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
        return regressionEffect(domain, new TsVariableList.ISelector() {

            @Override
            public boolean accept(ITsVariable var) {
                return ! (var instanceof DiffConstant) && DeterministicComponent.getType(var) == type;
            }
        });
    }

    private TsDomain fdomain() {
        return new TsDomain(edomain.getEnd(), edomain.getFrequency().intValue());
    }

    private TsData regressionEffect(TsDomain domain, TsVariableList.ISelector selector) {
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

    public static void fillDictionary(String prefix, Map<String, Class> map) {
        mapper.fillDictionary(prefix, map);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        return mapper.getData(this, id, tclass);
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
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
//    private static final String[] LIKELIHOOD_DICTIONARY = {
//        LVAL, AIC, AICC, BIC, BICC
//    };
//    private static final String[] RES_TEST_DICTIONARY = {
//        RES_MEAN,
//        RES_SKEWNESS, RES_KURTOSIS, RES_DH,
//        RES_LB, RES_LB2, RES_SEASLB,
//        RES_BP, RES_BP2, RES_SEASBP
//    };

    public static <T> void addMapping(String name, InformationMapper.Mapper<MixedFrequenciesModelEstimation, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }

    private static final InformationMapper<MixedFrequenciesModelEstimation> mapper = new InformationMapper<>();

    private static class ArimaMapper extends InformationMapper.Mapper<MixedFrequenciesModelEstimation, Parameter> {

        private final String name_;
        private final int lag_;

        ArimaMapper(String name, int lag) {
            super(Parameter.class);
            name_ = name;
            lag_ = lag;
        }

        @Override
        public Parameter retrieve(MixedFrequenciesModelEstimation source) {
            SarimaModel arima = source.getArima();
            int pos = -1;
            if (name_.equals(ARIMA_PHI)) {
                pos = arima.getPhiPosition(lag_);
            } else if (name_.equals(ARIMA_BPHI)) {
                pos = arima.getBPhiPosition(lag_);
            } else if (name_.equals(ARIMA_TH)) {
                pos = arima.getThetaPosition(lag_);
            } else if (name_.equals(ARIMA_BTH)) {
                pos = arima.getBThetaPosition(lag_);
            }
            if (pos < 0) {
                return null;
            }
            double err = source.pcov == null ? 0 : Math.sqrt(source.pcov.get(pos, pos));
            Parameter p = new Parameter(arima.getParameter(pos), err == 0 ? ParameterType.Fixed : ParameterType.Estimated);
            p.setStde(err);
            return p;
        }
    }
    // fill the mapper

    static {
        // Series
        mapper.add(YC, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                return source.getInterpolatedSeries(false);
            }
        });
        mapper.add(YC_E, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                return source.getInterpolationErrors(false);
            }
        });
        // Series
        mapper.add(YC_L, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                return source.getInterpolatedSeries(true);
            }
        });
        // Series
        mapper.add(YC_L_E, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                return source.getInterpolationErrors(true);
            }
        });
        mapper.add(ModellingDictionary.Y_LIN, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.linearizedSeries();
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.CAL, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.regressionEffect(source.edomain, ComponentType.CalendarEffect);
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.DET, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.regressionEffect(source.edomain);
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.TDE, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.tradingDaysEffect(source.edomain);
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.EE, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.movingHolidaysEffect(source.edomain);
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.CAL + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.regressionEffect(source.fdomain(), ComponentType.CalendarEffect);
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.DET + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.regressionEffect(source.fdomain());
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.TDE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.tradingDaysEffect(source.fdomain());
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });
        mapper.add(ModellingDictionary.EE + SeriesInfo.F_SUFFIX, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, TsData>(TsData.class) {
            @Override
            public TsData retrieve(MixedFrequenciesModelEstimation source) {
                TsData s = source.movingHolidaysEffect(source.fdomain());
                if (s == null) {
                    return null;
                }
                if (source.log) {
                    return s.exp();
                } else {
                    return s;
                }
            }
        });

        // Likelihood
        mapper.add(InformationSet.item(LIKELIHOOD, NEFFOBS), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.effectiveObservationsCount;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, NP), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.estimatedParametersCount;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, LVAL), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.logLikelihood;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, ADJLVAL), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.adjustedLogLikelihood;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, SSQERR), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.SsqErr;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, AIC), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.AIC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, AICC), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.AICC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, BIC), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.BIC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, BICC), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return source.statistics.BICC;
            }
        });

        // RESIDUALS
        mapper.add(InformationSet.item(RESIDUALS, SER), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return Math.sqrt(source.statistics.SsqErr
                        / (source.statistics.effectiveObservationsCount - source.statistics.estimatedParametersCount + 1));
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, SERML), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(MixedFrequenciesModelEstimation source) {
                return Math.sqrt(source.statistics.SsqErr
                        / (source.statistics.effectiveObservationsCount));
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_DATA), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, double[]>(double[].class) {

            @Override
            public double[] retrieve(MixedFrequenciesModelEstimation source) {
                return source.likelihood.getResiduals();
            }
        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_MEAN), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getMeanTest());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_SKEWNESS), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getSkewness());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_KURTOSIS), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getKurtosis());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_DH), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getNormalityTest());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_LB), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getLjungBox());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_LB2), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getLjungBoxOnSquare());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_SEASLB), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getSeasonalLjungBox());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_BP), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getBoxPierce());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_BP2), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getBoxPierceOnSquare());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_SEASBP), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                return StatisticalTest.create(source.getNiidTests().getSeasonalBoxPierce());
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_UD_NUMBER), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
//                ud.setKind(RunsTestKind.Number);
//                return StatisticalTest.create(ud);
//            }
//        });
//        mapper.add(InformationSet.item(RESIDUALS, RES_UD_LENGTH), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, StatisticalTest>(StatisticalTest.class) {
//
//            @Override
//            public StatisticalTest retrieve(MixedFrequenciesModelEstimation source) {
//                TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
//                ud.setKind(RunsTestKind.Length);
//                return StatisticalTest.create(ud);
//            }
//        });

        // ARIMA
        mapper.add(ARIMA, new InformationMapper.Mapper<MixedFrequenciesModelEstimation, SarimaModel>(SarimaModel.class) {

            @Override
            public SarimaModel retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima;
            }
        });

//        mapper.add(InformationSet.item(ARIMA, ARIMA_MEAN), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Boolean>(Boolean.class) {
//
//            @Override
//            public Boolean retrieve(MixedFrequenciesModelEstimation source) {
//                return source.model_.isMeanCorrection();
//            }
//        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_P), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getRegularAROrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_D), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getRegularDifferenceOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_Q), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getRegularMAOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BP), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getSeasonalAROrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BD), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getSeasonalDifferenceOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BQ), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(MixedFrequenciesModelEstimation source) {
                return source.arima.getSeasonalMAOrder();
            }
        });

        mapper.add(InformationSet.item(ARIMA, ARIMA_PHI1), new ArimaMapper(ARIMA_PHI, 1));
        mapper.add(InformationSet.item(ARIMA, ARIMA_PHI2), new ArimaMapper(ARIMA_PHI, 2));
        mapper.add(InformationSet.item(ARIMA, ARIMA_PHI3), new ArimaMapper(ARIMA_PHI, 3));
        mapper.add(InformationSet.item(ARIMA, ARIMA_PHI4), new ArimaMapper(ARIMA_PHI, 4));
        mapper.add(InformationSet.item(ARIMA, ARIMA_BPHI1), new ArimaMapper(ARIMA_BPHI, 1));
        mapper.add(InformationSet.item(ARIMA, ARIMA_TH1), new ArimaMapper(ARIMA_TH, 1));
        mapper.add(InformationSet.item(ARIMA, ARIMA_TH2), new ArimaMapper(ARIMA_TH, 2));
        mapper.add(InformationSet.item(ARIMA, ARIMA_TH3), new ArimaMapper(ARIMA_TH, 3));
        mapper.add(InformationSet.item(ARIMA, ARIMA_TH4), new ArimaMapper(ARIMA_TH, 4));
        mapper.add(InformationSet.item(ARIMA, ARIMA_BTH1), new ArimaMapper(ARIMA_BTH, 1));

        mapper.add(InformationSet.item(ARIMA, ARIMA_COVAR), new InformationMapper.Mapper<MixedFrequenciesModelEstimation, Matrix>(Matrix.class) {

            @Override
            public Matrix retrieve(MixedFrequenciesModelEstimation source) {
                return source.pcov;
            }
        });
    }
}

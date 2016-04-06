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

import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.arima.estimation.*;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.stats.RunsTestKind;
import ec.tstoolkit.stats.TestofUpDownRuns;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ModelEstimation implements IProcResults {

    private double logtransform_;
    private RegArimaModel<SarimaModel> model_;
    private ConcentratedLikelihood likelihood_;
    private LikelihoodStatistics statistics_;
    private NiidTests tests_;
    private Matrix pcov_;

    public ModelEstimation(RegArimaModel<SarimaModel> model) {
        model_ = model;
    }

    public ModelEstimation(RegArimaModel<SarimaModel> model, double transformCorrection) {
        model_ = model;
        logtransform_ = transformCorrection;
    }

    /**
     * Data corrected for regression effects (except mean effect)
     *
     * @return
     */
    public DataBlock getLinearizedData() {
        DataBlock res = model_.getY().deepClone();
        double[] b = likelihood_.getB();
        if (b == null) {
            return res;
        }

        // handle missing values:
        int start = model_.isMeanCorrection() ? 1 : 0;
        int[] missings = model_.getMissings();
        if (missings != null) {
            for (int i = 0; i < missings.length; ++i) {
                res.add(missings[i], -b[start + i]);
            }
            start += missings.length;
        }
        if (b != null) {
            for (int i = start; i < b.length; ++i) {
                res.addAY(-b[i], model_.X(i - start));
            }
        }
        return res;
    }

    public DataBlock getCorrectedData(int start, int end) {
        DataBlock res = model_.getY().deepClone();
        if (start == end) {
            return res;
        }
        double[] b = likelihood_.getB();
        if (b != null) {
            int del = model_.isMeanCorrection() ? 1 : 0;
            for (int i = start; i < end; ++i) {
                res.addAY(-b[i + del], model_.X(i));
            }
        }
        return res;
    }

    /**
     *
     * @return
     */
    public DataBlock getOlsResiduals() {
        RegModel dmodel = model_.getDModel();
        DataBlock yc = dmodel.getY();
        if (dmodel.getVarsCount() > 0) {
            Ols ols = new Ols();
            if (ols.process(dmodel)) {
                yc = dmodel.calcRes(new ReadDataBlock(ols.getLikelihood().getB()));
            }
        }
        return yc;
    }

    public DataBlock getFullResiduals() {
        RegArimaEstimation<SarimaModel> tmp =
                new RegArimaEstimation<>(model_, likelihood_);
        return new DataBlock(tmp.fullResiduals());
    }

    public boolean computeLikelihood(int nhp) {
        ConcentratedLikelihoodEstimation estimation = new ConcentratedLikelihoodEstimation();
        if (estimation.estimate(model_)) {
            likelihood_ = estimation.getLikelihood();
            statistics_ = new RegArimaEstimation<>(model_, likelihood_).statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        }
        else {
            return false;
        }
    }
    
    public boolean compute(IRegArimaProcessor<SarimaModel> monitor, int nhp) {
        if (likelihood_ != null) {
            return true;
        }
        RegArimaEstimation<SarimaModel> estimation = monitor.process(model_) ;
        if (estimation != null) {
            model_ = estimation.model;
            likelihood_ = estimation.likelihood;
            statistics_ = estimation.statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        }
        else {
            return false;
        }
    }

    public boolean improve(IRegArimaProcessor<SarimaModel> monitor, int nhp) {
//        if (likelihood_ != null) {
//            return true;
//        }
        RegArimaEstimation<SarimaModel> estimation = monitor.optimize(model_) ;
        if (estimation != null) {
            model_ = estimation.model;
            likelihood_ = estimation.likelihood;
            statistics_ = estimation.statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        }
        else {
            return false;
        }
    }

    public void updateParametersCovariance(Matrix pvar) {
        pcov_ = pvar;
    }

    public ConcentratedLikelihood getLikelihood() {
        return likelihood_;
    }

    public LikelihoodStatistics getStatistics() {
        return statistics_;
    }

    public NiidTests getNiidTests() {
        if (tests_ == null && likelihood_ != null) {
            tests_ = new NiidTests(getFullResiduals(), model_.getArima().getFrequency(),
                    calcFreeParameters(), true);
        }
        return tests_;
    }

    private int calcFreeParameters() {
        if (pcov_ == null) {
            return 0;
        }
        DataBlock diag = pcov_.diagonal();
        int n = 0;
        for (int i = 0; i < diag.getLength(); ++i) {
            if (diag.get(i) > 0) {
                ++n;
            }
        }
        return n;
    }

    public RegArimaModel<SarimaModel> getRegArima() {
        return model_;
    }

    public SarimaModel getArima() {
        return model_.getArima();
    }

    public Matrix getParametersCovariance() {
        return pcov_;
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
            ARIMA_BPHI = "bphi", ARIMA_BPHI1 = "bphi(1)", ARIMA_BTH = "bth", ARIMA_BTH1 = "bth(1)";
//    private static final String[] LIKELIHOOD_DICTIONARY = {
//        LVAL, AIC, AICC, BIC, BICC
//    };
//    private static final String[] RES_TEST_DICTIONARY = {
//        RES_MEAN,
//        RES_SKEWNESS, RES_KURTOSIS, RES_DH,
//        RES_LB, RES_LB2, RES_SEASLB,
//        RES_BP, RES_BP2, RES_SEASBP
//    };

    public static <T> void addMapping(String name, InformationMapper.Mapper<ModelEstimation, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }
    private static final InformationMapper<ModelEstimation> mapper = new InformationMapper<>();

    private static class ArimaMapper extends InformationMapper.Mapper<ModelEstimation, Parameter> {

        private final String name_;
        private final int lag_;

        ArimaMapper(String name, int lag) {
            super(Parameter.class);
            name_ = name;
            lag_ = lag;
        }

        @Override
        public Parameter retrieve(ModelEstimation source) {
            SarimaModel arima = source.getArima();
            int pos = -1;
            switch (name_) {
                case ARIMA_PHI:
                    pos = arima.getPhiPosition(lag_);
                    break;
                case ARIMA_BPHI:
                    pos = arima.getBPhiPosition(lag_);
                    break;
                case ARIMA_TH:
                    pos = arima.getThetaPosition(lag_);
                    break;
                case ARIMA_BTH:
                    pos = arima.getBThetaPosition(lag_);
                    break;
                default:
                    break;
            }
            if (pos < 0) {
                return null;
            }
            double err = source.pcov_ == null ? 0 : Math.sqrt(source.pcov_.get(pos, pos));
            Parameter p = new Parameter(arima.getParameter(pos), err == 0 ? ParameterType.Fixed : ParameterType.Estimated);
            p.setStde(err);
            return p;
        }
    }
    // fill the mapper

    static {

        // Likelihood
        mapper.add(InformationSet.item(LIKELIHOOD, NEFFOBS), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.statistics_.effectiveObservationsCount;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, NP), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.statistics_.estimatedParametersCount;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, LVAL), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.logLikelihood;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, ADJLVAL), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.adjustedLogLikelihood;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, SSQERR), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.SsqErr;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, AIC), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.AIC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, AICC), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.AICC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, BIC), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.BIC;
            }
        });
        mapper.add(InformationSet.item(LIKELIHOOD, BICC), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return source.statistics_.BICC;
            }
        });

        // RESIDUALS
        mapper.add(InformationSet.item(RESIDUALS, SER), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return Math.sqrt(source.statistics_.SsqErr / 
                       (source.statistics_.effectiveObservationsCount-source.statistics_.estimatedParametersCount+1));
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, SERML), new InformationMapper.Mapper<ModelEstimation, Double>(Double.class) {

            @Override
            public Double retrieve(ModelEstimation source) {
                return Math.sqrt(source.statistics_.SsqErr / 
                       (source.statistics_.effectiveObservationsCount));
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_DATA), new InformationMapper.Mapper<ModelEstimation, double[]>(double[].class) {

            @Override
            public double[] retrieve(ModelEstimation source) {
                return source.likelihood_.getResiduals();
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_MEAN), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getMeanTest());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_SKEWNESS), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getSkewness());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_KURTOSIS), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getKurtosis());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_DH), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getNormalityTest());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_LB), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getLjungBox());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_LB2), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getLjungBoxOnSquare());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_SEASLB), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getSeasonalLjungBox());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_BP), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getBoxPierce());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_BP2), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getBoxPierceOnSquare());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_SEASBP), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                return StatisticalTest.create(source.getNiidTests().getSeasonalBoxPierce());
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_UD_NUMBER), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
                ud.setKind(RunsTestKind.Number);
                return StatisticalTest.create(ud);
            }
        });
        mapper.add(InformationSet.item(RESIDUALS, RES_UD_LENGTH), new InformationMapper.Mapper<ModelEstimation, StatisticalTest>(StatisticalTest.class) {

            @Override
            public StatisticalTest retrieve(ModelEstimation source) {
                TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
                ud.setKind(RunsTestKind.Length);
                return StatisticalTest.create(ud);
            }
        });

        // ARIMA
        mapper.add(ARIMA, new InformationMapper.Mapper<ModelEstimation, SarimaModel>(SarimaModel.class) {

            @Override
            public SarimaModel retrieve(ModelEstimation source) {
                return source.model_.getArima();
            }
        });

        mapper.add(InformationSet.item(ARIMA, ARIMA_MEAN), new InformationMapper.Mapper<ModelEstimation, Boolean>(Boolean.class) {

            @Override
            public Boolean retrieve(ModelEstimation source) {
                return source.model_.isMeanCorrection();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_P), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getRegularAROrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_D), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getRegularDifferenceOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_Q), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getRegularMAOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BP), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getSeasonalAROrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BD), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getSeasonalDifferenceOrder();
            }
        });
        mapper.add(InformationSet.item(ARIMA, ARIMA_BQ), new InformationMapper.Mapper<ModelEstimation, Integer>(Integer.class) {

            @Override
            public Integer retrieve(ModelEstimation source) {
                return source.model_.getArima().getSeasonalMAOrder();
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

        mapper.add(InformationSet.item(ARIMA, ARIMA_COVAR), new InformationMapper.Mapper<ModelEstimation, Matrix>(Matrix.class) {

            @Override
            public Matrix retrieve(ModelEstimation source) {
                return source.pcov_;
            }
        });
    }
}

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
import ec.tstoolkit.information.InformationMapping;
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
import java.util.function.Function;

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
        RegArimaEstimation<SarimaModel> tmp
                = new RegArimaEstimation<>(model_, likelihood_);
        return new DataBlock(tmp.fullResiduals());
    }

    public boolean computeLikelihood(int nhp) {
        ConcentratedLikelihoodEstimation estimation = new ConcentratedLikelihoodEstimation();
        if (estimation.estimate(model_)) {
            likelihood_ = estimation.getLikelihood();
            statistics_ = new RegArimaEstimation<>(model_, likelihood_).statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean compute(IRegArimaProcessor<SarimaModel> monitor, int nhp) {
        if (likelihood_ != null) {
            return true;
        }
        RegArimaEstimation<SarimaModel> estimation = monitor.process(model_);
        if (estimation != null) {
            model_ = estimation.model;
            likelihood_ = estimation.likelihood;
            statistics_ = estimation.statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean improve(IRegArimaProcessor<SarimaModel> monitor, int nhp) {
//        if (likelihood_ != null) {
//            return true;
//        }
        RegArimaEstimation<SarimaModel> estimation = monitor.optimize(model_);
        if (estimation != null) {
            model_ = estimation.model;
            likelihood_ = estimation.likelihood;
            statistics_ = estimation.statistics(nhp, logtransform_);
            tests_ = null;
            return true;
        } else {
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

    public static void fillDictionary(String prefix, Map<String, Class> map, boolean compact) {
        MAPPING.fillDictionary(prefix, map, compact);
    }

    @Override
    public Map<String, Class> getDictionary() {
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        fillDictionary(null, map, false);
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
            ARIMA_BPHI = "bphi", ARIMA_BPHI1 = "bphi(1)", ARIMA_BTH = "bth", ARIMA_BTH1 = "bth(1)";

    // MAPPING
    public static InformationMapping<ModelEstimation> getMapping() {
        return MAPPING;
    }

    public static <T> void setMapping(String name, Class<T> tclass, Function<ModelEstimation, T> extractor) {
        synchronized (MAPPING) {
            MAPPING.set(name, tclass, extractor);
        }
    }

    private static final InformationMapping<ModelEstimation> MAPPING = new InformationMapping<>(ModelEstimation.class);

    private static Parameter param(ModelEstimation source, String name, int lag) {
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
        double err = source.pcov_ == null ? 0 : Math.sqrt(source.pcov_.get(pos, pos));
        Parameter p = new Parameter(arima.getParameter(pos), err == 0 ? ParameterType.Fixed : ParameterType.Estimated);
        p.setStde(err);
        return p;
    }

    static {

        // Likelihood
        MAPPING.set(InformationSet.item(LIKELIHOOD, NEFFOBS), Integer.class, source -> source.statistics_.effectiveObservationsCount);
        MAPPING.set(InformationSet.item(LIKELIHOOD, NP), Integer.class, source -> source.statistics_.estimatedParametersCount);
        MAPPING.set(InformationSet.item(LIKELIHOOD, LVAL), Double.class, source -> source.statistics_.logLikelihood);
        MAPPING.set(InformationSet.item(LIKELIHOOD, ADJLVAL), Double.class, source -> source.statistics_.adjustedLogLikelihood);
        MAPPING.set(InformationSet.item(LIKELIHOOD, SSQERR), Double.class, source -> source.statistics_.SsqErr);
        MAPPING.set(InformationSet.item(LIKELIHOOD, AIC), Double.class, source -> source.statistics_.AIC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, AICC), Double.class, source -> source.statistics_.AICC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, BIC), Double.class, source -> source.statistics_.BIC);
        MAPPING.set(InformationSet.item(LIKELIHOOD, BICC), Double.class, source -> source.statistics_.BICC);
        MAPPING.set(InformationSet.item(RESIDUALS, SER), Double.class,
                source -> Math.sqrt(source.statistics_.SsqErr / (source.statistics_.effectiveObservationsCount - source.statistics_.estimatedParametersCount + 1)));
        MAPPING.set(InformationSet.item(RESIDUALS, SERML), Double.class,
                source -> Math.sqrt(source.statistics_.SsqErr / (source.statistics_.effectiveObservationsCount)));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_DATA), double[].class, source -> source.likelihood_.getResiduals());
        MAPPING.set(InformationSet.item(RESIDUALS, RES_MEAN), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getMeanTest()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_SKEWNESS), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getSkewness()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_KURTOSIS), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getKurtosis()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_DH), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getNormalityTest()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_LB), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getLjungBox()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_LB2), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getLjungBoxOnSquare()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_SEASLB), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getSeasonalLjungBox()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_BP), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getBoxPierce()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_BP2), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getBoxPierceOnSquare()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_SEASBP), StatisticalTest.class,
                source -> StatisticalTest.create(source.getNiidTests().getSeasonalBoxPierce()));
        MAPPING.set(InformationSet.item(RESIDUALS, RES_UD_NUMBER), StatisticalTest.class,
                source -> {
                    TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
                    ud.setKind(RunsTestKind.Number);
                    return StatisticalTest.create(ud);
                });
        MAPPING.set(InformationSet.item(RESIDUALS, RES_UD_LENGTH), StatisticalTest.class,
                source -> {
                    TestofUpDownRuns ud = source.getNiidTests().getUpAndDownRuns();
                    ud.setKind(RunsTestKind.Length);
                    return StatisticalTest.create(ud);
                });

        // ARIMA
        MAPPING.set(ARIMA, SarimaModel.class, source -> source.model_.getArima());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_MEAN), Boolean.class, source -> source.model_.isMeanCorrection());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_P), Integer.class, source -> source.model_.getArima().getRegularAROrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_D), Integer.class, source -> source.model_.getArima().getRegularDifferenceOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_Q), Integer.class, source -> source.model_.getArima().getRegularMAOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BP), Integer.class, source -> source.model_.getArima().getSeasonalAROrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BD), Integer.class, source -> source.model_.getArima().getSeasonalDifferenceOrder());
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_BQ), Integer.class, source -> source.model_.getArima().getSeasonalMAOrder());
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_PHI), 1, 5, Parameter.class,
                (source, i) -> param(source, ARIMA_PHI, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_BPHI), 1, 2, Parameter.class,
                (source, i) -> param(source, ARIMA_BPHI, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_TH), 1, 5, Parameter.class,
                (source, i) -> param(source, ARIMA_TH, i));
        MAPPING.setList(InformationSet.item(ARIMA, ARIMA_BTH), 1, 2, Parameter.class,
                (source, i) -> param(source, ARIMA_BTH, i));
        MAPPING.set(InformationSet.item(ARIMA, ARIMA_COVAR), Matrix.class, source -> source.pcov_);
    }
}

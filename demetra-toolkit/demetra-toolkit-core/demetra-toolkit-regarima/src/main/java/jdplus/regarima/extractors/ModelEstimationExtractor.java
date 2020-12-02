/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.modelling.ModellingDictionary;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import jdplus.modelling.extractors.SarimaExtractor;
import jdplus.regsarima.regular.ModelEstimation;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ModelEstimationExtractor {

    public final String SARIMA = "sarima",
            LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX="max",
            OUTLIERS = "outlier(*)",
            CALENDAR = "calendar(*)",
            EASTER = "easter",
            NTD = "ntd", NMH = "nmh",
            TD = "td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
            LP = "lp", OUT = "out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar", SCORE="pscore";

    static final InformationMapping<ModelEstimation> MAPPING = new InformationMapping<>(ModelEstimation.class);

    static {
        MAPPING.set(PERIOD, Integer.class, source -> source.getOriginalSeries().getAnnualFrequency());
        MAPPING.set(InformationExtractor.concatenate(SPAN, START), TsPeriod.class, source -> source.getOriginalSeries().getStart());
        MAPPING.set(InformationExtractor.concatenate(SPAN, END), TsPeriod.class, source -> source.getOriginalSeries().getDomain().getLastPeriod());
        MAPPING.set(InformationExtractor.concatenate(SPAN, N), Integer.class, source -> source.getOriginalSeries().length());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, START), TsPeriod.class, source -> source.getEstimationDomain().getStartPeriod());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, END), TsPeriod.class, source -> source.getEstimationDomain().getLastPeriod());
        MAPPING.set(InformationExtractor.concatenate(ESPAN, N), Integer.class, source -> source.getEstimationDomain().getLength());
        MAPPING.set(LOG, Boolean.class, source -> source.isLogTransformation());
        MAPPING.set(ADJUST, Boolean.class, source -> source.getLpTransformation() != LengthOfPeriodType.None);
        MAPPING.set(InformationExtractor.concatenate(NM), Integer.class, source -> source.getMissing().length);
        MAPPING.set(ModellingDictionary.Y, TsData.class, source -> source.getOriginalSeries());
        MAPPING.set(InformationExtractor.concatenate(REGRESSION, COVAR), MatrixType.class, source -> source.getConcentratedLikelihood().covariance(source.getFreeParametersCount(), true));
        MAPPING.set(InformationExtractor.concatenate(MAX, PCOVAR), MatrixType.class, source -> source.getParametersCovariance());
        MAPPING.set(InformationExtractor.concatenate(MAX, SCORE), double[].class, source -> source.getScore());
        MAPPING.set(InformationExtractor.concatenate(REGRESSION, COEFF), double[].class, source -> source.getConcentratedLikelihood().coefficients().toArray());
        MAPPING.set(InformationExtractor.concatenate(REGRESSION, COEFFDESC), String[].class, source -> {
            Variable[] vars = source.getVariables();
            if (vars.length == 0) {
                return null;
            }
            String[] nvars = new String[vars.length];
            for (int i = 0; i < nvars.length; ++i) {
                nvars[i] = vars[i].getName();
            }
            return nvars;
        });
        
        MAPPING.delegate(SARIMA, SarimaExtractor.getMapping(), source -> source.getModel().arima());
        MAPPING.delegate(LIKELIHOOD, LikelihoodStatisticsExtractor.getMapping(), source -> source.getStatistics());
    }

    public InformationMapping<ModelEstimation> getMapping() {
        return MAPPING;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.toolkit.extractors;

import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.modelling.ModellingDictionary;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.modelling.GeneralLinearModel;
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationExtractor;
import nbbrd.service.ServiceProvider;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class LinearModelEstimationExtractor extends InformationMapping<GeneralLinearModel> {

    public final String LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
            REGRESSION = "regression",
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
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", PCOVAR = "pcovar";

    public LinearModelEstimationExtractor() {
        set(PERIOD, Integer.class, source -> source.getDescription().getSeries().getAnnualFrequency());
        set(BasicInformationExtractor.concatenate(SPAN, START), TsPeriod.class, source -> source.getDescription().getSeries().getStart());
        set(BasicInformationExtractor.concatenate(SPAN, END), TsPeriod.class, source -> source.getDescription().getSeries().getDomain().getLastPeriod());
        set(BasicInformationExtractor.concatenate(SPAN, N), Integer.class, source -> source.getDescription().getSeries().length());
        set(BasicInformationExtractor.concatenate(SPAN, NM), Integer.class, source -> source.getEstimation().getMissing().length);
//        MAPPING.set(BasicInformationExtractor.concatenate(ESPAN, START), TsPeriod.class, source -> source.getEstimationDomain().getStartPeriod());
//        MAPPING.set(BasicInformationExtractor.concatenate(ESPAN, END), TsPeriod.class, source -> source.getEstimationDomain().getLastPeriod());
//        MAPPING.set(BasicInformationExtractor.concatenate(ESPAN, N), Integer.class, source -> source.getEstimationDomain().getLength());
        set(LOG, Boolean.class, source -> source.getDescription().isLogTransformation());
        set(ADJUST, Boolean.class, source -> source.getDescription().getLengthOfPeriodTransformation() != LengthOfPeriodType.None);
        set(ModellingDictionary.Y, TsData.class, source -> source.getDescription().getSeries());
        set(BasicInformationExtractor.concatenate(REGRESSION, COVAR), MatrixType.class, source -> source.getEstimation().getCoefficientsCovariance());
        set(BasicInformationExtractor.concatenate(REGRESSION, PCOVAR), MatrixType.class, source -> source.getEstimation().getParameters().getCovariance());
    }


    @Override
    public Class getSourceClass() {
        return GeneralLinearModel.class;
    }

}

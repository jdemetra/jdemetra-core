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
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.RegressionItem;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.ICalendarVariable;
import ec.tstoolkit.timeseries.regression.IEasterVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.Residuals;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class RegArimaDictionary {

    public static final String ARIMA = "arima", SARIMASPEC = "model", RESIDUALS = "residuals", LL = "likelihood";
    public static final String REGS = "regressors", OUTLIERS = "outliers", CAL = "calendar"; // headers
    public static final String N = "n", LOG = "log", MEAN = "mu", P = "P", D = "D", Q = "Q", BP = "BP", BD = "BD", BQ = "BQ";
    public static final String SE = "SE (res)", BIC = "BIC", QVAL = "Q-val", QS = "Qs", Q2 = "Q2";
    public static final String TD0 = "Monday", TD1 = "Tuesday", TD2 = "Wednesday", TD3 = "Thursday", TD4 = "Friday", TD5 = "Saturday", TD6 = "Sunday";
    public static final String G_TD0 = "TD1", G_TD1 = "TD2", G_TD2 = "TD3", G_TD3 = "TD4", G_TD4 = "TD5", G_TD5 = "TD6";
    public static final String WD = "Week Days";
    public static final String LP = "LP", TD = "TD";
    public static final String EASTER = "Easter", EASTER_DUR = "Easter_Duration";
    public static final String PHI1 = "phi[1]", PHI2 = "phi[2]", PHI3 = "phi[3]", PHI4 = "phi[4]", BPHI1 = "bphi[1]",
            TH1 = "th[1]", TH2 = "th[2]", TH3 = "th[3]", TH4 = "th[4]", BTH1 = "bth[1]";
    public static final String T_TD0 = "t_td1", T_TD1 = "t_td2", T_TD2 = "t_td3", T_TD3 = "t_td4", T_TD4 = "t_td5", T_TD5 = "t_td6", T_TD6 = "t_td7";
    public static final String T_EASTER = "t_Easter";
    public static final String T_LP = "T_LP";
    public static final String T_PHI1 = "t_phi[1]", T_PHI2 = "t_phi[2]", T_PHI3 = "t_phi[3]", T_PHI4 = "t_phi[4]", T_BPHI1 = "t_bphi[1]", T_TH1 = "t_th[1]", T_TH2 = "t_th[2]", T_TH3 = "t_th[3]", T_TH4 = "t_th[4]", T_BTH1 = "t_bth[1]";
    public static final String LB_VAL = "Ljung-Box", SK_VAL = "Skewness", KURT_VAL = "Kurtosis", LB2_VAL = "Ljung-Box on Squares", LBS_VAL = "Seasonal Ljung-Box", TD_PEAK_VAL = "TD Peak", S_PEAK_VAL = "S Peak", TD_VPEAK = "TD visual peak", S_VPEAK = "S visual peak";
    public static final String LB_PVAL = "Ljung-Box: P-Value", SK_PVAL = "Skewness: P-Value", KURT_PVAL = "Kurtosis: P-Value", LB2_PVAL = "Ljung-Box on Squares: P-Value", LBS_PVAL = "Seasonal Ljung-Box: P-Value";
    public static final String OUTLIERSCOUNT = "noutliers", TDCOUNT = "ntd";
    public static final String I_FREQ = "frequency";
    public static final String I_SPAN = "span", I_START = "start", I_END = "end", I_N = "n";
    public static final String[] PHI_l = {PHI1, PHI2, PHI3, PHI4};
    public static final String[] TH_l = {TH1, TH2, TH3, TH4};
    public static final String[] PHI_s = {PHI1, PHI2, PHI3};
    public static final String[] TH_s = {TH1, TH2, TH3};
    public static final String[] BPHI = {BPHI1};
    public static final String[] BTH = {BTH1};
    public static final String[] OUTLIERS_LIST = {"out(1)", "out(2)", "out(3)", "out(4)",
        "out(5)", "out(6)", "out(7)", "out(8)", "out(9)", "out(10)", "out(11)", "out(12)",
        "out(13)", "out(14)", "out(15)", "out(16)", "out(17)", "out(18)", "out(19)", "out(20)"}; // parameter
    public static final String[] TD_LIST = {"td(1)", "td(2)", "td(3)", "td(4)", "td(5)",
        "td(6)", "td(7)", "td(8)", "td(9)", "td(10)", "td(11)", "td(12)", "td(13)", "td(14)"}; // parameter
    public static final String LB = "lb", SKEW = "skewness", KURT = "kurtosis", LB2 = "lb2",
            SEASLB = "seaslb", BPTest = "bp", BP2 = "bp2", SEASBP = "seasbp"; //statistical tests

    public static void fill(PreprocessingModel preprocessing, InformationSet info) {
        try {
            SarimaComponent sarima = preprocessing.description.getArimaComponent();
            if (sarima == null) {
                return;
            }
            TsDomain domain = preprocessing.description.getSeriesDomain();
            fillDomain(domain, info);
            fillTransformation(preprocessing.description, info);
            fillLikelihood(preprocessing.estimation.getStatistics(), info);
            fill(sarima, info);
            fillResiduals(preprocessing.getFullResiduals(), sarima.getFreeParametersCount(), preprocessing.estimation.getLikelihood().getNx(),
                    info);
            TsVariableList vars = preprocessing.description.buildRegressionVariables();
            if (!vars.isEmpty()) {
                int start = preprocessing.description.getRegressionVariablesStartingPosition();
                int nhp = sarima.getFreeParametersCount();
                fillTD(vars, preprocessing.estimation, info, start, nhp);
                fillEaster(vars, preprocessing.estimation, info, start, nhp);
                fillOutliers(vars, preprocessing.estimation, info, start, nhp);

                info.set(OUTLIERSCOUNT, vars.select(OutlierType.Undefined).getItemsCount());
            }
        }
        catch (Exception err) {
        }
    }

    private static void fillDomain(TsDomain domain, InformationSet info) {
        info.set(I_FREQ, domain.getFrequency());
        info.set(I_START, domain.getStart());
        info.set(I_END, domain.getLast());
        info.set(I_N, domain.getLength());
    }

    private static void fillTransformation(ModelDescription description, InformationSet info) {
        info.set(LOG, description.getTransformation() == DefaultTransformationType.Log ? Boolean.TRUE : Boolean.FALSE);
        info.set(LP, description.getLengthOfPeriodType() == LengthOfPeriodType.None ? Boolean.FALSE : Boolean.TRUE);
    }

    private static void fillLikelihood(LikelihoodStatistics statistics, InformationSet info) {
        if (statistics == null) {
            return;
        }
        InformationSet linfo = info.subSet(LL);
        linfo.set(BIC, statistics.BICC);
    }

    private static void fill(SarimaComponent sarima, InformationSet info) {
        InformationSet arimainfo = info.subSet(ARIMA);
        arimainfo.set(SARIMASPEC, sarima.getSpecification());
        arimainfo.set(P, sarima.getP());
        arimainfo.set(D, sarima.getD());
        arimainfo.set(Q, sarima.getQ());
        arimainfo.set(BP, sarima.getBP());
        arimainfo.set(BD, sarima.getBD());
        arimainfo.set(BQ, sarima.getBQ());
        arimainfo.set(MEAN, sarima.isMean());

        Parameter[] phi = sarima.getPhi();
        if (phi != null) {
            for (int i = 0; i < phi.length; ++i) {
                if (phi[i].getType() != ParameterType.Undefined) {
                    arimainfo.set(PHI_l[i], phi[i]);
                }
            }
        }
        Parameter[] th = sarima.getTheta();
        if (th != null) {
            for (int i = 0; i < th.length; ++i) {
                if (th[i].getType() != ParameterType.Undefined) {
                    arimainfo.set(TH_l[i], th[i]);
                }
            }
        }
        Parameter[] bphi = sarima.getBPhi();
        if (bphi != null) {
            for (int i = 0; i < bphi.length; ++i) {
                if (bphi[i].getType() != ParameterType.Undefined) {
                    arimainfo.set(BPHI[i], bphi[i]);
                }
            }
        }
        Parameter[] bth = sarima.getBTheta();
        if (bth != null) {
            for (int i = 0; i < bth.length; ++i) {
                if (bth[i].getType() != ParameterType.Undefined) {
                    arimainfo.set(BTH[i], bth[i]);
                }
            }
        }
    }

    private static void fillResiduals(TsData fullResiduals, int nhp, int nx, InformationSet info) {

        Residuals res = new Residuals();
        res.setDomain(fullResiduals.getDomain());
        res.setValues(fullResiduals.internalStorage());
        res.setType(Residuals.Type.FullResiduals);
        if (!res.calc(nhp, nx)) {
            return;
        }
        InformationSet linfo = info.subSet(LL);
        NiidTests wn = res.getTests();
        InformationSet rinfo = info.subSet(RESIDUALS);
        rinfo.set(SE, wn.getStatistics().getSumSquare() / (fullResiduals.getLength() - nhp - nx));
        rinfo.set(SKEW, wn.getSkewness().getPValue());
        rinfo.set(KURT, wn.getKurtosis().getPValue());
        if (wn.getLjungBox().isValid()) {
            linfo.set(QVAL, wn.getLjungBox().getValue());
            rinfo.set(LB, wn.getLjungBox().getPValue());
        }
        if (wn.getLjungBoxOnSquare().isValid()) {
            rinfo.set(LB2, wn.getLjungBoxOnSquare().getPValue());
        }
        if (wn.getSeasonalLjungBox().isValid()) {
            rinfo.set(SEASLB, wn.getSeasonalLjungBox().getPValue());
        }
        if (wn.getBoxPierce().isValid()) {
            rinfo.set(BPTest, wn.getBoxPierce().getPValue());
        }
        if (wn.getBoxPierceOnSquare().isValid()) {
            rinfo.set(BP2, wn.getBoxPierceOnSquare().getPValue());
        }
        if (wn.getSeasonalBoxPierce().isValid()) {
            rinfo.set(SEASBP, wn.getSeasonalBoxPierce().getPValue());
        }
    }

    private static void fillTD(TsVariableList vars, ModelEstimation estimation, InformationSet info, int start, int hpcount) {
        TsVariableSelection<ICalendarVariable> td = vars.select(ICalendarVariable.class);
        InformationSet cinfo = info.subSet(CAL);
        int ntd = td.getVariablesCount();
        cinfo.set(TDCOUNT, ntd);
        if (ntd == 0) {
            return;
        }
        double[] c = estimation.getLikelihood().getB();
        double[] e = estimation.getLikelihood().getBSer(true, hpcount);
        int tdmax = Math.min(ntd, TD_LIST.length);
        int icur = 0;
        for (TsVariableSelection.Item<ICalendarVariable> var : td.elements()) {
            for (int j = 0; j < var.variable.getDim(); ++j) {
                RegressionItem reg = new RegressionItem(null, false, c[start + var.position + j], e[start + var.position + j]);
                if (icur < tdmax) {
                    cinfo.set(TD_LIST[icur++], reg);
                }
                else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("td(").append(++icur).append(')');
                    cinfo.set(builder.toString(), reg);
                }
            }
        }
    }

    private static void fillEaster(TsVariableList vars, ModelEstimation estimation, InformationSet info, int start, int hpcount) {
         TsVariableSelection<IEasterVariable> easter = vars.select(IEasterVariable.class);
           if (easter.isEmpty())
                return;
            InformationSet cinfo = info.subSet(CAL);
        double[] c = estimation.getLikelihood().getB();
        double[] e = estimation.getLikelihood().getBSer(true, hpcount);
            int pos = easter.get(0).position+start;
            int dur = easter.get(0).variable.getDuration();
            StringBuilder builder = new StringBuilder();
            builder.append(EASTER).append('(').append(dur).append(')');
            RegressionItem reg = new RegressionItem(builder.toString(), false, c[pos], e[pos]);
            cinfo.set(EASTER, reg);
   }

    private static void fillOutliers(TsVariableList vars, ModelEstimation estimation, InformationSet info, int start, int hpcount) {
        InformationSet oinfo = info.subSet(OUTLIERS);
        int icur = 0;
        double[] b = estimation.getLikelihood().getB();
        double[] e = estimation.getLikelihood().getBSer(true, hpcount);
        TsVariableSelection<IOutlierVariable> outliers = vars.select(IOutlierVariable.class);
        for (TsVariableSelection.Item<IOutlierVariable> cur : outliers.elements()) {
            String name;
            if (icur < OUTLIERS_LIST.length) {
                name = OUTLIERS_LIST[icur++];
            }
            else {
                StringBuilder builder = new StringBuilder();
                builder.append("out(").append(++icur).append(')');
                name = builder.toString();
            }
            RegressionItem reg = new RegressionItem(cur.variable.getDescription(), cur.variable.isPrespecified(), b[start + cur.position], e[start + cur.position]);
            oinfo.set(name, reg);
        }
    }
}

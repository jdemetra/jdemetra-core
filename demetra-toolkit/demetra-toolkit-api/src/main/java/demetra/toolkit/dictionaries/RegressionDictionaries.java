/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.math.matrices.Matrix;
import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.RegressionItem;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;
import demetra.toolkit.dictionaries.Dictionary.EntryType;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionDictionaries {

    public static final String LOG = "log", ADJUST = "adjust", PERIOD = "period",
            SPAN_START = "span.start", SPAN_END = "span.end", SPAN_N = "span.n", SPAN_MISSING = "span.missing";

    public final Dictionary BASIC = AtomicDictionary.builder()
            .name("basic")
            .item(Item.builder().name(PERIOD).description("period of the series").outputClass(Integer.class).build())
            .item(Item.builder().name(SPAN_START).description("start of the considered (partial) series").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(SPAN_END).description("end of the considered (partial) series").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(SPAN_N).description("number of periods in the considered (partial) series").outputClass(Integer.class).build())
            .item(Item.builder().name(SPAN_MISSING).description("number of missing values in the considered (partial) series").outputClass(Integer.class).build())
            .item(Item.builder().name(LOG).description("log-transformtion").outputClass(Integer.class).build())
            .item(Item.builder().name(ADJUST).description("pre-adjustment for leap year").outputClass(String.class).build())
            .build();

    public static final String ESPAN_START = "espan.start", ESPAN_END = "espan.end", ESPAN_N = "espan.n", ESPAN_MISSING = "espan.missing",
            MEAN = "mean", NLP = "nlp", NTD = "ntd", LEASTER = "leaster", NMH = "nmh", NOUT = "nout", NAO = "nao", NLS = "nls", NTC = "ntc", NSO = "nso", NUSERS = "nusers",
            MU = "mu", LP = "lp", TD = "td", TDDERIVED = "td-derived", TDF = "td-ftest", EASTER = "easter", OUTLIERS = "outlier", USER = "user", MISSING = "missing";

    public final Dictionary REGRESSION_DESC = AtomicDictionary.builder()
            .name("regression")
            .item(Item.builder().name(ESPAN_START).description("start of the considered span in the estimation").outputClass(Integer.class).build())
            .item(Item.builder().name(ESPAN_END).description("end of the considered span in the estimation").outputClass(Integer.class).build())
            .item(Item.builder().name(ESPAN_N).description("number of periods in the considered span for estimation").outputClass(Integer.class).build())
            .item(Item.builder().name(ESPAN_MISSING).description("number of missing values in the considered span for estimation").outputClass(Integer.class).build())
            .item(Item.builder().name(MEAN).description("is trend constant").outputClass(Integer.class).build())
            .item(Item.builder().name(NLP).description("is leap year").outputClass(Integer.class).build())
            .item(Item.builder().name(NTD).description("number of trading days (outside lp)").outputClass(Integer.class).build())
            .item(Item.builder().name(LEASTER).description("length of the easter effect").outputClass(Integer.class).build())
            .item(Item.builder().name(NMH).description("number of moving holidays").outputClass(Integer.class).build())
            .item(Item.builder().name(NOUT).description("number of outliers").outputClass(Integer.class).build())
            .item(Item.builder().name(NAO).description("number of additive outliers").outputClass(Integer.class).build())
            .item(Item.builder().name(NLS).description("number of level shifts").outputClass(Integer.class).build())
            .item(Item.builder().name(NTC).description("number of transitory changes").outputClass(Integer.class).build())
            .item(Item.builder().name(NSO).description("number of seasonal outliers").outputClass(Integer.class).build())
            .item(Item.builder().name(NUSERS).description("number of user-defined variables").outputClass(Integer.class).build())
            .build();

    public final Dictionary REGRESSION_EST = AtomicDictionary.builder()
            .name("regression estimation")
            .item(Item.builder().name(MU).description("trend constant").outputClass(RegressionItem.class).build())
            .item(Item.builder().name(LP).description("leap year effect").outputClass(RegressionItem.class).build())
            .item(Item.builder().name(TD).description("trading days effect").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(TDDERIVED).description("derived trading day effect (contrast)").outputClass(RegressionItem.class).build())
            .item(Item.builder().name(TDF).description("derived trading day effect (contrast)").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(EASTER).description("easter effect").outputClass(RegressionItem.class).build())
            .item(Item.builder().name(OUTLIERS).description("outliers").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(USER).description("user variables").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(MISSING).description("estimation of missing values").outputClass(MissingValueEstimation.class).type(EntryType.Array).build())
            .build();

    public static final String Y = "y", // original series
            YC = "yc", // interpolated series. Untransformed
            Y_F = "y_f",
            Y_EF = "y_ef",
            Y_B = "y_b",
            Y_EB = "y_eb",
//            Y_LIN = "y_lin", // linearized series (transformed series without pre-adjustment and regression effects). Untransformed
//            Y_LIN_F = "y_lin_f",
//            Y_LIN_B = "y_lin_b",
            CAL = "cal", // all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed       
            CAL_F = "cal_f",
            CAL_B = "cal_b",
            YCAL = "ycal", // series corrected for calendar effects: y_cal = yc-/cal. Untransformed
//            YCAL_F = "ycal_f",
//            YCAL_B = "ycal_b",
            DET = "det", // all deterministic effects (including pre-adjustment). Untransformed
            DET_F = "det_f",
            DET_B = "det_b",
            TDE = "tde", // trading days effects (including leap year/length of period, includeing pre-adjustments). Untransformed
            TDE_F = "tde_f",
            TDE_B = "tde_b",
            EE = "ee", // Easter effects. Untransformed
            EE_F = "ee_f",
            EE_B = "ee_b",
            RMDE = "rmde", // Ramadan effects. Untransformed
            RMDE_F = "rmde_f",
            RMDE_B = "rmde_b",
            OMHE = "omhe", // Other mothing holidays effects. Untransformed
            OMHE_F = "omhe_f",
            OMHE_B = "omhe_b",
            MHE = "mhe", // All moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed
            MHE_F = "mhe_f",
            MHE_B = "mhe_b",
            OUT = "out", // All outliers effects. Untransformed
            OUT_F = "out_f",
            OUT_B = "out_b",
            REG = "reg", // All other regression effects (outside outliers and calendars). Untransformed
            REG_F = "reg_f",
            REG_B = "reg_b",
            L = "l" // linearized series (series without pre-adjustment and regression effects). l=yc-/det. Transformed
//            L_F = "l_f",
//            L_EF = "l_ef",
//            L_B = "l_b",
//            L_EB = "l_eb"
            ;

    public final Dictionary REGRESSION_EFFECTS = AtomicDictionary.builder()
            .name("regression estimation")
            .item(Item.builder().name(Y).description("original series").outputClass(TsData.class).build())
            .item(Item.builder().name(Y_F).description("forecasts of the original series").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(Y_EF).description("forecasts errors of the original series").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(Y_B).description("backcasts of the original series").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(Y_EB).description("backcasts errors of the original series").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(YC).description("interpolated series. Untransformed").outputClass(TsData.class).build())
//            .item(Item.builder().name(Y_LIN).description("linearized series (series without pre-adjustment and regression effects). l=yc-/det. Untransformed").outputClass(TsData.class).build())
//            .item(Item.builder().name(Y_LIN_F).description("forcasts of the linearized series. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
//            .item(Item.builder().name(Y_LIN_B).description("backcasts of the linearized series. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET).description("all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(DET_F).description("forcasts of all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_B).description("backcasts of all deterministic effects (including pre-adjustment). Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(CAL).description("all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(CAL_F).description("forecasts of all calendar effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(CAL_B).description("backcasts of all calendar effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(YCAL).description("series corrected for calendar effects: y_cal = yc-/cal. Untransformed").outputClass(TsData.class).build())
//            .item(Item.builder().name(YCAL_F).description("forecasts of the series corrected for calendar effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
//            .item(Item.builder().name(YCAL_B).description("backcasts of the series corrected for calendar effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(TDE).description("trading days effects (including leap year/length of period, including pre-adjustments). Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(TDE_F).description("forecasts of the trading days effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(TDE_B).description("backcasts of the trading days effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(EE).description("Easter effects. Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(EE_F).description("forecasts of the Easter effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(EE_B).description("backcasts of the Easter effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OMHE).description("other mothing holidays effects. Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(OMHE_F).description("forecasts of the other mothing holidays effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OMHE_B).description("backcasts of the other mothing holidays effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(MHE).description("all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(MHE_F).description("forecats of all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(MHE_B).description("backcasts of all moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT).description("all outliers effects. Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(OUT_F).description("forecasts of all outliers effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_B).description("backcasts of all outliers effects. Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG).description("all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_F).description("forecasts of all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_B).description("backcasts of all other regression effects (outside outliers and calendars). Untransformed").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(L).description("linearized series (transformed series without pre-adjustment and regression effects). Transformed)").outputClass(TsData.class).build())
//            .item(Item.builder().name(L_F).description("forecasts of the linearized. Transformed)").outputClass(TsData.class).type(EntryType.Parametric).build())
//            .item(Item.builder().name(L_B).description("backcasts of the linearized. Transformed)").outputClass(TsData.class).type(EntryType.Parametric).build())
//            .item(Item.builder().name(L_EF).description("forecast errors of the linearized. Transformed)").outputClass(TsData.class).type(EntryType.Parametric).build())
//            .item(Item.builder().name(L_EB).description("backcast errors of the linearized. Transformed)").outputClass(TsData.class).type(EntryType.Parametric).build())
            .build();

    public final String COEFFDESC = "description", REGTYPE = "type", COEFF = "coefficients", COVAR = "covar", COVAR_ML = "covar-ml";
    
    public final Dictionary REGRESSION_UTILITY = AtomicDictionary.builder()
            .name("regression utility")
            .item(Item.builder().name(COEFFDESC).description("description of all the regression variables").outputClass(String[].class).build())
            .item(Item.builder().name(REGTYPE).description("type of all the regression variables").outputClass(int[].class).build())
            .item(Item.builder().name(COEFF).description("coeff of all the regression variables").outputClass(double[].class).build())
            .item(Item.builder().name(COVAR).description("covariance of the parameters of all the regression variables").outputClass(Matrix.class).build())
            .item(Item.builder().name(COVAR_ML).description("ml-covariance of the parameters of all the regression variables").outputClass(Matrix.class).build())
            .build();
}

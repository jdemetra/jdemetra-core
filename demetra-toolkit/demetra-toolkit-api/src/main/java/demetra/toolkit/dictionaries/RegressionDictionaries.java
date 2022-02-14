/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.RegressionItem;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegressionDictionaries {
    
    public static final String LOG="log", ADJUST="adjust";
    
    public final Dictionary TRANSFORMATION = AtomicDictionary.builder()
            .name("transformation")
            .item(Item.builder().name(LOG).description("log-transformtion").type(boolean.class).build())
            .item(Item.builder().name(ADJUST).description("pre-adjustment for leap year").type(String.class).build())
            .build();
    
    public static final String MEAN = "mean", NLP="nlp", NTD="ntd", LEASTER="leaster", NMH="nmh", NOUT="nout", NAO="nao", NLS="nls", NTC="ntc", NSO="nso", NUSERS="nusers",
            MU = "mu", LP = "lp", TD="td*", TDDERIVED="td-derived", TDF="td-ftest", EASTER = "easter", OUTLIERS = "outlier(*)", USER= "user(*)", MISSING="missing(*)";

    public final Dictionary REGRESSION_DESC = AtomicDictionary.builder()
            .name("regression")
            .item(Item.builder().name(MEAN).description("is trend constant").type(int.class).build())
            .item(Item.builder().name(NLP).description("is leap year").type(int.class).build())
            .item(Item.builder().name(NTD).description("number of trading days (outside lp)").type(int.class).build())
            .item(Item.builder().name(LEASTER).description("length of the easter effect").type(int.class).build())
            .item(Item.builder().name(NMH).description("number of moving holidays").type(int.class).build())
            .item(Item.builder().name(NOUT).description("number of outliers").type(int.class).build())
            .item(Item.builder().name(NAO).description("number of additive outliers").type(int.class).build())
            .item(Item.builder().name(NLS).description("number of level shifts").type(int.class).build())
            .item(Item.builder().name(NTC).description("number of transitory changes").type(int.class).build())
            .item(Item.builder().name(NSO).description("number of seasonal outliers").type(int.class).build())
            .item(Item.builder().name(NUSERS).description("number of user-defined variables").type(int.class).build())
            .build();
    
    public final Dictionary REGRESSION_EST = AtomicDictionary.builder()
            .name("regression estimation")
            .item(Item.builder().name(MU).description("trend constant").type(RegressionItem.class).build())
            .item(Item.builder().name(LP).description("leap year effect").type(RegressionItem.class).build())
            .item(Item.builder().name(TD).description("trading days effect").type(RegressionItem.class).list(true).build())
            .item(Item.builder().name(TDDERIVED).description("derived trading day effect (contrast)").type(RegressionItem.class).build())
            .item(Item.builder().name(TDF).description("derived trading day effect (contrast)").type(StatisticalTest.class).build())
            .item(Item.builder().name(EASTER).description("easter effect").type(RegressionItem.class).build())
            .item(Item.builder().name(OUTLIERS).description("outliers").type(RegressionItem.class).list(true).build())
            .item(Item.builder().name(USER).description("user variables").type(RegressionItem.class).list(true).build())
            .item(Item.builder().name(MISSING).description("estimation of missing values").type(MissingValueEstimation.class).list(true).build())
            .build();
    
    
    public static final String
            Y="y",      // original series
            YC="yc",    // interpolated series. Untransformed
            L="l", // linearized series (series without pre-adjustment and regression effects). l=yc-/det. Untransformed
            Y_LIN="y_lin", // linearized series (transformed series without pre-adjustment and regression effects). Transformed
            CAL="cal",     // all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed       
            YCAL="ycal",  // series corrected for calendar effects: y_cal = yc-/cal. Untransformed
            DET="det",   // all deterministic effects (including pre-adjustment). Untransformed
            TDE="tde",  // trading days effects (including leap year/length of period, includeing pre-adjustments). Untransformed
            EE="ee", // Easter effects. Untransformed
            RMDE="rmde", // Ramadan effects. Untransformed
            OMHE="omhe", // Other mothing holidays effects. Untransformed
            MHE="mhe", // All moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed
            OUT="out", // All outliers effects. Untransformed
            REG="reg" // All other regression effects (outside outliers and calendars). Untransformed
            ;

                    
    public final Dictionary REGRESSION_EFFECTS = AtomicDictionary.builder()
            .name("regression estimation")
            .item(Item.builder().name(Y).description("original series").type(TsData.class).build())
            .item(Item.builder().name(YC).description("interpolated series. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(L).description("linearized series (series without pre-adjustment and regression effects). l=yc-/det. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(Y_LIN).description("linearized series (transformed series without pre-adjustment and regression effects). Transformed)").type(TsData.class).build())
            .item(Item.builder().name(DET).description("all deterministic effects (including pre-adjustment). Untransformed").type(TsData.class).build())
            .item(Item.builder().name(CAL).description("all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(YCAL).description("series corrected for calendar effects: y_cal = yc-/cal. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(TDE).description("trading days effects (including leap year/length of period, including pre-adjustments). Untransformed").type(TsData.class).build())
            .item(Item.builder().name(EE).description("Easter effects. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(OMHE).description("Other mothing holidays effects. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(MHE).description("All moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(OUT).description("All outliers effects. Untransformed").type(TsData.class).build())
            .item(Item.builder().name(REG).description("All other regression effects (outside outliers and calendars). Untransformed").type(TsData.class).build())
            .build();
    
}

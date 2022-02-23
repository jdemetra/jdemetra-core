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
package demetra.sa;

import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.AtomicDictionary;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.Dictionary.EntryType;
import nbbrd.design.Development;

/**
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public final class SaDictionaries {

    /**
     * Subdivision of a SA processing
     * Pre-processing of the series (usually reg-arima), decomposition of the
     * linearized series, final decomposition,
     * optional benchmarking and diagnostics
     *
     */
    public final String PREPROCESSING = "preprocessing", DECOMPOSITION = "decomposition", FINAL = "final",
            BENCHMARKING = "benchmarking", DIAGNOSTICS = "diagnostics";
    public final String VARIANCE = "variancedecomposition";
    /**
     * Default components: series, trend, seasonal, seasonally adjusted,
     * irregular, si-ratio, undefined
     */
    public final String Y = "y", T = "t", S = "s", SA = "sa", I = "i", SI = "si", U = "u";

    /**
     * Default suffixes
     * Linearized series are series after transformation and removal of the
     * deterministic components.
     * Components are series after removal of the deterministic components, but
     * not (log-)transformed
     */
    public final String FORECAST = "_f", FORECASTERROR = "_ef", BACKCAST = "_b", BACKCASTERROR = "_eb",
            LINEARIZED = "_lin", COMPONENT = "_cmp";

    /**
     * Default prefixes
     * Outliers, other regression effects and complete deterministic effects
     * (=out+reg)
     */
    public final String OUT = "out_", REG = "reg_", DET = "det_";

///////////////////////////////////////////////////////////////////////////////
    /**
     * Decomposition mode of the series
     */
    public final String MODE = "mode";

    public final String Y_CMP = "y_cmp", Y_CMP_F = "y_cmp_f", Y_CMP_B = "y_cmp_b",
            T_CMP = "t_cmp", S_CMP = "s_cmp", SA_CMP = "sa_cmp", I_CMP = "i_cmp", SI_CMP = "si_cmp";

    public final String Y_LIN = "y_lin", T_LIN = "t_lin", S_LIN = "s_lin", SA_LIN = "sa_lin", I_LIN = "i_lin", SI_LIN = "si_lin";
    public final String Y_LIN_F = "y_lin_f", T_LIN_F = "t_lin_f", S_LIN_F = "s_lin_f", SA_LIN_F = "sa_lin_f", I_LIN_F = "i_lin_f";
    public final String Y_LIN_EF = "y_lin_ef", T_LIN_EF = "t_lin_ef", S_LIN_EF = "s_lin_ef", SA_LIN_EF = "sa_lin_ef", I_LIN_EF = "i_lin_ef";
    public final String Y_LIN_B = "y_lin_b", T_LIN_B = "t_lin_b", S_LIN_B = "s_lin_b", SA_LIN_B = "sa_lin_b", I_LIN_B = "i_lin_b";
    public final String Y_LIN_EB = "y_lin_eb", T_LIN_EB = "t_lin_eb", S_LIN_EB = "s_lin_eb", SA_LIN_EB = "sa_lin_eb", I_LIN_EB = "i_lin_eb";
    
    public final String OUT_I = "out_i", OUT_S = "out_s", OUT_T = "out_t", 
            OUT_I_F = "out_i_f", OUT_S_F = "out_s_f", OUT_T_F = "out_t_f",
            OUT_I_B = "out_i_b", OUT_S_B = "out_s_b", OUT_T_B = "out_t_b";
    public final String REG_I = "reg_i", REG_S = "reg_s", REG_T = "reg_t", REG_SA = "reg_sa",
            REG_Y = "reg_y", REG_U = "reg_u", 
            REG_I_F = "reg_i_f", REG_S_F = "reg_s_f", REG_T_F = "reg_t_f", REG_SA_F = "reg_sa_f",
            REG_Y_F = "reg_y_f", REG_U_F = "reg_u_f",
            REG_I_B = "reg_i_b", REG_S_B = "reg_s_b", REG_T_B = "reg_t_b", REG_SA_B = "reg_sa_b",
            REG_Y_B = "reg_y_b", REG_U_B = "reg_u_b";
    public final String DET_I = "det_i", DET_S = "det_s", DET_T = "det_t", DET_I_F = "det_i_f",
            DET_S_F = "det_s_f", DET_T_F = "det_t_f", DET_I_B = "det_i_b",
            DET_S_B = "det_s_b", DET_T_B = "det_t_b";

    public final Dictionary REGEFFECTS = AtomicDictionary.builder()
            .name("regression effects")
            .item(Item.builder().name(OUT_T).description("outliers effects associated to the trend").outputClass(TsData.class).build())
            .item(Item.builder().name(OUT_S).description("outliers effects associated to the seasonal").outputClass(TsData.class).build())
            .item(Item.builder().name(OUT_I).description("outliers effects associated to the irregular").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_T).description("other regression effects associated to the trend").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_S).description("other regression effects associated to the seasonal").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_I).description("other regression effects associated to the irregular").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_SA).description("other regression effects associated to the sa").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_U).description("other undefined regression effects (split between the components").outputClass(TsData.class).build())
            .item(Item.builder().name(REG_Y).description("other regression effects removed from the series (not in the components").outputClass(TsData.class).build())
            .item(Item.builder().name(DET_T).description("all regression effects associated to the trend").outputClass(TsData.class).build())
            .item(Item.builder().name(DET_S).description("all regression effects associated to the seasonal").outputClass(TsData.class).build())
            .item(Item.builder().name(DET_I).description("all regression effects associated to the irregular").outputClass(TsData.class).build())
            .item(Item.builder().name(OUT_T_F).description("outliers effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_S_F).description("outliers effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_I_F).description("outliers effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_T_F).description("other regression effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_S_F).description("other regression effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_I_F).description("other regression effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_SA_F).description("other regression effects associated to the sa").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_U_F).description("other undefined regression effects (split between the components").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_Y_F).description("other regression effects removed from the series (not in the components").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_T_F).description("all regression effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_S_F).description("all regression effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_I_F).description("all regression effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_T_B).description("outliers effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_S_B).description("outliers effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(OUT_I_B).description("outliers effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_T_B).description("other regression effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_S_B).description("other regression effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_I_B).description("other regression effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_SA_B).description("other regression effects associated to the sa").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_U_B).description("other undefined regression effects (split between the components").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(REG_Y_B).description("other regression effects removed from the series (not in the components").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_T_B).description("all regression effects associated to the trend").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_S_B).description("all regression effects associated to the seasonal").outputClass(TsData.class).type(EntryType.Parametric).build())
            .item(Item.builder().name(DET_I_B).description("all regression effects associated to the irregular").outputClass(TsData.class).type(EntryType.Parametric).build())
            .build();



    public final Dictionary SADECOMPOSITION = AtomicDictionary.builder()
            .name("sadecomposition")
            .item(Item.builder().name(MODE).description("decomposition mode").outputClass(String.class).build())
//            .item(Item.builder().name(Y).description("series").outputClass(TsData.class).build())
            .item(Item.builder().name(SA).description("seasonal adjusted").outputClass(TsData.class).build())
            .item(Item.builder().name(T).description("trend").outputClass(TsData.class).build())
            .item(Item.builder().name(S).description("seasonal").outputClass(TsData.class).build())
            .item(Item.builder().name(I).description("irregular").outputClass(TsData.class).build())
            .build();

    public final Dictionary CMPDECOMPOSITION = AtomicDictionary.builder()
            .name("components")
            .item(Item.builder().name(Y_CMP).description("series (without regression effects other than trend-constant and regression effects linked to the series)").outputClass(TsData.class).build())
            .item(Item.builder().name(Y_CMP_F).description("forecasts of the series").outputClass(TsData.class).build())
            .item(Item.builder().name(Y_CMP_B).description("backcasts of the series").outputClass(TsData.class).build())
            .item(Item.builder().name(SA_CMP).description("seasonal adjusted component (without regression effects)").outputClass(TsData.class).build())
            .item(Item.builder().name(T_CMP).description("trend component (without regression effects)").outputClass(TsData.class).build())
            .item(Item.builder().name(S_CMP).description("seasonal component (without regression effects)").outputClass(TsData.class).build())
            .item(Item.builder().name(I_CMP).description("irregular component (without regression effects)").outputClass(TsData.class).build())
            .build();

    public final Dictionary SADECOMPOSITION_F = AtomicDictionary.builder()
            .name("sadecomposition (forecasts)")
//            .item(Item.builder().name(Y + FORECAST).description("series (forecasts)").outputClass(TsData.class).build())
            .item(Item.builder().name(SA + FORECAST).description("seasonal adjusted (forecasts)").outputClass(TsData.class).build())
            .item(Item.builder().name(T + FORECAST).description("trend (forecasts)").outputClass(TsData.class).build())
            .item(Item.builder().name(S + FORECAST).description("seasonal (forecasts)").outputClass(TsData.class).build())
            .item(Item.builder().name(I + FORECAST).description("irregular (forecasts)").outputClass(TsData.class).build())
            .build();

    public final Dictionary SADECOMPOSITION_EF = AtomicDictionary.builder()
            .name("sadecomposition (forecast errors")
//            .item(Item.builder().name(Y + FORECASTERROR).description("series (forecast errors)").outputClass(TsData.class).build())
            .item(Item.builder().name(SA + FORECASTERROR).description("seasonal adjusted (forecast errors)").outputClass(TsData.class).build())
            .item(Item.builder().name(T + FORECASTERROR).description("trend (forecast errors)").outputClass(TsData.class).build())
            .item(Item.builder().name(S + FORECASTERROR).description("seasonal (forecast errors)").outputClass(TsData.class).build())
            .item(Item.builder().name(I + FORECASTERROR).description("irregular (forecast errors)").outputClass(TsData.class).build())
            .build();

    public final Dictionary RAW_SADECOMPOSITION = AtomicDictionary.builder()
            .name("raw sadecomposition")
            .item(Item.builder().name(Y).description("series").outputClass(double[].class).build())
            .item(Item.builder().name(SA).description("seasonal adjusted").outputClass(double[].class).build())
            .item(Item.builder().name(T).description("trend").outputClass(double[].class).build())
            .item(Item.builder().name(S).description("seasonal").outputClass(double[].class).build())
            .item(Item.builder().name(I).description("irregular").outputClass(double[].class).build())
            .build();

    /*
    * Combined seasonality test
     */
    public final String SEAS_LIN_COMBINED = "seas-lin-combined",
            SEAS_LIN_EVOLUTIVE = "seas-lin-evolutive",
            SEAS_LIN_STABLE = "seas-lin-stable",
            SEAS_SI_COMBINED = "seas-si-combined",
            SEAS_SI_COMBINED3 = "seas-si-combined3",
            SEAS_SI_EVOLUTIVE = "seas-si-evolutive",
            SEAS_SI_STABLE = "seas-si-stable",
            SEAS_RES_COMBINED = "seas-res-combined",
            SEAS_RES_COMBINED3 = "seas-res-combined3",
            SEAS_RES_EVOLUTIVE = "seas-res-evolutive",
            SEAS_RES_STABLE = "seas-res-stable",
            SEAS_SA_COMBINED = "seas-sa-combined",
            SEAS_SA_COMBINED3 = "seas-sa-combined3",
            SEAS_SA_STABLE = "seas-sa-stable",
            SEAS_SA_EVOLUTIVE = "seas-sa-evolutive",
            SEAS_I_COMBINED = "seas-i-combined",
            SEAS_I_COMBINED3 = "seas-i-combined3",
            SEAS_I_STABLE = "seas-i-stable",
            SEAS_I_EVOLUTIVE = "seas-i-evolutive";

    public final Dictionary COMBINEDSEASONALITY = AtomicDictionary.builder()
            .name("combined seasonality tests")
            .item(Item.builder().name(SEAS_LIN_COMBINED).description("combined seasonality test on linearized series").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_LIN_EVOLUTIVE).description("evolutive seasonality test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_STABLE).description("stable seasonality test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SI_COMBINED).description("combined seasonality test on SI").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SI_COMBINED3).description("combined seasonality test on  SI (end)").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SI_EVOLUTIVE).description("evolutive seasonality test on SI").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SI_STABLE).description("stable seasonality test on SI").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_COMBINED).description("combined seasonality test on residuals").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_RES_COMBINED3).description("combined seasonality test on  residuals (end)").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_RES_EVOLUTIVE).description("evolutive seasonality test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_STABLE).description("stable seasonality test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_COMBINED).description("combined seasonality test on sa").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SA_COMBINED3).description("combined seasonality test on  sa (end)").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SA_EVOLUTIVE).description("evolutive seasonality test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_STABLE).description("stable seasonality test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_COMBINED).description("combined seasonality test on irregular").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_I_COMBINED3).description("combined seasonality test on  irregular (end)").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_I_EVOLUTIVE).description("evolutive seasonality test on irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_STABLE).description("stable seasonality test on irregular").outputClass(StatisticalTest.class).build())
            .build();

    /*
     * Generic SA tests
     */
    public final String SEAS_LIN_QS = "seas-lin-qs",
            SEAS_LIN_F = "seas-lin-f",
            SEAS_LIN_FRIEDMAN = "seas-lin-friedman",
            SEAS_LIN_KW = "seas-lin-kw",
            SEAS_LIN_PERIODOGRAM = "seas-lin-periodogram",
            SEAS_LIN_SP = "seas-lin-spectralpeaks",
            SEAS_RES_QS = "seas-res-qs",
            SEAS_RES_F = "seas-res-f",
            SEAS_RES_FRIEDMAN = "seas-res-friedman",
            SEAS_RES_KW = "seas-res-kw",
            SEAS_RES_PERIODOGRAM = "seas-res-periodogram",
            SEAS_RES_SP = "seas-res-spectralpeaks",
            SEAS_SA_QS = "seas-sa-qs",
            SEAS_SA_F = "seas-sa-f",
            SEAS_SA_FRIEDMAN = "seas-sa-friedman",
            SEAS_SA_KW = "seas-sa-kw",
            SEAS_SA_PERIODOGRAM = "seas-sa-periodogram",
            SEAS_SA_SP = "seas-sa-spectralpeaks",
            SEAS_SA_AC1 = "seas-sa-ac1",
            SEAS_I_QS = "seas-i-qs",
            SEAS_I_F = "seas-i-f",
            SEAS_I_FRIEDMAN = "seas-i-friedman",
            SEAS_I_KW = "seas-i-kw",
            SEAS_I_PERIODOGRAM = "seas-i-periodogram",
            SEAS_I_SP = "seas-i-spectralpeaks";

    public final Dictionary GENERICSEASONALITY = AtomicDictionary.builder()
            .name("generic seasonality tests")
            .item(Item.builder().name(SEAS_LIN_QS).description("qs seasonality test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_F).description("f test on seasonal dummies for the linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_FRIEDMAN).description("friedman seasonality test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_KW).description("kruskal-wallis seasonality test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_PERIODOGRAM).description("periodogram test on linearized series").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_LIN_SP).description("spectral peaks on linearized series").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_RES_QS).description("qs seasonality test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_F).description("f test on seasonal dummies for the residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_FRIEDMAN).description("friedman seasonality test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_KW).description("kruskal-wallis seasonality test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_PERIODOGRAM).description("periodogram test on residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_RES_SP).description("spectral peaks on residuals").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SA_QS).description("qs seasonality test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_F).description("f test on seasonal dummies for the sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_FRIEDMAN).description("friedman seasonality test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_KW).description("kruskal-wallis seasonality test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_PERIODOGRAM).description("periodogram test on sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_SA_SP).description("spectral peaks on sa").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_I_QS).description("qs seasonality test on irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_F).description("f test on seasonal dummies for the irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_FRIEDMAN).description("friedman seasonality test on irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_KW).description("kruskal-wallis seasonality test on irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_PERIODOGRAM).description("periodogram test on irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(SEAS_I_SP).description("spectral peaks on irregular").outputClass(String.class).build())
            .item(Item.builder().name(SEAS_SA_AC1).description("autocorrelation(1) of the differenced sa component").outputClass(Double.class).build())
            .build();
    
    /*
    * Trading days
    */
    
    public final String  TD_RES_ALL = "td-res-all",
            TD_RES_LAST = "td-res-last",
            TD_I_ALL = "td-i-all",
            TD_I_LAST = "td-i-last",
            TD_SA_ALL = "td-sa-all",
            TD_SA_LAST = "td-sa-last";
    
    public final Dictionary GENERICTRADINGDAYS = AtomicDictionary.builder()
            .name("generic trading days tests")
            .item(Item.builder().name(TD_RES_ALL).description("f test on default td for the residuals").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(TD_RES_LAST).description("f test on default td for the residuals (last years)").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(TD_SA_ALL).description("f test on default td for the sa").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(TD_SA_LAST).description("f test on default td for the sa (last years)").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(TD_I_ALL).description("f test on default td for the irregular").outputClass(StatisticalTest.class).build())
            .item(Item.builder().name(TD_I_LAST).description("f test on default td for the irregular (last years)").outputClass(StatisticalTest.class).build())
            .build();
    


}

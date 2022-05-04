/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.highfreq;

import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.RegressionItem;
import demetra.toolkit.dictionaries.ArimaDictionaries;
import demetra.toolkit.dictionaries.AtomicDictionary;
import demetra.toolkit.dictionaries.ComplexDictionary;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.LikelihoodDictionaries;
import demetra.toolkit.dictionaries.PrefixedDictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;
import demetra.toolkit.dictionaries.RegressionDictionaries;
import demetra.toolkit.dictionaries.ResidualsDictionaries;
import demetra.toolkit.dictionaries.UtilityDictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ExtendedAirlineDictionaries {
    // Decomposition


    // finals
    public final String FINAL = "";
     
    public final static String AR = "ar", DIFF = "diff",
            PARAMETERS = "parameters",
            PHI = "phi", THETA = "theta", BTHETA = "btheta", PERIODS = "periods";
    public final String LOG = "log",
            ADJUST = "adjust", MEAN = "mean",
            SPAN = "span", ESPAN="espan", ARIMA = "arima", MODEL = "model",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", RESIDUALS = "residuals"
                , MAX = "regression.ml", ADVANCED = "regression.details";
    
    public final Dictionary EXAIRLINE_ESTIMATION = AtomicDictionary.builder()
            .name("extended airline")
            .item(AtomicDictionary.Item.builder().name(AR).description("ar parameter").outputClass(Boolean.class).build())
            .item(AtomicDictionary.Item.builder().name(DIFF).description("differencing order").outputClass(Integer.class).build())
            .item(AtomicDictionary.Item.builder().name(THETA).description("regular moving-average parameter").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(PHI).description("seasonal autoregressive parameter").outputClass(RegressionItem.class).build())
            .item(AtomicDictionary.Item.builder().name(BTHETA).description("seasonal moving-average parameter").outputClass(RegressionItem.class).type(Dictionary.EntryType.Array).build())
            .build();
    
    
    public final Dictionary REGAIRLINE = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.BASIC))
            .dictionary(new PrefixedDictionary(LIKELIHOOD, LikelihoodDictionaries.LIKELIHOOD))
            .dictionary(new PrefixedDictionary(ARIMA, ArimaDictionaries.ARIMA))
            .dictionary(new PrefixedDictionary(MODEL, ArimaDictionaries.SARIMA_ESTIMATION))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_DESC))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_EST))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .dictionary(new PrefixedDictionary(MAX, UtilityDictionaries.LL_MAX))
            .dictionary(new PrefixedDictionary(ADVANCED, RegressionDictionaries.REGRESSION_UTILITY))
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.REGRESSION_EFFECTS))
            .build();
    
    public static final String 
            Y_CMP="y_cmp", Y_CMP_F="y_cmp_f", Y_CMP_B="y_cmp_b",
            SA_CMP="sa_cmp", SA_CMP_F="sa_cmp_f", SA_CMP_B="sa_cmp_b",
            T_CMP="t_cmp", T_CMP_F="t_cmp_f", T_CMP_B="t_cmp_b",
            S_CMP="s_cmp", S_CMP_F="s_cmp_f", S_CMP_B="s_cmp_b",
            SY_CMP="sy_cmp", SY_CMP_F="sy_cmp_f", SY_CMP_B="sy_cmp_b",
            SW_CMP="sw_cmp", SW_CMP_F="sw_cmp_f", SW_CMP_B="sw_cmp_b",
            I_CMP="i_cmp", I_CMP_F="i_cmp_f", I_CMP_B="i_cmp_b",
           SW_LIN="sw_lin", SW_LIN_F="sw_lin_f", SW_LIN_B="sw_lin_b",
            SW_LIN_E="sw_lin_e", SW_LIN_EF="sw_lin_ef", SW_LIN_EB="sw_lin_eb",
            SY_LIN="sy_lin", SY_LIN_F="sy_lin_f", SY_LIN_B="sy_lin_b",
            SY_LIN_E="sy_lin_e", SY_LIN_EF="sy_lin_ef", SY_LIN_EB="sy_lin_eb";
    
   public final Dictionary LINDECOMPOSITION = AtomicDictionary.builder()
            .name("stochastic components")
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.Y_LIN).description("linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN).description("seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN).description("trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN).description("seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN).description("irregular linearized component").outputClass(TsData.class).build())
             .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN_E).description("seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN_E).description("trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN_E).description("seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN_E).description("irregular linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.Y_LIN_F).description("forecasts of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN_F).description("forecasts of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN_F).description("forecasts of the trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN_F).description("forecasts of the seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN_F).description("forecasts of the irregular linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.Y_LIN_EF).description("forecast errors of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN_EF).description("forecast errors of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN_EF).description("forecast errors of the trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN_EF).description("forecast errors of the seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN_EF).description("forecast errors of the irregular linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.Y_LIN_B).description("backcast of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN_B).description("backcast of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN_B).description("backcast of the trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN_B).description("backcast of the seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN_B).description("backcast of the irregular linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.Y_LIN_EB).description("backcast errors of the linearized series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.SA_LIN_EB).description("backcast errors of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.T_LIN_EB).description("backcast errors of the trend linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.S_LIN_EB).description("backcast errors of the seasonal linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SaDictionaries.I_LIN_EB).description("backcast errors of the irregular linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN_F).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN_F).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN_B).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN_B).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN_E).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN_E).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN_EF).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN_EF).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SW_LIN_EB).description("weekly linearized component").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(SY_LIN_EB).description("annual seasonality linearized component").outputClass(TsData.class).build())
            .build();
   
    public final Dictionary EXTENDEDAIRLINEDICTIONARY=ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, SaDictionaries.REGEFFECTS))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION_F))
            .dictionary(new PrefixedDictionary(SaDictionaries.DECOMPOSITION, LINDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SaDictionaries.DECOMPOSITION, SaDictionaries.CMPDECOMPOSITION))
            .build();

    
}

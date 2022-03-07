/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaDictionaries {

    public final String LOG = "log",
            ADJUST = "adjust", MEAN = "mean",
            SPAN = "span", ESPAN="espan", PERIOD = "period", ARIMA = "arima",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", RESIDUALS = "residuals"
                , MAX = "regression.ml", ADVANCED = "regression.details";
    
    public final Dictionary REGSARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.BASIC))
            .dictionary(new PrefixedDictionary(LIKELIHOOD, LikelihoodDictionaries.LIKELIHOOD))
            .dictionary(new PrefixedDictionary(ARIMA, ArimaDictionaries.SARIMA_ESTIMATION))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_DESC))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_EST))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .dictionary(new PrefixedDictionary(MAX, UtilityDictionaries.LL_MAX))
            .dictionary(new PrefixedDictionary(ADVANCED, RegressionDictionaries.REGRESSION_UTILITY))
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.REGRESSION_EFFECTS))
            .build();
    

}

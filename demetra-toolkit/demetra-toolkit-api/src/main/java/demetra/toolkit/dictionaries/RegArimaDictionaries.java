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
            REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX = "max", RESIDUALS = "residuals";
    
    public final Dictionary REGSARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(SPAN, UtilityDictionaries.OBS_SPAN))
            .dictionary(new PrefixedDictionary(ESPAN, UtilityDictionaries.OBS_SPAN))
            .dictionary(new PrefixedDictionary(MAX, UtilityDictionaries.LL_MAX))
            .dictionary(new PrefixedDictionary(LIKELIHOOD, LikelihoodDictionaries.LIKELIHOOD))
            .dictionary(new PrefixedDictionary(ARIMA, ArimaDictionaries.SARIMA))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.TRANSFORMATION))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_DESC))
            .dictionary(new PrefixedDictionary(REGRESSION, RegressionDictionaries.REGRESSION_EST))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .dictionary(new PrefixedDictionary(null, RegressionDictionaries.REGRESSION_EFFECTS))
            .build();
    

}

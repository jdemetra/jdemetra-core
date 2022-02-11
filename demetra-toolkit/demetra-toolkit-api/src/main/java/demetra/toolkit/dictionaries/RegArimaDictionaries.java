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
            SPAN = "span", PERIOD = "period",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX = "max", RESIDUALS = "residuals",
            NTD = "ntd", NLP = "nlp", NMH = "nmh", NEASTER = "neaster",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            COEFF = "coefficients", COVAR = "covar", COVAR_ML = "covar-ml", COEFFDESC = "description", REGTYPE = "type";
    
    
    public final Dictionary REGSARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(SPAN, UtilDictionaries.OBS_SPAN))
            .dictionary(new PrefixedDictionary(MAX, UtilDictionaries.LL_MAX))
            .dictionary(new PrefixedDictionary(LIKELIHOOD, LikelihoodDictionaries.LIKELIHOOD))
            .dictionary(new PrefixedDictionary(RESIDUALS, ResidualsDictionaries.RESIDUALS_DEFAULT))
            .build();
    

}

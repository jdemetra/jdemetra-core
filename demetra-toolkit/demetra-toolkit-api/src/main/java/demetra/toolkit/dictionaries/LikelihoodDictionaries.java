/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.toolkit.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class LikelihoodDictionaries {

    public final String LL = "ll", LLC = "adjustedll", SSQ = "ssqerr", AIC = "aic", BIC = "bic", AICC = "aicc", BICC = "bicc", BIC2 = "bic2", HQ = "hannanquinn",
            NPARAMS = "nparams", NOBS = "nobs", NEFFECTIVEOBS = "neffectiveobs", DF = "df", NDIFFUSE = "ndiffuse";

    public final AtomicDictionary LIKELIHOOD = AtomicDictionary.builder()
            .name("likelihood")
            .item(Item.builder().name(LL).description("log-likelihood").outputClass(double.class).build())
            .item(Item.builder().name(LLC).description("adjusted log-likelihood").outputClass(double.class).build())
            .item(Item.builder().name(SSQ).description("sum of squares").outputClass(double.class).build())
            .item(Item.builder().name(AIC).description("aic").outputClass(double.class).build())
            .item(Item.builder().name(BIC).description("bic").outputClass(double.class).build())
            .item(Item.builder().name(AICC).description("aicc").outputClass(double.class).build())
            .item(Item.builder().name(BICC).description("bicc").outputClass(double.class).build())
            .item(Item.builder().name(BIC2).description("bic corrected for length").outputClass(double.class).build())
            .item(Item.builder().name(HQ).description("hannan-quinn").outputClass(double.class).build())
            .item(Item.builder().name(NPARAMS).description("number of parameters").outputClass(int.class).build())
            .item(Item.builder().name(NOBS).description("number of observtions").outputClass(int.class).build())
            .item(Item.builder().name(NEFFECTIVEOBS).description("number of effective observtions").outputClass(int.class).build())
            .item(Item.builder().name(DF).description("degrees of freedom (=number of effective obs - number of parameters)").outputClass(int.class).build())
            .build();

    public final AtomicDictionary DIFFUSELIKELIHOOD = AtomicDictionary.builder()
            .name("diffuse likelihood")
            .item(Item.builder().name(LL).description("log-likelihood").outputClass(double.class).build())
            .item(Item.builder().name(LLC).description("adjusted log-likelihood").outputClass(double.class).build())
            .item(Item.builder().name(SSQ).description("sum of squares").outputClass(double.class).build())
            .item(Item.builder().name(AIC).description("aic").outputClass(double.class).build())
            .item(Item.builder().name(BIC).description("bic").outputClass(double.class).build())
            .item(Item.builder().name(AICC).description("aicc").outputClass(double.class).build())
            .item(Item.builder().name(HQ).description("hannan-quinn").outputClass(double.class).build())
            .item(Item.builder().name(NPARAMS).description("number of parameters").outputClass(int.class).build())
            .item(Item.builder().name(NOBS).description("number of observtions").outputClass(int.class).build())
            .item(Item.builder().name(NDIFFUSE).description("number of diffuse effects").outputClass(int.class).build())
            .item(Item.builder().name(DF).description("degrees of freedom (=number of obs - number of diffuse - number of parameters)").outputClass(int.class).build())
            .build();

}

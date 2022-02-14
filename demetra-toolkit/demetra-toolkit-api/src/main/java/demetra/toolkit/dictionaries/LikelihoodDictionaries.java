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
            .item(Item.builder().name(LL).description("log-likelihood").type(double.class).build())
            .item(Item.builder().name(LLC).description("adjusted log-likelihood").type(double.class).build())
            .item(Item.builder().name(SSQ).description("sum of squares").type(double.class).build())
            .item(Item.builder().name(AIC).description("aic").type(double.class).build())
            .item(Item.builder().name(BIC).description("bic").type(double.class).build())
            .item(Item.builder().name(AICC).description("aicc").type(double.class).build())
            .item(Item.builder().name(BICC).description("bicc").type(double.class).build())
            .item(Item.builder().name(BIC2).description("bic corrected for length").type(double.class).build())
            .item(Item.builder().name(HQ).description("hannan-quinn").type(double.class).build())
            .item(Item.builder().name(NPARAMS).description("number of parameters").type(int.class).build())
            .item(Item.builder().name(NOBS).description("number of observtions").type(int.class).build())
            .item(Item.builder().name(NEFFECTIVEOBS).description("number of effective observtions").type(int.class).build())
            .item(Item.builder().name(DF).description("degrees of freedom (=number of effective obs - number of parameters)").type(int.class).build())
            .build();

    public final AtomicDictionary DIFFUSELIKELIHOOD = AtomicDictionary.builder()
            .name("diffuse likelihood")
            .item(Item.builder().name(LL).description("log-likelihood").type(double.class).build())
            .item(Item.builder().name(LLC).description("adjusted log-likelihood").type(double.class).build())
            .item(Item.builder().name(SSQ).description("sum of squares").type(double.class).build())
            .item(Item.builder().name(AIC).description("aic").type(double.class).build())
            .item(Item.builder().name(BIC).description("bic").type(double.class).build())
            .item(Item.builder().name(AICC).description("aicc").type(double.class).build())
            .item(Item.builder().name(HQ).description("hannan-quinn").type(double.class).build())
            .item(Item.builder().name(NPARAMS).description("number of parameters").type(int.class).build())
            .item(Item.builder().name(NOBS).description("number of observtions").type(int.class).build())
            .item(Item.builder().name(NDIFFUSE).description("number of diffuse effects").type(int.class).build())
            .item(Item.builder().name(DF).description("degrees of freedom (=number of obs - number of diffuse - number of parameters)").type(int.class).build())
            .build();

}

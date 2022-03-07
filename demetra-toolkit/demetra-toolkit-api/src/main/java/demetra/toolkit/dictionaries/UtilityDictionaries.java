/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.math.matrices.Matrix;
import demetra.timeseries.TsPeriod;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UtilityDictionaries {

    public final String START = "start", END = "end", N = "n", NM = "missing";

    public final Dictionary OBS_SPAN = AtomicDictionary.builder()
            .name("span")
            .item(Item.builder().name(START).description("start").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(END).description("end").outputClass(TsPeriod.class).build())
            .item(Item.builder().name(N).description("number of obs").outputClass(Integer.class).build())
            .item(Item.builder().name(NM).description("number of missing").outputClass(Integer.class).build())
            .build();

    public final String P = "parameters", PCOVAR = "pcovar", PCOVAR_ML = "pcovar-ml", PCORR = "pcorr", SCORE = "pscore";

    public final Dictionary LL_MAX = AtomicDictionary.builder()
            .name("span")
            .item(Item.builder().name(P).description("parameters").outputClass(double[].class).build())
            .item(Item.builder().name(PCOVAR).description("unbiased covariance of the parameters").outputClass(Matrix.class).build())
            .item(Item.builder().name(PCOVAR_ML).description("maximum-likelihood covariance of the parameters").outputClass(Matrix.class).build())
            .item(Item.builder().name(PCORR).description("correlations of the parameters (unbiased)").outputClass(Matrix.class).build())
            .item(Item.builder().name(SCORE).description("scores of the parameters").outputClass(double[].class).build())
            .build();

}

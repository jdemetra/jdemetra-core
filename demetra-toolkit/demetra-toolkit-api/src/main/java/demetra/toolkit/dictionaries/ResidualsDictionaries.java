/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.stats.StatisticalTest;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ResidualsDictionaries {

    public final String RES = "res", TSRES = "tsres", SER = "ser", TYPE = "type";
    
    public final String MEAN = "mean", SKEW = "skewness", KURT = "kurtosis", DH = "doornikhansen", LB = "lb", LB2 = "lb2",
            SEASLB = "seaslb", BP = "bp", BP2 = "bp2", SEASBP = "seasbp", NRUNS= "nruns", LRUNS = "lruns",
            NUDRUNS = "nudruns", LUDRUNS = "ludruns" ; 
    
    public final Dictionary RESIDUALS = AtomicDictionary.builder()
            .name("residuals")
            .item(Item.builder().name(RES).description("residuals").type(double[].class).build())
            .item(Item.builder().name(TSRES).description("timeseries residuals").type(TsData.class).build())
            .item(Item.builder().name(SER).description("standard error of the residuals").type(double.class).build())
            .item(Item.builder().name(TYPE).description("type of residuals").type(String.class).build())
            .build();
    
    public final Dictionary RESIDUALS_TESTS = AtomicDictionary.builder()
            .name("tests on residuals")
            .item(Item.builder().name(MEAN).description("mean test").type(StatisticalTest.class).build())
            .item(Item.builder().name(SKEW).description("skewness tess ").type(StatisticalTest.class).build())
            .item(Item.builder().name(KURT).description("kurtosis test").type(StatisticalTest.class).build())
            .item(Item.builder().name(DH).description("doornik-hansen normality test").type(StatisticalTest.class).build())
            .item(Item.builder().name(LB).description("ljung-box test").type(StatisticalTest.class).build())
            .item(Item.builder().name(BP).description("box-pierce test").type(StatisticalTest.class).build())
            .item(Item.builder().name(LB2).description("ljun-box test on squares").type(StatisticalTest.class).build())
            .item(Item.builder().name(BP2).description("box-pierce test on squares").type(StatisticalTest.class).build())
            .item(Item.builder().name(SEASLB).description("seasonal ljung-box test").type(StatisticalTest.class).build())
            .item(Item.builder().name(SEASBP).description("seasonal box-pierce test").type(StatisticalTest.class).build())
            .item(Item.builder().name(NRUNS).description("test on the number of runs").type(StatisticalTest.class).build())
            .item(Item.builder().name(LRUNS).description("test on the length of runs").type(StatisticalTest.class).build())
            .item(Item.builder().name(NUDRUNS).description("test on the number of up and down runs").type(StatisticalTest.class).build())
            .item(Item.builder().name(LUDRUNS).description("test on the length of up and down runs").type(StatisticalTest.class).build())
            .build();
    
        public final Dictionary RESIDUALS_DEFAULT = ComplexDictionary.builder()
                .dictionary(new PrefixedDictionary(null, RESIDUALS))
                .dictionary(new PrefixedDictionary(null, RESIDUALS_TESTS))
                .build();
                

}

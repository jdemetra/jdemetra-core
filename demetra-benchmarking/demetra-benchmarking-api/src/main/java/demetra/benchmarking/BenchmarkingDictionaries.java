/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.benchmarking;

import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.AtomicDictionary;
import demetra.toolkit.dictionaries.Dictionary;
import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
@Development(status = Development.Status.Beta)
public class BenchmarkingDictionaries {

    public static final String ORIGINAL = "original", TARGET = "target", BENCHMARKED = "benchmarked", BIRATIO="bi-ratio";

    public final Dictionary BENCHMARKING = AtomicDictionary.builder()
            .name("benchmarking")
            .item(AtomicDictionary.Item.builder().name(ORIGINAL).description("Original series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(TARGET).description("Aggregation constraint").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(BENCHMARKED).description("Benchmarked series").outputClass(TsData.class).build())
            .item(AtomicDictionary.Item.builder().name(BIRATIO).description("BI ratio").outputClass(TsData.class).build())
            .build();


}

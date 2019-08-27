/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts;

import jdplus.msts.internal.AggregationItem;
import jdplus.msts.internal.CumulatorItem;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class DerivedModels {

    public StateItem cumulator(String name, StateItem core, int period, int start) {
        return new CumulatorItem(name, core, period, start);
    }

    public StateItem aggregation(String name, StateItem... core) {
        return new AggregationItem(name, core);
    }
}

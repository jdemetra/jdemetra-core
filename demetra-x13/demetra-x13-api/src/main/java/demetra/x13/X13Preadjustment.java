/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13;

import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.x11.SeasonalFilterOption;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class X13Preadjustment {
    
    TsData a1, a1a, a1v, a6, a7, a8, a8t, a8s, a8i, a9, a9u, a9sa, a9ser;
}

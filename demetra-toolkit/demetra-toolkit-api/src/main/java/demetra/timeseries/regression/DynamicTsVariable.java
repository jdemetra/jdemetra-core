/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TsData;
import demetra.timeseries.TsMoniker;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class DynamicTsVariable implements IUserVariable {

    @lombok.NonNull
    private final TsMoniker moniker;
    private final TsData data;
    
 
    @Override
    public int dim() {
        return 1;
    }

}

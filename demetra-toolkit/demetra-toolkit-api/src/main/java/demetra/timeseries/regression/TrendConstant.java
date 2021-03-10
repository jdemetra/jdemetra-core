/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import java.time.LocalDateTime;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@Development(status=Development.Status.Release)
public class TrendConstant implements ISystemVariable {
    
    public static final String NAME="const";
    
    private int d, bd;
//    private LocalDateTime start; TODO

    @Override
    public int dim() {
        return 1;
    }

     @Override
    public <D extends TimeSeriesDomain<?>> String description(D context){
        return NAME;
    }
   
}

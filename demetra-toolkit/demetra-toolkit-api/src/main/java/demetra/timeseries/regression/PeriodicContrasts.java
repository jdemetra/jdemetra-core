/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
public class PeriodicContrasts implements IUserTsVariable {
    private int period;
    private LocalDateTime reference;
    
    public PeriodicContrasts(int period){
        this.period=period;
        this.reference=TsPeriod.DEFAULT_EPOCH;
    }

    @Override
    public int dim() {
        return period-1;
    }
    
}

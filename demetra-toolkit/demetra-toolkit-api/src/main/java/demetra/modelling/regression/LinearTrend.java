/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
public class LinearTrend implements IUserTsVariable {
    
    private LocalDateTime start;

    @Override
    public int dim() {
        return 1;
    }

}


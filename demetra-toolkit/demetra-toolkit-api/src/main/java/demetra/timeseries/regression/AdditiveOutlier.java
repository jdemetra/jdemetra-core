/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
public class AdditiveOutlier implements IOutlier {
    
    public static final String CODE = "AO";

    private LocalDateTime position;
    
    @Override
    public String getCode(){
        return CODE;
    }
}
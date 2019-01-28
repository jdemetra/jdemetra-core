/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
public class SwitchOutlier implements IOutlier {
    
    public static final String CODE = "WO";

    private LocalDateTime position;
    
    @Override
    public String getCode(){
        return CODE;
    }
}

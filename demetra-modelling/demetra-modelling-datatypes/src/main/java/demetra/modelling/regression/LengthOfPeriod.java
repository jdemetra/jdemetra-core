/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.calendars.LengthOfPeriodType;

/**
 *
 * @author palatej
 */
@lombok.Value
public class LengthOfPeriod implements ILengthOfPeriodVariable {
    private LengthOfPeriodType type;
    
}

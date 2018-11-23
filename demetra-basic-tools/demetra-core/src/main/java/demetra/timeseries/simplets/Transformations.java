/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.simplets;

import demetra.data.ConstTransformation;
import demetra.data.ExpTransformation;
import demetra.data.LogTransformation;
import demetra.data.OperationType;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.transformation.TimeSeriesTransformation;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Transformations {

    public TsDataTransformation log() {
        return new GenericTransformation(LogTransformation.EXEMPLAR);
    }

    public TsDataTransformation exp() {
        return new GenericTransformation(ExpTransformation.EXEMPLAR);
    }

    public TsDataTransformation lengthOfPeriod(LengthOfPeriodType lp) {
        return new LengthOfPeriodTransformation(lp);
    }
    
    public TsDataTransformation op(OperationType op, double factor) {
        return new GenericTransformation(new ConstTransformation(op, factor));
    }
    
    
}

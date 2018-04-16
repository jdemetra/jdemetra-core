/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;

/**
 *
 * @author Kristof Bayens
 */
public class TransformSpec extends BaseRegArimaSpec {

    @Override
    public String toString() {
        return "";
    }

    private ec.tstoolkit.modelling.arima.x13.TransformSpec inner() {
        return core.getTransform();
    }

    TransformSpec(RegArimaSpecification spec) {
        super(spec);
    }


    public String getFunction() {
        return inner().getFunction().name();
    }

    public void setFunction(String fn) {
        DefaultTransformationType value=DefaultTransformationType.valueOf(fn);
        inner().setFunction(value);
        core.getRegression().getTradingDays().setAutoAdjust(value == DefaultTransformationType.Auto);
        if (value == DefaultTransformationType.None && inner().getAdjust() != LengthOfPeriodType.None){
            inner().setAdjust(LengthOfPeriodType.None);
            core.getRegression().getTradingDays().setLengthOfPeriod(LengthOfPeriodType.LeapYear);
        }
    }

    public double getAic() {
        return inner().getAICDiff();
    }

    public void setAic(double value) {
        inner().setAICDiff(value);
    }

    public String getAdjust() {
        return inner().getAdjust().name();
    }

    public void setAdjust(String adj) {
        LengthOfPeriodType value=LengthOfPeriodType.valueOf(adj);
        inner().setAdjust(value);
        if (value != LengthOfPeriodType.None) {
            core.getRegression().getTradingDays().setLengthOfPeriod(LengthOfPeriodType.None);
        }
    }
}

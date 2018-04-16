/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.timeseries.regression.OutlierType;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Kristof Bayens
 */
public class OutlierSpec extends BaseRegArimaSpec {

    OutlierSpec(RegArimaSpecification spec) {
        super(spec);
    }

    @Override
    public String toString() {
        return "";
    }

    private ec.tstoolkit.modelling.arima.x13.OutlierSpec inner() {
        return core.getOutliers();
    }

    public boolean isEnabled() {
        return inner().getTypesCount() > 0;
    }

    public void setEnabled(boolean value) {
        
        if (!value) {
            inner().clearTypes();
        } else if (inner().getTypesCount() == 0) {
            inner().add(OutlierType.AO);
            inner().add(OutlierType.LS);
            inner().add(OutlierType.TC);
        }
    }

    public SpanSelector getSpan() {
        return new SpanSelector(inner().getSpan());
    }

    public boolean isAO() {
        return inner().search(OutlierType.AO) != null;
    }

    public void setAO(boolean value) {
        if (value) {
            inner().add(OutlierType.AO);
        } else {
            inner().remove(OutlierType.AO);
        }
    }

    public boolean isLS() {
        return inner().search(OutlierType.LS) != null;
    }

    public void setLS(boolean value) {
        if (value) {
            inner().add(OutlierType.LS);
        } else {
            inner().remove(OutlierType.LS);
        }
    }

    public boolean isTC() {
        return inner().search(OutlierType.TC) != null;
    }

    public void setTC(boolean value) {
        if (value) {
            inner().add(OutlierType.TC);
        } else {
            inner().remove(OutlierType.TC);
        }
    }

    public boolean isSO() {
        return inner().search(OutlierType.SO) != null;
    }

    public void setSO(boolean value) {
        if (value) {
            inner().add(OutlierType.SO);
        } else {
            inner().remove(OutlierType.SO);
        }
    }

    public boolean isDefaultVa() {
        return inner().getDefaultCriticalValue() == 0;

    }

    public void setDefaultVa(boolean value) {
        if (value) {
            inner().setDefaultCriticalValue(0);
        } else {
            inner().setDefaultCriticalValue(4);
        }
    }

    public double getVa() {
        return inner().getDefaultCriticalValue() == 0 ? 4 : inner().getDefaultCriticalValue();

    }

    public void setVa(double value) {
        inner().setDefaultCriticalValue(value);
    }

    public double getTCRate() {
        return inner().getMonthlyTCRate();
    }

    public void setTCRate(double value) {
        inner().setMonthlyTCRate(value);
    }

    public String getMethod() {
        return inner().getMethod().name();
    }

    public void setMethod(String value) {
        inner().setMethod(ec.tstoolkit.modelling.arima.x13.OutlierSpec.Method.valueOf(value));
    }

    public int getLSRun() {
        return inner().getLSRun();
    }

    public void setLSRun(int value) {
        inner().setLSRun(value);
    }
}
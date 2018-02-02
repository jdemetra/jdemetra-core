/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierType;
import jdr.spec.ts.SpanSelector;

/**
 *
 * @author Kristof Bayens
 */
public class OutlierSpec extends BaseTramoSpec {

    OutlierSpec(TramoSpecification spec) {
        super(spec);
    }

    @Override
    public String toString() {
        return "";
    }

    private ec.tstoolkit.modelling.arima.tramo.OutlierSpec inner() {
        return core.getOutliers();
    }

    public boolean isOutliersDetectionEnabled() {
        return inner().isUsed();
    }

    public void setOutliersDetectionEnabled(boolean value) {
        ec.tstoolkit.modelling.arima.tramo.OutlierSpec spec = inner();
        if (!value) {
            spec.clearTypes();
        } else {
            //} else if (spec.getTypes() != null) {
            spec.add(OutlierType.AO);
            spec.add(OutlierType.LS);
            spec.add(OutlierType.TC);
        }
    }

    public SpanSelector getSpan() {
        return new SpanSelector(inner().getSpan());
    }

    public boolean isAO() {
        return inner().contains(OutlierType.AO);
    }

    public void setAO(boolean ao) {
        ec.tstoolkit.modelling.arima.tramo.OutlierSpec spec = inner();
        if (ao) {
            spec.add(OutlierType.AO);
        } else {
            spec.remove(OutlierType.AO);
        }
    }

    public boolean isLS() {
        return inner().contains(OutlierType.LS);
    }

    public void setLS(boolean ls) {
        ec.tstoolkit.modelling.arima.tramo.OutlierSpec spec = inner();
        if (ls) {
            spec.add(OutlierType.LS);
        } else {
            spec.remove(OutlierType.LS);
        }
    }

    public boolean isTC() {
        return inner().contains(OutlierType.TC);
    }

    public void setTC(boolean tc) {
        ec.tstoolkit.modelling.arima.tramo.OutlierSpec spec = inner();
        if (tc) {
            spec.add(OutlierType.TC);
        } else {
            spec.remove(OutlierType.TC);
        }
    }

    public boolean isSO() {
        return inner().contains(OutlierType.SO);
    }

    public void setSO(boolean so) {
        ec.tstoolkit.modelling.arima.tramo.OutlierSpec spec = inner();
        if (so) {
            spec.add(OutlierType.SO);
        } else {
            spec.remove(OutlierType.SO);
        }
    }

    public double getVa() {
        double va = inner().getCriticalValue();
        return va == 0 ? 3.5 : va;
    }

    public void setVa(double value) {
        inner().setCriticalValue(value);
    }

    public boolean isAutoVa() {
        return inner().getCriticalValue() == 0;
    }

    public void setAutoVa(boolean value) {
        inner().setCriticalValue(value ? 0 : 3.5);
    }

    public double getTCRate() {
        return inner().getDeltaTC();
    }

    public void setTCRate(double value) {
        inner().setDeltaTC(value);
    }

    public boolean isEML() {
        return inner().isEML();
    }

    public void setEML(boolean value) {
        inner().setEML(value);
    }
}
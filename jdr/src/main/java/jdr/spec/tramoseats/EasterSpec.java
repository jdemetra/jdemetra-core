/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;

/**
 *
 * @author Jean Palate
 */
public class EasterSpec extends BaseTramoSpec {

    private ec.tstoolkit.modelling.arima.tramo.EasterSpec inner() {
        ec.tstoolkit.modelling.arima.tramo.EasterSpec easter = core.getRegression().getCalendar().getEaster();
        return easter;
    }

    private void disable() {
        core.getRegression().getCalendar().getEaster().setOption(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.Unused);
    }

    public EasterSpec(TramoSpecification spec) {
        super(spec);
    }

    public String getOption() {
        return inner().getOption().name();
    }

    public void setOption(String value) {
        inner().setOption(ec.tstoolkit.modelling.arima.tramo.EasterSpec.Type.valueOf(value));
    }

    public int getDuration() {
        return inner().getDuration();
    }

    public void setDuration(int value) {
        inner().setDuration(value);
    }

    public boolean isTest() {
        return inner().isUsed() && inner().isTest();
    }

    public void setTest(boolean value) {
        inner().setTest(value);
    }

    public boolean isJulian() {
        return inner().isUsed() && inner().isJulian();
    }

    public void setJulian(boolean value) {
        inner().setJulian(value);
    }

}

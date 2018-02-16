/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;

/**
 *
 * @author Jean Palate
 */
public class EasterSpec extends BaseRegArimaSpec {

    @Override
    public String toString() {
        return isEnabled() ? "in use" : "";
    }

    private MovingHolidaySpec getInner() {
        if (core.getRegression() == null) {
            return null;
        }
        return core.getRegression().getEaster();
    }

    public boolean isEnabled() {
        return getInner() != null;
    }

    private MovingHolidaySpec inner() {
        if (core.getRegression() == null) {
            core.setRegression(new RegressionSpec());
        }

        MovingHolidaySpec easter = core.getRegression().getEaster();

        if (easter == null) {
            easter = MovingHolidaySpec.easterSpec(true, isJulian());
            core.getRegression().add(easter);
        }
        return easter;
    }

    // should be changed in the future, with new moving holidays !!!
    public void setEnabled(boolean value) {
        if (!value) {
            core.getRegression().clearMovingHolidays();
        } else {
            inner();
        }
    }

    public EasterSpec(RegArimaSpecification spec) {
        super(spec);
    }

    public String getTest() {
        MovingHolidaySpec spec = getInner();
        if (spec == null) {
            return "None";
        } else {
            return spec.getTest().name();
        }
    }

    public void setTest(String value) {
        MovingHolidaySpec spec = inner();
        spec.setTest(RegressionTestSpec.valueOf(value));
    }

    public int getDuration() {
        MovingHolidaySpec spec = getInner();
        if (spec == null) {
            return MovingHolidaySpec.DEF_EASTERDUR;
        } else {
            return spec.getW();
        }
    }

    public void setDuration(int value) {
        inner().setW(value);
    }

    public boolean isJulian() {
        MovingHolidaySpec spec = getInner();
        if (spec == null) {
            return false;
        } else {
            return spec.getType() == MovingHolidaySpec.Type.JulianEaster;
        }
    }

    public void setJulian(boolean value) {
        inner().setType(value ? MovingHolidaySpec.Type.JulianEaster : MovingHolidaySpec.Type.Easter);
    }

}

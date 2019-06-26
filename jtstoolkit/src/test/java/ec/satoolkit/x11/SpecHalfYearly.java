/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.modelling.DefaultTransformationType;

/**
 *
 * @author Christiane Hofer
 */
public class SpecHalfYearly {

    public static final X13Specification getSpecHKAS() {
        X13Specification x13spec = X13Specification.RSA0.clone();
        X11Specification spec = new X11Specification();
        spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        spec.setLowerSigma(2);
        spec.setUpperSigma(3.0);
        spec.setHendersonFilterLength(5);
        spec.setCalendarSigma(CalendarSigma.All);
        spec.setExcludefcst(true);
        x13spec.setX11Specification(spec);
        return x13spec;
    }

    public static final X13Specification getSpecAgg() {
        X13Specification x13spec = X13Specification.RSA0.clone();
        x13spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        X11Specification spec = new X11Specification();
        spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        spec.setLowerSigma(2);
        spec.setUpperSigma(3.0);
        spec.setHendersonFilterLength(5);
        spec.setCalendarSigma(CalendarSigma.All);
        spec.setExcludefcst(true);
        x13spec.setX11Specification(spec);
        return x13spec;
    }

    public static final X13Specification getSpecSim() {
        X13Specification x13spec = X13Specification.RSA0.clone();
        x13spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        X11Specification spec = new X11Specification();
        spec.setSeasonalFilter(SeasonalFilterOption.S3X5);
        spec.setLowerSigma(2);
        spec.setUpperSigma(3.0);
        spec.setHendersonFilterLength(5);
        spec.setCalendarSigma(CalendarSigma.Signif);
        spec.setExcludefcst(true);
        x13spec.setX11Specification(spec);
        return x13spec;
    }

}

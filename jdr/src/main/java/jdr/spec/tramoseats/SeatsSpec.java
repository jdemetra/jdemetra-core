/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.satoolkit.seats.SeatsSpecification;
import ec.satoolkit.seats.SeatsSpecification.EstimationMethod;

/**
 *
 * @author Kristof Bayens
 */
public class SeatsSpec {
    
    private final SeatsSpecification core;

    public SeatsSpec(SeatsSpecification spec) {
      core = spec;
    }

    public double getRMod() {
        return core.getTrendBoundary();
    }

    public void setRMod(double value) {
        core.setTrendBoundary(value);
    }

    public double getSMod() {
        return core.getSeasBoundary();
    }

    public void setSMod(double value) {
        core.setSeasBoundary(value);
    }
    
    public double getSMod1() {
        return core.getSeasBoundary1();
    }

    public void setSMod1(double value) {
        core.setSeasBoundary1(value);
    }
    
    public double getEpsPhi() {
        return core.getSeasTolerance();
    }

    public void setEpsPhi(double value) {
        core.setSeasTolerance(value);
    }

    public String getApproximationMode() {
        return core.getApproximationMode().name();
    }

    public void setApproximationMode(String value) {
        core.setApproximationMode(SeatsSpecification.ApproximationMode.valueOf(value));
    }

    public double getXl() {
        return core.getXlBoundary();
    }

    public void setXl(double value) {
        core.setXlBoundary(value);
    }

    public String getMethod() {
        return core.getMethod().name();
    }

    public void setMethod(String value) {
        core.setMethod(EstimationMethod.valueOf(value));
    }
}

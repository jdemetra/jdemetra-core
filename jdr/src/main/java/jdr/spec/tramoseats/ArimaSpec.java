/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.tramoseats;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.tramo.AutoModelSpec;

/**
 *
 * @author Kristof Bayens
 */
public class ArimaSpec extends BaseTramoSpec {

    ArimaSpec(TramoSpecification spec) {
        super(spec);
    }

    private AutoModelSpec ami() {
        return core.getAutoModel();
    }

    private ec.tstoolkit.modelling.arima.tramo.ArimaSpec arima() {
        return core.getArima();
    }

    @Override
    public String toString() {
        if (core.isUsingAutoModel()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append('(').append(core.getArima().getP()).append(", ").append(core.getArima().getD()).append(", ").append(core.getArima().getQ()).append(")(").append(core.getArima().getBP()).append(", ").append(core.getArima().getBD()).append(", ").append(core.getArima().getBQ()).append(')');
            return builder.toString();
        }
    }

    public boolean isEnabled() {
        AutoModelSpec spec = ami();
        return spec.isEnabled();
    }

    public void setEnabled(boolean value) {
        ami().setEnabled(value);
    }

    public boolean isAcceptDefault() {
        return ami().isAcceptDefault();
    }

    public void setAcceptDefault(boolean value) {
        ami().setAcceptDefault(value);
    }

    public boolean isAmiCompare() {
        return ami().isAmiCompare();
    }

    public void setAmiCompare(boolean value) {
        ami().setAmiCompare(value);
    }

    public double getUb1() {
        return ami().getUb1();
    }

    public void setUb1(double value) {
        ami().setUb1(value);
    }

    public double getUb2() {
        return ami().getUb2();
    }

    public void setUb2(double value) {
        ami().setUb2(value);
    }

    public double getCancel() {
        return ami().getCancel();
    }

    public void setCancel(double value) {
        ami().setCancel(value);
    }

    public double getPcr() {
        return ami().getPcr();
    }

    public void setPcr(double value) {
        ami().setPcr(value);
    }

    public double getTsig() {
        return ami().getTsig();
    }

    public void setTsig(double value) {
        ami().setTsig(value);
    }

    public double getPc() {
        return ami().getPc();
    }

    public void setPc(double value) {
        ami().setPc(value);
    }

    public int getP() {
        return arima().getP();
    }

    public void setP(int value) {
        arima().setP(value);
    }

    public int getD() {
        return arima().getD();
    }

    public void setD(int value) {
        arima().setD(value);
    }

    public int getQ() {
        return arima().getQ();
    }

    public void setQ(int value) {
        arima().setQ(value);
    }

    public int getBP() {
        return arima().getBP();
    }

    public void setBP(int value) {
        arima().setBP(value);
    }

    public int getBD() {
        return arima().getBD();
    }

    public void setBD(int value) {
        arima().setBD(value);
    }

    public int getBQ() {
        return arima().getBQ();
    }

    public void setBQ(int value) {
        arima().setBQ(value);
    }

    public Parameter[] getPhi() {
        return arima().getPhi();
    }

    public void setPhi(Parameter[] value) {
        arima().setPhi(value);
    }

    public Parameter[] getTheta() {
        return arima().getTheta();
    }

    public void setTheta(Parameter[] value) {
        arima().setTheta(value);
    }

    public Parameter[] getBPhi() {
        return arima().getBPhi();
    }

    public void setBPhi(Parameter[] value) {
        arima().setBPhi(value);
    }

    public Parameter[] getBTheta() {
        return arima().getBTheta();
    }

    public void setBTheta(Parameter[] value) {
        arima().setBTheta(value);
    }

    public boolean isMean() {
        return arima().isMean();
    }

    public void setMean(boolean value) {
        arima().setMean(value);
    }

}

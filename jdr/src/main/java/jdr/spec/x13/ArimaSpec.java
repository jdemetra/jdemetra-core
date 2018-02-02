/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.x13;

import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;

/**
 *
 * @author Kristof Bayens
 */
public class ArimaSpec extends BaseRegArimaSpec {

    ArimaSpec(RegArimaSpecification spec) {
        super(spec);
    }

    private ec.tstoolkit.modelling.arima.x13.ArimaSpec arima() {
        return core.getArima();
    }

    private AutoModelSpec ami() {
        return core.getAutoModel();
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

    public boolean isAmiEnabled() {
        return ami().isEnabled();
    }

    public void setAmiEnabled(boolean value) {
        ami().setEnabled(value);
    }

    public boolean isAcceptDefault() {
        return ami().isAcceptDefault();
    }

    public void setAcceptDefault(boolean value) {
        ami().setAcceptDefault(value);
    }

    public boolean isBalanced() {
        return ami().isBalanced();
    }

    public void setBalanced(boolean value) {
        ami().setBalanced(value);
    }

    public boolean isMixed() {
        return ami().isMixed();
    }

    public void setMixed(boolean value) {
        ami().setMixed(value);
    }

    public boolean isCheckMu() {
        return ami().isCheckMu();
    }

    public void setCheckMu(boolean value) {
        ami().setCheckMu(value);
    }

    public boolean isHannanRissannen() {
        return ami().isHannanRissannen();
    }

    public void setHannanRissannen(boolean value) {
        ami().setHannanRissanen(value);
    }

    public double getUbFinal() {
        return ami().getUnitRootLimit();
    }

    public void setUbFinal(double value) {
        ami().setUnitRootLimit(value);
    }

    public double getUb1() {
        return ami().getInitialUnitRootLimit();
    }

    public void setUb1(double value) {
        ami().setInitialUnitRootLimit(value);
    }

    public double getUb2() {
        return ami().getFinalUnitRootLimit();
    }

    public void setUb2(double value) {
        ami().setFinalUnitRootLimit(value);
    }

    public double getCancel() {
        return ami().getCancelationLimit();
    }

    public void setCancel(double value) {
        ami().setCancelationLimit(value);
    }

    public double getPcr() {
        return ami().getLjungBoxLimit();
    }

    public void setPcr(double value) {
        ami().setLjungBoxLimit(value);
    }

    public double getTsig() {
        return ami().getArmaSignificance();
    }

    public void setTsig(double value) {
        ami().setArmaSignificance(value);
    }

    public double getPredCV() {
        return ami().getPercentReductionCV();
    }

    public void setPredCV(double value) {
        ami().setPercentReductionCV(value);
    }

    public double getPredSE() {
        return ami().getPercentRSE();
    }

    public void setPredSE(double value) {
        ami().setPercentRSE(value);
    }

}

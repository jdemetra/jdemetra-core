/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.timeseries.regression;

/**
 *
 * @author PALATEJ
 */
public enum ResidualsType {
    /**
     * no information
     */
    Undefined,
    /**
     * One step ahead forecast error
     */
    OneStepAHead,
    /**
     * Maximum likelihood estimates
     */
    MLEstimate,
    /**
     * Independent residuals obtained through a QR estimation of
     * the regression model (see for instance TRAMO)
     */
    QR_Transformed,
    /**
     * L^-1(y-Xb)
     */
    FullResiduals;

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.satoolkit.DecompositionMode;
import ec.tstoolkit.design.Development;
import ec.satoolkit.diagnostics.CochranTest;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * This Extremvalues Korrektor uses the Cochran Test to decide weather the
 * DefaulExtremValuesKorrektor or the PeriodSpecificExtremeValuesCorrector is
 * uses.
 *
 * @author Christiane Hofer only the methods of the interface are implemented
 */
@Development(status = Development.Status.Exploratory)
public class CochranDependentExtremeValuesCorrector extends DefaultX11Algorithm
        implements IExtremeValuesCorrector {

    private IExtremeValuesCorrector extremeValuesCorrector_;
    private DecompositionMode mode_;
    private double lsig_, usig_;

    @Override
    /**
     * Searches the extreme values in a given series and decides for series with
     * more then 5 years if it is nessesary to calculate period specific extreme
     * value correction with the PeriodSpecificExtremeValuesCorrector instead of
     * the DefaultExtremeValuesCorrector
     *
     * @param s The analysed series
     * @return The number of extreme values that have been detected (>= 0)
     */
    public int analyse(TsData s) {

        extremeValuesCorrector_.setContext(context);
        extremeValuesCorrector_.setSigma(lsig_, usig_);
        return extremeValuesCorrector_.analyse(s);
    }

    public void testCochran(TsData s){
            if (s.getDomain().getFullYearsCount() < 6) {      // the hypothesis of identical month variances shouldn't be rejected for less than 6 years,
            //therefore the test isn't calculated
            extremeValuesCorrector_ = new DefaultExtremeValuesCorrector();

        } else {
            CochranTest cochranTest = new CochranTest(s, isMultiplicative());
            cochranTest.calcCochranTest();
            if (cochranTest.getTestResult()) {
                extremeValuesCorrector_ = new DefaultExtremeValuesCorrector();            
            } else {
                extremeValuesCorrector_ = new PeriodSpecificExtremeValuesCorrector();
            }
        }
    }
    
    @Override
    public TsData computeCorrections(TsData s) {
        return extremeValuesCorrector_.computeCorrections(s);
    }

    @Override
    public TsData applyCorrections(TsData s, TsData corrections) {
        return extremeValuesCorrector_.applyCorrections(s, corrections);
    }

    @Override
    public TsData getObservationWeights() {
        return extremeValuesCorrector_.getObservationWeights();
    }

    @Override
    public TsData getCorrectionFactors() {
        return extremeValuesCorrector_.getCorrectionFactors();
    }

    /**
     * Sets the limits for the detection of extreme values.
     *
     * @param lsig The low sigma value
     * @param usig The high sigma value
     * @throws An exception is thrown when the limits are invalid (usig <= lsig
     * or lsig <= 0.5).
     */
    public void setSigma(double lsig, double usig) {
        lsig_ = lsig;
        usig_ = usig;
    }

    /**
     *
     * @param mode is the Decompositon mode, needed for the calculation of the
     * CochranTest
     */
    public void setMode(DecompositionMode mode) {
        mode_ = mode;
    }
}

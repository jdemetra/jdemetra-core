/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression.modelling;

import demetra.arima.SarimaModel;
import demetra.processing.ProcessingLog;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class RegSarimaResults {

    @lombok.NonNull
    private LinearModelEstimation<SarimaModel> regarima;

    @lombok.Singular
    private Map<String, Object> addtionalResults;
   
    @lombok.Singular
    private List<ProcessingLog.Information> logs;

}

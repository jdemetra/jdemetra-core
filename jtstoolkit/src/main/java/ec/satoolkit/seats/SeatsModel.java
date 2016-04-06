/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.satoolkit.seats;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityDetector;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeatsModel {

    private final TsData series;
    private boolean meanCorrection;
    private SarimaModel model;
    private double ser;
    private IArimaModel noisyModel;
    private final boolean hasSeas;
    private boolean cutoff, changed;

    /**
     *
     * @param series
     * @param model
     * @param meanCorrection
     */
    public SeatsModel(final TsData series, final SarimaModel model,
            final boolean meanCorrection) {
        this.series = series;
        this.model = model;
        this.meanCorrection = meanCorrection;
        SeasonalityDetector detector=new SeasonalityDetector();
        this.hasSeas=detector.hasSeasonality(series);
    }
    
    public boolean hasSignificantSeasonality(){
        return hasSeas;
    }
    /**
     * @return the model
     */
    public SarimaModel getSarima() {
        return model;
    }

    /**
     * @return the model
     */
    public IArimaModel getArima() {
        if (noisyModel == null) {
            return model;
        }
        else {
            return noisyModel;
        }

    }

    /**
     * 
     * @return
     */
    public RegArimaModel<SarimaModel> getRegarima() {
        RegArimaModel<SarimaModel> regarima = new RegArimaModel<>(
                model, new DataBlock(series.internalStorage()));
        regarima.setMeanCorrection(meanCorrection);
        return regarima;
    }

    public double getSer() {
        if (ser == 0) {
            computeSer();
        }
        return ser;
    }

    public void setSer(double value) {
        ser = value;
    }

    /**
     * @return the series
     */
    public TsData getSeries() {
        return series;
    }

    /**
     * @return the meanCorrection
     */
    public boolean isMeanCorrection() {
        return meanCorrection;
    }

    /**
     * @param meanCorrection
     *            the meanCorrection to set
     */
    public void setMeanCorrection(boolean meanCorrection) {
        this.meanCorrection = meanCorrection;
    }

    /**
     * @param model
     *            the model to set
     */
    public void setModel(SarimaModel model) {
        this.model = model;
        ser = 0;
        noisyModel = null;
    }
    
    public void setCutOff(boolean co){
        cutoff=co;
    }
    
    public boolean isCutOff(){
        return cutoff;
    }

    public void setChanged(boolean c){
        changed=c;
    }
    
    public boolean isChanged(){
        return changed;
    }
    /**
     * 
     * @param spec
     */
    public void setModelSpecification(SarimaSpecification spec) {
        setModel(new SarimaModel(spec));
    }

    public void setNoisyModel(IArimaModel model) {
        this.noisyModel=model;
        ser = 0;
    }
    
    public IArimaModel getNoisyModel(){
        return noisyModel;
    }

    private void computeSer() {
        if (noisyModel == null) {
            RegArimaModel<SarimaModel> regarima = this.getRegarima();
            ConcentratedLikelihood ll = regarima.computeLikelihood();
            RegArimaEstimation<SarimaModel> est = new RegArimaEstimation<>(regarima, ll);
            LikelihoodStatistics stats = est.statistics(this.getSarima().getSpecification().getParametersCount(), 0);
            ser = Math.sqrt(stats.SsqErr / (stats.effectiveObservationsCount-stats.estimatedParametersCount));
        }
        else {
            RegArimaModel<IArimaModel> regarima = new RegArimaModel<>(
                    noisyModel, new DataBlock(series.internalStorage()));
            regarima.setMeanCorrection(meanCorrection);
            ConcentratedLikelihood ll = regarima.computeLikelihood();
            RegArimaEstimation<IArimaModel> est = new RegArimaEstimation<>(regarima, ll);
            LikelihoodStatistics stats = est.statistics(this.getSarima().getSpecification().getParametersCount(), 0);
            ser = Math.sqrt(stats.SsqErr / (stats.effectiveObservationsCount-stats.estimatedParametersCount));
        }
    }
}

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

package ec.tstoolkit.arima.special;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.ssf.ICompositeModel;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.implementation.SsfNoise;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
class MixedAirlineCompositeModel implements ICompositeModel {

    final SsfArima ssf;
    final SsfNoise noise;
    final int freq;
    final int[] np;
    final double h;

    public MixedAirlineCompositeModel(MixedAirlineModel model) {
        ssf = new SsfArima(model.getAirline());
        noise = new SsfNoise();//
        h=Math.sqrt(model.getNoisyPeriodsVariance());
        freq = model.getFrequency();
        np = model.getNoisyPeriods();
    }

    public MixedAirlineCompositeModel(SarimaModel airline, double nweight, int[] np){
        ssf=new SsfArima(airline);
        noise=new SsfNoise();
        h=nweight;
        this.np=np;
        freq=airline.getSpecification().getFrequency();
    }

    @Override
    public int getComponentsCount() {
        return 2;
    }

    @Override
    public ISsf getComponent(int iCmp) {
        return iCmp == 0 ? ssf : noise;
    }

    @Override
    public double getWeight(int iCmp, int pos) {
        if (iCmp == 0) {
            return 1;
        } else {
            int cpos = pos % freq;
            for (int i = 0; i < np.length; ++i) {
                if (np[i] == cpos) {
                     return h;
               }
            }
            return 0;
        }
    }

    public double getNoiseWeight(){
        return h;
    }

    public SarimaModel getAirline(){
        return (SarimaModel) ssf.getModel();
    }

    public void setAirlineParameters(double th, double bth){

    }

    @Override
    public boolean hasConstantWeights() {
        return false;
    }
    
    MixedAirlineModel toModel(){
        MixedAirlineModel model=new MixedAirlineModel();
        model.setFrequency(freq);
        model.setNoisyPeriods(np);
        model.setNoisyPeriodsVariance(h*h);
        SarimaModel airline=(SarimaModel)ssf.getModel();
        model.setTheta(airline.theta(1));
        model.setBTheta(airline.btheta(1));
        return model;
    }
}

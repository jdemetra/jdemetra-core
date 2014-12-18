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

package ec.tstoolkit.modelling.arima.diagnostics;

import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class FastOneStepAheadForecastingTest  extends AbstractOneStepAheadForecastingTest {

    public FastOneStepAheadForecastingTest(int nback) {
        super(nback);
    }

    @Override
    protected DataBlock computeResiduals(RegArimaModel<SarimaModel> regarima) {
        try {
            RegArimaEstimation<SarimaModel> est = inSampleEstimate(regarima);
            if (est == null) {
                return null;
            }
            DataBlock res=new DataBlock(regarima.getDModel().getObsCount());
            res.copy(new ReadDataBlock(est.fullResiduals()));
            
            DataBlock yc;
            // remove mean, if need be
            if (regarima.isMeanCorrection()) {
                yc=regarima.calcRes(new ReadDataBlock(est.likelihood.getB()));
            }
            else {
                yc=regarima.getY();
            }
            // compute recursively the residuals
            int nr=res.getLength();
            DataBlock ar=new DataBlock(est.model.getArima().getAR().getWeights()), ma=new DataBlock(est.model.getArima().getMA().getWeights()).drop(0, 1);
            int nc=nr-getOutOfSampleLength();
            int ny=yc.getLength()-getOutOfSampleLength();
            DataBlock yr=yc.range(ny-ar.getLength()+1, ny+1);
            DataBlock er=res.range(nc-ma.getLength(), nc);
            for (int i=nc; i<nr; ++i){
                double z=yr.dot(ar);
                z-=ma.dot(er);
                res.set(i, z);
                yr.move(1);
                er.move(1);
            }
            return res;
        }
        catch (Exception err) {
            return null;
        }


    }
}

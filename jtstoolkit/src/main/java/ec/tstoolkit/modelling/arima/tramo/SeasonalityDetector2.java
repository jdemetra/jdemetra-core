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


package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.modelling.arima.ISeasonalityDetector;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.modelling.arima.tramo.seriestest.OverSeasTest2;
import ec.tstoolkit.modelling.arima.tramo.seriestest.SerType;
import ec.tstoolkit.modelling.arima.tramo.spectrum.PeaksEnum;
import ec.tstoolkit.modelling.arima.tramo.spectrum.Spect;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
public class SeasonalityDetector2 implements ISeasonalityDetector {
    
    private static final double DEF_THRESHOLD=6;
    
    private double threshold_=DEF_THRESHOLD;

    @Override
    public boolean hasSeasonality(ModelDescription desc) {
        TsData y=desc.transformedOriginal();
        if ( ! test1(y))
            return false;
        int idiff=desc.getSpecification().getD()+desc.getSpecification().getBD();
        return test2(y, idiff);
    }

    private boolean test1(TsData y){
        TsData dy=y.delta(1);
        int ifreq=y.getFrequency().intValue();
        int n=dy.getLength();
        int nlag=3*ifreq;
        if (nlag >= n )
            nlag=2*ifreq;
        if (nlag >= n)
            return false;
        
        double[] x=dy.internalStorage();
        
        double c0=DescriptiveStatistics.cov(0, x);
        if (c0 == 0)
            return false;
        double qstat=0;
        for (int i=1; i<=nlag; ++i){
            double acf=DescriptiveStatistics.cov(i, x)/c0;
            qstat+=acf*acf/(n-i);
        }
        qstat*=n*(n+2);
        return qstat > threshold_;
    }

    private boolean test2(TsData y, int idiff) {
        TsData d=y.delta(1);

        PeaksEnum[] peaks=Spect.SpectrumComputation(d);
        OverSeasTest2 seas=new OverSeasTest2(y, peaks, idiff, SerType.Xlin );
        return seas.getCheckOverSeasTest() != 0;
    }

}

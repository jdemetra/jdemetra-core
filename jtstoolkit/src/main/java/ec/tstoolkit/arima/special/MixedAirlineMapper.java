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

import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.SsfComposite;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class MixedAirlineMapper implements IParametricMapping<ISsf> {
    
    public static final String TH="th", BTH="bth", NVAR="noise var";

    public MixedAirlineMapper(MixedAirlineModel m){
        freq_=m.getFrequency();
        noisyPeriods_=m.getNoisyPeriods();
    }

    private final int freq_;
    private final int[] noisyPeriods_;

    @Override
    public ISsf map(IReadDataBlock p) {
        SarimaModelBuilder builder=new SarimaModelBuilder();
        SarimaModel airline=builder.createAirlineModel(freq_, p.get(0), p.get(1));
        MixedAirlineCompositeModel model=new MixedAirlineCompositeModel(airline, p.get(2), noisyPeriods_);
        return new SsfComposite(model);
    }

    @Override
    public IReadDataBlock map(ISsf t) {
        if (!(t instanceof SsfComposite))
            return null;
        SsfComposite ct=(SsfComposite) t;
        if (! (ct.getCompositeModel() instanceof MixedAirlineCompositeModel))
            return null;
        MixedAirlineCompositeModel cm=(MixedAirlineCompositeModel) ct.getCompositeModel();
        double[] p=new double[3];
        p[0]=cm.getAirline().theta(1);
        p[1]=cm.getAirline().btheta(1);
        p[2]=cm.getNoiseWeight();
        return new ReadDataBlock(p);
    }

    @Override
    public String getDescription(int idx) {
        switch (idx){
            case 0:
                return TH;
            case 1: 
                return BTH;
            case 2: 
                return NVAR;
        }
        return PARAM+idx; 
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
//        if (inparams.get(2)<=0)
//            return false;
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        return 1e-4;
    }

    @Override
    public int getDim() {
        return 3;
    }

    @Override
    public double lbound(int idx) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double ubound(int idx) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
//        if (ioparams.get(2) <=0){
//            ioparams.set(2, .0001);
//            return ParamValidation.Changed;
//        }
        return ParamValidation.Valid;
    }

}

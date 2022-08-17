/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.examples;

import demetra.data.Data;
import ec.satoolkit.seats.SeatsResults;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaManager;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsAPI2 {
    
    static final TramoSeatsProcessor TRAMOSEATS=new TramoSeatsProcessor();
    
    static{
        // To allow automatic processing through SaItem objects
        SaManager.instance.add(TRAMOSEATS);
    }
    

    public Ts createTs() {

        TsData data = new TsData(TsFrequency.Monthly, 1967, 0, Data.PROD, true);
        return TsFactory.instance.createTs("test", null, data);
    }

    /**
     * Create a document containing
     *
     * @param ts
     * @return
     */
    public SaItem createItem(Ts ts) {
        return new SaItem(TramoSeatsSpecification.RSAfull.clone(), ts);
    }
    
    public void main(String[] args){
        // time series
        Ts s=createTs();
        //item, with a default specification
        SaItem item = createItem(s);
        
        // compute (results stored in the item
        CompositeResults rslt = item.process();
        
        // Generic data retrieval through the "getData" method
        // we can retrieve any result using keys used - for instance - in the cruncher (see the WIKI of the cruncher)
        SarimaModel arima = rslt.getData("arima", SarimaModel.class);
        TsData sa = rslt.getData("sa", TsData.class);
        
        System.out.println(arima);
        System.out.println(sa);
        
        // Advanced data retrieval
        SeatsResults seats=rslt.get("decomposition", SeatsResults.class);
        
        UcarimaModel ucm = seats.getUcarimaModel();
        System.out.println(ucm);
        
        // refreshing the specification corresponding to the estimated model ('point specification')
        // null could be replaced by a frozen domain (outliers not modified for that span)
        // last param to remove pre-specified time span for the series (seldom used)
        TramoSeatsSpecification newSpecification = (TramoSeatsSpecification) TRAMOSEATS.createSpecification(item, null, EstimationPolicyType.FreeParameters, true);
        
        // That new specification can be used for further processing
        // for instance
        // In practice, s should have been updated with new obs. The estimation policy type is just for information (no actual impact on the computation)
        SaItem nitem=item.newSpecification(s, newSpecification, EstimationPolicyType.FreeParameters);
        CompositeResults nrslt = nitem.process();

        TsData nsa = nrslt.getData("sa", TsData.class);
        
        System.out.println(nsa);
    }

}

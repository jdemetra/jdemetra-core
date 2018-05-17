/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tstoolkit.jdr.ws;

import demetra.datatypes.Ts;
import demetra.datatypes.TsInformationType;
import demetra.datatypes.TsMoniker;
import demetra.datatypes.sa.SaItemType;
import demetra.datatypes.sa.SaProcessingType;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class MultiProcessing {
    
    public static MultiProcessing of(String name, SaProcessingType processing){
        MultiProcessing p=new MultiProcessing(name);
        p.metaData.putAll(processing.getMetaData());
        processing.getItems().forEach(v->p.items.add(new SaItem(v)));
        return p;
    }
    
    public void compute(ProcessingContext context){
        items.forEach(v->v.compute(context));
    }

    public void add(String name, TsData s, TramoSeatsSpecification spec){
        Ts ts = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .data(s).
                name(name).
                build();
        SaItemType item = SaItemType.builder()
                .name(name)
                .domainSpec(spec)
                .ts(ts)
                .build();
        items.add(new SaItem(item));
    }
    
    public void add(String name, TsData s, X13Specification spec){
        Ts ts = Ts.builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .data(s)
                .name(name)
                .build();
        SaItemType item = SaItemType.builder()
                .name(name)
                .domainSpec(spec)
                .ts(ts)
                .build();
        items.add(new SaItem(item));
    }
    
    public SaProcessingType toType(){
        SaProcessingType sa=new SaProcessingType();
        sa.getMetaData().putAll(metaData);
        for (SaItem cur : items){
            sa.getItems().add(cur.getSaDefinition());
        }
        return sa;
    }
    
    public SaItem get(int pos){
        return items.get(pos);
    }

    public int size(){
        return items.size();
    }
    

    private final String name;
    private final Map<String, String> metaData = new HashMap<>();
    private final List<SaItem> items = new ArrayList<>();
    
}

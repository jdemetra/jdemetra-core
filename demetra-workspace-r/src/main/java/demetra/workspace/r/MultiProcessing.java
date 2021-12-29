/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.workspace.r;

import demetra.sa.SaDefinition;
import demetra.sa.SaItem;
import demetra.sa.SaItems;
import demetra.sa.SaSpecification;
import demetra.timeseries.Ts;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author PALATEJ
 */
@lombok.Data
public class MultiProcessing {
    
    public static MultiProcessing of(String name, SaItems processing){
        MultiProcessing p=new MultiProcessing(name);
        p.metaData.putAll(processing.getMeta());
        p.items.addAll(processing.getItems());
        return p;
    }
    
    public MultiProcessing(String name){
        this.name=name;
    }
    
    public void compute(ModellingContext context){
        items.parallelStream().forEach(v->v.process(context, false));
    }

    public void add(String name, TsData s, SaSpecification spec){
        Ts ts = Ts.of(name, s);
        SaDefinition definition=SaDefinition.builder()
                .ts(ts)
                .domainSpec(spec)
                .build();
        SaItem item = SaItem.builder()
                .name(name)
                .definition(definition)
                .build();
        items.add(item);
    }
   
    public SaItem get(int pos){
        return items.get(pos);
    }
    
    public void set(int pos, SaItem newItem){
        items.set(pos, newItem);
    }

    public int size(){
        return items.size();
    }
    
    public void remove(int pos){
        items.remove(pos);
    }
    
    public void removeAll(){
        items.clear();
    }
    

    private final String name;
    private final Map<String, String> metaData = new HashMap<>();
    private final List<SaItem> items = new ArrayList<>();
    
}

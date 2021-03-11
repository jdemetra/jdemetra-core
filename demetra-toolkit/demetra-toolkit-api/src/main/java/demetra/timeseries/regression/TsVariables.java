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
package demetra.timeseries.regression;

import demetra.timeseries.TsDataSupplier;
import demetra.timeseries.TimeSeriesDomain;
import nbbrd.design.Development;
import demetra.timeseries.TsData;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class TsVariables implements ITsVariable {

    protected static TsData[] data(String[] id,  ModellingContext context) {
        TsData[] data = new TsData[id.length];
        for (int i = 0; i < id.length; ++i) {
            TsDataSupplier supplier = context.getTsVariable(id[i]);
            if (supplier == null) {
                return null;
            }
            TsData cur = supplier.get();
            if (cur == null) {
                return null;
            }
            data[i] = cur;
        }
        return data;
    }

    private final String gdesc;
    private final String[] id;
    private final TsData[] data;
    private final String[] desc;

    protected TsVariables(String gdesc, String[] id, TsData[] data, String[] desc) {
        this.gdesc=gdesc;
        this.id = id.clone();
        this.data = data;
        this.desc=desc;
    }

    @Override
    public int dim() {
        return id.length;
    }
    
    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return gdesc;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return desc == null ? gdesc+(idx+1) : desc[idx];
    }
    
    public String getId(int i){
        return id[i];
    }
    
    public TsData getData(int i){
        return data[i];
    }

    protected boolean equals(TsVariables var){
        return Arrays.deepEquals(id, var.id);
    }
    
    protected int hash(){
        return Arrays.hashCode(id);
    }
    
    
}

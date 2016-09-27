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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsVariableReference implements
        IUserTsVariable {

    private final ProcessingContext context;
    private final String id;
    private ITsVariable var;

    public ProcessingContext getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public TsVariableReference(String id) {
        this.id = id;
        this.context = null;
    }

    /**
     *
     * @param id
     */
    public TsVariableReference(String id, ProcessingContext context) {
        this.id = id;
        this.context = context;
    }

    private ProcessingContext context() {
        if (context == null) {
            return ProcessingContext.getActiveContext();
        } else {
            return context;
        }
    }

    private ITsVariable var() {
        if (var == null) {
            var = context().getTsVariable(id);
        }
        return var;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        var().data(domain, data);
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return var().getDefinitionDomain();
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return var().getDefinitionFrequency();
    }

    @Override
    public String getDescription(TsFrequency context) {
        return var().getDescription(context);
    }

    @Override
    public int getDim() {
        return var().getDim();
    }

    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        return var().getItemDescription(idx, context);
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return var().isSignificant(domain);
    }

}

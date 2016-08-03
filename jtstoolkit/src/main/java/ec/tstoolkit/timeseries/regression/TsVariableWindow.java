/*
 * Copyright 2016 National Bank of Belgium
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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class TsVariableWindow extends AbstractTsModifier {

    private final Day start, end;

    /**
     *
     * @param var
     * @param start
     * @param end
     */
    public TsVariableWindow(final ITsVariable var, final Day start, final Day end) {
        super(var);
        this.start = start;
        this.end = end;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        TsPeriodSelector sel=new TsPeriodSelector();
        sel.between(start, end);
        TsDomain sdom = domain.select(sel);
        int nbeg=sdom.getStart().minus(domain.getStart());
        int n=sdom.getLength();
        if (n == 0)
            return;
        ArrayList<DataBlock> tmp=new ArrayList<>();
        for (DataBlock bl : data){
            tmp.add(bl.extract(nbeg, n));
        }
        var.data(sdom, tmp);
    }

    private String description(String desc) {
        StringBuilder builder = new StringBuilder();
        builder.append(desc);
        builder.append('(').append(start.toString()).append("//");
        builder.append(end.toString()).append(')');

        return builder.toString();
    }

    /**
     *
     * @return
     */
    public Day getStart() {
        return start;
    }

    /**
     *
     * @return
     */
    public Day getEnd() {
        return end;
    }

    @Override
    public TsDomain getDefinitionDomain() {
        return var.getDefinitionDomain();
    }

    @Override
    public TsFrequency getDefinitionFrequency() {
        return var.getDefinitionFrequency();
    }

    @Override
    public String getDescription(TsFrequency context) {
        return description(var.getDescription(context)+getDescription(context));
    }

    @Override
    public int getDim() {
        return var.getDim();
    }

    @Override
    public String getItemDescription(int idx, TsFrequency context) {
        return description(var.getItemDescription(idx, context));
    }

 
    @Override
    public ITsVariable getVariable() {
        return var;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        TsPeriodSelector sel=new TsPeriodSelector();
        sel.between(start, end);
        TsDomain sdom = domain.select(sel);
        return ! sdom.isEmpty();
    }
}

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
package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.timeseries.IRegularPeriod;
import demetra.timeseries.ITimeDomain;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 * @param <E>
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractOutlier<E extends IRegularPeriod>
        implements IOutlierVariable<E> {

    public static <E extends IRegularPeriod> String defaultName(String code, LocalDateTime pos, ITimeDomain<E> context) {
        StringBuilder builder = new StringBuilder();
        builder.append(code).append(" (");
        if (context == null) {
            builder.append(pos);
        } else {
            E p = context.get(0);
            p.moveTo(pos);
            builder.append(p);
        }
        builder.append(')');
        return builder.toString();
    }

    protected final LocalDateTime position;
    protected final String name;

    protected AbstractOutlier(LocalDateTime pos, String name) {
        position = pos;
        this.name = name;
    }
    
    @Override
    public void data(E start, DataBlock buffer){
        IRegularPeriod p=start.moveTo(getPosition());
        long outlierPos=start.until(p);
        data((int)outlierPos, buffer);
    }
    
    protected abstract void data(int pos,DataBlock buffer);
     
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription(ITimeDomain<E> context) {
        StringBuilder builder = new StringBuilder();
        builder.append(getCode()).append(" (");
        if (context == null) {
            builder.append(position);
        } else {
            E p = context.get(0);
            p.moveTo(position);
            builder.append(p);
        }
        builder.append(')');
        return builder.toString();
    }

    // / <summary>Position of the outlier</summary>
    @Override
    public LocalDateTime getPosition() {
        return position;
    }

    @Override
    public int getDim() {
        return 1;
    }

    @Override
    public ITimeDomain<E> getDefinitionDomain() {
        return null;
    }

    @Override
    public Period getDefinitionPeriod() {
        return null;
    }

    @Override
    public String getItemDescription(int idx, ITimeDomain context) {
        return getDescription(context);
    }

}

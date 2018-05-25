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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.modelling.ComponentType;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
public class SwitchOutlier extends AbstractOutlier {

    public static final String WO = "WO";

    public static final Factory FACTORY = new Factory();

    public static class Factory implements IOutlierFactory {

        @Override
        public IOutlier make(LocalDateTime position) {
            return new SwitchOutlier(position);
        }

        @Override
        public void fill(int outlierPosition, DataBlock buffer) {
            buffer.set(outlierPosition, 1);
            buffer.set(outlierPosition, -1);
        }

        @Override
        public FilterRepresentation getFilterRepresentation() {
            return new FilterRepresentation(new RationalBackFilter(
                    BackFilter.D1, BackFilter.ONE, 0), 0);
        }

        @Override
        public int excludingZoneAtStart() {
            return 0;
        }

        @Override
        public int excludingZoneAtEnd() {
            return 1;
        }

        @Override
        public String getCode() {
            return WO;
        }

    }

    public SwitchOutlier(LocalDateTime pos) {
        super(pos, IOutlier.defaultName(WO, pos, null));
    }

    public SwitchOutlier(LocalDateTime pos, String name) {
        super(pos, name);
    }

    @Override
    protected void data(int pos, DataBlock buffer) {
        if (pos >= 0 && pos < buffer.length()) {
            buffer.set(pos, 1);
        }
        int npos = pos + 1;
        if (npos >= 0 && npos < buffer.length()) {
            buffer.set(npos, -1);
        }
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.Irregular;
    }

    @Override
    public String getCode() {
        return WO;
    }

    @Override
    public SwitchOutlier rename(String name) {
        return new SwitchOutlier(position, name);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof SwitchOutlier) {
            SwitchOutlier x = (SwitchOutlier) other;
            return this.position.equals(x.position);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + position.hashCode();
        return hash;
    }

}

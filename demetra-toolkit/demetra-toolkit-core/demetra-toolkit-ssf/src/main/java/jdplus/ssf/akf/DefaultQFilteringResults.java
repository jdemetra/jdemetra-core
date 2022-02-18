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
package jdplus.ssf.akf;

import jdplus.ssf.univariate.ISsf;

/**
 *
 * @author Jean Palate
 */
public class DefaultQFilteringResults extends DefaultAugmentedFilteringResults implements IQFilteringResults {

    private int collapsed;
    private final QAugmentation Q = new QAugmentation();

    private DefaultQFilteringResults(boolean var) {
        super(var);
    }

    public static DefaultQFilteringResults full() {
        return new DefaultQFilteringResults(true);
    }

    public static DefaultQFilteringResults light() {
        return new DefaultQFilteringResults(false);
    }

    @Override
    public void prepare(ISsf ssf, final int start, final int end) {
        super.prepare(ssf, start, end);
        int n = ssf.getDiffuseDim();
        Q.prepare(n, 1);
    }

    @Override
    public void save(int t, AugmentedUpdateInformation pe) {
        super.save(t, pe);
        Q.update(pe);
    }

    @Override
    public void close(int pos) {
    }

     @Override
    public void clear() {
        super.clear();
        collapsed = 0;
    }

    @Override
    public int getCollapsingPosition() {
        return collapsed;
    }

    @Override
    public QAugmentation getAugmentation() {
        return Q;
    }

    @Override
    public boolean canCollapse() {
        return Q.canCollapse();
    }

    @Override
    public boolean collapse(int pos, AugmentedState state) {
        if (Q.collapse(state)) {
            collapsed = pos;
            return true;
        } else {
            return false;
        }
    }

}

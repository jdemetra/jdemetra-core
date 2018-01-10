/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package demetra.ssf.univariate;

/**
 *
 * @author Jean Palate
 */
public class SsfDataWindow implements ISsfData {

    private final int start, end, n;
    private final ISsfData data;

    public SsfDataWindow(final ISsfData data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
        n = data.length();
    }

    @Override
    public double get(int pos) {
        int npos = pos + start;
        return npos < n ? data.get(npos) : Double.NaN;
    }

    @Override
    public boolean isMissing(int pos) {
        int npos = pos + start;
        return npos < n ? data.isMissing(npos) : true;
    }

    @Override
    public boolean hasData() {
        return data.hasData();
    }

    @Override
    public int length() {
        return end - start;
    }

}

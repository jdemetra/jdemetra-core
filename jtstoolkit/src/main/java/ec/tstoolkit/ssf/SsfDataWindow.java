/*
 * Copyright 2014 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf;

/**
 *
 * @author Jean Palate
 */
public class SsfDataWindow implements ISsfData {

    private final ISsfData data_;
    private final double[] s_start_;
    private final int start_, n_;

    public SsfDataWindow(final ISsfData data, final double[] state, final int start, final int length) {
        data_ = data;
        s_start_ = state;
        start_ = start;
        n_ = length >= 0 ? length : data.getCount() - start;
    }

    @Override
    public double get(int n) {
        return data_.get(n + start_);
    }

    @Override
    public int getCount() {
        return n_;
    }

    @Override
    public double[] getInitialState() {
        return s_start_;
    }

    @Override
    public int getObsCount() {
        int nobs = 0;
        for (int i = 0; i < n_; ++i) {
            if (!data_.isMissing(start_ + i)) {
                ++nobs;
            }
        }
        return nobs;
    }

    @Override
    public boolean hasData() {
        return data_.hasData();
    }

    @Override
    public boolean hasMissingValues() {
        return data_.hasMissingValues();
    }

    @Override
    public boolean isMissing(int pos) {
        return pos < 0 || pos >= n_ || data_.isMissing(pos + start_);
    }
}

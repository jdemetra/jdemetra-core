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

package ec.tstoolkit.utilities;

/**
 *
 * @author Jean Palate
 */
//@Immutable
public class LinearId implements Id {

    private static final IdAggregator defAggregator_ = new DefaultIdAggregator();
    private final String[] data_;
    private final IdAggregator aggregator_;

    public LinearId() {
        data_ = null;
        aggregator_ = defAggregator_;
    }

    public LinearId(String id) {
        data_ = new String[]{id};
        aggregator_ = defAggregator_;
    }

    public LinearId(String parent, String tail) {
        data_ = new String[]{parent, tail};
        aggregator_ = defAggregator_;
    }

    public LinearId(String... id) {
        data_ = id.clone();
        aggregator_ = defAggregator_;
    }

    public LinearId(String[] ids, IdAggregator aggregator) {
        if (ids != null) {
            data_ = ids.clone();
        }
        else {
            data_ = null;
        }
        aggregator_ = aggregator;
    }

    @Override
    public String get(int index) {
        return data_[index];
    }

    @Override
    public LinearId extend(String tail) {
        int n = getCount();
        String[] ids = new String[n + 1];
        for (int i = 0; i < n; ++i) {
            ids[i] = data_[i];
        }
        ids[n] = tail;
        return new LinearId(ids, aggregator_);
    }

    @Override
    public LinearId parent() {
        int n = getCount();
        if (n <= 1) {
            return new LinearId(null, aggregator_);
        }
        String[] ids = new String[n - 1];
        for (int i = 0; i < n - 1; ++i) {
            ids[i] = data_[i];
        }
        return new LinearId(ids, aggregator_);
    }

    @Override
    public String tail() {
        return (data_ == null || data_.length == 0) ? null : data_[data_.length - 1];
    }

    @Override
    public Id[] path() {
        int n = getCount();
        if (n == 0) {
            return new Id[0];
        }
        Id[] path = new Id[n];
        Id cur = this;
        while (n > 0) {
            path[--n] = cur;
            cur = cur.parent();
        }
        return path;
    }

    @Override
    public int getCount() {
        return data_ == null ? 0 : data_.length;
    }

    @Override
    public int compareTo(Id o) {
        int ln = getCount(), rn = o.getCount();
        if (ln < rn) {
            return -1;
        }
        if (ln > rn) {
            return 1;
        }
        for (int i = 0; i < ln; ++i) {
            int cmp = data_[i].compareTo(o.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof LinearId && equals((LinearId) obj));
    }

    private boolean equals(LinearId other) {
        return 0 == compareTo(other);
    }

    @Override
    public int hashCode() {
        if (data_ == null) {
            return 0;
        }
        return data_[data_.length - 1].hashCode();
    }

    @Override
    public String toString() {
        return aggregator_.aggregate(data_);
    }

    @Override
    public boolean startsWith(Id id) {
        int n = getCount(), sn = id.getCount();
        if (sn > n) {
            return false;
        }
        for (int i = 0; i < sn; ++i) {
            if (!data_[i].equals(id.get(i))) {
                return false;
            }
        }
        return true;
    }
}

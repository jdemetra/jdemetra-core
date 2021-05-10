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
package demetra.workspace.util;

import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
//@Immutable
public class LinearId implements Id {

    @NonNull
    public static LinearId of(@NonNull Id id) {
        if (id instanceof LinearId) {
            return (LinearId) id;
        }
        if (id.getCount() == 0) {
            return new LinearId(null, DEFAULT_AGGREGATOR, null);
        }
        return new LinearId(id.toArray(), DEFAULT_AGGREGATOR, null);
    }

    private static final IdAggregator DEFAULT_AGGREGATOR = new DefaultIdAggregator();

    private final String[] data_;
    private final IdAggregator aggregator_;

    public LinearId() {
        this(null, DEFAULT_AGGREGATOR, null);
    }

    public LinearId(String id) {
        this(new String[]{id}, DEFAULT_AGGREGATOR, null);
    }

    public LinearId(String parent, String tail) {
        this(new String[]{parent, tail}, DEFAULT_AGGREGATOR, null);
    }

    public LinearId(String... id) {
        this(id.clone(), DEFAULT_AGGREGATOR, null);
    }

    public LinearId(String[] ids, IdAggregator aggregator) {
        this(ids != null ? ids.clone() : null, aggregator, null);
    }

    private LinearId(String[] ids, IdAggregator aggregator, Void private_constructor) {
        this.data_ = ids;
        this.aggregator_ = aggregator;
    }

    @Override
    public String get(int index) {
        return data_[index];
    }

    @Override
    public LinearId extend(String tail) {
        int n = getCount();
        if (n <= 0) {
            return new LinearId(new String[]{tail}, aggregator_, null);
        }
        String[] ids = Arrays.copyOf(data_, n + 1);
        ids[n] = tail;
        return new LinearId(ids, aggregator_, null);
    }

    @Override
    public LinearId parent() {
        int n = getCount();
        if (n <= 1) {
            return new LinearId(null, aggregator_, null);
        }
        String[] ids = Arrays.copyOf(data_, n - 1);
        return new LinearId(ids, aggregator_, null);
    }

    @Override
    public int getCount() {
        return data_ == null ? 0 : data_.length;
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
    public String[] toArray() {
        return data_ != null ? data_.clone() : new String[0];
    }
}

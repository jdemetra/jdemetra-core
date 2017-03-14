/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package ec.demetra.workspace;

import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.utilities.Id;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
public final class WorkspaceFamily implements Id {

    @Nonnull
    public static WorkspaceFamily of(@Nonnull Id input) {
        if (input instanceof WorkspaceFamily) {
            return (WorkspaceFamily) input;
        }
        if (input.getCount() == 0) {
            return EMPTY;
        }
        String[] data = new String[input.getCount()];
        for (int i = 0; i < data.length; i++) {
            data[i] = input.get(i);
        }
        return new WorkspaceFamily(data);
    }

    @Nonnull
    public static WorkspaceFamily parse(@Nonnull String input) {
        return !input.isEmpty() ? new WorkspaceFamily(input.split("@", -1)) : EMPTY;
    }

    private static final WorkspaceFamily EMPTY = new WorkspaceFamily(new String[0]);

    private final String[] data;

    private WorkspaceFamily(String[] data) {
        this.data = data;
    }

    @Override
    public String get(int index) {
        return data[index];
    }

    @Override
    public WorkspaceFamily extend(String tail) {
        String[] result = Arrays.copyOf(data, data.length + 1);
        result[data.length] = tail;
        return new WorkspaceFamily(result);
    }

    @Override
    public WorkspaceFamily parent() {
        switch (getCount()) {
            case 0:
                return null;
            case 1:
                return EMPTY;
            default:
                return new WorkspaceFamily(Arrays.copyOf(data, data.length - 1));
        }
    }

    @Override
    public String tail() {
        int n = getCount();
        return n > 0 ? get(n - 1) : null;
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
        return data.length;
    }

    @Override
    public boolean startsWith(Id id) {
        int sn = id.getCount();
        if (sn > getCount()) {
            return false;
        }
        for (int i = 0; i < sn; ++i) {
            if (!get(i).equals(id.get(i))) {
                return false;
            }
        }
        return true;
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
            int cmp = get(i).compareTo(o.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof WorkspaceFamily && equals((WorkspaceFamily) obj));
    }

    private boolean equals(WorkspaceFamily that) {
        return Arrays.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return Arrays.stream(data).collect(Collectors.joining("@"));
    }

    public static final WorkspaceFamily SA_MULTI = parse("Seasonal adjustment@multi-documents");
    public static final WorkspaceFamily SA_DOC_X13 = parse("Seasonal adjustment@documents@x13");
    public static final WorkspaceFamily SA_DOC_TRAMOSEATS = parse("Seasonal adjustment@documents@tramoseats");
    public static final WorkspaceFamily SA_SPEC_X13 = parse("Seasonal adjustment@specifications@x13");
    public static final WorkspaceFamily SA_SPEC_TRAMOSEATS = parse("Seasonal adjustment@specifications@tramoseats");

    public static final WorkspaceFamily MOD_DOC_REGARIMA = parse("Modelling@documents@regarima");
    public static final WorkspaceFamily MOD_DOC_TRAMO = parse("Modelling@documents@tramo");
    public static final WorkspaceFamily MOD_SPEC_REGARIMA = parse("Modelling@specifications@regarima");
    public static final WorkspaceFamily MOD_SPEC_TRAMO = parse("Modelling@specifications@tramo");

    public static final WorkspaceFamily UTIL_CAL = parse("Utilities@Calendars");
    public static final WorkspaceFamily UTIL_VAR = parse("Utilities@Variables");
}

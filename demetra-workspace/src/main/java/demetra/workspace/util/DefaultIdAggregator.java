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

/**
 *
 * @author Jean Palate
 */
public class DefaultIdAggregator implements IdAggregator {

    private static final String EMPTY = "";
    private final char sep_;

    public DefaultIdAggregator() {
        sep_ = '.';
    }

    public DefaultIdAggregator(char sep) {
        sep_ = sep;
    }

    public String aggregate(Id ids) {
        int n = ids.getCount();
        if (n == 0) {
            return EMPTY;
        }
        if (n == 1) {
            return ids.get(0);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ids.get(0));
        for (int i = 1; i < n; ++i) {
            builder.append(sep_).append(ids.get(i));
        }
        return builder.toString();
    }

    @Override
    public String aggregate(String[] ids) {
        if (ids == null || ids.length == 0) {
            return EMPTY;
        }
        if (ids.length == 1) {
            return ids[0];
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ids[0]);
        for (int i = 1; i < ids.length; ++i) {
            builder.append(sep_).append(ids[i]);
        }
        return builder.toString();
    }
}

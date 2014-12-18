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

package ec.tss.tsproviders.sdmx;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.legacy.IStringHandler;
import ec.tss.tsproviders.legacy.LinearIdBuilder;
import ec.tss.tsproviders.legacy.StringHandlers;
import ec.tss.tsproviders.utils.Parsers;
import ec.tss.tsproviders.utils.Parsers.Parser;
import java.io.File;

/**
 *
 * @author Kristof Bayens
 */
@Deprecated
public final class SdmxDataSourceId implements CharSequence {

    private static final IStringHandler SH = StringHandlers.BASE64;

    public static SdmxDataSourceId parse(CharSequence input) {
        return input instanceof SdmxDataSourceId ? (SdmxDataSourceId) input : parse(input.toString());
    }

    public static SdmxDataSourceId parse(String input) {
        LinearIdBuilder idBuilder = LinearIdBuilder.parse(SH, input);
        if (idBuilder == null || idBuilder.getCount() != 2) {
            return null;
        }
        return new SdmxDataSourceId(idBuilder);
    }

    public static SdmxDataSourceId from(File file) {
        return parse(file.getAbsolutePath());
    }
    private final LinearIdBuilder id_;

    private SdmxDataSourceId(LinearIdBuilder id) {
        id_ = id;
    }

    public SdmxDataSourceId(String factory, String url) {
        id_ = LinearIdBuilder.from(SH, factory, url);
    }

    @Override
    public String toString() {
        return id_.toString();
    }

    @Override
    public int hashCode() {
        return id_.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SdmxDataSourceId && equals((SdmxDataSourceId) obj));
    }

    private boolean equals(SdmxDataSourceId other) {
        return this.id_.equals(other.id_);
    }

    public String getFactory() {
        return id_.get(0);
    }

    public String getUrl() {
        return id_.get(1);
    }

    @Override
    public int length() {
        return id_.toString().length();
    }

    @Override
    public char charAt(int index) {
        return id_.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return id_.toString().subSequence(start, end);
    }
    static final String X_FACTORY = "factory";
    static final String X_URL = "url";

    public static Parsers.Parser<DataSource> legacyParser(final String providerName, final String version) {
        return new Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                SdmxDataSourceId id = SdmxDataSourceId.parse(input);
                return id != null
                        ? DataSource.builder(providerName, version).put(X_URL, id.getUrl()).put(X_FACTORY, id.getFactory()).build() : null;
            }
        };
    }
}

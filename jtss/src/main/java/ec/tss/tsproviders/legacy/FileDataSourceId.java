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
package ec.tss.tsproviders.legacy;

import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileBean;
import ec.tss.tsproviders.utils.Parsers.Parser;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.utilities.Files2;
import java.io.File;

/**
 *
 * @author Demortier Jeremy
 */
@Immutable
@Deprecated
public final class FileDataSourceId implements CharSequence {

    private static final IStringHandler SH = StringHandlers.PLAIN;

    // Factory Methods ->
    public static FileDataSourceId parse(CharSequence input) {
        return input instanceof FileDataSourceId ? (FileDataSourceId) input : parse(input.toString());
    }

    public static FileDataSourceId parse(String input) {
        if (!isDemetraUri(input)) {
            LinearIdBuilder id = LinearIdBuilder.parse(SH, input);
            if (id != null && id.getCount() == 1 && Files2.isValidPath(new File(id.get(0)))) {
                return new FileDataSourceId(id);
            }
        }
        return null;
    }

    private static boolean isDemetraUri(String input) {
        return input != null && input.startsWith("demetra://");
    }

    public static FileDataSourceId from(File file) {
        return parse(file.getAbsolutePath());
    }
    // <-
    private final LinearIdBuilder id;

    private FileDataSourceId(LinearIdBuilder id) {
        this.id = id;
    }

    public <T extends IFileBean> T fill(T bean) {
        bean.setFile(new File(getFile()));
        return bean;
    }

    public String getFile() {
        return id.get(0);
    }

    public String getFileName() {
        File f = new File(getFile());
        return f.getName();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj ? true : obj instanceof FileDataSourceId ? this.getFile().equals(((FileDataSourceId) obj).getFile()) : false;
    }

    @Override
    public int length() {
        return id.toString().length();
    }

    @Override
    public char charAt(int index) {
        return id.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return id.toString().subSequence(start, end);
    }
    private static final String X_FILE = "file";

    public static Parser<DataSource> legacyParser(final String providerName, final String version) {
        return new Parser<DataSource>() {
            @Override
            public DataSource parse(CharSequence input) throws NullPointerException {
                FileDataSourceId id = FileDataSourceId.parse(input);
                return id != null
                        ? DataSource.builder(providerName, version).put(X_FILE, id.getFile()).build() : null;
            }
        };
    }
}

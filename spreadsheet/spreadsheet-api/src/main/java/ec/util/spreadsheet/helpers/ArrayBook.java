/*
 * Copyright 2013 National Bank of Belgium
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
package ec.util.spreadsheet.helpers;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
public final class ArrayBook extends Book implements Serializable {

    private final ArraySheet[] sheets;

    // @VisibleForTesting
    ArrayBook(@Nonnull ArraySheet[] sheets) {
        this.sheets = sheets;
    }

    @Override
    public int getSheetCount() {
        return sheets.length;
    }

    @Override
    public ArraySheet getSheet(int sheetIndex) {
        return sheets[sheetIndex];
    }

    @Override
    public void close() {
        // no resource to close
    }

    @Nonnull
    public ArrayBook copy() {
        ArraySheet[] result = new ArraySheet[getSheetCount()];
        for (int s = 0; s < result.length; s++) {
            result[s] = getSheet(s).copy();
        }
        return new ArrayBook(result);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ArrayBook && equals((ArrayBook) obj));
    }

    private boolean equals(ArrayBook that) {
        return Arrays.equals(this.sheets, that.sheets);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sheets);
    }

    @Override
    public String toString() {
        return "ArrayBook[" + sheets.length + "]";
    }

    @Nonnull
    public static ArrayBook copyOf(@Nonnull Book book) throws IOException {
        if (book instanceof ArrayBook) {
            return ((ArrayBook) book).copy();
        }
        ArraySheet[] sheets = new ArraySheet[book.getSheetCount()];
        for (int s = 0; s < sheets.length; s++) {
            sheets[s] = ArraySheet.copyOf(book.getSheet(s));
        }
        return new ArrayBook(sheets);
    }

    @Nonnull
    public static Builder builder() {
        return new ListBuilder();
    }

    public abstract static class Builder {

        @Nonnull
        abstract public Builder clear();

        @Nonnull
        abstract public Builder book(@Nonnull Book book) throws IOException;

        @Nonnull
        abstract public Builder sheet(@Nonnull Sheet sheet);

        @Nonnull
        abstract public ArrayBook build();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    private static final class ListBuilder extends Builder {

        private final List<ArraySheet> sheets = new ArrayList<>();

        @Override
        public Builder clear() {
            sheets.clear();
            return this;
        }

        @Override
        public Builder book(Book book) throws IOException {
            for (int i = 0; i < book.getSheetCount(); i++) {
                sheet(book.getSheet(i));
            }
            return this;
        }

        @Override
        public Builder sheet(Sheet sheet) {
            // FIXME: 2 sheets with the same name?
            sheets.add(ArraySheet.copyOf(sheet));
            return this;
        }

        @Override
        public ArrayBook build() {
            return new ArrayBook(sheets.toArray(new ArraySheet[sheets.size()]));
        }
    }
    //</editor-fold>
}

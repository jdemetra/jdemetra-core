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
package ec.util.spreadsheet.poi;

import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.Sheet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Philippe Charles
 */
final class PoiBook extends Book {

    private final Workbook workbook;

    @Nonnull
    public static PoiBook create(@Nonnull File file) throws IOException, InvalidFormatException {
        return new PoiBook(new XSSFWorkbook(OPCPackage.open(file.getPath(), PackageAccess.READ)));
    }

    @Nonnull
    public static PoiBook create(@Nonnull InputStream stream) throws IOException, InvalidFormatException {
        return new PoiBook(new XSSFWorkbook(OPCPackage.open(stream)));
    }

    @Nonnull
    public static PoiBook createClassic(@Nonnull File file) throws IOException {
        return new PoiBook(new HSSFWorkbook(new POIFSFileSystem(file)));
    }

    @Nonnull
    public static PoiBook createClassic(@Nonnull InputStream stream) throws IOException {
        return new PoiBook(new HSSFWorkbook(new POIFSFileSystem(stream)));
    }

    private PoiBook(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    @Override
    public Sheet getSheet(int index) {
        try {
            return new PoiSheet(workbook.getSheetAt(index));
        } catch (IllegalArgumentException ex) {
            throw index < 0 || index >= getSheetCount() ? new IndexOutOfBoundsException(ex.getMessage()) : ex;
        }
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Philippe Charles
 */
class PoiBook extends Book {

    private final Workbook workbook;

    public static PoiBook create(File file) throws IOException, InvalidFormatException {
        final OPCPackage pkg = OPCPackage.open(file.getPath(), PackageAccess.READ);
        return new PoiBook(new XSSFWorkbook(pkg)) {
            @Override
            public void close() throws IOException {
                pkg.close();
            }
        };
    }

    public static PoiBook create(InputStream stream) throws IOException, InvalidFormatException {
        final OPCPackage pkg = OPCPackage.open(stream);
        return new PoiBook(new XSSFWorkbook(pkg)) {
            @Override
            public void close() throws IOException {
                pkg.close();
            }
        };
    }

    public static PoiBook createClassic(File file) throws IOException {
        final FileInputStream stream = new FileInputStream(file);
        return new PoiBook(new HSSFWorkbook(stream)) {
            @Override
            public void close() throws IOException {
                stream.close();
            }
        };
    }

    public static PoiBook createClassic(InputStream stream) throws IOException {
        return new PoiBook(new HSSFWorkbook(stream));
    }

    PoiBook(Workbook workbook) throws IOException {
        this.workbook = workbook;
    }

    @Override
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    @Override
    public Sheet getSheet(int index) {
        return new PoiSheet(workbook.getSheetAt(index));
    }
}

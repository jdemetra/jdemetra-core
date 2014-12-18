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

import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Philippe Charles
 */
final class PoiBookWriter {

    public static void copy(ec.util.spreadsheet.Book source, Workbook target) throws IOException {
        new PoiBookWriter(target).copy(source);
    }

    private final Workbook workbook;
    private final CellStyle dateStyle;

    private PoiBookWriter(Workbook workbook) {
        this.workbook = workbook;
        this.dateStyle = createDateStyle(workbook);
    }

    private static CellStyle createDateStyle(Workbook workbook) {
//        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle result = workbook.createCellStyle();
//        result.setDataFormat(creationHelper.createDataFormat().getFormat("m/d/yy"));
        result.setDataFormat((short) 14);
        return result;
    }

    private void copy(ec.util.spreadsheet.Book source) throws IOException {
        for (int s = 0; s < source.getSheetCount(); s++) {
            ec.util.spreadsheet.Sheet sheet = source.getSheet(s);
            copy(sheet, workbook.createSheet(sheet.getName()));
        }
    }

    private void copy(ec.util.spreadsheet.Sheet source, Sheet target) throws IOException {
        for (int i = 0; i < source.getRowCount(); i++) {
            Row row = target.createRow(i);
            for (int j = 0; j < source.getColumnCount(); j++) {
                ec.util.spreadsheet.Cell cell = source.getCell(i, j);
                if (cell != null) {
                    copy(cell, row.createCell(j));
                }
            }
        }
    }

    private void copy(ec.util.spreadsheet.Cell source, Cell target) throws IOException {
        if (source.isDate()) {
            target.setCellValue(source.getDate());
            target.setCellStyle(dateStyle);
        } else if (source.isNumber()) {
            target.setCellValue(source.getNumber().doubleValue());
        } else if (source.isString()) {
            target.setCellValue(source.getString());
        }
    }
}

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
package ec.tss.sa.output;

import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsDataTableInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Kristof Bayens
 */
public class XSSFHelper {

//    public static XSSFCell setRowValues(XSSFCell cell, TsDomain domain) {
//
//    }
    @Deprecated
    public static XSSFSheet addSheet(XSSFWorkbook curBook, String sheetName, String[] headers0, String[] headers1, TsDataTable table, boolean verticalOrientation) {
        return (XSSFSheet) addSheet((Workbook) curBook, sheetName, headers0, headers1, table, verticalOrientation);
    }

    public static Sheet addSheet(Workbook curBook, String sheetName, String[] headers0, String[] headers1, TsDataTable table, boolean verticalOrientation) {
        Sheet sheet = curBook.createSheet(sheetName);
        Row currentRow = null;
        Cell currentCell = null;

        if (verticalOrientation) {
            //headers0
            int rowNum = 0;
            currentRow = sheet.createRow(rowNum);
            for (int cellNum = 0; cellNum < headers0.length; cellNum++) {
                currentCell = currentRow.createCell(cellNum + 1, CellType.STRING);
                currentCell.setCellValue(headers0[cellNum]);
            }
            //headers1
            rowNum++;
            currentRow = sheet.createRow(rowNum);
            for (int cellNum = 0; cellNum < headers1.length; cellNum++) {
                currentCell = currentRow.createCell(1 + cellNum, CellType.STRING);
                currentCell.setCellValue(headers1[cellNum]);
            }
            //columnvalues & data
            if (table.getDomain() != null) {
                for (int i = 0; i < table.getDomain().getLength(); i++) {
                    ++rowNum;
                    currentRow = sheet.createRow(rowNum);
                    int cellNum = 0;
                    currentCell = currentRow.createCell(cellNum);
                    currentCell.setCellValue(table.getDomain().get(i).firstday().toString());
                    for (int j = 0; j < table.getSeriesCount(); j++) {
                        cellNum++;
                        currentCell = currentRow.createCell(cellNum);
                        TsDataTableInfo info = table.getDataInfo(i, j);
                        if (info == TsDataTableInfo.Valid) {
                            currentCell.setCellValue(table.getData(i, j));
                        } else {
                            currentCell.setCellValue("");
                        }
                    }
                }
            }
        } else {
            // headers0
            int rowNum = 0;
            int nbComponents = headers1.length / countNbSeries(headers0);
            int currentData = 0;
            for (String h : headers0) {
                if (!h.isEmpty()) {
                    currentRow = sheet.createRow(rowNum);
                    currentCell = currentRow.createCell(1, CellType.STRING);
                    currentCell.setCellValue(h);

                    // Periods
                    rowNum++;
                    currentRow = sheet.createRow(rowNum);
                    for (int i = 0; i < table.getDomain().getLength(); i++) {
                        currentCell = currentRow.createCell(i + 1);
                        currentCell.setCellValue(table.getDomain().get(i).firstday().toString());
                    }

                    // Components + Data
                    for (int i = currentData; i < currentData + nbComponents; i++) {
                        currentRow = sheet.createRow(++rowNum);
                        currentCell = currentRow.createCell(0, CellType.STRING);
                        currentCell.setCellValue(headers1[i]);
                        for (int j = 0; j < table.getDomain().getLength(); j++) {
                            currentCell = currentRow.createCell(j + 1);
                            TsDataTableInfo info = table.getDataInfo(j, i);
                            if (info == TsDataTableInfo.Valid) {
                                currentCell.setCellValue(table.getData(j, i));
                            } else {
                                currentCell.setCellValue("");
                            }
                        }
                    }

                    rowNum += 2;
                }
                currentData++;
            }

        }

        return sheet;
    }

    private static int countNbSeries(String[] headers0) {
        if (headers0 == null || headers0.length == 0) {
            return 0;
        }

        int count = 0;
        for (String h : headers0) {
            if (!h.isEmpty()) {
                count++;
            }
        }

        return count;
    }
}

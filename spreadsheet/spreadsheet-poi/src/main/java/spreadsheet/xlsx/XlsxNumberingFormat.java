/*
 * Copyright 2016 National Bank of Belgium
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
package spreadsheet.xlsx;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import spreadsheet.xlsx.internal.DefaultXlsxNumberingFormat;

/**
 * Class that deals with the formatting of numbers in Excel.
 *
 * @author Philippe Charles
 * @since 2.2.0
 * @see
 * https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.numberingformat.aspx
 */
public interface XlsxNumberingFormat {

    /**
     * Checks if the specified format represents an Excel date.
     *
     * @param numFmtId
     * @param formatCode
     * @return
     */
    boolean isExcelDateFormat(int numFmtId, @Nullable String formatCode);

    /**
     * Gets the default implementation.
     *
     * @return a non-null implementation
     */
    @Nonnull
    static XlsxNumberingFormat getDefault() {
        return DefaultXlsxNumberingFormat.INSTANCE;
    }
}

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
package spreadsheet.xlsx.internal;

import java.util.PrimitiveIterator;
import java.util.function.IntPredicate;
import javax.annotation.Nullable;
import spreadsheet.xlsx.XlsxNumberingFormat;

/**
 *
 * @author Philippe Charles
 */
public final class DefaultXlsxNumberingFormat implements XlsxNumberingFormat {

    public static final XlsxNumberingFormat INSTANCE = new DefaultXlsxNumberingFormat();

    @Override
    public boolean isExcelDateFormat(int numFmtId, String formatCode) {
        return isBuiltInExcelDateFormat(numFmtId)
                || isCustomExcelDateFormat(formatCode);
    }

    // https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.numberingformat.aspx
    private boolean isBuiltInExcelDateFormat(int numFmtId) {
        switch (numFmtId) {
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 45:
            case 46:
            case 47:
                return true;
            default:
                return false;
        }
    }

    // https://support.office.com/en-us/article/Create-or-delete-a-custom-number-format-78f2a361-936b-4c03-8772-09fab54be7f4#bm1
    private boolean isCustomExcelDateFormat(@Nullable String formatCode) {
        if (formatCode != null) {
            PrimitiveIterator.OfInt iter = formatCode.chars().iterator();
            while (iter.hasNext()) {
                switch ((char) iter.nextInt()) {
                    case '"':
                        skipUntil(iter, o -> o == '"');
                        break;
                    case '[':
                        skipUntil(iter, o -> o == ']');
                        break;
                    case '\\':
                        if (iter.hasNext()) {
                            iter.nextInt();
                        }
                        break;
                    case 'm':
                    case 'd':
                    case 'y':
                    case 'h':
                    case 's':
                        return true;
                }
            }
        }
        return false;
    }

    private void skipUntil(PrimitiveIterator.OfInt iterator, IntPredicate predicate) {
        while (iterator.hasNext() && !predicate.test(iterator.nextInt())) {
        }
    }
}

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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import spreadsheet.xlsx.XlsxNumberingFormat;

/**
 *
 * @author Philippe Charles
 */
public class DefaultXlsxNumberingFormatlTest {

    @Test
    public void testDefault() {
        XlsxNumberingFormat dateFormat = XlsxNumberingFormat.getDefault();
        assertThat(dateFormat.isExcelDateFormat(146, "0")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "0.00")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "#.##0")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "#.##0;[Red]-#.##0")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "_ € * #.##0,00_ ;_ € * -#.##0,00_ ;_ € * \"-\"??_ ;_ @_ ")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "\"dd-mm-yyyy\"")).isFalse();
        assertThat(dateFormat.isExcelDateFormat(146, "dd-mm-yyyy")).isTrue();
        assertThat(dateFormat.isExcelDateFormat(146, "d/mm/yyyy")).isTrue();
        assertThat(dateFormat.isExcelDateFormat(146, "d-mmm-yy")).isTrue();
        assertThat(dateFormat.isExcelDateFormat(146, "h:mm:ss AM/PM")).isTrue();
        assertThat(dateFormat.isExcelDateFormat(146, "[$-80C]dddd d mmmm yyyy")).isTrue();
    }
}

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
package demetra.tsprovider.util;

import java.time.LocalDate;
import nbbrd.io.text.Parser;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class StrangeParsersTest {

    @Test
    public void testYearFreqPosParser() {
        Parser<LocalDate> parser = StrangeParsers.yearFreqPosParser();
        assertThat(parser.parse("2010M1")).isEqualTo("2010-01-01");
        assertThat(parser.parse("2010-M1")).isEqualTo("2010-01-01");
        assertThat(parser.parse("2010M2")).isEqualTo("2010-02-01");
        assertThat(parser.parse("2010-M2")).isEqualTo("2010-02-01");
        assertThat(parser.parse("2010Q1")).isEqualTo("2010-01-01");
        assertThat(parser.parse("2010-Q1")).isEqualTo("2010-01-01");
        assertThat(parser.parse("1234")).isNull();
    }
}

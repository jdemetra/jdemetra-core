/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.tsprovider;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class TsMetaTest {

    @Test
    @SuppressWarnings("null")
    public void testBeg() {
        assertThat(TsMeta.BEG.load(o -> "2010-02-15")).isEqualTo("2010-02-15T00:00");
        assertThat(TsMeta.BEG.load(o -> "hello")).isNull();
        assertThat(TsMeta.BEG.load(o -> "")).isNull();
        assertThat(TsMeta.BEG.load(o -> null)).isNull();
    }

    @Test
    @SuppressWarnings("null")
    public void testTimestamp() {
        LocalDateTime now = LocalDateTime.now();

        // Note: Date#toString() reduces precision to the second
        String legacyDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).toString();
        assertThat(TsMeta.TIMESTAMP.load(o -> legacyDate)).isEqualTo(now.with(ChronoField.MILLI_OF_SECOND, 0));

        assertThat(TsMeta.TIMESTAMP.load(o -> "hello")).isNull();
        assertThat(TsMeta.TIMESTAMP.load(o -> "")).isNull();
        assertThat(TsMeta.TIMESTAMP.load(o -> null)).isNull();

        Map<String, String> meta = new HashMap<>();
        TsMeta.TIMESTAMP.store(meta, now);
        assertThat(TsMeta.TIMESTAMP.load(meta)).isEqualTo(now);
    }
}

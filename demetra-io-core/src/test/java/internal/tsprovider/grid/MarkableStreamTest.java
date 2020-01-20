/*
 * Copyright 2020 National Bank of Belgium
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
package internal.tsprovider.grid;

import demetra.tsprovider.grid.GridInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import test.tsprovider.grid.ArrayGridInput;
import test.tsprovider.grid.Data;

/**
 *
 * @author Philippe Charles
 */
public class MarkableStreamTest {

    @Test
    public void testEmpty() throws IOException {
        MarkableStream x = new MarkableStream(Data.EMPTY.open());
        assertThat(x.readRow()).isFalse();
    }

    @Test
    public void testMark() throws IOException {
        ArrayGridInput sample = Data.HGRID;

        for (StreamConsumer peek : PEEKS) {
            assertThat(of(sample, peek))
                    .isEqualTo(sample);

            assertThat(of(sample, peek.then(peek)))
                    .isEqualTo(sample);

            assertThat(of(sample, READ_ROW.then(peek)))
                    .isEqualTo(sample.subrows(1, 2));

            assertThat(of(sample, peek.then(READ_ROW)))
                    .isEqualTo(sample.subrows(1, 2));

            assertThat(of(sample, peek.then(READ_ROW).then(peek)))
                    .isEqualTo(sample.subrows(1, 2));
        }
    }

    private ArrayGridInput of(GridInput in, StreamConsumer consumer) throws IOException {
        MarkableStream x = new MarkableStream(in.open());
        consumer.accept(x);
        return ArrayGridInput.of(toArray(x));
    }

    private Object[][] toArray(MarkableStream x) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        while (x.readRow()) {
            List<Object> row = new ArrayList<>();
            while (x.readCell()) {
                row.add(x.getCell());
            }
            rows.add(row.toArray());
        }
        return rows.toArray(new Object[0][]);
    }

    private interface StreamConsumer {

        void accept(MarkableStream x) throws IOException;

        default StreamConsumer then(StreamConsumer after) {
            Objects.requireNonNull(after);
            return (MarkableStream t) -> {
                accept(t);
                after.accept(t);
            };
        }
    }

    private static final StreamConsumer MARK = MarkableStream::mark;
    private static final StreamConsumer RESET = MarkableStream::reset;
    private static final StreamConsumer READ_ROW = x -> {
        x.readRow();
    };
    private static final StreamConsumer READ_ROW_CELL = x -> {
        if (x.readRow()) {
            x.readCell();
        }
    };
    private static final StreamConsumer READ_ROW_CELLS = x -> {
        if (x.readRow()) {
            while (x.readCell()) {
            }
        }
    };
    private static final StreamConsumer READ_ROWS = x -> {
        while (x.readRow()) {
        }
    };
    private static final StreamConsumer READ_ROWS_CELL = x -> {
        while (x.readRow()) {
            x.readCell();
        }
    };
    private static final StreamConsumer READ_ROWS_CELLS = x -> {
        while (x.readRow()) {
            while (x.readCell()) {
            }
        }
    };
    private static final StreamConsumer PEEK_ROW = MARK.then(READ_ROW).then(RESET);
    private static final StreamConsumer PEEK_ROW_CELL = MARK.then(READ_ROW_CELL).then(RESET);
    private static final StreamConsumer PEEK_ROW_CELLS = MARK.then(READ_ROW_CELLS).then(RESET);
    private static final StreamConsumer PEEK_ROWS = MARK.then(READ_ROWS).then(RESET);
    private static final StreamConsumer PEEK_ROWS_CELL = MARK.then(READ_ROWS_CELL).then(RESET);
    private static final StreamConsumer PEEK_ROWS_CELLS = MARK.then(READ_ROWS_CELLS).then(RESET);
    private static final StreamConsumer[] PEEKS = {PEEK_ROW, PEEK_ROW_CELL, PEEK_ROW_CELLS, PEEK_ROWS, PEEK_ROWS_CELL, PEEK_ROWS_CELLS};
}

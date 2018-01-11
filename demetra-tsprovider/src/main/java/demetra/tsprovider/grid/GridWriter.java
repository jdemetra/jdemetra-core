/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.tsprovider.grid;

import static demetra.tsprovider.grid.GridLayout.VERTICAL;
import internal.tsprovider.grid.InvGridOutput;
import internal.tsprovider.grid.TsDataTable;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * @author Philippe Charles
 */
@NotThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class GridWriter {

    @lombok.NonNull
    private final GridExport options;

    @lombok.NonNull
    private final GridInfo info;

    public void write(@Nonnull TsCollectionGrid value, @Nonnull GridOutput output) {
        output.setName(value.getName());

        TsDataTable table = new TsDataTable();
        for (TsGrid o : value.getItems()) {
            table.insert(-1, o.getData().get());
        }

        if (table.getDomain() != null) {
            if (!options.getLayout().equals(VERTICAL)) {
                output = InvGridOutput.of(output);
            }

            if (options.isShowTitle()) {
                output.setRow(0, options.isShowDates() ? 1 : 0, value.getItems().stream().map(TsGrid::getName).iterator());
            }

            if (options.isShowDates()) {
                output.setColumn(options.isShowTitle() ? 1 : 0, 0, table.getDomain().stream().map(options.isBeginPeriod() ? o -> o.start() : o -> o.end()).iterator());
            }

            int firstRow = options.isShowTitle() ? 1 : 0;
            int firstColumn = options.isShowDates() ? 1 : 0;
            int rowCount = table.getDomain().getLength();
            int columnCount = value.getItems().size();
            for (int i = 0; i < rowCount; ++i) {
                for (int j = 0; j < columnCount; ++j) {
                    if (table.getDataInfo(i, j) == TsDataTable.TsDataTableInfo.Valid) {
                        output.setValue(firstRow + i, firstColumn + j, table.getData(i, j));
                    }
                }
            }
        }
    }
}

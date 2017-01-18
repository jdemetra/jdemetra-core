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
package ec.tss.tsproviders.cursor;

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataDisplayName;
import ec.tss.tsproviders.HasDataMoniker;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tss.tsproviders.utils.TsFiller;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public final class TsCursorAsFiller {

    private TsCursorAsFiller() {
        // static class
    }

    /**
     * Creates a new instance of TsFiller using cursors.
     *
     * @param logger a non-null logger
     * @param hdc a non-null ts cursor support
     * @param hdm a non-null data moniker support
     * @param hddn a non-null data display name support
     * @return a non-null instance
     */
    @Nonnull
    public static TsFiller of(@Nonnull Logger logger, @Nonnull HasTsCursor hdc, @Nonnull HasDataMoniker hdm, @Nonnull HasDataDisplayName hddn) {
        return new ComposedFiller(new CursorResource(logger, hdc, hdm, hddn));
    }

    private static final class ComposedFiller implements TsFiller {

        private final Resource resource;

        ComposedFiller(@Nonnull Resource resource) {
            this.resource = Objects.requireNonNull(resource);
        }

        @Override
        public boolean fillCollection(TsCollectionInformation info) {
            DataSource dataSource = resource.toDataSource(info);
            if (isCollection(dataSource)) {
                try {
                    return resource.fill(info, dataSource);
                } catch (Exception ex) {
                    return resource.reportException(info, dataSource, ex);
                }
            }

            DataSet dataSet = resource.toDataSet(info);
            if (isCollection(dataSet)) {
                try {
                    return resource.fill(info, dataSet);
                } catch (Exception ex) {
                    return resource.reportException(info, dataSet, ex);
                }
            }

            return resource.reportInvalid(info);
        }

        @Override
        public boolean fillSeries(TsInformation info) {
            DataSet dataSet = resource.toDataSet(info);
            if (isSeries(dataSet)) {
                try {
                    return resource.fill(info, dataSet);
                } catch (Exception ex) {
                    return resource.reportException(info, dataSet, ex);
                }
            }

            return resource.reportInvalid(info);
        }

        private static boolean isCollection(DataSource dataSource) {
            return dataSource != null;
        }

        private static boolean isCollection(DataSet dataSet) {
            return dataSet != null && DataSet.Kind.COLLECTION.equals(dataSet.getKind());
        }

        private static boolean isSeries(DataSet dataSet) {
            return dataSet != null && DataSet.Kind.SERIES.equals(dataSet.getKind());
        }
    }

    private interface Resource {

        DataSource toDataSource(TsCollectionInformation info);

        boolean fill(TsCollectionInformation info, DataSource dataSource) throws Exception;

        boolean reportException(TsCollectionInformation info, DataSource dataSource, Exception ex);

        DataSet toDataSet(TsCollectionInformation info);

        boolean fill(TsCollectionInformation info, DataSet dataSet) throws Exception;

        boolean reportException(TsCollectionInformation info, DataSet dataSet, Exception ex);

        boolean reportInvalid(TsCollectionInformation info);

        DataSet toDataSet(TsInformation info);

        boolean fill(TsInformation info, DataSet dataSet) throws Exception;

        boolean reportException(TsInformation info, DataSet dataSet, Exception ex);

        boolean reportInvalid(TsInformation info);
    }

    private static final class CursorResource implements Resource {

        private final Logger logger;
        private final HasTsCursor htc;
        private final HasDataMoniker hdm;
        private final HasDataDisplayName hddn;

        CursorResource(@Nonnull Logger logger, @Nonnull HasTsCursor htc, @Nonnull HasDataMoniker hdm, @Nonnull HasDataDisplayName hddn) {
            this.logger = Objects.requireNonNull(logger, "Logger");
            this.htc = Objects.requireNonNull(htc, "HasTsCursor");
            this.hdm = Objects.requireNonNull(hdm, "HasDataMoniker");
            this.hddn = Objects.requireNonNull(hddn, "HasDataDisplayName");
        }

        @Override
        public DataSource toDataSource(TsCollectionInformation info) {
            return hdm.toDataSource(info.moniker);
        }

        @Override
        public DataSet toDataSet(TsCollectionInformation info) {
            return hdm.toDataSet(info.moniker);
        }

        @Override
        public DataSet toDataSet(TsInformation info) {
            return hdm.toDataSet(info.moniker);
        }

        @Override
        public boolean reportException(TsCollectionInformation info, DataSet dataSet, Exception ex) {
            logger.error("While getting collection '" + info.moniker + "'", ex);
            info.invalidDataCause = ex.getMessage();
            return false;
        }

        @Override
        public boolean reportException(TsCollectionInformation info, DataSource dataSource, Exception ex) {
            logger.error("While getting collection '" + info.moniker + "'", ex);
            info.invalidDataCause = ex.getMessage();
            return false;
        }

        @Override
        public boolean reportException(TsInformation info, DataSet dataSet, Exception ex) {
            logger.error("While getting series '" + info.moniker + "'", ex);
            info.invalidDataCause = ex.getMessage();
            return false;
        }

        @Override
        public boolean reportInvalid(TsCollectionInformation info) {
            logger.warn("Invalid collection moniker '{}'", info.moniker);
            info.invalidDataCause = "Invalid moniker";
            return false;
        }

        @Override
        public boolean reportInvalid(TsInformation info) {
            logger.warn("Invalid ts moniker '{}'", info.moniker);
            info.invalidDataCause = "Invalid moniker";
            return false;
        }

        @Override
        public boolean fill(TsCollectionInformation info, DataSource dataSource) throws IOException {
            try (TsCursor<DataSet> cursor = htc.getData(dataSource, info.type)) {
                fill(info, cursor);
                return true;
            }
        }

        @Override
        public boolean fill(TsCollectionInformation info, DataSet dataSet) throws IOException {
            try (TsCursor<DataSet> cursor = htc.getData(dataSet, info.type)) {
                fill(info, cursor);
                return true;
            }
        }

        @Override
        public boolean fill(TsInformation info, DataSet dataSet) throws IOException {
            try (TsCursor<DataSet> cursor = htc.getData(dataSet, info.type)) {
                if (cursor.nextSeries()) {
                    fill(info, cursor);
                    return true;
                } else {
                    info.invalidDataCause = "Missing time series";
                    return false;
                }
            }
        }

        private void fill(TsCollectionInformation info, TsCursor<DataSet> cursor) throws IOException {
            while (cursor.nextSeries()) {
                TsInformation item = new TsInformation();
                item.type = info.type;
                fill(item, cursor);
                info.items.add(item);
            }
        }

        private void fill(TsInformation info, TsCursor<DataSet> cursor) throws IOException {
            fillId(info, cursor);
            if (info.type.encompass(TsInformationType.Data)) {
                fillData(info, cursor);
            }
            if (info.type.encompass(TsInformationType.MetaData)) {
                fillMeta(info, cursor);
            }
        }

        private void fillId(TsInformation info, TsCursor<DataSet> cursor) throws IOException {
            DataSet dataSet = cursor.getId();
            info.name = hddn.getDisplayName(dataSet);
            info.moniker = hdm.toMoniker(dataSet);
        }

        private void fillData(TsInformation info, TsCursor<DataSet> cursor) throws IOException {
            OptionalTsData data = cursor.getData();
            if (data.isPresent()) {
                info.data = data.get();
                info.invalidDataCause = null;
            } else {
                info.data = null;
                info.invalidDataCause = data.getCause();
            }
        }

        private void fillMeta(TsInformation info, TsCursor<DataSet> cursor) throws IOException {
            info.metaData = cursor.getMetaData().orElse(null);
        }
    }
}

package demetra.demo;

/*
 * Copyright 2015 National Bank of Belgium
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
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceProvider;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.TsProviders;
import demetra.tsprovider.util.MultiLineNameUtil;
import demetra.timeseries.TsDomain;
import demetra.tsprovider.DataSourceLoader;
import demetra.tsprovider.TsMoniker;
import demetra.utilities.TreeTraverser;
import ioutil.IO;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ProviderDemo {

    public File resourceAsFile(Class<?> source, String name) {
        try {
            return new File(source.getResource(name).toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void printTree(DataSourceProvider provider, DataSource dataSource) throws IllegalArgumentException, IOException {
        System.out.println("[TREE]");
        TsProviders.prettyPrintTree(provider, dataSource, Integer.MAX_VALUE, System.out, true);
        System.out.println("");
    }

    public void printFirstSeries(DataSourceProvider provider, DataSource dataSource) throws IllegalArgumentException, IOException {
        printFirstSeries(provider, dataSource, TsStrategy.ALL);
    }

    public void printFirstSeries(DataSourceProvider provider, DataSource dataSource, TsStrategy strategy) throws IllegalArgumentException, IOException {
        System.out.println("[FIRST SERIES]");
        Clock clock = Clock.systemDefaultZone();
        Instant start = clock.instant();
        Optional<Ts> ts = strategy.getFirst(provider, dataSource);
        if (ts.isPresent()) {
            printSeries(provider, ts.get());
            printDuration(Duration.between(start, clock.instant()));
        } else {
            System.out.println("No series found");
        }
        System.out.println();
    }

    public void printSeries(DataSourceProvider provider, Ts ts) {
        printId(provider, provider.toDataSet(ts.getMoniker()));
        printLabel(ts.getName());
        printMetaData(ts.getMeta());
        printDataSummary(ts.getData());
    }

    public void printId(DataSourceProvider provider, DataSet id) {
        System.out.printf("%9s %s\n", "Uri:", DataSet.uriFormatter().format(id));
        System.out.printf("%9s %s\n", "Display:", MultiLineNameUtil.join(provider.getDisplayName(id), " \n          "));
    }

    public void printLabel(String label) {
        System.out.printf("%9s %s\n", "Label:", MultiLineNameUtil.join(label, " \n          "));
    }

    public void printMetaData(Map<String, String> metaData) {
        if (metaData != null && !metaData.isEmpty()) {
            Iterator<Entry<String, String>> iterator = metaData.entrySet().iterator();
            Entry<String, String> item = iterator.next();
            System.out.printf("%9s %s = %s\n", "MetaData:", item.getKey(), item.getValue());
            while (iterator.hasNext()) {
                item = iterator.next();
                System.out.printf("%9s %s = %s\n", "", item.getKey(), item.getValue());
            }
        } else {
            System.out.printf("%9s %s\n", "MetaData:", "none");
        }
    }

    public void printDataSummary(TsData data) {
        if (!data.isEmpty()) {
            TsDomain d = data.getDomain();
            int validValues = (int) data.getValues().reduce(0, (c, o) -> !Double.isNaN(o) ? c + 1 : c);
            System.out.printf("%9s %s, from %s to %s, %d/%d obs\n", "Data:", d.getTsUnit(), d.start(), d.end(), validValues, d.length());
        } else {
            System.out.printf("%9s %s\n", "Data:", data.getCause());
        }
    }

    public void printDuration(Duration duration) {
        System.out.printf("%9s %sms\n", "Duration:", duration.toMillis());
    }

    public enum TsStrategy {
        ALL {
            @Override
            Optional<Ts> getFirst(DataSourceProvider provider, DataSource dataSource) throws IOException {
                TsCollection info = provider.getTsCollection(provider.toMoniker(dataSource), TsInformationType.All);
                return info.getData().stream().findFirst();
            }
        },
        DEFINITION {
            @Override
            Optional<Ts> getFirst(DataSourceProvider provider, DataSource dataSource) throws IOException {
                TsCollection info = provider.getTsCollection(provider.toMoniker(dataSource), TsInformationType.Definition);
                if (!info.getData().isEmpty()) {
                    Ts ts = info.getData().get(0);
                    return Optional.of(provider.getTs(ts.getMoniker(), TsInformationType.All));
                }
                return Optional.empty();
            }
        },
        CHILDREN {
            @Override
            Optional<Ts> getFirst(DataSourceProvider provider, DataSource dataSource) throws IOException {
                IO.Function<Object, Iterable<? extends Object>> children = o -> {
                    return o instanceof DataSource
                            ? provider.children((DataSource) o)
                            : ((DataSet) o).getKind() == DataSet.Kind.COLLECTION ? provider.children((DataSet) o) : Collections.emptyList();
                };

                Optional<DataSet> result = TreeTraverser
                        .of(dataSource, children.asUnchecked())
                        .depthFirstStream()
                        .filter(DataSet.class::isInstance)
                        .map(DataSet.class::cast)
                        .filter(o -> o.getKind().equals(DataSet.Kind.SERIES))
                        .findFirst();
                return result.isPresent()
                        ? Optional.of(provider.getTs(provider.toMoniker(result.get()), TsInformationType.All))
                        : Optional.empty();
            }
        };

        abstract Optional<Ts> getFirst(DataSourceProvider provider, DataSource dataSource) throws IOException;
    }

    public void printDataTable(DataSourceProvider provider, DataSource dataSource, TsDataTable.DistributionType distribution) throws IOException {
        TsCollection col = provider.getTsCollection(provider.toMoniker(dataSource), TsInformationType.Data);

        Function<Ts, String> toDisplayNodeName = o -> provider.getDisplayNodeName(provider.toDataSet(o.getMoniker()));
        System.out.println("\t" + col.getData().stream().map(toDisplayNodeName).collect(Collectors.joining("\t")));

        TsDataTable table = TsDataTable.of(col.getData(), Ts::getData);
        DataTypesDemo.printDataTable(table, distribution);
    }

    public <B, P extends DataSourceLoader<B>> TsCollection getAll(P provider, TsInformationType infoType, Consumer<B> configurator) throws IOException {
        B bean = provider.newBean();
        configurator.accept(bean);
        DataSource dataSource = provider.encodeBean(bean);
        TsMoniker moniker = provider.toMoniker(dataSource);
        return provider.getTsCollection(moniker, infoType);
    }
}

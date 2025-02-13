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

import ec.satoolkit.ISaSpecification;
import ec.tss.sa.documents.SaDocument;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.algorithm.IOutput;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.utilities.NamedObject;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 *
 * @author Kristof Bayens
 */
public class SpreadsheetOutput implements IOutput<SaDocument<ISaSpecification>> {

    SpreadsheetOutputConfiguration config_;
    List<DefaultSummary> summaries_;
    File folder_;
    private boolean fullName;

    public SpreadsheetOutput(SpreadsheetOutputConfiguration config) {
        summaries_ = new ArrayList<>();
        config_ = (SpreadsheetOutputConfiguration) config.clone();
        fullName = config_.isFullName();
    }

    @Override
    public void process(SaDocument<ISaSpecification> document) {
        DefaultSummary summary = new DefaultSummary(document.getInput().getRawName(), document.getResults(), config_.getSeries());
        if (config_.isSaveModel()) {
            summary.setModel(document.getSpecification());
        }
        summaries_.add(summary);
    }

    @Override
    public void start(Object context) {
        summaries_.clear();
        folder_ = BasicConfiguration.folderFromContext(config_.getFolder(), context);
    }

    @Override
    public void end(Object context) throws Exception {
        String file = folder_.toPath().resolve(config_.getFileName()).toFile().getAbsolutePath();
        file = Paths.changeExtension(file, "xlsx");
        File ssfile = java.nio.file.Paths.get(file).toFile();
        //File ssfile = new File("C:\\test.xls");
        SXSSFWorkbook workbook = new SXSSFWorkbook(null, 100, false, true);

        try (OutputStream stream = Files.newOutputStream(ssfile.toPath())) {
            switch (config_.getLayout()) {
                case ByComponent: {
                    HashMap<String, List<NamedObject<TsData>>> allData = new HashMap<>();
                    for (DefaultSummary summary : summaries_) {
                        for (Entry<String, TsData> keyValue : summary.getAllSeries().entrySet()) {
                            List<NamedObject<TsData>> list = null;
                            if (!allData.containsKey(keyValue.getKey())) {
                                list = new ArrayList<>();
                                allData.put(keyValue.getKey(), list);
                            } else {
                                list = allData.get(keyValue.getKey());
                            }
                            String name;

                            if (fullName) {
                                name = MultiLineNameUtil.join(summary.getName(), " * ");
                            } else {
                                name = MultiLineNameUtil.last(summary.getName());
                            }
                            list.add(new NamedObject<>(name, keyValue.getValue()));
                        }
                    }
                    for (Entry<String, List<NamedObject<TsData>>> keyValue : allData.entrySet()) {
                        TsDataTable byComponentTable = new TsDataTable();
                        List<NamedObject<TsData>> value = keyValue.getValue();
                        String[] headers = new String[value.size()];
                        for (int i = 0; i < headers.length; i++) {
                            NamedObject<TsData> data = value.get(i);
                            headers[i] = data.name;
                            byComponentTable.insert(-1, data.object);
                        }
                        //ADD SHEET
                        XSSFHelper.addSheet(workbook, keyValue.getKey(), new String[]{keyValue.getKey()}, headers, byComponentTable, config_.isVerticalOrientation());
                    }
                    break;
                }
                case BySeries: {
                    for (int i = 0; i < summaries_.size(); i++) {
                        DefaultSummary summary = summaries_.get(i);
                        Set<Entry<String, TsData>> tmp = summary.getAllSeries().entrySet();
                        TsDataTable bySeriesTable = new TsDataTable();
                        String[] componentHeaders = new String[tmp.size()];
                        int j = 0;
                        for (Entry<String, TsData> keyValue : tmp) {
                            componentHeaders[j++] = keyValue.getKey();
                            bySeriesTable.insert(-1, keyValue.getValue());
                        }
                        //ADD SHEET
                        String name;
                        if (fullName) {
                            name = MultiLineNameUtil.join(summary.getName(), " * ");
                        } else {
                            name = MultiLineNameUtil.last(summary.getName());
                        }
                        XSSFHelper.addSheet(workbook, "Series" + Integer.toString(i), new String[]{name}, componentHeaders, bySeriesTable, config_.isVerticalOrientation());
                    }
                    break;
                }
                case OneSheet: {
                    List<String> headers0 = new ArrayList<>();
                    List<String> headers1 = new ArrayList<>();
                    TsDataTable oneSheetTable = new TsDataTable();

                    for (DefaultSummary summary : summaries_) {
                        String name;
                        if (fullName) {
                            name = MultiLineNameUtil.join(summary.getName(), " * ");
                        } else {
                            name = MultiLineNameUtil.last(summary.getName());
                        }
                        headers0.add(name);
                        Map<String, TsData> data = summary.getAllSeries();
                        for (Entry<String, TsData> keyValue : data.entrySet()) {
                            headers1.add(keyValue.getKey());
                            oneSheetTable.insert(-1, keyValue.getValue());
                        }
                        for (int i = 1; i < data.size(); i++) {
                            headers0.add("");
                        }
                    }
                    //ADD SHEET
                    XSSFHelper.addSheet(workbook, "Series", headers0.stream().toArray(String[]::new), headers1.stream().toArray(String[]::new), oneSheetTable, config_.isVerticalOrientation());
                    break;
                }
            }
            workbook.write(stream);
        } finally {
            workbook.dispose();            
        }
    }

    @Override
    public String getName() {
        return "Spreadsheet";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

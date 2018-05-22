/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.jdr.ws;

import demetra.datatypes.Ts;
import demetra.datatypes.sa.SaProcessingType;
import demetra.datatypes.sa.SaItemType;
import demetra.workspace.WorkspaceFamily;
import demetra.workspace.WorkspaceItem;
import demetra.workspace.file.FileFormat;
import demetra.workspace.file.FileRepository;
import demetra.workspace.file.FileWorkspace;
import demetra.workspace.util.Paths;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.jdr.sa.Processor;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import jdr.spec.ts.Utility;

/**
 *
 * @author Jean Palate
 */
public class Workspace {

    public static Workspace open(String fileName) {
        try {
            File file = new File(fileName);
            FileWorkspace fws = FileWorkspace.open(file.toPath());
            Collection<WorkspaceItem> items = fws.getItems();
            ProcessingContext context = new ProcessingContext();
            Map<WorkspaceItem, GregorianCalendarManager> cal = FileRepository.loadAllCalendars(fws, context);
            Map<WorkspaceItem, TsVariables> vars = FileRepository.loadAllVariables(fws, context);
            Map<WorkspaceItem, SaProcessingType> sa = FileRepository.loadAllSaProcessing(fws, context);
            Workspace ws = new Workspace(context);
            for (Map.Entry<WorkspaceItem, SaProcessingType> entry : sa.entrySet()) {
                ws.multiProcessing.add(MultiProcessing.of(entry.getKey().getLabel(), entry.getValue()));
            }
            return ws;
        } catch (IOException ex) {
            return null;
        }
    }

    public static Workspace create(Utility.Dictionary dic) {
        return new Workspace(dic == null ? new ProcessingContext() : dic.toContext());
    }

    public MultiProcessing getMultiProcessing(int idx) {
        return multiProcessing.get(idx);
    }

    public int getMultiProcessingCount() {
        return multiProcessing.size();
    }

    public MultiProcessing newMultiProcessing(String name) {
        MultiProcessing n = MultiProcessing.of(name, new SaProcessingType());
        multiProcessing.add(n);
        return n;
    }

    private final ProcessingContext context;
    private final List<MultiProcessing> multiProcessing = new ArrayList<>();

    private Workspace(final ProcessingContext context) {
        this.context = context;
    }

    public void computeAll() {
        multiProcessing.parallelStream().forEach(p -> p.compute(context));
    }

    public void compute(String name) {
        for (MultiProcessing p : multiProcessing) {
            if (p.getName().equals(name)) {
                p.compute(context);
            }
        }
    }

    public boolean save(String fileName) {
        File file = new File(fileName);
        try {
            try (FileWorkspace fws = FileWorkspace.create(file.toPath(), FileFormat.GENERIC)) {
                fws.setName(Paths.getBaseName(fileName));
                for (MultiProcessing p : multiProcessing) {
                    WorkspaceItem cur = WorkspaceItem.builder()
                            .family(WorkspaceFamily.SA_MULTI)
                            .id(p.getName())
                            .label(p.getName())
                            .build();
                    SaProcessingType sa = p.toType();
                    //sa.getMetaData().put(SaProcessingType.TIMESTAMP, LocalDate.now().toString());
                    fws.store(cur, sa);
                }
                String[] names = context.getTsVariableManagers().getNames();
                for (int i = 0; i < names.length; ++i) {
                    WorkspaceItem cur = WorkspaceItem.builder()
                            .family(WorkspaceFamily.UTIL_VAR)
                            .id(names[i])
                            .label(names[i])
                            .build();
                    fws.store(cur, context.getTsVariables(names[i]));
                }
                WorkspaceItem cal = WorkspaceItem.builder()
                        .family(WorkspaceFamily.UTIL_CAL)
                        .id("Calendars")
                        .label("Calendars")
                        .build();
                fws.store(cal, context.getGregorianCalendars());
            }
            return true;
        } catch (IOException err) {
            return false;
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.workspace.r;

import demetra.sa.SaItems;
import demetra.sa.workspace.SaHandlers;
import demetra.timeseries.regression.ModellingContext;
import demetra.util.r.Dictionary;
import demetra.workspace.WorkspaceItemDescriptor;
import demetra.workspace.WorkspaceUtility;
import demetra.workspace.file.FileWorkspace;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
public class Ws {

    private final ModellingContext context;
    private final List<MultiProcessing> multiProcessing = new ArrayList<>();

    public Ws() {
        this.context = new ModellingContext();
    }

    public Ws(final ModellingContext context) {
        this.context = context;
    }

    public static Ws create(Dictionary dic) {
        return new Ws(dic == null ? new ModellingContext() : dic.toContext());
    }

    public static Ws open(String fileName) throws IOException {
//        try {
        File file = new File(fileName);
        FileWorkspace fws = FileWorkspace.open(file.toPath());
        ModellingContext context = WorkspaceUtility.context(fws, false);
        Ws ws = new Ws(context);

        List<WorkspaceItemDescriptor> sa = WorkspaceUtility.select(fws, SaHandlers.SA_MULTI);
        for (WorkspaceItemDescriptor s : sa) {
            SaItems mp = (SaItems) fws.load(s.getKey());
            ws.multiProcessing.add(MultiProcessing.of(s.getAttributes().getLabel(), mp));
        }
        return ws;
//        } catch (IOException ex) {
//            return null;
//        }
    }
//
//

    public MultiProcessing getMultiProcessing(int idx) {
        return multiProcessing.get(idx);
    }

    public int getMultiProcessingCount() {
        return multiProcessing.size();
    }

    public MultiProcessing newMultiProcessing(String name) {
        MultiProcessing n = new MultiProcessing(name);
        multiProcessing.add(n);
        return n;
    }

    public ModellingContext getContext() {
        return (this.context);
    }

    public Dictionary dictionary() {
        return Dictionary.fromContext(context);
    }

    public void computeAll() {
        multiProcessing.stream().forEach(p -> p.compute(context));
    }

    public void compute(String name) {
        multiProcessing.stream().filter(p -> p.getName().equals(name)).forEach(q -> q.compute(context));
    }

//    public boolean save(String fileName) {
//        File file = new File(fileName);
//        try {
//            try (FileWorkspace fws = FileWorkspace.create(file.toPath(), FileFormat.GENERIC)) {
//                fws.setName(Paths.getBaseName(fileName));
//                for (MultiProcessing p : multiProcessing) {
//                    WorkspaceItem cur = WorkspaceItem.builder()
//                            .family(WorkspaceFamily.SA_MULTI)
//                            .id(p.getName())
//                            .label(p.getName())
//                            .build();
//                    SaProcessingType sa = p.toType();
//                    //sa.getMetaData().put(SaProcessingType.TIMESTAMP, LocalDate.now().toString());
//                    fws.store(cur, sa);
//                }
//                String[] names = context.getTsVariableManagers().getNames();
//                for (int i = 0; i < names.length; ++i) {
//                    WorkspaceItem cur = WorkspaceItem.builder()
//                            .family(WorkspaceFamily.UTIL_VAR)
//                            .id(names[i])
//                            .label(names[i])
//                            .build();
//                    fws.store(cur, context.getTsVariables(names[i]));
//                }
//                WorkspaceItem cal = WorkspaceItem.builder()
//                        .family(WorkspaceFamily.UTIL_CAL)
//                        .id("Calendars")
//                        .label("Calendars")
//                        .build();
//                fws.store(cal, context.getGregorianCalendars());
//            }
//            return true;
//        } catch (IOException err) {
//            return false;
//        }
//    }
}

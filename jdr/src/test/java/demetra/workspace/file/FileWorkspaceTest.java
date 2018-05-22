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
package demetra.workspace.file;

import demetra.datatypes.sa.SaProcessingType;
import demetra.workspace.Workspace;
import demetra.workspace.WorkspaceItem;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FileWorkspaceTest {
    
    public FileWorkspaceTest() {
    }
    
    //@Test
    public void testSomeMethod() throws IOException {
        File file = new File("c:\\sarepository\\mytest.xml");
        FileWorkspace ws = FileWorkspace.open(file.toPath());
        Collection<WorkspaceItem> items = ws.getItems();
        items.forEach(item -> System.out.println(item.getLabel()));
        ProcessingContext context = new ProcessingContext();
        Map<WorkspaceItem, GregorianCalendarManager> cal = FileRepository.loadAllCalendars(ws, context);
        Map<WorkspaceItem, TsVariables> vars = FileRepository.loadAllVariables(ws, context);
        Map<WorkspaceItem, SaProcessingType> sa = FileRepository.loadAllSaProcessing(ws, context);
        
        File file2 = new File("c:\\sarepository\\mytest2.xml");
        FileWorkspace ws2 = FileWorkspace.create(file2.toPath(), FileFormat.GENERIC);
        ws.copyTo(ws2);
        ws2.setName("myTest2");
        ws.close();
        ws2.close();
    }
    
}

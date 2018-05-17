/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.ws;

import data.Data;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import jdr.spec.ts.Utility.Dictionary;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class WorkspaceTest {
    
    public WorkspaceTest() {
    }

    //@Test
    public void testOpen() {
        Workspace ws = Workspace.open("c:\\sarepository\\mytest.xml");
        long t0=System.currentTimeMillis();
        ws.computeAll();
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        
        ws.save("c:\\sarepository\\mytest3.xml");
    }
    
    //@Test
    public void testNew() {
        Workspace ws = Workspace.create(new Dictionary());
        MultiProcessing mp = ws.newMultiProcessing("test");
        mp.add("a", Data.P, TramoSeatsSpecification.RSAfull);
        mp.add("p", Data.P, X13Specification.RSA5);
        mp = ws.newMultiProcessing("test2");
        ws.save("c:\\sarepository\\mytest3.xml");
        
        ws = Workspace.open("c:\\sarepository\\mytest3.xml");
        System.out.println(ws.getMultiProcessingCount());
     }
}

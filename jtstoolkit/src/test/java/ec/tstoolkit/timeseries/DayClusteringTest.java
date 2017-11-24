/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.timeseries;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DayClusteringTest {
    
    public DayClusteringTest() {
    }

    @Test
    public void testCreate() {
        assertTrue(DayClustering.create(new int[]{0,2,2,2,0,0,1})!= null);
        assertTrue(DayClustering.create(new int[]{0,2,2,2,0,0,0})== null);
        assertTrue(DayClustering.create(new int[]{0,2,2,2,2,2,1})!= null);
        assertTrue(DayClustering.create(new int[]{0,1,2,3,4,5,6})!= null);
    }
    
    @Test
    public void testCount() {
        DayClustering cl0 = DayClustering.create(new int[]{0,1,1,1,1,1,0});
        DayClustering cl1 = DayClustering.create(new int[]{0,2,2,2,2,2,1});
        DayClustering cl2 = DayClustering.create(new int[]{0,1,2,3,4,5,6});
        assertTrue(cl0.getGroupsCount()==2);
        assertTrue(cl1.getGroupsCount()==3);
        assertTrue(cl2.getGroupsCount()==7);
    }
    
    @Test
    public void testGroups() {
        DayClustering cl0 = DayClustering.create(new int[]{0,1,1,1,1,1,0});
        DayClustering cl1 = DayClustering.create(new int[]{0,2,2,2,2,2,1});
        DayClustering cl2 = DayClustering.create(new int[]{0,1,2,3,4,5,6});
        assertTrue(cl0.group(0).length==2);
        assertTrue(cl1.group(0).length==1);
        int n=0;
        for (int i=0; i<cl0.getGroupsCount(); ++i)
            n+=cl0.group(i).length;
        assertTrue(n ==7);
        n=0;
        for (int i=0; i<cl1.getGroupsCount(); ++i)
            n+=cl1.group(i).length;
        assertTrue(n ==7);
        n=0;
        for (int i=0; i<cl2.getGroupsCount(); ++i)
            n+=cl2.group(i).length;
        assertTrue(n ==7);
    }
    
    @Test
    public void testToString() {
        DayClustering cl0 = DayClustering.create(new int[]{1,1,1,1,1,0,0});
        DayClustering cl1 = DayClustering.create(new int[]{1,1,1,1,1,2,0});
        DayClustering cl2 = DayClustering.create(new int[]{1,2,3,4,5,6,0});
        assertTrue(cl0.toString().equalsIgnoreCase("td2"));
        assertTrue(cl1.toString().equalsIgnoreCase("td3"));
        assertTrue(cl2.toString().equalsIgnoreCase("td7"));
    } 
    
    @Test
    public void testIsInside() {
        DayClustering cl0 = DayClustering.create(new int[]{0,1,1,1,1,1,0});
        DayClustering cl1 = DayClustering.create(new int[]{0,2,2,2,2,2,1});
        DayClustering cl2 = DayClustering.create(new int[]{0,1,2,3,4,5,6});
        DayClustering cl3 = DayClustering.create(new int[]{0,1,2,0,2,3,3});
        DayClustering cl4 = DayClustering.create(new int[]{0,1,2,2,2,3,0});
        assertTrue(cl0.isInside(cl1));
        assertTrue(cl1.isInside(cl2));
        assertTrue(cl3.isInside(cl2));
        assertTrue(!cl4.isInside(cl0));
        assertTrue(cl0.isInside(cl4));
    }    

}

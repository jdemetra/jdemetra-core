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
package ec.tstoolkit.information;

import java.util.Arrays;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class InformationSetTest {

    public InformationSetTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void test1() {
        InformationSet info = new InformationSet();
        // add to the root of the information set the string "Test", which is called item0 
        info.set("item0", "Test");
        // add to the sub-set "sub1" the integer '10', which is called item1 
        info.subSet("sub1").set("item1", 10);

        // search for the item 'item1' in 'sub1'
        assert (info.search("sub1.item1", Integer.class) == 10);
        // or better
        assert (info.search(InformationSet.item("sub1", "item1"), Integer.class) == 10);
        // or equivalently
        assert (info.search(InformationSet.concatenate("sub1", "item1"), Integer.class) == 10);
        assert (info.search("item0", String.class).equals("Test"));

        // retrieve information using the sub-sets.
        assert (info.getSubSet("sub1").get("item1", Integer.class) == 10);
        assert (info.get("item0", String.class).equals("Test"));

        // show the dictionary
        assert (info.getDictionary().size() == 2);
//        for (String s : info.getDictionary()) {
//            System.out.println(s);
//        }
    }

    @Test
    public void testConvert() {
        Object[] s = new Object[]{"a", "b"};
        String[] o = InformationSet.convert(s, String[].class);
        Object p = InformationSet.convert(o, Object[].class);
        assertTrue(Arrays.deepEquals(s, (Object[]) p));
    }
}

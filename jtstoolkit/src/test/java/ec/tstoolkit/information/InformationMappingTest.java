/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.information;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class InformationMappingTest {

    static final InformationMapping<Integer> mapping = new InformationMapping<>(Integer.class);

    static {
        mapping.set("convert", Double.class, i -> (double) i);
        mapping.set("sqrt", Double.class, i -> Math.sqrt(i));
        mapping.set("square", Integer.class, i -> i * i);
        mapping.setList("squares", 1, 20, Integer.class, (i, j) -> i * j * j);
        mapping.setList("arrays", 1, 10, int[].class, (i, j) -> {
            int[] a = new int[j];
            for (int k = 0; k < a.length; ++k) {
                a[k] = i * (k + 1);
            }
            return a;
        });
    }

    public InformationMappingTest() {
    }

    @Test
    //@Ignore
    public void testDictionary() {
        LinkedHashMap<String, Class> dic = new LinkedHashMap();
        mapping.fillDictionary(null, dic, true);
        dic.entrySet().stream().forEach(x -> {
            System.out.print(x.getKey());
            System.out.print('\t');
            System.out.println(x.getValue());
        }
        );
    }

    @Test
    public void testSearchAll() {
        Map<String, Object> all = mapping.searchAll(3, "*s*", Object.class);
        assertTrue(all.size() == 32);
        Map<String, Integer> allInt = mapping.searchAll(3, "*s*", Integer.class);
        assertTrue(allInt.size() == 21);
    }

   @Test
    public void testSearchId() {
        assertTrue(mapping.contains("squares(4)"));
        assertTrue(mapping.getData(5,"squares(4)", Integer.class) != null);
        assertTrue(mapping.getData(5,"squares(20)", Integer.class) != null);
    }
}

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

package ec.util.jdbc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class TypeNames {

    private TypeNames() {
        // static class
    }

    // Get all field in java.sql.Types
    @NonNull
    public static Map<Integer, String> getTypeNames() {
        Map<Integer, String> result = new HashMap<>();
        for (Field field : java.sql.Types.class.getFields()) {
            try {
                String name = field.getName();
                Integer value = (Integer) field.get(null);
                result.put(value, name);
            }catch (IllegalAccessException ex) {
                // do nothing
            }
        }
        return result;
    }
}

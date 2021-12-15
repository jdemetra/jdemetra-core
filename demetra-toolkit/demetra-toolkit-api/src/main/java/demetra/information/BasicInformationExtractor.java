/*
 * Copyright 2021 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.information;

import nbbrd.design.Development;
import demetra.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * New implementation for JD3
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Release)
public interface BasicInformationExtractor<S> {

    public static final char SEP = '.';
    public static final String STRSEP = new String(new char[]{SEP});
    
    public static boolean isDummy(String s){
        return s == null || s.length() == 0;
    }

    public static String concatenate(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                boolean first = true;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    if (! isDummy(s[i])) {
                        if (!first) {
                            builder.append(SEP);
                        } else {
                            first = false;
                        }
                        builder.append(s[i]);
                    }
                }
                return builder.toString();
        }
    }
    
    public static String[] prefix(@NonNull String[] str, @NonNull String prefix){
        if (prefix.isEmpty())
            return str;
        String[] rslt=new String[str.length];
        for (int i=0; i<rslt.length; ++i){
            StringBuilder builder=new StringBuilder();
            builder.append(prefix).append(SEP).append(str[i]);
            rslt[i]=builder.toString();
        }
        return rslt;
    }

    void fillDictionary(String prefix, Map<String, Class> dic, boolean compact);

    boolean contains(String id);

    <T> T getData(S source, String id, Class<T> tclass);

    default <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> map){
        Map<String, Class> dic=new LinkedHashMap<>();
        fillDictionary(null, dic, false);
        dic.forEach((key, cl)->{
            if (tclass.isAssignableFrom(cl) && wc.match(key)){
                T obj=getData(source, key, tclass);
                if (obj != null){
                    map.put(key, obj);
                }
            }
        });
    }

    static <S, Q> BasicInformationExtractor<S> extractor(final String name, final Class<Q> targetClass,
            final Function<S, Q> fn) {
        return new BasicInformationExtractors.AtomicExtractor<>(name, targetClass, fn);
    }

    static <S, Q> BasicInformationExtractor<S> delegate(final String name, final Class<Q> target, final Function<S, Q> fn) {
        return new BasicInformationExtractors.ExtractorDelegate<>(name, target, fn);
    }

    /**
     * See array for details
     * 
     * @param <S>
     * @param <Q>
     * @param name
     * @param start
     * @param end
     * @param target
     * @param fn
     * @return 
     */
    static <S, Q> BasicInformationExtractor<S> delegateArray(final String name, final int start, final int end, final Class<Q> target, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractorDelegate<>(name, start, end, target, fn);
    }

    /**
     * Extract indexed items. The starting and ending parameters are used only
     * for:
     * - generating full dictionary
     * - searching with wild cards
     * 
     * However, values outside the given bounds can be used at run time to request 
     * additional information. So, the extraction functions must check themselves 
     * the given indexes and return null for unexpected values.
     * 
     * @param <S>
     * @param <Q>
     * @param name
     * @param start Starting index (included)
     * @param end Ending index (excluded)
     * @param targetClass
     * @param fn
     * @return 
     */
    static <S, Q> BasicInformationExtractor<S> array(final String name, final int start, final int end,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractor<>(name, start, end, targetClass, fn);
    }

    /**
     * Extract a parametric item. The default value is only used for:
     * - generating full dictionary
     * - searching with wild cards
     * 
     * However, other values can be used at run time to request 
     * other information. So, the extraction functions must check themselves 
     * the given indexes and return null for unexpected values.
     * @param <S>
     * @param <Q>
     * @param name
     * @param defparam
     * @param targetClass
     * @param fn
     * @return 
     */
    static <S, Q> BasicInformationExtractor<S> array(final String name, final int defparam,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        return new BasicInformationExtractors.ArrayExtractor<>(name, defparam, defparam, targetClass, fn);
    }

}

/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.information;

import ec.tstoolkit.design.ThreadSafe;
import ec.tstoolkit.utilities.WildCards;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * New implementation for JD3
 * @param <S>
 * @since 2.2.0
 * @author Jean Palate
 */
@ThreadSafe
public class InformationMapping<S> implements InformationExtractor<S> {

    private final LinkedHashMap<String, InformationExtractor<S>> map = new LinkedHashMap<>();
    private final Class<S> sourceClass;

    public InformationMapping(Class<S> sourceClass) {
        this.sourceClass = sourceClass;
    }

    public static void updateAll(ClassLoader loader) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        ServiceLoader<InformationMappingExtension> services = ServiceLoader.load(InformationMappingExtension.class, loader);
        HashSet<Class> set = new HashSet<>();
        for (InformationMappingExtension extension : services) {
            set.add(extension.getSourceClass());
        }
        for (Class sclass : set) {
            update(sclass, loader);
        }
    }

    public static boolean update(Class sourceClass, ClassLoader loader) {
        try {
            Method method = sourceClass.getMethod("getMapping");
            if (method == null) {
                return false;
            }
            InformationMapping rslt = (InformationMapping) method.invoke(null);
            if (!rslt.sourceClass.equals(sourceClass)) {
                return false;
            }
            rslt.update(loader);
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    public void update() {
        update(null);
    }

    public void update(ClassLoader loader) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        ServiceLoader<InformationMappingExtension> services = ServiceLoader.load(InformationMappingExtension.class, loader);
        for (InformationMappingExtension extension : services) {
            if (extension.getSourceClass().equals(sourceClass)) {
                extension.updateExtractors(this);
            }
        }
    }

    public <Q> void set(final String name, final Class<Q> targetClass,
            final Function<S, Q> fn) {
        map.put(name, InformationExtractor.extractor(name, targetClass, fn));
    }

    public <Q> void delegate(final String name, final InformationMapping<Q> mapping, final Function<S, Q> fn) {
        map.put(name, InformationExtractor.delegate(name, mapping, fn));
    }

    public <Q> void delegateArray(final String name, final int start, final int end, 
            final InformationMapping<Q> mapping, final BiFunction<S, Integer, Q> fn) {
        map.put(name, InformationExtractor.delegateArray(name, start, end, mapping, fn));
    }

    public <Q> void setArray(final String name, final int start, final int end,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        map.put(name, InformationExtractor.array(name, start, end, targetClass, fn));
    }

    public <Q> void setArray(final String name, final int defparam,
            final Class<Q> targetClass, final BiFunction<S, Integer, Q> fn) {
        map.put(name, InformationExtractor.array(name, defparam, targetClass, fn));
    }

    @Override
    public void fillDictionary(String prefix, Map<String, Class> dic, boolean compact) {
        synchronized (this) {
            map.forEach((key, extractor) -> extractor.fillDictionary(prefix, dic, compact));
        }
    }

    private InformationExtractor<S> search(String id) {
        // fast search
        synchronized (this) {
            InformationExtractor<S> extractor = map.get(id);
            if (extractor != null) {
                return extractor;
            } else {
                // slow search
                Optional<Entry<String, InformationExtractor<S>>> findFirst = map.entrySet().stream().filter(entry -> entry.getValue().contains(id)).findFirst();
                if (findFirst.isPresent()) {
                    return findFirst.get().getValue();
                } else {
                    return null;
                }
            }
        }
    }

    @Override
    public boolean contains(String id) {
        InformationExtractor<S> extractor = search(id);
        return extractor == null ? false : extractor.contains(id);
    }

    @Override
    public <T> T getData(S source, String id, Class<T> tclass) {
        InformationExtractor<S> extractor = search(id);
        return extractor == null ? null : extractor.getData(source, id, tclass);
    }

    @Override
    public <T> void searchAll(S source, WildCards wc, Class<T> tclass, Map<String, T> all) {
        map.forEach((name, extractor) -> extractor.searchAll(source, wc, tclass, all));
    }
}

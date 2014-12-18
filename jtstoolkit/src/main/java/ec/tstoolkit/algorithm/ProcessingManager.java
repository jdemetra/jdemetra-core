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
package ec.tstoolkit.algorithm;

import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public abstract class ProcessingManager {

    private static ProcessingManager instance_;

    public static synchronized ProcessingManager getInstance() {
        if (instance_ == null) {
            instance_ = new DefaultProcessingManager();
        }
        return instance_;
    }

    public abstract <S extends IProcSpecification, I, R extends IProcResults> void register(IProcessingFactory<S, I, R> factory, Class<S> sclass);

    public abstract List<IProcessingFactory<?, ?, ?>> processors();

    public abstract IProcessing<?, ?> createProcessor(InformationSet info, ProcessingContext context);

    public abstract IProcSpecification createSpecification(InformationSet info);

    public abstract <S extends IProcSpecification> IProcessing<?, ?> createProcessor(S spec, ProcessingContext context);

    public abstract List<Class<? extends IProcSpecification>> specifications(IProcessingFactory factory);
    
    static class DefaultProcessingManager extends ProcessingManager {

        @Override
        public <S extends IProcSpecification, I, R extends IProcResults> void register(IProcessingFactory<S, I, R> factory, Class<S> sclass) {
            items_.add(new Item<>(factory, sclass));
        }

        @Override
        public IProcessing<?, ?> createProcessor(InformationSet info, ProcessingContext context) {

            AlgorithmDescriptor desc = info.get(IProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
            if (desc == null) {
                return null;
            }
            for (Item<? extends IProcSpecification> item : items_) {
                if (item.factory.getInformation().isCompatible(desc)) {
                    try {
                        IProcSpecification spec = item.sclass.newInstance();
                        if (!spec.read(info)) {
                            return null;
                        }
                        return item.factory.generateProcessing(spec, context);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public <S extends IProcSpecification> IProcessing<?, ?> createProcessor(S spec, ProcessingContext context) {
            for (Item<? extends IProcSpecification> item : items_) {
                if (item.factory.canHandle(spec)) {
                    return item.factory.generateProcessing(spec, context);
                }
            }
            return null;
        }

        @Override
        public IProcSpecification createSpecification(InformationSet info) {
            AlgorithmDescriptor desc = info.get(IProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
            if (desc == null) {
                return null;
            }
            for (Item<? extends IProcSpecification> item : items_) {
                if (item.factory.getInformation().isCompatible(desc)) {
                    try {
                        IProcSpecification spec = item.sclass.newInstance();
                        if (!spec.read(info)) {
                            return null;
                        }
                        return spec;
                    } catch (InstantiationException | IllegalAccessException ex) {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public List<IProcessingFactory<?, ?, ?>> processors() {
            ArrayList<IProcessingFactory<?, ?, ?>> p = new ArrayList<>();
            for (Item item : items_) {
                p.add(item.factory);
            }
            return p;
        }

        @Override
        public List<Class<? extends IProcSpecification>> specifications(IProcessingFactory factory) {
             ArrayList<Class<? extends IProcSpecification>> specs = new ArrayList<>();
             for (Item<? extends IProcSpecification> cur : items_){
                 if (cur.factory == factory)
                     specs.add(cur.sclass);
             }
             return specs;
         }

        static class Item<S extends IProcSpecification> {

            Item(IProcessingFactory<S, ?, ?> factory, Class<S> sclass) {
                this.factory = factory;
                this.sclass = sclass;
            }
            final IProcessingFactory factory;
            final Class<S> sclass;
        }
        
        private ArrayList<Item<? extends IProcSpecification>> items_ = new ArrayList<>();
    }
}

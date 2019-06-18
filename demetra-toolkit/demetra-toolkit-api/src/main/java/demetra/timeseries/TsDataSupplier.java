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
package demetra.timeseries;

import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
@FunctionalInterface
public interface TsDataSupplier extends Supplier<TsData>{
    public static TsDataSupplier of(@NonNull TsData s){
        return ()->s;
    }
    
    public static TsDataSupplier ofDynamic(@NonNull TsData initial, @NonNull Supplier<TsData> supplier){
        return () -> {
            TsData ndata = supplier.get();
            return ndata == null ? initial : ndata;
        };
    }
    
}

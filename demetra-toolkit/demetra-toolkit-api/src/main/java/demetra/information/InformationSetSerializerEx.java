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

package demetra.information;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public interface InformationSetSerializerEx<T, C> extends InformationSetSerializer<T>{
    @Override
    default InformationSet write(T object, boolean verbose){
        return write(object, null, verbose);
    }
    
    @Override
    default T read(InformationSet info){
        return read(info, null);
    }

    InformationSet write(T object, C context, boolean verbose);
    T read(InformationSet info, C context);
}

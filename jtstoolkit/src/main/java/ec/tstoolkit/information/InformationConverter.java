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

/**
 *
 * @author Jean Palate
 * @param <T>
 */
public interface InformationConverter<T> {
    /**
     * Transform an information set in the corresponding type object
     * @param info
     * @return 
     */
    T decode(InformationSet info);
    /**
     * Extract information from an object. 
     * The returned information should contain an identification of the object,
     * so that the opposite transformation could be performed.
     * @param t
     * @return 
     */
    InformationSet encode(T t, boolean verbose);
    /**
     * The type that the converter can handle.
     * @return 
     */
    Class<T> getInformationType();
    /**
     * A unique description of the type handled by the converter.
     * The description should be set in the "contents" field of the information set
     * @return 
     */
    String getTypeDescription();
}

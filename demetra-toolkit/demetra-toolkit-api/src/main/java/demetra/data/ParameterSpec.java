/*
 * Copyright 2020 National Bank of Belgium.
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
package demetra.data;

/**
 * 
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class ParameterSpec {
    
    /**
     * Value of the parameter
     */
    private double value;
    /**
     * Type of the parameter. Should be undefined, initial or fixed 
     */
    private ParameterType type;
    
    public boolean isFixed(){
        return type == ParameterType.Fixed;
    }
    
    public static ParameterSpec undefined(){
        return UNDEFINED ;
    }
    
    public static ParameterSpec fixed(double value){
        return new ParameterSpec(value, ParameterType.Fixed);
    }
    
    public static ParameterSpec initial(double value){
        return new ParameterSpec(value, ParameterType.Initial);
    }
    
    public static ParameterSpec[] make(int n){
        ParameterSpec[] all=new ParameterSpec[n];
        for (int i=0; i<n; ++i)
            all[i]=UNDEFINED;
        return all;
    }

    private static final ParameterSpec UNDEFINED =new ParameterSpec(0,ParameterType.Undefined);
}

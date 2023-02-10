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
package demetra.processing;

import demetra.information.Explorable;
import java.util.List;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class GenericResults implements ProcResults {
    
    @lombok.experimental.Delegate
    GenericOutput output;
    
    ProcessingLog log;
    
    private static final GenericResults NOTIMPL=new GenericResults(GenericOutput.builder().build(), ProcessingLog.notImplemented());
    
    public static GenericResults notImplemented(){return NOTIMPL;}

    public static GenericResults of(Explorable explorable, List<String> items, ProcessingLog log) {

        return new GenericResults(GenericOutput.of(explorable, items), log);
    }

}

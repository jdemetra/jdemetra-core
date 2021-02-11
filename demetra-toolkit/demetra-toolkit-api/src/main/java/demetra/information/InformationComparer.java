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

/**
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class InformationComparer<S> implements java.util.Comparator<Information<S>>{

    @Override
    public int compare(Information<S> o1, Information<S> o2) {
        return Long.compare(o1.getIndex(), o2.getIndex());
    }
}

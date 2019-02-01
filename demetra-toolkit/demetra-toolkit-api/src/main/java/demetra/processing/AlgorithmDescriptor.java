/*
 * Copyright 2019 National Bank of Belgium.
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

import demetra.design.Development;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class AlgorithmDescriptor implements Comparable<AlgorithmDescriptor> {

    String family;
    String name;
    String version;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (family != null && family.length() > 0) {
            builder.append(family).append('/');
        }
        builder.append(name);
        if (version != null && version.length() > 0) {
            builder.append(" (").append(version).append(')');
        }
        return builder.toString();
    }

    public boolean match(String family, String name, String version) {
        if (family != null && !family.equalsIgnoreCase(this.family)) {
            return false;
        }
        if (!name.equalsIgnoreCase(this.name)) {
            return false;
        }
        return !(version != null && !version.equalsIgnoreCase(this.version));
    }

    public boolean isCompatible(AlgorithmDescriptor information) {
        return match(information.family, information.name, information.version);
    }

    @Override
    public int compareTo(AlgorithmDescriptor o) {
        int cmp = family.compareToIgnoreCase(o.family);
        if (cmp != 0) {
            return cmp;
            
        }
        cmp = name.compareToIgnoreCase(o.name);
        if (cmp != 0) {
            return cmp;

        }
        if (version == null) {
            if (o.version == null) {
                return 0;
                
            } else {
                return -1;

            }
        } else {
            if (o.version == null) {
                return 1;
                
            } else {
                return version.compareToIgnoreCase(o.version);
                
            }
        }
    }

}

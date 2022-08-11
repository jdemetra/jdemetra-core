/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.tsprovider.cube;

import nbbrd.design.LombokWorkaround;
import org.checkerframework.checker.index.qual.NonNegative;

import java.time.Duration;


/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class BulkCube {

    public static final BulkCube NONE = BulkCube.builder().build();

    @lombok.NonNull
    private Duration ttl;

    @NonNegative
    private int depth;

    @LombokWorkaround
    public static BulkCube.Builder builder() {
        return new Builder().ttl(Duration.ZERO).depth(0);
    }

    public boolean isCacheEnabled() {
        return getDepth() > 0 && !getTtl().isZero();
    }
}

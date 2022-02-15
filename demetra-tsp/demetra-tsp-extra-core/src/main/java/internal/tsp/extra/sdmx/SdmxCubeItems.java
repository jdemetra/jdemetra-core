/*
 * Copyright 2017 National Bank of Belgium
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
package internal.tsp.extra.sdmx;

import demetra.tsp.extra.sdmx.file.SdmxFileBean;
import demetra.tsprovider.HasFilePaths;
import demetra.tsprovider.cube.CubeId;
import sdmxdl.DataStructure;
import sdmxdl.Dimension;
import sdmxdl.file.SdmxFileSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxCubeItems {

    public static SdmxFileSource resolveFileSet(HasFilePaths paths, SdmxFileBean bean) throws FileNotFoundException {
        SdmxFileSource.Builder result = SdmxFileSource.builder().data(paths.resolveFilePath(bean.getFile()));
        File structure = bean.getStructureFile();
        if (structure != null && !structure.toString().isEmpty()) {
            result.structure(paths.resolveFilePath(structure));
        }
        String dialect = bean.getDialect();
        if (dialect != null && !dialect.isEmpty()) {
            result.dialect(dialect);
        }
        return result.build();
    }
}

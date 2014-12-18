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

package ec.tss.tsproviders;

import java.io.File;
import java.io.FileFilter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a specialized provider whose source is a file.
 *
 * @author Philippe Charles
 */
public interface IFileLoader extends IDataSourceLoader, FileFilter {

    @Override
    IFileBean newBean();

    @Override
    IFileBean decodeBean(DataSource dataSource) throws IllegalArgumentException;

    @Nonnull
    String getFileDescription();

    void setPaths(@Nullable File[] paths);

    @Nonnull
    File[] getPaths();
}

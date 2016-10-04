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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines a provider whose content can be changed programmatically.<br>
 * DataSource & DataSet being read-only and generic, a provider will handle its
 * specific configuration by using beans.<p>
 * Typical use is:<br>
 * <code>
 * Object bean = loader.newBean();<br>
 * // use it through reflection<br>
 * DataSource dataSource = loader.encodeBean(bean);<br>
 * loader.open(dataSource);
 * </code>
 *
 * @author Philippe Charles
 */
@ThreadSafe
public interface IDataSourceLoader extends IDataSourceProvider, HasDataSourceBean {

    /**
     * Adds a new DataSource to the provider.
     *
     * @param dataSource
     * @return true if the DataSource has been added to the provider, false
     * otherwise.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     */
    boolean open(@Nonnull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Removes a DataSource from the provider.
     *
     * @param dataSource
     * @return true if the DataSource has been removed from the provider, false
     * otherwise.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     */
    boolean close(@Nonnull DataSource dataSource) throws IllegalArgumentException;

    /**
     * Removes all the DataSources from this provider.
     */
    default void closeAll() {
        getDataSources().forEach(this::close);
    }
}

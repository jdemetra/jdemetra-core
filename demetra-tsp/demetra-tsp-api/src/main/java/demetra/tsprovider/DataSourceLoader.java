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
package demetra.tsprovider;

import nbbrd.design.ThreadSafe;


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
 * @param <BEAN> bean type
 * @since 1.0.0
 */
@ThreadSafe
public interface DataSourceLoader<BEAN> extends DataSourceProvider, HasDataSourceMutableList, HasDataSourceBean<BEAN> {

}

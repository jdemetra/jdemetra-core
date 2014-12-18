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
package ec.tss.tsproviders.jdbc.dsm.datasource;

import ec.tss.tsproviders.jdbc.dsm.datasource.interfaces.IManagedDataSource;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author Demortier Jeremy
 */
@Deprecated
public class DefaultManagedDataSource implements IManagedDataSource {

  protected final HashMap<String, String> m_properties;
  protected final String m_srcType;
  protected String m_name;

  public DefaultManagedDataSource(final String sourceType) {
    m_srcType = sourceType;
    m_properties = new HashMap<>();
  }

  public DefaultManagedDataSource(final String sourceType, final String name) {
    this(sourceType);
    m_name = name;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public void setName(final String name) {
    m_name = name;
  }

  @Override
  public String getProperty(final String propertyName) {
    return m_properties.get(propertyName);
  }

  @Override
  public void setProperty(final String propertyName, final String propertyValue) {
    m_properties.put(propertyName, propertyValue);
  }

  @Override
  public String getSourceType() {
    return m_srcType;
  }

  @Override
  public LinkedList<String> listProperties() {
    return new LinkedList<>(m_properties.keySet());
  }
}

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
package ec.tss.tsproviders.jdbc.dsm.datasource.interfaces;

import ec.tss.tsproviders.jdbc.dsm.datasource.DataSourceType;
import java.util.LinkedList;

/**
 * Interface to be implemented for each database type that should be monitored
 * by the DataSourceManager.
 * @author Demortier Jeremy
 */
@Deprecated
public interface IManagedDataSource {
  /**
   * Gets the name of the database in the manager.
   * @return
   */
  String getName();

  /**
   * Sets the name of the database that should identify it in the manager.
   * @param name
   */
  void setName(final String name);

  /**
   * Gets the database type.
   * @return
   * @see DataSourceType
   */
  String getSourceType();

  /**
   * Gets the list of the different elements used by this database type to be able
   * to create a connection.
   * @return
   */
  LinkedList<String> listProperties();

  /**
   * Gets the value of a property.
   * @param propertyName
   * @return
   */
  String getProperty(final String propertyName);

  /**
   * Sets the value of a property.
   * @param propertyName
   * @param propertyValue
   */
  void setProperty(final String propertyName, final String propertyValue);
}

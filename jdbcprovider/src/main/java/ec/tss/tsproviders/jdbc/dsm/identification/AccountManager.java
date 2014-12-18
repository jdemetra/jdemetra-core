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
package ec.tss.tsproviders.jdbc.dsm.identification;

import java.util.HashMap;
import java.util.List;

/**
 * Singleton; handles the security information needed for users to identify themselves
 * when connecting to a database.
 * @author Demortier Jeremy
 */
@Deprecated
public enum AccountManager {

  INSTANCE;
  private final HashMap<String, String> m_resources;
  private IContentManager m_content;

  private AccountManager() {
    m_resources = new HashMap<>();
  }

  /**
   * Sets the content manager. Without it, the AccountManager is unable to retrieve
   * previously saved information or save new ones.
   * @param init Implementation of IContentManager; it will provides the data.
   */
  public void setManager(final IContentManager init) {
    m_resources.clear();
    m_content = init;

    for (String s : m_content.getContent()) {
      String[] parts = s.split("/");
      if (parts.length == 2) {
        m_resources.put(parts[0], parts[1]);
      }
    }
  }

  public void save() {
    if (m_content != null) {
      m_content.saveContent(m_resources);
    }
  }

  private String assemble(String a, String b) {
    return a + ":" + b;
  }

  public void addAccount(final String provider, final String dbName, final Account account) {
    if (provider == null || dbName == null || account == null) {
      return;
    }

    m_resources.put(assemble(provider, dbName), assemble(account.getLogin(), account.getPassword()));
  }

  public void removeAccount(final String provider, final String dbName) {
    if (provider == null || dbName == null) {
      return;
    }

    m_resources.remove(assemble(provider, dbName));
  }

  /**
   * Removes the data that isn't valid because of a change of encryption key
   * @param defect List of keys to remove from the hashmap
   */
  private void cleanUp(List<String> defect) {
    for (String s : defect) {
      m_resources.remove(s);
    }
  }

  public Account getAccount(final String provider, final String dbName) {
    if (provider == null || dbName == null) {
      return null;
    }

    String s = m_resources.get(assemble(provider, dbName));
    if (s == null) {
      return null;
    }

    String[] parts = s.split(":");
    if (parts.length == 2) {
      return new Account(parts[0], parts[1]);
    }
    return null;
  }
}

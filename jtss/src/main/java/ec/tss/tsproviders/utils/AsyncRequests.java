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
package ec.tss.tsproviders.utils;

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author Philippe Charles
 */
public class AsyncRequests {

    private final Deque<TsInformation> m_srequests = new LinkedList<>();
    private final Deque<TsCollectionInformation> m_crequests = new LinkedList<>();

    public void clear() {
        synchronized (m_crequests) {
            m_crequests.clear();
        }
        synchronized (m_srequests) {
            m_srequests.clear();
        }
    }

    public boolean isEmpty() {
        synchronized (m_crequests) {
            if (!m_crequests.isEmpty()) {
                return false;
            }
        }
        synchronized (m_srequests) {
            if (!m_srequests.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void addTsCollection(TsMoniker moniker, TsInformationType type) {
        synchronized (m_crequests) {
            for (TsCollectionInformation o : m_crequests) {
                if (o.moniker.equals(moniker)) {
                    o.type = type.union(o.type);
                    return;
                }
            }
            m_crequests.addLast(new TsCollectionInformation(moniker, type));
        }
    }

    public boolean removeTsCollection(TsMoniker moniker, TsInformationType type) {
        synchronized (m_crequests) {
            for (TsCollectionInformation o : m_crequests) {
                if (o.moniker.equals(moniker)) {
                    return type.encompass(o.type) ? m_crequests.remove(o) : false;
                }
            }
            return false;
        }
    }

    public TsCollectionInformation nextTsCollection() {
        synchronized (m_crequests) {
            return m_crequests.pollFirst();
        }
    }

    public void addTs(TsMoniker moniker, TsInformationType type) {
        synchronized (m_srequests) {
            for (TsInformation o : m_srequests) {
                if (o.moniker.equals(moniker)) {
                    o.type = type.union(o.type);
                    return;
                }
            }
            m_srequests.addLast(new TsInformation(null, moniker, type));
        }
    }

    /**
     * Requests whose information is encompassed by the given type are removed.
     *
     * @param moniker
     * @param type
     * @return
     */
    public boolean removeTs(TsMoniker moniker, TsInformationType type) {
        synchronized (m_srequests) {
            for (TsInformation o : m_srequests) {
                if (o.moniker.equals(moniker)) {
                    return type.encompass(o.type) ? m_srequests.remove(o) : false;
                }
            }
            return false;
        }
    }

    public TsInformation nextTs() {
        synchronized (m_srequests) {
            return m_srequests.pollFirst();
        }
    }
}

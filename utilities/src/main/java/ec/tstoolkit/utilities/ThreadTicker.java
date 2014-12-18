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

package ec.tstoolkit.utilities;

import com.google.common.base.Ticker;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 *
 * @author Philippe Charles
 */
public final class ThreadTicker extends Ticker {

    private final static ThreadTicker INSTANCE = new ThreadTicker();
    private final static ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();

    public static Ticker getInstance() {
        return mxbean.isCurrentThreadCpuTimeSupported() ? INSTANCE : systemTicker();
    }

    private ThreadTicker() {
        // singleton
    }

    @Override
    public long read() {
        return mxbean.getCurrentThreadCpuTime();
    }
}

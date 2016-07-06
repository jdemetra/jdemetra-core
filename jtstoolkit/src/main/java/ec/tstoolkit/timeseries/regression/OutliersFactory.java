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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class OutliersFactory {

    /**
     *
     */
    public static final OutliersFactory defaultFactory;

    static {
	defaultFactory = new OutliersFactory(true);
    }

    private final ArrayList<IOutlierFactory> factories = new ArrayList<>();

    /**
     * 
     * @param def
     */
    public OutliersFactory(boolean def)
    {
	if (def) {
	    factories.add(new AdditiveOutlierFactory());
	    factories.add(new LevelShiftFactory());
	    factories.add(new TransitoryChangeFactory());
	    factories.add(new SeasonalOutlierFactory());

	}
    }

    /**
     * 
     * @return
     */
    public List<IOutlierFactory> getFactories()
    {
	return factories;
    }

    /**
     * 
     * @param def
     * @return
     */
    public IOutlierVariable make(OutlierDefinition def)
    {
	for (IOutlierFactory fac : factories)
	    if (fac.getOutlierType() == def.getType()){
		IOutlierVariable o=fac.create(def.getPosition());
                return o;
            }
	return null;
    }

    /**
     * 
     * @param fac
     * @return
     */
    public boolean register(IOutlierFactory fac)
    {
	for (IOutlierFactory f : factories)
	    if (fac.getOutlierType() == f.getOutlierType())
		return false;
	factories.add(fac);
	return true;
    }

    /**
     * 
     * @param code
     * @return
     */
    public boolean unregister(OutlierType code)
    {
	for (IOutlierFactory f : factories)
	    if (code == f.getOutlierType()) {
		factories.remove(f);
		return true;
	    }
	return false;
    }
    
    public IOutlierFactory getFactory(OutlierType code)
    {
	for (IOutlierFactory f : factories)
	    if (code == f.getOutlierType()) {
		return f;
	    }
	return null;
    }
}

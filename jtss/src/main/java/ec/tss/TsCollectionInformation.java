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

package ec.tss;

import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import java.util.ArrayList;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class TsCollectionInformation {

   /**
     * IN
     */
    public TsMoniker moniker;
    
    /**
     * OUT
     */
    public String name;
    /**
     *
     */
    public MetaData metaData;

    /**
     *
     */
    public ArrayList<TsInformation> items = new ArrayList<>();

    /**
     *
     */
    public TsInformationType type;

    /**
     * 
     */
    public TsCollectionInformation()
    {
	this.moniker = new TsMoniker();
	type = TsInformationType.UserDefined;
    }

    /**
     * 
     * @param ts
     * @param type
     */
    public TsCollectionInformation(TsCollection ts, TsInformationType type) {
	this.moniker = ts.getMoniker();
	this.type = type;
        this.name=ts.getName();
	ts.load(type);
	for (Ts item : ts.toArray())
	    this.items.add(new TsInformation(item, type));
	if (hasMetaData())
	    metaData = ts.getMetaData();
    }

    /**
     * 
     * @param moniker
     * @param type
     */
    public TsCollectionInformation(TsMoniker moniker, TsInformationType type)
    {
	this.moniker = moniker;
	this.type = type;
    }

    /**
     * 
     * @return
     */
    public boolean hasData() {
	return type == TsInformationType.All || type == TsInformationType.Data;
    }

    /**
     * 
     * @return
     */
    public boolean hasDefinition() {
	return type == TsInformationType.All
		|| type == TsInformationType.Definition
		|| type == TsInformationType.BaseInformation;
    }

    /**
     * 
     * @return
     */
    public boolean hasMetaData() {
	return type == TsInformationType.All
		|| type == TsInformationType.MetaData
		|| type == TsInformationType.BaseInformation;
    }
}

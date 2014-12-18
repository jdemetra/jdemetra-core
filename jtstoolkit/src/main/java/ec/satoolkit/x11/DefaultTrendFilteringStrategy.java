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


package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DefaultTrendFilteringStrategy implements IFiltering {
    

    SymmetricFilter filter;

    IEndPointsProcessor endPointsProcessor;

    private String description_;

    public void setDescription(String desc){
        description_=desc;
    }

    @Override
    public String getDescription(){
        return description_==null ? "" : description_;
    }
    /**
     *
     * @param filter
     * @param endPoints
     */
    public DefaultTrendFilteringStrategy(SymmetricFilter filter,
	    IEndPointsProcessor endPoints) {
	this.filter = filter;
	this.endPointsProcessor = endPoints;
    }

   public DefaultTrendFilteringStrategy(SymmetricFilter filter,
	    IEndPointsProcessor endPoints, String desc) {
	this.filter = filter;
	this.endPointsProcessor = endPoints;
        this.description_=desc;
    }

   private TsData compositeProcess(TsData s, TsDomain rdomain) {
	int len = rdomain.getLength();
	DataBlock out = new DataBlock(len);
	int nf = filter.getUpperBound();
	DataBlock in = TsDataBlock.select(s, rdomain).data;
	filter.filter(in, out.drop(nf, nf));

	// complete the missing items...
	this.endPointsProcessor.process(in, out);
	return new TsData(rdomain.getStart(), out.getData(), false);
    }

    /**
     *
     * @return
     */
    @Override
    public SymmetricFilter getCentralFilter() {
	return filter;
    }

    /**
     *
     * @param s
     * @param domain
     * @return
     */
    @Override
    public TsData process(TsData s, TsDomain domain) {
	TsDomain rdomain = domain == null ? s.getDomain() : domain;
	if (this.endPointsProcessor == null)
	    return simpleProcess(s, rdomain);
	else
	    return compositeProcess(s, rdomain);
    }

    private TsData simpleProcess(TsData s, TsDomain rdomain) {
	int n = filter.getLength();
	DataBlock out = new DataBlock(rdomain.getLength() - n + 1);
	filter.filter(TsDataBlock.select(s, rdomain).data, out);
	return new TsData(rdomain.getStart().minus(filter.getLowerBound()), out
		.getData(), false);
    }
}

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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsPeriodSelector;

/**
 * Iterator of a time series following the different periods.
 * In the case of a monthly series, the iterator will return successively
 * (in TsDataBlocks) all the Januaries, Februaries...As usual, the use of the 
 * iterator should contain code like
 * while (iterator.hasMoreElements()){
 *    TsDataBlock data=iterator.nextElement();
 *    // do something
 * }
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PeriodIterator implements java.util.Enumeration<TsDataBlock> {
    /**
     * Returns an iterator considering only complete years
     * @param series The analyzed series
     * @return An iterator with the different periods. 
     */
    public static PeriodIterator fullYears(TsData series)
    {
	TsDomain domain = series.getDomain();
	int ifreq = domain.getFrequency().intValue();
	int nbeg = series.getStart().getPosition();
	int nend = series.getEnd().getPosition();
	domain = domain.drop(nbeg == 0 ? 0 : ifreq - nbeg, nend);
	return new PeriodIterator(series, domain);
    }

    private final TsDataBlock data_;
    private TsDataBlock cur_;

    /**
     * Creates a new period iterator on the complete series. The data blocks 
     * returned by the iterator may have different lengths.
     * @param series The time series.
     */
    public PeriodIterator(TsData series) {
	if (!series.isEmpty())
	    data_ = TsDataBlock.all(series);
	else
	    data_ = null;
        
    }

    /**
     * Creates a partial iterator, limited on a specified domain.
     * @param series The considered time series.
     * @param domain The domain of the iterator. Must be compatible with the time series
     */
    public PeriodIterator(TsData series, TsDomain domain)
    {
	data_ = TsDataBlock.select(series, domain);
    }

    /**
     * Creates a partial iterator, limited by a period selection
     * @param series The considered time series
     * @param selector The selector.
     */
    public PeriodIterator(TsData series, TsPeriodSelector selector)
    {
	data_ = TsDataBlock.select(series, selector);
    }

    @Override
    public boolean hasMoreElements() {
	if (data_ == null)
	    return false;
	return cur_ == null
		|| cur_.start.getPosition() < data_.start.getFrequency()
			.intValue() - 1;
    }

    @Override
    public TsDataBlock nextElement() {
	if (cur_ == null)
	    initialize();
	else
	    cur_.move(1);
	int nbeg = 0, nend = 0;
	// we must extend if the beginning is >= ifreq
	int ifreq = data_.start.getFrequency().intValue();
        int cbeg=cur_.data.getStartPosition()-ifreq;
	if (cbeg >= data_.data.getStartPosition() && cbeg < data_.data.getEndPosition())
	    nbeg = 1;
	// or if the end is < ndata - freq.
	if (cur_.data.getEndPosition() < data_.data.getEndPosition())
	    nend = 1;
	if (nbeg > 0 || nend > 0)
	    return cur_.extend(nbeg, nend);
	else
	    return cur_;
    }

    /**
     * Restarts the iterator.
     */
    public void reset() {
	cur_ = null;
    }
    
    private void initialize() {
	int freq = data_.start.getFrequency().intValue();
	int istart = data_.start.getPosition();
	if (istart != 0)
	    istart = freq - istart;
	int nyears = (data_.data.getLength() - istart) / freq;
	cur_ = TsDataBlock.select(data_, istart, nyears, freq);
    }

}

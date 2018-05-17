/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.datatypes;

import java.util.function.IntSupplier;

/**
 *
 * @author Jean Palate
 */
public enum TsInformationType implements IntSupplier {

    /**
     * Information is not provided by a specific provider
     */
    UserDefined(-1),
    /**
     * No information has been loaded
     */
    None(0),
    /**
     * For collection only: the list of the time series has been changed
     */
    Definition(1),
    /**
     * The metadata of the object (not of the sub-objects) has been loaded
     */
    MetaData(2),
    /**
     * Corresponds to Definition + MetaData + MetaData of sub-objects
     */
    BaseInformation(3),
    /**
     * Only data have been loaded
     */
    Data(4),
    /**
     * All information is loaded.
     */
    All(5);
    private final int value;

    TsInformationType(final int value) {
        this.value = value;
    }

    /**
     * Returns the value of this TsInformationType as an int.
     *
     * @return
     */
    @Override
    public int getAsInt() {
        return value;
    }

    public boolean needsData() {
        return this == Data || this == All;
    }

    /**
     * Returns an information type that satisfies the two given information
     * types.
     *
     * @param rtype
     * @return
     */
    public TsInformationType union(TsInformationType rtype) {
        if (this == rtype) {
            return this;
        }
        if (this == All || rtype == All) {
            return All;
        }
        if (this == None) {
            return rtype;
        }
        if (rtype == None) {
            return this;
        }
        if (this == Data && rtype == Definition) {
            return Data;
        }
        if (this == Definition && rtype == Data) {
            return Data;
        }
        // Should not append in a normal use...
        return TsInformationType.All;
    }

    /**
     * Checks that an old (current) information type encompasses the new
     * (requested) one.
     *
     * @param newtype
     * @return True if the old type is larger than the new one
     */
    public boolean encompass(TsInformationType newtype) {
        switch (newtype) {
            case All:
                return this == All;
            case Data:
                return this == Data || this == All;
            case MetaData:
                return this == MetaData || this == All;
            case Definition:
                return this == All || this == Data || this == Definition;
            default:
                return true;
        }
    }
}

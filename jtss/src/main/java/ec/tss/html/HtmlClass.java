/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tss.html;

import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class HtmlClass {

    public static HtmlClass NO_CLASS = new HtmlClass("");

    @Nonnull
    public static HtmlClass of(@Nonnull String name) {
        return name.isEmpty() ? NO_CLASS : new HtmlClass(name);
    }

    private final String content;

    private HtmlClass(String content) {
        this.content = content;
    }

    @Nonnull
    public HtmlClass with(@Nonnull HtmlClass name) {
        return name.isEmpty() ? this : this.isEmpty() ? name : new HtmlClass(content + " " + name.content);
    }

    @Override
    public String toString() {
        return content;
    }

    boolean isEmpty() {
        return content.isEmpty();
    }
}

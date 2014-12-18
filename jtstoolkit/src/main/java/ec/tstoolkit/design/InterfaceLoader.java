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

package ec.tstoolkit.design;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class InterfaceLoader {
    /**
     * 
     * @param <T>
     * @param interfaceType
     * @param className
     * @return
     */
    public static <T> T create(Class<T> interfaceType, String className) {
	if (className == null)
	    return null;
	try {
	    // use the default class loader
	    return create(ClassLoader.getSystemClassLoader(), interfaceType,
		    className);
	} catch (IllegalArgumentException ex) {
	    return null;
	}
    }

    /**
     * 
     * @param <T>
     * @param loader
     * @param interfaceType
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(ClassLoader loader, Class<T> interfaceType,
	    String className) {
	Class<?> tclass = null;
	try {
	    tclass = loader.loadClass(className);

	} catch (ClassNotFoundException ex) {
	    return null;
	}
	// try first for singleton...
	Singleton an = tclass.getAnnotation(Singleton.class);
	if (an != null) {
 	    String instance = an.name();
	    try {
		Field field = tclass.getField(instance);
		Object obj = field.get(null);
		if (obj != null && interfaceType.isInstance(obj))
		    return (T) obj;
	    } catch (Exception e) {
	    }

	    String entry = an.entryPoint();
	    try {
		Method method = tclass.getMethod(entry);
		Object obj = method.invoke(null);
		if (obj != null && interfaceType.isInstance(obj))
		    return (T) obj;
	    } catch (Exception e) {
	    }
	}
	try {
	    // create an instance of this class
	    Constructor<?> c = tclass.getConstructor();
	    Object obj = c.newInstance();
	    if (interfaceType.isInstance(obj))
		return (T) obj;
	    else
		return null;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     *
     * @param <T>
     * @param urlString
     * @param interfaceType
     * @param className
     * @return
     */
    public static <T> T create(String urlString, Class<T> interfaceType,
	    String className) {
	if (className == null)
	    return null;
	if (urlString == null)
	    return create(interfaceType, className);
	try {
	    URL url = new URL(null, urlString);
	    URLClassLoader loader = new URLClassLoader(new URL[] { url });
	    // use the default class loader
	    return create(loader, interfaceType, className);
	} catch (Exception ex) {
	    return null;
	}
    }
}

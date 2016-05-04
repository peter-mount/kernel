/*
 * Copyright 2015 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.kernel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main
{

    public static void main( String[] args )
            throws Exception
    {
        try {
            Class<?> clazz = Class.forName( "onl.area51.kernel.Application", true, Thread.currentThread().getContextClassLoader() );
            Method method = clazz.getDeclaredMethod( "boot", String[].class );
            method.setAccessible( true );
            System.exit( (Integer) method.invoke( null, new Object[]{args} ) );
        }
        catch( InvocationTargetException ex ) {
            Throwable t = ex.getCause();
            throw t instanceof Exception ? (Exception) t : ex;
        }
    }

}

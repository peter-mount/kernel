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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main
{

    /**
     * The directories under our application which to include in the classloader.
     * <ul>
     * <li>cdilib - the CDI implementation</li>
     * <li>sharedlib - the core libraries</li>
     * <li>lib - the application libraries</li>
     * </ul>
     *
     * Classes installed in the following are in the main classloader so we don't include them here.
     * <ul>
     * <li>ext - extension libraries, those that don't fit in either</li>
     * </ul>
     */
    private static final String DIRECTORIES[] = {"sharedlib", "lib"};

    public static void main( String[] args )
            throws Exception
    {
        try {
            createClassLoader();
            boot( args );
        } catch( InvocationTargetException ex ) {
            Throwable t = ex.getCause();
            throw t instanceof Exception ? (Exception) t : ex;
        }
    }

    private static void boot( String[] args )
            throws Exception
    {
        Class<?> clazz = Class.forName( "onl.area51.kernel.Application", true, Thread.currentThread().getContextClassLoader() );
        Method method = clazz.getDeclaredMethod( "boot", String[].class );
        method.setAccessible( true );
        System.exit( (Integer) method.invoke( null, new Object[]{args} ) );
    }

    private static ClassLoader createClassLoader()
    {
        Path path = Paths.get( System.getenv( "KERNEL_LIB" ) );

        Collection<URL> urls = Stream.of( DIRECTORIES )
                // Convert directory name to a Path under path
                .map( path::resolve )
                // flatMap to a stream of files under those directories (or subdirectories)
                .flatMap( p -> {
                    try {
                        return Files.walk( p );
                    } catch( IOException ex ) {
                        // Simply ignore this path, possibly doesn't exist
                        return Stream.empty();
                    }
                } )
                // We only want files ending with .jar
                .filter( p -> Files.isRegularFile( p, LinkOption.NOFOLLOW_LINKS ) )
                .filter( p -> p.getName( p.getNameCount() - 1 ).toString().endsWith( ".jar" ) )
                // Convert to a URL
                .map( p -> {
                    try {
                        return p.toUri().toURL();
                    } catch( IOException ex ) {
                        throw new UncheckedIOException( ex );
                    }
                } )
                // Return them as a list
                .collect( Collectors.toList() );

        if( Boolean.valueOf( System.getenv( "KERNEL_DEBUG" ) ) ) {
            Logger.getGlobal().log( Level.INFO,
                                    () -> urls.stream()
                                    .map( Objects::toString )
                                    .collect( Collectors.joining( "\n", "Jars found:\n", "" ) ) );
        }

        ClassLoader classLoader = new URLClassLoader( urls.toArray( new URL[urls.size()] ), Thread.currentThread().getContextClassLoader() );
        Thread.currentThread().setContextClassLoader( classLoader );
        return classLoader;
    }
}

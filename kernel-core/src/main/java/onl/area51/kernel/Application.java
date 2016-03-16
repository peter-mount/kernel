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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.inject.spi.CDI;

/**
 * Instantiates the CDI container and starts the application.
 *
 * @author peter
 */
public class Application
{

    private static final Logger LOG = Logger.getGlobal();

    private static final Semaphore SEMAPHORE = new Semaphore( 0 );
    private static int returnCode = 0;

    Application()
    {
    }

    static int boot( String[] args )
            throws Exception
    {
        // Record start time
        final LocalDateTime start = LocalDateTime.now();

        // Write the issue file to the log - mainly for debugging
        try( BufferedReader r = new BufferedReader( new InputStreamReader( Application.class.getResourceAsStream( "/META-INF/issue" ) ) ) ) {
            LOG.log( Level.INFO, r.lines().collect( Collectors.joining( "\n" ) ) );
        }

        try( CDI<Object> cdi = CDI.getCDIProvider().initialize() ) {

            // Fire the CommandArguments event enabling tasks to start up
            List<String> commandArguments = Collections.unmodifiableList( Arrays.asList( args ) );
            CommandArguments event = () -> commandArguments;
            cdi.getBeanManager().fireEvent( event );

            // Now wait
            LOG.log( Level.INFO, () -> "Application started in " + Duration.between( start, LocalDateTime.now() ) );
            SEMAPHORE.acquire();
            return returnCode;
        }
    }

    /**
     * Exit the application. Normally an application will never need to use this but for those utilities that do one job and nothing else can use this
     * to exit rather than {@link System#exit(int)} as this method ensures we perform a clean shutdown.
     * <p>
     * It also handles an old Java Bug (not certain if it's fixed) where calling System.exit() outside of the main thread can cause the returnCode not to be
     * passed correctly.
     *
     * @param returnCode
     */
    public static void exit( int returnCode )
    {
        Application.returnCode = returnCode;
        SEMAPHORE.release( 10 );
    }
}

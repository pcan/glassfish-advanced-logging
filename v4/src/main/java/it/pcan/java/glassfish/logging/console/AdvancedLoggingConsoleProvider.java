/*
 * Copyright 2013 Pierantonio Cangianiello.
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
package it.pcan.java.glassfish.logging.console;

import java.net.URL;
import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is used by the kernel to report the presence of Admin Console files.
 * 
 * @author Pierantonio Cangianiello
 */
@Service
public class AdvancedLoggingConsoleProvider implements ConsoleProvider{

    @Override
    public URL getConfiguration() {
        return null;
    }
    
}

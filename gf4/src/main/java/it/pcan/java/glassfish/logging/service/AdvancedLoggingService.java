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
package it.pcan.java.glassfish.logging.service;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import it.pcan.java.glassfish.logging.configuration.control.ConfigurationManager;
import it.pcan.java.glassfish.logging.configuration.entity.AdvancedLoggingConfiguration;
import it.pcan.java.glassfish.logging.configuration.entity.HandlerConfiguration;
import it.pcan.java.glassfish.logging.configuration.entity.LoggerBinding;
import it.pcan.java.glassfish.logging.handler.LogHandlerManager;
import it.pcan.java.glassfish.logging.jsf.handlers.AdvancedLoggingJsfHandlers;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This is the "main" class for the Advanced-Logging module.
 * It runs at server startup, loads configuration and create the Log Handlers and Bindings.
 * 
 * @author Pierantonio Cangianiello
 */
@Service
@RunLevel
public class AdvancedLoggingService implements  PostConstruct {

    private final static Logger logger = Logger.getLogger(AdvancedLoggingService.class.getName());
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    //@Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    @Inject
    private Config currentConfig;
    @Inject
    private ServiceLocator locator;
    @Inject
    private LogHandlerManager logHandlerManager;
    @Inject
    private AdvancedLoggingJsfHandlers advancedLoggingJsfHandlers;
    
    
    @Override
    public void postConstruct() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(AdvancedLoggingService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String currentConfigName = currentConfig.getName();

        System.out.println("\033[34mLogHandlerService module: postConstruct()");
        System.out.println("\033[34mActive configuration: " + currentConfigName);


        ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager(currentConfigName, locator);
        List<HandlerConfiguration> handlerConfigurationList = configurationManager.getHandlerConfigurationList();
        if (handlerConfigurationList != null) {
            for (HandlerConfiguration handlerConfiguration : handlerConfigurationList) {
                try {
                    logHandlerManager.addLogHandler(handlerConfiguration);
                    logger.log(Level.FINE, "Advanced Logging: Registered handler: {0}", handlerConfiguration.getName());
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "I/O Exception: cannot create handler {0}", handlerConfiguration.getName());
                }
            }
        }
        List<LoggerBinding> loggerBindingList = configurationManager.getLoggerBindingList();
        if(loggerBindingList != null) {
            for (LoggerBinding loggerBinding : loggerBindingList) {
                String handlerName = loggerBinding.getHandlerName();
                String loggerName = loggerBinding.getLoggerName();
                boolean useParentHandlers = Boolean.parseBoolean(loggerBinding.getUseParentHandlers());
                logHandlerManager.bindLogger(handlerName, loggerName, useParentHandlers);
                logger.log(Level.FINE, "Advanced Logging: Registered binding: {0} -> {1}", new Object[]{loggerName, handlerName});
            }
        }

    }
   
}

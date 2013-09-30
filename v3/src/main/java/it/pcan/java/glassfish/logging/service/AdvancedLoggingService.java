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
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Startup;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * @author Pierantonio Cangianiello
 */
@Service
@Scoped(Singleton.class)
public class AdvancedLoggingService implements Startup, PostConstruct {

    private final static Logger logger = Logger.getLogger(AdvancedLoggingService.class.getName());
    @Inject(name = ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config currentConfig;
    @Inject
    private static Habitat habitat;
    @Inject
    private static LogHandlerManager logHandlerManager;

    @Override
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    @Override
    public void postConstruct() {
        String currentConfigName = currentConfig.getName();

        System.out.println("\033[34mLogHandlerService module: postConstruct()");
        System.out.println("\033[34mActive configuration: " + currentConfigName);


        ConfigurationManager configurationManager = getConfigurationManager(currentConfigName);
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

    public String getCurrentConfigName() {
        return currentConfig.getName();
    }

    public ConfigurationManager getConfigurationManager(String configName) {
        ConfigurationManager configurationManager = null;
        if (habitat != null) {
            Configs configs = habitat.getComponent(Configs.class);
            Config config = configs.getConfigByName(configName);
            AdvancedLoggingConfiguration advancedLoggingConfiguration =
                    config.getExtensionByType(AdvancedLoggingConfiguration.class);
            if (advancedLoggingConfiguration == null) {
                try {
                    advancedLoggingConfiguration = ConfigurationManager.createEmptyConfiguration(config);
                } catch (TransactionFailure ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            configurationManager = new ConfigurationManager(advancedLoggingConfiguration);
        }
        return configurationManager;
    }
}

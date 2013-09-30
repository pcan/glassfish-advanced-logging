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
package it.pcan.java.glassfish.logging.jsf.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import it.pcan.java.glassfish.logging.configuration.control.ConfigurationManager;
import it.pcan.java.glassfish.logging.configuration.entity.HandlerConfiguration;
import it.pcan.java.glassfish.logging.configuration.entity.LoggerBinding;
import it.pcan.java.glassfish.logging.handler.LogHandlerManager;
import it.pcan.java.glassfish.logging.service.AdvancedLoggingService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * @author Pierantonio Cangianiello
 */
@Service
@Scoped(Singleton.class)
public class AdvancedLoggingJsfHandlers implements Startup {

    private final static Logger logger = Logger.getLogger(AdvancedLoggingJsfHandlers.class.getName());
    @Inject
    private static AdvancedLoggingService advancedLoggingService;
    @Inject
    private static LogHandlerManager logHandlerManager;

    @Override
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    /**
     * Manages gf.getLogHandlers request from the Adming Console
     * 
     * @param handlerCtx
     */
    @Handler(id = "gf.getLogHandlers",
    input = {
        @HandlerInput(name = "configName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "logHandlers", type = List.class)})
    public static void getLogHandlers(HandlerContext handlerCtx) {
        ConfigurationManager configurationManager = getConfigurationManager(handlerCtx);
        List logHandlers = new ArrayList();
        if (configurationManager != null) {
            List<HandlerConfiguration> logHandlerList = configurationManager.getHandlerConfigurationList();
            for (HandlerConfiguration handlerConfiguration : logHandlerList) {
                Map rowMap = new HashMap<String, String>();
                rowMap.put("name", handlerConfiguration.getName());
                rowMap.put("fileNamePattern", handlerConfiguration.getFileNamePattern());
                rowMap.put("maxFileSize", handlerConfiguration.getMaxFileSize());
                rowMap.put("maxFileRotations", handlerConfiguration.getMaxFileRotations());
                rowMap.put("selected", false);
                logHandlers.add(rowMap);
            }
        }
        handlerCtx.setOutputValue("logHandlers", logHandlers);
    }
    
    /**
     *  Manages gf.getLogHandlerNames request from the Adming Console
     * 
     * @param handlerCtx 
     */
    @Handler(id = "gf.getLogHandlerNames",
    input = {
        @HandlerInput(name = "configName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "logHandlerNames", type = List.class)})
    public static void getLogHandlerNames(HandlerContext handlerCtx) {
        ConfigurationManager configurationManager = getConfigurationManager(handlerCtx);
        List logHandlerNames = new ArrayList();
        if (configurationManager != null) {
            List<HandlerConfiguration> handlerConfigurationList = configurationManager.getHandlerConfigurationList();
            logHandlerNames = new ArrayList(handlerConfigurationList.size());
            for(HandlerConfiguration handlerConfiguration : handlerConfigurationList) {
                logHandlerNames.add(handlerConfiguration.getName());
            }
        }
        handlerCtx.setOutputValue("logHandlerNames", logHandlerNames);
    }
    
    /**
     *  Manages gf.getLoggerBindings request from the Adming Console
     * 
     * @param handlerCtx 
     */
    @Handler(id = "gf.getLoggerBindings",
    input = {
        @HandlerInput(name = "configName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "bindingList", type = List.class)})
    public static void getLoggerBindings(HandlerContext handlerCtx) {
        ConfigurationManager configurationManager = getConfigurationManager(handlerCtx);
        List loggerBindings = new ArrayList();
        if (configurationManager != null) {
            List<LoggerBinding> loggerBindingList = configurationManager.getLoggerBindingList();
            for (LoggerBinding loggerBinding : loggerBindingList) {
                Map rowMap = new HashMap<String, String>();
                rowMap.put("loggerName", loggerBinding.getLoggerName());
                rowMap.put("handlerName", loggerBinding.getHandlerName());
                rowMap.put("useParentHandlers", Boolean.parseBoolean(loggerBinding.getUseParentHandlers()));
                rowMap.put("selected", false);
                loggerBindings.add(rowMap);
            }
        }
        handlerCtx.setOutputValue("bindingList", loggerBindings);
    }

    
    /**
     *  Manages gf.updateLogHandlers request from the Adming Console
     * 
     * @param handlerCtx 
     */
    @Handler(id = "gf.updateLogHandlers",
    input = {
        @HandlerInput(name = "allRows", type = List.class, required = true),
        @HandlerInput(name = "configName", type = String.class, required = true)})
    public static void updateLogHandlers(HandlerContext handlerCtx) {
        boolean currentConfiguration = isCurrentConfiguration(handlerCtx);
        ConfigurationManager configurationManager = getConfigurationManager(handlerCtx);
        List<Map<String, String>> rows = (List) handlerCtx.getInputValue("allRows");

        List<HandlerConfiguration> logHandlerList = configurationManager.getHandlerConfigurationList();

        Map<String, HandlerConfiguration> unchangedHandlers = new HashMap<String, HandlerConfiguration>();
        Map<String, HandlerConfiguration> changedHandlers = new HashMap<String, HandlerConfiguration>();
        Map<String, HandlerConfiguration> deletedHandlers = new HashMap<String, HandlerConfiguration>();
        Map<String, HandlerConfiguration> addedHandlers = new HashMap<String, HandlerConfiguration>();
        //look for unchanged, changed and deleted items...
        for (HandlerConfiguration handlerConfiguration : logHandlerList) {
            boolean found = false;
            for (Map<String, ?> row : rows) {
                String name = (String) row.get("name");
                String fileNamePattern = (String) row.get("fileNamePattern");
                int maxFileSize, maxFileRotations;
                try {
                    maxFileSize = Integer.parseInt("" + row.get("maxFileSize")); //empty string concat fixes Integer cast
                    maxFileRotations = Integer.parseInt("" + row.get("maxFileRotations"));
                } catch (NumberFormatException ex) {
                    GuiUtil.handleError(handlerCtx, "Invalid number provided for this handler: " + name);
                    return;
                }
                if (handlerConfiguration.getName().equals(name)) {
                    found = true;
                    if (handlerConfiguration.getFileNamePattern().equals(fileNamePattern)
                            && handlerConfiguration.getMaxFileSize() == maxFileSize
                            && handlerConfiguration.getMaxFileRotations() == maxFileRotations) {
                        unchangedHandlers.put(name, handlerConfiguration);
                    } else {
                        changedHandlers.put(name, handlerConfiguration);
                    }
                    break;
                }
            }
            if (!found) {
                deletedHandlers.put(handlerConfiguration.getName(), handlerConfiguration);
            }
        }

        //Look for new items and update changed ones...
        for (Map<String, ?> row : rows) {
            String name = (String) row.get("name");
            String fileNamePattern = (String) row.get("fileNamePattern");
            int maxFileSize = Integer.parseInt("" + row.get("maxFileSize"));
            int maxFileRotations = Integer.parseInt("" + row.get("maxFileRotations"));
            if (!changedHandlers.containsKey(name) && !deletedHandlers.containsKey(name)
                    && !addedHandlers.containsKey(name) && !unchangedHandlers.containsKey(name)) {
                try {
                    HandlerConfiguration addNewHandlerConfiguration =
                            configurationManager.addHandlerConfiguration(name, fileNamePattern, maxFileSize, maxFileRotations);
                    addedHandlers.put(name, addNewHandlerConfiguration);
                    if(currentConfiguration) {
                        logHandlerManager.addLogHandler(addNewHandlerConfiguration);
                    }
                } catch (TransactionFailure ex) {
                    GuiUtil.handleError(handlerCtx, "Cannot add this handler: " + name);
                    logger.log(Level.WARNING, "Cannot update log handler settings", ex);
                    return;
                } catch (IOException ex) {
                    GuiUtil.handleError(handlerCtx, "I/O Exception: cannot create this handler: " + name);
                    logger.log(Level.WARNING, "Cannot update log handler settings", ex);
                    return;
                }
            }
            if (changedHandlers.containsKey(name)) {
                try {
                    HandlerConfiguration handlerConfiguration = changedHandlers.get(name);
                    configurationManager.updateHandlerConfiguration(handlerConfiguration, fileNamePattern, maxFileSize, maxFileRotations);
                    if(currentConfiguration) {
                        logHandlerManager.updateLogHandler(handlerConfiguration);
                    }
                } catch (TransactionFailure ex) {
                    GuiUtil.handleError(handlerCtx, "Cannot update settings for this handler: " + name);
                    logger.log(Level.WARNING, "Cannot update log handler settings", ex);
                    return;
                } catch (IOException ex) {
                    GuiUtil.handleError(handlerCtx, "I/O Exception: cannot update this handler: " + name);
                    logger.log(Level.WARNING, "Cannot update log handler settings", ex);
                    return;
                }
            }
        }

        //delete configurations 
        for (HandlerConfiguration handlerConfiguration : deletedHandlers.values()) {
            String name = handlerConfiguration.getName();
            try {
                configurationManager.removeHandlerConfiguration(name);
                if(currentConfiguration) {
                    logHandlerManager.removeLogHandler(name);
                }
            } catch (TransactionFailure ex) {
                GuiUtil.handleError(handlerCtx, "Error while removing this handler: " + name);
                logger.log(Level.WARNING, "Cannot update log handler settings", ex);
            }
        }
    }

    /**
     * Manages gf.updateLoggersBinding request from the Adming Console
     *
     * @param handlerCtx
     */
    @Handler(id = "gf.updateLoggersBinding",
    input = {
        @HandlerInput(name = "allRows", type = List.class, required = true),
        @HandlerInput(name = "configName", type = String.class, required = true)})
    public static void updateLoggersBinding(HandlerContext handlerCtx) {
        boolean currentConfiguration = isCurrentConfiguration(handlerCtx);
        ConfigurationManager configurationManager = getConfigurationManager(handlerCtx);
        List<Map<String, String>> rows = (List) handlerCtx.getInputValue("allRows");
        
        List<LoggerBinding> loggerBindingList = configurationManager.getLoggerBindingList();        
        for (LoggerBinding loggerBinding : loggerBindingList) {
            boolean found = false;
            for (Map<String, ?> row : rows) {
                String loggerName = (String) row.get("loggerName");
                String handlerName = (String) row.get("handlerName");
                if (loggerBinding.getHandlerName().equals(handlerName)
                        && loggerBinding.getLoggerName().equals(loggerName)) {
                    found = true;
                    break;
                }
            }
            if (!found && currentConfiguration) {
                logHandlerManager.unbindLogger(loggerBinding.getHandlerName(), loggerBinding.getLoggerName());
            }
        }
        try {
            configurationManager.removeAllLoggerBindings();
        } catch (TransactionFailure ex) {
            GuiUtil.handleError(handlerCtx, "Error while removing old bindings");
            logger.log(Level.WARNING, "Cannot update logger bindings settings", ex);
            return;
        }
        for (Map<String, ?> row : rows) {
            String loggerName = (String) row.get("loggerName");
            String handlerName = (String) row.get("handlerName");
            Boolean useParentHandlers = Boolean.parseBoolean("" + row.get("useParentHandlers")); //empty string concat fixes Boolean cast
            try {
                configurationManager.addLoggerBinding(handlerName, loggerName, useParentHandlers);
                if(currentConfiguration) {
                    logHandlerManager.bindLogger(handlerName, loggerName, useParentHandlers);
                }
            } catch (TransactionFailure ex) {
                GuiUtil.handleError(handlerCtx, "Error while adding logger binding: " + loggerName);
                logger.log(Level.WARNING, "Cannot update logger bindings settings", ex);
                return;
            }
        }        
        
    }
       
    /**
     * Check if the configuration-name passed by the Admin Console is the active one.
     * 
     * @param handlerCtx
     * @return 
     */
    private static boolean isCurrentConfiguration(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("configName");
        return advancedLoggingService.getCurrentConfigName().equals(configName);
    }
    
    
    /**
     *
     * @param handlerCtx
     * @return
     */
    private static ConfigurationManager getConfigurationManager(HandlerContext handlerCtx) {
        String configName = (String) handlerCtx.getInputValue("configName");
        return advancedLoggingService.getConfigurationManager(configName);
    }
}

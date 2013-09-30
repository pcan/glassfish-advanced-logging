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
package it.pcan.java.glassfish.logging.configuration.control;

import com.sun.enterprise.config.serverbeans.Config;
import it.pcan.java.glassfish.logging.configuration.entity.AdvancedLoggingConfiguration;
import it.pcan.java.glassfish.logging.configuration.entity.HandlerConfiguration;
import it.pcan.java.glassfish.logging.configuration.entity.LoggerBinding;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * This class contains convenience methods for handling configuration.
 * 
 * @author Pierantonio Cangianiello
 */
public class ConfigurationManager {

    private final static Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    private final AdvancedLoggingConfiguration configuration;

    public ConfigurationManager(AdvancedLoggingConfiguration configuration) {
        this.configuration = configuration;
    }

    public List<HandlerConfiguration> getHandlerConfigurationList() {
        return configuration.getLogHandler();
    }

    public List<LoggerBinding> getLoggerBindingList() {
        return configuration.getLoggerBinding();
    }
    
    public void removeAllLoggerBindings() throws TransactionFailure {
        Iterator<LoggerBinding> iterator = configuration.getLoggerBinding().iterator();
        while (iterator.hasNext()) {
            LoggerBinding loggerBinding = iterator.next();
            ConfigBean loggerBindingBean = (ConfigBean) ConfigBean.unwrap(loggerBinding);
            ConfigSupport.deleteChild(loggerBindingBean.parent(), loggerBindingBean);
        }
    }
    
    protected void removeLoggerBindings(String handlerName) throws TransactionFailure {
        Iterator<LoggerBinding> iterator = configuration.getLoggerBinding().iterator();
        while (iterator.hasNext()) {
            LoggerBinding loggerBinding = iterator.next();
            if(loggerBinding.getHandlerName().equals(handlerName)) {
                ConfigBean loggerBindingBean = (ConfigBean) ConfigBean.unwrap(loggerBinding);
                ConfigSupport.deleteChild(loggerBindingBean.parent(), loggerBindingBean);
            }
        }
    }
    
    public HandlerConfiguration addHandlerConfiguration(final String name, final String fileNamePattern, final int maxFileSize, final int maxFileRotations) throws TransactionFailure {
        return (HandlerConfiguration) ConfigSupport.apply(new SingleConfigCode<AdvancedLoggingConfiguration>() {
            @Override
            public Object run(AdvancedLoggingConfiguration parent) throws TransactionFailure, PropertyVetoException {
                HandlerConfiguration child = parent.createChild(HandlerConfiguration.class);
                child.setName(name);
                child.setFileNamePattern(fileNamePattern);
                child.setMaxFileSize(maxFileSize);
                child.setMaxFileRotations(maxFileRotations);
                parent.getLogHandler().add(child);
                return child;
            }
        }, configuration);
    }
    
    public LoggerBinding addLoggerBinding(final String handlerName, final String loggerName, final Boolean useParentHandlers) throws TransactionFailure {
        return (LoggerBinding) ConfigSupport.apply(new SingleConfigCode<AdvancedLoggingConfiguration>() {
            @Override
            public Object run(AdvancedLoggingConfiguration parent) throws TransactionFailure, PropertyVetoException {
                LoggerBinding child = parent.createChild(LoggerBinding.class);
                child.setLoggerName(loggerName);
                child.setHandlerName(handlerName);
                child.setUseParentHandlers(useParentHandlers);
                parent.getLoggerBinding().add(child);
                return child;
            }
        }, configuration);
    }

    public void removeHandlerConfiguration(String logHandlerName) throws TransactionFailure {
        Iterator<HandlerConfiguration> iterator = configuration.getLogHandler().iterator();
        removeLoggerBindings(logHandlerName);
        while (iterator.hasNext()) {
            HandlerConfiguration handlerConfiguration = iterator.next();
            if (handlerConfiguration.getName().equals(logHandlerName)) {
                ConfigBean handlerConfigBean = (ConfigBean) ConfigBean.unwrap(handlerConfiguration);
                ConfigSupport.deleteChild(handlerConfigBean.parent(), handlerConfigBean);
            }
        }
    }

    public void updateHandlerConfiguration(final HandlerConfiguration handlerConfiguration, final String fileNamePattern, final int maxFileSize, final int maxFileRotations) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<HandlerConfiguration>() {
            @Override
            public Object run(HandlerConfiguration parent) throws TransactionFailure, PropertyVetoException {
                parent.setFileNamePattern(fileNamePattern);
                parent.setMaxFileSize(maxFileSize);
                parent.setMaxFileRotations(maxFileRotations);
                return null;
            }
        }, handlerConfiguration);
    }

    public static AdvancedLoggingConfiguration createEmptyConfiguration(Config config) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Config>() {
            @Override
            public Object run(Config parent) throws TransactionFailure {
                AdvancedLoggingConfiguration child = parent.createChild(AdvancedLoggingConfiguration.class);
                parent.getContainers().add(child);
                return child;
            }
        }, config);
        return config.getExtensionByType(AdvancedLoggingConfiguration.class);
    }
    
    
}

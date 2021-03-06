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
package it.pcan.java.glassfish.logging.configuration.entity;

import java.util.List;
import org.glassfish.api.admin.config.ConfigExtension;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * advanced-logging configuration entity for domain.xml
 * 
 * @author Pierantonio Cangianiello
 */
@Configured(name = "advanced-logging")
public interface AdvancedLoggingConfiguration extends ConfigExtension {
    
    @Element
    public List<HandlerConfiguration> getLogHandler();
    public void setLogHandler(List<HandlerConfiguration> list);
    
    
    @Element
    public List<LoggerBinding> getLoggerBinding();
    public void setLoggerBinding(List<LoggerBinding> list);
}

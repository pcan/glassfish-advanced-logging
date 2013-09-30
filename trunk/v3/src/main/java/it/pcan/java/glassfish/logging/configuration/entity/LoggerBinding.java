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

import javax.validation.constraints.NotNull;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 *
 * @author Pierantonio Cangianiello
 */
@Configured
public interface LoggerBinding extends ConfigBeanProxy{
    
    @Attribute(required=true)
    @NotNull
    public String getLoggerName();
    public void setLoggerName(String loggerName);
   
    //The following code has been commented due to a glassfish bug (side-effect NullPointer in ConfigModel)
    /*
    @Attribute(reference=true)
    @NotNull
    public HandlerConfiguration getHandlerName();
    public void setHandlerName(HandlerConfiguration handlerName);*/
    
    @Attribute(required=true)
    @NotNull
    public String getHandlerName();
    public void setHandlerName(String handlerName);
    
    @Attribute(defaultValue="true", dataType=Boolean.class)
    public String getUseParentHandlers();
    public void setUseParentHandlers(Boolean useParentHandlers);
    
}

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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.*;


/**
 * handler-configuration configuration entity for domain.xml
 * 
 * @author Pierantonio Cangianiello
 */
@Configured
public interface HandlerConfiguration extends ConfigBeanProxy, Named {
    
    @Attribute(required=true)
    @NotNull
    public String getFileNamePattern();
    public void setFileNamePattern(String fileNamePattern);
    
    @Max(2000000000) //2GB limit
    @Min(512)
    @Attribute(defaultValue="1000000") //default rotation: 1MB
    public int getMaxFileSize();
    public void setMaxFileSize(int MaxFileSize);
    
    
    @Max(2000000000)
    @Min(1)
    @Attribute(defaultValue="100") //default file count: 100
    public int getMaxFileRotations();
    public void setMaxFileRotations(int maxFileRotations);
}

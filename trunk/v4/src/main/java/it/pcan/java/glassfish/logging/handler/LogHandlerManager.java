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
package it.pcan.java.glassfish.logging.handler;

import it.pcan.java.glassfish.logging.configuration.entity.HandlerConfiguration;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.jvnet.hk2.annotations.Service;


/**
 * This class allows Log Handlers management (creation, update and removal) and logger binding/unbinding to handlers.
 * 
 * @author Pierantonio Cangianiello
 */
@Service
@Singleton
public class LogHandlerManager {

    private final Map<String, HandlerDetail> handlerMap = new HashMap<String, HandlerDetail>();

    /**
     * Creates and initializes a new Log Handler using the given configuration
     * @param config The Log Handler configuration
     * @throws IOException 
     */
    public void addLogHandler(HandlerConfiguration config) throws IOException {
        Handler handler = createHandler(config.getFileNamePattern(), config.getMaxFileSize(), config.getMaxFileRotations());
        HandlerDetail handlerDetail = new HandlerDetail(config.getName(), handler);
        handlerMap.put(config.getName(), handlerDetail);
    }

    /**
     * Updates an existing Log Handler using the given configuration.
     *
     * @param config The Log Handler configuration
     * @throws IOException 
     */
    public void updateLogHandler(HandlerConfiguration config) throws IOException {
        String name = config.getName();
        HandlerDetail oldHandlerDetail = handlerMap.get(name);
        if (oldHandlerDetail != null) {
            Handler oldHandler = oldHandlerDetail.handler;
            Set<Logger> loggers = oldHandlerDetail.loggers;
            Handler handler = createHandler(config.getFileNamePattern(), config.getMaxFileSize(), config.getMaxFileRotations());
            HandlerDetail newHandlerDetail = new HandlerDetail(name, handler, loggers);
            for (Logger logger : loggers) {
                logger.removeHandler(oldHandler);
                logger.addHandler(handler);
            }
            handlerMap.remove(config.getName()); //unnecessary, but left for clearness            
            handlerMap.put(name, newHandlerDetail);
            oldHandler.close();
        }
    }

    /**
     * Removes a Log Handler, given its name. It removes all logger bindings, too.
     * @param name 
     */
    public void removeLogHandler(String name) {
        HandlerDetail handlerDetail = handlerMap.get(name);
        if (handlerDetail != null) {
            for (Logger logger : handlerDetail.loggers) {
                logger.removeHandler(handlerDetail.handler);
                logger.setUseParentHandlers(true);
            }
            handlerDetail.handler.close();
            handlerDetail.loggers.clear();
            handlerMap.remove(name);
        }
    }

    /**
     * Bind a Logger to an existing Log Handler.
     * 
     * @param handlerName The name of the Log Handler
     * @param loggerName The name of the Logger
     * @param useParentHandlers If set to true, the Logger messages will be propagated to parent Loggers too.
     */
    public void bindLogger(String handlerName, String loggerName, boolean useParentHandlers) {
        HandlerDetail handlerDetail = handlerMap.get(handlerName);
        if (handlerDetail != null) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setUseParentHandlers(useParentHandlers);
            if (!handlerDetail.loggers.contains(logger)) {
                logger.addHandler(handlerDetail.handler);
                handlerDetail.loggers.add(logger);
            }
        }
    }

    /**
     * Unbind a logger
     * 
     * @param handlerName The name of the Log Handler
     * @param loggerName The name of the Logger
     */
    public void unbindLogger(String handlerName, String loggerName) {
        HandlerDetail handlerDetail = handlerMap.get(handlerName);
        if (handlerDetail != null) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setUseParentHandlers(true);
            logger.removeHandler(handlerDetail.handler);
            handlerDetail.loggers.remove(logger);
        }
    }

    /**
     * Get all valid Log Handler Names.
     * 
     * @return A collection of Log Handler Names.
     */
    public Collection<String> getAllHandlerNames() {
        return handlerMap.keySet();
    }

    /**
     * Get all valid Log Handler Details.
     * 
     * @return A collection of Log Handler Details.
     */
    public Collection<HandlerDetail> getAllHandlerDetails() {
        return handlerMap.values();
    }

    /**
     * This class holds the java.util.logging.Handler object, Handler bindings and Handler name.
     */
    public static class HandlerDetail {

        final String name;
        final Handler handler;
        final Set<Logger> loggers;

        HandlerDetail(String name, Handler handler) {
            this.handler = handler;
            this.name = name;
            this.loggers = new HashSet<Logger>();
        }

        HandlerDetail(String name, Handler handler, Set<Logger> loggers) {
            this.handler = handler;
            this.name = name;
            this.loggers = loggers;
        }

        public String getName() {
            return name;
        }

        public Collection<Logger> getLoggers() {
            return Collections.unmodifiableCollection(loggers);
        }
    }

    private Handler createHandler(String fileNamePattern, int maxFileSize, int maxFileRotations) throws IOException {
        Handler handler = new FileHandler(fileNamePattern, maxFileSize, maxFileRotations, true);
        handler.setFormatter(new MessageFormatter()); //TODO: choose custom formatter in Admin Console!
        return handler;
    }

    private class MessageFormatter extends Formatter {

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        
        @Override
        public String format(LogRecord record) {
            date.setTime(System.currentTimeMillis());
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append(dateFormat.format(date));
            builder.append("|");
            builder.append(record.getLevel().toString());
            builder.append("|");
            builder.append(record.getLoggerName());
            builder.append("] ");
            builder.append(formatMessage(record));
            if (record.getThrown() != null) {
                builder.append("\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                builder.append(sw.toString());
            }
            builder.append("\n");
            return builder.toString();
        }
    }
}

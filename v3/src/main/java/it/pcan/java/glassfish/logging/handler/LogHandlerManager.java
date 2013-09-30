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
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 *
 * @author Pierantonio Cangianiello
 */
@Service
@Scoped(Singleton.class)
public class LogHandlerManager {

    private final Map<String, HandlerDetail> handlerMap = new HashMap<String, HandlerDetail>();

    public void addLogHandler(HandlerConfiguration config) throws IOException {
        Handler handler = createHandler(config.getFileNamePattern(), config.getMaxFileSize(), config.getMaxFileRotations());
        HandlerDetail handlerDetail = new HandlerDetail(config.getName(), handler);
        handlerMap.put(config.getName(), handlerDetail);
    }

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

    public void removeLogHandler(String name) {
        HandlerDetail handlerDetail = handlerMap.get(name);
        if (handlerDetail != null) {
            for (Logger logger : handlerDetail.loggers) {
                logger.removeHandler(handlerDetail.handler);
            }
            handlerDetail.handler.close();
            handlerDetail.loggers.clear();
            handlerMap.remove(name);
        }
    }

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

    public void unbindLogger(String handlerName, String loggerName) {
        HandlerDetail handlerDetail = handlerMap.get(handlerName);
        if (handlerDetail != null) {
            Logger logger = Logger.getLogger(loggerName);
            logger.setUseParentHandlers(true);
            logger.removeHandler(handlerDetail.handler);
            handlerDetail.loggers.remove(logger);
        }
    }

    public Collection<String> getAllHandlerNames() {
        return handlerMap.keySet();
    }

    public Collection<HandlerDetail> getAllHandlerDetails() {
        return handlerMap.values();
    }

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
        handler.setFormatter(new MessageFormatter());
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

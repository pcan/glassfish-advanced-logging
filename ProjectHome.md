# Glassfish Advanced Logging #

## Intro ##
An extension for Glassfish v3 & v4 logging system that allows the redirection of specific log flows to different files. It is fully integrated with the existing HK2 Kernel, so it's a plug-and-play module. This module comes with a standard Glassfish Admin Console configuration interface, integrated under the Configuration node.

## Setup ##
Copy the [Glassfish3 jar archive](https://glassfish-advanced-logging.googlecode.com/svn/wiki/advanced-logging-gf3.jar) or the [Glassfish4 jar archive](https://glassfish-advanced-logging.googlecode.com/svn/wiki/advanced-logging-gf4.jar) in the modules directory, under the Glassfish Server installation path. Restart the server, the module is already up and functioning.

## Configuration ##
The configuration interface for the Advanced Logging module has been integrated in the Configuration Tree of the Admin Console (see below):

![https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/tree_node.jpg](https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/tree_node.jpg)

Usually, the active configuration for the running instance is **server-config** and not **default-config**, so pay attention when you open Configuration node and choose the right one.
In order to redirect a particular log flow to a file, a new Log Handler must be defined. Log handlers catch messages from loggers and redirect them to files or streams. Currently only file handler is implemented, but a in a future release this feature will be configurable. In the figure below there is a sample configuration for a Log Handler that creates log files with the maximum specified size and uses the specified quantity of files for rotation. With file rotation parameter the user can specify the maximum number of files that the Log Handler will create. In the configuration screen there is a list of some useful placeholders for file pattern definition.

![https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/log_handlers.jpg](https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/log_handlers.jpg)

Loggers can be bound to Log Handlers by adding a new row in the Loggers section (see below); the "Use parent handlers" checkbox can enable log flow propagation to parent loggers: this can be useful if a group of multiple loggers must write in a common file and each logger has its dedicated log file too.

![https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/bindings.jpg](https://glassfish-advanced-logging.googlecode.com/svn/wiki/img/bindings.jpg)

Please note that each logger can be bound to multiple Handlers, and a single Handler can manage multiple loggers.
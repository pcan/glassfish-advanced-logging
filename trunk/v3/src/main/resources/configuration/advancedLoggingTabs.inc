

<sun:tabSet id="advancedLoggingTabs" immediate="true" selected="#{sessionScope.advancedLoggingTabs}" >
    <sun:tab id="advancedLoggingHandlersTab" immediate="true" text="Log Handlers" toolTip="Log Handlers Tab">
	<!command
            setSessionAttribute(key="advancedLoggingTabs" value="advancedLoggingHandlersTab");
            gf.redirect(page="#{request.contextPath}/advancedlogging/configuration/logHandlers.jsf?configName=#{configName}");
	/>
    </sun:tab>

    <sun:tab id="advancedLoggingLoggersTab" immediate="true" text="Loggers" toolTip="Loggers Tab">
	<!command
            setSessionAttribute(key="advancedLoggingTabs" value="advancedLoggingLoggersTab");
            gf.redirect(page="#{request.contextPath}/advancedlogging/configuration/loggers.jsf?configName=#{configName}");
	/>
    </sun:tab>
</sun:tabSet>

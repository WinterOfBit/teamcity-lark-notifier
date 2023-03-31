package cn.scutbot.teamcitylark.notification;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import jetbrains.buildServer.notification.UserNotifierDescriptor;
import jetbrains.buildServer.parameters.ParametersUtil;
import jetbrains.buildServer.serverSide.ControlDescription;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.parameters.WellknownParameterArguments;
import jetbrains.buildServer.serverSide.parameters.types.TextParameter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkUserNotifierDescriptor  implements UserNotifierDescriptor {
    private final LarkNotifierProperties properties;
    private final LarkNotifierDescriptor larkNotifierDescriptor;
    private final PluginDescriptor pluginDescriptor;

    public LarkUserNotifierDescriptor(LarkNotifierProperties properties, LarkNotifierDescriptor larkNotifierDescriptor, PluginDescriptor pluginDescriptor) {
        this.properties = properties;
        this.larkNotifierDescriptor = larkNotifierDescriptor;
        this.pluginDescriptor = pluginDescriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return LarkNotifierProperties.TypeNotifier;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return LarkNotifierProperties.NameGlobal;
    }

    @NotNull
    @Override
    public Map<String, ControlDescription> getParameters() {
        return Map.of(
                properties.getPropertyConnection().getKey(), ParametersUtil.createControlDescription(
                        "selection",
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Connection",
                                WellknownParameterArguments.REQUIRED.getName(), "true"
                        )
                ),
                properties.getPropertyReceiverType().getKey(), ParametersUtil.createControlDescription(
                        TextParameter.DEFAULT_PARAMETER_ARG,
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Receiver ID Type",
                                WellknownParameterArguments.REQUIRED.getName(), "true",
                                "default", "email"
                        )
                ),
                properties.getPropertyReceiverID().getKey(), ParametersUtil.createControlDescription(
                        TextParameter.KEY,
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Your Receiver ID (Email by default)",
                                WellknownParameterArguments.REQUIRED.getName(), "true"
                        )
                )
        );
    }

    @NotNull
    @Override
    public Collection<InvalidProperty> validate(@NotNull Map<String, String> map) {
        return larkNotifierDescriptor.validate(map);
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return pluginDescriptor.getPluginResourcesPath("NotifierDescriptor.html");
    }
}

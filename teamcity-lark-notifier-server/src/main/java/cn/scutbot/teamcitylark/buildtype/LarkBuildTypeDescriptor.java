package cn.scutbot.teamcitylark.buildtype;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import cn.scutbot.teamcitylark.connection.LackConnectionSelectOptionsProvider;
import cn.scutbot.teamcitylark.lark.LarkReceiverTypeProvider;
import cn.scutbot.teamcitylark.notification.LarkNotifierDescriptor;
import jetbrains.buildServer.notification.BuildTypeNotifierDescriptor;
import jetbrains.buildServer.serverSide.ControlDescription;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.parameters.WellknownParameterArguments;
import jetbrains.buildServer.serverSide.parameters.types.TextParameter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.parameters.ParametersUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkBuildTypeDescriptor implements BuildTypeNotifierDescriptor {
    private final LarkNotifierDescriptor notifierDescriptor;
    private final PluginDescriptor pluginDescriptor;
    private final LarkNotifierProperties properties;

    private final LackConnectionSelectOptionsProvider connectionSelectOptionsProvider;
    private final LarkReceiverTypeProvider receiverTypeProvider;

    public LarkBuildTypeDescriptor(LarkNotifierDescriptor notifierDescriptor, PluginDescriptor pluginDescriptor, LarkNotifierProperties properties, LackConnectionSelectOptionsProvider connectionSelectOptionsProvider, LarkReceiverTypeProvider receiverTypeProvider) {
        this.notifierDescriptor = notifierDescriptor;
        this.pluginDescriptor = pluginDescriptor;
        this.properties = properties;
        this.connectionSelectOptionsProvider = connectionSelectOptionsProvider;
        this.receiverTypeProvider = receiverTypeProvider;
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> map) {
        return "Describe!";
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
                                WellknownParameterArguments.REQUIRED.getName(), "true",
                                "selectOptionsProviderId", connectionSelectOptionsProvider.getId()
                        )
                ),
                properties.getPropertyReceiverType().getKey(), ParametersUtil.createControlDescription(
                        "selection",
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Receiver type",
                                WellknownParameterArguments.REQUIRED.getName(), "true",
                                "selectOptionsProviderId", receiverTypeProvider.getId()
                        )
                ),
                properties.getPropertyReceiverID().getKey(), ParametersUtil.createControlDescription(
                        TextParameter.KEY,
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Receiver ID",
                                WellknownParameterArguments.REQUIRED.getName(), "true"
                        )
                ),
                properties.getPropertyOverriddenParams().getKey(), ParametersUtil.createControlDescription(
                        TextParameter.KEY,
                        Map.of(
                                WellknownParameterArguments.ARGUMENT_DESCRIPTION.getName(), "Overrider Params",
                                WellknownParameterArguments.REQUIRED.getName(), "true"
                        )
                )
        );
    }

    @NotNull
    @Override
    public Collection<InvalidProperty> validate(@NotNull Map<String, String> map) {
        return notifierDescriptor.validate(map);
    }

    @NotNull
    @Override
    public String getEditParametersUrl() {
        return pluginDescriptor.getPluginResourcesPath("EditBuildTypeParams.html");
    }
}

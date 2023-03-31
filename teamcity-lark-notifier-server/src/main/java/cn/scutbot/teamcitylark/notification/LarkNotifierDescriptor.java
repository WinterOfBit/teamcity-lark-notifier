package cn.scutbot.teamcitylark.notification;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.notification.NotifierDescriptor;
import jetbrains.buildServer.serverSide.ControlDescription;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkNotifierDescriptor implements NotifierDescriptor {
    private final NotificatorRegistry registry;
    private final PluginDescriptor descriptor;

    public LarkNotifierDescriptor(NotificatorRegistry registry, PluginDescriptor descriptor) {
        this.registry = registry;
        this.descriptor = descriptor;
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
                "ReceiverType", new ControlDescription() {
                    @NotNull
                    @Override
                    public String getParameterType() {
                        return "String";
                    }

                    @NotNull
                    @Override
                    public Map<String, String> getParameterTypeArguments() {
                        return Map.of();
                    }
                }
        );
    }

    @NotNull
    @Override
    public Collection<InvalidProperty> validate(@NotNull Map<String, String> map) {
        return List.of();
    }

    @Nullable
    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("EditNotifierParams.html");
    }
}

package cn.scutbot.teamcitylark.connection;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor;
import jetbrains.buildServer.serverSide.oauth.OAuthProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkConnectionProvider extends OAuthProvider {
    private final PluginDescriptor descriptor;

    public LarkConnectionProvider(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @NotNull
    @Override
    public String getType() {
        return LarkNotifierProperties.TypeConnection;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return LarkNotifierProperties.NameGlobal;
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return map -> {
            var errors = new HashSet<InvalidProperty>();

            var appId = map.getOrDefault("appId", "");
            var appSecret = map.getOrDefault("secure:appSecret", "");

            if (appId.equals(""))
                errors.add(new InvalidProperty("appId", "App ID cannot be null"));

            if (appSecret.equals(""))
                errors.add(new InvalidProperty("secure:appSecret", "App Secret cannot be null"));

            return errors;
        };
    }

    @NotNull
    @Override
    public String describeConnection(@NotNull OAuthConnectionDescriptor connection) {
        var displayName = connection.getConnectionDisplayName();
        var appId = connection.getParameters().getOrDefault("appId", "");

        return String.format("%s: Appid=%s", displayName, appId);
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultProperties() {
        return Map.of();
    }

    @NotNull
    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("EditConnectionParams.html");
    }
}

package cn.scutbot.teamcitylark.lark;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkClientProviderImpl implements LarkClientProvider {
    private final OAuthConnectionsManager connectionsManager;

    private final LarkTokenCacher cacher;
    private final ConcurrentHashMap<String, LarkClient> cachedClients = new ConcurrentHashMap<>();

    public LarkClientProviderImpl(OAuthConnectionsManager connectionsManager, LarkTokenCacher cacher) {
        this.connectionsManager = connectionsManager;
        this.cacher = cacher;
    }

    @Override
    public LarkClient createClient(SProject project, String connectionId) {
        var conn = connectionsManager.findConnectionById(project, connectionId);
        if (conn == null)
            return null;

        if (cachedClients.containsKey(conn.getId())) {
            return cachedClients.get(connectionId);
        } else {
            var appId = conn.getParameters().get("appId");
            var appSecret = conn.getParameters().get("secure:appSecret");

            if (appId == null || appSecret == null)
                return null;
            var client = createClient(appId, appSecret);
            cachedClients.put(conn.getId(), client);

            return client;
        }
    }

    @NotNull
    @Override
    public LarkClient createClient(String appId, String appSecret) {
        return new LarkClientImpl(appId, appSecret, cacher);
    }
}

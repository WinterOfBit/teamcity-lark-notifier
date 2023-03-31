package cn.scutbot.teamcitylark.lark;

import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;

public interface LarkClientProvider {
    LarkClient createClient(SProject project, String connectionId);

    @NotNull
    LarkClient createClient(String appId, String appSecret);
}

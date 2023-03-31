package cn.scutbot.teamcitylark.lark;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import jetbrains.buildServer.serverSide.parameters.SelectOption;
import jetbrains.buildServer.serverSide.parameters.UserSelectOptionsProvider;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkReceiverTypeProvider implements UserSelectOptionsProvider {
    private final LarkNotifierProperties properties;

    public LarkReceiverTypeProvider(LarkNotifierProperties properties) {
        this.properties = properties;
    }

    @NotNull
    @Override
    public String getId() {
        return properties.getPropertyLarkReceiverTypeProvider().getKey();
    }

    @NotNull
    @Override
    public List<SelectOption> getSelectOptions(@NotNull SUser sUser) {
        return List.of(
                new SelectOption("OPEN_ID", "open_id"),
                new SelectOption("USER_ID", "user_id"),
                new SelectOption("UNION_ID", "union_id"),
                new SelectOption("EMAIL", "email"),
                new SelectOption("CHAT_ID", "chat_id")
        );
    }
}

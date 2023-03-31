package cn.scutbot.teamcitylark.connection;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import jetbrains.buildServer.serverSide.parameters.SelectOption;
import jetbrains.buildServer.serverSide.parameters.UserSelectOptionsProvider;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LackConnectionSelectOptionsProvider implements UserSelectOptionsProvider {
    private final ProjectManager projectManager;
    private final OAuthConnectionsManager connectionsManager;
    private final LarkNotifierProperties properties;

    public LackConnectionSelectOptionsProvider(ProjectManager projectManager, OAuthConnectionsManager connectionsManager, LarkNotifierProperties properties) {
        this.projectManager = projectManager;
        this.connectionsManager = connectionsManager;
        this.properties = properties;
    }

    @NotNull
    @Override
    public String getId() {
        return properties.getPropertyConnectionSelectProvider().getKey();
    }

    @NotNull
    @Override
    public List<SelectOption> getSelectOptions(@NotNull SUser sUser) {
        var projects = projectManager.getProjects().stream().filter(sProject ->
                sUser.isPermissionGrantedForProject(sProject.getProjectId(), Permission.VIEW_PROJECT));

        var list = new ArrayList<SelectOption>();
        list.add(new SelectOption("", "-- Choose Connection --"));
        projects
                .flatMap(sProject ->
                        connectionsManager.getAvailableConnectionsOfType(sProject, LarkNotifierProperties.TypeConnection).stream())
                .forEach(descriptor -> {
                    list.add(new SelectOption(descriptor.getId(), descriptor.getConnectionDisplayName()));
                });

        return list;
    }
}

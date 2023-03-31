package cn.scutbot.teamcitylark.controllers;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import cn.scutbot.teamcitylark.connection.LarkConnectionProvider;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkConnectionParamsController extends BaseController {
    private final PluginDescriptor pluginDescriptor;
    private final ProjectManager projectManager;
    private final OAuthConnectionsManager connectionsManager;
    private final WebControllerManager webControllerManager;
    private final LarkConnectionProvider connectionProvider;
    private final WebLinks webLinks;
    private final LarkNotifierProperties larkProperties;

    public LarkConnectionParamsController(PluginDescriptor pluginDescriptor, ProjectManager projectManager, OAuthConnectionsManager connectionsManager, WebControllerManager webControllerManager, LarkConnectionProvider connectionProvider, WebLinks webLinks, LarkNotifierProperties larkProperties) {
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        this.connectionsManager = connectionsManager;
        this.webControllerManager = webControllerManager;
        this.connectionProvider = connectionProvider;
        this.webLinks = webLinks;
        this.larkProperties = larkProperties;

        webControllerManager.registerController(connectionProvider.getEditParametersUrl(), this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        var model = new ModelAndView(pluginDescriptor.getPluginResourcesPath("EditConnectionParams.jsp"));

        model.getModel().put("larkProperties", larkProperties);

        return model;
    }
}

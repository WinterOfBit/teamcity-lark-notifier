package cn.scutbot.teamcitylark.controllers;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import cn.scutbot.teamcitylark.buildtype.LarkBuildTypeDescriptor;
import cn.scutbot.teamcitylark.lark.LarkReceiverTypeProvider;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionsManager;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkBuildTypeParamsController extends BaseController {
    private final PluginDescriptor pluginDescriptor;
    private final ProjectManager projectManager;
    private final OAuthConnectionsManager connectionsManager;
    private final WebControllerManager webControllerManager;
    private final LarkBuildTypeDescriptor descriptor;
    private final WebLinks webLinks;
    private final LarkNotifierProperties larkProperties;
    private final LarkReceiverTypeProvider receiverTypeProvider;

    public LarkBuildTypeParamsController(@NotNull SBuildServer server, PluginDescriptor pluginDescriptor, ProjectManager projectManager, OAuthConnectionsManager connectionsManager, WebControllerManager webControllerManager, LarkBuildTypeDescriptor descriptor, WebLinks webLinks, LarkNotifierProperties larkProperties, LarkReceiverTypeProvider receiverTypeProvider) {
        super(server);
        this.pluginDescriptor = pluginDescriptor;
        this.projectManager = projectManager;
        this.connectionsManager = connectionsManager;
        this.webControllerManager = webControllerManager;
        this.descriptor = descriptor;
        this.webLinks = webLinks;
        this.larkProperties = larkProperties;
        this.receiverTypeProvider = receiverTypeProvider;

        webControllerManager.registerController(descriptor.getEditParametersUrl(), this);
    }

    private BuildTypeSettings findBuildTypeSettingsWithId(String buildTypeId) {
        if (buildTypeId.startsWith("buildType:")) {
            return projectManager.findBuildTypeByExternalId(buildTypeId.substring("buildType:".length()));
        }
        if (buildTypeId.startsWith("template:")) {
            return projectManager.findBuildTypeTemplateByExternalId(buildTypeId.substring("template:".length()));
        }

        return null;
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        var mv = new ModelAndView(pluginDescriptor.getPluginResourcesPath("EditBuildTypeParams.jsp"));

        var buildTypeId = request.getParameter("buildTypeId");
        var featureId = request.getParameter("featureId");
        var buildType = findBuildTypeSettingsWithId(buildTypeId);
        var user = SessionUser.getUser(request);
        if (buildType == null) {
            throw new BuildTypeNotFoundException("Build type with id " + buildTypeId + " does not exist.");
        }

        var project = buildType.getProject();
        var availableConns = connectionsManager.getAvailableConnectionsOfType(project, LarkNotifierProperties.TypeConnection);
        var receiverTypes = receiverTypeProvider.getSelectOptions(user);
        var feature = buildType.findBuildFeatureById(featureId);

        mv.getModel().put("availableConnections", availableConns);
        mv.getModel().put("larkProperties", larkProperties);
        mv.getModel().put("buildTypeId", buildTypeId);
        mv.getModel().put("receiverTypes", receiverTypes);
        mv.getModel().put("createConnectionUrl", webLinks.getEditProjectPageUrl(project.getExternalId()) + "&tab=oauthConnections");

        return mv;
    }
}

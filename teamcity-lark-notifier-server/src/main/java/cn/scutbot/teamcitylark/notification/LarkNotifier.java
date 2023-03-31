package cn.scutbot.teamcitylark.notification;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import cn.scutbot.teamcitylark.LarkNotifierProperties;
import cn.scutbot.teamcitylark.chart.ChartService;
import cn.scutbot.teamcitylark.chart.StatisticsChartImageProvider;
import cn.scutbot.teamcitylark.lark.LarkClientProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.intellij.openapi.diagnostic.Logger;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.serverSide.impl.ServerRootUrlHolder;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.SVcsModification;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.scutbot.teamcitylark.LarkNotifierProperties.format;


@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkNotifier extends NotificatorAdapter {
    private final NotificatorRegistry registry;
    private final LarkClientProvider clientProvider;
    private final LarkNotifierDescriptor descriptor;
    private final ProjectManager projectManager;
    private final LarkNotifierProperties larkProperties;
    private final Logger logger = Loggers.SERVER;
    private final List<UserPropertyInfo> userPropertyInfoList;
    private final ServerRootUrlHolder rootUrlHolder;
    private final StatisticsChartImageProvider chartImageProvider;
    public LarkNotifier(NotificatorRegistry registry, LarkClientProvider clientProvider, LarkNotifierDescriptor descriptor, ProjectManager projectManager, LarkNotifierProperties larkProperties, ServerRootUrlHolder rootUrlHolder, StatisticsChartImageProvider chartImageProvider) {
        this.registry = registry;
        this.clientProvider = clientProvider;
        this.descriptor = descriptor;
        this.projectManager = projectManager;
        this.larkProperties = larkProperties;
        this.rootUrlHolder = rootUrlHolder;
        this.chartImageProvider = chartImageProvider;

        userPropertyInfoList = List.of(
                new UserPropertyInfo(larkProperties.getPropertyConnection().getKey(), "connectionId"),
                new UserPropertyInfo(larkProperties.getPropertyDisplayName().getKey(), "displayName"),
                new UserPropertyInfo(larkProperties.getPropertyReceiverType().getKey(), "receiver type"),
                new UserPropertyInfo(larkProperties.getPropertyReceiverID().getKey(), "receiver id")
        );

        registry.register(this, userPropertyInfoList);
    }

    @NotNull
    @Override
    public String getNotificatorType() {
        return descriptor.getType();
    }

    private void larkNotify(JsonObject content, String messageType, SUser user, SProject project) {
        var display = user.getPropertyValue(larkProperties.getPropertyDisplayName());
        var receiverType = user.getPropertyValue(larkProperties.getPropertyReceiverType());
        var receiverId = user.getPropertyValue(larkProperties.getPropertyReceiverID());
        var connectionId = user.getPropertyValue(larkProperties.getPropertyConnection());
        var client = clientProvider.createClient(project, connectionId);

        if (client != null) {
            try {
                logger.info("Lark notifier: Sending " + content.toString() + " to " + receiverId + "@" + receiverType + ", conn=" + connectionId);
                var resp = client.sendMessage(
                        CreateMessageReceiveIdTypeEnum.valueOf(receiverType),
                        receiverId,
                        messageType,
                        content.toString());
                logger.info(String.valueOf(resp.getCode()));
                logger.info(resp.getMsg());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void larkNotify(SBuild build, Collection<SUser> users, String sourceType) {
        var buildType = build.getBuildType();
        logger.info("Lark wide notifier");
        if (buildType != null) {
            var project = projectManager.findProjectById(buildType.getProjectId());
            if (project != null) {
                for (SUser user : users) {
                    //chartImageProvider.getPerformanceMonImage(build, user);
                    var overriddenParams = user.getPropertyValue(larkProperties.getPropertyOverriddenParams());
                    var var_data = overrideParams(getParamSet(sourceType, user, build), overriddenParams);
                    var image = chartImageProvider.getBuildReportImage(build, user);
                    var_data.addProperty("agent_info", larkImageUpload(image, user, project));

                    var data = new JsonObject();
                    var templateData = new JsonObject();
                    templateData.addProperty("template_id", getMessageTemplateId(sourceType, user));
                    templateData.add("template_variable", var_data);

                    data.addProperty("type", "template");
                    data.add("data", templateData);

                    logger.info("Sending  " + data);
                    larkNotify(data, "interactive", user, project);
                }
            }
        }
    }

    private String larkImageUpload(File image, SUser user, SProject project) {
        var receiverType = user.getPropertyValue(larkProperties.getPropertyReceiverType());
        var receiverId = user.getPropertyValue(larkProperties.getPropertyReceiverID());
        var connectionId = user.getPropertyValue(larkProperties.getPropertyConnection());

        var client = clientProvider.createClient(project, connectionId);
        if (client != null) {
            try {
                var resp = client.uploadImage(image);
                logger.info(resp.getMsg());
                return resp.getData().getImageKey();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return "N/A";
    }

    private String getMessageTemplateId(String type, SUser user) {
        switch (type) {
            case "success": return user.getPropertyValue(larkProperties.getPropertyMessageBuildSuccessTemplateId());
            case "fail": return user.getPropertyValue(larkProperties.getPropertyMessageBuildFailTemplateId());
            case "start": return user.getPropertyValue(larkProperties.getPropertyMessageBuildStartTemplateId());
            default: return null;
        }
    }

    private String getOrEmpty(ParametersProvider provider, String key) {
        var value = provider.get(key);
        return Objects.requireNonNullElse(value, "");
    }

    private JsonObject getParamSet(String type, SUser user, SBuild build) {
        var json = new JsonObject();

        json.addProperty("agent", build.getAgentName());
        json.addProperty("build_id", build.getBuildId() + "");

        var branch = "";
        var trigger = "";
        var tag = "";
        var endpoint = "";
        var pull_command = "";
        var duration = "";
        var log_url = "";
        var artifact_name = "";
        var artifact_url = "";
        var commits = new JsonArray();
        var problems = new JsonArray();

        var buildType = build.getBuildType();
        if (buildType != null) {
            branch = getOrEmpty(build.getParametersProvider(), "branch_tag");
            endpoint = getOrEmpty(build.getParametersProvider(), ("docker.registry.endpoint"));
            pull_command = "docker pull center.sim.scutbot.cn/simulatorx/" + branch + ":" + tag;
            duration = build.getDuration() + "s";
            log_url = String.format("%s/downloadRawMessageFile.html?buildId=%s", rootUrlHolder.getRootUrl(), build.getBuildId());

            var origChannel = build.getParametersProvider().get("channel");
            var origTag = build.getParametersProvider().get("artifact_tag");
            tag = origChannel == null ? (origTag == null ? "" : origTag) : origChannel;

            var buildTrigger = build.getTriggeredBy();
            if (buildTrigger.isTriggeredByUser()) {
                trigger = buildTrigger.getUser().getDescriptiveName();
            } else {
                trigger = buildTrigger.getRawTriggeredBy();
            }

            if (build.isArtifactsExists()) {
                var artifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT);
                if (artifacts.isAvailable()) {
                    var rootArtifact = artifacts.getRootArtifact();
                    BuildArtifact compress = null;

                    if (rootArtifact.isArchive()) {
                        compress = rootArtifact;
                    } else {
                        for (BuildArtifact child : rootArtifact.getChildren()) {
                            if (child.isArchive()) {
                                compress = child;
                                break;
                            }
                        }
                    }

                    if (compress != null) {
                        artifact_name = compress.getName();
                        artifact_url = rootUrlHolder.getRootUrl() + compress.getRelativePath();
                    }
                }
            }

            for (SVcsModification vcsModification : build.getContainingChanges()) {
                var changeNode = new JsonObject();
                changeNode.addProperty("commit", format(vcsModification));
                commits.add(changeNode);
            }

            for (BuildProblemData failureReason : build.getFailureReasons()) {
                var changeNode = new JsonObject();
                changeNode.addProperty("problem", format(failureReason));
                problems.add(changeNode);
            }

        }

        switch (type) {
            case "fail":
            case "success": {
                json.addProperty("time", format(build.getFinishDate()));
                break;
            }
            case "start": {
                json.addProperty("time", format(build.getStartDate()));
                break;
            }
        }

        json.addProperty("branch", branch);
        json.addProperty("trigger", trigger);
        json.addProperty("tag", tag);
        json.addProperty("endpoint", endpoint);
        json.addProperty("pull_command", pull_command);
        json.addProperty("duration", duration);
        json.addProperty("log_url", log_url);
        json.addProperty("artifact_name", artifact_name);
        json.addProperty("artifact_url", artifact_url);
        json.add("commits", commits);
        json.add("problems", problems);

        return json;
    }

    private JsonObject overrideParams(JsonObject origin, String overrider) {
        try {
            if (overrider != null && !overrider.equals("")) {
                var overriderJ = JsonParser.parseString(overrider).getAsJsonObject();

                for (String s : overriderJ.keySet()) {
                    origin.add(s, overriderJ.get(s));
                }
            }
        } catch (Exception ignored) {}

        return origin;
    }

    @Override
    public void notifyBuildStarted(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        logger.info("Notifying start build");
        larkNotify(build, users, "start");
    }

    @Override
    public void notifyBuildSuccessful(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        logger.info("Notifying success build");
        larkNotify(build, users, "success");
    }

    @Override
    public void notifyBuildFailed(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        logger.info("Notifying fail build");
        larkNotify(build, users, "fail");
    }



    /*
    @Override
    public void notifyBuildProbablyHanging(@NotNull SRunningBuild build, @NotNull Set<SUser> users) {
        larkNotify(awa, "text", users, build);
    }

    @Override
    public void notifyBuildProblemsUnmuted(@NotNull Collection<BuildProblemInfo> buildProblems, @NotNull MuteInfo muteInfo, @Nullable SUser user, @NotNull Set<SUser> users) {
        larkNotify(awa, "text", users, build);
    }

    @Override
    public void notifyQueuedBuildWaitingForApproval(@NotNull SQueuedBuild queuedBuild, @NotNull Set<SUser> users) {
        larkNotify(awa, "text", users, build);
    }

     */
}

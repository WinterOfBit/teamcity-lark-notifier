package cn.scutbot.teamcitylark.chart;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Conditional(LarkNotifierEnabled.class)
public class StatisticsChartImageProviderImpl implements StatisticsChartImageProvider {
    private final PluginDescriptor descriptor;
    private final ChartService chartService;

    public StatisticsChartImageProviderImpl(PluginDescriptor descriptor, ChartService chartService) {
        this.descriptor = descriptor;
        this.chartService = chartService;
    }

    @Override
    public File getBuildReportImage(SBuild build, SUser user) {
        var durationSet = new JsonObject();
        durationSet.addProperty("type", "line");
        durationSet.addProperty("label", "Build Duration");
        durationSet.addProperty("borderColor", "rgb(41,85,17)");
        durationSet.addProperty("borderWidth", 2);
        durationSet.addProperty("fill", false);
        durationSet.add("data", new JsonArray());

        var artifactSet = new JsonObject();
        artifactSet.addProperty("type", "line");
        artifactSet.addProperty("label", "Artifact Size");
        artifactSet.addProperty("borderColor", "rgb(54.162.154)");
        artifactSet.addProperty("borderWidth", 2);
        artifactSet.addProperty("fill", false);
        artifactSet.add("data", new JsonArray());

        var successSet = new JsonObject();
        successSet.addProperty("type", "line");
        successSet.addProperty("label", "Build Duration");
        successSet.addProperty("borderColor", "rgb(33,98,244)");
        successSet.addProperty("borderWidth", 2);
        successSet.addProperty("fill", false);
        successSet.add("data", new JsonArray());

        build.getBuildType().getHistory().stream()
                .filter(sFinishedBuild -> sFinishedBuild.getAgent().getId() == build.getAgent().getId())
                .limit(30)
                .forEach(sFinishedBuild -> {
                    durationSet.getAsJsonArray("data").add(sFinishedBuild.getDuration());
                    artifactSet.getAsJsonArray("data").add(sFinishedBuild.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT).getRootArtifact().getSize());
                    successSet.getAsJsonArray("data").add(sFinishedBuild.getFailureReasons().size() == 0 ? 1 : 0);
                });

        var result = new JsonObject();
        result.addProperty("type", "bar");
        result.add("data", new JsonObject());
        result.getAsJsonObject("data").add("datasets", new JsonArray());
        result.getAsJsonObject("data").getAsJsonArray("datasets").add(durationSet);
        result.getAsJsonObject("data").getAsJsonArray("datasets").add(artifactSet);
        result.getAsJsonObject("data").getAsJsonArray("datasets").add(successSet);

        return chartService.drawChartImage(result);
    }

    @Override
    public File getPerformanceMonImage(SBuild build, SUser user) {
        return null;
    }
}

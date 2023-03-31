package cn.scutbot.teamcitylark.chart;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static cn.scutbot.teamcitylark.LarkNotifierProperties.chartFormat;

@Service
@Conditional(LarkNotifierEnabled.class)
public class StatisticsChartImageProviderImpl implements StatisticsChartImageProvider {
    private final PluginDescriptor descriptor;
    private final ChartService chartService;

    private static final JsonObject BuildReportChartOption = JsonParser.parseString("{\"scales\":{\"yAxes\":[{\"id\":\"duration\",\"position\":\"left\",\"display\":true},{\"id\":\"size\",\"position\":\"right\",\"display\":true},{\"id\":\"rate\",\"position\":\"top\",\"display\":true,\"min\":0,\"max\":1}]}}").getAsJsonObject();
    private static final JsonObject PerfMonChartOption = JsonParser.parseString("{\"scales\":{\"yAxes\":[{\"ticks\":{\"min\":0,\"max\":100,\"stepSize\":20}}]}}").getAsJsonObject();

    public StatisticsChartImageProviderImpl(PluginDescriptor descriptor, ChartService chartService) {
        this.descriptor = descriptor;
        this.chartService = chartService;
    }

    @Override
    public File getBuildReportImage(SBuild build, SUser user) {
        var durationSet = new JsonObject();
        var durationSetDataNode = new JsonArray();
        durationSet.addProperty("type", "line");
        durationSet.addProperty("label", "Build Duration");
        durationSet.addProperty("borderColor", "rgb(41,85,17)");
        durationSet.addProperty("borderWidth", 2);
        durationSet.addProperty("fill", false);

        var artifactSet = new JsonObject();
        var artifactSetDataNode = new JsonArray();
        artifactSet.addProperty("type", "line");
        artifactSet.addProperty("label", "Artifact Size");
        artifactSet.addProperty("borderColor", "rgb(54.162.154)");
        artifactSet.addProperty("borderWidth", 2);
        artifactSet.addProperty("fill", false);

        var successSet = new JsonObject();
        var successSetDataNode = new JsonArray();
        successSet.addProperty("type", "line");
        successSet.addProperty("label", "Build Duration");
        successSet.addProperty("borderColor", "rgb(33,98,244)");
        successSet.addProperty("borderWidth", 2);
        successSet.addProperty("fill", false);

        build.getBuildType().getHistory().stream()
                .filter(sFinishedBuild -> sFinishedBuild.getAgent().getId() == build.getAgent().getId())
                .limit(30)
                .forEach(sFinishedBuild -> {
                    durationSetDataNode.add(sFinishedBuild.getDuration());
                    artifactSetDataNode.add(sFinishedBuild.getArtifacts(BuildArtifactsViewMode.VIEW_DEFAULT).getRootArtifact().getSize());
                    successSetDataNode.add(sFinishedBuild.getFailureReasons().size() == 0 ? 1 : 0);
                });
        durationSet.add("data", durationSetDataNode);
        artifactSet.add("data", artifactSetDataNode);
        successSet.add("data", successSetDataNode);

        var result = new JsonObject();
        var dataNode = new JsonObject();
        var datasetsNode = new JsonArray();

        datasetsNode.add(durationSet);
        datasetsNode.add(artifactSet);
        datasetsNode.add(successSet);

        dataNode.add("datasets", datasetsNode);
        result.add("data", dataNode);
        result.addProperty("type", "line");
        result.add("options", BuildReportChartOption);

        return chartService.drawChartImage(result);
    }

    @Override
    public File getPerformanceMonImage(SBuild build, SUser user) {
        var artifactHolder = build.getArtifacts(BuildArtifactsViewMode.VIEW_HIDDEN_ONLY)
                .findArtifact(".teamcity/perfmon/perfmon.csv");

        if (!artifactHolder.isAccessible() || !artifactHolder.isAvailable())
            return null;
        var artifact = artifactHolder.getArtifact();

        try {
            var parser = CSVParser.parse(
                    artifact.getInputStream(),
                    StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT);
            var data = new JsonObject();
            var labels = new JsonArray();
            var datasets = new JsonArray();

            var cpuTimeDataSet = new JsonObject();
            var cpuTimeDataSetDataNode = new JsonArray();
            cpuTimeDataSet.addProperty("type", "line");
            cpuTimeDataSet.addProperty("label", "CPU Time");
            cpuTimeDataSet.addProperty("borderColor", "rgb(41,66,244)");
            cpuTimeDataSet.addProperty("borderWidth", 2);
            cpuTimeDataSet.addProperty("fill", false);

            var diskTimeDataSet = new JsonObject();
            var diskTimeDataSetDataNode = new JsonArray();
            diskTimeDataSet.addProperty("type", "line");
            diskTimeDataSet.addProperty("label", "Disk Time");
            diskTimeDataSet.addProperty("borderColor", "rgb(128,77,12)");
            diskTimeDataSet.addProperty("borderWidth", 2);
            diskTimeDataSet.addProperty("fill", false);

            var memoryDataSet = new JsonObject();
            var memoryDataSetDataNode = new JsonArray();
            memoryDataSet.addProperty("type", "line");
            memoryDataSet.addProperty("label", "Available memory");
            memoryDataSet.addProperty("borderColor", "rgb(78,111,143)");
            memoryDataSet.addProperty("borderWidth", 2);
            memoryDataSet.addProperty("fill", false);

            for (CSVRecord record : parser.getRecords()) {
                System.out.println(record.getRecordNumber());
                System.out.println(record.toString());
                if (record.getRecordNumber() > 1) {
                    var values = record.values();
                    labels.add(chartFormat(Long.valueOf(values[0])));

                    System.out.println("Adding " + values[1]);
                    cpuTimeDataSetDataNode.add(Integer.parseInt(values[1].trim()));
                    diskTimeDataSetDataNode.add(Integer.parseInt(values[2].trim()));
                    memoryDataSetDataNode.add(Integer.parseInt(values[3].trim()));
                }
            }

            cpuTimeDataSet.add("data", cpuTimeDataSetDataNode);
            diskTimeDataSet.add("data", diskTimeDataSetDataNode);
            memoryDataSet.add("data", memoryDataSetDataNode);

            datasets.add(cpuTimeDataSet);
            datasets.add(diskTimeDataSet);
            datasets.add(memoryDataSet);

            data.add("labels", labels);
            data.add("datasets", datasets);

            var chartData = new JsonObject();
            chartData.add("data", data);
            chartData.add("options", PerfMonChartOption);
            chartData.addProperty("type", "line");

            return chartService.drawChartImage(chartData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readString(InputStream is) {
        try (is;
            var os = new ByteArrayOutputStream();) {
            byte[] bytes = new byte[32768];

            while (true) {
                int len = is.read(bytes);
                if (len > 0) {
                    os.write(bytes, 0, len);
                    os.flush();
                } else {
                    return os.toString();
                }
            }
        } catch (Exception e) {
            return "";
        }
    }
}

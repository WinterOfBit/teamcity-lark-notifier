package cn.scutbot.teamcitylark.chart;

import com.google.gson.JsonObject;

import java.io.File;

public interface ChartService {
    File drawChartImage(JsonObject chartData);
}

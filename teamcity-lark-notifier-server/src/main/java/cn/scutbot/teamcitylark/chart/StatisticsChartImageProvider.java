package cn.scutbot.teamcitylark.chart;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.users.SUser;

import java.io.File;

public interface StatisticsChartImageProvider {
    File getBuildReportImage(SBuild build, SUser user);
    File getPerformanceMonImage(SBuild build, SUser user);
}

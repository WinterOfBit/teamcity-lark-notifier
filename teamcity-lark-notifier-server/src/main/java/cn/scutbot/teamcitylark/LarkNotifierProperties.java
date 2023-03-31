package cn.scutbot.teamcitylark;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.PluginTypes;
import jetbrains.buildServer.users.PluginPropertyKey;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsFileModification;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkNotifierProperties {
    public static final String EntryEnabled = "teamcity.internal.notification.LarkNotifier.enable";
    public static final String EntryConnectionsSelectProvider = "connectionSelectOptionsProvider";
    public static final String EntryLarkReceiverTypeProvider = "larkReceiverTypeProvider";
    public static final String EntryConnection = "connection";
    public static final String EntryDisplayName = "displayName";
    public static final String EntryReceiverType = "receiverType";
    public static final String EntryReceiverID = "receiverId";
    public static final String EntryOverriddenParams = "overriddenParams";
    public static final String EntryMessageBuildSuccessTemplateId = "buildSuccessMsgTemplate";
    public static final String EntryMessageBuildStartTemplateId = "buildStartMsgTemplate";
    public static final String EntryMessageBuildFailTemplateId = "buildFailMsgTemplate";

    public static final String TypeNotifier = "LarkNotifier";
    public static final String TypeConnection = "LarkConnection";

    public static final String NameGlobal = "Lark";

    public static final DateFormat DateFormatterGlobal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String format(Date date) {
        return DateFormatterGlobal.format(date);
    }

    public static String format(BuildProblemData failureReason) {
        return failureReason.getType() + ": " + failureReason.getDescription();
    }

    public static String format(SVcsModification vcsModification) {
        var build = new StringBuilder();
        for (VcsFileModification change : vcsModification.getChanges()) {
            build.append(change.getChangeTypeName())
                    .append(": ")
                    .append(change.getRelativeFileName())
                    .append("\n");
        }
        return String.format("**%s**: %s \n%s", vcsModification.getUserName(), vcsModification.getDescription(), build);
    }

    public static PluginPropertyKey getProperty(String key) {
        return new PluginPropertyKey(
                PluginTypes.NOTIFICATOR_PLUGIN_TYPE,
                TypeNotifier,
                key
        );
    }

    public PluginPropertyKey getPropertyConnectionSelectProvider() {
        return getProperty(EntryConnectionsSelectProvider);
    }

    public PluginPropertyKey getPropertyLarkReceiverTypeProvider() {
        return getProperty(EntryLarkReceiverTypeProvider);
    }

    public PluginPropertyKey getPropertyConnection() {
        return getProperty(EntryConnection);
    }

    public PluginPropertyKey getPropertyDisplayName() {
        return getProperty(EntryDisplayName);
    }

    public PluginPropertyKey getPropertyReceiverType() {
        return getProperty(EntryReceiverType);
    }

    public PluginPropertyKey getPropertyReceiverID() {
        return getProperty(EntryReceiverID);
    }

    public PluginPropertyKey getPropertyOverriddenParams() {
        return getProperty(EntryOverriddenParams);
    }

    public PluginPropertyKey getPropertyMessageBuildSuccessTemplateId() {
        return getProperty(EntryMessageBuildSuccessTemplateId);
    }

    public PluginPropertyKey getPropertyMessageBuildFailTemplateId() {
        return getProperty(EntryMessageBuildFailTemplateId);
    }

    public PluginPropertyKey getPropertyMessageBuildStartTemplateId() {
        return getProperty(EntryMessageBuildStartTemplateId);
    }
}

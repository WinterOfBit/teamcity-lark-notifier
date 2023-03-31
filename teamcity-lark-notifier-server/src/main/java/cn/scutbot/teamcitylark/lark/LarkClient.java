package cn.scutbot.teamcitylark.lark;

import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateImageResp;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;

import java.io.File;
import java.util.UUID;

public interface LarkClient {
    Client getClient();

    CreateMessageResp sendMessage(
            CreateMessageReceiveIdTypeEnum receiveIdType,
            String receiveId,
            String messageType,
            String content,
            String uuid
    ) throws Exception;

    CreateImageResp uploadImage(File image) throws Exception;

    default CreateMessageResp sendMessage(
            CreateMessageReceiveIdTypeEnum receiveIdType,
            String receiveId,
            String messageType,
            String content
    ) throws Exception {
        return sendMessage(receiveIdType, receiveId, messageType, content, UUID.randomUUID().toString());
    }

    default CreateMessageResp sendGroupMessage(
            String receiveId,
            String messageType,
            String content
    ) throws Exception {
        return sendMessage(CreateMessageReceiveIdTypeEnum.CHAT_ID, receiveId, messageType, content);
    }

    default void sendPrivateMessage(
            String receiveId,
            String messageType,
            String content
    ) throws Exception {
        sendMessage(CreateMessageReceiveIdTypeEnum.EMAIL, receiveId, messageType, content);
    }
}

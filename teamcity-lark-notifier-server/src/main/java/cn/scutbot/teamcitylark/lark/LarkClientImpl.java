package cn.scutbot.teamcitylark.lark;

import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.enums.CreateImageImageTypeEnum;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.model.*;
import jetbrains.buildServer.log.Loggers;

import java.io.File;

public class LarkClientImpl implements LarkClient {
    private final Client client;

    public LarkClientImpl(String appId, String appSecret, LarkTokenCacher cacher) {
        Loggers.SERVER.info("Creating lark client with " + appId + " " + appSecret);
        client = Client.newBuilder(appId, appSecret).tokenCache(cacher).build();
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public CreateMessageResp sendMessage(CreateMessageReceiveIdTypeEnum receiveIdType, String receiveId, String messageType, String content, String uuid) throws Exception {
        var msgBody = CreateMessageReqBody.newBuilder()
                .receiveId(receiveId)
                .msgType(messageType)
                .content(content)
                .uuid(uuid)
                .build();
        var msgReq = CreateMessageReq.newBuilder()
                .receiveIdType(receiveIdType)
                .createMessageReqBody(msgBody)
                .build();
        return client.im().message().create(msgReq);
    }

    @Override
    public CreateImageResp uploadImage(File image) throws Exception {
        var imgReqBody = CreateImageReqBody.newBuilder()
                .imageType(CreateImageImageTypeEnum.MESSAGE)
                .image(image)
                .build();
        var imgReq = CreateImageReq.newBuilder()
                .createImageReqBody(imgReqBody)
                .build();
        return client.im().image().create(imgReq);
    }
}

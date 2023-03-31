package cn.scutbot.teamcitylark.chart;

import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import com.google.gson.JsonObject;
import jetbrains.buildServer.util.HTTPRequestBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Service
@Conditional(LarkNotifierEnabled.class)
public class QuickChartServiceImpl implements ChartService {
    private final HTTPRequestBuilder.RequestHandler requestHandler;

    public QuickChartServiceImpl(HTTPRequestBuilder.RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    private static final String QuickChartEndpoint = "https://quickchart.io/chart";
    @Override
    public File drawChartImage(JsonObject chartData) {
        var data = new JsonObject();
        data.addProperty("version", 4);
        data.addProperty("format", "png");
        data.add("chart", chartData);

        try {
            File target = File.createTempFile("quick-chart", ".png");
            var request = new HTTPRequestBuilder(QuickChartEndpoint)
                    .withMethod("POST")
                    .withPostStringEntity(
                            data.toString(),
                            "application/json",
                            StandardCharsets.UTF_8)
                    .onSuccess(response -> {
                        try (var fos = new FileOutputStream(target);
                            var is = response.getContentStream()) {
                            assert is != null;
                            fos.write(is.readAllBytes());
                            /*
                            int byteCount, byteWritten = 0;
                            byte[] bytes = new byte[1024];
                            while ((byteCount = is.read(bytes)) != -1) {
                                fos.write(bytes, byteWritten, byteCount);
                                byteWritten += byteCount;
                            }

                             */
                        }
                    }).build();
            requestHandler.doRequest(request);
            return target;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package cn.scutbot.teamcitylark.lark;


import cn.scutbot.teamcitylark.LarkNotifierEnabled;
import com.ctc.wstx.shaded.msv_core.datatype.xsd.datetime.TimeZone;
import com.intellij.openapi.diagnostic.Logger;
import com.lark.oapi.core.cache.ICache;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Conditional(LarkNotifierEnabled.class)
public class LarkTokenCacher implements ICache {
    private final ConcurrentHashMap<String, ExpirableValue> CachePool = new ConcurrentHashMap<>(64);
    private static final Logger logger = Logger.getInstance(LarkTokenCacher.class);

    @Override
    public String get(String key) {
        var ev = CachePool.get(key);
        if (ev == null || ev.expires < Calendar.getInstance().getTimeInMillis()) {
            return "";
        }

        return ev.value;
    }

    @Override
    public void set(String key, String value, int expire, TimeUnit timeUnit) {
        var now = Calendar.getInstance(TimeZone.ZERO);
        now.add(Calendar.MILLISECOND, (int) timeUnit.toMillis(expire));
        var time = now.getTimeInMillis();
        var ev = new ExpirableValue(value, time);
        CachePool.put(key, ev);
    }

    private static class ExpirableValue {
        public final String value;
        public final long expires;

        public ExpirableValue(String value, long expires) {
            this.value = value;
            this.expires = expires;
        }
    }
}

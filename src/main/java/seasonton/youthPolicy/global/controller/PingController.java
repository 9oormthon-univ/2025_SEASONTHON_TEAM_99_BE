package seasonton.youthPolicy.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PingController {

    @RequestMapping(value = "/ping", produces = "application/json")
    @ResponseBody
    public Object healthCheck(){
        Instant start = Instant.now();

        Map<String, Object> map = new HashMap<>();
        map.put("today", ZonedDateTime.now().getMonth() + ":" + ZonedDateTime.now().getDayOfMonth());

        Instant end = Instant.now();
        long latencyMs = Duration.between(start, end).toMillis();
        map.put("ms", latencyMs);

        return map;
    }
}

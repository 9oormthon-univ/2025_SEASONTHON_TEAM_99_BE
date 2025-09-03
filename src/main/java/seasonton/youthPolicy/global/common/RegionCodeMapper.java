package seasonton.youthPolicy.global.common;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class RegionCodeMapper {
    private final Map<String, String> regionMap = new HashMap<>();

    @PostConstruct
    public void loadRegionCodes() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("국토교통부_법정동코드_20250805.csv").getInputStream(),
                        Charset.forName("EUC-KR")))) {

            reader.lines().skip(1).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length >= 3 && !"폐지".equals(parts[2].trim()) && !"1".equals(parts[2].trim())) {
                    String code = parts[0].trim();
                    if (code.length() >= 5) {
                        code = code.substring(0, 5); // 앞 5자리만 사용
                    }
                    String name = parts[1].trim();
                    regionMap.put(code, name);
                }
            });
        }
    }

    public String getRegionName(String code) {
        return regionMap.getOrDefault(code, code);
    }
}



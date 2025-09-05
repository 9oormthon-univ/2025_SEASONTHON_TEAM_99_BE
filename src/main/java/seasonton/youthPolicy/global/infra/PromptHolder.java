package seasonton.youthPolicy.global.infra;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class PromptHolder {
    private final ConcurrentMap<Resource, String> cache = new ConcurrentHashMap<>();

    public String reportPrompt(Resource r) {
        return cache.computeIfAbsent(r, res -> {
            try (var in = res.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
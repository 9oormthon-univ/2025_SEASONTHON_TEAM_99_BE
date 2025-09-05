package seasonton.youthPolicy.global.infra;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import seasonton.youthPolicy.domain.report.dto.perplexityDTO.*;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PerplexityClient {
    private final WebClient webClient;
    private final PromptHolder promptHolder;

    @Value("${perplexity.model}")
    private String model;

    @Value("classpath:templates/chat-start-prompt.st")
    private Resource summarizePr;
    private String systemPrompt;

    @PostConstruct
    public void init() {
        this.systemPrompt = promptHolder.reportPrompt(summarizePr);
    }

    public PerplexityChatResponse summarize(String content) {
        var req = PerplexityChatRequest.builder()
                .model(model)
                .messages(new PerplexityMessage[]{
                        new PerplexityMessage("system", systemPrompt),
                        new PerplexityMessage("user", content)
                })
                // 옵션 설정
                .disable_search(true)
                .max_tokens(1024)
                .temperature(0.3)
                .top_p(0.9)
                .return_citations(false)
                .return_images(false)
                .return_related_questions(false)
                .stream(false)
                .presence_penalty(0.0)
                .frequency_penalty(0.8)
                .build();

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("Perplexity error: " + body))))
                .bodyToMono(PerplexityChatResponse.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))) // 간단 재시도
                .block();
    }

    // WebClient Bean
    @Component
    public static class PerplexityWebClientFactory {
        public PerplexityWebClientFactory(
                WebClient.Builder builder,
                @Value("${perplexity.base-url}") String baseUrl,
                @Value("${perplexity.api-key}") String apiKey,
                @Value("${perplexity.timeout-ms}") long timeoutMs
        ) {
            this.webClient = builder
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .clientConnector(new ReactorClientHttpConnector(
                            HttpClient.create()
                                    .responseTimeout(Duration.ofMillis(timeoutMs))
                    ))
                    .build();
        }
        private final WebClient webClient;
        @Bean
        public WebClient perplexityWebClient() { return webClient; }
    }
}

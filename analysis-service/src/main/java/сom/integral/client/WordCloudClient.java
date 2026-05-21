package сom.integral.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP-клиент для интеграции со сторонним внешним API (QuickChart).
 * <p>
 * Инкапсулирует логику сетевого взаимодействия через {@link org.springframework.web.client.RestTemplate}.
 * Формирует URI с нужными query-параметрами, обрабатывает возможные сетевые сбои
 * и возвращает сгенерированное изображение.
 *
 * @author [Dmitry]
 * @version 1.0
 */
@Slf4j
@Service
public class WordCloudClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.external-api.word-cloud-url}")
    private String wordCloudApiUrl;

    public byte[] generateWordCloud(String text){
        log.info("Отправка запроса на внешнее  API для генерации текста  (длина: {})", text.length());
        try {
            String url = UriComponentsBuilder.fromHttpUrl(wordCloudApiUrl)
                    .queryParam("text", text)
                    .queryParam("width", 500)
                    .queryParam("height", 500)
                    .toUriString();

            return  restTemplate.getForObject(url, byte[].class);
        }
        catch (Exception e){
            log.error("Ошибка не удалось получить ответ: {}", e.getMessage());
            return null;
        }
    }
}

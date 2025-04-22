package com.example.demo.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    protected Logger logger = LogManager.getLogger(getClass());

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final long TIMEOUT = 60 * 1000;
    private final long HEARTBEAT_RATE = TIMEOUT - (5 * 1000L);

    @PreDestroy
    public void cleanUpEmitters() {
        for (SseEmitter emitter : emitters) {
            // 연결 종료
            emitter.complete();
        }

        emitters.clear();
    }

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onCompletion(() -> removeEmitter(emitter));

        emitter.onTimeout(emitter::complete);

        emitter.onError((e) -> {
            logger.error("SseEmitter error {}", e.getMessage());
            emitter.complete();
        });

        emitters.add(emitter);

        System.out.println("Emitter created size --> " + emitters.size());

        return emitter;
    }

    public void broadcast(Map<String, Object> data) {
        String jsonData = null;
        try{
            jsonData = objectMapper.writeValueAsString(data);
        }catch (JsonProcessingException jpe){
            logger.error(jpe.getMessage());
        }

        if(jsonData == null) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("customData")
                        .data(jsonData, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                removeEmitter(emitter);

                logger.error("[broadcast] failed to send data to emitter --> {}", e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = HEARTBEAT_RATE)
    public void heartbeat(){

        if(emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("ping").reconnectTime(5000));
            } catch (Exception e) {
                logger.error("[heartbeat] failed to send data to emitter --> {}", e.getMessage());
                removeEmitter(emitter);
            }
        }
    }

    private void removeEmitter(SseEmitter emitter) {
        logger.info("[Before] emitter size --> {}", emitters.size());
        emitters.remove(emitter);
        logger.info("[After] emitter size --> {}", emitters.size());
    }
}

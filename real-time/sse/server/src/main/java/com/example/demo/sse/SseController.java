package com.example.demo.sse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping(value="/sse/*")
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping(value="/subscribe", produces = "text/event-stream;charset=UTF-8")
    public ResponseEntity<SseEmitter> subscribe() {
        return ResponseEntity.ok(sseService.createEmitter());
    }

    @PostMapping("/broadcast")
    public ResponseEntity<Void> broadcast(@RequestBody Map<String, Object> data) {
        sseService.broadcast(data);

        return ResponseEntity.ok().build();
    }
}
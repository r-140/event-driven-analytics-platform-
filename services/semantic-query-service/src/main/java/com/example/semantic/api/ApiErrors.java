package com.example.semantic.api;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.Map;
@RestControllerAdvice public class ApiErrors {@ExceptionHandler({IllegalArgumentException.class}) ResponseEntity<Map<String,Object>> badRequest(Exception e){return ResponseEntity.badRequest().body(Map.of("timestamp",Instant.now().toString(),"error",e.getMessage()));}}

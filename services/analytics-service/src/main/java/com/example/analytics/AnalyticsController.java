package com.example.analytics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/analytics") public class AnalyticsController { private final JdbcClient jdbc; public AnalyticsController(JdbcClient jdbc){this.jdbc=jdbc;} @GetMapping("/customers-by-country") public List<Map<String,Object>> summary(){return jdbc.sql("select country_code,customer_count,refreshed_at from gold.customer_summary order by customer_count desc").query().listOfRows();} }

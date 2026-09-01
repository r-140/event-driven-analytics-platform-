package com.example.semantic.query;

import io.micrometer.core.instrument.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class QueryExecutor {
    private final QueryCompiler compiler;
    private final NamedParameterJdbcTemplate jdbc;
    private final MeterRegistry meters;
    private final int maxRows;

    public QueryExecutor(QueryCompiler compiler, NamedParameterJdbcTemplate jdbc, MeterRegistry meters,
                         @Value("${semantic.query-timeout-seconds:10}") int timeout,
                         @Value("${semantic.max-rows:5000}") int maxRows) {
        this.compiler=compiler; this.jdbc=jdbc; this.meters=meters; this.maxRows=maxRows;
        jdbc.getJdbcTemplate().setQueryTimeout(timeout);
    }

    public Result execute(SemanticQuery request) {
        if (request.limit()>maxRows) { reject("limit"); throw new IllegalArgumentException("Limit exceeds "+maxRows); }
        final CompiledQuery compiled;
        try { compiled=compiler.compile(request); }
        catch (IllegalArgumentException e) { reject("catalog_validation"); throw e; }
        long start=System.nanoTime();
        try {
            var rows=jdbc.queryForList(compiled.sql(),compiled.parameters());
            meters.counter("semantic_queries_total","model",request.model(),"status","success").increment();
            DistributionSummary.builder("semantic_query_rows").tag("model",request.model()).register(meters).record(rows.size());
            return new Result(compiled.columns(),rows,new Metadata(Duration.ofNanos(System.nanoTime()-start).toMillis(),rows.size(),compiled.sql(),"1.0.0",Instant.now()));
        } catch (RuntimeException e) {
            meters.counter("semantic_queries_total","model",request.model(),"status","error").increment();
            throw e;
        } finally {
            Timer.builder("semantic_query_duration").tag("model",request.model()).publishPercentileHistogram().register(meters).record(System.nanoTime()-start,TimeUnit.NANOSECONDS);
        }
    }
    private void reject(String reason){meters.counter("semantic_query_rejections_total","reason",reason).increment();}
    public record Result(List<CompiledQuery.Column> columns,List<Map<String,Object>> rows,Metadata metadata){}
    public record Metadata(long executionTimeMs,int rowCount,String generatedSql,String semanticModelVersion,Instant servedAt){}
}

package com.example.semantic.query;
import java.util.List; import java.util.Map;
public record CompiledQuery(String sql,Map<String,Object> parameters,List<Column> columns){public record Column(String name,String label,String type) {}}

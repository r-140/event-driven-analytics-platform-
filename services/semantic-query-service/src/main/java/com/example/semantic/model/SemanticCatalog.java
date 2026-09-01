package com.example.semantic.model;
import java.util.List;
public record SemanticCatalog(String version,List<Model> models) {
 public record Model(String name,String label,String source,String primaryKey,List<Dimension> dimensions,List<Metric> metrics) {}
 public record Dimension(String name,String label,String column,String type) {}
 public record Metric(String name,String label,String expression,String type) {}
}

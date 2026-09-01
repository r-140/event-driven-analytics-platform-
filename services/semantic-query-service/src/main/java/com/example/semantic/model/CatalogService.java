package com.example.semantic.model;
import org.springframework.core.io.ClassPathResource; import org.springframework.stereotype.Service; import tools.jackson.databind.ObjectMapper; import java.io.IOException;
@Service public class CatalogService {
 private final SemanticCatalog catalog;
 public CatalogService(ObjectMapper mapper) throws IOException {try(var in=new ClassPathResource("semantic-catalog.json").getInputStream()){catalog=mapper.readValue(in,SemanticCatalog.class);} validate();}
 public SemanticCatalog catalog(){return catalog;}
 public SemanticCatalog.Model model(String name){return catalog.models().stream().filter(m->m.name().equals(name)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown semantic model: "+name));}
 private void validate(){for(var m:catalog.models()){safe(m.source());for(var d:m.dimensions())safe(d.column());}}
 private void safe(String value){if(!value.matches("[a-z_][a-z0-9_.]*"))throw new IllegalStateException("Unsafe catalog identifier: "+value);}
}

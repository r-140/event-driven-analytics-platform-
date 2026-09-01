package com.example.semantic.api;
import com.example.semantic.model.*; import com.example.semantic.query.*; import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/semantic") @CrossOrigin(origins={"http://localhost:5173","http://localhost:8090"}) public class SemanticController {
 private final CatalogService catalog;private final QueryExecutor executor;public SemanticController(CatalogService catalog,QueryExecutor executor){this.catalog=catalog;this.executor=executor;}
 @GetMapping("/catalog") public SemanticCatalog catalog(){return catalog.catalog();}
 @PostMapping("/query") public QueryExecutor.Result query(@Valid @RequestBody SemanticQuery query){return executor.execute(query);}
 @GetMapping("/reports") public List<SavedReport> reports(){return List.of(new SavedReport("customers-by-country","Customers by country","customer",List.of("customer_count"),List.of("country"),null),new SavedReport("invoice-value-by-month","Invoice value by month","invoice",List.of("issued_amount","adjusted_amount","net_amount"),List.of("currency"),new SemanticQuery.TimeDimension("issued_at","month")),new SavedReport("payments-by-month","Payments by month","payment",List.of("payment_count","received_amount"),List.of("currency"),new SemanticQuery.TimeDimension("received_at","month")));}
 public record SavedReport(String id,String name,String model,List<String> metrics,List<String> dimensions,SemanticQuery.TimeDimension timeDimension){}
}

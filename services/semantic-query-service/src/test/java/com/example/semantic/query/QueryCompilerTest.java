package com.example.semantic.query;
import com.example.semantic.model.CatalogService; import org.junit.jupiter.api.Test; import tools.jackson.databind.ObjectMapper; import java.util.List; import static org.assertj.core.api.Assertions.*;
class QueryCompilerTest {
 private final QueryCompiler compiler=new QueryCompiler(uncheckedCatalog());
 @Test void compilesGovernedMetricWithTimeGrainAndParameterizedFilter(){var query=new SemanticQuery("invoice",List.of("net_amount"),List.of("currency"),new SemanticQuery.TimeDimension("issued_at","month"),List.of(new SemanticQuery.Filter("currency","in",List.of("EUR","USD"))),List.of(new SemanticQuery.Order("issued_at_month","asc")),100);var result=compiler.compile(query);assertThat(result.sql()).contains("sum(f.net_amount) as net_amount","date_trunc('month',f.issued_at)","f.currency in (:p0)","limit :resultLimit").doesNotContain("EUR","USD");assertThat(result.parameters()).containsEntry("p0",List.of("EUR","USD")).containsEntry("resultLimit",100);}
 @Test void rejectsUnknownFields(){var query=new SemanticQuery("invoice",List.of("drop_table"),List.of(),null,List.of(),List.of(),10);assertThatThrownBy(()->compiler.compile(query)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unknown metric");}
 private static CatalogService uncheckedCatalog(){try{return new CatalogService(new ObjectMapper());}catch(Exception e){throw new RuntimeException(e);}}
}

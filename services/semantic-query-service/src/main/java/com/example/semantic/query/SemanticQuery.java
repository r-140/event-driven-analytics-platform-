package com.example.semantic.query;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.util.List;
public record SemanticQuery(@NotBlank String model,@NotEmpty List<@NotBlank String> metrics,List<@NotBlank String> dimensions,@Valid TimeDimension timeDimension,List<@Valid Filter> filters,List<@Valid Order> orderBy,@Min(1) @Max(5000) Integer limit) {
 public SemanticQuery {dimensions=dimensions==null?List.of():List.copyOf(dimensions);filters=filters==null?List.of():List.copyOf(filters);orderBy=orderBy==null?List.of():List.copyOf(orderBy);limit=limit==null?1000:limit;}
 public record TimeDimension(@NotBlank String name,@Pattern(regexp="day|week|month|quarter|year") String grain){}
 public record Filter(@NotBlank String dimension,@Pattern(regexp="eq|in|gte|lte") String operator,@NotEmpty List<String> values){}
 public record Order(@NotBlank String field,@Pattern(regexp="asc|desc",flags=Pattern.Flag.CASE_INSENSITIVE) String direction){}
}

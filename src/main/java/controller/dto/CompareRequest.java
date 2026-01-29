package controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompareRequest {
    @NotEmpty(message = "At least one SQL query is required")
    @Size(max = 5, message = "Maximum 5 queries can be compared at once")
    private List<String> queries;
}

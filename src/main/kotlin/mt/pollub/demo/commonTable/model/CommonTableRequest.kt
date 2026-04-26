package mt.pollub.demo.commonTable.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CommonTableRequest(
    @field:NotBlank(message = "tableKey is required")
    val tableKey: String,

    @field:Min(value = 0, message = "page must be >= 0")
    val page: Int = 0,

    @field:Min(value = 1, message = "pageSize must be >= 1")
    @field:Max(value = 200, message = "pageSize must be <= 200")
    val pageSize: Int = 20,

    val sortBy: String? = null,
    val sortDirection: SortDirection? = SortDirection.DESC,
    val filters: Map<String, String>? = emptyMap(),
    val sorts: List<SortRule>? = emptyList(),
    val filterRules: List<FilterRule>? = emptyList()
)

data class SortRule(
    val field: String,
    val direction: SortDirection? = SortDirection.DESC
)

data class FilterRule(
    val field: String,
    val operator: FilterOperator? = FilterOperator.EQ,
    val value: String? = null,
    val values: List<String>? = emptyList(),
    val from: String? = null,
    val to: String? = null
)

enum class FilterOperator {
    EQ,
    CONTAINS,
    IN,
    BETWEEN,
    GTE,
    LTE
}

enum class SortDirection {
    ASC,
    DESC
}

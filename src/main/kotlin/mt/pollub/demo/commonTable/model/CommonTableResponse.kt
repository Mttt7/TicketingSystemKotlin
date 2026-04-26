package mt.pollub.demo.commonTable.model

data class CommonTableResponse(
    val tableKey: String,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val columns: List<CommonTableColumnMeta>,
    val rows: List<Map<String, Any?>>
)

data class CommonTableColumnMeta(
    val key: String,
    val displayName: String,
    val path: String,
    val sortable: Boolean,
    val filterable: Boolean
)


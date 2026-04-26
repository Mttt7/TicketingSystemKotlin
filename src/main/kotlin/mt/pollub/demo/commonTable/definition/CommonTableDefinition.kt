package mt.pollub.demo.commonTable.definition

import mt.pollub.demo.commonTable.model.CommonTableColumnMeta
import mt.pollub.demo.commonTable.model.CommonTableRequest
import mt.pollub.demo.commonTable.model.CommonTableResponse
import mt.pollub.demo.commonTable.model.SortDirection
import mt.pollub.demo.commonTable.model.SortRule
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

interface CommonTableDefinition {
    val tableKey: String
    fun handle(request: CommonTableRequest): CommonTableResponse
}

abstract class BaseCommonTableDefinition<T : Any> : CommonTableDefinition {

    protected abstract fun columns(): List<ColumnDefinition<T>>
    protected abstract fun defaultSortPath(): String
    protected abstract fun findPage(request: CommonTableRequest, pageable: Pageable): Page<T>

    override fun handle(request: CommonTableRequest): CommonTableResponse {
        val tableColumns = columns()
        val pageable = buildPageable(request, tableColumns)
        val page = findPage(request, pageable)

        return CommonTableResponse(
            tableKey = tableKey,
            page = page.number,
            pageSize = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            columns = tableColumns.map {
                CommonTableColumnMeta(
                    key = it.key,
                    displayName = it.displayName,
                    path = it.path,
                    sortable = it.sortable,
                    filterable = it.filterable
                )
            },
            rows = page.content.map { row ->
                tableColumns.associate { col -> col.key to col.valueExtractor(row) }
            }
        )
    }

    private fun buildPageable(
        request: CommonTableRequest,
        tableColumns: List<ColumnDefinition<T>>
    ): Pageable {
        val sortable = tableColumns.filter { it.sortable }
        val keyToPath = sortable.associate { it.key to it.path }
        val pathSet = sortable.map { it.path }.toSet()

        val sort = buildSortFromRules(request.sorts.orEmpty(), keyToPath, pathSet)
            ?: buildSortFromLegacy(request, keyToPath, pathSet)
            ?: Sort.by(Sort.Direction.DESC, defaultSortPath())

        return PageRequest.of(request.page, request.pageSize, sort)
    }

    private fun buildSortFromRules(
        rules: List<SortRule>,
        keyToPath: Map<String, String>,
        pathSet: Set<String>
    ): Sort? {
        val orders = rules.mapNotNull { rule ->
            val path = resolveSortPath(rule.field, keyToPath, pathSet) ?: return@mapNotNull null
            val direction = if ((rule.direction ?: SortDirection.DESC) == SortDirection.ASC) {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
            Sort.Order(direction, path)
        }
        return if (orders.isEmpty()) null else Sort.by(orders)
    }

    private fun buildSortFromLegacy(
        request: CommonTableRequest,
        keyToPath: Map<String, String>,
        pathSet: Set<String>
    ): Sort? {
        val field = request.sortBy ?: return null
        val path = resolveSortPath(field, keyToPath, pathSet) ?: return null
        val direction = if ((request.sortDirection ?: SortDirection.DESC) == SortDirection.ASC) {
            Sort.Direction.ASC
        } else {
            Sort.Direction.DESC
        }
        return Sort.by(direction, path)
    }

    private fun resolveSortPath(
        field: String,
        keyToPath: Map<String, String>,
        pathSet: Set<String>
    ): String? {
        return when {
            keyToPath.containsKey(field) -> keyToPath[field]
            pathSet.contains(field) -> field
            else -> null
        }
    }
}

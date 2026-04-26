package mt.pollub.demo.commonTable

import mt.pollub.demo.commonTable.definition.CommonTableDefinition
import mt.pollub.demo.commonTable.model.CommonTableRequest
import mt.pollub.demo.commonTable.model.CommonTableResponse
import mt.pollub.demo.shared.exception.NotFoundException
import org.springframework.stereotype.Service

@Service
class TableFacade(definitions: List<CommonTableDefinition>) {

    private val byKey: Map<String, CommonTableDefinition> = definitions.associateBy { it.tableKey.lowercase() }

    fun handle(request: CommonTableRequest): CommonTableResponse {
        val definition = byKey[request.tableKey.lowercase()]
            ?: throw NotFoundException("Table definition not found for key: ${request.tableKey}")
        return definition.handle(request)
    }
}


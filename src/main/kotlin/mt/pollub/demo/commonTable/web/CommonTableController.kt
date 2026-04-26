package mt.pollub.demo.commonTable.web

import jakarta.validation.Valid
import mt.pollub.demo.commonTable.TableFacade
import mt.pollub.demo.commonTable.model.CommonTableRequest
import mt.pollub.demo.commonTable.model.CommonTableResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/common-table")
class CommonTableController(
    private val tableFacade: TableFacade
) {
    @PostMapping
    fun getTable(@RequestBody @Valid request: CommonTableRequest): ResponseEntity<CommonTableResponse> {
        return ResponseEntity.ok(tableFacade.handle(request))
    }
}


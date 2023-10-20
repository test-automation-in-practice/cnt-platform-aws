package example.aws.s3.api

import example.aws.s3.domain.BookCoverData
import example.aws.s3.domain.BookCoverService
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Simple REST controller that allows to either GET or PUT a cover for a specific book.
 */
@RestController
class BookCoverController(
    private val bookCoverService: BookCoverService
) {

    @GetMapping("/books/{id}/cover")
    fun getCover(@PathVariable("id") id: UUID): ResponseEntity<Resource> {
        val coverData: BookCoverData = bookCoverService.findCover(id)
            ?: return notFound().build()

        return ok()
            .contentLength(coverData.contentLength)
            .contentType(MediaType.parseMediaType(coverData.contentType))
            .body(InputStreamResource(coverData.byteStream))
    }

    @ResponseStatus(CREATED)
    @PutMapping("/books/{id}/cover")
    fun putCover(
        @PathVariable("id") id: UUID,
        @RequestParam("cover") cover: MultipartFile,
    ) {
        bookCoverService.saveCover(id, cover)
    }

}
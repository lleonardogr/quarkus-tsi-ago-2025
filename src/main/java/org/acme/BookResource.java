package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.vertx.core.cli.annotations.Summary;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/books")
@Tag(name = "Books", description = "Book management operations")
public class BookResource {

    @Context
    UriInfo uriInfo;

    private BookRepresentation rep(Book b){
        return BookRepresentation.from(b, uriInfo);
    }

    private List<BookRepresentation> repList(List<Book> books){
        return books.stream().map(this::rep).collect(Collectors.toList());
    }


    @GET
    @Operation(
        summary = "List all books",
        description = "Retrieves a complete list of all books in the catalog"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved list of books",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Book.class, type = SchemaType.ARRAY),
                examples = @ExampleObject(
                    name = "Book list",
                    value = "[{\"id\":1,\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":true,\"links\":{\"self\":\"/books/1\"}}]"
                )
            )
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    public Response getAll(){
        return Response.ok(repList(Book.listAll())).build();
    }



    @GET
    @Path("{id}")
    @Operation(
        summary = "Get book by ID",
        description = "Retrieves a specific book by its unique identifier"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved the book",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookRepresentation.class),
                examples = @ExampleObject(
                    name = "Book details",
                    value = "{\"id\":1,\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":true,\"links\":{\"self\":\"/books/1\",\"all\":\"/books\"}}"
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found with the provided ID",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    public Response getById(
            @Parameter(description = "Unique identifier of the book", required = true, example = "1")
            @PathParam("id") long id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(
        summary = "Search and filter books",
        description = "Search books by title, author, or publisher with pagination and sorting capabilities"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved search results",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SearchBookResponse.class),
                examples = @ExampleObject(
                    name = "Search results",
                    value = "{\"items\":[{\"id\":1,\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\"}],\"page\":1,\"size\":10,\"total\":1,\"totalPages\":1}"
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid parameters",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    public Response search(
            @Parameter(description = "Search query for title, author, or publisher", example = "Clean Code")
            @QueryParam("q") String q,
            @Parameter(description = "Field to sort by (id, titulo, autor, editora, anoLancamento, estaDisponivel)", example = "titulo")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @Parameter(description = "Page number (1-based)", example = "1")
            @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @QueryParam("size") @DefaultValue("10") int size) {

        Set<String> allowed = Set.of("id","titulo","autor","editora","anoLancamento","estaDisponivel");
        if (!allowed.contains(sort)) {
            sort = "id";
        }

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Book> query = (q == null || q.isBlank())
                ? Book.findAll(sortObj)
                : Book.find("lower(titulo) like ?1 or lower(autor) like ?1 or lower(editora) like ?1",
                            sortObj,
                            "%" + q.toLowerCase() + "%");

        long totalElements = query.count();
        long totalPages = (long) Math.ceil((double) totalElements / size);
        
        List<Book> books = query.page(effectivePage, size).list();
        
        SearchBookResponse response = SearchBookResponse.from(
            books, uriInfo, q, sort, direction, page, size, totalElements, totalPages
        );
        
        return Response.ok(response).build();
    }

    @POST
    @Operation(
        summary = "Create a new book",
        description = "Creates a new book entry in the catalog. Supports idempotency via the Idempotency-Key header to prevent duplicate requests. " +
                      "Include a unique Idempotency-Key header (e.g., UUID) to prevent duplicate book creation if the same request is sent multiple times. " +
                      "Cached responses are stored for 24 hours."
    )
    @RequestBody(
        required = true,
        description = "Book object to be created",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Book.class),
            examples = @ExampleObject(
                name = "New book",
                value = "{\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":true}"
            )
        )
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "201",
            description = "Book successfully created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookRepresentation.class),
                examples = @ExampleObject(
                    name = "Created book",
                    value = "{\"id\":1,\"titulo\":\"Clean Code\",\"autor\":\"Robert Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":true,\"links\":{\"self\":\"/books/1\",\"all\":\"/books\"}}"
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid book data",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "409",
            description = "Conflict - request with this Idempotency-Key is currently being processed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request with this idempotency key is currently being processed\"}")
            )
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    @Transactional
    public Response insert(Book book){
        Book.persist(book);
        return Response.status(201).entity(rep(book)).build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    @Operation(
        summary = "Delete a book",
        description = "Deletes a book from the catalog by its unique identifier"
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "204",
            description = "Book successfully deleted - no content returned"
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found with the provided ID",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    public Response delete(
            @Parameter(description = "Unique identifier of the book to delete", required = true, example = "1")
            @PathParam("id") long id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();

        Book.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("{id}")
    @Operation(
        summary = "Update an existing book",
        description = "Updates all fields of an existing book identified by its unique ID"
    )
    @RequestBody(
        required = true,
        description = "Updated book object with new values",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Book.class),
            examples = @ExampleObject(
                name = "Update book",
                value = "{\"titulo\":\"Clean Code - Updated\",\"autor\":\"Robert C. Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":false}"
            )
        )
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Book successfully updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookRepresentation.class),
                examples = @ExampleObject(
                    name = "Updated book",
                    value = "{\"id\":1,\"titulo\":\"Clean Code - Updated\",\"autor\":\"Robert C. Martin\",\"editora\":\"Prentice Hall\",\"anoLancamento\":2008,\"estaDisponivel\":false,\"links\":{\"self\":\"/books/1\",\"all\":\"/books\"}}"
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid book data",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "404",
            description = "Book not found with the provided ID",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Too many requests. Please try again later.\"}")
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        ),
        @APIResponse(
            responseCode = "504",
            description = "Gateway timeout - request took too long to process",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"error\": \"Request timeout\"}")
            )
        )
    })
    public Response update(
            @Parameter(description = "Unique identifier of the book to update", required = true, example = "1")
            @PathParam("id") long id,
            Book newBook){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();

        entity.titulo = newBook.titulo;
        entity.autor = newBook.autor;
        entity.editora = newBook.editora;
        entity.anoLancamento = newBook.anoLancamento;
        entity.estaDisponivel = newBook.estaDisponivel;

        return Response.status(200).entity(rep(entity)).build();
    }
}

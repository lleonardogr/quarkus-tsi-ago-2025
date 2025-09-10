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
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/books")
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
        summary = "Retorna todos os livros",
        description = "Retorna uma lista de livros por padrão no formato JSON"
    )
    @APIResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Book.class, type = SchemaType.ARRAY)
        )
    )
    public Response getAll(){
        return Response.ok(repList(Book.listAll())).build();
    }



    @GET
    @Path("{id}")
    public Response getById(
            @Parameter(description = "Id do livro a ser pesquisado", required = true)
            @PathParam("id") long id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();
        return Response.ok(rep(entity)).build();
    }

    @GET
    @Path("/search")
    public Response search(
            @Parameter(description = "Query de busca por titulo ou autor ou editora")
            @QueryParam("q") String q,
            @Parameter(description = "Campo de ordenação da lista de retorno")
            @QueryParam("sort") @DefaultValue("id") String sort,
            @Parameter(description = "Direção da ordenação ascendente/descendente")
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("1") int page,
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
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)
            )
    )
    @APIResponse(
        responseCode = "201",
        description = "Created",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Book.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Bad Request",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class))
    )
    @Transactional
    public Response insert(Book book){
        Book.persist(book);
        return Response.status(201).entity(rep(book)).build();
    }

    @DELETE
    @Transactional
    @Path("{id}")
    public Response delete(@PathParam("id") long id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();

        Book.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("{id}")
    public Response update(@PathParam("id") long id, Book newBook){
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

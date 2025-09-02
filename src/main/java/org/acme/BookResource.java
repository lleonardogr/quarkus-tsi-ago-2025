package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/books")
public class BookResource {


    @GET
    public Response getAll(){
        return Response.ok(Book.listAll()).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") long id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();
        return Response.ok(entity).build();
    }

    @GET
    @Path("/search")
    public Response search(
            @QueryParam("q") String q,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("direction") @DefaultValue("asc") String direction,
            @QueryParam("page") @DefaultValue("0") int page,
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

        List<Book> books = query.page(effectivePage, size).list();
        return Response.ok(books).build();
    }

    @POST
    @Transactional
    public Response insert(Book book){
        Book.persist(book);
        return Response.status(201).entity(book).build();
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

        return Response.status(200).entity((entity)).build();
    }
}

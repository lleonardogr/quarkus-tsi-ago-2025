package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/books")
public class BookResource {

    @GET
    public Response getAll(){
        return Response.ok(Book.listAll()).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") int id){
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

        String searchTerm = q == null ? "%" : "%" + q.toLowerCase() + "%";
        String sortParam = sort + " " + ("desc".equalsIgnoreCase(direction) ? "desc" : "asc");
        String query = "lower(titulo) like ?1 or lower(autor) like ?1 or lower(editora) like ?1";

        var books = Book.find(query, sortParam, searchTerm)
                        .page(page, size)
                        .list();

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
    public Response delete(@PathParam("id") int id){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();

        Book.deleteById(id);
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("{id}")
    public Response update(@PathParam("id") int id, Book newBook){
        Book entity = Book.findById(id);
        if(entity == null)
            return Response.status(404).build();

        entity.titulo = newBook.titulo;
        entity.autor = newBook.autor;
        entity.editora = newBook.editora;
        entity.anoLancamento = newBook.anoLancamento;
        entity.estaDisponivel = newBook.estaDisponivel;

        return Response.status(200).entity(entity).build();
    }
}

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
    public Response search(@QueryParam("title") String title,
                           @QueryParam("sort") String sort,
                           @QueryParam("direction") String direction){
        var books = Book.find("titulo = ?1", title);
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

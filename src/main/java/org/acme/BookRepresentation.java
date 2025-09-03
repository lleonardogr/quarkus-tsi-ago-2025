package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BookRepresentation {
    public Long id;
    public String titulo;
    public String autor;
    public String editora;
    public int anoLancamento;
    public boolean estaDisponivel;
    public Map<String, String> _links;

    public BookRepresentation() {
    }

    public static BookRepresentation from(Book book, UriInfo uriInfo) {
        BookRepresentation rep = new BookRepresentation();
        rep.id = book.id;
        rep.titulo = book.titulo;
        rep.autor = book.autor;
        rep.editora = book.editora;
        rep.anoLancamento = book.anoLancamento;
        rep.estaDisponivel = book.estaDisponivel;
        
        rep._links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();
        
        rep._links.put("self", baseUri + "books/" + book.id);
        rep._links.put("all", baseUri + "books");
        rep._links.put("delete", baseUri + "books/" + book.id);
        rep._links.put("update", baseUri + "books/" + book.id);
        rep._links.put("search", baseUri + "books/search");
        
        return rep;
    }
}
package org.acme;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

public class BookRepresentation {
    public Long id;
    public String titulo;
    public String autor;
    public String editora;
    public int anoLancamento;
    public boolean estaDisponivel;

    @JsonProperty("_links")
    public Map<String, String> links = new HashMap<>();

    public static BookRepresentation from(Book book, UriInfo uriInfo) {
        BookRepresentation rep = new BookRepresentation();
        rep.id = book.id;
        rep.titulo = book.titulo;
        rep.autor = book.autor;
        rep.editora = book.editora;
        rep.anoLancamento = book.anoLancamento;
        rep.estaDisponivel = book.estaDisponivel;

        String base = uriInfo.getBaseUriBuilder().path(BookResource.class).build().toString();
        String self = uriInfo.getBaseUriBuilder().path(BookResource.class).path(String.valueOf(book.id)).build().toString();
        rep.links.put("self", self);
        rep.links.put("all", base);
        rep.links.put("delete", self);
        rep.links.put("update", self);
        rep.links.put("search", base + "/search");
        return rep;
    }
}


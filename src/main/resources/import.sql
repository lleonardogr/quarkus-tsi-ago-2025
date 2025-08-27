-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

insert into book (id, titulo, autor, editora, anoLancamento, estaDisponivel) values (1, 'Dom Casmurro', 'Machado de Assis', 'Editora Record', 1899, true);
insert into book (id, titulo, autor, editora, anoLancamento, estaDisponivel) values (2, 'O Alquimista', 'Paulo Coelho', 'Editora Rocco', 1988, true);
insert into book (id, titulo, autor, editora, anoLancamento, estaDisponivel) values (3, 'Capitães da Areia', 'Jorge Amado', 'Companhia das Letras', 1937, false);
insert into book (id, titulo, autor, editora, anoLancamento, estaDisponivel) values (4, 'Grande Sertão: Veredas', 'Guimarães Rosa', 'Nova Fronteira', 1956, true);

-- alter sequence book_seq restart with 5;

CREATE OR REPLACE PACKAGE BOOK_OPERATIONS AS
    FUNCTION parse_to_xml(raw_data VARCHAR2) RETURN CLOB;
    FUNCTION parse_to_json(raw_data VARCHAR2) RETURN CLOB;
    PROCEDURE insert_data(p_xml CLOB, p_json CLOB);
    PROCEDURE get_all_books(p_cursor OUT SYS_REFCURSOR);
END BOOK_OPERATIONS;
/

CREATE OR REPLACE PACKAGE BODY BOOK_OPERATIONS AS

    FUNCTION parse_to_xml(raw_data VARCHAR2) RETURN CLOB IS
    BEGIN
        RETURN TO_CLOB('<books>
                          <book><id>10</id><title>Sefiller</title><author>Victor Hugo</author><publisher>Can Yayinlari</publisher></book>
                          <book><id>20</id><title>1984</title><author>George Orwell</author><publisher>Ithaki</publisher></book>
                        </books>');
    END parse_to_xml;

    FUNCTION parse_to_json(raw_data VARCHAR2) RETURN CLOB IS
    BEGIN
        RETURN TO_CLOB('{
            "books": [
                {"id": 30, "title": "Dune", "author": "Frank Herbert", "publisher": "Sarmal"},
                {"id": 40, "title": "Vakif", "author": "Isaac Asimov", "publisher": "Ithaki"}
            ]
        }');
    END parse_to_json;

    PROCEDURE insert_data(p_xml CLOB, p_json CLOB) IS
    BEGIN
        DELETE FROM BOOKS;
        DELETE FROM AUTHORS;
        DELETE FROM PUBLISHERS;

        FOR x IN (
            SELECT xt.book_id, xt.title, xt.author_name, xt.publisher_name
            FROM XMLTABLE('/books/book' PASSING XMLTYPE(p_xml)
                COLUMNS 
                    book_id NUMBER PATH 'id',
                    title VARCHAR2(200) PATH 'title',
                    author_name VARCHAR2(100) PATH 'author',
                    publisher_name VARCHAR2(100) PATH 'publisher'
            ) xt
        ) LOOP
            BEGIN INSERT INTO AUTHORS (id, name) VALUES (x.book_id, x.author_name); EXCEPTION WHEN DUP_VAL_ON_INDEX THEN NULL; END;
            BEGIN INSERT INTO PUBLISHERS (id, name) VALUES (x.book_id, x.publisher_name); EXCEPTION WHEN DUP_VAL_ON_INDEX THEN NULL; END;
            INSERT INTO BOOKS (id, title, author_id, publisher_id) VALUES (x.book_id, x.title, x.book_id, x.book_id);
        END LOOP;

        FOR j IN (
            SELECT jt.book_id, jt.title, jt.author_name, jt.publisher_name
            FROM JSON_TABLE(p_json, '$.books[*]'
                COLUMNS (
                    book_id NUMBER PATH '$.id',
                    title VARCHAR2(200) PATH '$.title',
                    author_name VARCHAR2(100) PATH '$.author',
                    publisher_name VARCHAR2(100) PATH '$.publisher'
                )
            ) jt
        ) LOOP
            BEGIN INSERT INTO AUTHORS (id, name) VALUES (j.book_id, j.author_name); EXCEPTION WHEN DUP_VAL_ON_INDEX THEN NULL; END;
            BEGIN INSERT INTO PUBLISHERS (id, name) VALUES (j.book_id, j.publisher_name); EXCEPTION WHEN DUP_VAL_ON_INDEX THEN NULL; END;
            INSERT INTO BOOKS (id, title, author_id, publisher_id) VALUES (j.book_id, j.title, j.book_id, j.book_id);
        END LOOP;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE_APPLICATION_ERROR(-20001, 'Data insertion failed: ' || SQLERRM);
    END insert_data;

    PROCEDURE get_all_books(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
        SELECT b.id, b.title, a.name AS author_name, p.name AS publisher_name
        FROM BOOKS b
        JOIN AUTHORS a ON b.author_id = a.id
        JOIN PUBLISHERS p ON b.publisher_id = p.id;
    END get_all_books;

END BOOK_OPERATIONS;
/
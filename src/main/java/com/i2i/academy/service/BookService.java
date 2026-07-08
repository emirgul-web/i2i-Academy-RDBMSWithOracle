package com.i2i.academy.service;

import com.i2i.academy.dto.BookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // PL/SQL Paketindeki fonksiyonları ve insert prosedürünü çağıran metot
    public void processAndInsert(String rawData) throws SQLException {
        jdbcTemplate.execute((Connection conn) -> {
            // 1. Raw Data'yı XML'e Çeviren Fonksiyonu Çağır
            CallableStatement xmlCall = conn.prepareCall("{ ? = call BOOK_OPERATIONS.parse_to_xml(?) }");
            xmlCall.registerOutParameter(1, Types.CLOB);
            xmlCall.setString(2, rawData);
            xmlCall.execute();
            Clob xmlClob = xmlCall.getClob(1);

            // 2. Raw Data'yı JSON'a Çeviren Fonksiyonu Çağır
            CallableStatement jsonCall = conn.prepareCall("{ ? = call BOOK_OPERATIONS.parse_to_json(?) }");
            jsonCall.registerOutParameter(1, Types.CLOB);
            jsonCall.setString(2, rawData);
            jsonCall.execute();
            Clob jsonClob = jsonCall.getClob(1);

            // 3. Elde edilen XML ve JSON'ı Veritabanına Kaydeden Prosedürü Çağır
            CallableStatement insertCall = conn.prepareCall("{ call BOOK_OPERATIONS.insert_data(?, ?) }");
            insertCall.setClob(1, xmlClob);
            insertCall.setClob(2, jsonClob);
            insertCall.execute();
            
            return null;
        });
    }

    // PL/SQL Paketindeki Cursor döndüren prosedürü çağırıp verileri DTO listesine eşleyen metot
    public List<BookDto> getAllBooks() {
        return jdbcTemplate.execute((Connection conn) -> {
            // Oracle Ref Cursor için JDBC üzerinde -10 (OracleTypes.CURSOR) değeri kullanılır
            CallableStatement fetchCall = conn.prepareCall("{ call BOOK_OPERATIONS.get_all_books(?) }");
            fetchCall.registerOutParameter(1, -10); // -10 doğrudan OracleTypes.CURSOR'a karşılık gelir
            fetchCall.execute();
            
            ResultSet rs = (ResultSet) fetchCall.getObject(1);
            List<BookDto> books = new ArrayList<>();
            while (rs.next()) {
                BookDto book = new BookDto();
                book.setId(rs.getLong("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthorName(rs.getString("author_name"));
                book.setPublisherName(rs.getString("publisher_name"));
                books.add(book);
            }
            return books;
        });
    }
}
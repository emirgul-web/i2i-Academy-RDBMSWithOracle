package com.i2i.academy.controller;

import com.i2i.academy.dto.BookDto;
import com.i2i.academy.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    // Ham veriyi alıp PL/SQL üzerinden işleyen POST endpoint'i
    @PostMapping("/import")
    public ResponseEntity<?> importBooks(@RequestBody String rawData) {
        try {
            bookService.processAndInsert(rawData);
            return ResponseEntity.ok("Books imported successfully.");
        } catch (SQLException e) {
            // PL/SQL'den fırlatılan RAISE_APPLICATION_ERROR hatasını yakalayıp HTTP 400 döner
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Database error: " + e.getMessage());
        }
    }

    // Veritabanındaki kitapları Cursor ile çekip JSON dizi olarak dönen GET endpoint'i
    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
}
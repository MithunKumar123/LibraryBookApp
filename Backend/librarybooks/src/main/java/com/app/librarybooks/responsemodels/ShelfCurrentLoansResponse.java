package com.app.librarybooks.responsemodels;

import com.app.librarybooks.entity.Book;
import lombok.Data;

@Data
public class ShelfCurrentLoansResponse {

    private int daysLeft;
    private Book book;

    public ShelfCurrentLoansResponse(Book book, int daysLeft) {
        this.book = book;
        this.daysLeft = daysLeft;
    }
}

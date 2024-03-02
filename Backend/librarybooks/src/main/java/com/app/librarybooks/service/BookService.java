package com.app.librarybooks.service;

import com.app.librarybooks.dao.BookRepository;
import com.app.librarybooks.dao.CheckoutRepository;
import com.app.librarybooks.dao.HistoryRepository;
import com.app.librarybooks.dao.PaymentRepository;
import com.app.librarybooks.entity.Book;
import com.app.librarybooks.entity.Checkout;
import com.app.librarybooks.entity.History;
import com.app.librarybooks.entity.Payment;
import com.app.librarybooks.responsemodels.ShelfCurrentLoansResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BookService {

    private BookRepository bookRepository;

    private CheckoutRepository checkoutRepository;

    private HistoryRepository historyRepository;

    private PaymentRepository paymentRepository;

    public BookService(BookRepository bookRepository, CheckoutRepository checkoutRepository, HistoryRepository historyRepository, PaymentRepository paymentRepository) {
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
    }

    public Book checkoutBook(String userEmail, Long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);

        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);

        if(!book.isPresent() || validateCheckout != null || book.get().getCopiesAvailable() <= 0){
            throw new Exception("Book doesn't exist or alreadyt checkedout by user");
        }

        List<Checkout> currentUserBookCheckedout = checkoutRepository.findBookByUserEmail(userEmail);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        boolean bookNeedsReturned = false;

        for(Checkout checkout: currentUserBookCheckedout){
            Date d1 = dateFormat.parse(checkout.getReturnDate());
            Date d2 = dateFormat.parse(LocalDate.now().toString());

            TimeUnit unit = TimeUnit.DAYS;

            double difference = unit.convert(d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);

            if(difference < 0){
                bookNeedsReturned = true;
                break;
            }
        }

        Payment payment = paymentRepository.findByUserEmail(userEmail);
        if((payment != null && payment.getAmount() > 0) || (payment != null && bookNeedsReturned)){
            throw new Exception("Outstanding fees");
        }

        if(payment == null){
            Payment newPayment = new Payment();
            newPayment.setAmount(0.00);
            newPayment.setUserEmail(userEmail);
            paymentRepository.save(newPayment);
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() -1);
        bookRepository.save(book.get());

        Checkout checkout = new Checkout(
                userEmail,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(7).toString(),
                book.get().getId()
        );

        checkoutRepository.save(checkout);
        return book.get();
    }

    public boolean checkoutBookByUser(String userEmail, Long bookId){
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail,bookId);
        if(validateCheckout != null){
            return true;
        }else{
            return false;
        }
    }

    public int currentLoanCount(String userEmail){
        return checkoutRepository.findBookByUserEmail(userEmail).size();
    }

    public List<ShelfCurrentLoansResponse> currentLoans(String userEmail) throws Exception{
        List<ShelfCurrentLoansResponse> currentLoansDetailsList = new ArrayList<>();
        List<Checkout> checkoutList = checkoutRepository.findBookByUserEmail(userEmail);
        List<Long> bookIdList = new ArrayList<>();
        for(Checkout checkout: checkoutList){
            bookIdList.add(checkout.getBookId());
        }
        List<Book> books = bookRepository.findBooksByBookId(bookIdList);
        SimpleDateFormat dateObject = new SimpleDateFormat("yyyy-MM-dd");
        for(Book book: books){
            Optional<Checkout> checkout = checkoutList.stream()
                    .filter(x -> x.getBookId() == book.getId()).findFirst();
            if(checkout.isPresent()){
                Date returnDate = dateObject.parse(checkout.get().getReturnDate());
                Date currentDate = dateObject.parse(LocalDate.now().toString());

                TimeUnit time = TimeUnit.DAYS;
                long differnce_in_time = time
                        .convert(returnDate.getTime() - currentDate.getTime(), TimeUnit.MILLISECONDS);
                currentLoansDetailsList.add(new ShelfCurrentLoansResponse(book, (int) differnce_in_time));
            }
        }
        return currentLoansDetailsList;
    }

    public void returnBook(String userEmail, Long bookId) throws Exception{
        Optional<Book> book = bookRepository.findById(bookId);
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if(!book.isPresent() || validateCheckout == null){
            throw new Exception("Book doesnot exist or not checkout by user");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 = dateFormat.parse(validateCheckout.getReturnDate());
        Date d2 = dateFormat.parse(LocalDate.now().toString());

        TimeUnit unit = TimeUnit.DAYS;

        double difference = unit.convert(d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);

        if(difference < 0){
            Payment payment = paymentRepository.findByUserEmail(userEmail);
            payment.setAmount(payment.getAmount() + (difference * -1));
            paymentRepository.save(payment);
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() + 1);
        bookRepository.save(book.get());
        checkoutRepository.deleteById(validateCheckout.getId());
        History history = new History(
                userEmail,
                validateCheckout.getCheckoutDate(),
                LocalDate.now().toString(),
                book.get().getTitle(),
                book.get().getAuthor(),
                book.get().getDescription(),
                book.get().getImg()
        );
        historyRepository.save(history);
    }

    public void renewLoan(String userEmail, Long bookId) throws Exception{
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if(validateCheckout == null){
            throw new Exception("Book doesnot exist or not checkout by user");
        }
        SimpleDateFormat dateObject = new SimpleDateFormat("yyyy-MM-dd");
        Date returnDate = dateObject.parse(validateCheckout.getReturnDate());
        Date currentDate = dateObject.parse(LocalDate.now().toString());
        if(returnDate.compareTo(currentDate) > 0 || returnDate.compareTo(currentDate) == 0){
            validateCheckout.setReturnDate(LocalDate.now().plusDays(7).toString());
            checkoutRepository.save(validateCheckout);
        }
    }

}

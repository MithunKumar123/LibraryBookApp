package com.app.librarybooks.service;

import com.app.librarybooks.dao.BookRepository;
import com.app.librarybooks.dao.ReviewRepository;
import com.app.librarybooks.entity.Review;
import com.app.librarybooks.requestmodels.ReviewRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Objects;

@Service
@Transactional
public class ReviewService {

    private BookRepository bookRepository;

    private ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    public void postReview(String userEmail, ReviewRequest reviewRequest) throws Exception {
        Review validateReview = reviewRepository
                .findByUserEmailAndBookId(userEmail,
                reviewRequest.getBookId());
        if(validateReview != null){
            throw new Exception("Review already created");
        }

        Review review = new Review();
        review.setBookId(reviewRequest.getBookId());
        review.setRating(reviewRequest.getRating());
        review.setUserEmail(userEmail);
        if(reviewRequest.getReviewDescription().isPresent()){
            review.setReviewDescription(reviewRequest.getReviewDescription().map(
                    Objects::toString
            ).orElse(null));
            review.setDate(Date.valueOf(LocalDate.now()));
            reviewRepository.save(review);
        }

    }

    public Boolean userReviewListed(String userEmail, Long bookId){
        Review validateReview = reviewRepository.findByUserEmailAndBookId(userEmail, bookId);
        if(validateReview != null){
            return true;
        }
        return false;
    }

}

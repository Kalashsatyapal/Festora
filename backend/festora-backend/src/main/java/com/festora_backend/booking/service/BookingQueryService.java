package com.festora_backend.booking.service;
import com.festora_backend.booking.dto.BookingSummaryResponse;
import com.festora_backend.booking.entity.Booking;
import com.festora_backend.booking.repository.BookingRepository;
import com.festora_backend.common.enums.BookingStatus;
import com.festora_backend.common.response.PageResponse;
import com.festora_backend.user.entity.User;
import com.festora_backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
public class BookingQueryService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    public BookingQueryService(
            BookingRepository bookingRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }
    public PageResponse<BookingSummaryResponse> getMyBookings(
            String email,
            BookingStatus status,
            int page,
            int size
    ) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, Booking::getCreatedAt)
        );
        Page<Booking> bookings = status == null
                ? bookingRepository.findByUserId(user.getId(), pageable)
                : bookingRepository.findByUserIdAndStatus(user.getId(), status, pageable);
        return PageResponse.from(bookings.map(booking ->
                new BookingSummaryResponse(
                        booking.getBookingReference(),
                        booking.getEvent().getId(),
                        booking.getEvent().getTitle(),
                        booking.getEvent().getStartAt(),
                        booking.getQuantity(),
                        booking.getTotalAmount(),
                        booking.getStatus().name(),
                        booking.getCreatedAt()
                )
        ));
    }
}
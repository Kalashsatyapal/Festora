package com.festora.billing.refund;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    private final RefundRepository refundRepository;

    public RefundService(RefundRepository refundRepository) {
        this.refundRepository = refundRepository;
    }

    @Transactional
    public RefundResponse process(RefundRequest request) {
        var existing = refundRepository.findByBookingReference(
                request.bookingReference()
        );

        if (existing.isPresent()) {
            Refund refund = existing.get();

            if (refund.getAmount().compareTo(request.amount()) != 0) {
                throw new IllegalArgumentException(
                        "A refund already exists with a different amount"
                );
            }

            return RefundResponse.from(refund);
        }

        Refund refund = Refund.successful(
                request.bookingReference(),
                request.amount(),
                request.reason().trim()
        );

        return RefundResponse.from(refundRepository.save(refund));
    }
}
package project.commondto.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import project.commondto.dto.booking.CreateBookingDto;
import project.commondto.dto.booking.ReservedRequest;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CheckOutAfterCheckInValidator
        implements ConstraintValidator<CheckOutAfterCheckIn, CreateBookingDto> {

    @Override
    public boolean isValid(CreateBookingDto request, ConstraintValidatorContext context) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();

        if (checkIn == null || checkOut == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (checkIn.isBefore(LocalDate.now())) {
            context.buildConstraintViolationWithTemplate("Check-in date must not be in the past")
                    .addPropertyNode("checkInDate")
                    .addConstraintViolation();
            return false;
        }

        if (!checkOut.isAfter(checkIn.plusDays(0)) || ChronoUnit.DAYS.between(checkIn, checkOut) < 1) {
            context.buildConstraintViolationWithTemplate("Check-out date must be at least 1 day after check-in date")
                    .addPropertyNode("checkOutDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
package de.rwth.idsg.bikeman.ixsi.processor.query.user;

import com.google.common.base.Optional;
import de.rwth.idsg.bikeman.domain.Booking;
import de.rwth.idsg.bikeman.ixsi.ErrorFactory;
import de.rwth.idsg.bikeman.ixsi.IxsiProcessingException;
import de.rwth.idsg.bikeman.ixsi.processor.api.UserRequestProcessor;
import de.rwth.idsg.bikeman.ixsi.service.AvailabilityPushService;
import de.rwth.idsg.bikeman.ixsi.service.BookingCheckService;
import de.rwth.idsg.bikeman.ixsi.service.BookingService;
import de.rwth.idsg.bikeman.web.rest.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xjc.schema.ixsi.BookingType;
import xjc.schema.ixsi.ChangeBookingRequestType;
import xjc.schema.ixsi.ChangeBookingResponseType;
import xjc.schema.ixsi.ErrorType;
import xjc.schema.ixsi.Language;
import xjc.schema.ixsi.TimePeriodType;
import xjc.schema.ixsi.UserInfoType;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Component
public class ChangeBookingRequestProcessor implements
        UserRequestProcessor<ChangeBookingRequestType, ChangeBookingResponseType> {

    @Autowired private BookingService bookingService;
    @Autowired private AvailabilityPushService availabilityPushService;
    @Autowired private BookingCheckService bookingCheckService;

    @Override
    public Class<ChangeBookingRequestType> getProcessingClass() {
        return ChangeBookingRequestType.class;
    }

    @Override
    public ChangeBookingResponseType processAnonymously(ChangeBookingRequestType request, Optional<Language> lan) {
        return buildError(ErrorFactory.Auth.notAnonym("Anonymous change booking request not allowed", null));
    }

    @Override
    public ChangeBookingResponseType processForUser(ChangeBookingRequestType request, Optional<Language> lan,
                                                    UserInfoType userInfo) {
        try {
            if (request.isSetCancel() && request.isCancel()) {
                return proceedCancel(request, userInfo);

            } else {
                return proceedChange(request, userInfo);
            }
        } catch (DatabaseException e) {
            return buildError(ErrorFactory.Booking.idUnknown(e.getMessage(), null));

        } catch (IxsiProcessingException e) {
            return buildError(ErrorFactory.Booking.changeNotPossible(e.getMessage(), e.getMessage()));
        }
    }

    private ChangeBookingResponseType proceedChange(ChangeBookingRequestType request, UserInfoType userInfo) {
        Booking oldBooking = bookingService.get(request.getBookingID(), userInfo.getUserID());
        TimePeriodType oldTimePeriod = buildTimePeriod(oldBooking);

        Booking newBooking = bookingService.update(oldBooking, request.getNewTimePeriodProposal());
        TimePeriodType newTimePeriod = buildTimePeriod(newBooking);

        BookingType responseBooking = new BookingType()
            .withID(newBooking.getIxsiBookingId())
            .withTimePeriod(newTimePeriod);

        String pedelecId = newBooking.getReservation()
                                     .getPedelec()
                                     .getManufacturerId();

        String placeId = newBooking.getReservation()
                                   .getPedelec()
                                   .getStationSlot()
                                   .getStation()
                                   .getManufacturerId();

        availabilityPushService.changedBooking(pedelecId, placeId, oldTimePeriod, newTimePeriod);
        bookingCheckService.changedBooking(oldBooking, newBooking);

        return new ChangeBookingResponseType().withBooking(responseBooking);
    }

    private ChangeBookingResponseType proceedCancel(ChangeBookingRequestType request, UserInfoType userInfo) {
        Booking booking = bookingService.cancel(request.getBookingID(), userInfo.getUserID());
        TimePeriodType timePeriod = buildTimePeriod(booking);

        String pedelecId = booking.getReservation()
                                  .getPedelec()
                                  .getManufacturerId();

        String placeId = booking.getReservation()
                                .getPedelec()
                                .getStationSlot()
                                .getStation()
                                .getManufacturerId();

        availabilityPushService.cancelledBooking(pedelecId, placeId, timePeriod);
        bookingCheckService.cancelledBooking(booking);

        return new ChangeBookingResponseType();
    }

    private TimePeriodType buildTimePeriod(Booking booking) {
        return new TimePeriodType()
            .withBegin(booking.getReservation().getStartDateTime().toDateTime())
            .withEnd(booking.getReservation().getEndDateTime().toDateTime());
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Override
    public ChangeBookingResponseType buildError(ErrorType e) {
        return new ChangeBookingResponseType().withError(e);
    }
}

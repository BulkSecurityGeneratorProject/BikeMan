package de.rwth.idsg.bikeman.ixsi.service;

import de.rwth.idsg.bikeman.ixsi.IXSIConstants;
import de.rwth.idsg.bikeman.ixsi.endpoint.Producer;
import de.rwth.idsg.bikeman.ixsi.store.AvailabilityStore;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xjc.schema.ixsi.AvailabilityPushMessageType;
import xjc.schema.ixsi.BookingTargetChangeAvailabilityType;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.IxsiMessageType;
import xjc.schema.ixsi.SubscriptionMessageType;
import xjc.schema.ixsi.TimePeriodType;

import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2014
 */
@Slf4j
@Service
public class AvailabilityPushService {

    @Autowired private Producer producer;
    @Autowired private AvailabilityStore availabilityStore;

    // -------------------------------------------------------------------------
    // Booking-related
    // -------------------------------------------------------------------------

    public void placedBooking(String bookeeID, String placeId, TimePeriodType timePeriod) {
        buildAndSend(bookeeID, placeId, timePeriod, false);
    }

    public void changedBooking(String bookeeID, String placeId,
                               TimePeriodType oldTimePeriod, TimePeriodType newTimePeriod) {
        cancelledBooking(bookeeID, placeId, oldTimePeriod);
        placedBooking(bookeeID, placeId, newTimePeriod);
    }

    public void cancelledBooking(String bookeeID, String placeId, TimePeriodType timePeriod) {
        buildAndSend(bookeeID, placeId, timePeriod, true);
    }

    // -------------------------------------------------------------------------
    // Transaction-related
    // -------------------------------------------------------------------------

    /**
     * @param bookeeID  Manufacturer ID of the pedelec.
     * @param departure Date/time of the start of the transaction.
     */
    public void takenFromPlace(String bookeeID, DateTime departure) {
        TimePeriodType tp = buildTimePeriodForTransaction(departure);
        buildAndSend(bookeeID, null, tp, false);
    }

    /**
     * @param bookeeID  Manufacturer ID of the pedelec.
     * @param placeID   Manufacturer ID of the station.
     * @param departure Date/time of the start of the transaction. This is rightfully so and not the date/time
     *                  of the arrival, because in client system we want to invalidate the time period that
     *                  was sent earlier with {@link #takenFromPlace(String, org.joda.time.DateTime)}.
     *                  Therefore, the two values have to match.
     */
    public void arrivedAtPlace(String bookeeID, String placeID, DateTime departure) {
        TimePeriodType tp = buildTimePeriodForTransaction(departure);
        buildAndSend(bookeeID, placeID, tp, true);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    @Async
    public void buildAndSend(String bookeeID, String placeID, TimePeriodType period, boolean isAvailable) {
        BookingTargetIDType bookingTargetID = new BookingTargetIDType()
                .withBookeeID(bookeeID)
                .withProviderID(IXSIConstants.Provider.id);

        Set<String> systemIdSet = availabilityStore.getSubscribedSystems(bookingTargetID);
        if (systemIdSet.isEmpty()) {
            log.debug("Will not push. There is no subscribed system for bookeeID '{}'", bookeeID);
            return;
        }

        BookingTargetChangeAvailabilityType targetChange = new BookingTargetChangeAvailabilityType()
            .withID(bookingTargetID)
            .withPlaceID(placeID);

        if (isAvailable) {
            targetChange.setAvailability(period);
        } else {
            targetChange.setInavailability(period);
        }

        AvailabilityPushMessageType avail = new AvailabilityPushMessageType().withAvailabilityChange(targetChange);
        SubscriptionMessageType sub = new SubscriptionMessageType().withPushMessageGroup(avail);
        IxsiMessageType ixsi = new IxsiMessageType().withSubscriptionMessage(sub);

        producer.send(ixsi, systemIdSet);
    }

    private TimePeriodType buildTimePeriodForTransaction(DateTime departure) {
        return new TimePeriodType()
            .withBegin(departure)
            .withEnd(IXSIConstants.constructReturnDateTime(departure));
    }

}

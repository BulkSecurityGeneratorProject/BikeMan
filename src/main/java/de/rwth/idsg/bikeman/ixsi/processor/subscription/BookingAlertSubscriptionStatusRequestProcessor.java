package de.rwth.idsg.bikeman.ixsi.processor.subscription;

import de.rwth.idsg.bikeman.ixsi.schema.BookingAlertSubscriptionStatusRequestType;
import de.rwth.idsg.bikeman.ixsi.schema.BookingAlertSubscriptionStatusResponseType;
import org.springframework.stereotype.Component;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Component
public class BookingAlertSubscriptionStatusRequestProcessor implements
        SubscriptionRequestProcessor<BookingAlertSubscriptionStatusRequestType, BookingAlertSubscriptionStatusResponseType> {

    @Override
    public BookingAlertSubscriptionStatusResponseType process(BookingAlertSubscriptionStatusRequestType request) {
        return null;
    }

    @Override
    public BookingAlertSubscriptionStatusResponseType invalidSystem() {
        return null;
    }
}
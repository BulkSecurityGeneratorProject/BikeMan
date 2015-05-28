package de.rwth.idsg.bikeman.ixsi.processor.subscription.complete;

import de.rwth.idsg.bikeman.ixsi.ErrorFactory;
import de.rwth.idsg.bikeman.ixsi.processor.api.SubscriptionRequestMessageProcessor;
import de.rwth.idsg.bikeman.ixsi.schema.CompleteBookingAlertRequestType;
import de.rwth.idsg.bikeman.ixsi.schema.CompleteBookingAlertResponseType;
import de.rwth.idsg.bikeman.ixsi.schema.ErrorType;
import org.springframework.stereotype.Component;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Component
public class CompleteBookingAlertRequestProcessor implements
        SubscriptionRequestMessageProcessor<CompleteBookingAlertRequestType, CompleteBookingAlertResponseType> {

    @Override
    public CompleteBookingAlertResponseType process(CompleteBookingAlertRequestType request, String systemId) {
        // TODO FUTURE
        return buildError(ErrorFactory.notImplemented(null, null));
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Override
    public CompleteBookingAlertResponseType buildError(ErrorType e) {
        return new CompleteBookingAlertResponseType().withError(e);
    }
}

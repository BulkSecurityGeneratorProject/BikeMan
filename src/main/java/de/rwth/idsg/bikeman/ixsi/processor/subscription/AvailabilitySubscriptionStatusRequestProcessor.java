package de.rwth.idsg.bikeman.ixsi.processor.subscription;

import de.rwth.idsg.bikeman.ixsi.ErrorFactory;
import de.rwth.idsg.bikeman.ixsi.schema.AvailabilitySubscriptionResponseType;
import de.rwth.idsg.bikeman.ixsi.schema.AvailabilitySubscriptionStatusRequest;
import de.rwth.idsg.bikeman.ixsi.schema.AvailabilitySubscriptionStatusResponse;
import org.springframework.stereotype.Component;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Component
public class AvailabilitySubscriptionStatusRequestProcessor implements
        SubscriptionRequestProcessor<AvailabilitySubscriptionStatusRequest, AvailabilitySubscriptionStatusResponse> {

    @Override
    public AvailabilitySubscriptionStatusResponse process(AvailabilitySubscriptionStatusRequest request) {
        return null;
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Override
    public AvailabilitySubscriptionStatusResponse invalidSystem() {
        AvailabilitySubscriptionStatusResponse b = new AvailabilitySubscriptionStatusResponse();
        b.getError().add(ErrorFactory.invalidSystem());
        return b;
    }
}
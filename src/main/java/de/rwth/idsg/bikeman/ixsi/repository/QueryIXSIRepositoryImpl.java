package de.rwth.idsg.bikeman.ixsi.repository;

import de.rwth.idsg.bikeman.ixsi.dto.query.AvailabilityResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.BookingTargetsInfoResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.ChangedProvidersResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.CloseSessionResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.OpenSessionResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.PedelecDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.PlaceAvailabilityResponseDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.StationDTO;
import de.rwth.idsg.bikeman.ixsi.dto.query.TokenGenerationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 * Created by max on 06/10/14.
 * Repository for handling
 */
@Slf4j
@Repository
public class QueryIXSIRepositoryImpl implements QueryIXSIRepository {

    @PersistenceContext private EntityManager em;

    @Override
    public BookingTargetsInfoResponseDTO bookingTargetInfos() {

        // TODO: The value 0 for maxDistance is a placeholder! Pedelec entity has to be expanded to contain such a property
        //
        final String pedelecQuery = "SELECT new de.rwth.idsg.bikeman.ixsi.dto.query." +
                                    "PedelecDTO(p.pedelecId, p.manufacturerId, 0) " +
                                    "FROM Pedelec p";

        final String stationQuery = "SELECT new de.rwth.idsg.bikeman.ixsi.dto.query." +
                                    "StationDTO(s.stationId, s.locationLongitude, s.locationLatitude, " +
                                    "s.stationSlots.size, s.name, s.note, " +
                                    "a.streetAndHousenumber, a.zip, a.city, a.country) " +
                                    "FROM Station s LEFT JOIN s.address a";

        List<PedelecDTO> pedelecList = em.createQuery(pedelecQuery, PedelecDTO.class).getResultList();
        List<StationDTO> stationList = em.createQuery(stationQuery, StationDTO.class).getResultList();

        Date pedelecUpdated = em.createQuery("SELECT max(p.updated) FROM Pedelec p", Date.class)
                                .getSingleResult();

        Date stationUpdated = em.createQuery("SELECT max(s.updated) FROM Station s", Date.class)
                                .getSingleResult();

        long timestamp;
        if (pedelecUpdated.after(stationUpdated)) {
            timestamp = pedelecUpdated.getTime();
        } else {
            timestamp = stationUpdated.getTime();
        }

        BookingTargetsInfoResponseDTO dto = new BookingTargetsInfoResponseDTO();
        dto.setPedelecs(pedelecList);
        dto.setStations(stationList);
        dto.setTimestamp(timestamp);
        return dto;
    }

    @Override
    public ChangedProvidersResponseDTO changedProviders() {
        return null;
    }

    @Override
    public AvailabilityResponseDTO availability() {
        return null;
    }

    @Override
    public CloseSessionResponseDTO closeSession() {
        return null;
    }

    @Override
    public OpenSessionResponseDTO openSession() {
        return null;
    }

    @Override
    public PlaceAvailabilityResponseDTO placeAvailability() {
        return null;
    }

    @Override
    public TokenGenerationResponseDTO tokenGeneration() {
        return null;
    }
}

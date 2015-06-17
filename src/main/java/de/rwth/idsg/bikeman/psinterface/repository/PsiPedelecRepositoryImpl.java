package de.rwth.idsg.bikeman.psinterface.repository;

import de.rwth.idsg.bikeman.domain.OperationState;
import de.rwth.idsg.bikeman.psinterface.Utils;
import de.rwth.idsg.bikeman.psinterface.dto.request.ChargingStatusDTO;
import de.rwth.idsg.bikeman.psinterface.dto.request.PedelecStatusDTO;
import de.rwth.idsg.bikeman.psinterface.dto.response.AvailablePedelecDTO;
import de.rwth.idsg.bikeman.web.rest.exception.DatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.06.2015
 */
@Repository
@Slf4j
public class PsiPedelecRepositoryImpl implements PsiPedelecRepository {

    @PersistenceContext private EntityManager em;

    // nachfolgende funktion nur mit einem rückgabewert (keine liste) und als parameter stationId statt endpointAddress

    @Override
    @Transactional(readOnly = true)
    public List<AvailablePedelecDTO> findAvailablePedelecs(String endpointAddress) {
        final String q = "SELECT new de.rwth.idsg.bikeman.psinterface.dto.response." +
                         "AvailablePedelecDTO(p.manufacturerId) " +
                         "from Pedelec p " +
                         "where p.stationSlot.station.endpointAddress = :endpointAddress " +
                         "and p.state = de.rwth.idsg.bikeman.domain.OperationState.OPERATIVE " +
                         "order by p.batteryStateOfCharge desc";

        try {
            return em.createQuery(q, AvailablePedelecDTO.class)
                    .setParameter("endpointAddress", endpointAddress)
                    .setMaxResults(5)
                    .getResultList();
        } catch (Exception e) {
            throw new DatabaseException("Failed to find pedelecs in station with endpoint address"
                + endpointAddress, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePedelecStatus(PedelecStatusDTO dto) {
        final String s = "UPDATE Pedelec p SET " +
                         "p.errorCode = :pedelecErrorCode, " +
                         "p.errorInfo = :pedelecErrorInfo, " +
                         "p.state = :pedelecState, " +
                         "p.updated = :updated " +
                         "WHERE p.manufacturerId = :pedelecManufacturerId";

        try {
            em.createQuery(s)
              .setParameter("pedelecErrorCode", dto.getPedelecErrorCode())
              .setParameter("pedelecErrorInfo", dto.getPedelecErrorInfo())
              .setParameter("pedelecState", OperationState.valueOf(dto.getPedelecState().name()))
              .setParameter("updated", new Date(Utils.toMillis(dto.getTimestamp())))
              .setParameter("pedelecManufacturerId", dto.getPedelecManufacturerId())
              .executeUpdate();
        } catch (Exception e) {
            throw new DatabaseException("Failed to update the pedelec status with manufacturerId "
                + dto.getPedelecManufacturerId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePedelecChargingStatus(List<ChargingStatusDTO> dtoList) {
        final String s = "UPDATE Pedelec p SET " +
                "p.batteryStateOfCharge = :stateOfCharge " +
                "WHERE p.manufacturerId = :pedelecManufacturerId";

        //TODO: currently only SOC gets updated

        try {
            for (ChargingStatusDTO dto : dtoList) {
                em.createQuery(s)
                        .setParameter("stateOfCharge", dto.getBattery().getSoc())
                        .setParameter("pedelecManufacturerId", dto.getPedelecManufacturerId())
                        .executeUpdate();
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to update the charging status.", e);
        }
    }

}

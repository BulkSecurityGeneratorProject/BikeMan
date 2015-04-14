package de.rwth.idsg.bikeman.app.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.rwth.idsg.bikeman.domain.TariffType;
import de.rwth.idsg.bikeman.web.rest.dto.util.CustomLocalDateTimeSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.LocalDateTime;

@Getter
@ToString(includeFieldNames = true)
public class ViewBookedTariffDTO {

    private Long tariffId;
    private TariffType name;
    private Boolean automaticRenewal;

    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime expiryDateTime;

    public ViewBookedTariffDTO (Long tariffId, TariffType name, Boolean automaticRenewal, LocalDateTime expiryDateTime) {
        this.tariffId = tariffId;
        this.name = name;
        this.automaticRenewal = automaticRenewal;
        this.expiryDateTime = expiryDateTime;
    }
}
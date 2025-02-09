package proyectoNetBanking.dto.tarjetasCredito;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record DatosTarjetaDTO(

        @DecimalMin(value = "2000.0", inclusive = true, message = "El límite de crédito no puede ser menor a DOP 2000.")
        BigDecimal limiteCredito
) {
}

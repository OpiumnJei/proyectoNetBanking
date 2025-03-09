package proyectoNetBanking.dto.pagos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DatosPagoBeneficiarioDTO(

        @NotNull(message = "El id de la cuenta de ahorro es requerido.")
        Long cuentaOrigenId,

        @NotNull(message = "El monto del pago es un campo requerido.")
        @Positive(message = "El monto de pago debe ser positivo")
        @DecimalMin(value = "15.0", inclusive = true, message = "El monto minimo que puede ser transferido es de 15.0 DOP")
        BigDecimal montoPago
) {

}

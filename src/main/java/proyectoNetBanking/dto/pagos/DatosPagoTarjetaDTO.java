package proyectoNetBanking.dto.pagos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosPagoTarjetaDTO(

        @NotNull(message = "El id de la cuenta es un campo obligatorio.")
        Long cuentaId,

        @NotNull(message = "El monto del pago es un campo obligatorio.")
        @DecimalMin(value = "200.0", inclusive = true, message = "El monto minimo aceptado para el pago de tarjetas de credito es de DOP 200.")
        BigDecimal montoPago
) {
}

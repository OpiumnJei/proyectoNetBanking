package proyectoNetBanking.domain.tarjetasCredito;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DatosPagoTarjetaDTO(

        @NotNull
        Long cuentaId,

        @DecimalMin(value = "200.0", inclusive = true, message = "El monto minimo aceptado para el pago de tarjetas de credito es de DOP 200.")
        BigDecimal montoPago
) {
}

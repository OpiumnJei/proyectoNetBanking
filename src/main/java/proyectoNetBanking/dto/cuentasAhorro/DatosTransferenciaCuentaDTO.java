package proyectoNetBanking.dto.cuentasAhorro;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DatosTransferenciaCuentaDTO(

        @NotNull(message = "El id de la cuenta origen es un campo requerido.")
        Long cuentaOrigenId,

        @NotNull(message = "El id de la cuenta destinataria es un campo requerido.")
        Long cuentaDestinoId,

        @NotNull(message = "El monto de la transferencia es un campo requerido.")
        @Positive(message = "El monto de la transferencia  debe ser positivo")
        @DecimalMin(value = "15.0", inclusive = true, message = "El monto minimo que puede ser transferido es de 15.0 DOP")
        BigDecimal montoTransferencia
) {
}

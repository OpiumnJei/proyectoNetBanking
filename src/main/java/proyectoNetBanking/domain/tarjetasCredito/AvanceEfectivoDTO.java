package proyectoNetBanking.domain.tarjetasCredito;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AvanceEfectivoDTO(
        @NotNull(message = "El ID de la tarjeta de crédito es requerido.")
        Long tarjetaCreditoId,

        @NotNull(message = "El ID de la cuenta de ahorro es requerido.")
        Long cuentaAhorroId,

        @NotNull(message = "El monto del avance es requerido.")
        @Positive(message = "El monto debe ser mayor a 0.")
        BigDecimal montoAvanceEfectivo
) {
}

package proyectoNetBanking.dto.pagos;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePagoPrestamoDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        Long prestamoId,
        BigDecimal montoPago,
        BigDecimal saldoPorPagar,
        String mensaje
) {
}

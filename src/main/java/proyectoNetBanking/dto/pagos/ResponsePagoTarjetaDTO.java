package proyectoNetBanking.dto.pagos;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePagoTarjetaDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        Long tarjetaId,
        BigDecimal saldoPorPagar,
        String mensaje
) {
}

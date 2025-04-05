package proyectoNetBanking.dto.cuentasAhorro;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponseTransferenciaDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        Long cuentaDestinoId,
        BigDecimal montoTransferencia
) {
}

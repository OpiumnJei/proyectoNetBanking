package proyectoNetBanking.dto.pagos;

import proyectoNetBanking.domain.transacciones.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResponsePagoBeneficiarioDTO(
        Long transaccionId,
        TipoTransaccion tipoTransaccion,
        LocalDateTime fechaTransaccion,
        Long cuentaOrigenId,
        Long cuentaBeneficiarioId,
        BigDecimal montoPago,
        String mensaje
) {
}

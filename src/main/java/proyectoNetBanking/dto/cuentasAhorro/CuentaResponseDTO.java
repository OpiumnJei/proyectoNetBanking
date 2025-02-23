package proyectoNetBanking.dto.cuentasAhorro;

import java.math.BigDecimal;
import java.time.Instant;

public record CuentaResponseDTO(
        Long usuarioId,
        BigDecimal montoCuenta,
        String proposito,
        Instant fechaCreacion
) {
}

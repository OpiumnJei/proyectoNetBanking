package proyectoNetBanking.dto.prestamos;

import java.math.BigDecimal;
import java.time.Instant;

public record PrestamoResponseDTO(
        Long usuarioId,
        String productoId,
        BigDecimal montoPrestamo,
        BigDecimal montoAPagar,
        Instant fechaCreacion
) {
}

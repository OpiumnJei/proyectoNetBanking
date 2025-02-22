package proyectoNetBanking.dto.usuarios;

import java.math.BigDecimal;

public record ClienteResponseDTO(

        String nuevoNombre,
        String nuevoApellido,
        String nuevaCedula,
        String nuevoCorreo,
        String newPassword,
        BigDecimal montoAdicionalAgregado
) {
}

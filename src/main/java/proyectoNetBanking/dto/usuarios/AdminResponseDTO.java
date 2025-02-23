package proyectoNetBanking.dto.usuarios;

public record AdminResponseDTO(

        String nuevoNombre,
        String nuevoApellido,
        String nuevaCedula,
        String nuevoCorreo,
        String newPassword
) {
}

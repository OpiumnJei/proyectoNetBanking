package proyectoNetBanking.dto.usuarios;

public record ListaUsuariosDTO(
        Long id,
        String nombre,
        String apellido,
        String tipoUsuario,
        boolean activo
) {
}

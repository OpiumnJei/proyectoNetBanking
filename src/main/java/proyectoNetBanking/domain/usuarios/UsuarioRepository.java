package proyectoNetBanking.domain.usuarios;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

// Usuario Este es el tipo de entidad para la cual el repositorio está diseñado.
// Loong Este es el tipo de dato de la clave primaria (ID) de la entidad Usuario.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCedula(@NotBlank String cedula);

    boolean existsByCorreo(@NotBlank String correo);

    Long countByTipoUsuarioAndActivo(TipoUsuario usuarioCliente, boolean clienteActivo);
}

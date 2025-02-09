package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.usuarios.TipoUsuario;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {

    //verificar si el tipo de usuario existe en la bd
    boolean existsByNombreTipoUsuario(String nombre);
}

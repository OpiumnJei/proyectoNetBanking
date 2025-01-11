package proyectoNetBanking.domain.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {

    //verificar si el tipo de usuario existe en la bd
    boolean existsByNombreTipoUsuario(String nombre);
}

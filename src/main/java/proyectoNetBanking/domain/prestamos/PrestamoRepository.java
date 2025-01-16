package proyectoNetBanking.domain.prestamos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(Long id);

    boolean existsByIdProducto(String idProducto);

    int countByUsuarioIdAndEstadoProducto(Long idUsuario);
}

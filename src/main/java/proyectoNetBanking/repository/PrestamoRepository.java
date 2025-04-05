package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuarioId(Long id);

    boolean existsByIdProducto(String idProducto);

    //se hace un conteo en la bd de los prestamos activos, es decir que sean igual a 1
    Long countByEstadoProductoId(Long estadoId);
}

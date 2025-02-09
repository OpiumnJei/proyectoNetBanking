package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;

import java.util.Optional;

public interface EstadoProductoRepository extends JpaRepository<EstadoProducto, Long> {

    //verificar si el producto existe en la bd
    boolean existsByNombreEstado(String nombre);

    Optional<EstadoProducto> findByNombreEstadoIgnoreCase(String nombre);
}

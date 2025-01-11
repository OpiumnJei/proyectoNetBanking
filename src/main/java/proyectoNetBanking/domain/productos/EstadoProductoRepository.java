package proyectoNetBanking.domain.productos;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoProductoRepository extends JpaRepository<EstadoProducto, Long> {

    //verificar si el producto existe en la bd
    boolean existsByNombreEstado(String nombre);
}

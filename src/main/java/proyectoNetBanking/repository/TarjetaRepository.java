package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.tarjetasCredito.TarjetaCredito;

import java.util.List;

public interface TarjetaRepository extends JpaRepository<TarjetaCredito, Long> {

    List<TarjetaCredito> findByUsuarioId(Long id);

    Long countByEstadoProductoId(Long estadoIdo);
}

package proyectoNetBanking.domain.tarjetasCredito;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.prestamos.Prestamo;

import java.util.List;

public interface TarjetaRepository extends JpaRepository<TarjetaCredito, Long> {

    List<TarjetaCredito> findByUsuarioId(Long id);
}

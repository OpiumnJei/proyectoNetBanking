package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;

import java.time.LocalDateTime;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Long countByFechaBetween(LocalDateTime inicioDia, LocalDateTime finDia);

    Long countByTipoTransaccion(TipoTransaccion tipoTransaccion);
}

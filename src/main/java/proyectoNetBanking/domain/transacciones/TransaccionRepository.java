package proyectoNetBanking.domain.transacciones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Long countByFechaBetween(LocalDateTime inicioDia, LocalDateTime finDia);

    Long countByTipoTransaccion(TipoTransaccion tipoTransaccion);
}

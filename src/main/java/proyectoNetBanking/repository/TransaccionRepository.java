package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import proyectoNetBanking.domain.transacciones.TipoTransaccion;
import proyectoNetBanking.domain.transacciones.Transaccion;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Long countByFechaBetween(LocalDateTime inicioDia, LocalDateTime finDia);

    Long countByTipoTransaccion(TipoTransaccion tipoTransaccion);

    // Metodo para contar transacciones de tipo "pago" en un rango de fechas
    @Query("SELECT COUNT(t) " +
            "FROM Transaccion t " +
            "WHERE t.tipoTransaccion " +
            "IN :tiposPagos " +
            "AND t.fecha BETWEEN :inicio AND :fin")
    Long countByTipoTransaccionInAndFechaBetween(
            @Param("tiposPagos") List<TipoTransaccion> tiposPagos,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}

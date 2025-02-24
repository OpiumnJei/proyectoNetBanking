package proyectoNetBanking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;

import java.util.List;
import java.util.Optional;

public interface CuentaAhorroRepository extends JpaRepository<CuentaAhorro, Long> {

    //verificar si el id existe en la bd
    boolean existsByIdProducto(String idProducto);

    //trae una lista de cuentas asociadas a idUsuario
    List<CuentaAhorro> findByUsuarioId(Long idUsuario);

    //se hace un conteo de los registros en la tabla de cuentas_ahorro que coincidan con el idUsario
    int countByUsuarioId(Long idUsuario);

    Optional<CuentaAhorro> findByIdProducto(String numeroCuenta);

    Long countByEstadoProductoId(Long estadoId);
}

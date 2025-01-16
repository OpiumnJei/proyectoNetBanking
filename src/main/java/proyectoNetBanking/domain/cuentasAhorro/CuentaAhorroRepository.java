package proyectoNetBanking.domain.cuentasAhorro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaAhorroRepository extends JpaRepository<CuentaAhorro, Long> {

    //verificar si el id existe en la bd
    boolean existsByIdProducto(String idProducto);

    //retorna true
//    boolean existsByIdProducto();


    //verificar si el hay un usuario relacionado a la cuenta de ahorro
//    boolean existsByUsuarioId(Long id);

    //trae una lista de cuentas asociadas a idUsuario
    List<CuentaAhorro> findByUsuarioId(Long idUsuario);

    //se hace un conteo de los registros en la tabla de cuentas_ahorro que coincidan con el idUsario
    int  countByUsuarioId(Long idUsuario);
}

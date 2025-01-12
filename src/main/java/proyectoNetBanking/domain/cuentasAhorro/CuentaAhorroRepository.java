package proyectoNetBanking.domain.cuentasAhorro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaAhorroRepository extends JpaRepository<CuentaAhorro, Long> {

    //verificar si el id existe en la bd
    boolean existsByIdProducto(String idProducto);

    //verificar si el hay un usuario relacionado a la cuenta de ahorro
    boolean existsByUsuarioId(Long id);


    List<CuentaAhorro> findByUsuarioId(Long idUsuario);
}

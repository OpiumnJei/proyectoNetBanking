package proyectoNetBanking.domain.cuentasAhorro;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaAhorroRepository extends JpaRepository<CuentaAhorro, Long> {

    //verificar si el id existe en la bd
    boolean existsByIdProducto(String idProducto);
}

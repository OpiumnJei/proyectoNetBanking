package proyectoNetBanking.service.cuentasAhorro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.dto.cuentasAhorro.DatosCuentaAhorroDTO;
import proyectoNetBanking.dto.cuentasAhorro.DatosEliminarCuentaDTO;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class CuentaAhorroService {

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GeneradorId generadorId; //inyectamos el generador de ids

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;


    //crear una cuenta de ahorro
    @Transactional
    public void CrearCuentaNoPrincipal(DatosCuentaAhorroDTO datosCuentasAhorroDTO) {

        //verificar si el usuario existe
        Usuario usuario = usuarioRepository.findById(datosCuentasAhorroDTO.usuarioId())
                .orElseThrow(() -> new UsuarioNotFoundException("El id proporcionado no existe se encuentra en el sistema."));

        // Validar límite de cuentas
        int maxCuentas = 5; // Límite máximo permitido
        if (cuentaAhorroRepository.countByUsuarioId(usuario.getId()) >= maxCuentas) {
            throw new RuntimeException("El usuario ya alcanzo el límite de cuentas permitidas.");
        }

        CuentaAhorro cuentaNoPrincipal = new CuentaAhorro();
        cuentaNoPrincipal.setIdProducto(generarIdUnicoProducto());
        cuentaNoPrincipal.setUsuario(usuario);
        cuentaNoPrincipal.setSaldoDisponible((datosCuentasAhorroDTO.montoCuenta()));
        cuentaNoPrincipal.setProposito(datosCuentasAhorroDTO.proposito());
        cuentaNoPrincipal.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));
        cuentaAhorroRepository.save(cuentaNoPrincipal);
    }

    //metodo encargado de la gestion de estados
    public EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }

    @Transactional
    public void eliminarCuenta(DatosEliminarCuentaDTO datosEliminarCuenta) {

        // Verificar si el usuario existe
        Usuario usuario = usuarioRepository.findById(datosEliminarCuenta.idUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException("El usuario no existe"));

        // Verificar si la cuenta secundaria existe
        CuentaAhorro cuentaSecundaria = cuentaAhorroRepository.findById(datosEliminarCuenta.idCuenta())
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe"));

        // Verificar que la cuenta pertenece al usuario
        if (!cuentaSecundaria.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("La cuenta no pertenece al usuario especificado");
        }

        // Verificar si es la cuenta principal
        if (cuentaSecundaria.isEsPrincipal()) {
            throw new RuntimeException("Es la cuenta principal del usuario, no puede ser eliminada");
        }

        //Se usa compareTo para la comprobar que el saldo de la cuenta sea cero
        if (BigDecimal.ZERO.compareTo(cuentaSecundaria.getSaldoDisponible()) == 0) { // Si el saldo de la cuenta secundaria es cero
            cuentaSecundaria.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Eliminación lógica directa
            cuentaAhorroRepository.save(cuentaSecundaria);
        } else {

            // encontrar la cuenta principal del usuario
            CuentaAhorro cuentaPrincipal = cuentaAhorroRepository.findByUsuarioId(usuario.getId()).stream()
                    .filter(CuentaAhorro::isEsPrincipal)
                    .findFirst()//extrae el primer registro en donde esPrincipal = true
                    .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));

            // Transferir saldo
            BigDecimal nuevoSaldoPrincipal = (cuentaPrincipal.getSaldoDisponible().add(cuentaSecundaria.getSaldoDisponible()));
            cuentaPrincipal.setSaldoDisponible(nuevoSaldoPrincipal); //se setea el nuevo saldo
            cuentaAhorroRepository.save(cuentaPrincipal);//luego se guardan los cambios efectuados en esa cuenta

            cuentaSecundaria.setSaldoDisponible(BigDecimal.ZERO); //hacer 0 el saldo para que cobre mas sentido la transferencia entre cuentas
            cuentaSecundaria.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name())); // Inactivar la cuenta secundaria
            cuentaAhorroRepository.save(cuentaSecundaria);

//            // Registrar operación
//            logger.info("Saldo transferido de la cuenta secundaria a la principal. ID Usuario: {}, ID Cuenta Principal: {}, ID Cuenta Secundaria: {}",
//                    usuario.getId(), cuentaPrincipal.getId(), cuentaSecundaria.getId());
        }

    }

    //generar id del producto
    public String generarIdUnicoProducto() {

        return generadorId.generarIdUnicoProducto(cuentaAhorroRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }
}



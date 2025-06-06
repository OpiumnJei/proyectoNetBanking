package proyectoNetBanking.service.prestamos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.dto.prestamos.PrestamoResponseDTO;
import proyectoNetBanking.infra.errors.DatosInvalidosException;
import proyectoNetBanking.infra.errors.UsuarioInactivoException;
import proyectoNetBanking.repository.CuentaAhorroRepository;
import proyectoNetBanking.dto.prestamos.DatosPrestamoDTO;
import proyectoNetBanking.domain.prestamos.Prestamo;
import proyectoNetBanking.repository.PrestamoRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.repository.EstadoProductoRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.repository.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class PrestamoService {

    //monto minimo del prestamo
    private final BigDecimal MONTO_MINIMO_PRESTAMO = BigDecimal.valueOf(1000);
    //cantidad max de préstamos activos que un usuario puede tener
    private final int CANT_MAX_PRESTAMOS_ACTIVOS = 2;
    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GeneradorId generadorId;

    @Autowired
    private CuentaAhorroRepository cuentaAhorroRepository;

    @Autowired
    private EstadoProductoRepository estadoProductoRepository;

    @Transactional
    public PrestamoResponseDTO crearPrestamo(Long usuarioId, DatosPrestamoDTO datosPrestamoDTO) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new DatosInvalidosException("El ID del usuario no puede ser nulo ni un número negativo");
        }

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNotFoundException()); // en lugar de usar la lambda tambien se puede usar UsuarioNotFoundException::new

        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException("El usuario introducido se encuentra inactivo");
        }
        // Validar que el monto solicitado sea mayor o igual a 1,000 DOP
        if (datosPrestamoDTO.montoPrestamo().compareTo(MONTO_MINIMO_PRESTAMO) < 0) {
            throw new RuntimeException("El monto introducido es menor al monto minimo aceptado.");
        }

        //validar que el usuario no tenga mas de dos prestamos activos
        if (prestamoRepository.countByEstadoProductoId(1L) >= CANT_MAX_PRESTAMOS_ACTIVOS) { //1L es el id del estado activo
            throw new RuntimeException("El usuario ya alcanzo el límite de prestamos permitidos");
        }

        //crear instancia de un prestamo
        Prestamo prestamoCreado = asignarPrestamo(usuario, datosPrestamoDTO);

        //se traslada el dinero a la cuenta principal
        trasladarMontoACuentaPrincipal(prestamoCreado.getMontoPrestamo(), usuario.getId());

        return new PrestamoResponseDTO(
                prestamoCreado.getUsuario().getId(),
                prestamoCreado.getIdProducto(),
                prestamoCreado.getMontoPrestamo(),
                prestamoCreado.getMontoApagar(),
                prestamoCreado.getCreated()
        );
    }

    private Prestamo asignarPrestamo(Usuario usuario, DatosPrestamoDTO datosPrestamoDTO) {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdProducto(generarIdUnicoProducto());
        prestamo.setMontoPrestamo(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoApagar(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoPagado(BigDecimal.ZERO);
        prestamo.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));
        prestamo.setUsuario(usuario);

        return prestamoRepository.save(prestamo);
    }


    private void trasladarMontoACuentaPrincipal(BigDecimal monto, Long idUsuario) {

        //buscar cuenta principal
        CuentaAhorro cuentaPrincipal = cuentaAhorroRepository.findByUsuarioId(idUsuario)
                .stream()
                .filter(CuentaAhorro::isEsPrincipal)
                .findFirst()//extrae el primer registro en donde esPrincipal = true
                .orElseThrow(() -> new CuentaNotFoundException("No se encontró una cuenta principal para este usuario"));

        // Transferir monto del prestamo a la cuenta principal
        cuentaPrincipal.setSaldoDisponible(cuentaPrincipal.getSaldoDisponible().add(monto));

        cuentaAhorroRepository.save(cuentaPrincipal);
    }


    //metodo encargado de la gestion de estados
    private EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }


    //generar id del producto
    private String generarIdUnicoProducto() {
       /*
       Esta linea -> return generadorId.generarIdUnicoProducto(id -> prestamo.existsByIdProducto(id));
       Hace lo mismo que la linea de abajo:
       *  */
        return generadorId.generarIdUnicoProducto(prestamoRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }
}

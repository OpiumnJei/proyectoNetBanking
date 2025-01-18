package proyectoNetBanking.domain.prestamos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import proyectoNetBanking.domain.common.GeneradorId;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorro;
import proyectoNetBanking.domain.cuentasAhorro.CuentaAhorroRepository;
import proyectoNetBanking.domain.productos.EstadoProducto;
import proyectoNetBanking.domain.productos.EstadoProductoEnum;
import proyectoNetBanking.domain.productos.EstadoProductoRepository;
import proyectoNetBanking.domain.usuarios.Usuario;
import proyectoNetBanking.domain.usuarios.UsuarioRepository;
import proyectoNetBanking.infra.errors.CuentaNotFoundException;
import proyectoNetBanking.infra.errors.UsuarioNotFoundException;

import java.math.BigDecimal;

@Service
public class PrestamoService {

    //monto minimo del prestamo
    private final BigDecimal MONTO_MINIMO_PRESTAMO = BigDecimal.valueOf(1000);
    //cantidad max de prestamos activos que un usuario puede tener
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
    public void crearPrestamo(DatosPrestamoDTO datosPrestamoDTO) {

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(datosPrestamoDTO.idUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException()); // en lugar de usar la lambda tambien se puede usar UsuarioNotFoundException::new

        // Validar que el monto solicitado sea mayor o igual a 1,000 DOP
        if (datosPrestamoDTO.montoPrestamo().compareTo(MONTO_MINIMO_PRESTAMO) < 0) {
            throw new RuntimeException("El monto introducido es menor al monto minimo aceptado.");
        }

        //validar que el usuario no tenga mas de dos prestamos activos
        if (prestamoRepository.countByEstadoProductoId(1L) >= CANT_MAX_PRESTAMOS_ACTIVOS) { //1L es el id del estado activo
            throw new RuntimeException("El usuario ya alcanzo el límite de prestamos permitidos");
        }

        //crear instancia de un prestamo
        Prestamo prestamo = new Prestamo();
        prestamo.setIdProducto(generarIdUnicoProducto());
        prestamo.setMontoPrestamo(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoApagar(datosPrestamoDTO.montoPrestamo());
        prestamo.setMontoPagado(BigDecimal.ZERO);
        prestamo.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.ACTIVO.name()));
        prestamo.setUsuario(usuario);

        prestamoRepository.save(prestamo);
        //se traslada el dinero a la cuenta principal
        trasladarMontoACuentaPrincipal(prestamo.getMontoPrestamo(), usuario.getId());
    }


    public void trasladarMontoACuentaPrincipal(BigDecimal monto, Long idUsuario) {

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

    //validar datos enviados por el admin
    public void validarPagoPrestamo(DatosPagoPrestamoDTO datosPagoPrestamoDTO) {

        //verificar que el usuario exista en la bd
        Usuario usuario = usuarioRepository.findById(datosPagoPrestamoDTO.idUsuario())
                .orElseThrow(() -> new UsuarioNotFoundException());

        //verificar que el prestamo exista
        Prestamo prestamo = prestamoRepository.findById(datosPagoPrestamoDTO.idPrestamo())
                .orElseThrow(() -> new RuntimeException("El prestamo no existe."));

        if(prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) == 0){
            throw new RuntimeException("El monto del prestamo ya ha sido saldado.");
        }
        //verificar que el prestamo pertenezca al usuario
        if (!prestamo.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("El prestamo especificado no pertenece al usuario");
        }
        //verificar que la cuenta exista
        CuentaAhorro cuentaAhorro = cuentaAhorroRepository.findById(datosPagoPrestamoDTO.idCuentaUsuario())
                .orElseThrow(() -> new CuentaNotFoundException("La cuenta de ahorro no existe."));


        //verificar que la cuenta pertenezca al usuario
        if (!cuentaAhorro.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("La cuenta especificada no pertenece al usuario");
        }

        //verificar que Si el saldo disponible en la cuenta es menor al monto enviado por usuario
        if (cuentaAhorro.getSaldoDisponible().compareTo(datosPagoPrestamoDTO.montoPago()) < 0) {
            throw new RuntimeException("El monto que se quiere pagar es mayor que el monto disponible en la cuenta");
        }

        //realizar el pago
        registrarPagoPrestamo(cuentaAhorro, prestamo, datosPagoPrestamoDTO.montoPago());
    }


    //pagar un prestamo y cambiar su estado
    private void registrarPagoPrestamo(CuentaAhorro cuentaAhorro, Prestamo prestamo, BigDecimal montoPago) {
        //restar el monto pagado a la cuenta de ahorro
        cuentaAhorro.setSaldoDisponible(cuentaAhorro.getSaldoDisponible().subtract(montoPago));
        cuentaAhorroRepository.save(cuentaAhorro);

        //sumar el monto pagado por el usuario al campo correspondiente en la entidad prestamo
        prestamo.setMontoPagado(prestamo.getMontoPagado().add(montoPago));

        //restar la cantidad pagada por el usuario al monto original del prestamo
        prestamo.setMontoApagar(prestamo.getMontoPrestamo().subtract(montoPago));

        if (prestamo.getMontoApagar().compareTo(BigDecimal.ZERO) == 0) { //si la cantidad a pagar es 0, quiere decir que ya esta pagp
            prestamo.setEstadoProducto(colocarEstadoProductos(EstadoProductoEnum.INACTIVO.name()));
        }

        prestamoRepository.save(prestamo); //guardar en la bd
    }

    //metodo encargado de la gestion de estados
    public EstadoProducto colocarEstadoProductos(String nombreEstado) {

        return estadoProductoRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new RuntimeException("El estado no existe"));
    }


    //generar id del producto
    public String generarIdUnicoProducto() {
       /*
       Esta linea -> return generadorId.generarIdUnicoProducto(id -> prestamo.existsByIdProducto(id));
       Hace lo mismo que la linea de abajo:
       *  */
        return generadorId.generarIdUnicoProducto(prestamoRepository::existsByIdProducto); //se traduce del repositorio toma el metodo existsbyIdProducto como una referencia
    }
}

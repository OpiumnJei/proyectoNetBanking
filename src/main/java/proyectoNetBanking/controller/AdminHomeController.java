package proyectoNetBanking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proyectoNetBanking.service.usuarios.admin.IndicadoresAdminService;

import java.util.Map;

@RestController
@RequestMapping("/netbanking/indicadores")
public class AdminHomeController {

    @Autowired
    private IndicadoresAdminService indicadoresAdminService;

    /**
     * Endpoint para obtener el total de transacciones y las transacciones del día.
     *
     * @return Un Map con el total de transacciones y las transacciones del día.
     */
    @GetMapping("/transacciones")
    public Map<String, Long> listarTotalTransacciones() {
        return indicadoresAdminService.ListarTotalTransacciones();
    }

    /**
     * Endpoint para obtener el total de pagos realizados.
     *
     * @return Un Map con el total de pagos expresos, pagos con tarjeta, pagos de préstamos y pagos a beneficiarios.
     */
    @GetMapping("/pagos")
    public Map<String, Long> listarTotalPagos() {
        return indicadoresAdminService.listarTotalPagos();
    }

    /**
     * Endpoint para obtener la cantidad de clientes activos e inactivos.
     *
     * @return Un Map con la cantidad de clientes activos e inactivos.
     */
    @GetMapping("/clientes")
    public Map<String, Long> listarClientes() {
        return indicadoresAdminService.listarClientes();
    }

    /**
     * Endpoint para obtener la cantidad de productos activos asignados a los clientes.
     *
     * @return Un Map con la cantidad de cuentas de ahorro activas, préstamos activos y tarjetas activas.
     */

    @GetMapping("/productos-asignados")
    public Map<String, Long> listarProductosAsignados() {
        return indicadoresAdminService.listarProductosAsignados();

    }
}



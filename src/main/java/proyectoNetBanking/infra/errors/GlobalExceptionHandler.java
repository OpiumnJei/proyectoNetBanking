package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de solicitud sin cuerpo (body vacío)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "El cuerpo de la solicitud está vacío o mal formado.");
        errorResponse.put("solución", "Asegúrate de enviar un JSON válido con los campos requeridos.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //bad request(400) error del lado del cliente, para que las validaciones funcionen en el dto
    @ExceptionHandler(MethodArgumentNotValidException.class)//clase de la excepcion
    public ResponseEntity<Map<String, String>> tratarError400(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult() //obtiene un objeto que contiene los errores de validación lanzados por la excepcion
                .getFieldErrors() //lista de errores obtenidos
                .forEach(error ->
                        errors.put(error.getField(), //nombre del campo donde fallo la validacion
                                error.getDefaultMessage()) //mensaje de error del campo asociado
                );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors); // se retorna un 400, y el mapa de errores
    }

    /**
     * (rutas no encontradas)
     * NoHandlerFoundException.class  es una clase que maneja excepciones no encontradas
     * (es decir, que no son manejadas directamente por springboot)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> TratarExcepcionesDeEnrutamiento(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("La ruta solicitada no existe: " + ex.getRequestURL());
    }

    //tratar 404 usuario no encontrado
    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<String> tratarUsuarioNoEncontrado(UsuarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //tratar 404 cuenta no encontrada
    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<String> tratarCuentaNoEncontrada(CuentaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductosNotFoundException.class)
    public ResponseEntity<String> tratarProductoNoEncontrado(ProductosNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(BeneficiarioNotFoundException.class)
    public ResponseEntity<String> tratarBeneficiarioNoEncontrado(BeneficiarioNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<String> tratarUsuarioInactivo(UsuarioInactivoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CuentaInactivaException.class)
    public ResponseEntity<String> tratarCuentaAhorroInactiva(CuentaInactivaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<String> tratarDatosInvalidos(DatosInvalidosException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Manejar MissingPathVariableException (cuando falta una path variable)
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<String> handleMissingPathVariable(MissingPathVariableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Falta la variable de ruta: " + ex.getVariableName());
    }

    // Manejar MethodArgumentTypeMismatchException (cuando el tipo de la path variable es incorrecto)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("El valor de la variable de ruta es inválido: " + ex.getName());
    }

    // tratar 409
    @ExceptionHandler(BeneficiarioAlreadyExistsException.class)
    public ResponseEntity<String> tratarBeneficiarioExistente(BeneficiarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

}

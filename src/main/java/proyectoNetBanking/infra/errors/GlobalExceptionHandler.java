package proyectoNetBanking.infra.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> tratarError404(UsuarioNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //tratar 404 cuenta no encontrada
    @ExceptionHandler(CuentaNotFoundException.class)
    public ResponseEntity<String> tratarError404(CuentaNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ProductosNotFoundException.class)
    public ResponseEntity<String> tratarError404(ProductosNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Manejar IllegalArgumentException (cuando el usuarioId es nulo)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarUsuarioIdInvalido(IllegalArgumentException ex) {
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

}

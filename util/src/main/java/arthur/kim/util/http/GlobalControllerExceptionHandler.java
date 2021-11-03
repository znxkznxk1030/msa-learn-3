package arthur.kim.util.http;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import arthur.kim.util.exceptions.InvalidInputException;
import arthur.kim.util.exceptions.NotFoundException;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	@ResponseBody
	public ResponseEntity<?> handleNotFoundException(Exception ex) {
		return new ResponseEntity<>(ex, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InvalidInputException.class)
	@ResponseBody
	public ResponseEntity<?> handleInvalidInputException(Exception ex) {
		return new ResponseEntity<>(ex, HttpStatus.UNPROCESSABLE_ENTITY);
	}

}

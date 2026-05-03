package roshan.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class MvcExceptionHandler {
	
	@ExceptionHandler
	public String EmployeeNotFound(DataIntegrityViolationException e, Model model ) {
		
		 e.printStackTrace(); 
		
		model.addAttribute("message", "unable to save employee");
		
		return "error-page";
	}
	
	@ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e, Model model) {
		 e.printStackTrace(); 
        model.addAttribute("message", "Something went wrong");
        return "error-page";
    }
	
	@ExceptionHandler(EmployeeNotFoundException.class)
	public String handleEmployeeNotFound(EmployeeNotFoundException e, Model model) {
		 e.printStackTrace(); 
		model.addAttribute("message", e.getMessage());
		return "error-page";
	}

}

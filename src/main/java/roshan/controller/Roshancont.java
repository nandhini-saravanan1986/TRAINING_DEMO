package roshan.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import roshan.entities.Roshanent;
import roshan.service.Roshansrv;

// ✅ FIX 4a: Removed "import javassist.expr.NewArray;" — it was unused and causes compile errors
// ✅ FIX 4b: Changed @PutMapping to @PostMapping — HTML forms only support GET and POST.
//            Using @PutMapping from a Thymeleaf form causes a 405 Method Not Allowed error.

@Controller
public class Roshancont {

    private Roshansrv employeeService;

    public Roshancont(Roshansrv employeeService) {
        super();
        this.employeeService = employeeService;
    }

    // Show list of all employees
    @GetMapping("/employeeDetails")
    public String employeeDetails(Model model) {
        List<Roshanent> employees = employeeService.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("formmode", "list");
        return "employeePage";
    }

    // Show blank Add form
    @GetMapping("/empForm")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Roshanent());
        model.addAttribute("formmode", "add");
        return "employeePage";
    }

    // Save new employee
    @PostMapping("/empSave")
    public String saveEmployee(@ModelAttribute("employee") Roshanent emp) {
        employeeService.save(emp);
        return "redirect:/employeeDetails";
    }

    // Load existing data into edit form
    @GetMapping("/empForm/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Roshanent emp = employeeService.findEmp(id);
        model.addAttribute("employee", emp);
        model.addAttribute("formmode", "edit");
        return "employeePage";
    }

    // Save updated employee (POST instead of PUT — HTML forms don't support PUT)
    @PostMapping("/empUpdate/{id}")
    public String updateEmployee(@PathVariable Long id,
                                 @ModelAttribute("employee") Roshanent emp) {
        emp.setId(id);
        employeeService.save(emp);
        return "redirect:/employeeDetails";
    }

    // Delete employee — called via JavaScript fetch() from the page
    @DeleteMapping("/empDelete/{id}")
    @ResponseBody
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
        return "success";
    }
}

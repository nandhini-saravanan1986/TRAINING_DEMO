package com.bornfire.brf.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bornfire.brf.entities.Employee;
import com.bornfire.brf.entities.EmployeeRepository;
import com.bornfire.brf.entities.UserProfile;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    public List<Employee> getAllEmployees(){
        return repo.findAll();
    }

    public Employee saveEmployee(Employee emp){
        return repo.save(emp);
    }

    public Employee updateEmployee(Long id, Employee emp){
        Optional<Employee> e = repo.findById(id);

        if(e.isPresent()){
            Employee employee = e.get();
            employee.setName(emp.getName());
            employee.setEmail(emp.getEmail());
            employee.setDepartment(emp.getDepartment());
            employee.setSalary(emp.getSalary());
            return repo.save(employee);
        }
        return null;
    }

    public void deleteEmployee(Long id){
        repo.deleteById(id);
    }
    
    
    public Employee getUser(Long id) {
        Optional<Employee> emp = repo.findById(id);

        if(emp.isPresent()) {
            return emp.get();
        } else {
            return null;
        }
    }
    
}

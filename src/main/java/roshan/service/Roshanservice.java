package roshan.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roshan.entities.Roshanent;
import roshan.exception.EmployeeNotFoundException;
import roshan.repo.Roshanjpa;

@Service
public class Roshanservice implements Roshansrv {

    private Roshanjpa employeeRepo;

    public Roshanservice(Roshanjpa employeeRepo) {
        super();
        this.employeeRepo = employeeRepo;
    }

    @Override
    public List<Roshanent> findAll() {
        return employeeRepo.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    @Override
    public Roshanent save(Roshanent employee) {
        return employeeRepo.save(employee);
    }

    // ✅ FIX 3: Changed int → Long in both methods to match Roshanent @Id (Long)
    @Override
    public void deleteById(Long id) {
        Roshanent employee = employeeRepo.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee Id not found: " + id));
        employeeRepo.delete(employee);
    }

    @Override
    public Roshanent findEmp(Long id) {
        return employeeRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }
}

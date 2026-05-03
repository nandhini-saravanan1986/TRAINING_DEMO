package roshan.service;

import java.util.List;
import roshan.entities.Roshanent;

public interface Roshansrv {

    List<Roshanent> findAll();

    Roshanent save(Roshanent employee);

    // ✅ FIX 2: Changed int → Long to match the @Id field type in Roshanent
    Roshanent findEmp(Long id);

    void deleteById(Long id);
}

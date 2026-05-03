package roshan.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import roshan.entities.Roshanent;

@Repository
public interface Roshanjpa extends JpaRepository<Roshanent, Long> {
    // findAll(), findById(), save(), delete() — all free from JpaRepository
}
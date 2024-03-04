package org.example.examenejercicio2;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEstado(String estado);

    Cliente findByIdAndEstado(Long id, String estado);
}

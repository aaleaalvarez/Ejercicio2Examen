package org.example.examenejercicio2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/web")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping("/")
    public ResponseEntity<?> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteRepository.save(cliente);
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ventas")
    public ResponseEntity<?> obtenerClientesPorVentas(@RequestParam BigDecimal ventas) {
        List<Cliente> clientes = clienteRepository.findAll().stream()
                .filter(c -> c.getTotal().compareTo(ventas) > 0)
                .collect(Collectors.toList());
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas() {
        List<Cliente> clientes = clienteRepository.findAll();
        BigDecimal totalVentas = clientes.stream()
                .map(Cliente::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedioVentasActivos = BigDecimal.ZERO;
        long activosCount = clientes.stream().filter(c -> "activo".equals(c.getEstado())).count();
        if (activosCount > 0) {
            promedioVentasActivos = clientes.stream()
                    .filter(c -> "activo".equals(c.getEstado()))
                    .map(Cliente::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(activosCount), BigDecimal.ROUND_HALF_UP);
        }
        long cantidadInactivos = clientes.stream()
                .filter(c -> "inactivo".equals(c.getEstado()) && c.getTotal().compareTo(BigDecimal.ZERO) > 0)
                .count();
        Estadisticas estadisticas = new Estadisticas(totalVentas, promedioVentasActivos, cantidadInactivos);

        return new ResponseEntity<>(estadisticas, HttpStatus.OK);
    }

    static class Estadisticas {
        private final BigDecimal totalVentas;
        private final BigDecimal promedioVentasActivos;
        private final long cantidadInactivos;

        public Estadisticas(BigDecimal totalVentas, BigDecimal promedioVentasActivos, long cantidadInactivos) {
            this.totalVentas = totalVentas;
            this.promedioVentasActivos = promedioVentasActivos;
            this.cantidadInactivos = cantidadInactivos;
        }

        public BigDecimal getTotalVentas() {
            return totalVentas;
        }

        public BigDecimal getPromedioVentasActivos() {
            return promedioVentasActivos;
        }

        public long getCantidadInactivos() {
            return cantidadInactivos;
        }
    }
}

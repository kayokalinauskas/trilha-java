package dev.trilhajava.m09.pedidos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByClienteIdOrderByCriadoEmDescIdDesc(long clienteId, Pageable pageable);
}

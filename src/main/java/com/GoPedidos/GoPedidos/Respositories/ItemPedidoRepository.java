package com.GoPedidos.GoPedidos.Respositories;

import com.GoPedidos.GoPedidos.Models.ItemPedido;
import com.GoPedidos.GoPedidos.Models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}

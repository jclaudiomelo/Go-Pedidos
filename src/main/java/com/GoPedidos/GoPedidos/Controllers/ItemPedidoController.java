package com.GoPedidos.GoPedidos.Controllers;

import com.GoPedidos.GoPedidos.DTOS.ItemPedidoDTO;
import com.GoPedidos.GoPedidos.Models.ItemPedido;
import com.GoPedidos.GoPedidos.Services.ItemPedidoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("item-pedido")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ItemPedidoController {

	private final ItemPedidoService itemPedidoService;

	@PreAuthorize("hasAnyRole('MASTER', 'GERENTE')")
	@PostMapping("/criar")
	public ResponseEntity<ItemPedido> criarItemPedido(@RequestBody @Valid ItemPedidoDTO dto) {
		ItemPedido itemPedidoCriado = itemPedidoService.criarItemPedido(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(itemPedidoCriado);
	}

	@PreAuthorize("hasAnyRole('MASTER', 'GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<ItemPedido> editarItemPedido(@PathVariable Long id, @RequestBody @Valid ItemPedidoDTO dto) {
		ItemPedido itemAtualizado = itemPedidoService.editarItemPedido(id, dto);
		return ResponseEntity.ok(itemAtualizado);
	}

	@PreAuthorize("hasAnyRole('MASTER', 'GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletarItemPedido(@PathVariable Long id) {
		itemPedidoService.deletarItemPedido(id);
		return ResponseEntity.noContent().build();
	}
}

package dev.trilhajava.m10.aulas.fatias;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final CatalogoDeProdutos catalogo;

    public ProdutoController(CatalogoDeProdutos catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/{sku}")
    public ProdutoResposta buscar(@PathVariable String sku) {
        return catalogo.buscar(sku);
    }

    @GetMapping
    public List<ProdutoResposta> listar() {
        return catalogo.listarAtivos();
    }

    @PostMapping
    public ResponseEntity<ProdutoResposta> cadastrar(@Valid @RequestBody NovoProduto novo) {
        ProdutoResposta criado = catalogo.cadastrar(novo);
        return ResponseEntity.created(URI.create("/produtos/" + criado.sku())).body(criado);
    }

    @PostMapping("/{sku}/baixas")
    public SaldoDeEstoque baixar(@PathVariable String sku, @Valid @RequestBody BaixaDeEstoque baixa) {
        int saldo = catalogo.baixarEstoque(sku, baixa.quantidade());
        return new SaldoDeEstoque(sku, saldo);
    }
}

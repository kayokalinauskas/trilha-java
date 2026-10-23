package dev.trilhajava.m13.aulas.ddd.pedidos.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Raiz do agregado Pedido. Toda mudança passa por um método daqui, que protege as invariantes:
 * <ol>
 *   <li>itens só mudam enquanto o pedido é RASCUNHO;</li>
 *   <li>o mesmo SKU aparece uma vez só (quantidades se somam) e sempre com o mesmo preço;</li>
 *   <li>no máximo 20 itens distintos e total de até R$ 50.000,00;</li>
 *   <li>só se confirma um pedido com pelo menos um item;</li>
 *   <li>cancelar um pedido já cancelado não faz nada (nem gera evento).</li>
 * </ol>
 */
public final class Pedido {

    public static final int MAXIMO_DE_ITENS = 20;
    public static final Dinheiro LIMITE_POR_PEDIDO = Dinheiro.de("50000.00");

    private final PedidoId id;
    private final ClienteId cliente;
    private final List<ItemDoPedido> itens = new ArrayList<>();
    private StatusDoPedido status = StatusDoPedido.RASCUNHO;
    private final List<EventoDeDominio> eventos = new ArrayList<>();

    private Pedido(PedidoId id, ClienteId cliente) {
        this.id = Objects.requireNonNull(id, "id");
        this.cliente = Objects.requireNonNull(cliente, "cliente");
    }

    public static Pedido abrir(PedidoId id, ClienteId cliente) {
        return new Pedido(id, cliente);
    }

    public void adicionarItem(Sku sku, int quantidade, Dinheiro precoUnitario) {
        exigirRascunho("adicionar itens");
        ItemDoPedido novo = new ItemDoPedido(sku, quantidade, precoUnitario);

        List<ItemDoPedido> candidatos = new ArrayList<>(itens);      // valida numa cópia...
        int posicao = posicaoDo(sku);
        if (posicao >= 0) {
            ItemDoPedido atual = candidatos.get(posicao);
            if (!atual.precoUnitario().equals(precoUnitario)) {
                throw new RegraDoPedidoException("O SKU " + sku + " já está no pedido com o preço " + atual.precoUnitario());
            }
            candidatos.set(posicao, atual.comMais(quantidade));
        } else {
            if (candidatos.size() == MAXIMO_DE_ITENS) {
                throw new RegraDoPedidoException("Um pedido tem no máximo " + MAXIMO_DE_ITENS + " itens distintos");
            }
            candidatos.add(novo);
        }
        Dinheiro novoTotal = somar(candidatos);
        if (novoTotal.maiorQue(LIMITE_POR_PEDIDO)) {
            throw new RegraDoPedidoException("O total ficaria em " + novoTotal + ", acima do limite de " + LIMITE_POR_PEDIDO);
        }
        itens.clear();                                                // ...e só então altera o estado
        itens.addAll(candidatos);
    }

    public void confirmar(Instant agora) {
        exigirRascunho("confirmar");
        if (itens.isEmpty()) {
            throw new RegraDoPedidoException("Um pedido sem itens não pode ser confirmado");
        }
        status = StatusDoPedido.CONFIRMADO;
        eventos.add(new PedidoConfirmado(id, cliente, total(), agora));
    }

    public void cancelar(String motivo, Instant agora) {
        if (status == StatusDoPedido.CANCELADO) {
            return;
        }
        status = StatusDoPedido.CANCELADO;
        eventos.add(new PedidoCancelado(id, motivo, agora));
    }

    public Dinheiro total() {
        return somar(itens);
    }

    /** Devolve os eventos ainda não publicados e esvazia a lista: cada evento sai uma vez só. */
    public List<EventoDeDominio> retirarEventos() {
        List<EventoDeDominio> pendentes = List.copyOf(eventos);
        eventos.clear();
        return pendentes;
    }

    public PedidoId id() {
        return id;
    }

    public ClienteId cliente() {
        return cliente;
    }

    public StatusDoPedido status() {
        return status;
    }

    /** Uma cópia que não aceita alterações: ninguém de fora mexe nos itens sem passar pelas regras. */
    public List<ItemDoPedido> itens() {
        return List.copyOf(itens);
    }

    private void exigirRascunho(String acao) {
        if (status != StatusDoPedido.RASCUNHO) {
            throw new RegraDoPedidoException("Não é possível " + acao + ": o pedido " + id + " está " + status);
        }
    }

    private int posicaoDo(Sku sku) {
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).sku().equals(sku)) {
                return i;
            }
        }
        return -1;
    }

    private static Dinheiro somar(List<ItemDoPedido> itens) {
        Dinheiro total = Dinheiro.ZERO;
        for (ItemDoPedido item : itens) {
            total = total.mais(item.subtotal());
        }
        return total;
    }
}

package estoque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import estoque.excecoes.ProdutoIndisponivelException;
import estoque.produtos.Product;

/**
 * Estoque da loja.
 *
 * <p>Composição: o Estoque <b>TEM UMA</b> lista de produtos — ele não herda
 * de {@link Product}. O Estoque trabalha apenas com o tipo abstrato
 * {@code Product}, sem precisar saber qual é o subtipo de cada item.</p>
 */
public class Estoque {

    private final List<Product> produtos = new ArrayList<>();

    /**
     * Adiciona um produto ao estoque.
     */
    public void adicionarProduto(Product p) {
        if (p == null) {
            throw new IllegalArgumentException("Não é possível adicionar um produto nulo ao estoque.");
        }
        produtos.add(p);
    }

    /**
     * Vende a quantidade informada do produto na posição {@code indice},
     * delegando a venda ao método {@code vender()} do próprio produto.
     *
     * @throws ProdutoIndisponivelException se o produto não existir no estoque ou
     *                                      se não houver quantidade suficiente
     */
    public void venderProduto(int indice, int quantidade) throws ProdutoIndisponivelException {
        if (indice < 0 || indice >= produtos.size()) {
            throw new ProdutoIndisponivelException("Não existe produto no índice " + indice + " do estoque.");
        }
        produtos.get(indice).vender(quantidade);
    }

    /**
     * Soma o valor total de todos os produtos do estoque.
     *
     * <p>Polimorfismo dinâmico: cada produto calcula o seu valor do seu próprio
     * jeito (comum ou perecível) e o Estoque apenas soma os resultados.</p>
     */
    public double calcularValorTotalEstoque() {
        double total = 0;
        for (Product p : produtos) {
            total += p.calcularValorTotal();
        }
        return total;
    }

    /** Devolve os produtos do estoque (somente leitura). */
    public List<Product> getProdutos() {
        return Collections.unmodifiableList(produtos);
    }

    public int getQuantidadeDeProdutos() {
        return produtos.size();
    }
}

package estoque.produtos;

import estoque.excecoes.QuantidadeInvalidaException;

/**
 * Produto sem regra especial de cálculo: valor total = preço × quantidade.
 */
public class ProdutoComum extends Product {

    public ProdutoComum(String nome, double preco, int quantidade) throws QuantidadeInvalidaException {
        super(nome, preco, quantidade);
    }

    @Override
    public double calcularValorTotal() {
        return getPreco() * getQuantidade();
    }
}

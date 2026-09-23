package estoque.produtos;

import estoque.excecoes.ProdutoIndisponivelException;

/**
 * Contrato de tudo aquilo que pode ser vendido.
 */
public interface Vendavel {

    /**
     * Vende a quantidade desejada, subtraindo-a do estoque disponível.
     *
     * @param quantidadeDesejada quantidade a vender
     * @throws ProdutoIndisponivelException se a quantidade desejada for maior que o estoque disponível
     */
    void vender(int quantidadeDesejada)
        throws ProdutoIndisponivelException;
}

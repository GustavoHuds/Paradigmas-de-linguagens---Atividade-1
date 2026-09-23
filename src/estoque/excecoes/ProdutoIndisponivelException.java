package estoque.excecoes;

/**
 * Lançada quando se tenta vender uma quantidade maior do que a
 * disponível em estoque (ou um produto que não existe no estoque).
 */
public class ProdutoIndisponivelException extends EstoqueException {

    private static final long serialVersionUID = 1L;

    public ProdutoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}

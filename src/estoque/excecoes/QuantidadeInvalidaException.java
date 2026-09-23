package estoque.excecoes;

/**
 * Lançada quando um produto é criado com valores inválidos,
 * como preço ou quantidade negativos.
 */
public class QuantidadeInvalidaException extends EstoqueException {

    private static final long serialVersionUID = 1L;

    public QuantidadeInvalidaException(String mensagem) {
        super(mensagem);
    }
}

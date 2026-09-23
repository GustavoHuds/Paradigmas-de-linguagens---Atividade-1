package estoque.excecoes;

/**
 * Exceção base de todas as situações inválidas do sistema de estoque.
 *
 * <p>É uma exceção verificada (checked): quem chama um método que a lança
 * é obrigado a tratá-la com try/catch ou a declará-la com throws.</p>
 */
public class EstoqueException extends Exception {

    private static final long serialVersionUID = 1L;

    public EstoqueException(String mensagem) {
        super(mensagem);
    }
}

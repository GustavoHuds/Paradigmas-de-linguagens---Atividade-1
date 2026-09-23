package estoque.produtos;

import estoque.excecoes.QuantidadeInvalidaException;

/**
 * Produto com validade. Quando faltam poucos dias para vencer,
 * o seu valor total recebe um desconto automático.
 */
public class ProdutoPerecivel extends Product {

    /** Até quantos dias para o vencimento o desconto automático é aplicado. */
    public static final int DIAS_LIMITE_DESCONTO = 3;

    /** Desconto automático aplicado a produtos perto do vencimento (20%). */
    public static final double PERCENTUAL_DESCONTO_VENCIMENTO = 0.20;

    private int diasParaVencer;

    /**
     * @throws QuantidadeInvalidaException se o preço, a quantidade ou os dias para vencer forem negativos
     */
    public ProdutoPerecivel(String nome, double preco, int quantidade, int diasParaVencer)
            throws QuantidadeInvalidaException {
        super(nome, preco, quantidade);
        if (diasParaVencer < 0) {
            throw new QuantidadeInvalidaException(
                "Dias para vencer inválido para \"" + nome + "\": " + diasParaVencer
                    + " (não pode ser negativo; produto vencido não pode ser cadastrado).");
        }
        this.diasParaVencer = diasParaVencer;
    }

    /**
     * Valor total = preço × quantidade, com 20% de desconto automático
     * quando faltam {@value #DIAS_LIMITE_DESCONTO} dias ou menos para vencer.
     */
    @Override
    public double calcularValorTotal() {
        double total = getPreco() * getQuantidade();
        if (temDescontoPorVencimento()) {
            total -= total * PERCENTUAL_DESCONTO_VENCIMENTO;
        }
        return total;
    }

    /**
     * Inclui a validade na descrição herdada de {@link Product}.
     */
    @Override
    public String getDescricao() {
        String descricao = super.getDescricao() + " | Vence em: " + diasParaVencer + " dia(s)";
        if (temDescontoPorVencimento()) {
            descricao += String.format(LOCALE_BR, " -> %.0f%% de desconto (perto do vencimento)",
                PERCENTUAL_DESCONTO_VENCIMENTO * 100);
        }
        return descricao;
    }

    public boolean temDescontoPorVencimento() {
        return diasParaVencer <= DIAS_LIMITE_DESCONTO;
    }

    public int getDiasParaVencer() {
        return diasParaVencer;
    }
}

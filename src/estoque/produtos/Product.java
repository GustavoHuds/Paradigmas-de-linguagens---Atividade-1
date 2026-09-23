package estoque.produtos;

import java.util.Locale;

import estoque.excecoes.ProdutoIndisponivelException;
import estoque.excecoes.QuantidadeInvalidaException;

/**
 * Classe abstrata que representa um produto do estoque.
 *
 * <p>Não pode ser instanciada diretamente: cada tipo concreto de produto
 * (subclasse) decide como calcular o seu valor total, por meio do método
 * abstrato {@link #calcularValorTotal()}.</p>
 */
public abstract class Product implements Vendavel {

    /** Locale usado para formatar valores no padrão brasileiro (ex.: R$ 1.234,56). */
    protected static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    private String nome;
    private double preco;
    private int quantidade;

    /**
     * @throws QuantidadeInvalidaException se o preço ou a quantidade forem negativos
     */
    public Product(String nome, double preco, int quantidade) throws QuantidadeInvalidaException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (preco < 0) {
            throw new QuantidadeInvalidaException(
                "Preço inválido para \"" + nome + "\": " + formatarMoeda(preco) + " (não pode ser negativo).");
        }
        if (quantidade < 0) {
            throw new QuantidadeInvalidaException(
                "Quantidade inválida para \"" + nome + "\": " + quantidade + " (não pode ser negativa).");
        }
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    /**
     * Calcula o valor total deste produto em estoque.
     * Cada subclasse define a sua própria regra de cálculo.
     */
    public abstract double calcularValorTotal();

    /**
     * Devolve nome, preço e quantidade formatados.
     */
    public String getDescricao() {
        return String.format(LOCALE_BR, "%-22s | Preço: %12s | Quantidade: %4d un.",
            nome, formatarMoeda(preco), quantidade);
    }

    /**
     * Vende a quantidade desejada, subtraindo-a do estoque.
     *
     * @throws ProdutoIndisponivelException se a quantidade desejada for maior que a disponível
     * @throws IllegalArgumentException     se a quantidade desejada não for positiva
     */
    @Override
    public void vender(int quantidadeDesejada) throws ProdutoIndisponivelException {
        if (quantidadeDesejada <= 0) {
            throw new IllegalArgumentException("A quantidade a vender deve ser maior que zero.");
        }
        if (quantidadeDesejada > quantidade) {
            throw new ProdutoIndisponivelException(
                "Estoque insuficiente de \"" + nome + "\": solicitado " + quantidadeDesejada
                    + " un., disponível " + quantidade + " un.");
        }
        quantidade -= quantidadeDesejada;
    }

    /**
     * Sobrecarga 1: aplica um desconto percentual sobre o preço unitário.
     *
     * @param percentual percentual de desconto, entre 0 e 100 (ex.: 10 = 10%)
     */
    public void aplicarDesconto(double percentual) {
        validarPercentual(percentual);
        preco -= preco * percentual / 100.0;
    }

    /**
     * Sobrecarga 2: aplica um desconto percentual sobre o preço unitário,
     * limitado a um valor máximo (em reais).
     *
     * @param percentual     percentual de desconto, entre 0 e 100 (ex.: 10 = 10%)
     * @param descontoMaximo valor máximo, em reais, que pode ser descontado
     */
    public void aplicarDesconto(double percentual, double descontoMaximo) {
        validarPercentual(percentual);
        if (descontoMaximo < 0) {
            throw new IllegalArgumentException("O desconto máximo não pode ser negativo.");
        }
        double desconto = Math.min(preco * percentual / 100.0, descontoMaximo);
        preco -= desconto;
    }

    private static void validarPercentual(double percentual) {
        if (percentual < 0 || percentual > 100) {
            throw new IllegalArgumentException("O percentual de desconto deve estar entre 0 e 100.");
        }
    }

    /** Formata um valor em reais no padrão brasileiro (ex.: R$ 1.234,56). */
    protected static String formatarMoeda(double valor) {
        return String.format(LOCALE_BR, "R$ %,.2f", valor);
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public String toString() {
        return getDescricao();
    }
}

package estoque;

import java.util.List;
import java.util.Locale;

import estoque.excecoes.EstoqueException;
import estoque.excecoes.ProdutoIndisponivelException;
import estoque.excecoes.QuantidadeInvalidaException;
import estoque.produtos.Product;
import estoque.produtos.ProdutoComum;
import estoque.produtos.ProdutoPerecivel;

/**
 * Aplicação de demonstração do Sistema de Estoque de Produtos.
 */
public class EstoqueApp {

    private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
    private static final String LINHA = "==========================================================================";

    public static void main(String[] args) {
        Estoque estoque = new Estoque();

        System.out.println(LINHA);
        System.out.println("                    SISTEMA DE ESTOQUE DE PRODUTOS");
        System.out.println(LINHA);

        // ------------------------------------------------------------------
        // 1. Cadastro de 2 produtos comuns e 2 perecíveis
        // ------------------------------------------------------------------
        titulo("1. Cadastro de produtos");
        try {
            // Variáveis declaradas com o tipo abstrato Product (polimorfismo)
            Product arroz  = new ProdutoComum("Arroz 5kg", 27.90, 40);
            Product feijao = new ProdutoComum("Feijão Carioca 1kg", 8.49, 60);
            Product leite  = new ProdutoPerecivel("Leite Integral 1L", 5.79, 50, 2);       // vence em <= 3 dias
            Product queijo = new ProdutoPerecivel("Queijo Mussarela 500g", 24.90, 20, 15);

            estoque.adicionarProduto(arroz);
            estoque.adicionarProduto(feijao);
            estoque.adicionarProduto(leite);
            estoque.adicionarProduto(queijo);

            System.out.println(estoque.getQuantidadeDeProdutos() + " produtos cadastrados com sucesso:");
            imprimirProdutos(estoque);
            System.out.println("Valor total do estoque: " + moeda(estoque.calcularValorTotalEstoque()));
        } catch (QuantidadeInvalidaException e) {
            System.out.println("[ERRO] Falha no cadastro: " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // 2. Tentativa de cadastro inválido (captura QuantidadeInvalidaException)
        // ------------------------------------------------------------------
        titulo("2. Tentativa de cadastro com valores inválidos");
        try {
            System.out.println("Tentando cadastrar \"Açúcar 1kg\" com quantidade -5...");
            estoque.adicionarProduto(new ProdutoComum("Açúcar 1kg", 4.99, -5));
            System.out.println("Produto cadastrado (isso NÃO deveria acontecer).");
        } catch (QuantidadeInvalidaException e) {
            System.out.println("[QuantidadeInvalidaException capturada] " + e.getMessage());
        }
        try {
            System.out.println("Tentando cadastrar \"Iogurte Natural\" com preço -3,50...");
            estoque.adicionarProduto(new ProdutoPerecivel("Iogurte Natural", -3.50, 10, 5));
            System.out.println("Produto cadastrado (isso NÃO deveria acontecer).");
        } catch (QuantidadeInvalidaException e) {
            System.out.println("[QuantidadeInvalidaException capturada] " + e.getMessage());
        }
        System.out.println("Produtos no estoque após as tentativas: " + estoque.getQuantidadeDeProdutos()
            + " (nenhum produto inválido foi adicionado).");

        // ------------------------------------------------------------------
        // 3. Venda válida
        // ------------------------------------------------------------------
        titulo("3. Venda de uma quantidade válida");
        venderEImprimir(estoque, 0, 5);

        // ------------------------------------------------------------------
        // 4. Venda acima do disponível (captura ProdutoIndisponivelException)
        // ------------------------------------------------------------------
        titulo("4. Tentativa de venda acima do disponível");
        venderEImprimir(estoque, 2, 100);

        // ------------------------------------------------------------------
        // 5. Sobrecarga de aplicarDesconto() (polimorfismo estático)
        // ------------------------------------------------------------------
        titulo("5. Sobrecarga de aplicarDesconto()");
        List<Product> produtos = estoque.getProdutos();
        if (produtos.size() >= 4) {
            Product arroz = produtos.get(0);
            double precoAntes = arroz.getPreco();
            arroz.aplicarDesconto(10);
            System.out.println("aplicarDesconto(10) em \"" + arroz.getNome() + "\": "
                + moeda(precoAntes) + " -> " + moeda(arroz.getPreco()) + " (10% de desconto)");

            Product queijo = produtos.get(3);
            precoAntes = queijo.getPreco();
            queijo.aplicarDesconto(50, 5.00);
            System.out.println("aplicarDesconto(50, 5.00) em \"" + queijo.getNome() + "\": "
                + moeda(precoAntes) + " -> " + moeda(queijo.getPreco())
                + " (50% daria " + moeda(precoAntes * 0.5) + " de desconto, mas foi limitado a R$ 5,00)");
        }

        // ------------------------------------------------------------------
        // 6. Várias exceções no mesmo try: a mais genérica vem por último
        // ------------------------------------------------------------------
        titulo("6. Ordem dos catches (cadastro + venda no mesmo try)");
        cadastrarEVender(estoque, "Detergente 500ml", 2.49, -3, 1);
        cadastrarEVender(estoque, "Refrigerante 2L", 9.99, 12, 20);
        cadastrarEVender(estoque, "Macarrão 500g", 4.29, 25, 0);
        cadastrarEVender(estoque, "Café 500g", 18.75, 30, 6);

        // ------------------------------------------------------------------
        // 7. Estoque final e valor total (polimorfismo dinâmico)
        // ------------------------------------------------------------------
        titulo("7. Estoque final e valor total");
        imprimirProdutos(estoque);
        System.out.println();
        System.out.println("VALOR TOTAL DO ESTOQUE: " + moeda(estoque.calcularValorTotalEstoque()));
        System.out.println("(soma de calcularValorTotal() de cada produto: comuns = preço x quantidade;");
        System.out.println(" perecíveis com até " + ProdutoPerecivel.DIAS_LIMITE_DESCONTO
            + " dias para vencer recebem "
            + Math.round(ProdutoPerecivel.PERCENTUAL_DESCONTO_VENCIMENTO * 100) + "% de desconto automático)");
        System.out.println(LINHA);
    }

    /**
     * Vende um produto do estoque e mostra o resultado, tratando a falta de estoque.
     */
    private static void venderEImprimir(Estoque estoque, int indice, int quantidade) {
        try {
            System.out.println("Vendendo " + quantidade + " un. do produto no índice " + indice + "...");
            estoque.venderProduto(indice, quantidade);
            Product vendido = estoque.getProdutos().get(indice);
            System.out.println("[VENDA REALIZADA] " + quantidade + " un. de \"" + vendido.getNome()
                + "\". Restam " + vendido.getQuantidade() + " un. em estoque.");
        } catch (ProdutoIndisponivelException e) {
            System.out.println("[ProdutoIndisponivelException capturada] " + e.getMessage());
        }
    }

    /**
     * Cadastra um produto e já realiza uma venda dele, no mesmo bloco try.
     *
     * <p>Os catches vão do mais específico para o mais genérico: se
     * EstoqueException viesse antes, os catches seguintes ficariam
     * inalcançáveis e o código nem compilaria.</p>
     */
    private static void cadastrarEVender(Estoque estoque, String nome, double preco,
                                         int quantidade, int quantidadeVenda) {
        System.out.println("Pedido: cadastrar \"" + nome + "\" (" + quantidade + " un.) e vender "
            + quantidadeVenda + " un.");
        try {
            if (quantidadeVenda <= 0) {
                // Regra geral do pedido: não se encaixa em nenhuma exceção específica
                throw new EstoqueException("Pedido recusado: a quantidade de venda deve ser maior que zero.");
            }
            estoque.adicionarProduto(new ProdutoComum(nome, preco, quantidade));
            estoque.venderProduto(estoque.getQuantidadeDeProdutos() - 1, quantidadeVenda);
            System.out.println("  [OK] Produto cadastrado e venda realizada.");
        } catch (QuantidadeInvalidaException e) {
            System.out.println("  [QuantidadeInvalidaException capturada] " + e.getMessage());
        } catch (ProdutoIndisponivelException e) {
            System.out.println("  [ProdutoIndisponivelException capturada] " + e.getMessage());
        } catch (EstoqueException e) {
            // Mais genérica por último: captura qualquer outra exceção do estoque
            System.out.println("  [EstoqueException capturada] " + e.getMessage());
        }
    }

    private static void imprimirProdutos(Estoque estoque) {
        List<Product> produtos = estoque.getProdutos();
        for (int i = 0; i < produtos.size(); i++) {
            Product p = produtos.get(i);
            System.out.printf("[%d] %-16s %s%n", i, p.getClass().getSimpleName(), p.getDescricao());
            System.out.println("    Valor total: " + moeda(p.calcularValorTotal()));
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("--- " + texto + " ---");
    }

    private static String moeda(double valor) {
        return String.format(LOCALE_BR, "R$ %,.2f", valor);
    }
}

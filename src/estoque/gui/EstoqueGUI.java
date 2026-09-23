package estoque.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import estoque.Estoque;
import estoque.excecoes.ProdutoIndisponivelException;
import estoque.excecoes.QuantidadeInvalidaException;
import estoque.gui.Componentes.Botao;
import estoque.gui.Componentes.CaixaProduto;
import estoque.gui.Componentes.CampoTexto;
import estoque.gui.Componentes.Indicador;
import estoque.gui.Componentes.Seletor;
import estoque.produtos.Product;
import estoque.produtos.ProdutoComum;
import estoque.produtos.ProdutoPerecivel;

/**
 * Interface gráfica do Sistema de Estoque de Produtos.
 *
 * <p>Usa exatamente as mesmas classes de domínio da {@code EstoqueApp}
 * ({@link Estoque}, {@link Product} e subclasses, exceções) — a interface
 * apenas apresenta os dados e mostra as exceções capturadas no registro
 * de operações.</p>
 */
public final class EstoqueGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] MESES = {
        "JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"
    };

    private final transient Estoque estoque = new Estoque();
    private final TabelaProdutos tabela = new TabelaProdutos(estoque);

    private final Indicador indValor = new Indicador("Valor total do estoque", false);
    private final Indicador indUnidades = new Indicador("Unidades em estoque", true);
    private final Indicador indProdutos = new Indicador("Produtos cadastrados", true);
    private final Indicador indAlerta = new Indicador("Perto do vencimento", true);

    private final JTextPane registro = new JTextPane();

    // Formulário: cadastro
    private final Seletor seletorTipo = new Seletor(10.5f, "Comum", "Perecível");
    private final CampoTexto campoNome = new CampoTexto("Ex.: Arroz 5kg");
    private final CampoTexto campoPreco = new CampoTexto("0,00");
    private final CampoTexto campoQuantidade = new CampoTexto("0");
    private final CampoTexto campoDias = new CampoTexto("0");

    // Formulário: venda
    private final CaixaProduto caixaVenda = new CaixaProduto();
    private final CampoTexto campoQtdVenda = new CampoTexto("0");

    // Formulário: desconto
    private final CaixaProduto caixaDesconto = new CaixaProduto();
    private final CampoTexto campoPercentual = new CampoTexto("0");
    private final CampoTexto campoDescontoMax = new CampoTexto("opcional");

    private final Seletor seletorOperacao = new Seletor(11f, "Cadastrar", "Vender", "Desconto");

    public EstoqueGUI() {
        super("Estoque · Sistema de Estoque de Produtos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImages(icones());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FUNDO);
        raiz.add(new Cabecalho(), BorderLayout.NORTH);
        raiz.add(criarAreaPrincipal(), BorderLayout.CENTER);
        raiz.add(criarPainelOperacoes(), BorderLayout.EAST);
        setContentPane(raiz);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarSelecao();
            }
        });
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() >= 0) {
                    seletorOperacao.setSelecionado(1);
                    campoQtdVenda.requestFocusInWindow();
                }
            }
        });

        carregarDemonstracao();
        atualizarTudo();

        setMinimumSize(new Dimension(1180, 740));
        setSize(1360, 840);
        setLocationRelativeTo(null);
    }

    // ======================================================================
    // Montagem da interface
    // ======================================================================

    private JComponent criarAreaPrincipal() {
        JPanel area = new JPanel(new BorderLayout(0, 22));
        area.setBackground(Tema.FUNDO);
        area.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));

        JPanel indicadores = new JPanel(new GridLayout(1, 4, 0, 0));
        indicadores.setOpaque(false);
        indicadores.add(indValor);
        indicadores.add(indUnidades);
        indicadores.add(indProdutos);
        indicadores.add(indAlerta);
        area.add(indicadores, BorderLayout.NORTH);

        JPanel inventario = new JPanel(new BorderLayout(0, 10));
        inventario.setOpaque(false);
        inventario.add(tituloSecao("Inventário", "Clique para selecionar  ·  duplo clique para vender"),
            BorderLayout.NORTH);
        JPanel moldura = new JPanel(new BorderLayout());
        moldura.setBorder(BorderFactory.createLineBorder(Tema.LINHA));
        moldura.add(Componentes.rolagem(tabela, Tema.PAINEL), BorderLayout.CENTER);
        inventario.add(moldura, BorderLayout.CENTER);

        JPanel log = new JPanel(new BorderLayout(0, 10));
        log.setOpaque(false);
        log.add(tituloSecao("Registro de operações", "Exceções capturadas aparecem aqui"), BorderLayout.NORTH);
        registro.setEditable(false);
        registro.setBackground(Tema.FUNDO);
        registro.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        registro.setCaretColor(Tema.FUNDO);
        registro.setSelectionColor(Tema.AMBAR_FUNDO);
        JPanel molduraLog = new JPanel(new BorderLayout());
        molduraLog.setBorder(BorderFactory.createLineBorder(Tema.LINHA));
        molduraLog.add(Componentes.rolagem(registro, Tema.FUNDO), BorderLayout.CENTER);
        molduraLog.setPreferredSize(new Dimension(10, 170));
        log.add(molduraLog, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 22));
        centro.setOpaque(false);
        centro.add(inventario, BorderLayout.CENTER);
        centro.add(log, BorderLayout.SOUTH);
        area.add(centro, BorderLayout.CENTER);
        return area;
    }

    private JComponent criarPainelOperacoes() {
        JPanel painel = new JPanel(new BorderLayout(0, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Tema.LINHA);
                g.fillRect(0, 0, 1, getHeight());
            }
        };
        painel.setBackground(Tema.PAINEL);
        painel.setPreferredSize(new Dimension(372, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(22, 28, 24, 28));

        JPanel topo = new JPanel(new BorderLayout(0, 12));
        topo.setOpaque(false);
        topo.add(Componentes.rotulo("Operações"), BorderLayout.NORTH);
        topo.add(seletorOperacao, BorderLayout.CENTER);
        painel.add(topo, BorderLayout.NORTH);

        CardLayout cartoes = new CardLayout();
        JPanel formularios = new JPanel(cartoes);
        formularios.setOpaque(false);
        formularios.setBorder(BorderFactory.createEmptyBorder(26, 0, 0, 0));
        formularios.add(criarFormularioCadastro(), "0");
        formularios.add(criarFormularioVenda(), "1");
        formularios.add(criarFormularioDesconto(), "2");
        seletorOperacao.aoSelecionar(i -> cartoes.show(formularios, String.valueOf(i)));
        painel.add(formularios, BorderLayout.CENTER);
        return painel;
    }

    private JComponent criarFormularioCadastro() {
        Formulario f = new Formulario();
        f.adicionar(Componentes.rotulo("Tipo de produto"), 0);
        f.adicionar(seletorTipo, 18);
        f.campo("Nome", campoNome);
        f.campo("Preço unitário (R$)", campoPreco);
        f.lado(par("Quantidade", campoQuantidade), par("Dias para vencer", campoDias));

        Botao botao = new Botao("Cadastrar produto", true);
        botao.addActionListener(e -> cadastrar());
        f.adicionar(botao, 16);
        f.adicionar(Componentes.nota(
            "Preço ou quantidade negativos lançam QuantidadeInvalidaException. "
                + "Perecíveis com até " + ProdutoPerecivel.DIAS_LIMITE_DESCONTO
                + " dias para vencer recebem 20% de desconto no valor total.", false), 0);
        f.fim();

        seletorTipo.aoSelecionar(i -> atualizarCampoDias());
        atualizarCampoDias();
        campoNome.addActionListener(e -> campoPreco.requestFocusInWindow());
        campoPreco.addActionListener(e -> campoQuantidade.requestFocusInWindow());
        campoQuantidade.addActionListener(e -> {
            if (campoDias.isEnabled()) {
                campoDias.requestFocusInWindow();
            } else {
                cadastrar();
            }
        });
        campoDias.addActionListener(e -> cadastrar());
        return f;
    }

    private JComponent criarFormularioVenda() {
        Formulario f = new Formulario();
        f.adicionar(caixaVenda, 22);
        f.campo("Quantidade a vender", campoQtdVenda);
        Botao botao = new Botao("Registrar venda", true);
        botao.addActionListener(e -> vender());
        campoQtdVenda.addActionListener(e -> vender());
        f.adicionar(botao, 16);
        f.adicionar(Componentes.nota(
            "Estoque.venderProduto() delega a venda para Product.vender(). "
                + "Pedir mais do que o disponível lança ProdutoIndisponivelException.", false), 0);
        f.fim();
        return f;
    }

    private JComponent criarFormularioDesconto() {
        Formulario f = new Formulario();
        f.adicionar(caixaDesconto, 22);
        f.lado(par("Percentual (%)", campoPercentual), par("Desconto máx. (R$)", campoDescontoMax));
        Botao botao = new Botao("Aplicar desconto", true);
        botao.addActionListener(e -> aplicarDesconto());
        campoPercentual.addActionListener(e -> aplicarDesconto());
        campoDescontoMax.addActionListener(e -> aplicarDesconto());
        f.adicionar(botao, 16);
        f.adicionar(Componentes.nota(
            "Sobrecarga de métodos:\n"
                + "sem limite  -> aplicarDesconto(percentual)\n"
                + "com limite  -> aplicarDesconto(percentual, descontoMaximo)", true), 0);
        f.fim();
        return f;
    }

    private JComponent tituloSecao(String titulo, String dica) {
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);
        linha.add(Componentes.rotulo(titulo), BorderLayout.WEST);
        javax.swing.JLabel rotuloDica = new javax.swing.JLabel(dica);
        rotuloDica.setFont(Tema.sans(Font.PLAIN, 12f));
        rotuloDica.setForeground(Tema.TEXTO_3);
        linha.add(rotuloDica, BorderLayout.EAST);
        return linha;
    }

    private static JComponent par(String rotulo, JComponent campo) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(Componentes.rotulo(rotulo), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    /** Painel de formulário vertical simples (um componente por linha). */
    private static final class Formulario extends JPanel {

        private static final long serialVersionUID = 1L;
        private int linha;

        Formulario() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        void adicionar(JComponent c, int espacoDepois) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = linha++;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(0, 0, espacoDepois, 0);
            add(c, g);
        }

        void campo(String rotulo, JComponent campo) {
            adicionar(par(rotulo, campo), 18);
        }

        void lado(JComponent esquerda, JComponent direita) {
            JPanel p = new JPanel(new GridLayout(1, 2, 20, 0));
            p.setOpaque(false);
            p.add(esquerda);
            p.add(direita);
            adicionar(p, 18);
        }

        /** Empurra o conteúdo para o topo. */
        void fim() {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = linha++;
            g.weighty = 1;
            add(javax.swing.Box.createGlue(), g);
        }
    }

    // ======================================================================
    // Operações
    // ======================================================================

    private void cadastrar() {
        boolean perecivel = seletorTipo.getSelecionado() == 1;
        try {
            String nome = campoNome.getText().trim();
            double preco = lerDecimal(campoPreco, "Preço");
            int quantidade = lerInteiro(campoQuantidade, "Quantidade");
            Product produto;
            if (perecivel) {
                int dias = lerInteiro(campoDias, "Dias para vencer");
                produto = new ProdutoPerecivel(nome, preco, quantidade, dias);
            } else {
                produto = new ProdutoComum(nome, preco, quantidade);
            }
            estoque.adicionarProduto(produto);
            atualizarTudo();
            tabela.selecionar(estoque.getQuantidadeDeProdutos() - 1);
            limpar(campoNome, campoPreco, campoQuantidade, campoDias);
            campoNome.requestFocusInWindow();
            registrarOk(String.format("Cadastrado: \"%s\" (%s), %s un. a %s. Valor total %s.",
                produto.getNome(), perecivel ? "perecível" : "comum", Tema.numero(produto.getQuantidade()),
                Tema.moeda(produto.getPreco()), Tema.moeda(produto.calcularValorTotal())));
        } catch (EntradaInvalida e) {
            registrarErro("Entrada inválida", e.getMessage());
        } catch (QuantidadeInvalidaException e) {
            registrarErro(e.getClass().getSimpleName(), e.getMessage());
        } catch (IllegalArgumentException e) {
            registrarErro("Validação", e.getMessage());
        }
    }

    private void vender() {
        int indice = tabela.getSelectedRow();
        if (indice < 0) {
            registrarErro("Nenhum produto selecionado", "Selecione um produto no inventário antes de vender.");
            return;
        }
        try {
            int quantidade = lerInteiro(campoQtdVenda, "Quantidade a vender");
            estoque.venderProduto(indice, quantidade);
            Product p = estoque.getProdutos().get(indice);
            atualizarTudo();
            limpar(campoQtdVenda);
            registrarOk(String.format("Venda: %s un. de \"%s\". Restam %s un. em estoque.",
                Tema.numero(quantidade), p.getNome(), Tema.numero(p.getQuantidade())));
        } catch (EntradaInvalida e) {
            registrarErro("Entrada inválida", e.getMessage());
        } catch (ProdutoIndisponivelException e) {
            registrarErro(e.getClass().getSimpleName(), e.getMessage());
        } catch (IllegalArgumentException e) {
            registrarErro("Validação", e.getMessage());
        }
    }

    private void aplicarDesconto() {
        Product p = tabela.produtoSelecionado();
        if (p == null) {
            registrarErro("Nenhum produto selecionado", "Selecione um produto no inventário antes de aplicar desconto.");
            return;
        }
        try {
            double percentual = lerDecimal(campoPercentual, "Percentual");
            double antes = p.getPreco();
            String chamada;
            if (campoDescontoMax.getText().trim().isEmpty()) {
                p.aplicarDesconto(percentual);
                chamada = String.format(Tema.LOCALE_BR, "aplicarDesconto(%.2f)", percentual);
            } else {
                double maximo = lerDecimal(campoDescontoMax, "Desconto máximo");
                p.aplicarDesconto(percentual, maximo);
                chamada = String.format(Tema.LOCALE_BR, "aplicarDesconto(%.2f, %.2f)", percentual, maximo);
            }
            atualizarTudo();
            limpar(campoPercentual, campoDescontoMax);
            registrarOk(String.format("%s em \"%s\": %s -> %s por unidade.",
                chamada, p.getNome(), Tema.moeda(antes), Tema.moeda(p.getPreco())));
        } catch (EntradaInvalida e) {
            registrarErro("Entrada inválida", e.getMessage());
        } catch (IllegalArgumentException e) {
            registrarErro("Validação", e.getMessage());
        }
    }

    // ======================================================================
    // Leitura de campos
    // ======================================================================

    /** Erro de digitação em um campo (texto que não é número). */
    private static final class EntradaInvalida extends Exception {

        private static final long serialVersionUID = 1L;

        EntradaInvalida(String mensagem) {
            super(mensagem);
        }
    }

    /** Aceita "5,79", "5.79", "1.234,56" e "R$ 5,79". */
    private static double lerDecimal(CampoTexto campo, String nome) throws EntradaInvalida {
        String original = campo.getText().trim();
        if (original.isEmpty()) {
            throw new EntradaInvalida("O campo \"" + nome + "\" é obrigatório.");
        }
        String texto = original.replace("R$", "").replace("%", "").replace(" ", "");
        if (texto.contains(",")) {
            texto = texto.replace(".", "").replace(',', '.');
        }
        try {
            double valor = Double.parseDouble(texto);
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new EntradaInvalida(nome + ": \"" + original + "\" não é um número válido.");
        }
    }

    private static int lerInteiro(CampoTexto campo, String nome) throws EntradaInvalida {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            throw new EntradaInvalida("O campo \"" + nome + "\" é obrigatório.");
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new EntradaInvalida(nome + ": \"" + texto + "\" não é um número inteiro válido.");
        }
    }

    private static void limpar(CampoTexto... campos) {
        for (CampoTexto c : campos) {
            c.setText("");
        }
    }

    // ======================================================================
    // Atualização da tela
    // ======================================================================

    private void atualizarCampoDias() {
        boolean perecivel = seletorTipo.getSelecionado() == 1;
        campoDias.setEnabled(perecivel);
        if (!perecivel) {
            campoDias.setText("");
        }
        campoDias.repaint();
    }

    private void atualizarSelecao() {
        int indice = tabela.getSelectedRow();
        Product p = tabela.produtoSelecionado();
        caixaVenda.setProduto(p, indice);
        caixaDesconto.setProduto(p, indice);
    }

    private void atualizarTudo() {
        tabela.atualizar();
        atualizarSelecao();

        long unidades = 0;
        int comuns = 0;
        int pereciveis = 0;
        int emAlerta = 0;
        for (Product p : estoque.getProdutos()) {
            unidades += p.getQuantidade();
            if (p instanceof ProdutoPerecivel) {
                pereciveis++;
                if (((ProdutoPerecivel) p).temDescontoPorVencimento()) {
                    emAlerta++;
                }
            } else {
                comuns++;
            }
        }
        indValor.atualizar(Tema.moeda(estoque.calcularValorTotalEstoque()),
            "Soma de calcularValorTotal()", Tema.TEXTO);
        indUnidades.atualizar(Tema.numero(unidades),
            "Em " + estoque.getQuantidadeDeProdutos() + " produtos", Tema.TEXTO);
        indProdutos.atualizar(String.valueOf(estoque.getQuantidadeDeProdutos()),
            comuns + " comuns  ·  " + pereciveis + " perecíveis", Tema.TEXTO);
        indAlerta.atualizar(String.valueOf(emAlerta),
            "Até " + ProdutoPerecivel.DIAS_LIMITE_DESCONTO + " dias  ·  20% de desconto",
            emAlerta > 0 ? Tema.AMBAR : Tema.TEXTO);
    }

    // ======================================================================
    // Registro de operações
    // ======================================================================

    private void registrarOk(String mensagem) {
        registrar("OK", Tema.SALVIA, null, mensagem);
    }

    private void registrarErro(String origem, String mensagem) {
        registrar("ERRO", Tema.OXIDO, origem, mensagem);
    }

    private void registrarInfo(String mensagem) {
        registrar("INFO", Tema.TEXTO_3, null, mensagem);
    }

    private void registrar(String nivel, Color corNivel, String origem, String mensagem) {
        StyledDocument doc = registro.getStyledDocument();
        try {
            if (doc.getLength() > 0) {
                doc.insertString(doc.getLength(), "\n", estilo(Tema.TEXTO_3, false));
            }
            doc.insertString(doc.getLength(), LocalTime.now().format(HORA) + "   ", estilo(Tema.TEXTO_3, false));
            doc.insertString(doc.getLength(), String.format("%-6s", nivel), estilo(corNivel, true));
            if (origem != null) {
                doc.insertString(doc.getLength(), origem + "   ", estilo(Tema.OXIDO, false));
            }
            doc.insertString(doc.getLength(), mensagem, estilo(Tema.TEXTO_2, false));

            // Recuo deslocado: linhas quebradas continuam alinhadas após a hora e o nível
            SimpleAttributeSet paragrafo = new SimpleAttributeSet();
            float recuo = registro.getFontMetrics(Tema.mono(Font.PLAIN, 13f)).stringWidth("00:00:00   ERRO  ");
            StyleConstants.setLeftIndent(paragrafo, recuo);
            StyleConstants.setFirstLineIndent(paragrafo, -recuo);
            StyleConstants.setLineSpacing(paragrafo, 0.25f);
            doc.setParagraphAttributes(0, doc.getLength(), paragrafo, false);
            registro.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static SimpleAttributeSet estilo(Color cor, boolean negrito) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        Font fonte = Tema.mono(Font.PLAIN, 12.5f);
        StyleConstants.setFontFamily(s, fonte.getFamily());
        StyleConstants.setFontSize(s, 13);
        StyleConstants.setForeground(s, cor);
        StyleConstants.setBold(s, negrito);
        return s;
    }

    // ======================================================================
    // Dados iniciais
    // ======================================================================

    private void carregarDemonstracao() {
        try {
            estoque.adicionarProduto(new ProdutoComum("Arroz 5kg", 27.90, 40));
            estoque.adicionarProduto(new ProdutoComum("Feijão Carioca 1kg", 8.49, 60));
            estoque.adicionarProduto(new ProdutoPerecivel("Leite Integral 1L", 5.79, 50, 2));
            estoque.adicionarProduto(new ProdutoPerecivel("Queijo Mussarela 500g", 24.90, 20, 15));
            estoque.adicionarProduto(new ProdutoComum("Café Torrado 500g", 18.75, 30));
            estoque.adicionarProduto(new ProdutoPerecivel("Iogurte Natural 170g", 3.49, 48, 3));
            registrarInfo("Estoque carregado com " + estoque.getQuantidadeDeProdutos() + " produtos de demonstração.");
        } catch (QuantidadeInvalidaException e) {
            registrarErro(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ======================================================================
    // Cabeçalho e ícone
    // ======================================================================

    /** Faixa superior com a marca e a data. */
    private static final class Cabecalho extends JComponent {

        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(10, 60);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Tema.FUNDO);
                g2.fillRect(0, 0, w, h);
                g2.setColor(Tema.LINHA);
                g2.fillRect(0, h - 1, w, 1);

                desenharMarca(g2, 28, (h - 16) / 2, 16);

                g2.setFont(Tema.rotulo(15f).deriveFont(Font.BOLD));
                g2.setColor(Tema.TEXTO);
                FontMetrics fm = g2.getFontMetrics();
                int base = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("ESTOQUE", 56, base);
                int x = 56 + fm.stringWidth("ESTOQUE") + 18;

                g2.setColor(Tema.LINHA_FORTE);
                g2.fillRect(x, h / 2 - 9, 1, 18);

                g2.setFont(Tema.sans(Font.PLAIN, 13.5f));
                g2.setColor(Tema.TEXTO_2);
                fm = g2.getFontMetrics();
                g2.drawString("Sistema de Estoque de Produtos", x + 18, (h - fm.getHeight()) / 2 + fm.getAscent());

                LocalDate hoje = LocalDate.now();
                String data = String.format("%02d %s %d", hoje.getDayOfMonth(),
                    MESES[hoje.getMonthValue() - 1], hoje.getYear());
                g2.setFont(Tema.rotulo(10.5f));
                g2.setColor(Tema.TEXTO_3);
                fm = g2.getFontMetrics();
                g2.drawString(data, w - 28 - fm.stringWidth(data), (h - fm.getHeight()) / 2 + fm.getAscent());
            } finally {
                g2.dispose();
            }
        }
    }

    /** Marca: três prateleiras em âmbar, a de baixo mais longa. */
    private static void desenharMarca(Graphics2D g2, int x, int y, int tamanho) {
        int barra = Math.max(2, tamanho / 6);
        int passo = (tamanho - barra) / 2;
        g2.setColor(Tema.AMBAR);
        g2.fillRect(x, y, tamanho * 5 / 8, barra);
        g2.fillRect(x, y + passo, tamanho * 13 / 16, barra);
        g2.fillRect(x, y + 2 * passo, tamanho, barra);
    }

    private static List<Image> icones() {
        List<Image> lista = new ArrayList<>();
        for (int tamanho : new int[] {16, 32, 48, 64}) {
            BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(Tema.FUNDO);
            g2.fillRect(0, 0, tamanho, tamanho);
            int margem = tamanho / 5;
            desenharMarca(g2, margem, margem + tamanho / 16, tamanho - 2 * margem);
            g2.dispose();
            lista.add(img);
        }
        return lista;
    }

    // ======================================================================

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // Mantém o look and feel padrão; todos os componentes são desenhados sob medida.
            }
            new EstoqueGUI().setVisible(true);
        });
    }
}

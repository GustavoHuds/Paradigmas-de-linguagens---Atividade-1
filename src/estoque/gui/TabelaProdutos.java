package estoque.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import estoque.Estoque;
import estoque.produtos.Product;
import estoque.produtos.ProdutoPerecivel;

/**
 * Tabela de inventário: lê os produtos diretamente do {@link Estoque}.
 * A linha da tabela corresponde ao índice do produto no estoque.
 */
final class TabelaProdutos extends JTable {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUNAS = {
        "#", "Tipo", "Produto", "Preço unit.", "Qtd.", "Validade", "Valor total"
    };
    private static final int[] LARGURAS = {52, 118, 250, 120, 80, 190, 140};
    private static final int COL_INDICE = 0;
    private static final int COL_TIPO = 1;
    private static final int COL_NOME = 2;
    private static final int COL_PRECO = 3;
    private static final int COL_QTD = 4;
    private static final int COL_VALIDADE = 5;
    private static final int COL_TOTAL = 6;

    /** Escala da barra de validade: 30 dias ou mais preenchem a barra inteira. */
    private static final int DIAS_ESCALA = 30;
    private static final int PADDING = 14;

    private final Modelo modelo;
    private int linhaSobre = -1;

    TabelaProdutos(Estoque estoque) {
        super(new Modelo(estoque));
        this.modelo = (Modelo) getModel();

        setRowHeight(48);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setBackground(Tema.PAINEL);
        setForeground(Tema.TEXTO);
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultRenderer(Object.class, new Celula());

        JTableHeader cabecalho = getTableHeader();
        cabecalho.setReorderingAllowed(false);
        cabecalho.setResizingAllowed(false);
        cabecalho.setBackground(Tema.PAINEL);
        cabecalho.setDefaultRenderer(new Cabecalho());
        cabecalho.setPreferredSize(new Dimension(10, 38));

        TableColumnModel colunas = getColumnModel();
        for (int i = 0; i < LARGURAS.length; i++) {
            colunas.getColumn(i).setPreferredWidth(LARGURAS[i]);
        }
        colunas.getColumn(COL_INDICE).setMaxWidth(LARGURAS[COL_INDICE]);

        MouseAdapter hover = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int linha = rowAtPoint(e.getPoint());
                if (linha != linhaSobre) {
                    linhaSobre = linha;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linhaSobre = -1;
                repaint();
            }
        };
        addMouseListener(hover);
        addMouseMotionListener(hover);
    }

    /** Recarrega os dados do estoque mantendo a linha selecionada. */
    void atualizar() {
        int selecionada = getSelectedRow();
        modelo.fireTableDataChanged();
        if (selecionada >= 0 && selecionada < getRowCount()) {
            setRowSelectionInterval(selecionada, selecionada);
        }
    }

    void selecionar(int linha) {
        if (linha >= 0 && linha < getRowCount()) {
            setRowSelectionInterval(linha, linha);
            scrollRectToVisible(getCellRect(linha, 0, true));
        }
    }

    Product produtoSelecionado() {
        int linha = getSelectedRow();
        return linha < 0 ? null : modelo.produto(linha);
    }

    // ----------------------------------------------------------------------

    private static final class Modelo extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        private final transient Estoque estoque;

        Modelo(Estoque estoque) {
            this.estoque = estoque;
        }

        Product produto(int linha) {
            return estoque.getProdutos().get(linha);
        }

        @Override
        public int getRowCount() {
            return estoque.getQuantidadeDeProdutos();
        }

        @Override
        public int getColumnCount() {
            return COLUNAS.length;
        }

        @Override
        public String getColumnName(int coluna) {
            return COLUNAS[coluna];
        }

        @Override
        public Object getValueAt(int linha, int coluna) {
            return produto(linha);
        }

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    }

    // ----------------------------------------------------------------------

    private static boolean alinhadaDireita(int coluna) {
        return coluna == COL_PRECO || coluna == COL_QTD || coluna == COL_TOTAL;
    }

    private static int baseline(FontMetrics fm, int altura) {
        return (altura - fm.getHeight()) / 2 + fm.getAscent();
    }

    private final class Celula extends JComponent implements TableCellRenderer {

        private static final long serialVersionUID = 1L;
        private transient Product produto;
        private int linha;
        private int coluna;
        private boolean selecionada;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            this.produto = (Product) valor;
            this.linha = linha;
            this.coluna = tabela.convertColumnIndexToModel(coluna);
            this.selecionada = selecionada;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(selecionada ? Tema.PAINEL_ALTO : linha == linhaSobre ? Tema.HOVER : Tema.PAINEL);
                g2.fillRect(0, 0, w, h);
                g2.setColor(Tema.LINHA);
                g2.fillRect(0, h - 1, w, 1);

                switch (coluna) {
                    case COL_INDICE:
                        if (selecionada) {
                            g2.setColor(Tema.AMBAR);
                            g2.fillRect(0, 0, 3, h - 1);
                        }
                        texto(g2, String.format("%02d", linha), Tema.mono(Font.PLAIN, 12.5f),
                            selecionada ? Tema.AMBAR : Tema.TEXTO_3, false);
                        break;
                    case COL_TIPO:
                        pintarTipo(g2, h);
                        break;
                    case COL_NOME:
                        g2.setFont(Tema.sans(Font.PLAIN, 14.5f));
                        texto(g2, Componentes.encurtar(g2, produto.getNome(), w - 2 * PADDING),
                            g2.getFont(), Tema.TEXTO, false);
                        break;
                    case COL_PRECO:
                        texto(g2, Tema.moeda(produto.getPreco()), Tema.mono(Font.PLAIN, 13.5f), Tema.TEXTO, true);
                        break;
                    case COL_QTD:
                        texto(g2, Tema.numero(produto.getQuantidade()), Tema.mono(Font.PLAIN, 13.5f),
                            produto.getQuantidade() == 0 ? Tema.OXIDO : Tema.TEXTO, true);
                        break;
                    case COL_VALIDADE:
                        pintarValidade(g2, h);
                        break;
                    case COL_TOTAL:
                        texto(g2, Tema.moeda(produto.calcularValorTotal()), Tema.mono(Font.BOLD, 13.5f),
                            Tema.TEXTO, true);
                        break;
                    default:
                        break;
                }
            } finally {
                g2.dispose();
            }
        }

        private void texto(Graphics2D g2, String texto, Font fonte, Color cor, boolean direita) {
            g2.setFont(fonte);
            g2.setColor(cor);
            FontMetrics fm = g2.getFontMetrics();
            int x = direita ? getWidth() - PADDING - fm.stringWidth(texto) : PADDING;
            g2.drawString(texto, x, baseline(fm, getHeight()));
        }

        private void pintarTipo(Graphics2D g2, int h) {
            boolean perecivel = produto instanceof ProdutoPerecivel;
            String texto = perecivel ? "PERECÍVEL" : "COMUM";
            g2.setFont(Tema.rotulo(9.5f).deriveFont(Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            int largura = fm.stringWidth(texto) + 14;
            int altura = 20;
            int y = (h - altura) / 2;
            g2.setColor(perecivel ? Tema.TEXTO_3 : Tema.LINHA_FORTE);
            g2.drawRect(PADDING, y, largura, altura);
            g2.setColor(perecivel ? Tema.TEXTO : Tema.TEXTO_2);
            g2.drawString(texto, PADDING + 7, y + (altura - fm.getHeight()) / 2 + fm.getAscent() + 1);
        }

        private void pintarValidade(Graphics2D g2, int h) {
            if (!(produto instanceof ProdutoPerecivel)) {
                texto(g2, "—", Tema.mono(Font.PLAIN, 13f), Tema.TEXTO_3, false);
                return;
            }
            ProdutoPerecivel perecivel = (ProdutoPerecivel) produto;
            int dias = perecivel.getDiasParaVencer();
            boolean alerta = perecivel.temDescontoPorVencimento();

            g2.setFont(Tema.mono(Font.PLAIN, 13f));
            FontMetrics fm = g2.getFontMetrics();
            String texto = String.format("%2d d", dias);
            g2.setColor(alerta ? Tema.AMBAR : Tema.TEXTO);
            g2.drawString(texto, PADDING, baseline(fm, h));

            int xBarra = PADDING + fm.stringWidth("00 d") + 12;
            int larguraBarra = 54;
            int yBarra = h / 2 - 1;
            g2.setColor(Tema.LINHA_FORTE);
            g2.fillRect(xBarra, yBarra, larguraBarra, 3);
            int preenchido = Math.max(2, Math.round(larguraBarra * Math.min(dias, DIAS_ESCALA) / (float) DIAS_ESCALA));
            g2.setColor(alerta ? Tema.AMBAR : Tema.TEXTO_2);
            g2.fillRect(xBarra, yBarra, preenchido, 3);

            if (alerta) {
                String desconto = String.format("−%d%%",
                    Math.round(ProdutoPerecivel.PERCENTUAL_DESCONTO_VENCIMENTO * 100));
                g2.setFont(Tema.mono(Font.BOLD, 11.5f));
                FontMetrics fmd = g2.getFontMetrics();
                int xTag = xBarra + larguraBarra + 12;
                int larguraTag = fmd.stringWidth(desconto) + 10;
                g2.setColor(Tema.AMBAR_FUNDO);
                g2.fillRect(xTag, (h - 20) / 2, larguraTag, 20);
                g2.setColor(Tema.AMBAR);
                g2.drawString(desconto, xTag + 5, baseline(fmd, h));
            }
        }
    }

    // ----------------------------------------------------------------------

    private static final class Cabecalho extends JComponent implements TableCellRenderer {

        private static final long serialVersionUID = 1L;
        private String texto = "";
        private int coluna;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            this.texto = String.valueOf(valor).toUpperCase(Tema.LOCALE_BR);
            this.coluna = tabela.convertColumnIndexToModel(coluna);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Tema.PAINEL);
                g2.fillRect(0, 0, w, h);
                g2.setColor(Tema.LINHA_FORTE);
                g2.fillRect(0, h - 1, w, 1);
                g2.setFont(Tema.rotulo(10f));
                g2.setColor(Tema.TEXTO_3);
                FontMetrics fm = g2.getFontMetrics();
                int x = alinhadaDireita(coluna) ? w - PADDING - fm.stringWidth(texto) : PADDING;
                g2.drawString(texto, x, baseline(fm, h));
            } finally {
                g2.dispose();
            }
        }
    }
}

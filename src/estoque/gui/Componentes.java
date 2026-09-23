package estoque.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;

import estoque.produtos.Product;
import estoque.produtos.ProdutoPerecivel;

/**
 * Componentes visuais desenhados sob medida para a interface.
 */
final class Componentes {

    private Componentes() {
    }

    /** Rótulo pequeno em caixa alta, usado acima de campos e seções. */
    static JLabel rotulo(String texto) {
        String maiusculas = texto.toUpperCase(Tema.LOCALE_BR);
        float tamanho = 10.5f;
        JLabel label = new JLabel(maiusculas);
        label.setFont(Tema.rotulo(tamanho));
        label.setForeground(Tema.TEXTO_3);
        // O JLabel não considera o espaçamento entre letras ao medir o texto:
        // a margem à direita evita que o rótulo seja cortado.
        int extra = Math.round(maiusculas.length() * tamanho * Tema.TRACKING) + 2;
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, extra));
        return label;
    }

    /** Texto explicativo com quebra de linha automática. */
    static JTextArea nota(String texto, boolean mono) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setBorder(null);
        area.setFont(mono ? Tema.mono(Font.PLAIN, 11.5f) : Tema.sans(Font.PLAIN, 12f));
        area.setForeground(Tema.TEXTO_3);
        return area;
    }

    /** Envolve um componente em um JScrollPane com barras de rolagem discretas. */
    static JScrollPane rolagem(Component conteudo, Color fundo) {
        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(fundo);
        scroll.setBackground(fundo);
        scroll.getVerticalScrollBar().setUI(new BarraRolagemUI(fundo));
        scroll.getHorizontalScrollBar().setUI(new BarraRolagemUI(fundo));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ----------------------------------------------------------------------

    /** Campo de texto sem caixa: apenas uma linha de base que acende em âmbar no foco. */
    static class CampoTexto extends JTextField {

        private static final long serialVersionUID = 1L;
        private final String dica;

        CampoTexto(String dica) {
            this.dica = dica;
            setOpaque(false);
            setFont(Tema.sans(Font.PLAIN, 15f));
            setForeground(Tema.TEXTO);
            setCaretColor(Tema.AMBAR);
            setSelectionColor(Tema.AMBAR_FUNDO);
            setSelectedTextColor(Tema.TEXTO);
            setDisabledTextColor(Tema.TEXTO_3);
            setBorder(BorderFactory.createEmptyBorder(7, 0, 9, 0));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    selectAll();
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                if (dica != null && getText().isEmpty() && !isFocusOwner()) {
                    Insets in = getInsets();
                    FontMetrics fm = g2.getFontMetrics(getFont());
                    int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();
                    g2.setFont(getFont());
                    g2.setColor(isEnabled() ? Tema.TEXTO_3 : Tema.LINHA_FORTE);
                    g2.drawString(dica, in.left, y);
                }
                boolean foco = isFocusOwner() && isEnabled();
                g2.setColor(!isEnabled() ? Tema.LINHA : foco ? Tema.AMBAR : Tema.LINHA_FORTE);
                int espessura = foco ? 2 : 1;
                g2.fillRect(0, getHeight() - espessura, getWidth(), espessura);
            } finally {
                g2.dispose();
            }
        }
    }

    // ----------------------------------------------------------------------

    /** Botão retangular: primário em âmbar sólido, secundário em contorno. */
    static class Botao extends JButton {

        private static final long serialVersionUID = 1L;
        private final boolean primario;
        private boolean sobre;

        Botao(String texto, boolean primario) {
            super(texto.toUpperCase(Tema.LOCALE_BR));
            this.primario = primario;
            setFont(Tema.rotulo(11.5f).deriveFont(Font.BOLD));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(120, 44));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                boolean pressionado = getModel().isArmed() && getModel().isPressed();
                Color corTexto;
                if (primario) {
                    g2.setColor(pressionado ? Tema.AMBAR.darker() : sobre ? Tema.AMBAR_CLARO : Tema.AMBAR);
                    g2.fillRect(0, 0, w, h);
                    corTexto = Tema.FUNDO;
                } else {
                    g2.setColor(pressionado ? Tema.PAINEL : sobre ? Tema.HOVER : Tema.PAINEL_ALTO);
                    g2.fillRect(0, 0, w, h);
                    g2.setColor(sobre ? Tema.TEXTO_2 : Tema.LINHA_FORTE);
                    g2.drawRect(0, 0, w - 1, h - 1);
                    corTexto = Tema.TEXTO;
                }
                if (isFocusOwner()) {
                    g2.setColor(primario ? Tema.FUNDO : Tema.AMBAR);
                    g2.drawRect(3, 3, w - 7, h - 7);
                }
                g2.setFont(getFont());
                g2.setColor(corTexto);
                FontMetrics fm = g2.getFontMetrics();
                String texto = getText();
                g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            } finally {
                g2.dispose();
            }
        }
    }

    // ----------------------------------------------------------------------

    /** Seletor segmentado: a opção ativa é marcada por um traço âmbar. */
    static class Seletor extends JComponent {

        private static final long serialVersionUID = 1L;
        private final String[] opcoes;
        private final float tamanhoFonte;
        private final transient List<IntConsumer> ouvintes = new ArrayList<>();
        private int selecionado;
        private int sobre = -1;

        Seletor(float tamanhoFonte, String... opcoes) {
            this.opcoes = opcoes;
            this.tamanhoFonte = tamanhoFonte;
            setFocusable(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    setSelecionado(indiceEm(e.getX()));
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = indiceEm(e.getX());
                    if (i != sobre) {
                        sobre = i;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = -1;
                    repaint();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        setSelecionado(Math.min(selecionado + 1, opcoes.length - 1));
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        setSelecionado(Math.max(selecionado - 1, 0));
                    }
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        private int indiceEm(int x) {
            int largura = Math.max(1, getWidth() / opcoes.length);
            return Math.max(0, Math.min(opcoes.length - 1, x / largura));
        }

        void aoSelecionar(IntConsumer ouvinte) {
            ouvintes.add(ouvinte);
        }

        int getSelecionado() {
            return selecionado;
        }

        void setSelecionado(int indice) {
            if (indice == selecionado) {
                return;
            }
            selecionado = indice;
            repaint();
            for (IntConsumer o : ouvintes) {
                o.accept(indice);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(240, 40);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Tema.LINHA);
                g2.fillRect(0, h - 1, w, 1);
                g2.setFont(Tema.rotulo(tamanhoFonte).deriveFont(Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                int largura = w / opcoes.length;
                for (int i = 0; i < opcoes.length; i++) {
                    int x = i * largura;
                    int lw = (i == opcoes.length - 1) ? w - x : largura;
                    String texto = opcoes[i].toUpperCase(Tema.LOCALE_BR);
                    if (i == selecionado) {
                        g2.setColor(Tema.TEXTO);
                    } else if (i == sobre) {
                        g2.setColor(Tema.TEXTO_2);
                    } else {
                        g2.setColor(Tema.TEXTO_3);
                    }
                    g2.drawString(texto, x + (lw - fm.stringWidth(texto)) / 2,
                        (h - 2 - fm.getHeight()) / 2 + fm.getAscent());
                    if (i == selecionado) {
                        g2.setColor(isFocusOwner() ? Tema.AMBAR_CLARO : Tema.AMBAR);
                        g2.fillRect(x, h - 2, lw, 2);
                    }
                }
            } finally {
                g2.dispose();
            }
        }
    }

    // ----------------------------------------------------------------------

    /** Bloco de indicador: rótulo, valor em destaque e uma linha de detalhe. */
    static class Indicador extends JComponent {

        private static final long serialVersionUID = 1L;
        private final String rotulo;
        private final boolean divisoria;
        private String valor = "";
        private String detalhe = "";
        private Color corValor = Tema.TEXTO;

        Indicador(String rotulo, boolean divisoria) {
            this.rotulo = rotulo.toUpperCase(Tema.LOCALE_BR);
            this.divisoria = divisoria;
        }

        void atualizar(String valor, String detalhe, Color corValor) {
            this.valor = valor;
            this.detalhe = detalhe;
            this.corValor = corValor;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 96);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int x = divisoria ? 24 : 0;
                if (divisoria) {
                    g2.setColor(Tema.LINHA);
                    g2.fillRect(0, 6, 1, getHeight() - 12);
                }
                g2.setFont(Tema.rotulo(10.5f));
                g2.setColor(Tema.TEXTO_3);
                g2.drawString(rotulo, x, 18);

                g2.setFont(Tema.sans(Font.PLAIN, 30f));
                g2.setColor(corValor);
                g2.drawString(valor, x - 1, 58);

                g2.setFont(Tema.sans(Font.PLAIN, 12.5f));
                g2.setColor(Tema.TEXTO_2);
                g2.drawString(detalhe, x, 82);
            } finally {
                g2.dispose();
            }
        }
    }

    // ----------------------------------------------------------------------

    /** Caixa que mostra o produto selecionado na tabela. */
    static class CaixaProduto extends JComponent {

        private static final long serialVersionUID = 1L;
        private transient Product produto;
        private int indice = -1;

        void setProduto(Product produto, int indice) {
            this.produto = produto;
            this.indice = indice;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(240, 88);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suavizar(g.create());
            try {
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Tema.PAINEL_ALTO);
                g2.fillRect(0, 0, w, h);
                g2.setColor(produto != null ? Tema.AMBAR : Tema.LINHA_FORTE);
                g2.fillRect(0, 0, 2, h);

                g2.setFont(Tema.rotulo(10f));
                g2.setColor(Tema.TEXTO_3);
                String cabecalho = produto != null
                    ? String.format("PRODUTO SELECIONADO  ·  ÍNDICE %02d", indice)
                    : "PRODUTO SELECIONADO";
                g2.drawString(cabecalho, 18, 24);

                if (produto == null) {
                    g2.setFont(Tema.sans(Font.PLAIN, 14f));
                    g2.setColor(Tema.TEXTO_2);
                    g2.drawString("Nenhum. Selecione uma linha no inventário.", 18, 54);
                    return;
                }
                g2.setFont(Tema.sans(Font.PLAIN, 17f));
                g2.setColor(Tema.TEXTO);
                g2.drawString(encurtar(g2, produto.getNome(), w - 36), 18, 52);

                g2.setFont(Tema.mono(Font.PLAIN, 12f));
                g2.setColor(Tema.TEXTO_2);
                String detalhe = Tema.numero(produto.getQuantidade()) + " un. · "
                    + Tema.moeda(produto.getPreco()) + "/un.";
                if (produto instanceof ProdutoPerecivel) {
                    detalhe += " · vence em " + ((ProdutoPerecivel) produto).getDiasParaVencer() + " d";
                }
                g2.drawString(encurtar(g2, detalhe, w - 36), 18, 72);
            } finally {
                g2.dispose();
            }
        }
    }

    /** Corta o texto com reticências para caber na largura informada. */
    static String encurtar(Graphics2D g2, String texto, int largura) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        String reticencias = "…";
        int fim = texto.length();
        while (fim > 0 && fm.stringWidth(texto.substring(0, fim) + reticencias) > largura) {
            fim--;
        }
        return texto.substring(0, fim) + reticencias;
    }

    // ----------------------------------------------------------------------

    /** Barra de rolagem fina, sem setas. */
    static class BarraRolagemUI extends BasicScrollBarUI {

        private final Color fundo;

        BarraRolagemUI(Color fundo) {
            this.fundo = fundo;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(10, 10);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
            g.setColor(fundo);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            g.setColor(isThumbRollover() ? Tema.TEXTO_3 : Tema.LINHA_FORTE);
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                g.fillRect(r.x + 3, r.y + 2, r.width - 6, r.height - 4);
            } else {
                g.fillRect(r.x + 2, r.y + 3, r.width - 4, r.height - 6);
            }
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return botaoVazio();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return botaoVazio();
        }

        private static JButton botaoVazio() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            return b;
        }
    }
}

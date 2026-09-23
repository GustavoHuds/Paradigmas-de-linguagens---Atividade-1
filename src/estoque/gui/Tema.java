package estoque.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Paleta, tipografia e utilitários de desenho da interface gráfica.
 *
 * <p>Direção visual: terminal de almoxarifado. Grafite quase preto, texto
 * em tom de papel, um único acento âmbar de sinalização e cantos retos.</p>
 */
final class Tema {

    // Superfícies
    static final Color FUNDO       = new Color(0x0E0F10);
    static final Color PAINEL      = new Color(0x141619);
    static final Color PAINEL_ALTO = new Color(0x1A1D21);
    static final Color HOVER       = new Color(0x1E2226);
    static final Color LINHA       = new Color(0x24282D);
    static final Color LINHA_FORTE = new Color(0x363C43);

    // Texto
    static final Color TEXTO   = new Color(0xE4DFD4);
    static final Color TEXTO_2 = new Color(0x9C978C);
    static final Color TEXTO_3 = new Color(0x625F59);

    // Sinalização
    static final Color AMBAR       = new Color(0xD9A441);
    static final Color AMBAR_CLARO = new Color(0xE8B95A);
    static final Color AMBAR_FUNDO = new Color(0x2A2213);
    static final Color OXIDO       = new Color(0xCF5B43);
    static final Color SALVIA      = new Color(0x8AA982);

    static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");

    /** Espaçamento entre letras dos rótulos em caixa alta (fração do tamanho da fonte). */
    static final float TRACKING = 0.14f;

    private static final String FAMILIA_SANS = escolher("SansSerif", "Bahnschrift", "Segoe UI");
    private static final String FAMILIA_MONO = escolher("Monospaced", "Consolas", "Cascadia Mono");

    private Tema() {
    }

    private static String escolher(String padrao, String... preferidas) {
        Set<String> instaladas = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : preferidas) {
            if (instaladas.contains(f)) {
                return f;
            }
        }
        return padrao;
    }

    static Font sans(int estilo, float tamanho) {
        return new Font(FAMILIA_SANS, estilo, 1).deriveFont(tamanho);
    }

    static Font mono(int estilo, float tamanho) {
        return new Font(FAMILIA_MONO, estilo, 1).deriveFont(tamanho);
    }

    /** Fonte para rótulos em caixa alta, com espaçamento entre letras. */
    static Font rotulo(float tamanho) {
        Map<TextAttribute, Object> atributos = new HashMap<>();
        atributos.put(TextAttribute.TRACKING, TRACKING);
        return sans(Font.PLAIN, tamanho).deriveFont(atributos);
    }

    static String moeda(double valor) {
        return String.format(LOCALE_BR, "R$ %,.2f", valor);
    }

    static String numero(long valor) {
        return String.format(LOCALE_BR, "%,d", valor);
    }

    static Graphics2D suavizar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        return g2;
    }
}

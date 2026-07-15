package com.portal.conecta.checklist.modules.checklist.domain.enums;

/**
 * Classificação do checklist por grupo de itens da sala.
 *
 * <p>Independente de {@link ChecklistType} ({@code ARRIVAL}/{@code POST_BREAK}),
 * que representa o momento operacional. Esta categoria segmenta templates e
 * execuções pelo tipo de patrimônio/item verificado na sala, por exemplo:</p>
 * <ul>
 *   <li>{@link #ELETRONICOS} — computador, projetor, controles, TV…</li>
 *   <li>{@link #MOVEIS} — cadeira, mesa, armário…</li>
 *   <li>{@link #ILUMINACAO} — lâmpadas, luminárias…</li>
 *   <li>{@link #CLIMATIZACAO} — ar-condicionado, ventilação…</li>
 *   <li>{@link #INFRAESTRUTURA} — piso, parede, porta, janela…</li>
 *   <li>{@link #HIGIENE} — limpeza e condições de higiene…</li>
 *   <li>{@link #GERAL} — legado ou checklist misto sem recorte específico</li>
 * </ul>
 */
public enum ChecklistCategory {
    ELETRONICOS,
    MOVEIS,
    ILUMINACAO,
    CLIMATIZACAO,
    INFRAESTRUTURA,
    HIGIENE,
    GERAL
}

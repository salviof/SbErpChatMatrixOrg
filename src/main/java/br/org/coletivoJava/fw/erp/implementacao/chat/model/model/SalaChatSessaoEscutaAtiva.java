/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfSalaChatSessaoEescutaAtiva;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;

/**
 *
 * @author salvio
 */
public class SalaChatSessaoEscutaAtiva implements ItfSalaChatSessaoEescutaAtiva {

    private final ItfChatSalaBean sala;
    private final ItfListenerEventoMatrix escuta;

    public SalaChatSessaoEscutaAtiva(final ItfChatSalaBean pSala, final ItfListenerEventoMatrix pEscuta) {
        sala = pSala;
        escuta = pEscuta;
    }

    @Override
    public ItfChatSalaBean getSala() {
        return sala;
    }

    @Override
    public ItfListenerEventoMatrix getEscuta() {
        return escuta;
    }

    @Override
    public String toString() {
        return sala.getApelido();
    }

}

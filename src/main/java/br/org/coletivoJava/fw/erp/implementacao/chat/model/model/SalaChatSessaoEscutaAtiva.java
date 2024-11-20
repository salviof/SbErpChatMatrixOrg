/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;

/**
 *
 * @author salvio
 */
public class SalaChatSessaoEscutaAtiva {

    private final ItfChatSalaBean sala;
    private final RoomEventsCallback escuta;

    public SalaChatSessaoEscutaAtiva(final ItfChatSalaBean pSala, final RoomEventsCallback pEscuta) {
        sala = pSala;
        escuta = pEscuta;
    }

    public ItfChatSalaBean getSala() {
        return sala;
    }

    public RoomEventsCallback getEscuta() {
        return escuta;
    }

}

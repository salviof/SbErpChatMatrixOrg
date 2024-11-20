/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author salvio
 */
public class ListenerDaSalaExemplo implements RoomEventsCallback {

    private final ItfChatSalaBean sala;

    public ListenerDaSalaExemplo(ItfChatSalaBean pSala) {
        sala = pSala;
    }

    @Override
    public void onEventReceived(List<RoomEvent> list) throws IOException {
        for (RoomEvent evento : list) {

        }
    }

}

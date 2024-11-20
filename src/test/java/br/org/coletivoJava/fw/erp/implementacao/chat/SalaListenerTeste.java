/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.EscutaSalaMatrixAbst;
import de.jojii.matrixclientserver.Bot.Events.RoomEvent;
import de.jojii.matrixclientserver.Callbacks.RoomEventsCallback;

/**
 *
 * @author salvio
 */
public class SalaListenerTeste extends EscutaSalaMatrixAbst {

    public SalaListenerTeste(ItfChatSalaBean pSala) {
        super(pSala);
    }

    @Override
    public void provessarEvento(RoomEvent pEvento) {
        super.provessarEvento(pEvento);
    }

    @Override
    public void eventoDigitando(RoomEvent pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

    @Override
    public void eventoMensagem(RoomEvent pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

    @Override
    public void eventoLeitura(RoomEvent pEvento) {
        System.out.println(pEvento.getType());
        System.out.println(pEvento.getRoom_id());
        System.out.println(pEvento.getSender());
        System.out.println(pEvento.getEvent_id());
        System.out.println(pEvento.getContent());
        System.out.println(pEvento.getRaw());
    }

}

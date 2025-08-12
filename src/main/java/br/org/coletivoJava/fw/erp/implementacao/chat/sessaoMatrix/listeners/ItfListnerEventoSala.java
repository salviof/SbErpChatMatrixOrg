/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.sessaoMatrix.listeners;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfListenerEventoMatrix;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfChatSalaBean;

/**
 *
 * @author salvio
 */
public interface ItfListnerEventoSala extends ItfListenerEventoMatrix {

    public ItfChatSalaBean getSala();

}

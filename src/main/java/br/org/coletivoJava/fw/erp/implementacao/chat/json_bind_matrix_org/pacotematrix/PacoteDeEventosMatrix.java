/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.json_bind_matrix_org.pacotematrix;

import br.org.coletivoJava.fw.api.erp.chat.model.ComandoDeAtendimento;
import br.org.coletivoJava.fw.api.erp.chat.model.ItfEventoMatix;
import java.util.List;

/**
 *
 * @author salvio
 */
public class PacoteDeEventosMatrix {

    private final List<ItfEventoMatix> eventos;
    private final List<ComandoDeAtendimento> comandos;

    public PacoteDeEventosMatrix(List<ItfEventoMatix> eventos, List<ComandoDeAtendimento> comandos) {
        this.eventos = eventos;
        this.comandos = comandos;
    }

    public List<ComandoDeAtendimento> getComandos() {
        return comandos;
    }

    public List<ItfEventoMatix> getEventos() {
        return eventos;
    }

}

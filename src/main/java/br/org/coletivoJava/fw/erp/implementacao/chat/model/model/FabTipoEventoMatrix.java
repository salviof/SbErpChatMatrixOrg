/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

/**
 *
 * @author salvio
 */
public enum FabTipoEventoMatrix {

    MENSAGEM,
    DIGITANDO,
    LEITURA;

    public static FabTipoEventoMatrix getTipoEventoByTypeStr(String pTipoEvento) {

        switch (pTipoEvento) {
            case "m.typing":
                return DIGITANDO;

            case "m.room.message":
                return MENSAGEM;

            case "m.receipt":
                return LEITURA;
            default:
                return null;
        }
    }
}

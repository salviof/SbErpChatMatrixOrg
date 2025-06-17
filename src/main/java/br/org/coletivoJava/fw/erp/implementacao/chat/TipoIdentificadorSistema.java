/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat;

import com.super_bits.modulosSB.SBCore.modulos.objetos.InfoCampos.campo.FabTipoAtributoObjeto;

/**
 *
 * @author salvio
 */
public enum TipoIdentificadorSistema {

    ATENDIMENTO,
    LEAD;

    public FabTipoAtributoObjeto getTipoCampoIdentificador() {
        switch (this) {

            case ATENDIMENTO:
                return FabTipoAtributoObjeto.TELEFONE_CELULAR;

            case LEAD:
                return FabTipoAtributoObjeto.EMAIL;

            default:
                throw new AssertionError();
        }
    }

}

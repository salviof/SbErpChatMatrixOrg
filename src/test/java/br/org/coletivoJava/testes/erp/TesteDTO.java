/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.org.coletivoJava.testes.erp;

import br.org.coletivoJava.fw.api.erp.chat.ERPChat;
import br.org.coletivoJava.fw.api.erp.chat.model.ComoChatSalaBean;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.modulos.erp.ErroJsonInterpredador;
import java.util.logging.Level;
import java.util.logging.Logger;
import testesFW.ConfigCoreJunitPadraoDevLib;

/**
 *
 * @author salvio
 */
public class TesteDTO {

    public void teste() {
        SBCore.configurar(new ConfigCoreJunitPadraoDevLib(), SBCore.ESTADO_APP.DESENVOLVIMENTO);
        try {
            ERPChat.MATRIX_ORG.getDTO("", ComoChatSalaBean.class);
        } catch (ErroJsonInterpredador ex) {
            Logger.getLogger(TesteDTO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ItfUsuarioChat;
import com.google.common.collect.Lists;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ItfBeanSimplesSomenteLeitura;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;

/**
 *
 * @author salvio
 */
public interface ItfFabricaSalaChat {

    public String getAliasSala(ItfUsuarioChat pUSuarioLead);

    public String getAliasSala(ItfBeanSimplesSomenteLeitura pBeanVinculado);

    public String getApelidoNomeUnicoSpace();

    public String getDescricao();

    public String getNomeSpaceDysplay();

    public SalaMatrxOrg getSalaMatrix(ItfBeanSimplesSomenteLeitura pBeanVinculado, ItfUsuarioChat pUsuarioDono, List<ItfUsuarioChat> usuariosIntranet, List<ItfUsuarioChat> pUsuariosInternet) throws ErroPreparandoObjeto;

    public default SalaMatrxOrg getSalaMatrix(ItfUsuarioChat pUsuarioDono, ItfUsuarioChat pUsuarioAtendimento, ItfUsuarioChat pUsuarioInternet) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pUsuarioDono, Lists.newArrayList(pUsuarioAtendimento), Lists.newArrayList(pUsuarioInternet));
    }

    public default SalaMatrxOrg getSalaMatrix(ItfUsuarioChat pUsuarioAtendimento, ItfUsuarioChat pUsuarioInternet) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pUsuarioAtendimento, Lists.newArrayList(pUsuarioInternet), Lists.newArrayList(pUsuarioAtendimento));
    }

    public String getSlug();

}

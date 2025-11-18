/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package br.org.coletivoJava.fw.erp.implementacao.chat.model.model;

import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import com.google.common.collect.Lists;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ComoEntidadeSimplesSomenteLeitura;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;

/**
 *
 * @author salvio
 */
public interface ComoFabricaSalaChat {

    public String getAliasSalaParaUsuario(ComoUsuarioChat pUSuarioLead);

    public String getAliasSalaParaEnttidade(ComoEntidadeSimplesSomenteLeitura pBeanVinculado);

    public String getApelidoNomeUnicoSpace();

    public String getDescricao();

    public String getNomeSpaceDysplay();

    public SalaMatrxOrg getSalaMatrix(ComoEntidadeSimplesSomenteLeitura pBeanVinculado, ComoUsuarioChat pUsuarioDono, List<ComoUsuarioChat> pAtendentes, List<ComoUsuarioChat> pContatosWtzp) throws ErroPreparandoObjeto;

    public default SalaMatrxOrg getSalaMatrix(ComoUsuarioChat pUsuarioDono, ComoUsuarioChat pUsuarioAtendimento, ComoUsuarioChat pContatoWtzp) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pUsuarioDono, Lists.newArrayList(pUsuarioAtendimento), Lists.newArrayList(pContatoWtzp));
    }

    public default SalaMatrxOrg getSalaMatrixPadrao(ComoUsuarioChat pAtendimento, ComoUsuarioChat pContatoWtzp) throws ErroPreparandoObjeto {
        return getSalaMatrix(null, pAtendimento, Lists.newArrayList(pAtendimento), Lists.newArrayList(pContatoWtzp));
    }

    public String getSlug();

}

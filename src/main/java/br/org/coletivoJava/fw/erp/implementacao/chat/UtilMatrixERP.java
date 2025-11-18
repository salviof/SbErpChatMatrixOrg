package br.org.coletivoJava.fw.erp.implementacao.chat;

import br.org.coletivoJava.fw.api.erp.chat.model.ComoUsuarioChat;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.FabTipoSalaMatrix;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.SalaMatrxOrg;
import br.org.coletivoJava.fw.erp.implementacao.chat.model.model.UsuarioChatMatrixOrg;
import br.org.coletivoJava.integracoes.matrixChat.config.FabConfigApiMatrixChat;
import br.org.coletivoJava.integracoes.restIntmatrixchat.UtilsbApiMatrixChat;
import com.google.common.collect.Lists;
import com.super_bits.modulosSB.SBCore.ConfigGeral.SBCore;
import com.super_bits.modulosSB.SBCore.ConfigGeral.arquivosConfiguracao.ConfigModulo;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringSlugs;
import com.super_bits.modulosSB.SBCore.UtilGeral.UtilSBCoreStringValidador;
import com.super_bits.modulosSB.SBCore.modulos.objetos.registro.Interfaces.basico.ComoEntidadeSimplesSomenteLeitura;
import java.util.List;
import org.coletivojava.fw.api.tratamentoErros.ErroPreparandoObjeto;

/**
 *
 * @author salvio
 */
public class UtilMatrixERP {

    private static ConfigModulo config = SBCore.getConfigModulo(FabConfigApiMatrixChat.class);

    public static String gerarSenha(ComoUsuarioChat pUsuairo) {

        StringBuilder senhaBuilder = new StringBuilder();
        //  System.out.println(config.getPropriedade(FabConfigApiMatrixChat.SEGREDO).toLowerCase());
        System.out.println(pUsuairo.getEmail());

        String senhaProt = config.getPropriedade(FabConfigApiMatrixChat.SEGREDO).toLowerCase() + pUsuairo.getEmail();

        senhaBuilder.append(senhaProt.hashCode());
        return senhaBuilder.toString();
    }

    public static String gerarSlugUsuario(ComoUsuarioChat pusuario) {
        String nome = UtilSBCoreStringSlugs.gerarSlugCaixaAlta(pusuario.getNome()).toLowerCase();
        String codigo = String.valueOf(pusuario.getEmail().hashCode()).replace("-", "0").substring(0, 3);
        return nome + codigo;
    }

    public static String gerarCodigoByUser(ComoUsuarioChat pUsername) {
        String slugUsuario = gerarSlugUsuario(pUsername);
        return UtilsbApiMatrixChat.gerarCodigoBySlugUser(slugUsuario);
    }

    public static String gerarAliasSalaIDCanonico(String pNomeCurtoAlias) {

        String dominioFederado = SBCore.getConfigModulo(FabConfigApiMatrixChat.class).getPropriedade(FabConfigApiMatrixChat.DOMINIO_FEDERADO);
        String normalizacaoSlug = UtilSBCoreStringSlugs.gerarSlugSimplesForcandoMaiusculoAposEspaco(pNomeCurtoAlias);
        normalizacaoSlug = UtilSBCoreStringSlugs.gerarSlugCaixaAltaByCammelCase(normalizacaoSlug).toLowerCase();
        return "#" + normalizacaoSlug + ":" + dominioFederado;

    }

    public static String gerarAliasSalaIDCanonicoObjetoRelacionado(ComoEntidadeSimplesSomenteLeitura pObjetoRelacionado, String pSlugCanalComunicacao) {
        if (pObjetoRelacionado == null) {
            throw new UnsupportedOperationException("Objeto relacionado para criação de alias canonico da sala não enviado");
        }
        return gerarAliasSalaIDCanonico((pObjetoRelacionado.getClassePontoIdentificador().replace(".", "_")).toLowerCase() + "_" + pSlugCanalComunicacao);
    }

    public static String gerarAliasSalaIDCanonicoUsuarioWhatsapp(ComoUsuarioChat pUsuarioExternoWhatsapp, String pSlugCanalComunicacao) {
        if (UtilSBCoreStringValidador.isNuloOuEmbranco(pUsuarioExternoWhatsapp.getTelefone()) && !UtilSBCoreStringValidador.isNuloOuEmbranco(pUsuarioExternoWhatsapp.getEmail())) {
            throw new UnsupportedOperationException("O Usuário enviado não parece ser do tipo usuário lead, pois um e-mail relacionado foi encontrado");
        }
        return gerarAliasSalaIDCanonico(pUsuarioExternoWhatsapp.getTelefone() + pSlugCanalComunicacao);
    }

    public static String extrairSlugSala(String pCanonical_alias) {
        return pCanonical_alias.substring(1).substring(0, pCanonical_alias.indexOf(":") - 1);
    }

    public static String extrairSlugUsuario(String pCanonical_alias) {
        return pCanonical_alias.substring(1).substring(0, pCanonical_alias.indexOf(":") - 1);
    }

    @Deprecated
    public static UsuarioChatMatrixOrg gerarUsuarioUnicoByEmail(String pNome, String pEmail, String pTelefone) {
        UsuarioChatMatrixOrg usuario = new UsuarioChatMatrixOrg();
        usuario.setNome(pNome);
        usuario.setEmail(pEmail);
        usuario.setTelefone(pTelefone);
        usuario.setCodigoUsuario(gerarCodigoByUser(usuario));
        usuario.setApelido(gerarSlugUsuario(usuario));
        return usuario;
    }

    public static SalaMatrxOrg gerarSala(FabTipoSalaMatrix pTipoSala, ComoUsuarioChat pUsuarioDono, ComoUsuarioChat usuariosIntranet, ComoUsuarioChat pUsuariosInternet) throws ErroPreparandoObjeto {
        return gerarSala(pTipoSala, pUsuarioDono, Lists.newArrayList(usuariosIntranet), Lists.newArrayList(pUsuariosInternet));
    }

    public static SalaMatrxOrg gerarSala(FabTipoSalaMatrix pTipoSala, ComoUsuarioChat pUsuarioDono, List<ComoUsuarioChat> usuariosIntranet, List<ComoUsuarioChat> pUsuariosInternet) throws ErroPreparandoObjeto {

        return pTipoSala.getSalaMatrix(null, pUsuarioDono, usuariosIntranet, pUsuariosInternet);

    }

}

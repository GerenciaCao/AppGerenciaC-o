package com.example.gerenciacao

import androidx.compose.runtime.*
import androidx.compose.material3.Text

// Enumera apenas as novas rotas; a Agenda integrada e chamada pela rota ja existente.
val DestinosRestantes = setOf("novo_compromisso", "detalhes_compromisso", "registrar_doacao", "registrar_despesa",
    "relatorios", "oferta_parceiro", "criar_anuncio", "divulgacao", "criar_postagem",
    "meu_abrigo", "equipe_abrigo", "convidar_membro", "permissoes", "perfil_membro", "editar_membro", "seguranca_privacidade", "preferencias_notificacoes","aparencia_acessibilidade")

// Direciona cada nome de rota para uma tela completa, sem instanciar uma Activity por tela.
@Composable
fun RotasRestantes(destino: String, ctx: ContextoAbrigo) {
    when (destino) {
        "novo_compromisso" -> TelaNovoCompromisso(ctx)
        "detalhes_compromisso" -> TelaDetalhesCompromisso(ctx)
        "registrar_doacao" -> TelaRegistrarDoacao(ctx)
        "registrar_despesa" -> TelaRegistrarDespesa(ctx)
        "relatorios" -> TelaRelatorios(ctx)
        "oferta_parceiro" -> TelaOfertaParceiro(ctx)
        "criar_anuncio" -> TelaCriarAnuncio(ctx)
        "divulgacao" -> TelaDivulgacao(ctx)
        "criar_postagem" -> TelaCriarPostagem(ctx)
        "meu_abrigo" -> TelaMeuAbrigo(ctx)
        "equipe_abrigo" -> TelaEquipeAbrigo(ctx)
        "convidar_membro" -> TelaConvidarMembro(ctx)
        // Abre a consulta do integrante selecionado na equipe.
        "perfil_membro" -> TelaPerfilMembro(ctx)
        // Reutiliza o formulário com os dados do integrante selecionado.
        "editar_membro" -> TelaConvidarMembro(ctx, membroEdicaoId = ctx.dados.membroId)
        "permissoes" -> TelaPermissoes(ctx)
        // Abre a nova página pelo botão Segurança e acesso.
        "seguranca_privacidade" -> TelaSegurancaPrivacidade(ctx)
        // Abre as preferências de notificações pelo Meu perfil.
        "preferencias_notificacoes" -> TelaNotificacoes(ctx)
        // Abre as preferências de aparência e acessibilidade.
        "aparencia_acessibilidade" -> TelaAparenciaAcessibilidade(ctx)
        else -> Text("Tela indisponível.")
    }
}

// Fornece dados controlados aos Previews e permite interacoes locais sem servidor.
@Composable
fun contextoPreviewRestante(): ContextoAbrigo {
    val animais = remember { AnimaisExemplo }
    var dados by remember { mutableStateOf(dadosRestantesExemplo(animais).let {
        it.copy(compromissoId = it.compromissos.firstOrNull()?.id, ofertaId = it.ofertas.firstOrNull()?.id)
    }) }
    return ContextoAbrigo(dados, animais, atualizar = { dados = it }, abrir = {}, voltar = {}, principal = {}, verAnimal = {})
}
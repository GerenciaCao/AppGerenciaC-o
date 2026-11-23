package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mostra as opções de segurança e privacidade do protótipo.
@Composable
fun TelaSegurancaPrivacidade(
    // Recebe os dados e os destinos existentes no aplicativo.
    ctx: ContextoAbrigo
) {
    // Mantém a seleção ilustrativa enquanto a tela estiver no estado salvo.
    var salvarInformacoes by rememberSaveable { mutableStateOf(false) }
    // Mantém a seleção ilustrativa de conta privada.
    var contaPrivada by rememberSaveable { mutableStateOf(false) }
    // Define qual informação aparece ao abrir uma opção ainda não integrada.
    var opcao by rememberSaveable { mutableStateOf<String?>(null) }
    // Guarda eventuais avisos da navegação inferior.
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Retorna à página anterior pelo botão do Android.
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Mantém laranja nesta tela, como no protótipo.
        LocalCorCabecalhoApp provides LaranjaPadraoApp,
        // Mantém a foto e os três pontos sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false),
        // Mantém a navegação para Loja.
        LocalAbrirLoja provides { ctx.principal("loja") },
        // Mantém a navegação para Finanças.
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Mantém o fundo claro padrão do aplicativo.
            containerColor = FundoPadraoApp,
            // Mantém os mesmos destinos da barra de navegação.
            bottomBar = {
                MenuInferiorInicio(
                    // Mostra mensagens produzidas pela navegação.
                    avisar = { aviso = it },
                    // Não seleciona uma aba principal em configurações.
                    selecionado = "",
                    // Abre a tela inicial.
                    aoInicio = { ctx.principal("inicio") },
                    // Abre a lista de animais.
                    aoAnimais = { ctx.principal("animais") },
                    // Abre as ações rápidas.
                    aoAdicionar = { ctx.principal("adicionar") },
                    // Abre a agenda.
                    aoAgenda = { ctx.principal("agenda") }
                )
            }
        ) { espacos ->
            Column(
                // Mantém as margens e permite rolar até Excluir conta.
                modifier = Modifier.fillMaxSize().padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa as seções da página.
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TituloPaginaPadrao(
                    // Identifica a nova página.
                    titulo = "Segurança e privacidade",
                    // Mantém a seta ao lado do título.
                    aoVoltar = ctx.voltar
                )
                Text(
                    // Evita apresentar opções ilustrativas como proteção já ativada.
                    text = "Demonstração: as opções de conta e privacidade ainda não alteram uma conta real.",
                    // Mantém a observação secundária aos títulos.
                    fontSize = 12.sp,
                    // Define uma leitura confortável em mais de uma linha.
                    lineHeight = 17.sp,
                    // Usa a cor dos textos auxiliares.
                    color = MarromRestante
                )
                SecaoSeguranca("Segurança da conta") {
                    OpcaoSeguranca("Alterar senha") { opcao = "Alterar senha" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Autenticação em duas etapas") { opcao = "Autenticação em duas etapas" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Verificação de e-mail") { opcao = "Verificação de e-mail" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Dispositivos conectados") { opcao = "Dispositivos conectados" }
                    DivisoriaSeguranca()
                    InterruptorSeguranca(
                        // Define o texto da preferência ilustrativa.
                        titulo = "Salvar informações da conta",
                        // Mostra a escolha atual do protótipo.
                        marcado = salvarInformacoes,
                        // Atualiza somente a preferência visual desta demonstração.
                        mudar = { salvarInformacoes = it }
                    )
                }
                SecaoSeguranca("Gerenciamento de permissões") {
                    OpcaoSeguranca("Permissões da equipe") {
                        // Preserva o acesso real já existente no protótipo.
                        if (administradorAbrigo(ctx.dados)) {
                            ctx.abrir("permissoes")
                        } else {
                            aviso = "Apenas administradores podem gerenciar as permissões da equipe."
                        }
                    }
                }
                SecaoSeguranca("Privacidade") {
                    InterruptorSeguranca(
                        // Define a preferência apresentada na referência.
                        titulo = "Conta privada",
                        // Mostra a seleção local de demonstração.
                        marcado = contaPrivada,
                        // Não altera a visibilidade real de nenhum cadastro.
                        mudar = { contaPrivada = it }
                    )
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Visibilidade da conta") { opcao = "Visibilidade da conta" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Dados de contato") { opcao = "Dados de contato" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Compartilhamento de informações") { opcao = "Compartilhamento de informações" }
                }
                SecaoSeguranca("Dados e informações") {
                    OpcaoSeguranca("Política de privacidade") { opcao = "Política de privacidade" }
                    DivisoriaSeguranca()
                    OpcaoSeguranca("Termos de uso") { opcao = "Termos de uso" }
                }
                TextButton(
                    // Explica a indisponibilidade sem apagar dados locais.
                    onClick = { opcao = "Excluir conta" }
                ) {
                    Text(
                        // Identifica a ação mostrada no protótipo.
                        text = "Excluir conta",
                        // Destaca a ação de exclusão.
                        color = Color(0xFF9C302D),
                        // Mantém o texto em destaque.
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    val selecionada = opcao
    if (selecionada != null) {
        val explicacao = when (selecionada) {
            "Alterar senha" -> "A alteração de senha será conectada ao serviço de acesso. Nenhuma senha é alterada nesta demonstração."
            "Autenticação em duas etapas" -> "Esta opção permitirá uma segunda confirmação ao entrar. A proteção ainda não está ativada no protótipo."
            "Verificação de e-mail" -> "Esta opção será conectada ao envio e à confirmação de e-mail. Nenhuma mensagem será enviada nesta demonstração."
            "Dispositivos conectados" -> "A lista de sessões dependerá do serviço de acesso. O protótipo ainda não possui uma lista real de dispositivos."
            "Visibilidade da conta" -> "Os controles de visibilidade serão aplicados quando os dados estiverem integrados ao serviço do aplicativo."
            "Dados de contato" -> "Você já pode editar seu nome, e-mail e telefone em Meu perfil."
            "Compartilhamento de informações" -> "As preferências de compartilhamento serão definidas na integração. Nenhuma informação é compartilhada por esta opção."
            "Política de privacidade" -> "A política oficial ainda precisa ser fornecida pela equipe. Esta opção abrirá o documento aprovado."
            "Termos de uso" -> "Os termos oficiais ainda precisam ser fornecidos pela equipe. Esta opção abrirá o documento aprovado."
            "Excluir conta" -> "A exclusão será conectada ao serviço de contas. Nenhuma conta ou registro é apagado nesta demonstração."
            else -> ""
        }
        AlertDialog(
            // Fecha o aviso ao tocar fora dele.
            onDismissRequest = { opcao = null },
            // Identifica a opção selecionada.
            title = { Text(selecionada) },
            // Explica o funcionamento disponível no protótipo.
            text = { Text(explicacao) },
            // Oferece uma ação de fechamento.
            confirmButton = {
                TextButton(
                    // Fecha a janela sem alterar a conta.
                    onClick = { opcao = null }
                ) { Text("Entendi") }
            },
            // Oferece acesso ao perfil para editar os contatos existentes.
            dismissButton = {
                if (selecionada == "Dados de contato") {
                    TextButton(
                        // Abre a página de perfil que já existe.
                        onClick = { opcao = null; ctx.abrir("perfil") }
                    ) { Text("Abrir meu perfil") }
                }
            },
            // Usa o fundo padrão das janelas do aplicativo.
            containerColor = FundoPadraoApp
        )
    }
    // Mostra os avisos da barra e das restrições de administração.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Agrupa opções relacionadas, mantendo o alinhamento da referência.
@Composable
private fun SecaoSeguranca(
    // Define o título do grupo.
    titulo: String,
    // Recebe as opções exibidas no grupo.
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Column(
        // Separa o título do cartão de opções.
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            // Identifica a seção.
            text = titulo,
            // Destaca o título do grupo.
            fontWeight = FontWeight.Bold,
            // Mantém a tipografia dos títulos secundários.
            fontSize = 16.sp,
            // Usa a cor dos textos do aplicativo.
            color = TextoRestante
        )
        Surface(
            // Ocupa a largura da página.
            modifier = Modifier.fillMaxWidth(),
            // Mantém o fundo pêssego claro do protótipo.
            color = Color(0xFFFFF3E2),
            // Arredonda o grupo de opções.
            shape = RoundedCornerShape(18.dp),
            // Mantém um contorno discreto.
            border = BorderStroke(1.dp, Color(0xFFF1E4D2))
        ) {
            Column(
                // Organiza as opções em uma única coluna.
                modifier = Modifier.fillMaxWidth(),
                // Exibe o conteúdo recebido.
                content = conteudo
            )
        }
    }
}

// Mostra uma opção clicável com seta à direita.
@Composable
private fun OpcaoSeguranca(
    // Define o nome da opção.
    titulo: String,
    // Define a ação executada ao tocar.
    abrir: () -> Unit
) {
    Surface(
        // Aciona a opção ao tocar na linha inteira.
        onClick = abrir,
        // Mantém visível a cor do grupo.
        color = Color.Transparent
    ) {
        Row(
            // Mantém uma área de toque confortável e espaço interno.
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            // Centraliza o texto e a seta verticalmente.
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                // Mostra o nome da opção.
                text = titulo,
                // Reserva o espaço restante para o texto.
                modifier = Modifier.weight(1f),
                // Mantém o tamanho das opções da página.
                fontSize = 14.sp,
                // Destaca o texto no fundo claro.
                fontWeight = FontWeight.SemiBold,
                // Usa marrom escuro para contraste.
                color = TextoRestante
            )
            // Separa o texto da seta.
            Spacer(Modifier.width(10.dp))
            Icon(
                // Reutiliza a seta já existente.
                painter = painterResource(R.drawable.inicio_seta),
                // O título da opção já identifica a ação.
                contentDescription = null,
                // Mantém a seta proporcional à linha.
                modifier = Modifier.size(16.dp),
                // Usa o marrom dos controles.
                tint = MarromRestante
            )
        }
    }
}

// Mostra um interruptor de preferência ilustrativa do protótipo.
@Composable
private fun InterruptorSeguranca(
    // Define o nome da preferência.
    titulo: String,
    // Recebe o estado visual atual.
    marcado: Boolean,
    // Atualiza a seleção local.
    mudar: (Boolean) -> Unit
) {
    Row(
        // Mantém a preferência alinhada às outras opções.
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        // Centraliza o texto e o interruptor.
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            // Mostra o nome da preferência.
            text = titulo,
            // Evita que o texto empurre o interruptor para fora.
            modifier = Modifier.weight(1f),
            // Mantém a tipografia das opções.
            fontSize = 14.sp,
            // Destaca a preferência.
            fontWeight = FontWeight.SemiBold,
            // Mantém contraste no fundo claro.
            color = TextoRestante
        )
        // Separa o texto do controle.
        Spacer(Modifier.width(8.dp))
        Switch(
            // Mostra a seleção ilustrativa atual.
            checked = marcado,
            // Atualiza somente a seleção da demonstração.
            onCheckedChange = mudar
        )
    }
}

// Separa as opções sem acrescentar margens diferentes entre os grupos.
@Composable
private fun DivisoriaSeguranca() {
    HorizontalDivider(
        // Define uma linha discreta.
        thickness = 1.dp,
        // Usa uma cor clara entre as opções.
        color = Color.White
    )
}

// Permite conferir a tela no Android Studio.
@Preview(
    // Identifica a prévia no painel.
    name = "Segurança e privacidade",
    // Mostra o fundo da tela.
    showBackground = true,
    // Define a largura da prévia.
    widthDp = 412,
    // Define a altura da prévia.
    heightDp = 917
)
@Composable
private fun PreviewSegurancaPrivacidade() {
    MaterialTheme {
        TelaSegurancaPrivacidade(
            // Usa os dados de demonstração do projeto.
            ctx = contextoPreviewRestante()
        )
    }
}
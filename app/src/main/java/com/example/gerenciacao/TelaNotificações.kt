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

// Organiza as preferências demonstrativas de notificações.
@Composable
fun TelaNotificacoes(
    // Recebe as ações de navegação compartilhadas.
    ctx: ContextoAbrigo
) {
    // Pausa visualmente todos os canais sem apagar as escolhas individuais.
    var pausarTodas by rememberSaveable { mutableStateOf(false) }
    // Pausa somente as notificações push.
    var pausarPush by rememberSaveable { mutableStateOf(false) }
    // Define a preferência ilustrativa de mensagens.
    var mensagens by rememberSaveable { mutableStateOf(true) }
    // Define a preferência ilustrativa de e-mail.
    var email by rememberSaveable { mutableStateOf(true) }
    // Define a preferência ilustrativa de SMS.
    var sms by rememberSaveable { mutableStateOf(false) }
    // Define se o período de descanso está selecionado.
    var descanso by rememberSaveable { mutableStateOf(false) }
    // Guarda o horário inicial do período de descanso.
    var inicioDescanso by rememberSaveable { mutableStateOf("22:00") }
    // Guarda o horário final do período de descanso.
    var fimDescanso by rememberSaveable { mutableStateOf("08:00") }
    // Controla qual janela de preferências está aberta.
    var painel by rememberSaveable { mutableStateOf<String?>(null) }
    // Guarda os avisos da navegação inferior.
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Mantém o retorno pelo botão do Android.
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Mantém o cabeçalho laranja nesta tela.
        LocalCorCabecalhoApp provides LaranjaPadraoApp,
        // Mantém perfil e três pontos, sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false),
        // Mantém o destino da Loja na barra inferior.
        LocalAbrirLoja provides { ctx.principal("loja") },
        // Mantém o destino de Finanças na barra inferior.
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Mantém o fundo claro do aplicativo.
            containerColor = FundoPadraoApp,
            // Preserva a navegação inferior usada nas outras telas.
            bottomBar = {
                MenuInferiorInicio(
                    // Exibe mensagens da navegação.
                    avisar = { aviso = it },
                    // Não seleciona uma aba principal nas preferências.
                    selecionado = "",
                    // Abre o início.
                    aoInicio = { ctx.principal("inicio") },
                    // Abre Animais.
                    aoAnimais = { ctx.principal("animais") },
                    // Abre Ações rápidas.
                    aoAdicionar = { ctx.principal("adicionar") },
                    // Abre Agenda.
                    aoAgenda = { ctx.principal("agenda") }
                )
            }
        ) { espacos ->
            Column(
                // Respeita as barras e permite rolar em telas menores.
                modifier = Modifier.fillMaxSize().padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Mantém espaço entre os grupos.
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TituloPaginaPadrao(
                    // Define o título da página.
                    titulo = "Notificações",
                    // Mantém a seta ao lado do título.
                    aoVoltar = ctx.voltar
                )
                Text(
                    // Diferencia a seleção visual do envio real de notificações.
                    text = "Demonstração: estas preferências ainda não controlam o envio de push, e-mail ou SMS.",
                    // Mantém o aviso secundário ao conteúdo.
                    fontSize = 12.sp,
                    // Mantém uma leitura confortável.
                    lineHeight = 17.sp,
                    // Usa a cor auxiliar do aplicativo.
                    color = MarromRestante
                )
                GrupoNotificacoes {
                    ChaveNotificacao(
                        // Define o controle principal da página.
                        titulo = "Pausar todas as notificações",
                        // Mostra a seleção global atual.
                        marcado = pausarTodas,
                        // Atualiza a pausa global sem apagar as demais preferências.
                        mudar = { pausarTodas = it }
                    )
                }
                TituloNotificacoes("Notificações push")
                GrupoNotificacoes {
                    ChaveNotificacao(
                        // Define a pausa exclusiva do canal push.
                        titulo = "Pausar tudo",
                        // Mostra a preferência de push.
                        marcado = pausarPush,
                        // Atualiza a pausa de push.
                        mudar = { pausarPush = it },
                        // A pausa global tem prioridade sobre este controle.
                        habilitado = !pausarTodas
                    )
                    LinhaNotificacoes()
                    AcessoNotificacao(
                        // Identifica o período de descanso.
                        titulo = "Modo descanso",
                        // Mostra o horário escolhido ou o estado desativado.
                        resumo = if (descanso) inicioDescanso + " – " + fimDescanso else "Desativado",
                        // Desabilita a opção quando push ou todos os canais estão pausados.
                        habilitado = !pausarTodas && !pausarPush,
                        // Abre as preferências do período de descanso.
                        abrir = { painel = "Modo descanso" }
                    )
                    LinhaNotificacoes()
                    AcessoNotificacao(
                        // Identifica as preferências de mensagens.
                        titulo = "Mensagens",
                        // Mostra a seleção atual.
                        resumo = if (mensagens) "Ativadas" else "Desativadas",
                        // Respeita as pausas global e de push.
                        habilitado = !pausarTodas && !pausarPush,
                        // Abre o controle de mensagens.
                        abrir = { painel = "Mensagens" }
                    )
                }
                // Diferencia e-mail e SMS do canal push.
                TituloNotificacoes("Outros canais")
                GrupoNotificacoes {
                    AcessoNotificacao(
                        // Identifica o canal de e-mail.
                        titulo = "Notificações por e-mail",
                        // Mostra a preferência selecionada.
                        resumo = if (email) "Ativadas" else "Desativadas",
                        // Respeita a pausa global.
                        habilitado = !pausarTodas,
                        // Abre a escolha de e-mail.
                        abrir = { painel = "Notificações por e-mail" }
                    )
                    LinhaNotificacoes()
                    AcessoNotificacao(
                        // Identifica o canal de SMS.
                        titulo = "Notificações por SMS",
                        // Mostra a preferência selecionada.
                        resumo = if (sms) "Ativadas" else "Desativadas",
                        // Respeita a pausa global.
                        habilitado = !pausarTodas,
                        // Abre a escolha de SMS.
                        abrir = { painel = "Notificações por SMS" }
                    )
                }
            }
        }
    }

    val selecionado = painel
    if (selecionado != null) {
        // Usa um rascunho para que Cancelar não altere as preferências.
        var rascunho by rememberSaveable(selecionado) {
            mutableStateOf(when (selecionado) {
                "Modo descanso" -> descanso
                "Mensagens" -> mensagens
                "Notificações por e-mail" -> email
                else -> sms
            })
        }
        // Guarda os horários somente enquanto o painel estiver aberto.
        var inicio by rememberSaveable(selecionado) { mutableStateOf(inicioDescanso) }
        var fim by rememberSaveable(selecionado) { mutableStateOf(fimDescanso) }
        // Controla a exibição de erros no horário.
        var tentou by rememberSaveable(selecionado) { mutableStateOf(false) }
        // Aceita horários de 00:00 até 23:59.
        val formato = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")
        // Valida horários diferentes quando o descanso estiver ligado.
        val horariosValidos = formato.matches(inicio) && formato.matches(fim) && inicio != fim
        AlertDialog(
            // Descarta o rascunho ao tocar fora da janela.
            onDismissRequest = { painel = null },
            // Identifica a preferência aberta.
            title = { Text(selecionado) },
            // Mostra os controles da preferência.
            text = {
                Column(
                    // Separa os controles e permite acompanhar o teclado.
                    modifier = Modifier.verticalScroll(rememberScrollState()).imePadding(),
                    // Mantém respiro entre os controles.
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChaveNotificacao(
                        // Identifica a ativação da preferência selecionada.
                        titulo = if (selecionado == "Modo descanso") "Ativar modo descanso" else "Receber notificações",
                        // Mostra o rascunho da seleção.
                        marcado = rascunho,
                        // Altera somente o rascunho.
                        mudar = { rascunho = it }
                    )
                    if (selecionado == "Modo descanso" && rascunho) {
                        OutlinedTextField(
                            // Mostra o horário inicial.
                            value = inicio,
                            // Limita a entrada ao formato de cinco caracteres.
                            onValueChange = { inicio = it.take(5) },
                            // Identifica o formato esperado.
                            label = { Text("Início · HH:MM") },
                            // Mantém o horário em uma linha.
                            singleLine = true,
                            // Indica um horário inválido depois de tentar salvar.
                            isError = tentou && !horariosValidos,
                            // Mantém a largura da janela.
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            // Mostra o horário final.
                            value = fim,
                            // Limita a entrada ao formato de cinco caracteres.
                            onValueChange = { fim = it.take(5) },
                            // Identifica o formato esperado.
                            label = { Text("Fim · HH:MM") },
                            // Mantém o horário em uma linha.
                            singleLine = true,
                            // Indica um horário inválido depois de tentar salvar.
                            isError = tentou && !horariosValidos,
                            // Mantém a largura da janela.
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (tentou && !horariosValidos) {
                            Text("Informe horários diferentes no formato HH:MM.", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Text(
                        // Explica que salvar não ativa um serviço de entrega real.
                        text = "Preferência de demonstração, ainda sem envio ou bloqueio real de notificações.",
                        // Mantém a observação em tamanho secundário.
                        fontSize = 12.sp
                    )
                }
            },
            // Aplica o rascunho somente depois da confirmação.
            confirmButton = {
                TextButton(
                    // Valida os horários e atualiza a opção selecionada.
                    onClick = {
                        tentou = true
                        if (selecionado != "Modo descanso" || !rascunho || horariosValidos) {
                            when (selecionado) {
                                "Modo descanso" -> {
                                    descanso = rascunho
                                    inicioDescanso = inicio
                                    fimDescanso = fim
                                }
                                "Mensagens" -> mensagens = rascunho
                                "Notificações por e-mail" -> email = rascunho
                                else -> sms = rascunho
                            }
                            painel = null
                        }
                    }
                ) { Text("Salvar") }
            },
            // Permite sair sem aplicar o rascunho.
            dismissButton = {
                TextButton(
                    // Fecha a janela sem mudar a preferência.
                    onClick = { painel = null }
                ) { Text("Cancelar") }
            },
            // Usa a mesma cor das janelas do aplicativo.
            containerColor = FundoPadraoApp
        )
    }
    // Exibe avisos da navegação sem substituir a tela.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra o nome de cada seção.
@Composable
private fun TituloNotificacoes(
    // Recebe o texto do título.
    titulo: String
) {
    Text(
        // Identifica o grupo de preferências.
        text = titulo,
        // Usa o tamanho dos títulos secundários.
        fontSize = 16.sp,
        // Destaca a seção.
        fontWeight = FontWeight.Bold,
        // Mantém contraste com o fundo claro.
        color = TextoRestante
    )
}

// Agrupa opções relacionadas em um cartão arredondado.
@Composable
private fun GrupoNotificacoes(
    // Recebe as opções exibidas no cartão.
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Surface(
        // Mantém o cartão na largura da página.
        modifier = Modifier.fillMaxWidth(),
        // Usa o fundo pêssego claro da referência.
        color = Color(0xFFFFF3E2),
        // Arredonda as bordas do grupo.
        shape = RoundedCornerShape(18.dp),
        // Mantém um contorno discreto.
        border = BorderStroke(1.dp, Color(0xFFF1E4D2))
    ) {
        Column(
            // Ocupa a largura do cartão.
            modifier = Modifier.fillMaxWidth(),
            // Exibe as preferências recebidas.
            content = conteudo
        )
    }
}

// Mostra uma preferência com interruptor.
@Composable
private fun ChaveNotificacao(
    // Recebe o título da opção.
    titulo: String,
    // Recebe a seleção atual.
    marcado: Boolean,
    // Atualiza a seleção.
    mudar: (Boolean) -> Unit,
    // Controla se a opção está disponível.
    habilitado: Boolean = true
) {
    Row(
        // Mantém margens internas confortáveis.
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        // Centraliza o texto e o controle verticalmente.
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            // Mostra o nome da preferência.
            text = titulo,
            // Reserva o espaço restante para o texto.
            modifier = Modifier.weight(1f),
            // Mantém o tamanho das opções.
            fontSize = 14.sp,
            // Destaca a opção.
            fontWeight = FontWeight.SemiBold,
            // Suaviza o texto quando a opção está indisponível.
            color = TextoRestante.copy(alpha = if (habilitado) 1f else 0.5f)
        )
        // Separa o texto do interruptor.
        Spacer(Modifier.width(8.dp))
        Switch(
            // Mostra a seleção atual.
            checked = marcado,
            // Atualiza a preferência local.
            onCheckedChange = mudar,
            // Respeita a pausa global.
            enabled = habilitado
        )
    }
}

// Mostra um acesso às preferências detalhadas de um canal.
@Composable
private fun AcessoNotificacao(
    // Define o nome da opção.
    titulo: String,
    // Mostra o resumo da preferência.
    resumo: String,
    // Controla a disponibilidade da linha.
    habilitado: Boolean,
    // Abre a janela correspondente.
    abrir: () -> Unit
) {
    Surface(
        // Abre a preferência ao tocar na linha.
        onClick = abrir,
        // Respeita as pausas dos canais.
        enabled = habilitado,
        // Preserva o fundo do cartão.
        color = Color.Transparent
    ) {
        Row(
            // Mantém área de toque e espaço interno.
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(14.dp),
            // Centraliza o conteúdo da linha.
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                // Reserva o espaço restante para os textos.
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    // Identifica a preferência.
                    text = titulo,
                    // Usa o tamanho das opções.
                    fontSize = 14.sp,
                    // Destaca o título.
                    fontWeight = FontWeight.SemiBold,
                    // Indica visualmente a disponibilidade da opção.
                    color = TextoRestante.copy(alpha = if (habilitado) 1f else 0.5f)
                )
                Text(
                    // Mostra o estado selecionado, que será retomado após a pausa.
                    text = if (habilitado) resumo else "Pausado",
                    // Mantém o resumo secundário ao título.
                    fontSize = 12.sp,
                    // Usa a cor dos textos auxiliares.
                    color = MarromRestante
                )
            }
            Icon(
                // Reutiliza a seta já presente no projeto.
                painter = painterResource(R.drawable.inicio_seta),
                // O título da linha já identifica a ação.
                contentDescription = null,
                // Mantém o ícone proporcional à linha.
                modifier = Modifier.size(16.dp),
                // Usa a cor dos controles do aplicativo.
                tint = MarromRestante
            )
        }
    }
}

// Separa as opções dentro de cada cartão.
@Composable
private fun LinhaNotificacoes() {
    HorizontalDivider(
        // Define uma linha discreta entre opções.
        thickness = 1.dp,
        // Mantém a divisória clara.
        color = Color.White
    )
}

// Permite conferir a página pelo editor.
@Preview(
    // Define o nome da prévia.
    name = "Notificações",
    // Mostra o fundo da tela.
    showBackground = true,
    // Define a largura da prévia.
    widthDp = 412,
    // Define a altura da prévia.
    heightDp = 917
)
@Composable
private fun PreviewTelaNotificacoes() {
    MaterialTheme {
        TelaNotificacoes(
            // Usa o contexto demonstrativo do aplicativo.
            ctx = contextoPreviewRestante()
        )
    }
}
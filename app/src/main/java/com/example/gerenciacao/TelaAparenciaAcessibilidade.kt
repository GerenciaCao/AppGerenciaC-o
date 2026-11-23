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

// Mostra as escolhas de aparência e acessibilidade do protótipo.
@Composable
fun TelaAparenciaAcessibilidade(
    // Recebe os destinos de navegação do aplicativo.
    ctx: ContextoAbrigo
) {
    // Guarda a opção de tema selecionada na demonstração.
    var tema by rememberSaveable { mutableStateOf("Claro") }
    // Guarda o valor demonstrativo do controle de brilho.
    var brilho by rememberSaveable { mutableFloatStateOf(0.6f) }
    // Guarda a preferência ilustrativa de conforto ocular.
    var conforto by rememberSaveable { mutableStateOf(false) }
    // Guarda a preferência ilustrativa de redução de movimentos.
    var reduzir by rememberSaveable { mutableStateOf(false) }
    // Guarda a opção demonstrativa de tamanho da fonte.
    var fonte by rememberSaveable { mutableStateOf("Padrão") }
    // Guarda a opção demonstrativa de visualização das fichas.
    var fichas by rememberSaveable { mutableStateOf("Padrão") }
    // Controla qual janela de preferências está aberta.
    var painel by rememberSaveable { mutableStateOf<String?>(null) }
    // Guarda mensagens produzidas pela navegação.
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Mantém o retorno pelo botão do Android.
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Mantém laranja neste cabeçalho.
        LocalCorCabecalhoApp provides LaranjaPadraoApp,
        // Mantém foto e três pontos, sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false),
        // Mantém o acesso à loja.
        LocalAbrirLoja provides { ctx.principal("loja") },
        // Mantém o acesso às finanças.
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Mantém o fundo padrão do aplicativo.
            containerColor = FundoPadraoApp,
            // Reutiliza a barra inferior compartilhada.
            bottomBar = {
                MenuInferiorInicio(
                    // Mostra avisos da navegação.
                    avisar = { aviso = it },
                    // Não seleciona uma aba principal em configurações.
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
                // Respeita as barras, permite rolagem e mantém as margens padrão.
                modifier = Modifier.fillMaxSize().padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa título, aviso e opções.
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TituloPaginaPadrao(
                    // Identifica a página.
                    titulo = "Aparência e acessibilidade",
                    // Mantém a seta ao lado do título.
                    aoVoltar = ctx.voltar
                )
                Text(
                    // Diferencia o protótipo de configurações já aplicadas ao aplicativo.
                    text = "Prévia do MVP: as escolhas abaixo são demonstrativas e ainda não alteram outras telas nem as configurações do celular.",
                    // Mantém o aviso secundário ao conteúdo.
                    fontSize = 12.sp,
                    // Separa as linhas do aviso.
                    lineHeight = 17.sp,
                    // Usa a cor dos textos auxiliares.
                    color = MarromRestante
                )
                Surface(
                    // Mantém o grupo na largura da página.
                    modifier = Modifier.fillMaxWidth(),
                    // Usa o pêssego claro da referência.
                    color = Color(0xFFFFF3E2),
                    // Arredonda a área de opções.
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column {
                        Column(
                            // Define o espaço em torno da seleção de tema.
                            modifier = Modifier.padding(14.dp),
                            // Separa o rótulo dos botões.
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Tema", fontWeight = FontWeight.Bold, color = TextoRestante)
                            Row(
                                // Mantém os três temas na mesma linha.
                                modifier = Modifier.fillMaxWidth(),
                                // Separa os botões de tema.
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Claro", "Escuro", "Automático").forEach { opcao ->
                                    Surface(
                                        // Atualiza a escolha ilustrativa do tema.
                                        onClick = { tema = opcao },
                                        // Divide a largura igualmente entre as opções.
                                        modifier = Modifier.weight(1f),
                                        // Destaca a opção selecionada em marrom.
                                        color = if (tema == opcao) Color(0xFF493020) else Color.Transparent,
                                        // Mantém as bordas arredondadas.
                                        shape = RoundedCornerShape(14.dp),
                                        // Delimita cada botão.
                                        border = BorderStroke(1.dp, MarromRestante)
                                    ) {
                                        Column(
                                            // Define altura e espaço interno dos botões.
                                            modifier = Modifier.heightIn(min = 76.dp).padding(horizontal = 4.dp, vertical = 12.dp),
                                            // Centraliza o nome do tema.
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            // Centraliza o conteúdo verticalmente.
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                // Mostra o nome da opção.
                                                text = opcao,
                                                // Mantém os três nomes dentro da largura disponível.
                                                fontSize = 12.sp,
                                                // Destaca a opção.
                                                fontWeight = FontWeight.SemiBold,
                                                // Mantém contraste no botão selecionado.
                                                color = if (tema == opcao) Color.White else TextoRestante
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        LinhaAparencia()
                        Column(
                            // Mantém as margens internas do grupo.
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text("Brilho", fontWeight = FontWeight.Bold, color = TextoRestante)
                            Slider(
                                // Mostra o valor escolhido na demonstração.
                                value = brilho,
                                // Atualiza somente o controle visual do protótipo.
                                onValueChange = { brilho = it },
                                // Mantém o intervalo de 0 a 100 por cento.
                                valueRange = 0f..1f,
                                // Usa a largura disponível do cartão.
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                // Mostra a porcentagem ilustrativa.
                                text = (brilho * 100).toInt().toString() + "%",
                                // Mantém o valor secundário ao rótulo.
                                fontSize = 12.sp,
                                // Usa a cor auxiliar do aplicativo.
                                color = MarromRestante
                            )
                        }
                        LinhaAparencia()
                        OpcaoAparencia("Tamanho e estilo da fonte", fonte) { painel = "Fonte" }
                        LinhaAparencia()
                        OpcaoAparencia("Visualização das fichas", fichas) { painel = "Fichas" }
                        LinhaAparencia()
                        OpcaoAparencia("Reduzir movimentos", if (reduzir) "Ativado" else "Desativado") {
                            painel = "Movimentos"
                        }
                        LinhaAparencia()
                        Row(
                            // Mantém o alinhamento com as outras opções.
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                            // Centraliza texto e interruptor.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                // Identifica a preferência ilustrativa.
                                text = "Proteção para conforto ocular",
                                // Reserva espaço para o interruptor.
                                modifier = Modifier.weight(1f),
                                // Mantém o texto proporcional às outras opções.
                                fontSize = 14.sp,
                                // Destaca a preferência.
                                fontWeight = FontWeight.SemiBold,
                                // Mantém contraste no fundo claro.
                                color = TextoRestante
                            )
                            Switch(
                                // Mostra a escolha atual.
                                checked = conforto,
                                // Atualiza somente a preferência demonstrativa.
                                onCheckedChange = { conforto = it }
                            )
                        }
                    }
                }
            }
        }
    }

    val selecionado = painel
    if (selecionado != null) {
        val opcoes = when (selecionado) {
            "Fonte" -> listOf("Padrão", "Maior", "Muito maior")
            "Fichas" -> listOf("Padrão", "Compacta", "Detalhada")
            else -> listOf("Desativado", "Ativado")
        }
        val atual = when (selecionado) {
            "Fonte" -> fonte
            "Fichas" -> fichas
            else -> if (reduzir) "Ativado" else "Desativado"
        }
        AlertDialog(
            // Fecha o painel sem mudar a opção.
            onDismissRequest = { painel = null },
            // Identifica a preferência aberta.
            title = {
                Text(when (selecionado) {
                    "Fonte" -> "Tamanho e estilo da fonte"
                    "Fichas" -> "Visualização das fichas"
                    else -> "Reduzir movimentos"
                })
            },
            // Mostra as escolhas possíveis da demonstração.
            text = {
                Column {
                    Text(
                        // Explica que a escolha ainda não altera as outras telas.
                        text = "Seleção demonstrativa. A aplicação desta preferência será integrada depois.",
                        // Mantém o aviso em tamanho secundário.
                        fontSize = 12.sp
                    )
                    opcoes.forEach { opcao ->
                        Row(
                            // Ocupa a largura do painel.
                            modifier = Modifier.fillMaxWidth(),
                            // Centraliza o texto e o controle.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                // Marca a preferência atual.
                                selected = atual == opcao,
                                // Atualiza a opção correspondente e fecha o painel.
                                onClick = {
                                    when (selecionado) {
                                        "Fonte" -> fonte = opcao
                                        "Fichas" -> fichas = opcao
                                        else -> reduzir = opcao == "Ativado"
                                    }
                                    painel = null
                                }
                            )
                            Text(
                                // Mostra o nome da escolha.
                                text = opcao,
                                // Usa a cor padrão dos textos.
                                color = TextoRestante
                            )
                        }
                    }
                }
            },
            // Permite fechar sem alterar a escolha.
            confirmButton = {
                TextButton(
                    // Fecha o painel de opções.
                    onClick = { painel = null }
                ) { Text("Fechar") }
            },
            // Usa o fundo padrão das janelas.
            containerColor = FundoPadraoApp
        )
    }
    // Mostra os avisos da navegação inferior.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra uma preferência que abre opções detalhadas.
@Composable
private fun OpcaoAparencia(
    // Define o título da preferência.
    titulo: String,
    // Mostra a opção escolhida.
    resumo: String,
    // Abre o painel correspondente.
    abrir: () -> Unit
) {
    Surface(
        // Aciona o painel ao tocar na linha.
        onClick = abrir,
        // Preserva a cor do grupo.
        color = Color.Transparent
    ) {
        Row(
            // Define espaço interno e altura de toque.
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(14.dp),
            // Centraliza os elementos verticalmente.
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                // Reserva a largura restante para os textos.
                modifier = Modifier.weight(1f)
            ) {
                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextoRestante)
                Text(resumo, fontSize = 12.sp, color = MarromRestante)
            }
            Icon(
                // Reutiliza a seta existente no projeto.
                painter = painterResource(R.drawable.inicio_seta),
                // O texto da preferência já descreve a ação.
                contentDescription = null,
                // Mantém a seta proporcional à linha.
                modifier = Modifier.size(16.dp),
                // Usa a cor dos controles do aplicativo.
                tint = MarromRestante
            )
        }
    }
}

// Separa as opções do cartão.
@Composable
private fun LinhaAparencia() {
    HorizontalDivider(
        // Define uma linha discreta.
        thickness = 1.dp,
        // Mantém a separação clara.
        color = Color.White
    )
}

// Mostra esta tela no editor do Android Studio.
@Preview(
    // Define o nome da prévia.
    name = "Aparência e acessibilidade",
    // Mostra o fundo.
    showBackground = true,
    // Define a largura de referência.
    widthDp = 412,
    // Define a altura de referência.
    heightDp = 917
)
@Composable
private fun PreviewAparenciaAcessibilidade() {
    MaterialTheme {
        TelaAparenciaAcessibilidade(
            // Usa os dados de demonstração do projeto.
            ctx = contextoPreviewRestante()
        )
    }
}
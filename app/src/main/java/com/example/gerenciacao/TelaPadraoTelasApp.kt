package com.example.gerenciacao

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Disponibiliza a ação compartilhada e o posicionamento do botão flutuante.
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.FabPosition

// Habilita a prévia no Android Studio.
import androidx.compose.ui.tooling.preview.Preview

// Uma unica origem para as cores, margens e medidas do cabecalho de todas as telas internas.
internal val LaranjaPadraoApp = Color(0xFFFFAE49)
// Permite que uma tela use outra cor, mantendo o laranja como padrão.
val LocalCorCabecalhoApp = staticCompositionLocalOf { LaranjaPadraoApp }
internal val FundoPadraoApp = Color(0xFFFFFCF7)
internal val TextoPadraoApp = Color(0xFF38291F)
internal val MarromPadraoApp = Color(0xFF783E19)
internal val CremePadraoApp = Color(0xFFFFEFC5)

// A Activity compartilha a pessoa atual e os destinos; a foto nao e duplicada por tela.
data class DadosCabecalhoApp(
    val nome: String = "Helena",
    val categoria: String = "Administradora · Abrigo Esperança",
    val foto: ImageBitmap? = null,
    val aoPerfil: () -> Unit = {},
    val aoParcerias: () -> Unit = {},
    val aoSair: () -> Unit = {},
    val aoNotificacoes: (() -> Unit)? = null,
    val aoDescartarRascunho: () -> Unit = {},
    // Controla em quais telas a saudação da pessoa aparece.
    val mostrarSaudacao: Boolean = true
)

// O valor padrao permite abrir Previews sem uma Activity, sem fixar dados no aplicativo real.
val LocalCabecalhoApp = staticCompositionLocalOf { DadosCabecalhoApp() }

// Compartilha a ação padrão do botão adicionar entre as telas internas.
val LocalAdicionarFlutuanteApp = staticCompositionLocalOf<() -> Unit> { {} }

// Repete exatamente o padrao da home: avatar de 48 dp, saudacao, categoria e menu de tres pontos.
@Composable
fun CabecalhoPadraoApp(interceptarNavegacao: (() -> Unit) -> Unit = { it() }) {
    val dados = LocalCabecalhoApp.current
    var menu by rememberSaveable { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val executar: (() -> Unit) -> Unit = { acao -> menu = false; interceptarNavegacao(acao) }
    Row(Modifier.fillMaxWidth().background(LocalCorCabecalhoApp.current).statusBarsPadding()
        .padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(CremePadraoApp)
            .semantics { contentDescription = "Abrir perfil de ${dados.nome}" }
            .clickable { executar(dados.aoPerfil) }, contentAlignment = Alignment.Center) {
            val foto = dados.foto
            if (foto != null) Image(foto, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(dados.nome.trim().take(1).uppercase().ifBlank { "?" }, fontSize = 23.sp,
                fontWeight = FontWeight.Bold, color = MarromPadraoApp)
        }
        Spacer(Modifier.width(12.dp))
        // Mostra a saudação somente quando a tela permitir.
// O nome vem dos dados do perfil, sem fixar "Helena".
        if (dados.mostrarSaudacao) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val primeiroNome = dados.nome.trim().substringBefore(" ")

                Text(
                    text = if (primeiroNome.isBlank()) "Olá!" else "Olá, $primeiroNome!",
                    fontSize = 21.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPadraoApp
                )

                Text(
                    text = dados.categoria,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MarromPadraoApp
                )
            }
        } else {
            // Mantém o avatar à esquerda e os três pontos à direita.
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.width(6.dp))
        Box {
            IconButton(
                onClick = { menu = true }, modifier = Modifier.size(48.dp)
                    .semantics { contentDescription = "Abrir menu" }) {
                Canvas(Modifier.size(24.dp)) {
                    listOf(0.23f, 0.5f, 0.77f).forEach { y ->
                        drawCircle(
                            MarromPadraoApp,
                            2.dp.toPx(),
                            Offset(size.width / 2, size.height * y)
                        )
                    }
                }
            }
            DropdownMenu(
                // Mostra o menu quando os três pontos são pressionados.
                expanded = menu,
                // Fecha o menu ao tocar fora.
                onDismissRequest = { menu = false }
            ) {
                DropdownMenuItem(
                    // Mantém o acesso ao perfil e às configurações.
                    text = { Text("Meu perfil") },
                    // Abre o perfil da pessoa conectada.
                    onClick = { executar(dados.aoPerfil) }
                )

                // Separa o perfil da opção de saída.
                HorizontalDivider()

                DropdownMenuItem(
                    // Mantém a opção de sair.
                    text = { Text("Sair da conta") },
                    // Fecha o menu e solicita confirmação.
                    onClick = {
                        menu = false
                        confirmarSaida = true
                    }
                )
            }
        }
    }
    // Mantem a mesma confirmacao de saida em qualquer tela interna.
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false }, title = { Text("Sair da conta?") },
        text = { Text("Você voltará para a tela de login.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; executar(dados.aoSair) }) { Text("Sair", color = MarromPadraoApp) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = MarromPadraoApp) } }, containerColor = FundoPadraoApp)
    aviso?.let { texto -> AlertDialog(onDismissRequest = { aviso = null }, text = { Text(texto) },
        confirmButton = { TextButton(onClick = { aviso = null }) { Text("Entendi") } }, containerColor = FundoPadraoApp) }
}

// Padroniza cabeçalho, conteúdo, navegação e botão adicionar nas telas internas.
@Composable
fun MolduraPadraoApp(
    modifier: Modifier = Modifier,
    containerColor: Color = FundoPadraoApp,
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    interceptarCabecalho: (() -> Unit) -> Unit = { it() },
    floatingActionButton: @Composable () -> Unit = {
        // Usa a ação compartilhada quando a tela não definir um botão próprio.
        val abrirAdicionar = LocalAdicionarFlutuanteApp.current
        BotaoAdicionarFlutuante(
            aoAdicionar = {
                // Reaproveita a proteção de rascunho nas telas que já a configuram.
                interceptarCabecalho(abrirAdicionar)
            }
        )
    },
    conteudo: @Composable (PaddingValues) -> Unit
) {
    // Posiciona o mais sobre o conteúdo, acima da barra, sem criar uma faixa extra.
    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Aplica a mesma cor ao fundo atrás dos cantos arredondados.
        containerColor = LocalCorCabecalhoApp.current,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { CabecalhoPadraoApp(interceptarCabecalho) },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        // Mantém os botões antigos disponíveis no código, mas ocultos.
                floatingActionButton = {
            if (MOSTRAR_ADICIONAR_FLUTUANTE) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { espacos ->
        // Preserva os cantos arredondados e evita aplicar o espaçamento duas vezes.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacos)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(containerColor)
        ) {
            conteudo(PaddingValues(0.dp))
        }
    }
}

// Compartilha o mesmo botão de voltar entre as telas.
@Composable
fun BotaoVoltarPadrao(
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    descricao: String = "Voltar"
) {
    FilledIconButton(
        onClick = aoVoltar,
        modifier = modifier
            // Mantém uma área de toque confortável.
            .size(48.dp)
            .semantics {
                contentDescription = descricao
            },
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            // Usa o mesmo marrom do botão Demonstração.
            containerColor = Color(0xFF432818),
            contentColor = Color.White
        )
    ) {
        Canvas(Modifier.size(28.dp)) {
            val ponta = Offset(
                size.width * 0.20f,
                size.height * 0.50f
            )

            val extremidades = listOf(
                Offset(size.width * 0.85f, size.height * 0.50f),
                Offset(size.width * 0.50f, size.height * 0.20f),
                Offset(size.width * 0.50f, size.height * 0.80f)
            )

            extremidades.forEach { destino ->
                drawLine(
                    // Garante contraste com o círculo marrom.
                    color = Color.White,
                    start = ponta,
                    end = destino,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

// Mantém o botão de voltar ao lado do título nas telas internas.
@Composable
fun TituloPaginaPadrao(
    titulo: String,
    aoVoltar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (aoVoltar != null) {
            BotaoVoltarPadrao(aoVoltar = aoVoltar)

            Spacer(Modifier.width(8.dp))
        }

        Text(
            text = titulo,
            modifier = Modifier.weight(1f),
            fontSize = 24.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.Bold,
            color = TextoPadraoApp
        )
    }
}

// Protege rascunhos quando o novo cabecalho e usado para sair de um formulario.
@Composable
fun protegerRascunhoNoCabecalho(alterado: Boolean): (() -> Unit) -> Unit {
    var acaoPendente by remember { mutableStateOf<(() -> Unit)?>(null) }
    val dados = LocalCabecalhoApp.current
    if (acaoPendente != null) AlertDialog(onDismissRequest = { acaoPendente = null },
        title = { Text("Descartar alterações?") }, text = { Text("As alterações deste formulário ainda não foram salvas.") },
        confirmButton = { TextButton(onClick = {
            val acao = acaoPendente; acaoPendente = null
            dados.aoDescartarRascunho(); acao?.invoke()
        }) { Text("Descartar e sair") } },
        dismissButton = { TextButton(onClick = { acaoPendente = null }) { Text("Continuar editando") } }, containerColor = FundoPadraoApp)
    return { acao -> if (alterado) acaoPendente = acao else acao() }
}

// Mostra o cabeçalho e a estrutura compartilhada das telas.
// A saudação fica oculta para representar as telas internas.
@Preview(
    name = "Padrão das telas",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)

// Mostra a agenda completa, incluindo o cabeçalho padrão.
// A própria TelaAgenda já contém a estrutura compartilhada.
@Preview(
    name = "Agenda com cabeçalho padrão",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaTelaPadraoApp() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalCabecalhoApp provides DadosCabecalhoApp(
                nome = "Helena",
                mostrarSaudacao = false
            )
        ) {
            TelaAgenda(
                aoInicio = {},
                aoAnimais = {},
                aoAdicionar = {}
            )
        }
    }
}

// Troque para true caso queira reativar os botões flutuantes.
private const val MOSTRAR_ADICIONAR_FLUTUANTE = false

@Composable
fun BotaoAcaoTitulo(
    texto: String,
    aoClicar: () -> Unit
) {
    Button(
        onClick = aoClicar,
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFB21C),
            contentColor = Color(0xFF432818)
        )
    ) {
        Text(
            text = "+ $texto",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
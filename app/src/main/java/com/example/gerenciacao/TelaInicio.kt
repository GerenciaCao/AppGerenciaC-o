package com.example.gerenciacao

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
// Detecta a prévia para evitar conteúdo transparente no editor.
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// Mantem o creme e o marrom do aplicativo e acrescenta laranja apenas ao cabecalho da home.
private val FundoInicio = Color(0xFFFFFCF7)
private val TextoInicio = Color(0xFF38291F)
private val MarromInicio = Color(0xFF783E19)
private val AmareloInicio = Color(0xFFFFB414)
private val CremeInicio = Color(0xFFFFEFC5)
private val BordaInicio = Color(0xFFEADFD2)
private val CabecalhoInicio = Color(0xFFFFAE49)
private val BannerInicio = Color(0xFFFFD600)

// Reorganiza somente a tela inicial e conserva os callbacks utilizados no projeto atual.
@Composable
fun TelaInicio(
    aoSair: () -> Unit,
    aoAbrirAnimais: () -> Unit,
    aoNovaFicha: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit = {},
    nomePessoa: String = "Helena",
    categoriaPessoa: String = "Administradora · Abrigo Esperança",
    fotoPerfil: ImageBitmap? = null,
    aoAbrirPerfil: () -> Unit = {},
    aoNotificacoes: (() -> Unit)? = null,
    aoRegistrarDoacao: (() -> Unit)? = null,
    aoRegistrarDespesa: (() -> Unit)? = null,
    compromissos: List<CompromissoAbrigo> = emptyList(),
    // Recebe as fichas para localizar a foto do animal vinculado.
    animais: List<AnimalExemplo> = emptyList(),
    // Abre os detalhes do compromisso selecionado.
    aoAbrirCompromisso: (String) -> Unit = { aoAgenda() },
    aoAbrirParcerias: (() -> Unit)? = null,
    aoEncontrarCuidadores: (() -> Unit)? = null,
    imagemParceria: Int? = null,
    // Recebe a quantidade de animais atualmente acolhidos.
    totalAnimaisNoAbrigo: Int = 0,
    // Recebe a quantidade de animais que já foram adotados.
    totalAnimaisAdotados: Int = 0,
    // Recebe a capacidade cadastrada em Meu abrigo.
    capacidadeAbrigo: Int = 0,
    // Abre os dados do abrigo pelo indicador de ocupação.
    aoVerDadosAbrigo: () -> Unit = {},
    // Recebe a data do compromisso tocado no aviso do Início.
    aoAbrirAgendaNaData: (String) -> Unit = { aoAgenda() }
) {
    // Mantem o menu e os avisos locais durante a rotacao do celular.
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }
    val avisar: (String) -> Unit = { mensagem = it }
    val abrirParcerias = {
        aoAbrirParcerias?.invoke() ?: avisar("As parcerias estarão disponíveis em breve.")
    }
    // O botao Voltar usa a mesma confirmacao da opcao Sair, evitando saida acidental.
    BackHandler { confirmarSaida = true }

    // A prévia mostra a tela pronta, sem executar atualizações contínuas.
    val modoPrevia = LocalInspectionMode.current

    // Mantém o horário atualizado no aplicativo, mas não inicia o loop no editor.
    val instante by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = modoPrevia
    ) {
        if (!modoPrevia) {
            while (true) {
                value = System.currentTimeMillis()
                delay(60_000)
            }
        }
    }
    val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Date(instante))
    val horarioAtual = SimpleDateFormat("HH:mm", Locale.ROOT).format(Date(instante))
    val proximo = compromissos.asSequence()
        .filter { dataValidaAbrigo(it.data) && Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(it.horario) }
        .filter { chaveDataAbrigo(it.data) + it.horario >= chaveDataAbrigo(hoje) + horarioAtual }
        .minByOrNull { chaveDataAbrigo(it.data) + it.horario }

    // Animacao curta de entrada; o Compose respeita a escala de animacoes do Android.
    // Começa visível na prévia; preserva a animação de entrada no aplicativo.
    var conteudoVisivel by remember(modoPrevia) { mutableStateOf(modoPrevia) }
    LaunchedEffect(modoPrevia) {
        if (!modoPrevia) conteudoVisivel = true
    }
    val entrada by animateFloatAsState(if (conteudoVisivel) 1f else 0f,
        animationSpec = tween(280), label = "entradaInicio")

    // A home e as outras telas internas agora usam a mesma moldura e o mesmo cabecalho.
    // Recupera o destino da Loja para o botão do banner.
    val abrirLojaBanner = LocalAbrirLoja.current
    val topoAtual = LocalCabecalhoApp.current
    CompositionLocalProvider(LocalCabecalhoApp provides topoAtual.copy(
        // A saudação permanece visível no Início, inclusive em prévias.
        mostrarSaudacao = true,
        nome = nomePessoa, categoria = categoriaPessoa, foto = fotoPerfil,
        aoPerfil = aoAbrirPerfil, aoParcerias = { abrirParcerias() }, aoSair = aoSair,
        aoNotificacoes = aoNotificacoes ?: topoAtual.aoNotificacoes
    )) {
        // Liga o botão ao espaço flutuante da moldura, separado da barra inferior.
        MolduraPadraoApp(
            // Esconde o botão + no Início, que já possui ações rápidas.
            floatingActionButton = {},
            bottomBar = {
                MenuInferiorInicio(
                    avisar = avisar,
                    aoAnimais = aoAbrirAnimais,
                    aoAdicionar = aoAdicionar,
                    aoAgenda = aoAgenda
                )
            }
        ) { espacos ->
            Column(Modifier.fillMaxSize().padding(espacos).graphicsLayer {
                // Não depende do relógio de animação do editor para mostrar a tela.
                alpha = if (modoPrevia) 1f else entrada
                translationY = if (modoPrevia) 0f else (1f - entrada) * 12.dp.toPx()
            }.verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 22.dp, bottom = 28.dp)) {
                // Posiciona o carrossel no topo do conteúdo.
                CarrosselAutomaticoInicio(
                    // Mantém o direcionamento do banner para a Loja.
                    aoAbrirParcerias = abrirLojaBanner
                )

                // Separa o aviso das ações rápidas.
                Spacer(Modifier.height(22.dp))

                // Identifica a seção de atalhos.
                Text(
                    text = "Ações rápidas",
                    // Mantém o tamanho dos outros títulos da tela.
                    fontSize = 18.sp,
                    // Destaca o título.
                    fontWeight = FontWeight.Bold,
                    // Usa a cor principal do aplicativo.
                    color = TextoInicio
                )

                // Separa o título dos botões.
                Spacer(Modifier.height(12.dp))

                // Atalhos menores: icone a esquerda e texto ao lado, com rolagem horizontal.
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val largura = (maxWidth * 0.47f).coerceIn(142.dp, 166.dp)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AtalhoCompactoInicio("Cadastrar animal", R.drawable.iconanimais, Color(0xFF432818), largura) { aoNovaFicha() }
                        AtalhoCompactoInicio("Registrar doação", R.drawable.inicio_carteira, Color(0xFF432818), largura) {
                            aoRegistrarDoacao?.invoke() ?: avisar("O registro de doações estará disponível em breve.")
                        }
                        AtalhoCompactoInicio("Registrar despesa", R.drawable.inicio_recibo, Color(0xFF432818), largura) {
                            aoRegistrarDespesa?.invoke() ?: avisar("O registro de despesas estará disponível em breve.")
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))

                AgendaPilulasInicio(
                    // Recebe os compromissos cadastrados.
                    compromissos = compromissos,
                    // Recebe os animais e suas fotos.
                    animais = animais,
                    // Define o começo da sequência de dias.
                    hoje = hoje,
                    // Abre o calendário completo.
                    aoAbrirAgenda = aoAgenda,
                    // Abre uma data específica.
                    aoAbrirDia = aoAbrirAgendaNaData,
                    // Abre os detalhes ao tocar no cartão.
                    aoAbrirCompromisso = aoAbrirCompromisso
                )

                // Separa a agenda do indicador.
                Spacer(Modifier.height(22.dp))

                // Apresenta as três estatísticas do abrigo.
                OcupacaoAbrigoInicio(
                    // Animais que atualmente ocupam vagas.
                    totalAnimais = totalAnimaisNoAbrigo,
                    // Animais que já foram adotados.
                    adotados = totalAnimaisAdotados,
                    // Capacidade cadastrada em Meu abrigo.
                    capacidade = capacidadeAbrigo,
                    // Abre as informações do abrigo.
                    aoVerDados = aoVerDadosAbrigo
                )

                // Separa a ocupação do cartão de redes de conexões.
                Spacer(Modifier.height(22.dp))

                // Reserva o acesso solicitado, sem consultar localizacao ou mostrar resultados ficticios.
                Surface(onClick = {
                    aoEncontrarCuidadores?.invoke() ?: avisar("Em breve você poderá encontrar cuidadores e abrigos na sua região.")
                }, color = Color.White, shape = RoundedCornerShape(19.dp), border = BorderStroke(1.dp, BordaInicio)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                            IconeInicio(R.drawable.iconabrigos, null, 50, Color.Unspecified)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Encontre cuidadores perto de você", fontSize = 15.sp, lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold, color = TextoInicio)
                            Text("Conheça outros abrigos e quem acolhe na sua região.", fontSize = 12.sp, lineHeight = 17.sp, color = MarromInicio)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconeInicio(R.drawable.inicio_seta, null, 15)
                    }
                }
            }
        }
    }
    // Confirma a saida da conta em vez de sair imediatamente por um toque acidental.
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false }, title = { Text("Sair da conta?") },
        text = { Text("Você voltará para a tela de login.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; aoSair() }) { Text("Sair", color = MarromInicio) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = MarromInicio) } }, containerColor = FundoInicio)
    mensagem?.let { texto -> AlertDialog(onDismissRequest = { mensagem = null }, text = { Text(texto) },
        confirmButton = { TextButton(onClick = { mensagem = null }) { Text("Entendi", color = MarromInicio) } }, containerColor = FundoInicio) }
}

// Apresenta uma proposta de divulgacao da marca com texto separado da fotografia.
@Composable
private fun BannerPedigreeInicio(imagem: Int?) {
    val navegador = LocalUriHandler.current
    var erro by remember { mutableStateOf(false) }
    Surface(color = BannerInicio, shape = RoundedCornerShape(21.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(0.58f).padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Usa o nome da marca em texto, mantendo a leitura acessivel.
                Surface(color = Color(0xFFB5132A), shape = RoundedCornerShape(8.dp)) {
                    Text("PEDIGREE®", Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White)
                }
                Text("Para quem ama\ncuidar de cães.", fontSize = 18.sp, lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold, color = TextoInicio)
                // Abre o site oficial no navegador; nao existe compra ou pagamento dentro do app.
                Button(onClick = {
                    runCatching { navegador.openUri("https://www.pedigree.com.br/") }
                        .onFailure { erro = true }
                }, modifier = Modifier.heightIn(min = 44.dp), contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C231B), contentColor = Color.White)) {
                    Text("Conheça a marca", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            // Reutiliza a imagem escolhida no projeto, incluindo o nome cachorro_inicio do usuario.
            Box(Modifier.weight(0.42f).height(174.dp), contentAlignment = Alignment.BottomCenter) {
                if (imagem != null) Image(painterResource(imagem), null, Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, alignment = Alignment.BottomCenter)
                else Icon(painterResource(R.drawable.inicio_pata), null,
                    Modifier.size(72.dp).padding(8.dp), tint = MarromInicio)
            }
        }
    }
    // Retorna um aviso caso o aparelho nao consiga abrir o navegador.
    if (erro) AlertDialog(onDismissRequest = { erro = false },
        text = { Text("Não foi possível abrir o site agora. Tente novamente mais tarde.") },
        confirmButton = { TextButton(onClick = { erro = false }) { Text("Entendi") } },
        containerColor = FundoInicio)
}

// Destaca a categoria e responde ao toque com uma pequena compressao e retorno suave.
@Composable
private fun AtalhoCompactoInicio(titulo: String, icone: Int, fundo: Color, largura: androidx.compose.ui.unit.Dp, acao: () -> Unit) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(if (pressionado) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 550f), label = "escalaAtalho")
    val destaque = when (icone) {
        R.drawable.inicio_pata -> Color(0xFF432818)
        R.drawable.inicio_coracao -> Color(0xFF432818)
        R.drawable.inicio_recibo -> Color(0xFF432818)
        else -> Color(0xFF432818)
    }
    Surface(onClick = acao, interactionSource = interacao,
        modifier = Modifier.width(largura).graphicsLayer { scaleX = escala; scaleY = escala },
        color = fundo, shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, destaque.copy(alpha = 0.18f)), shadowElevation = 1.dp) {
        Row(Modifier.heightIn(min = 70.dp).padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(destaque, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                IconeInicio(icone, null, 20, Color.White)
            }
            Spacer(Modifier.width(9.dp))
            Text(titulo, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFFCF7))
        }
    }
}

// Renderiza os icones vetoriais existentes sem exigir novos arquivos para o menu.
@Composable
private fun IconeInicio(recurso: Int, descricao: String?, tamanho: Int = 24, cor: Color = MarromInicio) {
    Icon(painterResource(recurso), descricao, Modifier.size(tamanho.dp), tint = cor)
}

// Distribui os cinco destinos igualmente, sem reservar uma faixa para o botão mais.
@Composable
fun MenuInferiorInicio(
    avisar: (String) -> Unit,
    selecionado: String = "Início",
    aoInicio: () -> Unit = {},
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit
) {
    // Preserva os destinos que já estão conectados ao aplicativo.
    val abrirFinancas = LocalAbrirFinancas.current
    val abrirLoja = LocalAbrirLoja.current

    // Mantém a barra branca e reta, como no print enviado.
    // Somente o item selecionado recebe a cápsula marrom.
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, BordaInicio)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemMenuInicio(
                "Início", R.drawable.iconhome,
                selecionado == "Início", Modifier.weight(1f), aoInicio
            )
            ItemMenuInicio(
                "Loja", R.drawable.inicio_loja,
                selecionado == "Loja", Modifier.weight(1f), abrirLoja
            )
            ItemMenuInicio(
                "Animais", R.drawable.iconanimais,
                selecionado == "Animais", Modifier.weight(1f), aoAnimais
            )
            ItemMenuInicio(
                "Finanças", R.drawable.inicio_carteira,
                selecionado == "Finanças", Modifier.weight(1f), abrirFinancas
            )
            ItemMenuInicio(
                "Agenda", R.drawable.iconcalendario,
                selecionado == "Agenda", Modifier.weight(1f), aoAgenda
            )
        }
    }
}

// Mantém o botão mais independente da barra de navegação.
@Composable
fun BotaoAdicionarFlutuante(aoAdicionar: () -> Unit) {
    FloatingActionButton(
        onClick = aoAdicionar,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        containerColor = AmareloInicio,
        contentColor = TextoInicio
    ) {
        IconeInicio(R.drawable.inicio_mais, "Adicionar", 28, TextoInicio)
    }
}

// Anima a selecao do menu e mantem os mesmos destinos e rotulos.
@Composable
private fun ItemMenuInicio(
    titulo: String,
    icone: Int,
    selecionado: Boolean,
    modifier: Modifier,
    aoClicar: () -> Unit
) {
    // Detecta o toque para dar uma resposta visual suave.
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val modoPrevia = LocalInspectionMode.current
    val marromSelecionado = Color(0xFF493020)

    // Faz a cápsula mudar gradualmente entre transparente e marrom.
    val fundoAnimado by animateColorAsState(
        targetValue = if (selecionado) marromSelecionado else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "fundoItemNavegacao"
    )

    // Ícone e texto ficam claros sobre o marrom e escuros sobre o branco.
    val conteudoAnimado by animateColorAsState(
        targetValue = if (selecionado) Color(0xFFFFFCF7) else Color(0xFF706359),
        animationSpec = tween(durationMillis = 180),
        label = "corItemNavegacao"
    )

    // A leve compressão responde ao toque e retorna com uma mola suave.
    val escalaAnimada by animateFloatAsState(
        targetValue = if (pressionado) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = 500f
        ),
        label = "toqueItemNavegacao"
    )

    // A prévia mostra o resultado final sem depender do relógio de animação.
    val fundo = if (modoPrevia) {
        if (selecionado) marromSelecionado else Color.Transparent
    } else fundoAnimado
    val corConteudo = if (modoPrevia) {
        if (selecionado) Color(0xFFFFFCF7) else Color(0xFF706359)
    } else conteudoAnimado

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .graphicsLayer {
                scaleX = if (modoPrevia) 1f else escalaAnimada
                scaleY = if (modoPrevia) 1f else escalaAnimada
            }
            .clip(RoundedCornerShape(17.dp))
            .background(fundo)
            // Usa a seleção como estado acessível, inclusive para leitores de tela.
            .selectable(
                selected = selecionado,
                interactionSource = interacao,
                indication = androidx.compose.foundation.LocalIndication.current,
                role = androidx.compose.ui.semantics.Role.Tab,
                onClick = aoClicar
            )
            .heightIn(min = 56.dp)
            .padding(horizontal = 2.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(icone),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = corConteudo
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = titulo,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
            color = corConteudo
        )
    }
}

// Mostra a tela inteira com o item Início selecionado em marrom.
@Preview(
    name = "Início — celular",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviewInicioRefinado() {
    MaterialTheme {
        TelaInicio(
            aoSair = {},
            aoAbrirAnimais = {},
            aoNovaFicha = {},
            aoAdicionar = {},
            imagemParceria = R.drawable.pedigree
        )
    }
}

// Prévia adicional para conferir textos maiores sem alterar o aplicativo.
@Preview(
    name = "Início — fonte ampliada",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.3f
)
@Composable
private fun PreviewInicioRefinadoFonteMaior() {
    MaterialTheme {
        TelaInicio(
            aoSair = {},
            aoAbrirAnimais = {},
            aoNovaFicha = {},
            aoAdicionar = {},
            imagemParceria = R.drawable.pedigree
        )
    }
}
// Apresenta um resumo do dia sem chamar todo agendamento de urgência.
// É um aviso dentro do aplicativo, não uma notificação do Android.
@Composable
private fun AvisoCuidadosHojeInicio(
    compromissos: List<CompromissoAbrigo>,
    hoje: String,
    horarioAtual: String,
    // Envia a data do próximo compromisso para a navegação.
    aoAbrirAgenda: (String?) -> Unit
) {
    // Aceita apenas datas e horários válidos para comparar os agendamentos.
    val horariosValidos = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")
    // Monta uma chave ordenável com data e horário atuais.
    val instanteAtual = chaveDataAbrigo(hoje) + horarioAtual

    // Escolhe o compromisso futuro mais próximo, independentemente do mês.
    // Não considera datas passadas como pendências sem um campo de conclusão.
    val seguinte = compromissos
        .filter { dataValidaAbrigo(it.data) && horariosValidos.matches(it.horario) }
        .filter { chaveDataAbrigo(it.data) + it.horario >= instanteAtual }
        .minByOrNull { chaveDataAbrigo(it.data) + it.horario }

    // Usa um título correto para hoje ou qualquer data futura.
    val tituloAviso = if (seguinte?.data == hoje) {
        "Cuidados de hoje"
    } else {
        "Próximo compromisso"
    }

    // Mostra o nome cadastrado do compromisso.
    val resumo = seguinte?.titulo ?: "Nenhum compromisso futuro cadastrado."

    // Mostra a data exata para não confundir amanhã com próxima semana.
    val complemento = seguinte?.let {
        (if (it.data == hoje) "Hoje" else it.data) +
                " · " + it.horario + " · Ver agenda"
    } ?: "Toque para abrir a agenda"
    // Mantém o cartão compacto com a paleta creme e marrom do aplicativo.
    Surface(
        // Usa a data do mesmo compromisso exibido no cartão.
        onClick = { aoAbrirAgenda(seguinte?.data) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFE4EBDD),
        border = BorderStroke(1.dp, Color(0xFFE8C7B8))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
                    .background(Color(0xFFFFFCF7), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.iconcalendario),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = MarromInicio
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                // Destaca o título do aviso com uma fonte mais encorpada.
                Text(
                    text = tituloAviso,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextoInicio
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    resumo,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextoInicio
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    complemento,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MarromInicio
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.inicio_seta),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MarromInicio
            )
        }
    }
}
// TROQUE AQUI pelos nomes dos seus dois banners completos.
// Estas imagens provisórias já existem no projeto.
private val ImagensCarrosselInicio = listOf(
    R.drawable.bannerinicio,
    R.drawable.bannerpedigree
)

// Alterna automaticamente os banners sem prejudicar a prévia.
@Composable
private fun CarrosselAutomaticoInicio(aoAbrirParcerias: () -> Unit) {
    // Mantém a quantidade real de banners usada pelos pontos e pelas imagens.
    val quantidade = ImagensCarrosselInicio.size
    // Começa no meio da sequência, alinhado ao primeiro banner real.
    val primeiraPagina = if (quantidade > 1) {
        val meio = Int.MAX_VALUE / 2
        meio - meio % quantidade
    } else 0
    // Usa uma identidade nova para não restaurar a posição do carrossel antigo.
    val paginas = key("circular-inicio", quantidade) {
        rememberPagerState(
            // Permite mostrar o banner anterior já na primeira abertura.
            initialPage = primeiraPagina,
            // Repete a sequência sem saltar do último ao primeiro na navegação normal.
            pageCount = { if (quantidade > 1) Int.MAX_VALUE else 1 }
        )
    }
    val arrastando by paginas.interactionSource.collectIsDraggedAsState()
    var pausado by rememberSaveable { mutableStateOf(false) }
    val modoPrevia = LocalInspectionMode.current
    val escopo = rememberCoroutineScope()

    // Aguarda três segundos e passa ao próximo banner com movimento suave.
    // Arrastar pausa a troca; ao soltar, a contagem começa novamente.
    LaunchedEffect(paginas, arrastando, pausado, modoPrevia) {
        if (!modoPrevia && !arrastando && !pausado && ImagensCarrosselInicio.size > 1) {
            while (true) {
                delay(2_500)
                if (!paginas.isScrollInProgress) {
                    paginas.animateScrollToPage(
                        // Avança na sequência repetida mantendo os dois vizinhos.
                        page = paginas.currentPage + 1,
                        animationSpec = tween(durationMillis = 600)
                    )
                }
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // A altura padrão evita que o restante da tela salte a cada troca.
        HorizontalPager(
            // Reserva o mesmo espaço para o banner anterior e para o próximo.
            contentPadding = PaddingValues(horizontal = if (quantidade > 1) 20.dp else 0.dp),
            state = paginas,
            // Define a distância entre um banner e outro.
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { paginaVirtual ->
            // Converte a posição repetida no índice de uma imagem já existente.
            val indice = paginaVirtual % quantidade
            if (indice == 0) {
                // Monta o primeiro banner com textos e botão reais.
                BannerParceriasNativoInicio(
                    // Abre a mesma Loja utilizada pela navegação inferior.
                    aoAbrirLoja = aoAbrirParcerias
                )
            } else {
                // Mantém a segunda arte inteira no formato padrão da loja.
                Box(
                    modifier = Modifier
                        // Ocupa a largura entre as margens do conteúdo.
                        .fillMaxWidth()
                        // Define a proporção de referência da loja.
                        .aspectRatio(372f / 154f)
                        // Arredonda os cantos da arte.
                        .clip(RoundedCornerShape(16.dp))
                        // Preenche eventuais transparências da segunda imagem.
                        .background(Color(0xFFFFD600))
                ) {
                    Image(
                        // Usa a segunda imagem da lista do carrossel.
                        painter = painterResource(ImagensCarrosselInicio[indice]),
                        // Identifica a arte para leitores de tela.
                        contentDescription = "Banner do parceiro",
                        // Ocupa a área reservada para a imagem.
                        modifier = Modifier.fillMaxSize(),
                        // Mantém a imagem inteira sem deformar a arte.
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        // Centraliza os indicadores na largura total, como no carrossel da Loja.
        Box(
            modifier = Modifier
                // Usa a mesma largura do banner.
                .fillMaxWidth()
                // Reserva uma linha para os pontos e o controle de pausa.
                .heightIn(min = 48.dp)
        ) {
            Row(
                // Coloca o grupo de pontos exatamente no centro da linha.
                modifier = Modifier.align(Alignment.Center),
                // Alinha os pontos pela mesma altura.
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagensCarrosselInicio.forEachIndexed { indice, _ ->
                    // Mantém apenas um ponto para cada banner real.
                    val ativo = paginas.currentPage % quantidade == indice
                    Box(
                        modifier = Modifier
                            // Mantém uma área de toque maior que o ponto visível.
                            .size(40.dp)
                            .semantics {
                                // Identifica qual banner será aberto.
                                contentDescription = "Mostrar banner " + (indice + 1)
                            }
                            .clickable {
                                // Permite selecionar um banner manualmente.
                                escopo.launch {
                                    // Abre a cópia próxima do banner escolhido, sem voltar ao começo da sequência.
                                    val destino = paginas.currentPage - (paginas.currentPage % quantidade) + indice
                                    paginas.animateScrollToPage(destino)
                                }
                            },
                        // Centraliza o ponto dentro da área de toque.
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                // O ponto ativo é maior que o inativo.
                                .size(if (ativo) 8.dp else 6.dp)
                                // Desenha o formato circular.
                                .clip(CircleShape)
                                // Destaca o ponto do banner atual.
                                .background(
                                    if (ativo) MarromInicio
                                    else MarromInicio.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// Cria o banner sem fundo, texto ou botão incorporados ao PNG.
@Composable
private fun BannerParceriasNativoInicio(
    // Ação executada ao tocar em Ir para parcerias.
    aoAbrirLoja: () -> Unit
) {
    // Consulta o tamanho de fonte do aparelho para evitar textos cortados.
    val escalaFonte = LocalDensity.current.fontScale

    BoxWithConstraints(
        // Usa a mesma largura disponível para os banners da loja.
        modifier = Modifier.fillMaxWidth()
    ) {
        // Calcula a altura pela mesma proporção do banner da loja.
        val alturaBanner = maxWidth / (372f / 154f)

        Surface(
            // Define a largura e a altura do cartão.
            modifier = Modifier
                .fillMaxWidth()
                .height(alturaBanner),
            // Arredonda os quatro cantos do banner.
            shape = RoundedCornerShape(16.dp),
            // Desenha o laranja no código, sem depender de uma imagem de fundo.
            color = Color(0xFFFFAE43)
        ) {
            Row(
                // Preenche o cartão sem sobrepor texto e cachorro.
                modifier = Modifier.fillMaxSize(),
                // Mantém o cachorro apoiado na parte inferior do cartão.
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        // Reserva 60% da largura útil para o texto e o botão.
                        .weight(0.60f)
                        // Usa toda a altura disponível do banner.
                        .fillMaxHeight()
                        // Afasta o texto das bordas e mantém espaço junto à foto.
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    // Distribui título, descrição e botão sem sobreposição.
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Spacer(Modifier.width(8.dp))
                    Text(
                        // Título do banner, editável diretamente no Kotlin.
                        text = "Cuidar do seu pet custa menos!",
                        // Mantém o azul da referência do marketing.
                        color = Color(0xFF175A91),
                        // Define o tamanho das letras do título.
                        fontSize = 20.sp,
                        // Define a distância entre as duas linhas.
                        lineHeight = 20.sp,
                        // Deixa o título mais encorpado.
                        fontWeight = FontWeight.ExtraBold
                    )

                    Button(
                        // Redireciona para a Loja pelo destino compartilhado do app.
                        onClick = aoAbrirLoja,
                        modifier = Modifier
                            // Mantém uma área confortável para tocar.
                            .heightIn(min = 48.dp),
                        // Arredonda o botão sem transformá-lo em uma cápsula.
                        shape = RoundedCornerShape(12.dp),
                        // Controla o espaço interno da legenda.
                        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 6.dp),
                        // Define as cores do botão e do texto.
                        colors = ButtonDefaults.buttonColors(
                            // Fundo azul seguindo a paleta do marketing.
                            containerColor = Color(0xFF00A9F4),
                            // Texto marrom para contrastar com o fundo azul.
                            contentColor = TextoInicio
                        )
                    ) {
                        Text(
                            // Legenda da ação.
                            text = "Ir para Loja",
                            // Tamanho da legenda.
                            fontSize = 15.sp,
                            // Espaçamento vertical das linhas da legenda.
                            lineHeight = 14.sp,
                            // Destaca o botão com texto em negrito.
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFCF7)
                        )
                    }
                }

                Image(
                    // Usa somente o cachorro PNG que foi adicionado ao drawable.
                    painter = painterResource(R.drawable.cachorroinicio),
                    // A foto é decorativa; os textos já descrevem a oferta.
                    contentDescription = null,
                    modifier = Modifier
                        // Reserva 40% da largura para o cachorro.
                        .weight(0.40f)
                        // Permite que a foto use a altura inteira.
                        .fillMaxHeight(),
                    // Preserva as proporções do cachorro sem esticar ou cortar.
                    contentScale = ContentScale.Fit,
                    // Encosta a base da imagem na base do banner.
                    alignment = Alignment.BottomEnd
                )
            }
        }
    }
}
        // Exibe os próximos sete dias e os compromissos do dia selecionado.
        @Composable
        private fun AgendaPilulasInicio(
        // Compromissos disponíveis para cada dia.
        compromissos: List<CompromissoAbrigo>,
          // Fichas usadas para identificar o animal e sua foto.
         animais: List<AnimalExemplo>,
         // Data atual.
         hoje: String,
         // Navegação para o calendário completo.
         aoAbrirAgenda: () -> Unit,
          // Navegação para uma data específica.
         aoAbrirDia: (String) -> Unit,
          // Navegação para os detalhes do compromisso.
         aoAbrirCompromisso: (String) -> Unit
) {
            // Guarda o dia expandido e fecha a seleção quando a data atual muda.
            var diaAberto by rememberSaveable(hoje) {
                mutableStateOf<String?>(null)
            }

            // Gera datas completas, inclusive ao atravessar meses e anos.
            val dias = remember(hoje) {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
                val formatoSemana = SimpleDateFormat(
                    "EEE",
                    Locale("pt", "BR")
                )
                val calendario = java.util.Calendar.getInstance().apply {
                    time = requireNotNull(formato.parse(hoje))
                }

                List(7) { indice ->
                    val dataDia = calendario.clone() as java.util.Calendar
                    dataDia.add(java.util.Calendar.DAY_OF_MONTH, indice)

                    Triple(
                        formato.format(dataDia.time),
                        dataDia.get(java.util.Calendar.DAY_OF_MONTH),
                        formatoSemana.format(dataDia.time)
                            .replace(".", "")
                            .uppercase(Locale("pt", "BR"))
                    )
                }
            }

            // Filtra os compromissos do dia escolhido.
            val registros = compromissos
                .filter { it.data == diaAberto }
                .sortedBy { it.horario }

            Column(
                // Ocupa a largura do conteúdo.
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    // Distribui título e acesso ao calendário.
                    modifier = Modifier.fillMaxWidth(),
                    // Alinha os dois elementos verticalmente.
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // Identifica a seção.
                        text = "Agenda",
                        // Reserva espaço para o botão à direita.
                        modifier = Modifier.weight(1f),
                        // Mantém o padrão dos títulos da tela inicial.
                        fontSize = 18.sp,
                        // Destaca o título.
                        fontWeight = FontWeight.Bold,
                        // Usa o texto principal do aplicativo.
                        color = TextoInicio
                    )

                    TextButton(
                        // Abre a agenda completa.
                        onClick = aoAbrirAgenda
                    ) {
                        Text(
                            // Identifica a navegação.
                            text = "Ver agenda",
                            // Mantém a cor dos links.
                            color = MarromInicio,
                            // Define o tamanho do link.
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    // Permite rolar em celulares com largura menor.
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    // Separa as pílulas.
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dias.forEach { (data, numero, semana) ->
                        // Identifica a pílula selecionada.
                        val selecionado = diaAberto == data
                        // Verifica compromissos de qualquer categoria.
                        val temCompromisso = compromissos.any {
                            it.data == data
                        }

                        Surface(
                            // Um segundo toque no mesmo dia recolhe os detalhes.
                            onClick = {
                                diaAberto = if (selecionado) null else data
                            },
                            // Define uma área de toque confortável.
                            modifier = Modifier
                                .width(48.dp)
                                .heightIn(min = 76.dp)
                                .semantics {
                                    contentDescription = data +
                                            if (temCompromisso) {
                                                ", com compromissos"
                                            } else {
                                                ", sem compromissos"
                                            }
                                },
                            // Deixa o formato de pílula.
                            shape = RoundedCornerShape(24.dp),
                            // Usa laranja na seleção e vermelho claro nos dias agendados.
                            color = when {
                                selecionado -> Color(0xFFFFAE49)
                                temCompromisso -> Color(0xFFFFC5C5)
                                else -> Color.White
                            },
                            // Destaca hoje mesmo quando a pílula não está selecionada.
                            border = BorderStroke(
                                1.dp,
                                if (data == hoje) {
                                    Color(0xFFFFAE49)
                                } else {
                                    BordaInicio
                                }
                            )
                        ) {
                            Column(
                                // Mantém os textos afastados das bordas.
                                modifier = Modifier.padding(vertical = 12.dp),
                                // Centraliza o número e o dia da semana.
                                horizontalAlignment = Alignment.CenterHorizontally,
                                // Separa os dois textos.
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    // Mostra o número do dia.
                                    text = numero.toString(),
                                    // Dá destaque ao número.
                                    fontSize = 19.sp,
                                    // Mantém boa leitura.
                                    fontWeight = FontWeight.Bold,
                                    // Usa contraste sobre os fundos claros.
                                    color = TextoInicio
                                )

                                Text(
                                    // Mostra a abreviação do dia da semana.
                                    text = semana,
                                    // Mantém a abreviação compacta.
                                    fontSize = 11.sp,
                                    // Usa a mesma cor do número.
                                    color = TextoInicio
                                )
                            }
                        }
                    }
                }

                // Mostra os detalhes somente depois de escolher uma pílula.
                androidx.compose.animation.AnimatedVisibility(
                    // Mantém a seção recolhida inicialmente.
                    visible = diaAberto != null
                ) {
                    Column(
                        // Separa os detalhes das pílulas.
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            // Explicita a data selecionada.
                            text = "Compromissos de " + diaAberto.orEmpty(),
                            // Destaca a data.
                            fontWeight = FontWeight.SemiBold,
                            // Mantém o tamanho do conteúdo.
                            fontSize = 14.sp,
                            // Usa a cor principal.
                            color = TextoInicio
                        )

                        if (registros.isEmpty()) {
                            Text(
                                // Explica quando o dia não tem registros.
                                text = "Nenhum compromisso neste dia.",
                                // Separa a mensagem do título.
                                modifier = Modifier.padding(top = 8.dp),
                                // Mantém o texto secundário.
                                fontSize = 14.sp,
                                // Usa a cor marrom.
                                color = MarromInicio
                            )
                        } else {
                            // Limita a prévia para não alongar demais a tela inicial.
                            // Mantém até três cartões na prévia do dia.
                            registros.take(3).forEach { compromisso ->
                                // Localiza a ficha pelo vínculo salvo no compromisso.
                                val animalVinculado = animais.firstOrNull {
                                    it.id == compromisso.animalId
                                }

                                // Separa os cartões e o título da data.
                                Spacer(Modifier.height(10.dp))

                                // Reutiliza exatamente o cartão da agenda.
                                CartaoCompromissoAgendaUnica(
                                    // Exibe título, data, horário e categoria.
                                    compromisso = compromisso,
                                    // Usa a foto da ficha quando houver animal vinculado.
                                    animal = animalVinculado,
                                    // Não adiciona fotos fictícias aos compromissos.
                                    fotoIlustrativa = null,
                                    // Abre os detalhes desse registro.
                                    aoClicar = {
                                        aoAbrirCompromisso(compromisso.id)
                                    }
                                )
                            }
                        }

                        TextButton(
                            // Abre o dia selecionado no calendário completo.
                            onClick = {
                                diaAberto?.let { aoAbrirDia(it) }
                            }
                        ) {
                            Text(
                                // Dá acesso também aos registros além dos três da prévia.
                                text = "Abrir este dia na agenda",
                                // Mantém a cor dos links.
                                color = MarromInicio
                            )
                        }
                    }
                }
            }
        }

// Apresenta ocupação, vagas disponíveis e adoções.
@Composable
private fun OcupacaoAbrigoInicio(
    totalAnimais: Int,
    adotados: Int,
    capacidade: Int,
    aoVerDados: () -> Unit
) {
    // Mantém as contagens iguais ou maiores que zero.
    val noAbrigo = totalAnimais.coerceAtLeast(0)
    val totalAdotados = adotados.coerceAtLeast(0)

    // Evita calcular percentual sem uma capacidade válida.
    val temCapacidade = capacidade > 0

    // Calcula a proporção de vagas ocupadas.
    val proporcao = if (temCapacidade) {
        noAbrigo.toFloat() / capacidade
    } else {
        0f
    }

    // Impede a apresentação de vagas negativas.
    val vagas = (capacidade - noAbrigo).coerceAtLeast(0)

    // Formata o percentual com arredondamento.
    val percentual = if (temCapacidade) {
        String.format(
            Locale("pt", "BR"),
            "%.0f%%",
            proporcao * 100
        )
    } else {
        "--"
    }

    // Identifica cada estatística por uma cor.
    val azulVagas = Color(0xFF2684C7)
    val vermelhoAdotados = Color(0xFFC84F5E)
    val verdeAbrigo = Color(0xFF43864A)

    Column(
        // Ocupa a largura disponível.
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            // Distribui título e botão na mesma linha.
            modifier = Modifier.fillMaxWidth(),
            // Alinha os elementos verticalmente.
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                // Identifica o gráfico.
                text = "Nível de ocupação",
                // Reserva espaço para o botão.
                modifier = Modifier.weight(1f),
                // Mantém o padrão de tamanho dos títulos.
                fontSize = 18.sp,
                // Destaca o título.
                fontWeight = FontWeight.Bold,
                // Usa a cor principal do aplicativo.
                color = TextoInicio
            )

            TextButton(
                // Abre os dados cadastrados do abrigo.
                onClick = aoVerDados
            ) {
                Text(
                    // Identifica a navegação.
                    text = "Ver dados",
                    // Mantém o padrão dos links.
                    color = MarromInicio,
                    // Define o tamanho do link.
                    fontSize = 12.sp
                )
            }
        }

        Row(
            // Ocupa a largura da seção.
            modifier = Modifier.fillMaxWidth(),
            // Centraliza o círculo em relação às estatísticas.
            verticalAlignment = Alignment.CenterVertically,
            // Separa o gráfico dos números.
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                // Define o tamanho do círculo.
                modifier = Modifier.size(112.dp),
                // Centraliza os textos dentro dele.
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    // Representa somente a ocupação das vagas.
                    progress = { proporcao.coerceIn(0f, 1f) },
                    // Preenche o espaço reservado.
                    modifier = Modifier.fillMaxSize(),
                    // Destaca em verde as vagas ocupadas.
                    color = verdeAbrigo,
                    // Representa em azul claro a parte disponível.
                    trackColor = azulVagas.copy(alpha = 0.18f),
                    // Define a espessura do círculo.
                    strokeWidth = 10.dp
                )

                Column(
                    // Centraliza os dois textos.
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        // Mostra o percentual de ocupação.
                        text = percentual,
                        // Destaca o percentual.
                        fontSize = 26.sp,
                        // Usa negrito.
                        fontWeight = FontWeight.Bold,
                        // Relaciona o percentual aos animais no abrigo.
                        color = verdeAbrigo
                    )

                    Text(
                        // Explica o significado do percentual.
                        text = "ocupado",
                        // Mantém o texto dentro do círculo.
                        fontSize = 12.sp,
                        // Usa a cor principal.
                        color = TextoInicio
                    )
                }
            }

            Column(
                // Utiliza o espaço restante.
                modifier = Modifier.weight(1f),
                // Separa as três estatísticas.
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EstatisticaAbrigoInicio(
                    // Identifica os espaços ainda disponíveis.
                    titulo = "Vagas disponíveis",
                    // Não inventa vagas sem capacidade cadastrada.
                    valor = if (temCapacidade) vagas.toString() else "--",
                    // Associa vagas à cor azul.
                    cor = azulVagas
                )

                EstatisticaAbrigoInicio(
                    // Identifica as adoções registradas.
                    titulo = "Animais adotados",
                    // Mostra a contagem recebida.
                    valor = totalAdotados.toString(),
                    // Associa adoções à cor vermelha.
                    cor = vermelhoAdotados
                )

                EstatisticaAbrigoInicio(
                    // Identifica os animais atualmente acolhidos.
                    titulo = "Animais no abrigo",
                    // Mostra a quantidade que ocupa vagas.
                    valor = noAbrigo.toString(),
                    // Associa ocupação à cor verde.
                    cor = verdeAbrigo
                )
            }
        }

        if (!temCapacidade) {
            Text(
                // Orienta quando falta cadastrar a capacidade.
                text = "Defina a capacidade em Meu abrigo.",
                // Separa a orientação do gráfico.
                modifier = Modifier.padding(top = 12.dp),
                // Define o tamanho da orientação.
                fontSize = 13.sp,
                // Mantém a cor secundária.
                color = MarromInicio
            )
        }
    }
}

// Padroniza o marcador, o nome e o valor de cada estatística.
@Composable
private fun EstatisticaAbrigoInicio(
    titulo: String,
    valor: String,
    cor: Color
) {
    Row(
        // Alinha o marcador ao centro dos textos.
        verticalAlignment = Alignment.CenterVertically,
        // Separa o marcador das informações.
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            // Desenha um pequeno marcador colorido.
            modifier = Modifier
                .size(10.dp)
                .background(cor, CircleShape)
        )

        Column(
            // Permite acomodar os textos na largura restante.
            modifier = Modifier.weight(1f)
        ) {
            Text(
                // Mostra o nome da estatística.
                text = titulo,
                // Mantém os rótulos compactos e legíveis.
                fontSize = 13.sp,
                // Mantém contraste sem depender apenas da cor.
                color = TextoInicio
            )

            Text(
                // Mostra a quantidade correspondente.
                text = valor,
                // Destaca o número.
                fontSize = 23.sp,
                // Aplica negrito.
                fontWeight = FontWeight.Bold,
                // Repete a cor do marcador.
                color = cor
            )
        }
    }
}
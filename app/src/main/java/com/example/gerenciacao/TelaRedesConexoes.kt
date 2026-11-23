package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define o azul do cabeçalho de Redes e Conexões.
private val AzulCabecalhoRedes = Color(0xFF8AC8FC)
// Define a cor do filtro Todos.
private val AzulEscuroRedes = Color(0xFF285483)
// Define a cor do filtro Suas conexões.
private val MarromEscuroRedes = Color(0xFF493020)

// A lista Todos e a lista Suas conexoes sao dois estados da mesma tela, nao telas duplicadas.
@Composable
fun TelaRedesConexoes(ctx: ContextoAbrigo, abrigos: List<AbrigoRede>, conexoes: Set<String>, aoAbrirAbrigo: (String) -> Unit) {
    var busca by rememberSaveable { mutableStateOf("") }
    var aba by rememberSaveable { mutableStateOf("Todos") }
    var cidade by rememberSaveable { mutableStateOf("Todas") }
    var raio by rememberSaveable { mutableIntStateOf(0) }
    var vagas by rememberSaveable { mutableStateOf(false) }
    var ordem by rememberSaveable { mutableStateOf("Distância") }
    var filtrosAbertos by rememberSaveable { mutableStateOf(false) }
    val quantidadeFiltros = (if (cidade != "Todas") 1 else 0) + (if (raio > 0) 1 else 0) + (if (vagas) 1 else 0)
    val resultados = filtrarRedes(abrigos, conexoes, aba == "Suas conexões", busca, cidade, raio, vagas, ordem)
    EstruturaRedes(ctx, "Redes e Conexões") {
        // A lista e virtualizada para nao montar todos os cartoes ao mesmo tempo.
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TituloPaginaPadrao("Redes e Conexões", ctx.voltar)
                // Separa o título da busca, mantendo o respiro das outras telas.
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    // Mostra o texto digitado na busca.
                    value = busca,
                    // Atualiza os resultados enquanto o usuário digita.
                    onValueChange = { busca = it.take(120) },
                    // Ocupa a largura disponível entre as margens da tela.
                    modifier = Modifier.fillMaxWidth(),
                    // Mantém a pesquisa em uma única linha.
                    singleLine = true,
                    // Mostra a orientação dentro do campo quando ele está vazio.
                    placeholder = { Text("Buscar por nome ou cidade") },
                    // Mantém a lupa na entrada da busca.
                    leadingIcon = {
                        Icon(
                            // Reutiliza o desenho de busca existente.
                            painter = painterResource(R.drawable.inicio_busca),
                            // A lupa é decorativa; o campo já informa sua função.
                            contentDescription = null,
                            // Mantém a lupa proporcional ao campo.
                            modifier = Modifier.size(20.dp),
                            // Usa o marrom da identidade visual.
                            tint = MarromRestante
                        )
                    },
                    // Coloca o único botão de filtros na extremidade da busca.
                    trailingIcon = {
                        IconButton(
                            // Abre o mesmo painel de filtros já existente.
                            onClick = { filtrosAbertos = true },
                            // Mantém uma área confortável para tocar no controle.
                            modifier = Modifier.size(48.dp).semantics {
                                contentDescription = "Abrir filtros de redes; " +
                                        quantidadeFiltros + " filtros ativos"
                            }
                        ) {
                            Box(
                                // Desenha o fundo circular marrom do filtro.
                                modifier = Modifier.size(36.dp)
                                    .background(MarromEscuroRedes, androidx.compose.foundation.shape.CircleShape),
                                // Centraliza os traços dentro do círculo.
                                contentAlignment = Alignment.Center
                            ) {
                                IconeFiltroRedes()
                            }
                        }
                    },
                    // Mantém a busca arredondada como nas telas de Animais e Loja.
                    shape = RoundedCornerShape(28.dp),
                    // Define as cores do campo sem depender do tema padrão.
                    colors = OutlinedTextFieldDefaults.colors(
                        // Destaca o campo quando recebe o foco.
                        focusedBorderColor = MarromRestante,
                        // Mantém a borda discreta quando o campo não está selecionado.
                        unfocusedBorderColor = BordaRestante,
                        // Mantém o fundo branco durante a digitação.
                        focusedContainerColor = Color.White,
                        // Mantém o fundo branco sem foco.
                        unfocusedContainerColor = Color.White,
                        // Usa o marrom para o cursor.
                        cursorColor = MarromRestante
                    )
                )
                // Separa a busca dos filtros de conexão.
                Spacer(Modifier.height(10.dp))
                Row(
                    // Mantém os dois filtros lado a lado.
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "Suas conexões").forEach { opcao ->
                        // Mantém uma cor própria para cada opção.
                        val corAba = if (opcao == "Todos") AzulEscuroRedes else MarromEscuroRedes
                        FilterChip(
                            // Indica qual lista está sendo exibida.
                            selected = aba == opcao,
                            // Alterna entre todos os abrigos e as conexões adicionadas.
                            onClick = { aba = opcao },
                            // Mostra o nome da opção.
                            label = {
                                Text(
                                    // Usa o texto do filtro correspondente.
                                    text = opcao,
                                    // Destaca a opção selecionada pelo peso da fonte.
                                    fontWeight = if (aba == opcao) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            // Mantém o formato de cápsula do protótipo.
                            shape = RoundedCornerShape(24.dp),
                            // Reforça a borda da opção ativa sem trocar sua cor.
                            border = BorderStroke(
                                // Aumenta o contorno da opção selecionada.
                                width = if (aba == opcao) 2.dp else 1.dp,
                                // Mantém o contorno na cor do botão.
                                color = corAba
                            ),
                            // Mantém Todos azul-escuro e Suas conexões marrom.
                            colors = FilterChipDefaults.filterChipColors(
                                // Define o fundo do botão não selecionado.
                                containerColor = corAba,
                                // Mantém o texto legível no fundo escuro.
                                labelColor = Color.White,
                                // Preserva a mesma identidade ao selecionar o botão.
                                selectedContainerColor = corAba,
                                // Mantém o texto branco na opção selecionada.
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            item {
                Text(
                    // Mostra quantos abrigos correspondem à busca e aos filtros.
                    text = resultados.size.toString() + " " +
                            if (resultados.size == 1) "abrigo encontrado" else "abrigos encontrados",
                    // Ocupa a largura da lista.
                    modifier = Modifier.fillMaxWidth(),
                    // Mantém a contagem secundária em relação aos títulos dos abrigos.
                    fontSize = 12.sp,
                    // Usa o marrom dos textos auxiliares.
                    color = MarromRestante
                )
            }
            items(resultados, key = { it.id }) { abrigo ->
                CartaoAbrigoRede(abrigo, abrigo.id in conexoes) { aoAbrirAbrigo(abrigo.id) }
            }
            if (resultados.isEmpty()) item {
                CartaoRestante(CremeRestante) {
                    Text(if (aba == "Suas conexões" && conexoes.isEmpty()) "Você ainda não adicionou uma conexão." else "Nenhum abrigo corresponde à busca e aos filtros.", fontWeight = FontWeight.SemiBold)
                    Text(if (aba == "Suas conexões" && conexoes.isEmpty()) "Abra Todos, escolha um abrigo e toque em Conectar-se." else "Tente outro nome ou limpe os filtros.")
                    TextButton(onClick = {
                        busca = ""; cidade = "Todas"; raio = 0; vagas = false
                        if (conexoes.isEmpty()) aba = "Todos"
                    }) { Text("Limpar busca e filtros", color = MarromRestante) }
                }
            }

        }
    }
    // O painel trabalha em um rascunho; cancelar nao muda os filtros aplicados.
    if (filtrosAbertos) FiltrosConexoesRede(cidade, raio, vagas, ordem, abrigos.map { it.cidade }.distinct().sorted(),
        aoFechar = { filtrosAbertos = false },
        aoAplicar = { novaCidade, novoRaio, novasVagas, novaOrdem ->
            cidade = novaCidade; raio = novoRaio; vagas = novasVagas; ordem = novaOrdem; filtrosAbertos = false
        })
}

// Compartilha o cabecalho e o menu inferior das duas telas de redes.
@Composable
internal fun EstruturaRedes(ctx: ContextoAbrigo, titulo: String, conteudo: @Composable () -> Unit) {
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Mantém o destino de Finanças da navegação inferior.
        LocalAbrirFinancas provides { ctx.principal("financas") },
        // Usa azul apenas no fluxo de Redes e Conexões.
        LocalCorCabecalhoApp provides AzulCabecalhoRedes,
        // Mantém a foto e o menu do usuário, sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false)
    ) {
        MolduraPadraoApp(
            bottomBar = { MenuInferiorInicio(avisar = { aviso = it }, selecionado = "",
                aoInicio = { ctx.principal("inicio") }, aoAnimais = { ctx.principal("animais") },
                aoAdicionar = { ctx.principal("adicionar") }, aoAgenda = { ctx.principal("agenda") }) }
        ) { conteudo() }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra um cartao clicavel por abrigo e identifica as conexoes ja adicionadas.
@Composable
private fun CartaoAbrigoRede(abrigo: AbrigoRede, conectado: Boolean, abrir: () -> Unit) {
    Surface(onClick = abrir, color = Color.White, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BordaRestante)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CapaAbrigoRede(abrigo, Modifier.size(58.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(abrigo.nome, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                Text("${abrigo.bairro} · ${abrigo.cidade}", fontSize = 11.sp, lineHeight = 15.sp, color = MarromRestante)
                Text(distanciaRede(abrigo), fontSize = 11.sp, color = MarromRestante)
                Text(
                    // Mostra a descrição cadastrada para este abrigo.
                    text = abrigo.descricao,
                    // Define o tamanho do texto.
                    fontSize = 12.sp,
                    // Define o espaço entre as linhas.
                    lineHeight = 17.sp,
                    // Limita o resumo a duas linhas.
                    maxLines = 2,
                    // Mostra reticências quando a descrição for maior.
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    // Mantém a cor dos textos do cartão.
                    color = TextoRestante
                )
                if (conectado) Text("Sua conexão", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F7051))
            }
            Spacer(Modifier.width(6.dp))
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = MarromRestante)
        }
    }
}

// Usa a foto opcional do abrigo; sem foto, mostra uma capa de patinha da identidade existente.
@Composable
internal fun CapaAbrigoRede(abrigo: AbrigoRede, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(15.dp)).background(CremeRestante), contentAlignment = Alignment.Center) {
        val foto = abrigo.fotoRecurso
        if (foto != null) Image(painterResource(foto), "Foto de ${abrigo.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(painterResource(R.drawable.inicio_pata), null, Modifier.fillMaxSize().padding(14.dp), tint = MarromRestante)
    }
}

// Desenha três traços claros sobre o círculo marrom do botão de filtros.
@Composable
private fun IconeFiltroRedes() {
    Canvas(
        // Define a área ocupada pelo desenho dentro do botão.
        modifier = Modifier.size(18.dp)
    ) {
        // Reduz a largura de cada traço para formar o símbolo de filtro.
        listOf(0.30f to 0.90f, 0.50f to 0.60f, 0.70f to 0.30f).forEach { (altura, largura) ->
            drawLine(
                // Usa creme para contrastar com o fundo marrom.
                color = Color(0xFFFFEFC5),
                // Centraliza o início do traço horizontal.
                start = Offset(size.width * (1f - largura) / 2f, size.height * altura),
                // Centraliza o final do traço horizontal.
                end = Offset(size.width * (1f + largura) / 2f, size.height * altura),
                // Mantém os traços visíveis no tamanho do controle.
                strokeWidth = 1.6.dp.toPx(),
                // Suaviza as pontas dos traços.
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
// Aplica cidade, distancia de exemplo, vagas e ordenacao apenas ao confirmar o painel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosConexoesRede(cidadeAtual: String, raioAtual: Int, vagasAtuais: Boolean, ordemAtual: String,
                                cidades: List<String>, aoFechar: () -> Unit, aoAplicar: (String, Int, Boolean, String) -> Unit) {
    var cidade by rememberSaveable { mutableStateOf(cidadeAtual) }
    var raio by rememberSaveable { mutableIntStateOf(raioAtual) }
    var vagas by rememberSaveable { mutableStateOf(vagasAtuais) }
    var ordem by rememberSaveable { mutableStateOf(ordemAtual) }
    ModalBottomSheet(onDismissRequest = aoFechar, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = FundoRestante) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Filtrar redes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            SeletorRestante("Cidade", cidade, listOf("Todas" to "Todas") + cidades.map { it to it }) { cidade = it }
            SeletorRestante("Distância ilustrativa", if (raio == 0) "Qualquer distância" else "Até $raio km",
                listOf("0" to "Qualquer distância", "5" to "Até 5 km", "10" to "Até 10 km", "25" to "Até 25 km")) { raio = it.toInt() }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Somente abrigos com vagas", Modifier.weight(1f), fontSize = 14.sp, color = TextoRestante)
                Switch(checked = vagas, onCheckedChange = { vagas = it })
            }
            SeletorRestante("Ordenar por", ordem, listOf("Distância" to "Menor distância de exemplo", "Nome" to "Nome")) { ordem = it }
            Text("As distâncias são exemplos. Não há consulta de GPS nesta etapa.", fontSize = 12.sp, color = MarromRestante)
            BotaoRestante("Aplicar filtros") { aoAplicar(cidade, raio, vagas, ordem) }
            TextButton(onClick = { cidade = "Todas"; raio = 0; vagas = false; ordem = "Distância" }) { Text("Limpar filtros", color = MarromRestante) }
        }
    }
}

// Mostra a primeira tela do fluxo com uma conexao previamente marcada no exemplo.
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewRedesConexoes() {
    MaterialTheme { TelaRedesConexoes(contextoPreviewRestante(), AbrigosRedeExemplo, setOf("cantinho-focinhos"), {}) }
}
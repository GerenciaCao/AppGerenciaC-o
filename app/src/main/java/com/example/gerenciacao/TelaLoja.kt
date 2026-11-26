package com.example.gerenciacao

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.selected
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Define o formato circular dos indicadores do carrossel.
import androidx.compose.foundation.shape.CircleShape

// Conserva o destino Loja que ja e usado pela navegacao do aplicativo.
val LocalAbrirLoja = staticCompositionLocalOf<() -> Unit> { {} }

// EDITE AQUI AS IMAGENS: troque null por R.drawable.nome_da_sua_imagem.
// Salve cada arquivo individual na pasta app/src/main/res/drawable.
private val ImagensIndividuaisLoja: Map<String, Int?> = mapOf(
    "vitrine-demo-racao" to R.drawable.pedigree, // Mostra a embalagem de ração no banner.
    "vitrine-demo-umido" to R.drawable.loja_alimento_umido, // Mostra o sachê no cartão de R$ 5,99.
    "vitrine-demo-petisco" to R.drawable.pedigree, // Sua imagem ja existente.
    "vitrine-demo-banho" to null,        // Ex.: R.drawable.banho_tosa
    "vitrine-demo-spa" to null           // Ex.: R.drawable.spa_bichinhos
)

// Exemplos ilustrativos do molde; seus valores nao representam ofertas comerciais reais.
private val OfertasDemonstrativasLoja = listOf(
    OfertaAbrigo(id = "vitrine-demo-racao", especieAlvo = "Cães", titulo = "Sachê Pedigree Carne", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 6990L,
        descricao = "Para cães adultos de raças pequenas. Embalagem de 100 g."),
    OfertaAbrigo(id = "vitrine-demo-umido", especieAlvo = "Gatos", titulo = "Sachê Whiskas", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 599L,
        descricao = "Para gatos adultos. Embalagem de 85 g."),
    OfertaAbrigo(id = "vitrine-demo-petisco", titulo = "Sachê Pedigree Carne", categoria = "Alimentação", parceiro = "Exemplo de catálogo", centavos = 499L,
        descricao = "Para cães adultos de raças pequenas. Embalagem de 100 g."),
    OfertaAbrigo(id = "vitrine-demo-banho", titulo = "Banho e tosa", categoria = "Serviços", parceiro = "Prestador de exemplo", tipo = "Serviço", centavos = 7999L,
        descricao = "Serviço demonstrativo. Confirme o atendimento, o preço real e as condições com o prestador."),
    OfertaAbrigo(id = "vitrine-demo-spa", titulo = "Spa para bichinhos", categoria = "Serviços", parceiro = "Prestador de exemplo", tipo = "Serviço", centavos = 7999L,
        descricao = "Serviço demonstrativo com imagem ilustrativa. Confirme condições e disponibilidade com o prestador.")
)

// Os detalhes recebem exatamente os dados do cartao, incluindo os exemplos apresentados primeiro.
fun catalogoLoja(ofertasCadastradas: List<OfertaAbrigo>): List<OfertaAbrigo> =
    OfertasDemonstrativasLoja.filter { exemplo -> ofertasCadastradas.none { it.id == exemplo.id } } + ofertasCadastradas

// Preserva a assinatura anterior e combina busca, categoria e tipo.
fun filtrarCatalogoLoja(ofertas: List<OfertaAbrigo>, busca: String, tipo: String, categoria: String): List<OfertaAbrigo> = ofertas.filter {
    (busca.isBlank() || "${it.titulo} ${it.parceiro} ${it.categoria}".contains(busca.trim(), true)) &&
            (tipo == "Todos" || if (tipo == "Serviços") it.tipo == "Serviço" else it.tipo != "Serviço") &&
            (categoria == "Todas" || it.categoria == categoria)
}

// Aplica os filtros do painel sem misturar valores ausentes com precos iguais a zero.
fun aplicarFiltrosPrecoLoja(ofertas: List<OfertaAbrigo>, parceiro: String, minimo: Long?, maximo: Long?, ordem: String): List<OfertaAbrigo> {
    val resultado = ofertas.filter {
        (parceiro == "Todos" || it.parceiro == parceiro) &&
                (minimo == null || it.centavos?.let { valor -> valor >= minimo } == true) &&
                (maximo == null || it.centavos?.let { valor -> valor <= maximo } == true)
    }
    return when (ordem) {
        "Menor preço" -> resultado.sortedBy { it.centavos ?: Long.MAX_VALUE }
        "Maior preço" -> resultado.sortedByDescending { it.centavos ?: Long.MIN_VALUE }
        else -> resultado
    }
}

// Paleta da proposta de marketing, aplicada apenas ao conteúdo da loja.
private val BrancoLoja = Color(0xFFFFFCF7)
private val AzulLoja = Color(0xFF8EB8E8)
private val LaranjaLoja = Color(0xFFFF9F43)
private val TextoLoja = Color(0xFF41201F)

// Mantém a assinatura e os destinos que a navegação já utiliza.
@Composable
fun TelaLoja(ctx: ContextoAbrigo) {
    // Guarda a categoria de serviço aberta ao tocar em um banner.
    var servicoSelecionado by rememberSaveable { mutableStateOf<String?>(null) }
    // Guarda o título do produto para apresentar o aviso de redirecionamento.
    var anuncioExterno by rememberSaveable { mutableStateOf<String?>(null) }
    var busca by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("Produtos") }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    // Guarda o público escolhido pelo banner; vazio aceita todos os públicos.
    var especieProduto by rememberSaveable { mutableStateOf("") }
    var parceiro by rememberSaveable { mutableStateOf("Todos") }
    var minimo by rememberSaveable { mutableStateOf<Long?>(null) }
    var maximo by rememberSaveable { mutableStateOf<Long?>(null) }
    var ordem by rememberSaveable { mutableStateOf("Destaques") }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Consulta o tamanho da fonte no contexto Compose antes de montar a lista.
    val colunas = if (LocalDensity.current.fontScale > 1.2f) 1 else 2
    val catalogo = catalogoLoja(ctx.dados.ofertas)
    // Combina busca, categoria, público, parceiro e preço.
    val visiveis = aplicarFiltrosPrecoLoja(
        filtrarCatalogoLoja(catalogo, busca, tipo, categoria)
            .filter { oferta ->
                especieProduto.isBlank() ||
                        oferta.especieAlvo == especieProduto ||
                        oferta.especieAlvo == "Ambos"
            },
        parceiro, minimo, maximo, ordem
    )

    val filtrosAtivos = categoria != "Todas" ||
            especieProduto.isNotBlank() ||
            parceiro != "Todos" ||
            minimo != null ||
            maximo != null ||
            ordem != "Destaques"

// Apresenta os resultados sem o carrossel ao buscar ou aplicar filtros.
    val exibindoResultados = busca.isNotBlank() || filtrosAtivos

// Retorna à vitrine inicial e remove os filtros da busca.
    val voltarParaVitrine: () -> Unit = {
        busca = ""
        categoria = "Todas"
        especieProduto = ""
        parceiro = "Todos"
        minimo = null
        maximo = null
        ordem = "Destaques"
        servicoSelecionado = null
    }

    val abrir: (OfertaAbrigo) -> Unit = { oferta ->
        if (oferta.tipo == "Serviço") {
            // Mantém a navegação atual dos serviços.
            ctx.atualizar(ctx.dados.copy(ofertaId = oferta.id))
            ctx.abrir("oferta_parceiro")
        } else {
            // Apresenta o aviso ao tocar em um anúncio de produto.
            anuncioExterno = oferta.titulo
        }
    }

    if (tipo == "Serviços" && servicoSelecionado == "Clínicas veterinárias") {
        val clinicas = catalogo.filter {
            val texto = (it.titulo + " " + it.categoria).lowercase(java.util.Locale.ROOT)
            it.tipo == "Serviço" && (
                    texto.contains("veterin") ||
                            texto.contains("clínic") ||
                            texto.contains("clinic")
                    )
        }.map { oferta ->
            EstabelecimentoClinicaVeterinaria(
                id = oferta.id,
                nome = oferta.parceiro,
                fotoUri = oferta.imagemUri
            )
        }
        TelaClinicasVeterinarias(
            estabelecimentos = clinicas,
            aoVoltar = { servicoSelecionado = null; busca = "" },
            aoInicio = { ctx.principal("inicio") },
            aoAnimais = { ctx.principal("animais") },
            aoAdicionar = { ctx.principal("adicionar") },
            aoAgenda = { ctx.principal("agenda") },
            aoAbrirServico = { id ->
                catalogo.firstOrNull { it.id == id }?.let { abrir(it) }
            }
        )
        return
    }

    if (tipo == "Serviços" && servicoSelecionado == "Banho e tosa") {
        val estabelecimentos = catalogo.filter {
            val texto = (it.titulo + " " + it.categoria).lowercase(java.util.Locale.ROOT)
            it.tipo == "Serviço" && (texto.contains("banho") || texto.contains("tosa"))
        }.map { oferta ->
            EstabelecimentoBanhoTosa(
                id = oferta.id,
                nome = oferta.parceiro,
                fotoUri = oferta.imagemUri
            )
        }
        TelaBanhoTosa(
            estabelecimentos = estabelecimentos,
            aoVoltar = { servicoSelecionado = null; busca = "" },
            aoInicio = { ctx.principal("inicio") },
            aoAnimais = { ctx.principal("animais") },
            aoAdicionar = { ctx.principal("adicionar") },
            aoAgenda = { ctx.principal("agenda") },
            aoAbrirServico = { id ->
                catalogo.firstOrNull { it.id == id }?.let { abrir(it) }
            }
        )
        return
    }

    // Voltar retorna às categorias antes de sair da loja.
    BackHandler {
        when {
            tipo == "Serviços" && servicoSelecionado != null -> {
                servicoSelecionado = null
                busca = ""
            }

            // Voltar sai dos resultados antes de sair da loja.
            exibindoResultados -> voltarParaVitrine()

            else -> ctx.principal("inicio")
        }
    }

    // Usa exatamente o cabeçalho e a navegação existentes no aplicativo.
    // A loja não precisa do botão flutuante de criar registros.
    MolduraPadraoApp(
        containerColor = BrancoLoja,
        floatingActionButton = {},
        bottomBar = {
            MenuInferiorInicio(
                avisar = { aviso = it }, selecionado = "Loja",
                aoInicio = { ctx.principal("inicio") },
                aoAnimais = { ctx.principal("animais") },
                aoAdicionar = { ctx.principal("adicionar") },
                aoAgenda = { ctx.principal("agenda") }
            )
        }
    ) { espacos ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(espacos).imePadding(),
            contentPadding = PaddingValues(
                // Repete as margens da lista de Animais.
                start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Repete tamanho, peso e altura do título de Animais.
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        // Nome apresentado na página da Loja.
                        text = "Parcerias",
                        // Ocupa a largura disponível na linha.
                        modifier = Modifier.weight(1f),
                        // Mesmo tamanho do título Animais.
                        fontSize = 30.sp,
                        // Mesma altura de linha de Animais.
                        lineHeight = 34.sp,
                        // Mesmo peso tipográfico de Animais.
                        fontWeight = FontWeight.Bold,
                        // Mesmo tom usado nos textos principais.
                        color = TextoPadraoApp
                    )
                }
                // Repete a distância entre título e busca da tela de Animais.
                Spacer(Modifier.height(24.dp))
                // Mantém busca e filtros funcionais sobre o catálogo atual.
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Buscar produtos ou serviços", fontSize = 14.sp, color = Color(0xFF7E7066))
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.inicio_busca), null,
                            Modifier.size(20.dp), tint = TextoLoja
                        )
                    },
                    // Usa o mesmo botão circular marrom da tela de Animais.
                    trailingIcon = {
                        FilledIconButton(
                            // Mantém o painel de filtros da loja já existente.
                            onClick = { mostrarFiltros = true },
                            modifier = Modifier
                                // Define o diâmetro visual do botão.
                                .size(38.dp)
                                .semantics {
                                    // Descreve a ação para leitores de tela.
                                    contentDescription = "Filtrar ofertas"
                                },
                            // Deixa o botão circular.
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                // Usa o mesmo marrom do filtro de Animais.
                                containerColor = Color(0xFF783E19)
                            )
                        ) {
                            Canvas(
                                // Define o tamanho das três linhas do filtro.
                                modifier = Modifier.size(18.dp)
                            ) {
                                // Define a cor clara das linhas sobre o botão marrom.
                                val cor = Color(0xFFFFD993)
                                // Define a espessura das linhas.
                                val espessura = 1.5.dp.toPx()
                                // Desenha linhas progressivamente menores.
                                listOf(0.25f to 0.8f, 0.5f to 0.5f, 0.75f to 0.2f)
                                    .forEach { (y, largura) ->
                                        drawLine(
                                            // Aplica a cor das linhas.
                                            color = cor,
                                            // Centraliza o início da linha.
                                            start = Offset(size.width * (1f - largura) / 2, size.height * y),
                                            // Centraliza o fim da linha.
                                            end = Offset(size.width * (1f + largura) / 2, size.height * y),
                                            // Aplica a espessura definida acima.
                                            strokeWidth = espessura
                                        )
                                    }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = LaranjaLoja,
                        unfocusedBorderColor = Color(0xFFEADFD2)
                    )
                )
                Spacer(Modifier.height(12.dp))

                // Alterna produtos e serviços sem misturar ações diferentes.
                Row(Modifier.fillMaxWidth()) {
                    listOf("Produtos", "Serviços").forEach { opcao ->
                        Column(Modifier.weight(1f)) {
                            TextButton(
                                onClick = {
                                    // Remove os filtros da aba anterior.
                                    voltarParaVitrine()
                                    tipo = opcao
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    opcao, color = TextoLoja, fontSize = 16.sp,
                                    fontWeight = if (tipo == opcao) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Box(
                                Modifier.fillMaxWidth().height(3.dp)
                                    .background(
                                        if (tipo == opcao) LaranjaLoja
                                        else TextoLoja.copy(alpha = 0.12f)
                                    )
                            )
                        }
                    }
                }
                if (exibindoResultados) {
                    TextButton(onClick = voltarParaVitrine) {
                        Text(
                            text = "← Voltar para a vitrine",
                            color = TextoLoja,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Identifica os filtros definidos pelo banner.
                    val resumoFiltro = listOfNotNull(
                        categoria.takeIf { it != "Todas" },
                        especieProduto.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")

                    if (resumoFiltro.isNotBlank()) {
                        Surface(
                            color = Color(0xFFE7F4FE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Filtro: $resumoFiltro",
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF285484)
                            )
                        }
                    }
                }
            }

            // A aba Serviços troca os produtos por banners das categorias.
            if (tipo == "Serviços") {
                val categoriaAberta = servicoSelecionado
                if (categoriaAberta == null) {
                    // Usa somente as duas imagens já adicionadas no drawable.
                    val categoriasServicos = listOf(
                        "Banho e tosa" to R.drawable.bannerbanhoetosa,
                        "Clínicas veterinárias" to R.drawable.bannerclinicas
                    ).filter { busca.isBlank() || it.first.contains(busca.trim(), ignoreCase = true) }

                    items(categoriasServicos, key = { it.first }) { (nome, imagem) ->
                        BannerCategoriaServicoLoja(
                            titulo = nome,
                            imagem = imagem,
                            aoClicar = {
                                servicoSelecionado = nome
                                busca = ""
                            }
                        )
                    }
                    if (categoriasServicos.isEmpty()) {
                        item { Text("Nenhuma categoria encontrada.", color = TextoLoja) }
                    }
                } else {
                    item {
                        // Volta às categorias sem sair da aba Serviços.
                        TextButton(onClick = { servicoSelecionado = null; busca = "" }) {
                            Text("‹ Todas as categorias", color = TextoLoja)
                        }
                        Text(
                            categoriaAberta,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoLoja
                        )
                        Spacer(Modifier.height(8.dp))
                        // Não informa proximidade sem localização dos estabelecimentos.
                        Text(
                            "Serviços cadastrados. Localização e distância ainda não disponíveis.",
                            fontSize = 12.sp, color = TextoLoja
                        )
                    }

                    // Classifica os serviços pelo título e pela categoria já cadastrados.
                    val ofertasDaCategoria = visiveis.filter { oferta ->
                        val texto = (oferta.titulo + " " + oferta.categoria)
                            .lowercase(java.util.Locale.ROOT)
                        when (categoriaAberta) {
                            "Banho e tosa" -> texto.contains("banho") || texto.contains("tosa")
                            else -> texto.contains("veterin") || texto.contains("clínic") || texto.contains("clinic")
                        }
                    }
                    items(ofertasDaCategoria, key = { it.id }) { oferta ->
                        Surface(
                            onClick = { abrir(oferta) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, TextoLoja.copy(alpha = 0.2f))
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        oferta.parceiro,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoLoja
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(oferta.titulo, fontSize = 14.sp, color = TextoLoja)
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        oferta.centavos?.let(::reaisAbrigo) ?: "Consultar valor",
                                        fontSize = 14.sp, color = TextoLoja
                                    )
                                }
                                Text("›", fontSize = 26.sp, color = TextoLoja)
                            }
                        }
                    }
                    if (ofertasDaCategoria.isEmpty()) {
                        item {
                            Text(
                                "Ainda não há serviços cadastrados nesta categoria.",
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = TextoLoja
                            )
                        }
                    }
                }
            } else {
                // O carrossel aparece somente na vitrine inicial.
                if (!exibindoResultados) {
                    item {
                        CarrosselBannersParcerias(
                            aoAbrirBusca = { banner ->
                                // Começa uma busca nova com os filtros deste banner.
                                voltarParaVitrine()
                                tipo = "Produtos"
                                categoria = banner.categoria
                                especieProduto = banner.especie
                            }
                        )
                    }
                }

                item {
                    Text(
                        text = if (exibindoResultados) {
                            "Resultados da busca"
                        } else {
                            "Produtos em destaque"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoLoja
                    )
                }

                // Organiza dois cartões por linha e acompanha a largura disponível.
                // Com fonte ampliada, usa uma coluna para preservar a leitura.

                items(visiveis.chunked(colunas), key = { linha -> linha.first().id }) { linha ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        linha.forEach { oferta ->
                            CartaoProdutoNovaLoja(
                                // Recebe os dados do anúncio desta posição.
                                oferta = oferta,
                                // Mantém dois cartões por linha quando houver espaço.
                                modifier = Modifier.weight(1f),
                                // Abre os detalhes do anúncio ao tocar no cartão.
                                abrir = { abrir(oferta) }
                            )
                        }
                        if (linha.size < colunas) Spacer(Modifier.weight(1f))
                    }
                }
                if (visiveis.isEmpty()) {
                    item { Text("Nenhuma oferta encontrada.", color = TextoLoja) }
                }



            }

        }
    }

    // Reaproveita o painel de filtros existente com preço, parceiro e categoria.
    if (mostrarFiltros) {
        PainelFiltrosModeloLoja(
            catalogo, categoria, parceiro, minimo, maximo, ordem,
            fechar = { mostrarFiltros = false },
            aplicar = { c, p, min, max, o ->
                categoria = c; parceiro = p; minimo = min; maximo = max
                ordem = o; mostrarFiltros = false
            }
        )
    }

    // Mostra o destino previsto sem depender de internet na apresentação.
    anuncioExterno?.let { tituloProduto ->
        AlertDialog(
            // Permite fechar ao tocar fora do aviso ou usar o botão Voltar.
            onDismissRequest = { anuncioExterno = null },
            // Mantém o fundo branco e as bordas arredondadas.
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Você vai para o site do parceiro",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoLoja
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        // Identifica o anúncio selecionado.
                        text = tituloProduto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextoLoja
                    )
                    Text(
                        text = "Este anúncio leva à página de compra indicada pelo parceiro.",
                        fontSize = 16.sp,
                        lineHeight = 23.sp,
                        color = TextoLoja
                    )
                    Surface(
                        // Destaca a explicação sobre o funcionamento da demonstração.
                        color = Color(0xFFE7F4FE),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Nesta demonstração sem internet, o link externo não será aberto.",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF941913)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    // Fecha o aviso e permanece na loja.
                    onClick = { anuncioExterno = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF432818),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Entendi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Mostra os itens adicionados; não simula um checkout que ainda não existe.

    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra foto, informações, preço e ação nessa ordem.
// A imagem permanece livre de botões sobrepostos.
// Mostra somente foto, título breve e preço; tocar abre os detalhes do anúncio.
@Composable
private fun CartaoProdutoNovaLoja(
    // Recebe os dados reais ou demonstrativos do anúncio.
    oferta: OfertaAbrigo,
    // Permite dimensionar o cartão dentro da linha.
    modifier: Modifier = Modifier,
    // Abre a página de detalhes já existente.
    abrir: () -> Unit
) {
    // Separa os dados de embalagem do título para manter o anúncio breve.
    val resumo = when (oferta.id) {
        // Identifica os dois exemplos que usam a mesma embalagem Pedigree.
        "vitrine-demo-racao", "vitrine-demo-petisco" -> "Cães adultos · 100 g"
        // Identifica o exemplo que usa a embalagem Whiskas.
        "vitrine-demo-umido" -> "Gatos adultos · 85 g"
        // Não inventa dados de anúncios sem embalagem identificada.
        else -> ""
    }
    Surface(
        // Torna o cartão inteiro clicável.
        onClick = abrir,
        // Mantém o tamanho definido pela lista.
        modifier = modifier,
        // Preserva as bordas arredondadas.
        shape = RoundedCornerShape(18.dp),
        // Mantém a cor clara do cartão atual.
        color = Color(0xFFFFF8F4),
        // Mantém a borda discreta.
        border = BorderStroke(1.dp, TextoLoja.copy(alpha = 0.2f))
    ) {
        Column {
            FotoModeloLoja(
                // Mantém a imagem que já estava vinculada ao produto.
                oferta = oferta,
                // Preserva a altura atual da fotografia.
                modifier = Modifier.fillMaxWidth().height(145.dp)
            )
            Column(
                // Mantém espaço interno ao redor do título e do preço.
                modifier = Modifier.padding(10.dp),
                // Separa o preço do nome do produto.
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    // Mostra somente o tipo de produto e a marca.
                    text = oferta.titulo,
                    // Mantém o nome legível dentro das duas colunas.
                    fontSize = 14.sp,
                    // Define o espaço entre as linhas do título.
                    lineHeight = 18.sp,
                    // Reserva duas linhas para manter os títulos alinhados.
                    minLines = 1,
                    // Limita o tamanho do título na vitrine.
                    maxLines = 2,
                    // Mantém o nome completo disponível nos detalhes.
                    overflow = TextOverflow.Ellipsis,
                    // Destaca o nome do anúncio.
                    fontWeight = FontWeight.SemiBold,
                    // Preserva o marrom dos textos.
                    color = TextoLoja
                )
                if (resumo.isNotBlank()) {
                    Text(
                        // Mostra público e peso em uma única linha auxiliar.
                        text = resumo,
                        // Mantém a descrição menor que o título.
                        fontSize = 12.sp,
                        // Define uma altura confortável para a linha.
                        lineHeight = 16.sp,
                        // Limita o resumo a uma linha.
                        maxLines = 1,
                        // Evita que o texto ultrapasse o cartão.
                        overflow = TextOverflow.Ellipsis,
                        // Usa a cor auxiliar da identidade do aplicativo.
                        color = MarromRestante
                    )
                }
                Text(
                    // Mantém o valor cadastrado ou a orientação para consultar.
                    text = oferta.centavos?.let(::reaisAbrigo) ?: "Consultar valor",
                    // Destaca o preço.
                    fontSize = 19.sp,
                    // Usa negrito no valor.
                    fontWeight = FontWeight.Bold,
                    // Aplica laranja escuro para manter leitura no fundo claro.
                    color = Color(0xFFB94E00)
                )
                Text(
                    // Explica a ação de tocar no cartão do anúncio.
                    text = "Ver no site do parceiro ↗",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF285484)
                )
            }
        }
    }
}
// Exibe somente a imagem individual escolhida para este item, sempre inteira e sem recortar.
@Composable
private fun FotoModeloLoja(oferta: OfertaAbrigo, modifier: Modifier, banner: Boolean = false) {
    val fotoCadastrada = fotoMiniaturaAdocao(oferta.imagemUri)
    val recursoIndividual = ImagensIndividuaisLoja[oferta.id]
    val espacoImagem = if (banner) 4.dp else 6.dp
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        when {
            // Imagem escolhida no formulario de anuncio, quando existir.
            fotoCadastrada != null -> Image(fotoCadastrada, oferta.titulo,
                Modifier.fillMaxSize().padding(espacoImagem), contentScale = ContentScale.Fit)
            // Imagem que voce vinculou manualmente na tabela acima.
            recursoIndividual != null -> Image(painterResource(recursoIndividual), oferta.titulo,
                Modifier.fillMaxSize().padding(espacoImagem), contentScale = ContentScale.Fit)
            // Mantem o espaco do cartao enquanto a imagem individual ainda nao foi adicionada.
            else -> Column(Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(painterResource(R.drawable.inicio_pata), null, Modifier.size(25.dp), tint = MarromRestante.copy(alpha = 0.45f))
                Spacer(Modifier.height(7.dp))
                Text(if (oferta.tipo == "Serviço") "Imagem do serviço" else "Imagem do produto",
                    fontSize = 10.sp, color = MarromRestante, textAlign = TextAlign.Center)
            }
        }
    }
}

// Trabalha com valores de rascunho; somente Aplicar altera a lista e os precos ausentes nao passam no intervalo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PainelFiltrosModeloLoja(catalogo: List<OfertaAbrigo>, categoriaAtual: String, parceiroAtual: String,
                                    minimoAtual: Long?, maximoAtual: Long?, ordemAtual: String, fechar: () -> Unit,
                                    aplicar: (String, String, Long?, Long?, String) -> Unit) {
    var categoria by rememberSaveable { mutableStateOf(categoriaAtual) }
    var parceiro by rememberSaveable { mutableStateOf(parceiroAtual) }
    var minTexto by rememberSaveable { mutableStateOf(minimoAtual?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
    var maxTexto by rememberSaveable { mutableStateOf(maximoAtual?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
    var ordem by rememberSaveable { mutableStateOf(ordemAtual) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = fechar, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = FundoRestante) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 22.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Filtrar ofertas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            SeletorRestante("Categoria", categoria, listOf("Todas" to "Todas") + catalogo.map { it.categoria }.distinct().sorted().map { it to it }) { categoria = it }
            SeletorRestante("Parceiro", parceiro, listOf("Todos" to "Todos") + catalogo.map { it.parceiro }.distinct().sorted().map { it to it }) { parceiro = it }
            CampoRestante("Preço mínimo · opcional", minTexto, { minTexto = it }, dica = "0,00")
            CampoRestante("Preço máximo · opcional", maxTexto, { maxTexto = it }, dica = "100,00")
            SeletorRestante("Ordenar", ordem, listOf("Destaques", "Menor preço", "Maior preço").map { it to it }) { ordem = it }
            erro?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            BotaoRestante("Aplicar filtros") {
                val min = if (minTexto.trim() in listOf("0", "0,0", "0,00")) 0L else centavosAbrigo(minTexto)
                val max = if (maxTexto.trim() in listOf("0", "0,0", "0,00")) 0L else centavosAbrigo(maxTexto)
                erro = when {
                    minTexto.isNotBlank() && min == null -> "Informe um preço mínimo válido, sem ponto de milhar."
                    maxTexto.isNotBlank() && max == null -> "Informe um preço máximo válido, sem ponto de milhar."
                    min != null && max != null && min > max -> "O preço mínimo não pode ser maior que o máximo."
                    else -> null
                }
                if (erro == null) aplicar(categoria, parceiro, min, max, ordem)
            }
            TextButton(onClick = { categoria = "Todas"; parceiro = "Todos"; minTexto = ""; maxTexto = ""; ordem = "Destaques"; erro = null }) { Text("Limpar filtros") }
        }
    }
}

// Previa da composicao baseada no molde enviado.
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewTelaLoja() { MaterialTheme { TelaLoja(contextoPreviewRestante()) } }
// Apresenta o banner inteiro, respeitando a proporção original da imagem.
// O container completo pode ser tocado para abrir a categoria.
@Composable
private fun BannerCategoriaServicoLoja(
    titulo: String,
    imagem: Int,
    aoClicar: () -> Unit
) {
    val pintura = painterResource(imagem)
    val tamanho = pintura.intrinsicSize
    val proporcao = if (
        tamanho.width.isFinite() && tamanho.height.isFinite() &&
        tamanho.width > 0f && tamanho.height > 0f
    ) tamanho.width / tamanho.height else 3f

    Surface(
        onClick = aoClicar,
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "Abrir " + titulo
        },
        shape = RoundedCornerShape(14.dp),
        color = AzulLoja
    ) {
        Image(
            painter = pintura,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(proporcao),
            contentScale = ContentScale.Fit
        )
    }
}


// Associa cada imagem do carrossel ao seu texto acessível.
// Exporte cada banner separadamente, sem as guias de alinhamento do Figma.
// Associa cada banner à imagem e aos filtros dos produtos.
private data class BannerCarrosselParceria(
    val imagem: Int,
    val descricao: String,
    val categoria: String,
    // Vazio permite mostrar produtos de qualquer público.
    val especie: String = ""
)

private val BannersCarrosselParcerias = listOf(
    BannerCarrosselParceria(
        imagem = R.drawable.banner_farmacia_caes,
        descricao = "Farmácia para cães",
        categoria = "Saúde",
        especie = "Cães"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_alimentos_gatos,
        descricao = "Alimentos para gatos",
        categoria = "Alimentação",
        especie = "Gatos"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_brinquedos_caes,
        descricao = "Brinquedos para cães",
        categoria = "Brinquedos",
        especie = "Cães"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_higiene_animal,
        descricao = "Higiene animal",
        categoria = "Higiene"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_brinquedos_gatos,
        descricao = "Brinquedos para gatos",
        categoria = "Brinquedos",
        especie = "Gatos"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_alimentos_caes,
        descricao = "Alimentos para cães",
        categoria = "Alimentação",
        especie = "Cães"
    ),
    BannerCarrosselParceria(
        imagem = R.drawable.banner_farmacia_gatos,
        descricao = "Farmácia para gatos",
        categoria = "Saúde",
        especie = "Gatos"
    )
)

// Permite deslizar os banners e escolher uma página pelos indicadores.
// Alterna os banners automaticamente e também permite arrastar.
@Composable
private fun CarrosselBannersParcerias(
    aoAbrirBusca: (BannerCarrosselParceria) -> Unit
) {
    // Mantém a quantidade real de banners usada pelos pontos e pelas imagens.
    val quantidade = BannersCarrosselParcerias.size
    // Começa no meio da sequência, alinhado ao primeiro banner real.
    val primeiraPagina = if (quantidade > 1) {
        val meio = Int.MAX_VALUE / 2
        meio - meio % quantidade
    } else 0
    // Usa uma identidade nova para não restaurar a posição do carrossel antigo.
    val paginas = key("circular-loja", quantidade) {
        rememberPagerState(
            // Permite mostrar o banner anterior já na primeira abertura.
            initialPage = primeiraPagina,
            // Repete a sequência sem saltar do último ao primeiro na navegação normal.
            pageCount = { if (quantidade > 1) Int.MAX_VALUE else 1 }
        )
    }
    val escopo = rememberCoroutineScope()
    // Detecta arraste para não disputar a navegação com a pessoa.
    val arrastando by paginas.interactionSource.collectIsDraggedAsState()
    // Mantém a prévia estática para facilitar a edição.
    val modoPrevia = LocalInspectionMode.current

    // Troca a cada três segundos, como solicitado para o Início.
    LaunchedEffect(paginas, arrastando, modoPrevia) {
        if (!modoPrevia && !arrastando && BannersCarrosselParcerias.size > 1) {
            while (true) {
                // Intervalo entre as trocas automáticas.
                delay(2_500)
                if (!paginas.isScrollInProgress) {
                    paginas.animateScrollToPage(
                        // Avança mantendo a sequência circular dos banners.
                        page = paginas.currentPage + 1,
                        // Define a duração do movimento.
                        animationSpec = tween(durationMillis = 400)
                    )
                }
            }
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HorizontalPager(
            // Reserva o mesmo espaço para o banner anterior e para o próximo.
            contentPadding = PaddingValues(horizontal = if (quantidade > 1) 20.dp else 0.dp),
            state = paginas,
            modifier = Modifier.fillMaxWidth(),
            // Deixa somente uma pequena faixa dos banners vizinhos visível.
            pageSpacing = 12.dp,
            // Identifica cada posição repetida sem duplicar as chaves das imagens.
            key = { it }
        ) { paginaVirtual ->
            // Converte a posição repetida no índice de uma imagem já existente.
            val indice = paginaVirtual % quantidade
            val banner = BannersCarrosselParcerias[indice]
            val pintura = painterResource(banner.imagem)
            val tamanho = pintura.intrinsicSize
            // Respeita as proporções do arquivo sem cortar o texto do banner.
            val proporcao = if (
                tamanho.width.isFinite() && tamanho.height.isFinite() &&
                tamanho.width > 0f && tamanho.height > 0f
            ) tamanho.width / tamanho.height else 2.45f

            Image(
                painter = pintura,
                contentDescription = banner.descricao,
                modifier = Modifier.fillMaxWidth()
                    .aspectRatio(proporcao)
                    .clip(RoundedCornerShape(16.dp))
                    // Abre os resultados correspondentes ao banner tocado.
                    .clickable(
                        onClickLabel = "Ver " + banner.descricao
                    ) {
                        aoAbrirBusca(banner)
                    },
                contentScale = ContentScale.Fit
            )
        }

        // Os indicadores têm área de toque maior que o círculo visível.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BannersCarrosselParcerias.forEachIndexed { indice, banner ->
                // Mantém apenas um ponto para cada banner real.
                val ativo = paginas.currentPage % quantidade == indice
                Box(
                    modifier = Modifier.size(40.dp).semantics {
                        contentDescription = "Mostrar banner " + (indice + 1) + ": " + banner.descricao
                        selected = ativo
                    }.clickable {
                        escopo.launch {
                            // Abre a cópia próxima do banner escolhido, sem voltar ao começo da sequência.
                            val destino = paginas.currentPage - (paginas.currentPage % quantidade) + indice
                            paginas.animateScrollToPage(destino)
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier.size(if (ativo) 9.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (ativo) TextoLoja
                                else TextoLoja.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}
package com.example.gerenciacao

import androidx.activity.compose.BackHandler
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
    OfertaAbrigo(id = "vitrine-demo-racao", titulo = "Ração Premium", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 6990L,
        descricao = "Produto demonstrativo com valor ilustrativo. Consulte embalagem, composição, disponibilidade e preço real com o parceiro."),
    OfertaAbrigo(id = "vitrine-demo-umido", titulo = "Alimento úmido", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 599L,
        descricao = "Imagem ilustrativa de alimento úmido. Este preço é apenas um exemplo para visualizar a vitrine."),
    OfertaAbrigo(id = "vitrine-demo-petisco", titulo = "Alimento úmido Pedigree", categoria = "Alimentação", parceiro = "Exemplo de catálogo", centavos = 499L,
        descricao = "Usa a imagem Pedigree adicionada ao projeto. O preço é ilustrativo, não uma oferta real da marca ou de um varejista."),
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
private val TextoLoja = Color(0xFF493020)

// Mantém a assinatura e os destinos que a navegação já utiliza.
@Composable
fun TelaLoja(ctx: ContextoAbrigo) {
    // Guarda a categoria de serviço aberta ao tocar em um banner.
    var servicoSelecionado by rememberSaveable { mutableStateOf<String?>(null) }
    var busca by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("Produtos") }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    var parceiro by rememberSaveable { mutableStateOf("Todos") }
    var minimo by rememberSaveable { mutableStateOf<Long?>(null) }
    var maximo by rememberSaveable { mutableStateOf<Long?>(null) }
    var ordem by rememberSaveable { mutableStateOf("Destaques") }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }
    var mostrarCarrinho by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Carrinho de demonstração desta tela: cada ID representa uma embalagem.
    // Não realiza compra, pagamento, entrega ou reserva de estoque.
    var carrinho by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    // Consulta o tamanho da fonte no contexto Compose antes de montar a lista.
    val colunas = if (LocalDensity.current.fontScale > 1.2f) 1 else 2
    val catalogo = catalogoLoja(ctx.dados.ofertas)
    val visiveis = aplicarFiltrosPrecoLoja(
        filtrarCatalogoLoja(catalogo, busca, tipo, categoria),
        parceiro, minimo, maximo, ordem
    )
    val filtrosAtivos = categoria != "Todas" || parceiro != "Todos" ||
            minimo != null || maximo != null || ordem != "Destaques"

    val abrir: (OfertaAbrigo) -> Unit = { oferta ->
        ctx.atualizar(ctx.dados.copy(ofertaId = oferta.id))
        ctx.abrir("oferta_parceiro")
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
        if (tipo == "Serviços" && servicoSelecionado != null) {
            servicoSelecionado = null
            busca = ""
        } else {
            ctx.principal("inicio")
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Alinha o título à esquerda e o carrinho à direita.
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Parcerias", Modifier.weight(1f), color = TextoLoja,
                        fontSize = 26.sp, fontWeight = FontWeight.Bold
                    )
                    BadgedBox(
                        badge = {
                            if (carrinho.isNotEmpty()) {
                                Badge(containerColor = LaranjaLoja, contentColor = TextoLoja) {
                                    Text(carrinho.size.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { mostrarCarrinho = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Abrir carrinho demonstrativo"
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.inicio_loja), null,
                                Modifier.size(25.dp), tint = TextoLoja
                            )
                        }
                    }
                }

                // Mantém busca e filtros funcionais sobre o catálogo atual.
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Buscar produtos ou serviços", fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.inicio_busca), null,
                            Modifier.size(20.dp), tint = TextoLoja
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { mostrarFiltros = true },
                            modifier = Modifier.semantics {
                                contentDescription = "Filtrar ofertas"
                            }
                        ) {
                            Canvas(Modifier.size(22.dp)) {
                                listOf(0.25f to 0.85f, 0.5f to 0.6f, 0.75f to 0.35f)
                                    .forEach { (y, largura) ->
                                        drawLine(
                                            TextoLoja,
                                            Offset(size.width * (1 - largura) / 2, size.height * y),
                                            Offset(size.width * (1 + largura) / 2, size.height * y),
                                            2.dp.toPx()
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
                        unfocusedBorderColor = TextoLoja.copy(alpha = 0.2f)
                    )
                )
                Spacer(Modifier.height(12.dp))

                // Alterna produtos e serviços sem misturar ações diferentes.
                Row(Modifier.fillMaxWidth()) {
                    listOf("Produtos", "Serviços").forEach { opcao ->
                        Column(Modifier.weight(1f)) {
                            TextButton(
                                onClick = { tipo = opcao; categoria = "Todas"; servicoSelecionado = null; busca = "" },
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
                if (filtrosAtivos) {
                    TextButton(onClick = {
                        categoria = "Todas"; parceiro = "Todos"
                        minimo = null; maximo = null; ordem = "Destaques"
                    }) {
                        Text("Limpar filtros", color = TextoLoja)
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
                // Banner único: evita esconder produtos com vários anúncios grandes.
                item {
                    Surface(
                        onClick = { aviso = "Veja as ofertas dos parceiros abaixo." },
                        shape = RoundedCornerShape(20.dp),
                        color = AzulLoja
                    ) {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 150.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f).padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Cuidados para seu pet",
                                    fontSize = 22.sp, lineHeight = 25.sp,
                                    fontWeight = FontWeight.Bold, color = TextoLoja
                                )
                                Text("Ofertas dos parceiros", fontSize = 12.sp, color = TextoLoja)
                                Text("Explore o catálogo ↓", fontSize = 12.sp, color = TextoLoja)
                            }
                            Box(
                                Modifier.weight(0.75f).height(155.dp).background(LaranjaLoja),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painterResource(R.drawable.cachorro_inicio),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                item {
                    // Sem histórico de compras, o título não promete "comprar novamente".
                    Text(
                        if (tipo == "Serviços") "Serviços dos parceiros" else "Produtos em destaque",
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoLoja
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
                                oferta = oferta,
                                quantidade = carrinho.count { it == oferta.id },
                                modifier = Modifier.weight(1f),
                                abrir = { abrir(oferta) },
                                adicionar = {
                                    carrinho = ArrayList(carrinho).apply { add(oferta.id) }
                                },
                                remover = {
                                    carrinho = ArrayList(carrinho).apply { remove(oferta.id) }
                                }
                            )
                        }
                        if (linha.size < colunas) Spacer(Modifier.weight(1f))
                    }
                }
                if (visiveis.isEmpty()) {
                    item { Text("Nenhuma oferta encontrada.", color = TextoLoja) }
                }

                // Categorias vêm das ofertas reais, evitando botões sem resultados.
                item {
                    Text(
                        "Explore por categoria", fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, color = TextoLoja
                    )
                    Spacer(Modifier.height(8.dp))
                    val categorias = catalogo
                        .filter { if (tipo == "Serviços") it.tipo == "Serviço" else it.tipo != "Serviço" }
                        .map { it.categoria }.distinct().sorted()
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Todas") + categorias) { opcao ->
                            FilterChip(
                                selected = categoria == opcao,
                                onClick = { categoria = opcao },
                                label = { Text(opcao) },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = AzulLoja,
                                    selectedContainerColor = LaranjaLoja,
                                    labelColor = TextoLoja,
                                    selectedLabelColor = TextoLoja
                                )
                            )
                        }
                    }
                }

            }
            item {
                Text(
                    "Catálogo e carrinho demonstrativos. Compras e pagamentos são feitos com o parceiro.",
                    fontSize = 11.sp, lineHeight = 16.sp, color = TextoLoja
                )
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

    // Mostra os itens adicionados; não simula um checkout que ainda não existe.
    if (mostrarCarrinho) {
        AlertDialog(
            onDismissRequest = { mostrarCarrinho = false },
            title = { Text("Carrinho demonstrativo") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (carrinho.isEmpty()) Text("Seu carrinho está vazio.")
                    carrinho.distinct().forEach { id ->
                        val oferta = catalogo.firstOrNull { it.id == id }
                        if (oferta != null) {
                            Text(oferta.titulo + " × " + carrinho.count { it == id })
                            TextButton(onClick = { mostrarCarrinho = false; abrir(oferta) }) {
                                Text("Ver com o parceiro", color = TextoLoja)
                            }
                        }
                    }
                    if (carrinho.isNotEmpty()) {
                        Text("Lista local de demonstração. Não reserva produtos nem realiza pagamentos.")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarCarrinho = false }) { Text("Fechar") }
            },
            dismissButton = {
                if (carrinho.isNotEmpty()) {
                    TextButton(onClick = { carrinho = arrayListOf() }) { Text("Esvaziar") }
                }
            },
            containerColor = Color(0xFFFFF8F4)
        )
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra foto, informações, preço e ação nessa ordem.
// A imagem permanece livre de botões sobrepostos.
@Composable
private fun CartaoProdutoNovaLoja(
    oferta: OfertaAbrigo,
    quantidade: Int,
    modifier: Modifier = Modifier,
    abrir: () -> Unit,
    adicionar: () -> Unit,
    remover: () -> Unit
) {
    val servico = oferta.tipo == "Serviço"
    // As embalagens correspondem às fotos que já existem no seu projeto.
    val embalagem = when (oferta.id) {
        "vitrine-demo-racao", "vitrine-demo-petisco" -> "Embalagem de 100 g"
        "vitrine-demo-umido" -> "Embalagem de 85 g"
        else -> if (servico) "Consulte horários" else "Consulte a embalagem"
    }
    // Produtos sem embalagem definida levam aos detalhes antes de adicionar.
    val embalagemDefinida = oferta.id in setOf(
        "vitrine-demo-racao", "vitrine-demo-petisco", "vitrine-demo-umido"
    )
    Surface(
        onClick = abrir, modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFFF8F4),
        border = BorderStroke(1.dp, TextoLoja.copy(alpha = 0.2f))
    ) {
        Column {
            FotoModeloLoja(
                oferta,
                Modifier.fillMaxWidth().height(145.dp)
            )
            Column(
                Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    oferta.titulo, fontSize = 15.sp, lineHeight = 19.sp,
                    minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold, color = TextoLoja
                )
                Text(embalagem, fontSize = 11.sp, lineHeight = 15.sp, color = TextoLoja)
                Text(
                    oferta.parceiro, fontSize = 11.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, color = TextoLoja
                )
                Text(
                    oferta.centavos?.let(::reaisAbrigo) ?: "Consultar valor",
                    fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextoLoja
                )
                if (!servico && embalagemDefinida && quantidade > 0) {
                    // Permite ajustar a quantidade de embalagens sem cobrir a foto.
                    Row(
                        Modifier.fillMaxWidth().background(AzulLoja, RoundedCornerShape(12.dp)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = remover,
                            modifier = Modifier.semantics { contentDescription = "Remover uma embalagem" }
                        ) { Text("−", fontSize = 22.sp, color = TextoLoja) }
                        Text(quantidade.toString(), fontWeight = FontWeight.Bold, color = TextoLoja)
                        IconButton(
                            onClick = adicionar,
                            modifier = Modifier.semantics { contentDescription = "Adicionar uma embalagem" }
                        ) { Text("+", fontSize = 22.sp, color = TextoLoja) }
                    }
                } else {
                    Button(
                        onClick = if (servico || !embalagemDefinida) abrir else adicionar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulLoja, contentColor = TextoLoja
                        )
                    ) {
                        Text(
                            if (servico) "Ver serviço"
                            else if (embalagemDefinida) "Adicionar"
                            else "Ver opções",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
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


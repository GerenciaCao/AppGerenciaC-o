package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

// Mantém o nome utilizado pela navegação, embora exista apenas uma tela.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelaAgendaIntegrada(
    // Recebe os compromissos, os animais e as ações reais do aplicativo.
    ctx: ContextoAbrigo,
    // Permite ilustrar as fotos somente na prévia, sem alterar os cadastros.
    fotosIlustrativas: Map<String, Int> = emptyMap()
) {
    // Começa no dia do registro recém-salvo ou na data atual.
    val dataInicial = remember {
        ctx.dados.agendaDataAlvo?.takeIf { dataValidaAbrigo(it) } ?: hojeAbrigo()
    }
    // Guarda a seleção ao girar o celular.
    var ano by rememberSaveable { mutableIntStateOf(dataInicial.takeLast(4).toInt()) }
    var mes by rememberSaveable { mutableIntStateOf(dataInicial.substring(3, 5).toInt() - 1) }
    var dia by rememberSaveable { mutableIntStateOf(dataInicial.take(2).toInt()) }
    var todos by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Mostra o dia do compromisso criado ou reagendado ao retornar do formulário.
    LaunchedEffect(ctx.dados.agendaDataAlvo) {
        val alvo = ctx.dados.agendaDataAlvo
        if (alvo != null && dataValidaAbrigo(alvo)) {
            ano = alvo.takeLast(4).toInt()
            mes = alvo.substring(3, 5).toInt() - 1
            dia = alvo.take(2).toInt()
            todos = false
            ctx.atualizar(ctx.dados.copy(agendaDataAlvo = null))
        }
    }

    // Calendar usa janeiro como mês zero.
    val meses = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    val calendario = GregorianCalendar(ano, mes, 1)
    val quantidade = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val colunaInicial = calendario.get(Calendar.DAY_OF_WEEK) - 1

    // Ignora datas inválidas antes de acessar os números da data.
    val compromissosMes = ctx.dados.compromissos.filter {
        dataValidaAbrigo(it.data) && doPeriodoAbrigo(it.data, mes + 1, ano)
    }
    // Reúne as cores de cada dia, sem repetir a mesma categoria.
    val coresPorDia = compromissosMes
        .groupBy { it.data.take(2).toInt() }
        .mapValues { (_, registros) ->
            registros.map { corTipoAgenda(it.tipo) }.distinct()
        }
    // Exibe os registros do dia selecionado ou todos os registros do mês.
    val lista = compromissosMes.filter {
        todos || it.data.take(2).toInt() == dia
    }.sortedBy { chaveDataAbrigo(it.data) + it.horario }

    // Abre o formulário existente para criar um compromisso novo.
    val novoCompromisso: () -> Unit = {
        ctx.atualizar(ctx.dados.copy(editarCompromissoId = null))
        ctx.abrir("novo_compromisso")
    }

    BackHandler { ctx.voltar() }
    val cabecalho = LocalCabecalhoApp.current
    CompositionLocalProvider(
        // Mantém a saudação apenas nas telas em que ela foi solicitada.
        LocalCabecalhoApp provides cabecalho.copy(mostrarSaudacao = false),
        // Mantém os destinos da navegação compartilhada.
        LocalAbrirFinancas provides { ctx.principal("financas") },
        LocalAbrirLoja provides { ctx.principal("loja") }
    ) {
        MolduraPadraoApp(
            // Usa o creme padrão do aplicativo.
            containerColor = FundoPadraoApp,
            bottomBar = {
                MenuInferiorInicio(
                    // Recebe os avisos do menu compartilhado.
                    avisar = { aviso = it },
                    // Destaca a aba Agenda.
                    selecionado = "Agenda",
                    // Preserva os destinos já utilizados pelo aplicativo.
                    aoInicio = { ctx.principal("inicio") },
                    aoAnimais = { ctx.principal("animais") },
                    aoAdicionar = { ctx.principal("adicionar") },
                    aoAgenda = {}
                )
            }
        ) { espacos ->
            LazyColumn(
                modifier = Modifier
                    // Ocupa a área disponível.
                    .fillMaxSize()
                    // Respeita cabeçalho e navegação inferior.
                    .padding(espacos)
                    // Evita cobrir conteúdo com o teclado.
                    .imePadding(),
                // Repete as margens de Animais e Parcerias.
                contentPadding = PaddingValues(
                    start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp
                ),
                // Separa os cartões sem grandes espaços vazios.
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Na fonte ampliada, o botão pode passar para a linha seguinte.
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        // Distribui título e botão nas extremidades.
                        horizontalArrangement = Arrangement.SpaceBetween,
                        // Separa as linhas se houver quebra.
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            // Título sem seta, como nas outras abas principais.
                            text = "Agenda",
                            // Alinha visualmente o texto ao botão.
                            modifier = Modifier.padding(top = 7.dp, end = 8.dp),
                            // Usa a mesma tipografia de Animais e Parcerias.
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPadraoApp
                        )
                        Button(
                            // Abre o formulário de criação.
                            onClick = novoCompromisso,
                            // Mantém área de toque confortável.
                            modifier = Modifier.heightIn(min = 48.dp),
                            // Mantém o formato arredondado.
                            shape = RoundedCornerShape(24.dp),
                            // Define o espaço interno.
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                // Laranja da ação principal.
                                containerColor = LaranjaRestante,
                                // Texto escuro sobre o laranja.
                                contentColor = TextoRestante
                            )
                        ) {
                            Text(
                                // Nome da ação.
                                text = "+ Novo compromisso",
                                // Tamanho e peso da legenda.
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(30.dp))

                    // Seleciona o mês, inclusive na passagem entre anos.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = meses[mes] + " " + ano,
                            // Reserva espaço para as setas à direita.
                            modifier = Modifier.weight(1f),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPadraoApp
                        )
                        TextButton(onClick = {
                            if (mes == 0) { mes = 11; ano-- } else mes--
                            dia = 1
                            todos = false
                        }) {
                            Text("‹", fontSize = 24.sp, color = MarromRestante)
                        }
                        TextButton(onClick = {
                            if (mes == 11) { mes = 0; ano++ } else mes++
                            dia = 1
                            todos = false
                        }) {
                            Text("›", fontSize = 24.sp, color = MarromRestante)
                        }
                    }

                    // Mantém o calendário compacto sem espaço extra entre semanas.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, BordaRestante)
                    ) {
                        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                            Row(Modifier.fillMaxWidth()) {
                                listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { letra ->
                                    Box(
                                        // Distribui as sete colunas igualmente.
                                        modifier = Modifier.weight(1f).height(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letra,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MarromRestante
                                        )
                                    }
                                }
                            }
                            repeat((colunaInicial + quantidade + 6) / 7) { semana ->
                                Row(Modifier.fillMaxWidth()) {
                                    repeat(7) { coluna ->
                                        val numero = semana * 7 + coluna - colunaInicial + 1
                                        Box(
                                            modifier = Modifier.weight(1f).height(48.dp)
                                                // Seleciona somente datas válidas do mês.
                                                .clickable(enabled = numero in 1..quantidade) {
                                                    dia = numero
                                                    todos = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (numero in 1..quantidade) {
                                                // Um dia pode ter mais de um tipo de compromisso.
                                                val coresDia = coresPorDia[numero].orEmpty()
                                                Column(
                                                    // Centraliza número e indicadores na célula.
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    // Separa o círculo dos indicadores sem aumentar a semana.
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            // Mantém o tamanho do círculo do calendário.
                                                            .size(34.dp)
                                                            .background(
                                                                color = when {
                                                                    // O dia clicado fica sempre laranja.
                                                                    dia == numero -> Color(0xFFFFAE49)

                                                                    // Outros dias com compromisso ficam vermelhos.
                                                                    coresDia.isNotEmpty() -> Color(0xFFFFC5C5)

                                                                    // Dias sem compromisso permanecem sem fundo.
                                                                    else -> Color.Transparent
                                                                },
                                                                // Mantém o destaque circular.
                                                                shape = CircleShape
                                                            ),
                                                        // Centraliza o número dentro do círculo.
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            // Número correspondente ao dia do mês.
                                                            text = numero.toString(),
                                                            // Mantém o tamanho de leitura atual.
                                                            fontSize = 16.sp,
                                                            // Destaca o dia selecionado.
                                                            fontWeight = if (dia == numero) FontWeight.Bold else FontWeight.Normal,
                                                            // Texto escuro para leitura sobre os fundos suaves.
                                                            color = TextoRestante
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Mostra o título da lista à esquerda e o filtro de período à direita.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (todos) "Compromissos do mês" else "Próximos compromissos",
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPadraoApp
                        )
                        TextButton(onClick = { todos = !todos }) {
                            Text(
                                text = if (todos) "Ver dia" else "Ver todos",
                                fontSize = 12.sp,
                                color = MarromRestante
                            )
                        }
                    }
                    // Mostra a data dos compromissos selecionados.
                    if (!todos) {
                        Text(
                            // Exibe o dia e o mês selecionados.
                            text = "Dia " + dia + " de " + meses[mes].lowercase(Locale("pt", "BR")),
                            // Define o tamanho da data.
                            fontSize = 12.sp,
                            // Mantém a cor marrom do aplicativo.
                            color = MarromRestante
                        )
                    }

                    // Separa a data da legenda das categorias.
                    Spacer(Modifier.height(10.dp))

                    // Posiciona a legenda junto à lista de compromissos.
                    FlowRow(
                        // Ocupa a largura disponível.
                        modifier = Modifier.fillMaxWidth(),
                        // Define o espaço entre as categorias.
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        // Define o espaço caso a legenda quebre de linha.
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Identifica os cartões da categoria Visitas.
                        LegendaTipoAgenda("Visitas", corTipoAgenda("Visitas"))
                        // Identifica os cartões da categoria Cuidados.
                        LegendaTipoAgenda("Cuidados", corTipoAgenda("Cuidados"))
                        // Identifica os cartões da categoria Serviços.
                        LegendaTipoAgenda("Serviços", corTipoAgenda("Serviços"))
                    }

                    // Separa a legenda dos cartões abaixo.
                    Spacer(Modifier.height(12.dp))
                }

                // Cada cartão usa o animal vinculado pelo animalId do compromisso.
                items(lista, key = { it.id }) { compromisso ->
                    val animal = ctx.animais.firstOrNull { it.id == compromisso.animalId }
                    CartaoCompromissoAgendaUnica(
                        compromisso = compromisso,
                        animal = animal,
                        fotoIlustrativa = compromisso.animalId?.let { fotosIlustrativas[it] },
                        aoClicar = {
                            // Abre os detalhes do registro selecionado.
                            ctx.atualizar(ctx.dados.copy(compromissoId = compromisso.id))
                            ctx.abrir("detalhes_compromisso")
                        }
                    )
                }
                if (lista.isEmpty()) {
                    item {
                        Text(
                            text = if (todos) "Nenhum compromisso neste mês." else "Nenhum compromisso neste dia.",
                            modifier = Modifier.padding(vertical = 18.dp),
                            fontSize = 14.sp,
                            color = MarromRestante
                        )
                    }
                }
            }
        }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Cartão inspirado na agenda antiga, alimentado pelos registros reais.
@Composable
internal fun CartaoCompromissoAgendaUnica(
    // Registro selecionado da agenda.
    compromisso: CompromissoAbrigo,
    // Animal vinculado; pode ser ausente em visitas gerais.
    animal: AnimalExemplo?,
    // Recurso usado apenas nos exemplos da prévia.
    fotoIlustrativa: Int?,
    // Ação de abertura dos detalhes.
    aoClicar: () -> Unit
) {
    // Reutiliza o carregamento de imagens locais existente no projeto.
    val foto = fotoMiniaturaAdocao(animal?.fotoUri)
    val dataCurta = compromisso.data.take(2).toInt().toString() + " " +
            listOf("jan.", "fev.", "mar.", "abr.", "mai.", "jun.", "jul.", "ago.", "set.", "out.", "nov.", "dez.")[
                compromisso.data.substring(3, 5).toInt() - 1
            ]
    Surface(
        // Abre os detalhes ao tocar em qualquer parte do cartão.
        onClick = aoClicar,
        modifier = Modifier.fillMaxWidth(),
        // Mantém o formato do cartão antigo.
        shape = RoundedCornerShape(20.dp),
        // Usa fundo branco para destacar a foto e o texto.
        color = Color.White,
        // Mantém uma borda discreta.
        border = BorderStroke(1.dp, BordaRestante),
        // Cria profundidade leve, como na referência.
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                // Preserva o tamanho compacto dos cartões antigos.
                .heightIn(min = 78.dp)
                // Afasta o conteúdo da borda.
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Relaciona o cartão à mesma categoria marcada no calendário.
            Box(
                modifier = Modifier
                    // Define uma faixa discreta à esquerda da foto.
                    .width(4.dp)
                    // Mantém a faixa compatível com a altura do cartão.
                    .height(48.dp)
                    // Aplica a cor correspondente ao tipo cadastrado.
                    .background(
                        corTipoAgenda(compromisso.tipo),
                        RoundedCornerShape(4.dp)
                    )
            )
            // Separa a faixa da miniatura do animal.
            Spacer(Modifier.width(8.dp))
            Surface(
                // Reserva o mesmo espaço de foto em todos os cartões.
                modifier = Modifier.size(46.dp),
                // Arredonda a miniatura.
                shape = RoundedCornerShape(14.dp),
                // Mostra creme se não houver foto.
                color = CremeRestante
            ) {
                when {
                    foto != null -> Image(
                        bitmap = foto,
                        contentDescription = animal?.nome,
                        modifier = Modifier.fillMaxSize(),
                        // Preenche a miniatura sem deformar a imagem.
                        contentScale = ContentScale.Crop
                    )
                    fotoIlustrativa != null -> Image(
                        painter = painterResource(fotoIlustrativa),
                        contentDescription = animal?.nome,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    else -> Box(contentAlignment = Alignment.Center) {
                        // Não atribui uma foto fictícia a um animal real.
                        Text(
                            text = animal?.nome?.trim()?.take(1)?.uppercase()?.ifBlank { "?" } ?: "•",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarromRestante
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                // Reserva o restante para título e especificações.
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = compromisso.titulo,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPadraoApp
                )
                Text(
                    // Exibe data, horário e tipo cadastrados.
                    text = dataCurta + " · " + compromisso.horario + " · " + compromisso.tipo,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MarromRestante
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                // Indica que o cartão abre mais informações.
                text = "›",
                fontSize = 25.sp,
                color = MarromRestante
            )
        }
    }
}

// Demonstra a tela sem salvar os exemplos na coleção real do aplicativo.
@Preview(name = "Agenda", showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun PreviaAgendaUnica() {
    val base = contextoPreviewRestante()
    val animaisExemplo = remember {
        listOf(
            AnimalExemplo(id = "preview-lua", nome = "Lua", idade = "2 anos", situacao = "Disponível"),
            AnimalExemplo(id = "preview-bento", nome = "Bento", idade = "5 anos", situacao = "Disponível")
        )
    }
    var dados by remember {
        mutableStateOf(
            base.dados.copy(
                agendaDataAlvo = "07/11/2026",
                compromissos = listOf(
                    CompromissoAbrigo(
                        id = "preview-vacina", titulo = "Vacina da Lua", tipo = "Cuidados",
                        data = "07/11/2026", horario = "14:00", animalId = "preview-lua"
                    ),
                    CompromissoAbrigo(
                        id = "preview-visita", titulo = "Visita ao Bento", tipo = "Visitas",
                        data = "07/11/2026", horario = "16:00", animalId = "preview-bento"
                    )
                )
            )
        )
    }
    MaterialTheme {
        TelaAgendaIntegrada(
            ctx = ContextoAbrigo(
                dados = dados, animais = animaisExemplo,
                atualizar = { dados = it }, abrir = {}, voltar = {},
                principal = {}, verAnimal = {}
            ),
            // Usa os recursos já presentes no projeto somente nesta prévia.
            fotosIlustrativas = mapOf(
                "preview-lua" to R.drawable.iconcat,
                "preview-bento" to R.drawable.icondog
            )
        )
    }
}
// Define uma única regra de cores para calendário, legenda e cartões.
// O formulário atual já oferece Cuidados, Visitas e Serviços.
private fun corTipoAgenda(tipo: String): Color =
    when (tipo.trim().lowercase(Locale.ROOT)) {
        // Vermelho/coral identifica visitas e encontros de adoção.
        "visitas", "visita", "adoção", "adocao" -> Color(0xFFF2C94C)
        // Verde identifica cuidados, consultas e vacinação.
        "cuidados", "consulta", "consulta veterinária", "vacinação" -> Color(0xFFA6CF72)
        // Azul identifica serviços, como banho e tosa.
        "serviços", "servicos", "serviço", "banho e tosa" -> Color(0xFF80CDF8)
        // Registros antigos com outro tipo recebem uma cor neutra.
        else -> Color(0xFFAA998C)
    }

// Associa o ponto colorido ao nome da categoria para facilitar a interpretação.
@Composable
private fun LegendaTipoAgenda(
    // Nome exibido ao lado do ponto.
    texto: String,
    // Cor utilizada no ponto.
    cor: Color
) {
    Row(
        // Centraliza ponto e texto na mesma linha.
        verticalAlignment = Alignment.CenterVertically,
        // Define o espaço entre o ponto e sua legenda.
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                // Define o diâmetro do ponto da legenda.
                .size(15.dp)
                // Aplica a cor da categoria em formato circular.
                .background(cor, CircleShape)
        )
        Text(
            // Mostra a categoria correspondente à cor.
            text = texto,
            // Mantém a legenda secundária em relação ao calendário.
            fontSize = 15.sp,
            // Usa a cor principal para leitura.
            color = TextoRestante
        )
    }
}
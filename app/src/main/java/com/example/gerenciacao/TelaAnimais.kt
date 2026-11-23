package com.example.gerenciacao

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Guarda os dados de cada animal.
data class AnimalExemplo(
    val nome: String,
    val idade: String,
    val situacao: String,
    val raca: String = "Não informada",
    val sexo: String = "Não informado",
    val castrado: Boolean? = null,
    val fotoUri: String? = null,
    val id: String = java.util.UUID.randomUUID().toString(),
    val sobre: String = "",
    val historicoMedico: String = "",
    val arquivado: Boolean = false,
    val especie: String = "Não informada",
    // Guarda o porte selecionado no cadastro.
    val porte: String = "",
    // Guarda a cor da pelagem.
    val corPelagem: String = "",
    // Guarda as vacinas informadas, sem presumir aplicação.
    val vacinacoes: String = "",
    // Guarda alergias, doenças e necessidades informadas.
    val condicoesSaude: String = "",
    // Guarda os medicamentos informados no cadastro.
    val medicamentos: String = "",
    // Guarda as características de comportamento.
    val comportamento: String = "",
    // Guarda informações complementares.
    val observacoes: String = ""
) : java.io.Serializable

// Mantém os animais de exemplo do projeto.
val AnimaisExemplo = listOf(
    AnimalExemplo(
        nome = "Lua",
        idade = "2 anos",
        situacao = "Disponível",
        raca = "SRD",
        sexo = "Fêmea",
        castrado = true,
        sobre = "Carinhosa, brincalhona e gosta de companhia."
    ),
    AnimalExemplo(
        nome = "Bento",
        idade = "5 anos",
        situacao = "Disponível"
    ),
    AnimalExemplo(
        nome = "Mel",
        idade = "1 ano",
        situacao = "Visita marcada"
    ),
    AnimalExemplo(
        nome = "Tico",
        idade = "3 anos",
        situacao = "Adotado"
    )
)

// Define as cores da tela.
private val FundoAnimais = Color(0xFFFFFCF7)
private val TextoAnimais = Color(0xFF38291F)
private val MarromAnimais = Color(0xFF783E19)
private val CremeAnimais = Color(0xFFFFEFC5)
private val AmareloAnimais = Color(0xFFFFB414)
private val BordaAnimais = Color(0xFFEADFD2)

// Mostra os animais e mantém a navegação do projeto.
@Composable
fun TelaAnimais(
    aoVoltarInicio: () -> Unit,
    animais: List<AnimalExemplo>,
    aoAdicionar: () -> Unit,
    aoAbrirFicha: (String) -> Unit,
    aoAgenda: () -> Unit,
    aoAdocoes: () -> Unit
) {
    // Guarda a pesquisa e os dois filtros separadamente.
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    var filtroSituacao by rememberSaveable { mutableStateOf("Todas") }
    var mostrarMenuFiltro by remember { mutableStateOf(false) }

    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }

    BackHandler(onBack = aoVoltarInicio)

    // Combina pesquisa, espécie e disponibilidade.
    val animaisVisiveis = animais.filter { animal ->
        !animal.arquivado &&
                animal.nome.contains(busca.trim(), ignoreCase = true) &&
                (
                        filtro == "Todos" ||
                                animal.especie.equals(filtro, ignoreCase = true)
                        ) &&
                (
                        filtroSituacao == "Todas" ||
                                animal.situacao.equals(
                                    filtroSituacao,
                                    ignoreCase = true
                                )
                        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoAnimais)
    ) {
        MolduraPadraoApp(
            modifier = Modifier.weight(1f),
            containerColor = FundoPadraoApp,
            snackbarHost = {
                SnackbarHost(avisos)
            },
            bottomBar = {
                MenuInferiorInicio(
                    avisar,
                    selecionado = "Animais",
                    aoInicio = aoVoltarInicio,
                    aoAnimais = {},
                    aoAdicionar = aoAdicionar,
                    aoAgenda = aoAgenda
                )
            }
        ) { espacos ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacos)
                    .background(FundoAnimais)
                    .imePadding(),
                contentPadding = PaddingValues(
                    start = 22.dp,
                    end = 22.dp,
                    top = 22.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Mostra o título e o acesso às adoções.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Animais",
                            modifier = Modifier.weight(1f),
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoAnimais
                        )

                        TextButton(onClick = aoAdocoes) {
                            Text(
                                text = "Adoções",
                                color = MarromAnimais,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Pesquisa pelo nome e abre o filtro de disponibilidade.
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Buscar por ficha",
                                fontSize = 14.sp,
                                color = Color(0xFF7E7066)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(
                                    R.drawable.inicio_busca
                                ),
                                contentDescription = "Buscar pelo nome",
                                modifier = Modifier.size(20.dp),
                                tint = MarromAnimais
                            )
                        },
                        trailingIcon = {
                            Box {
                                FilledIconButton(
                                    onClick = {
                                        mostrarMenuFiltro = true
                                    },
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    colors = IconButtonDefaults
                                        .filledIconButtonColors(
                                            containerColor = MarromAnimais
                                        )
                                ) {
                                    // Desenha o ícone com três linhas.
                                    Canvas(Modifier.size(18.dp)) {
                                        val cor = Color(0xFFFFD993)
                                        val espessura = 1.5.dp.toPx()

                                        drawLine(
                                            color = cor,
                                            start = Offset(
                                                size.width * 0.10f,
                                                size.height * 0.25f
                                            ),
                                            end = Offset(
                                                size.width * 0.90f,
                                                size.height * 0.25f
                                            ),
                                            strokeWidth = espessura
                                        )

                                        drawLine(
                                            color = cor,
                                            start = Offset(
                                                size.width * 0.25f,
                                                size.height * 0.50f
                                            ),
                                            end = Offset(
                                                size.width * 0.75f,
                                                size.height * 0.50f
                                            ),
                                            strokeWidth = espessura
                                        )

                                        drawLine(
                                            color = cor,
                                            start = Offset(
                                                size.width * 0.40f,
                                                size.height * 0.75f
                                            ),
                                            end = Offset(
                                                size.width * 0.60f,
                                                size.height * 0.75f
                                            ),
                                            strokeWidth = espessura
                                        )
                                    }
                                }

                                // Altera somente o filtro de disponibilidade.
                                DropdownMenu(
                                    expanded = mostrarMenuFiltro,
                                    onDismissRequest = {
                                        mostrarMenuFiltro = false
                                    }
                                ) {
                                    listOf(
                                        "Todas",
                                        "Disponível",
                                        "Visita marcada",
                                        "Em tratamento",
                                        "Adotado"
                                    ).forEach { opcao ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (
                                                        filtroSituacao == opcao
                                                    ) {
                                                        "✓ $opcao"
                                                    } else {
                                                        opcao
                                                    }
                                                )
                                            },
                                            onClick = {
                                                filtroSituacao = opcao
                                                mostrarMenuFiltro = false
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmareloAnimais,
                            unfocusedBorderColor = BordaAnimais,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = TextoAnimais,
                            unfocusedTextColor = TextoAnimais,
                            cursorColor = MarromAnimais
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Filtro dos animais por espécie: Todos, Cães ou Gatos.
                    Row(
                        // Define o espaço entre os botões de filtro.
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cria um botão para cada opção da lista.
                        listOf("Todos", "Cães", "Gatos").forEach { opcao ->

                            // Define a foto utilizada em cada botão do filtro de animais.
                            val fotoFiltro = when (opcao) {
                                "Cães" -> R.drawable.icondog
                                "Gatos" -> R.drawable.iconcat
                                else -> null
                            }

                            FilterChip(
                                // Destaca o botão correspondente ao filtro atual.
                                selected = filtro == opcao,

                                // Atualiza o filtro ao tocar no botão.
                                // A lista animaisVisiveis usa esse valor para filtrar as fichas.
                                onClick = { filtro = opcao },

                                // Exibe uma foto provisória somente nos filtros Cães e Gatos.
                                // O filtro Todos permanece apenas com o texto.
                                leadingIcon = if (opcao != "Todos") {
                                    {
                                        Image(
                                            // Usa uma imagem que já existe no projeto.
                                            painter = painterResource(id = fotoFiltro ?: R.drawable.icondog),
                                            contentDescription = opcao,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    null
                                },

                                // Define o nome e a aparência do texto do botão.
                                label = {
                                    Text(
                                        text = opcao,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },

                                // Deixa as extremidades do botão arredondadas.
                                shape = CircleShape,

                                // Remove a borda do botão.
                                border = null,

                                // Define as cores dos botões selecionados e não selecionados.
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmareloAnimais,
                                    selectedLabelColor = MarromAnimais,
                                    containerColor = CremeAnimais,
                                    labelColor = MarromAnimais
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Atualiza o contador conforme os filtros.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fichas do abrigo",
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoAnimais
                        )

                        val total = animaisVisiveis.size

                        Text(
                            text = "$total ${
                                if (total == 1) "ficha" else "fichas"
                            }",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarromAnimais
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }

                // Abre a ficha correspondente ao cartão selecionado.
                items(
                    items = animaisVisiveis,
                    key = { it.id }
                ) { animal ->
                    FichaAnimal(
                        animal = animal,
                        aoAbrir = { aoAbrirFicha(animal.id) }
                    )
                }

                if (animaisVisiveis.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum bichinho encontrado.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            fontSize = 15.sp,
                            color = Color(0xFF7E7066)
                        )
                    }
                }
            }
        }
    }
}

// Mostra o cartão com a disponibilidade no canto superior direito.
@Composable
private fun FichaAnimal(
    animal: AnimalExemplo,
    aoAbrir: () -> Unit
) {
    val foto = lembrarFotoListaAnimal(animal.fotoUri)

    val corSituacao = when (animal.situacao) {
        "Disponível" -> Color(0xFFBCEB83)
        "Visita marcada" -> Color(0xFFFFDE83)
        "Em tratamento" -> Color(0xFFFFC48C)
        "Adotado" -> Color(0xFFFFB0B0)
        else -> CremeAnimais
    }

    Surface(
        onClick = aoAbrir,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BordaAnimais)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 100.dp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mostra a foto salva ou a inicial do nome.
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(CremeAnimais),
                contentAlignment = Alignment.Center
            ) {
                if (foto != null) {
                    Image(
                        bitmap = foto,
                        contentDescription = "Foto de ${animal.nome}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = animal.nome.trim()
                            .take(1)
                            .uppercase()
                            .ifBlank { "?" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarromAnimais
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = animal.nome,
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoAnimais
                    )

                    Spacer(Modifier.width(8.dp))

                    // Exibe a etiqueta colorida de disponibilidade.
                    Row(
                        modifier = Modifier
                            .widthIn(max = 140.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(corSituacao)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(MarromAnimais)
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            text = animal.situacao,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MarromAnimais
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = animal.idade,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF7E7066)
                )
            }
        }
    }
}

// Carrega uma miniatura da foto salva no aparelho.
@Composable
private fun lembrarFotoListaAnimal(endereco: String?): ImageBitmap? {
    val contexto = LocalContext.current

    val resultado by produceState<Pair<String?, ImageBitmap?>>(
        initialValue = null to null,
        key1 = endereco,
        key2 = contexto
    ) {
        value = endereco to null

        if (!endereco.isNullOrBlank()) {
            val imagem = withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(endereco)
                    val resolver = contexto.contentResolver

                    val limites = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    resolver.openInputStream(uri)?.use { entrada ->
                        BitmapFactory.decodeStream(entrada, null, limites)
                    }

                    if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                        null
                    } else {
                        val opcoes = BitmapFactory.Options().apply {
                            inSampleSize = 1

                            while (
                                maxOf(
                                    limites.outWidth,
                                    limites.outHeight
                                ) / inSampleSize > 256
                            ) {
                                inSampleSize *= 2
                            }
                        }

                        resolver.openInputStream(uri)?.use { entrada ->
                            BitmapFactory.decodeStream(
                                entrada,
                                null,
                                opcoes
                            )?.asImageBitmap()
                        }
                    }
                } catch (_: java.io.IOException) {
                    null
                } catch (_: SecurityException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            value = endereco to imagem
        }
    }

    return if (resultado.first == endereco) {
        resultado.second
    } else {
        null
    }
}

// Permite visualizar a tela no Android Studio.
@Preview(
    name = "Tela de animais",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaTelaAnimais() {
    MaterialTheme {
        TelaAnimais(
            aoVoltarInicio = {},
            animais = AnimaisExemplo,
            aoAdicionar = {},
            aoAbrirFicha = {},
            aoAgenda = {},
            aoAdocoes = {}
        )
    }
}
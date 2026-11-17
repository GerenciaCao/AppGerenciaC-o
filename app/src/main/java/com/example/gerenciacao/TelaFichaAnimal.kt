package com.example.gerenciacao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import java.io.File
import java.security.MessageDigest

// Define as cores da ficha individual.
private val FundoFicha = Color(0xFFFFF8EF)
private val LaranjaFicha = Color(0xFFFFAD43)
private val MarromFicha = Color(0xFF493020)
private val PessegoFicha = Color(0xFFFFDFB8)

// Mostra os dados do animal selecionado na lista.
// Os campos adicionais têm valores padrão até serem incluídos no cadastro.
@Composable
fun TelaFichaAnimal(
    animal: AnimalExemplo,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAgenda: () -> Unit,
    aoAdicionar: () -> Unit,
    aoEditar: () -> Unit,
    aoAcompanharAdocao: () -> Unit,
    corPelagem: String = "",
    pesoPorte: String = "",
    comportamento: String = "",
    resumoAdocao: String = "",
    etapasAdocao: List<String> = emptyList()
) {
    // Controla os detalhes complementares e o acompanhamento na própria ficha.
    var detalhesAbertos by rememberSaveable(animal.id) { mutableStateOf(false) }
    var adocaoAberta by rememberSaveable(animal.id) { mutableStateOf(false) }
    // Mantém o histórico aberto ao girar o aparelho.
    var mostrarHistorico by rememberSaveable(animal.id) {
        mutableStateOf(false)
    }

    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }

    // Carrega a foto e gera uma cópia PNG com fundo branco.
    val foto = lembrarFotoPngFicha(animal.fotoUri)

    // Define a cor da disponibilidade cadastrada.
    val corSituacao = when (animal.situacao) {
        "Disponível" -> Color(0xFF96C665)
        "Visita marcada" -> Color(0xFFFFDE83)
        "Em tratamento" -> Color(0xFFFFBD83)
        "Adotado" -> Color(0xFFFFAAAA)
        else -> Color(0xFFE5DFD5)
    }

    // Mostra o símbolo correspondente ao sexo informado.
    val simboloSexo = when (animal.sexo) {
        "Fêmea" -> "♀"
        "Macho" -> "♂"
        else -> ""
    }

    val corSexo = if (animal.sexo == "Fêmea") {
        Color(0xFFFF6587)
    } else {
        Color(0xFF428ACB)
    }

    // Converte a informação de castração em um texto legível.
    val castracao = when (animal.castrado) {
        true -> if (animal.sexo == "Fêmea") "Castrada" else "Castrado"
        false -> if (animal.sexo == "Fêmea") {
            "Não castrada"
        } else {
            "Não castrado"
        }
        null -> "Não informado"
    }

    BackHandler(onBack = aoVoltar)

    // Mantém a estrutura e o menu inferior já usados no projeto.
    MolduraPadraoApp(
        containerColor = FundoFicha,
        snackbarHost = {
            SnackbarHost(avisos)
        },
        bottomBar = {
            MenuInferiorInicio(
                avisar,
                selecionado = "Animais",
                aoInicio = aoInicio,
                aoAnimais = aoVoltar,
                aoAdicionar = aoAdicionar,
                aoAgenda = aoAgenda
            )
        }
    ) { espacos ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacos)
                .background(FundoFicha)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho laranja inspirado na ficha do Figma.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp
                        )
                    )
                    .background(LaranjaFicha)
                    .padding(18.dp)
            ) {
                // Retorna à lista de animais.
                FilledIconButton(
                    onClick = aoVoltar,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFFFCE88),
                        contentColor = MarromFicha
                    )
                ) {
                    Text(
                        text = "‹",
                        fontSize = 36.sp,
                        lineHeight = 36.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Ficha Individual",
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MarromFicha
                )
            }

            // Área branca para apresentar a foto sem cortar o animal.
            // Uma foto com cenário continuará mostrando esse cenário.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                val imagem = foto

                if (imagem != null) {
                    Image(
                        bitmap = imagem,
                        contentDescription = "Foto de ${animal.nome}",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Mantém a logo enquanto não há uma foto disponível.
                    Image(
                        painter = painterResource(R.drawable.pet2),
                        contentDescription = "Animal sem foto cadastrada",
                        modifier = Modifier.size(330.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Cartão principal com borda marrom e fundo creme.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(22.dp),
                color = FundoFicha,
                border = BorderStroke(1.5.dp, MarromFicha)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Nome e sexo à esquerda; disponibilidade à direita.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = animal.nome,
                                modifier = Modifier.weight(
                                    weight = 1f,
                                    fill = false
                                ),
                                fontSize = 34.sp,
                                lineHeight = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = MarromFicha
                            )

                            if (simboloSexo.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))

                                Text(
                                    text = simboloSexo,
                                    fontSize = 32.sp,
                                    color = corSexo
                                )
                            }
                        }

                        // Identifica a disponibilidade com cor e texto.
                        Row(
                            modifier = Modifier
                                .widthIn(max = 145.dp)
                                .clip(CircleShape)
                                .background(corSituacao)
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 7.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF36722A))
                            )

                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = animal.situacao,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MarromFicha
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Mostra primeiro raça, idade e sexo, sem várias caixas abertas.
                    Text(
                        text = "${animal.raca} · ${animal.idade} · ${animal.sexo}",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MarromFicha
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = listOf(pesoPorte, castracao).filter { it.isNotBlank() }.joinToString(" · "),
                        fontSize = 14.sp,
                        color = MarromFicha
                    )

                    // Expande os campos adicionais sem sair da ficha.
                    TextButton(onClick = { detalhesAbertos = !detalhesAbertos }) {
                        Text(
                            if (detalhesAbertos) "Menos informações ⌃" else "Mais informações ⌄",
                            color = MarromFicha
                        )
                    }
                    if (detalhesAbertos) {
                        // Organiza as informações principais em quatro colunas.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DadoPrincipalFicha(
                                titulo = "Espécie",
                                valor = when (animal.especie) {
                                    "Cães" -> "Cão"
                                    "Gatos" -> "Gato"
                                    else -> animal.especie
                                },
                                modifier = Modifier.weight(1f)
                            )

                            DadoPrincipalFicha(
                                titulo = "Raça",
                                valor = animal.raca,
                                modifier = Modifier.weight(1f)
                            )

                            DadoPrincipalFicha(
                                titulo = "Idade",
                                valor = animal.idade,
                                modifier = Modifier.weight(1f)
                            )

                            DadoPrincipalFicha(
                                titulo = "Cor",
                                valor = corPelagem,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // Destaca os dados complementares em faixas pêssego.
                        FaixaInformacaoFicha(
                            titulo = "Peso atual / Porte",
                            valor = pesoPorte
                        )

                        Spacer(Modifier.height(10.dp))

                        FaixaInformacaoFicha(
                            titulo = "Status reprodutivo",
                            valor = castracao
                        )

                        Spacer(Modifier.height(10.dp))

                        FaixaInformacaoFicha(
                            titulo = "Comportamento",
                            valor = comportamento
                        )

                        Spacer(Modifier.height(24.dp))


                    }
                    Text(
                        text = "Descrição do animal",
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MarromFicha
                    )

                    Spacer(Modifier.height(12.dp))

                    // Exibe a descrição real, sem inventar dados do animal.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = PessegoFicha,
                        border = BorderStroke(
                            1.dp,
                            Color(0xFFE4C8AA)
                        )
                    ) {
                        Text(
                            maxLines = if (detalhesAbertos) Int.MAX_VALUE else 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            text = animal.sobre.ifBlank {
                                "Sem descrição cadastrada."
                            },
                            modifier = Modifier.padding(18.dp),
                            fontSize = 16.sp,
                            lineHeight = 23.sp,
                            color = MarromFicha
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Reúne o acompanhamento na ficha sem exibir todas as etapas de início.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PessegoFicha
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Adoção", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = MarromFicha)
                            Spacer(Modifier.height(8.dp))
                            // Não inventa entrevistas ou interessados: os dados precisam vir da gestão.
                            Text(
                                resumoAdocao.ifBlank { "Nenhum acompanhamento informado." },
                                fontSize = 14.sp, lineHeight = 20.sp, color = MarromFicha
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { adocaoAberta = !adocaoAberta },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MarromFicha, contentColor = FundoFicha)
                            ) {
                                Text(if (adocaoAberta) "Recolher acompanhamento" else "Acompanhar adoção")
                            }
                            if (adocaoAberta) {
                                // Exibe as etapas reais recebidas como parâmetro, na mesma tela.
                                Spacer(Modifier.height(12.dp))
                                if (etapasAdocao.isEmpty()) {
                                    Text("Nenhuma etapa informada.", color = MarromFicha)
                                } else {
                                    etapasAdocao.forEachIndexed { indice, etapa ->
                                        Text("${indice + 1}. $etapa", Modifier.padding(vertical = 6.dp), color = MarromFicha)
                                    }
                                }
                                // Preserva o acesso às ações de gestão existentes.
                                TextButton(onClick = aoAcompanharAdocao) {
                                    Text("Gerenciar adoção", color = MarromFicha)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    // Abre o histórico médico cadastrado para este animal.
                    OutlinedButton(
                        onClick = { mostrarHistorico = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MarromFicha),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MarromFicha
                        )
                    ) {
                        Text("Histórico de saúde")
                    }

                    Spacer(Modifier.height(10.dp))

                    // Abre o formulário de edição existente.
                    TextButton(
                        onClick = aoEditar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Editar ficha",
                            color = MarromFicha,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Mostra o histórico em uma janela sobre a ficha.
    if (mostrarHistorico) {
        AlertDialog(
            onDismissRequest = { mostrarHistorico = false },
            title = {
                Text("Histórico de ${animal.nome}")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    Text(
                        text = animal.historicoMedico.ifBlank {
                            "Nenhum histórico médico cadastrado."
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarHistorico = false }
                ) {
                    Text("Fechar", color = MarromFicha)
                }
            },
            containerColor = FundoFicha
        )
    }
}

// Apresenta um dos quatro dados principais abaixo do nome.
@Composable
private fun DadoPrincipalFicha(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            fontSize = 11.sp,
            color = MarromFicha
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = valor.ifBlank { "Não informado" },
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MarromFicha
        )
    }
}

// Cria uma faixa com o nome da informação e seu valor.
@Composable
private fun FaixaInformacaoFicha(
    titulo: String,
    valor: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PessegoFicha,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 11.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = MarromFicha
            )

            Text(
                text = valor.ifBlank { "Não informado" },
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                color = MarromFicha
            )
        }
    }
}

// Lê a foto, reduz o tamanho e cria uma cópia PNG no cache.
// O arquivo original enviado pela pessoa permanece preservado.
// Esta conversão não recorta o animal nem remove o cenário da foto.
@Composable
private fun lembrarFotoPngFicha(endereco: String?): ImageBitmap? {
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

                    // Lê as dimensões antes de carregar a imagem inteira.
                    val limites = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    resolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, limites)
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
                                ) / inSampleSize > 1600
                            ) {
                                inSampleSize *= 2
                            }
                        }

                        val original = resolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, opcoes)
                        }

                        if (original == null) {
                            null
                        } else {
                            // Preenche de branco as áreas transparentes.
                            val branca = Bitmap.createBitmap(
                                original.width,
                                original.height,
                                Bitmap.Config.ARGB_8888
                            )

                            val canvas = AndroidCanvas(branca)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            canvas.drawBitmap(original, 0f, 0f, null)

                            // Usa um nome seguro para a cópia temporária PNG.
                            val chave = MessageDigest
                                .getInstance("SHA-256")
                                .digest(endereco.toByteArray())
                                .joinToString("") {
                                    "%02x".format(it.toInt() and 0xff)
                                }

                            val pasta = File(
                                contexto.cacheDir,
                                "fotos_animais_png"
                            )

                            if (!pasta.exists()) {
                                pasta.mkdirs()
                            }

                            val arquivo = File(pasta, "$chave.png")

                            // Salva a cópia em PNG sem alterar o cadastro.
                            try {
                                arquivo.outputStream().use { saida ->
                                    branca.compress(
                                        Bitmap.CompressFormat.PNG,
                                        100,
                                        saida
                                    )
                                }
                            } catch (_: java.io.IOException) {
                                // Ainda permite exibir a foto se o cache falhar.
                            }

                            branca.asImageBitmap()
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

    // Evita mostrar a imagem anterior enquanto outra é carregada.
    return if (resultado.first == endereco) {
        resultado.second
    } else {
        null
    }
}

// Mostra a ficha no painel de prévia do Android Studio.
@Preview(
    name = "Ficha Individual",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaTelaFichaAnimal() {
    MaterialTheme {
        TelaFichaAnimal(
            animal = AnimaisExemplo.first(),
            aoVoltar = {},
            aoInicio = {},
            aoAgenda = {},
            aoAdicionar = {},
            aoEditar = {},
            aoAcompanharAdocao = {}
        )
    }
}
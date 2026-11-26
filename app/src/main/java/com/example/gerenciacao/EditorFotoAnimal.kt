package com.example.gerenciacao

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay


import androidx.compose.ui.tooling.preview.Preview

// Permite enquadrar a imagem antes de confirmar a foto da ficha.
@Composable
fun DialogoEnquadrarFotoAnimal(
    // Endereço da imagem original.
    origem: String,
    // Fecha o editor preservando a foto anteriormente escolhida.
    aoCancelar: () -> Unit,
    // Entrega o endereço da cópia com o enquadramento confirmado.
    aoConfirmar: (String) -> Unit
) {
    // Reutiliza a leitura que já corrige a orientação da câmera.
    val estado = lembrarFotoOriginalFicha(origem)
    val imagem = estado.imagem

    // Permite salvar o recorte no armazenamento interno do aplicativo.
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()

    // Guarda o tamanho real da área de enquadramento.
    var medida by remember { mutableStateOf(IntSize.Zero) }

    // NaN indica que o enquadramento inicial ainda será calculado.
    var zoom by rememberSaveable(origem) {
        mutableFloatStateOf(Float.NaN)
    }

    // Guarda a posição proporcionalmente ao tamanho do recipiente.
    var deslocamentoX by rememberSaveable(origem) {
        mutableFloatStateOf(0f)
    }
    var deslocamentoY by rememberSaveable(origem) {
        mutableFloatStateOf(0f)
    }

    // Evita confirmar duas vezes durante a gravação.
    var salvando by remember { mutableStateOf(false) }

    // Exibe problemas sem perder o enquadramento.
    var erro by remember { mutableStateOf<String?>(null) }
    // Mostra uma orientação visual ao abrir o editor.
    var mostrarDica by rememberSaveable(origem) {
        mutableStateOf(true)
    }

    // Retira a dica após seis segundos, contados depois de carregar a foto.
    LaunchedEffect(origem, imagem) {
        if (imagem != null) {
            delay(6_000)
            mostrarDica = false
        }
    }

    // Calcula a escala que permite mostrar a imagem inteira.
    val escalaBase = if (
        imagem != null && medida.width > 0 && medida.height > 0
    ) {
        minOf(
            medida.width.toFloat() / imagem.width,
            medida.height.toFloat() / imagem.height
        )
    } else {
        1f
    }

    // Fotos comuns começam preenchendo a área; PNGs transparentes, inteiros.
    val zoomInicial = if (
        imagem != null && medida.width > 0 && medida.height > 0
    ) {
        if (estado.transparente) {
            1f
        } else {
            maxOf(
                medida.width.toFloat() / imagem.width,
                medida.height.toFloat() / imagem.height
            ) / escalaBase
        }
    } else {
        1f
    }

    // Permite aproximar além do enquadramento inicial.
    val zoomMaximo = maxOf(6f, zoomInicial * 4f)
    val zoomExibido = if (zoom.isNaN()) zoomInicial else zoom

    // Aplica zoom e arraste sem perder a imagem para fora do recipiente.
    fun ajustarEnquadramento(
        novoZoom: Float,
        movimento: Offset = Offset.Zero
    ) {
        val foto = imagem ?: return
        if (medida.width == 0 || medida.height == 0) return
        // Esconde a orientação quando a pessoa começa a ajustar.
        mostrarDica = false

        val anterior = if (zoom.isNaN()) zoomInicial else zoom
        val novo = novoZoom.coerceIn(1f, zoomMaximo)
        val escala = escalaBase * novo

        // Limita o arraste nas dimensões em que a foto ultrapassa a moldura.
        val limiteX = (
                (foto.width * escala - medida.width) / 2f
                ).coerceAtLeast(0f)
        val limiteY = (
                (foto.height * escala - medida.height) / 2f
                ).coerceAtLeast(0f)

        val x = deslocamentoX * medida.width * (novo / anterior) +
                movimento.x
        val y = deslocamentoY * medida.height * (novo / anterior) +
                movimento.y

        zoom = novo
        deslocamentoX = x.coerceIn(-limiteX, limiteX) / medida.width
        deslocamentoY = y.coerceIn(-limiteY, limiteY) / medida.height
    }

    Dialog(
        // Cancelar não substitui a foto da ficha.
        onDismissRequest = {
            if (!salvando) aoCancelar()
        },
        // Mantém espaço para os controles e protege a gravação em andamento.
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !salvando,
            dismissOnClickOutside = !salvando
        )
    ) {
        Surface(
            // Afasta o editor das bordas do aparelho.
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            // Mantém o formato dos recipientes do aplicativo.
            shape = RoundedCornerShape(20.dp),
            // Usa o fundo claro da interface.
            color = Color(0xFFFFFCF7)
        ) {
            Column(
                // Permite acessar os botões em aparelhos com pouca altura.
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                // Separa os controles do editor.
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    // Identifica a tarefa.
                    text = "Enquadrar foto",
                    // Destaca o título.
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF432818)
                )

                if (mostrarDica && imagem != null) {
                    // Mostra um exemplo visual do movimento.
                    DicaAnimadaEnquadramento()
                } else {
                    Text(
                        // Mantém uma orientação curta após a animação.
                        text = "Posicione o rosto do bichinho dentro da foto.",
                        // Define um tamanho legível.
                        fontSize = 14.sp,
                        // Mantém o marrom do aplicativo.
                        color = Color(0xFF6B3919)
                    )
                }

                if (imagem != null) {
                    Canvas(
                        modifier = Modifier
                            // Usa a mesma proporção da foto na ficha.
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            // Respeita os limites visuais da prévia.
                            .clip(RoundedCornerShape(16.dp))
                            // Mostra o azul atrás de áreas transparentes.
                            .background(Color(0xFFB7E7FC))
                            // Obtém as dimensões usadas também ao salvar.
                            .onSizeChanged { medida = it }
                            // Permite arrastar e aproximar com dois dedos.
                            .pointerInput(origem, medida, salvando) {
                                if (!salvando) {
                                    detectTransformGestures {
                                            _, movimento, fatorZoom, _ ->
                                        val atual = if (zoom.isNaN()) {
                                            zoomInicial
                                        } else {
                                            zoom
                                        }
                                        ajustarEnquadramento(
                                            atual * fatorZoom,
                                            movimento
                                        )
                                    }
                                }
                            }
                    ) {
                        val escala = escalaBase * zoomExibido

                        // Centraliza a imagem e acrescenta o arraste do usuário.
                        val esquerda = (
                                size.width - imagem.width * escala
                                ) / 2f + deslocamentoX * size.width

                        val topo = (
                                size.height - imagem.height * escala
                                ) / 2f + deslocamentoY * size.height

                        withTransform({
                            // Posiciona a imagem na moldura.
                            translate(esquerda, topo)
                            // Aplica o zoom sem alterar a proporção do animal.
                            scale(
                                escala,
                                escala,
                                pivot = Offset.Zero
                            )
                        }) {
                            drawImage(imagem)
                        }
                    }

                    Text(
                        // Identifica o controle deslizante.
                        text = " Aproximar ou afastar (Zoom)",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF432818)
                    )

                    Slider(
                        // Mostra o zoom atual.
                        value = zoomExibido.coerceIn(1f, zoomMaximo),
                        // Atualiza o enquadramento ao mover a barra.
                        onValueChange = {
                            ajustarEnquadramento(it)
                        },
                        // Permite voltar a mostrar a imagem inteira.
                        valueRange = 1f..zoomMaximo,
                        // Bloqueia alterações enquanto grava.
                        enabled = !salvando
                    )

                    TextButton(
                        // Restaura o enquadramento inicial.
                        onClick = {
                            zoom = Float.NaN
                            deslocamentoX = 0f
                            deslocamentoY = 0f
                        },
                        // Não interfere em uma gravação.
                        enabled = !salvando
                    ) {
                        Text(
                            text = "Redefinir enquadramento",
                            color = Color(0xFF6B3919)
                        )
                    }
                } else {
                    Text(
                        // Diferencia carregamento de falha na leitura.
                        text = if (estado.erro) {
                            "Não foi possível abrir a imagem. Escolha outra foto."
                        } else {
                            "Carregando foto…"
                        },
                        color = Color(0xFF6B3919)
                    )
                }

                erro?.let { mensagem ->
                    Text(
                        // Explica uma falha ao salvar o recorte.
                        text = mensagem,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                Row(
                    // Divide a largura entre as duas ações.
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        // Volta sem aplicar o novo enquadramento.
                        onClick = aoCancelar,
                        modifier = Modifier.weight(1f),
                        enabled = !salvando
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        // Grava uma cópia com o enquadramento escolhido.
                        onClick = {
                            val foto = imagem ?: return@Button
                            val tamanho = medida
                            val zoomFinal = zoomExibido
                            val xFinal = deslocamentoX
                            val yFinal = deslocamentoY

                            salvando = true
                            erro = null

                            escopo.launch {
                                try {
                                    val endereco = withContext(Dispatchers.IO) {
                                        salvarRecorteAnimal(
                                            contexto = contexto,
                                            original = foto.asAndroidBitmap(),
                                            medida = tamanho,
                                            zoom = zoomFinal,
                                            deslocamentoX = xFinal,
                                            deslocamentoY = yFinal
                                        )
                                    }
                                    aoConfirmar(endereco)
                                } catch (cancelado: CancellationException) {
                                    throw cancelado
                                } catch (_: Exception) {
                                    erro = "Não foi possível salvar. Tente novamente."
                                } finally {
                                    salvando = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        // Só confirma quando a foto e as dimensões estão prontas.
                        enabled = imagem != null &&
                                medida.width > 0 &&
                                medida.height > 0 &&
                                !salvando,
                        // Mantém o botão principal marrom.
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF432818),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (salvando) "Salvando…" else "Usar foto"
                        )
                    }
                }
            }
        }
    }
}

// Salva uma cópia local sem sobrescrever a imagem original.
private fun salvarRecorteAnimal(
    contexto: Context,
    original: Bitmap,
    medida: IntSize,
    zoom: Float,
    deslocamentoX: Float,
    deslocamentoY: Float
): String {
    // Usa uma resolução uniforme na proporção de quatro por três.
    val larguraSaida = 1200
    val alturaSaida = 900

    // Repete o mesmo cálculo de escala usado na prévia.
    val escalaBase = minOf(
        medida.width.toFloat() / original.width,
        medida.height.toFloat() / original.height
    )
    val escala = escalaBase * zoom
    val fatorSaida = larguraSaida.toFloat() / medida.width

    // Repete a posição escolhida pelo usuário.
    val esquerda = (
            medida.width - original.width * escala
            ) / 2f + deslocamentoX * medida.width

    val topo = (
            medida.height - original.height * escala
            ) / 2f + deslocamentoY * medida.height

    // Preserva transparência nas regiões vazias do enquadramento.
    val resultado = Bitmap.createBitmap(
        larguraSaida,
        alturaSaida,
        Bitmap.Config.ARGB_8888
    )

    try {
        val matriz = Matrix().apply {
            setScale(escala * fatorSaida, escala * fatorSaida)
            postTranslate(esquerda * fatorSaida, topo * fatorSaida)
        }

        // Renderiza a mesma composição mostrada no editor.
        android.graphics.Canvas(resultado).drawBitmap(
            original,
            matriz,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )

        // Guarda a cópia fora do cache para ela continuar disponível.
        val pasta = File(contexto.filesDir, "fotos_animais")
        check(pasta.isDirectory || pasta.mkdirs())

        val arquivo = File(
            pasta,
            "animal_" + UUID.randomUUID().toString() + ".png"
        )

        try {
            arquivo.outputStream().use { saida ->
                check(
                    resultado.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        saida
                    )
                )
            }
        } catch (falha: Exception) {
            // Remove somente a cópia incompleta desta tentativa.
            arquivo.delete()
            throw falha
        }

        return Uri.fromFile(arquivo).toString()
    } finally {
        // Libera somente a imagem criada para a saída.
        resultado.recycle()
    }
}

// Mostra o editor de fotos no Preview.
@Preview(
    name = "Editor de foto",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaEditorFotoAnimal() {
    MaterialTheme {
        DialogoEnquadrarFotoAnimal(
            // Usa uma imagem que já existe no drawable.
            origem = "android.resource://" +
                    LocalContext.current.packageName + "/" + R.drawable.dog1,
            // Mantém as ações vazias na prévia.
            aoCancelar = {},
            aoConfirmar = {}
        )
    }
}

// Demonstra o movimento de arrastar sem modificar a foto.
@Composable
private fun DicaAnimadaEnquadramento() {
    // Guarda a posição vertical do indicador.
    val movimento = remember { Animatable(0f) }

    // Executa uma demonstração curta ao aparecer.
    LaunchedEffect(Unit) {
        repeat(2) {
            // Move o indicador para cima.
            movimento.animateTo(
                targetValue = -8f,
                animationSpec = tween(durationMillis = 450)
            )

            // Move o indicador para baixo.
            movimento.animateTo(
                targetValue = 8f,
                animationSpec = tween(durationMillis = 650)
            )
        }

        // Retorna à posição inicial ao terminar.
        movimento.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350)
        )
    }

    Row(
        // Reserva espaço para o movimento sem empurrar os outros controles.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .background(
                color = Color(0xFFE7F4FE),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        // Centraliza o indicador em relação à orientação.
        verticalAlignment = Alignment.CenterVertically,
        // Separa o indicador do texto.
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            // Representa o movimento para cima e para baixo.
            text = "↕",
            // Aplica a animação somente ao indicador.
            modifier = Modifier.offset(y = movimento.value.dp),
            // Mantém a seta grande e visível.
            fontSize = 38.sp,
            // Destaca os traços.
            fontWeight = FontWeight.Bold,
            // Usa um azul escuro com contraste.
            color = Color(0xFF285484)
        )

        Column(
            // Permite que a orientação quebre de linha se necessário.
            modifier = Modifier.weight(1f),
            // Separa a instrução principal da explicação.
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                // Explica a ação principal.
                text = "Arraste para posicionar",
                // Destaca a instrução.
                fontWeight = FontWeight.Bold,
                // Define o tamanho da orientação.
                fontSize = 16.sp,
                // Mantém contraste com o fundo claro.
                color = Color(0xFF432818)
            )

            Text(
                // Explica onde tocar e em quais direções mover.
                text = "Toque na foto, segure e mova para cima, para baixo ou para os lados.",
                // Define o tamanho da explicação.
                fontSize = 14.sp,
                // Mantém espaço entre as linhas.
                lineHeight = 20.sp,
                // Usa o texto secundário.
                color = Color(0xFF6B3919)
            )
        }
    }
}
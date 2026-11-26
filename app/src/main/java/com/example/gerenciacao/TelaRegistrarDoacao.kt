package com.example.gerenciacao

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun TelaRegistrarDoacao(ctx: ContextoAbrigo) {
    val contexto = LocalContext.current

    var tipoDoacao by rememberSaveable { mutableStateOf("Dinheiro") }
    var valor by rememberSaveable { mutableStateOf("") }

    // Mantém os textos das duas abas independentes.
    var descricaoDinheiro by rememberSaveable { mutableStateOf("") }
    var tituloProduto by rememberSaveable { mutableStateOf("") }
    var quantidade by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }
    var animalId by rememberSaveable { mutableStateOf("") }

    val instanteInicial = rememberSaveable {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(Date())
    }

    var dataDoacao by rememberSaveable {
        mutableStateOf(instanteInicial.substringBefore(' '))
    }
    var horarioDoacao by rememberSaveable {
        mutableStateOf(instanteInicial.substringAfter(' '))
    }

    var tentouSalvar by rememberSaveable { mutableStateOf(false) }
    var erroVinculo by rememberSaveable { mutableStateOf<String?>(null) }

    val dinheiro = tipoDoacao == "Dinheiro"

    // Valida os campos da aba atual.
    val erroValor = if (dinheiro && centavosAbrigo(valor) == null) {
        "Informe um valor maior que zero, como 200,00."
    } else null

    val erroTitulo = if (!dinheiro && tituloProduto.isBlank()) {
        "Informe o título do produto."
    } else null

    val quantidadeValida = Regex(
        "^[0-9]+(?:[,.][0-9]+)?\\s+.*\\p{L}.*$"
    ).matches(quantidade.trim()) &&
            quantidade.trim()
                .substringBefore(' ')
                .replace(',', '.')
                .toBigDecimalOrNull()
                ?.signum() == 1

    val erroQuantidade = if (!dinheiro && !quantidadeValida) {
        "Informe quantidade e unidade, como 20 kg ou 2 pacotes."
    } else null

    val erroData = if (!dataValidaAbrigo(normalizarDataDoacao(dataDoacao))) {
        "Informe uma data válida: DD/MM/AAAA."
    } else null

    val erroHorario = if (!horarioValidoDoacao(horarioDoacao.trim())) {
        "Informe um horário válido: HH:mm, como 14:30."
    } else null

    val alterado = valor.isNotBlank() ||
            descricaoDinheiro.isNotBlank() ||
            tituloProduto.isNotBlank() ||
            quantidade.isNotBlank() ||
            observacoes.isNotBlank() ||
            animalId.isNotEmpty() ||
            tipoDoacao != "Dinheiro" ||
            normalizarDataDoacao(dataDoacao) != instanteInicial.substringBefore(' ') ||
            horarioDoacao != instanteInicial.substringAfter(' ')

    BaseRestante(
        ctx = ctx,
        titulo = "Registrar doação",
        aba = "Finanças",
        alterado = alterado
    ) {
        Text(
            text = "Cada contribuição faz diferença",
            color = MarromRestante
        )

        // Remove a categoria Bichinho deste formulário.
        FiltrosRestantes(
            opcoes = listOf("Dinheiro", "Produtos"),
            atual = tipoDoacao
        ) {
            tipoDoacao = it
            tentouSalvar = false
            erroVinculo = null
        }

        if (dinheiro) {
            CampoDoacao(
                rotulo = "Valor *",
                valor = valor,
                aoAlterar = { valor = it },
                exemplo = "200,00",
                teclado = KeyboardType.Decimal,
                prefixo = "R$ ",
                limite = 16,
                erro = if (tentouSalvar) erroValor else null
            )
        } else {
            CampoDoacao(
                rotulo = "Título do produto *",
                valor = tituloProduto,
                aoAlterar = { tituloProduto = it },
                exemplo = "Ex.: ração para cães adultos",
                limite = 120,
                erro = if (tentouSalvar) erroTitulo else null
            )

            CampoDoacao(
                rotulo = "Quantidade e unidade *",
                valor = quantidade,
                aoAlterar = { quantidade = it },
                exemplo = "Ex.: 20 kg ou 2 pacotes",
                limite = 120,
                erro = if (tentouSalvar) erroQuantidade else null
            )
        }

        // Permite digitar a data ou escolher pelo calendário.
        CampoDoacao(
            rotulo = "Data *",
            calendario = true,
            valor = dataDoacao,
            aoAlterar = { dataDoacao = it },
            exemplo = "Dia/Mes/Ano",
            limite = 10,
            erro = if (tentouSalvar) erroData else null,
            aoAbrirSeletor = {
                val calendario = Calendar.getInstance()

                if (dataValidaAbrigo(normalizarDataDoacao(dataDoacao))) {
                    val partes = normalizarDataDoacao(dataDoacao).split("/")
                    calendario.set(
                        partes[2].toInt(),
                        partes[1].toInt() - 1,
                        partes[0].toInt()
                    )
                }

                DatePickerDialog(
                    contexto,
                    { _, ano, mes, dia ->
                        dataDoacao = String.format(
                            Locale.ROOT,
                            "%02d/%02d/%04d",
                            dia,
                            mes + 1,
                            ano
                        )
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        )

        // Permite digitar o horário ou escolher pelo relógio.
        CampoDoacao(
            rotulo = "Horário *",
            valor = horarioDoacao,
            aoAlterar = { horarioDoacao = it },
            exemplo = "Hora:minuto",
            limite = 5,
            erro = if (tentouSalvar) erroHorario else null,
            relogio = true,
            aoAbrirSeletor = {
                val agora = Calendar.getInstance()
                val horarioAtual = normalizarHorarioDoacao(horarioDoacao)
                val valido = horarioValidoDoacao(horarioAtual)

                val hora = if (valido) {
                    horarioAtual.substringBefore(':').toInt()
                } else {
                    agora.get(Calendar.HOUR_OF_DAY)
                }

                val minuto = if (valido) {
                    horarioAtual.substringAfter(':').toInt()
                } else {
                    agora.get(Calendar.MINUTE)
                }

                TimePickerDialog(
                    contexto,
                    { _, horaEscolhida, minutoEscolhido ->
                        horarioDoacao = String.format(
                            Locale.ROOT,
                            "%02d:%02d",
                            horaEscolhida,
                            minutoEscolhido
                        )
                    },
                    hora,
                    minuto,
                    true
                ).show()
            }
        )

        // O vínculo é opcional nas duas categorias.
        SeletorRestante(
            rotulo = "Bichinho relacionado · opcional",
            valor = ctx.animais
                .firstOrNull { it.id == animalId }
                ?.nome ?: "Não vinculado",
            opcoes = listOf("" to "Não vinculado") +
                    ctx.animais
                        .filterNot { it.arquivado }
                        .map { it.id to it.nome },
            selecionar = {
                animalId = it
                erroVinculo = null
            }
        )

        if (dinheiro) {
            CampoDoacao(
                rotulo = "Descrição · opcional",
                valor = descricaoDinheiro,
                aoAlterar = { descricaoDinheiro = it },
                exemplo = "Ex.: contribuição para os cuidados da Lua",
                multilinha = true,
                limite = 2000
            )
        } else {
            CampoDoacao(
                rotulo = "Observações · opcional",
                valor = observacoes,
                aoAlterar = { observacoes = it },
                exemplo = "Outras informações sobre os produtos recebidos",
                multilinha = true,
                limite = 2000
            )
        }

        erroVinculo?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        BotaoRestante("Salvar doação") {
            tentouSalvar = true
            erroVinculo = null

            val camposValidos = listOf(
                erroValor,
                erroTitulo,
                erroQuantidade,
                erroData,
                erroHorario
            ).all { it == null }

            if (camposValidos) {
                val vinculoValido = animalId.isEmpty() ||
                        ctx.animais.any {
                            it.id == animalId && !it.arquivado
                        }

                if (!vinculoValido) {
                    erroVinculo = "Escolha um bichinho da lista ativa."
                } else {
                    val registro = LancamentoAbrigo(
                        descricao = if (dinheiro) {
                            descricaoDinheiro.trim().ifBlank {
                                "Doação em dinheiro"
                            }
                        } else {
                            tituloProduto.trim()
                        },
                        natureza = if (dinheiro) {
                            NaturezaAbrigo.DINHEIRO
                        } else {
                            NaturezaAbrigo.PRODUTOS
                        },
                        data = normalizarDataDoacao(dataDoacao),
                        horario = normalizarHorarioDoacao(horarioDoacao),
                        centavos = if (dinheiro) {
                            requireNotNull(centavosAbrigo(valor))
                        } else {
                            0L
                        },
                        quantidade = if (dinheiro) {
                            ""
                        } else {
                            quantidade.trim()
                        },
                        animalId = animalId.ifEmpty { null },
                        observacoes = if (dinheiro) {
                            ""
                        } else {
                            observacoes.trim()
                        }
                    )

                    ctx.atualizar(
                        ctx.dados.copy(
                            lancamentos = ctx.dados.lancamentos + registro
                        )
                    )
                    ctx.voltar()
                }
            }
        }
    }
}

// Campo editável com ícone opcional dentro do container, à direita.
@Composable
private fun CampoDoacao(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit,
    exemplo: String,
    teclado: KeyboardType = KeyboardType.Text,
    prefixo: String? = null,
    multilinha: Boolean = false,
    limite: Int = 160,
    erro: String? = null,
    relogio: Boolean = false,
    calendario: Boolean = false,
    aoAbrirSeletor: (() -> Unit)? = null
) {
    OutlinedTextField(
        // O horário mantém apenas os números no texto editável.
        value = when {
            relogio -> valor.filter { it in '0'..'9' }.take(4)
            calendario -> valor.filter { it in '0'..'9' }.take(8)
            else -> valor
        },
        onValueChange = { novoValor ->
            aoAlterar(
                when {
                    relogio -> novoValor
                        .filter { it in '0'..'9' }
                        .take(4)

                    calendario -> novoValor
                        .filter { it in '0'..'9' }
                        .take(8)

                    else -> novoValor.take(limite)
                }
            )
        },
        visualTransformation = when {
            relogio -> MascaraHorarioDoacao
            calendario -> MascaraDataDoacao
            else -> VisualTransformation.None
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(rotulo) },
        placeholder = { Text(exemplo) },
        singleLine = !multilinha,
        minLines = if (multilinha) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        isError = erro != null,
        supportingText = if (erro != null) {
            { Text(erro) }
        } else null,
        prefix = if (prefixo != null) {
            { Text(prefixo) }
        } else null,
        trailingIcon = if (aoAbrirSeletor != null) {
            {
                IconButton(
                    onClick = aoAbrirSeletor,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = if (relogio) {
                                "Escolher horário pelo relógio"
                            } else {
                                "Escolher data pelo calendário"
                            }
                        }
                ) {
                    if (relogio) {
                        IconeRelogioDoacao()
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.iconcalendario),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MarromRestante
                        )
                    }
                }
            }
        } else null,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LaranjaRestante,
            unfocusedBorderColor = BordaRestante,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = TextoRestante,
            unfocusedTextColor = TextoRestante,
            cursorColor = MarromRestante
        )
    )
}

// Desenha o relógio sem depender de uma biblioteca extra de ícones.
@Composable
private fun IconeRelogioDoacao() {
    Canvas(Modifier.size(24.dp)) {
        val espessura = 2.dp.toPx()
        val centro = Offset(size.width / 2, size.height / 2)

        drawCircle(
            color = MarromRestante,
            radius = size.minDimension * 0.40f,
            center = centro,
            style = Stroke(width = espessura)
        )

        drawLine(
            color = MarromRestante,
            start = centro,
            end = Offset(size.width * 0.50f, size.height * 0.26f),
            strokeWidth = espessura,
            cap = StrokeCap.Round
        )

        drawLine(
            color = MarromRestante,
            start = centro,
            end = Offset(size.width * 0.69f, size.height * 0.61f),
            strokeWidth = espessura,
            cap = StrokeCap.Round
        )
    }
}

private fun horarioValidoDoacao(valor: String): Boolean =
    Regex("([01][0-9]|2[0-3]):[0-5][0-9]")
        .matches(normalizarHorarioDoacao(valor))

@Preview(
    name = "Registrar doação",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviewTelaRegistrarDoacao() {
    MaterialTheme {
        TelaRegistrarDoacao(contextoPreviewRestante())
    }
}

// Insere os dois-pontos automaticamente após os dois primeiros números.
private object MascaraHorarioDoacao : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val numeros = text.text

        if (numeros.length < 2) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatado = numeros.take(2) + ":" + numeros.drop(2)

        val mapeamento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset < 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                (if (offset <= 2) offset else offset - 1)
                    .coerceIn(0, numeros.length)
        }

        return TransformedText(
            AnnotatedString(formatado),
            mapeamento
        )
    }
}

// Converte tanto a digitação quanto a escolha pelo relógio para HH:mm.
private fun normalizarHorarioDoacao(valor: String): String {
    val numeros = valor.filter { it in '0'..'9' }

    return if (numeros.length == 4) {
        numeros.take(2) + ":" + numeros.drop(2)
    } else {
        numeros
    }
}

// Exibe DD/MM/AAAA enquanto a pessoa digita somente os números.
private object MascaraDataDoacao : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val numeros = text.text

        val formatado = buildString {
            numeros.forEachIndexed { indice, numero ->
                append(numero)

                // Acrescenta uma barra depois do dia e outra depois do mês.
                if (indice == 1 || indice == 3) {
                    append('/')
                }
            }
        }

        val mapeamento = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val barras = when {
                    offset >= 4 -> 2
                    offset >= 2 -> 1
                    else -> 0
                }

                return (offset + barras).coerceIn(0, formatado.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                return formatado
                    .take(offset.coerceIn(0, formatado.length))
                    .count { it in '0'..'9' }
                    .coerceIn(0, numeros.length)
            }
        }

        return TransformedText(
            AnnotatedString(formatado),
            mapeamento
        )
    }
}

// Padroniza a data digitada e a data escolhida pelo calendário.
private fun normalizarDataDoacao(valor: String): String {
    val numeros = valor.filter { it in '0'..'9' }

    return if (numeros.length == 8) {
        "${numeros.take(2)}/${numeros.substring(2, 4)}/${numeros.drop(4)}"
    } else {
        numeros
    }
}
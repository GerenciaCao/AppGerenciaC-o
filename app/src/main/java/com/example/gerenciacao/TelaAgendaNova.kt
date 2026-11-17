package com.example.gerenciacao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.GregorianCalendar

// Mantem o calendario existente e passa a listar os compromissos realmente salvos.
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TelaAgendaIntegrada(ctx: ContextoAbrigo) {
    var ano by rememberSaveable { mutableIntStateOf(2026) }
    var mes by rememberSaveable { mutableIntStateOf(10) }
    var dia by rememberSaveable { mutableIntStateOf(7) }
    var todos by rememberSaveable { mutableStateOf(false) }
    // Ao criar ou reagendar, abre o dia do registro salvo em vez de parecer que ele sumiu.
    LaunchedEffect(ctx.dados.agendaDataAlvo) {
        val alvo = ctx.dados.agendaDataAlvo
        if (alvo != null && dataValidaAbrigo(alvo)) {
            ano = alvo.takeLast(4).toInt(); mes = alvo.substring(3, 5).toInt() - 1; dia = alvo.take(2).toInt(); todos = false
            ctx.atualizar(ctx.dados.copy(agendaDataAlvo = null))
        }
    }
    val meses = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val calendario = GregorianCalendar(ano, mes, 1)
    val quantidade = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val colunaInicial = calendario.get(Calendar.DAY_OF_WEEK) - 1
    val lista = ctx.dados.compromissos.filter { doPeriodoAbrigo(it.data, mes + 1, ano) && (todos || it.data.take(2).toInt() == dia) }
        .sortedBy { chaveDataAbrigo(it.data) + it.horario }
    BaseRestante(ctx, "Agenda", "Agenda") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${meses[mes]} $ano", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            TextButton(onClick = { if (mes == 0) { mes = 11; ano-- } else mes--; dia = 1; todos = false }) { Text("‹", fontSize = 24.sp) }
            TextButton(onClick = { if (mes == 11) { mes = 0; ano++ } else mes++; dia = 1; todos = false }) { Text("›", fontSize = 24.sp) }
        }
        // Mantém o cartão sem o espaçamento automático entre as semanas.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            color = androidx.compose.ui.graphics.Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BordaRestante)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                // Aumenta a leitura das letras sem aumentar a altura do calendário.
                Row(Modifier.fillMaxWidth()) {
                    listOf("D", "S", "T", "Q", "Q", "S", "S").forEach { letra ->
                        Box(
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

                // Agrupa as semanas sem espaços adicionais entre as linhas.
                repeat((colunaInicial + quantidade + 6) / 7) { semana ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { coluna ->
                            val numero = semana * 7 + coluna - colunaInicial + 1
                            // Toda a célula responde ao toque, e não só o número.
                            Box(
                                modifier = Modifier.weight(1f).height(48.dp)
                                    .clickable(enabled = numero in 1..quantidade) {
                                        dia = numero
                                        todos = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (numero in 1..quantidade) {
                                    // Destaca somente a data selecionada.
                                    Box(
                                        modifier = Modifier.size(34.dp).background(
                                            if (dia == numero) LaranjaRestante
                                            else androidx.compose.ui.graphics.Color.Transparent,
                                            CircleShape
                                        ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = numero.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = if (dia == numero) FontWeight.Bold
                                            else FontWeight.Normal,
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

        // Alinha as ações em uma linha, liberando espaço para os compromissos.
        // FlowRow permite quebrar a linha quando a fonte do aparelho estiver ampliada.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TextButton(onClick = { todos = !todos }) {
                Text(
                    text = if (todos) "Ver dia" else "Ver mês",
                    color = MarromRestante
                )
            }
            Button(
                onClick = {
                    ctx.atualizar(ctx.dados.copy(editarCompromissoId = null))
                    ctx.abrir("novo_compromisso")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjaRestante,
                    contentColor = TextoRestante
                )
            ) {
                Text("+ Novo compromisso")
            }
        }
        Text(if (todos) "Compromissos do mês" else "Compromissos de $dia/${mes + 1}/$ano", fontWeight = FontWeight.Bold)
        if (lista.isEmpty()) Text("Nenhum compromisso neste período.", color = MarromRestante)
        lista.forEach { compromisso ->
            AtalhoRestante(compromisso.titulo, "${compromisso.data} · ${compromisso.horario} · ${compromisso.tipo}") {
                ctx.atualizar(ctx.dados.copy(compromissoId = compromisso.id))
                ctx.abrir("detalhes_compromisso")
            }
        }
        Text("Agenda local. Nenhuma notificação automática será enviada.", fontSize = 12.sp)
    }
}
// Mostra a agenda realmente usada pelo aplicativo, com dados demonstrativos.
// Nenhum compromisso da prévia é salvo nos dados reais.
@androidx.compose.ui.tooling.preview.Preview(
    name = "Agenda integrada compacta",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaAgendaIntegradaCompacta() {
    val contextoBase = contextoPreviewRestante()
    var dadosPreview by remember {
        mutableStateOf(
            contextoBase.dados.copy(
                compromissos = listOf(
                    CompromissoAbrigo(
                        id = "preview-vacina",
                        titulo = "Vacina da Lua",
                        tipo = "Cuidados",
                        data = "07/11/2026",
                        horario = "14:00"
                    ),
                    CompromissoAbrigo(
                        id = "preview-visita",
                        titulo = "Visita ao Bento",
                        tipo = "Adoção",
                        data = "07/11/2026",
                        horario = "16:00"
                    )
                )
            )
        )
    }
    MaterialTheme {
        CompositionLocalProvider(
            LocalCabecalhoApp provides DadosCabecalhoApp(
                nome = "Helena",
                mostrarSaudacao = false
            )
        ) {
            TelaAgendaIntegrada(
                ContextoAbrigo(
                    dados = dadosPreview,
                    animais = contextoBase.animais,
                    atualizar = { dadosPreview = it },
                    abrir = {},
                    voltar = {},
                    principal = {},
                    verAnimal = {}
                )
            )
        }
    }
}
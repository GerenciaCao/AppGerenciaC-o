package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// O mesmo formulario cria um compromisso ou reagenda o registro recebido pelo ID.
@Composable
fun TelaNovoCompromisso(ctx: ContextoAbrigo) {
    val original = ctx.dados.compromissos.firstOrNull { it.id == ctx.dados.editarCompromissoId }
    val chave = original?.id ?: "novo"
    var titulo by rememberSaveable(chave) { mutableStateOf(original?.titulo ?: "") }
    var tipo by rememberSaveable(chave) { mutableStateOf(original?.tipo ?: "Cuidados") }
    var data by rememberSaveable(chave) { mutableStateOf(original?.data ?: hojeAbrigo()) }
    var horario by rememberSaveable(chave) { mutableStateOf(original?.horario ?: "") }
    var animalId by rememberSaveable(chave) { mutableStateOf(original?.animalId ?: "") }
    var local by rememberSaveable(chave) { mutableStateOf(original?.local ?: "") }
    var observacoes by rememberSaveable(chave) { mutableStateOf(original?.observacoes ?: "") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    val alterado = titulo != (original?.titulo ?: "") || tipo != (original?.tipo ?: "Cuidados") ||
            data != (original?.data ?: hojeAbrigo()) || horario != (original?.horario ?: "") ||
            animalId != (original?.animalId ?: "") || local != (original?.local ?: "") || observacoes != (original?.observacoes ?: "")
    BaseRestante(ctx, if (original == null) "Novo compromisso" else "Reagendar", "Agenda", alterado) {
        Text("Organize os próximos cuidados", color = MarromRestante)
        CampoRestante("Nome do compromisso", titulo, { titulo = it })
        // Controla a abertura das opções do campo Tipo.
        var menuTipoAberto by remember { mutableStateOf(false) }

// Usa as mesmas cores dos cartões e da legenda da agenda.
        val coresTipos = mapOf(
            "Cuidados" to Color(0xFFA6CF72),
            // Define o amarelo de Visitas no formulário de novo compromisso.
            "Visitas" to Color(0xFFF2C94C),
            "Serviços" to Color(0xFF80CDF8)
        )

// Identifica o campo do formulário.
        Text(
            text = "Tipo",
            color = MarromRestante
        )

        Box(
            // Faz o campo ocupar a largura disponível.
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                // Abre as opções ao tocar no campo.
                onClick = { menuTipoAberto = true },

                // Mantém uma área confortável para tocar.
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                Box(
                    modifier = Modifier
                        // Define o tamanho do indicador.
                        .size(10.dp)
                        // Mostra a cor correspondente ao tipo selecionado.
                        .background(
                            coresTipos[tipo] ?: Color.Gray,
                            CircleShape
                        )
                )

                // Separa o indicador do nome.
                Spacer(Modifier.width(8.dp))

                Text(
                    // Mostra o tipo selecionado.
                    text = tipo,

                    // Reserva o restante do campo para o texto.
                    modifier = Modifier.weight(1f),

                    // Mantém a cor padrão da escrita.
                    color = TextoRestante
                )

                // Indica que o campo possui opções.
                Text("⌄", color = MarromRestante)
            }

            DropdownMenu(
                // Exibe as opções quando o campo estiver aberto.
                expanded = menuTipoAberto,

                // Fecha ao tocar fora do menu.
                onDismissRequest = { menuTipoAberto = false }
            ) {
                coresTipos.forEach { (nome, cor) ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                // Alinha o indicador com o nome da categoria.
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        // Define o tamanho do ponto.
                                        .size(10.dp)
                                        // Aplica a cor da categoria.
                                        .background(cor, CircleShape)
                                )

                                // Separa o ponto do nome.
                                Spacer(Modifier.width(8.dp))

                                Text(
                                    // Identifica a opção.
                                    text = nome,
                                    color = TextoRestante
                                )
                            }
                        },
                        onClick = {
                            // Atualiza o mesmo campo que já é salvo no compromisso.
                            tipo = nome

                            // Fecha o menu após a escolha.
                            menuTipoAberto = false
                        }
                    )
                }
            }
        }

// Explica a relação entre o campo e as cores da agenda.
        Text(
            text = "Essa cor identifica o compromisso na agenda.",
            color = MarromRestante
        )
        CampoRestante("Data · DD/MM/AAAA", data, { data = it }, dica = "07/11/2026")
        CampoRestante("Horário · Hora:Minuto:Segundo", horario, { horario = it }, dica = "14:00")
        SeletorRestante("Bichinho relacionado · opcional",
            ctx.animais.firstOrNull { it.id == animalId }?.nome ?: "Não vinculado",
            listOf("" to "Não vinculado") + ctx.animais.filterNot { it.arquivado }.map { it.id to it.nome }) { animalId = it }
        CampoRestante("Local · opcional", local, { local = it })
        CampoRestante("Observações · opcional", observacoes, { observacoes = it }, longo = true)
        CartaoRestante(CremeRestante) { Text("O compromisso aparecerá na agenda do abrigo. Nenhum lembrete automático será enviado.") }
        // Valida antes de alterar a colecao; a edicao preserva o mesmo ID.
        BotaoRestante("Salvar compromisso") {
            when {
                titulo.trim().length < 3 -> erro = "Informe um título com pelo menos 3 caracteres."
                !dataValidaAbrigo(data.trim()) -> erro = "Informe uma data válida no formato DD/MM/AAAA."
                !Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(horario.trim()) -> erro = "Informe um horário válido, como 14:00."
                animalId.isNotEmpty() && ctx.animais.none { it.id == animalId && !it.arquivado } -> erro = "Escolha um bichinho disponível ou Não vinculado."
                else -> {
                    val salvo = CompromissoAbrigo(id = original?.id ?: java.util.UUID.randomUUID().toString(),
                        titulo = titulo.trim(), tipo = tipo, data = data.trim(), horario = horario.trim(),
                        animalId = animalId.ifEmpty { null }, local = local.trim(), observacoes = observacoes.trim())
                    ctx.atualizar(ctx.dados.copy(compromissos = ctx.dados.compromissos.filterNot { it.id == salvo.id } + salvo,
                        compromissoId = salvo.id, editarCompromissoId = null, agendaDataAlvo = salvo.data))
                    ctx.voltar()
                }
            }
        }
    }
    erro?.let { MensagemRestante(it) { erro = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaNovoCompromisso() {
    androidx.compose.material3.MaterialTheme { TelaNovoCompromisso(contextoPreviewRestante()) }
}
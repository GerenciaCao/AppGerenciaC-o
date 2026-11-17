package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// Representa um estabelecimento sem inventar distância ou avaliação.
data class EstabelecimentoClinicaVeterinaria(
    val id: String,
    val nome: String,
    val endereco: String = "",
    val distanciaKm: Double? = null,
    val avaliacao: Double? = null,
    val fotoUri: String? = null
)

// Usa a paleta azul, pêssego e laranja da referência do marketing.
private val FundoClinicasVeterinarias = Color(0xFFFFFFFF)
private val AzulClinicasVeterinarias = Color(0xFF8EB8E8)
private val LaranjaClinicasVeterinarias = Color(0xFFFF9F43)
private val TextoClinicasVeterinarias = Color(0xFF493020)

// Recebe os estabelecimentos reais; exemplos só são usados na prévia.
@Composable
fun TelaClinicasVeterinarias(
    estabelecimentos: List<EstabelecimentoClinicaVeterinaria>,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoAbrirServico: (String) -> Unit,
    localizacao: String = ""
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var ordenar by rememberSaveable { mutableStateOf("Nome") }
    var menuFiltro by remember { mutableStateOf(false) }
    var estabelecimentoSelecionado by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Combina busca com a ordenação, colocando valores ausentes por último.
    val encontrados = estabelecimentos.filter {
        (it.nome + " " + it.endereco).contains(busca.trim(), ignoreCase = true)
    }
    val visiveis = when (ordenar) {
        "Distância" -> encontrados.sortedBy { it.distanciaKm ?: Double.MAX_VALUE }
        "Avaliação" -> encontrados.sortedByDescending { it.avaliacao ?: -1.0 }
        else -> encontrados.sortedBy { it.nome.lowercase(java.util.Locale.ROOT) }
    }

    BackHandler(onBack = aoVoltar)

    // Mantém o cabeçalho e a navegação padrão sem a saudação nesta tela.
    val cabecalho = LocalCabecalhoApp.current
    CompositionLocalProvider(
        LocalCabecalhoApp provides cabecalho.copy(mostrarSaudacao = false)
    ) {
        MolduraPadraoApp(
            containerColor = FundoClinicasVeterinarias,
            floatingActionButton = {},
            bottomBar = {
                MenuInferiorInicio(
                    avisar = { aviso = it },
                    selecionado = "Loja",
                    aoInicio = aoInicio,
                    aoAnimais = aoAnimais,
                    aoAdicionar = aoAdicionar,
                    aoAgenda = aoAgenda
                )
            }
        ) { espacos ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(espacos).imePadding(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // A seta retorna às categorias de serviços.
                    TituloPaginaPadrao("Clínicas Veterinárias", aoVoltar)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        localizacao.ifBlank { "Localização não definida" },
                        fontSize = 13.sp, color = TextoClinicasVeterinarias
                    )
                    Spacer(Modifier.height(14.dp))

                    // Procura estabelecimentos pelo nome ou endereço.
                    OutlinedTextField(
                        value = busca,
                        onValueChange = { busca = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Buscar estabelecimento", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.inicio_busca),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = TextoClinicasVeterinarias
                            )
                        },
                        trailingIcon = {
                            Box {
                                TextButton(onClick = { menuFiltro = true }) {
                                    Text("Filtrar", color = TextoClinicasVeterinarias, fontSize = 12.sp)
                                }
                                DropdownMenu(
                                    expanded = menuFiltro,
                                    onDismissRequest = { menuFiltro = false }
                                ) {
                                    listOf("Nome", "Distância", "Avaliação").forEach { opcao ->
                                        DropdownMenuItem(
                                            text = { Text("Ordenar por " + opcao.lowercase()) },
                                            onClick = { ordenar = opcao; menuFiltro = false }
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = AzulClinicasVeterinarias,
                            unfocusedBorderColor = TextoClinicasVeterinarias.copy(alpha = 0.18f)
                        )
                    )
                }

                // Cada cartão mostra um estabelecimento e sua ação de agendamento.
                items(visiveis, key = { it.id }) { estabelecimento ->
                    val foto = fotoMiniaturaAdocao(estabelecimento.fotoUri)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFEDE6),
                        border = BorderStroke(1.dp, TextoClinicasVeterinarias.copy(alpha = 0.15f))
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(70.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = AzulClinicasVeterinarias.copy(alpha = 0.4f)
                                ) {
                                    if (foto != null) {
                                        Image(
                                            bitmap = foto,
                                            contentDescription = "Foto de " + estabelecimento.nome,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("Sem foto", fontSize = 11.sp, color = TextoClinicasVeterinarias)
                                        }
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        estabelecimento.nome,
                                        fontSize = 16.sp, lineHeight = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextoClinicasVeterinarias
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        estabelecimento.endereco.ifBlank { "Endereço não informado" },
                                        fontSize = 12.sp, color = TextoClinicasVeterinarias
                                    )
                                    estabelecimento.distanciaKm?.let { distancia ->
                                        Text(
                                            String.format(java.util.Locale("pt", "BR"), "%.1f km de distância", distancia),
                                            fontSize = 12.sp, color = TextoClinicasVeterinarias
                                        )
                                    }
                                    Text(
                                        estabelecimento.avaliacao?.let {
                                            String.format(java.util.Locale("pt", "BR"), "★ %.1f", it)
                                        } ?: "Sem avaliações",
                                        fontSize = 12.sp, color = TextoClinicasVeterinarias
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { estabelecimentoSelecionado = estabelecimento.id },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LaranjaClinicasVeterinarias,
                                    contentColor = TextoClinicasVeterinarias
                                )
                            ) {
                                Text("Agendar consulta", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (visiveis.isEmpty()) {
                    item {
                        Text(
                            "Nenhum estabelecimento encontrado.",
                            Modifier.padding(vertical = 24.dp),
                            color = TextoClinicasVeterinarias
                        )
                    }
                }
            }
        }
    }

    // Não confirma reservas sem uma integração de horários com o prestador.
    val selecionado = estabelecimentos.firstOrNull { it.id == estabelecimentoSelecionado }
    if (selecionado != null) {
        AlertDialog(
            onDismissRequest = { estabelecimentoSelecionado = null },
            title = { Text(selecionado.nome) },
            text = { Text("Consulte os horários e as condições diretamente com o parceiro. Nenhum agendamento foi confirmado.") },
            confirmButton = {
                TextButton(onClick = {
                    estabelecimentoSelecionado = null
                    aoAbrirServico(selecionado.id)
                }) { Text("Ver clínica", color = TextoClinicasVeterinarias) }
            },
            dismissButton = {
                TextButton(onClick = { estabelecimentoSelecionado = null }) { Text("Cancelar") }
            },
            containerColor = Color(0xFFFFF8F4)
        )
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Os dados abaixo existem apenas para conferir a aparência no Android Studio.
@Preview(name = "Clínicas Veterinárias", showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun PreviaTelaClinicasVeterinarias() {
    MaterialTheme {
        TelaClinicasVeterinarias(
            estabelecimentos = listOf(
                EstabelecimentoClinicaVeterinaria("preview-1", "Clínica Amigo Animal", "Rua de exemplo, 123", 3.0, 4.5),
                EstabelecimentoClinicaVeterinaria("preview-2", "Clínica Patas e Cuidado", "Avenida de exemplo, 45", 7.0, 4.0),
                EstabelecimentoClinicaVeterinaria("preview-3", "Veterinária Vida Pet", "Praça de exemplo, 10", 10.0, 4.8)
            ),
            aoVoltar = {}, aoInicio = {}, aoAnimais = {},
            aoAdicionar = {}, aoAgenda = {}, aoAbrirServico = {},
            localizacao = "Endereço de exemplo"
        )
    }
}
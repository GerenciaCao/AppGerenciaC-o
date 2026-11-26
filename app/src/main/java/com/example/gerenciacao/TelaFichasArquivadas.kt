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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TelaFichasArquivadas(
    animais: List<AnimalExemplo>,
    agora: Long,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAgenda: () -> Unit,
    aoAbrirFicha: (String) -> Unit,
    aoRestaurar: (String) -> Unit
) {
    BackHandler(onBack = aoVoltar)

    val arquivadas = animais
        .filter { it.arquivado && !arquivoAnimalVencido(it, agora) }
        .sortedByDescending { it.arquivadoEm ?: 0L }

    MolduraPadraoApp(
        containerColor = FundoPadraoApp,
        floatingActionButton = {},
        bottomBar = {
            MenuInferiorInicio(
                avisar = {},
                selecionado = "Animais",
                aoInicio = aoInicio,
                aoAnimais = aoVoltar,
                aoAdicionar = {},
                aoAgenda = aoAgenda
            )
        }
    ) { espacos ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacos),
            contentPadding = PaddingValues(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                TituloPaginaPadrao(
                    titulo = "Fichas arquivadas",
                    aoVoltar = aoVoltar
                )
            }

            item {
                Text(
                    text = "Você pode restaurar uma ficha por até 30 dias após arquivá-la. Depois desse prazo, ela será excluída.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MarromPadraoApp
                )
            }

            if (arquivadas.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma ficha arquivada.",
                        modifier = Modifier.padding(vertical = 24.dp),
                        fontSize = 16.sp,
                        color = MarromPadraoApp
                    )
                }
            }

            items(arquivadas, key = { it.id }) { animal ->
                val foto = lembrarFotoOriginalFicha(animal.fotoUri).imagem
                val dias = diasRestantesArquivoAnimal(animal, agora)

                Surface(
                    onClick = { aoAbrirFicha(animal.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE8DFD4))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFB7E7FC)),
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
                                        text = animal.nome.take(1).uppercase(),
                                        fontSize = 25.sp,
                                        color = MarromPadraoApp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = animal.nome,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextoPadraoApp
                                )

                                Text(
                                    text = if (dias == 1) {
                                        "Exclusão em 1 dia"
                                    } else {
                                        "Exclusão em $dias dias"
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFFB94E00)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { aoAbrirFicha(animal.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ver ficha", color = MarromPadraoApp)
                            }

                            Button(
                                onClick = { aoRestaurar(animal.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF432818),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Restaurar")
                            }
                        }
                    }
                }
            }
        }
    }
}
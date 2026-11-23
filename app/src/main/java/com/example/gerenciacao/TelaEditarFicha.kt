package com.example.gerenciacao

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview

// Reutiliza o formulário completo para que criação e edição tenham os mesmos campos.
@Composable
fun TelaEditarFicha(
    animal: AnimalExemplo,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoSalvar: (AnimalExemplo) -> Unit,
    aoArquivar: (String) -> Unit
) {
    TelaNovaFicha(
        // Preenche todos os campos com os dados do animal selecionado.
        animalInicial = animal,
        // Mantém os destinos já ligados no aplicativo.
        aoVoltar = aoVoltar,
        aoInicio = aoInicio,
        aoAgenda = aoAgenda,
        aoAnimais = aoAnimais,
        aoAdicionar = aoAdicionar,
        // Atualiza o mesmo registro e preserva o arquivamento.
        aoSalvar = aoSalvar,
        aoArquivar = aoArquivar
    )
}

// Mostra a edição no painel de prévia.
@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun PreviaEditarFichaCompleta() {
    MaterialTheme {
        TelaEditarFicha(
            animal = AnimaisExemplo.first(),
            aoVoltar = {}, aoInicio = {}, aoAnimais = {}, aoAdicionar = {},
            aoAgenda = {}, aoSalvar = {}, aoArquivar = {}
        )
    }
}
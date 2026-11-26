package com.example.gerenciacao

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private const val DIA_ARQUIVO_MS = 24L * 60 * 60 * 1000
private const val PRAZO_ARQUIVO_MS = 30L * DIA_ARQUIVO_MS

// Verifica o prazo somente nas fichas arquivadas.
fun arquivoAnimalVencido(
    animal: AnimalExemplo,
    agora: Long = System.currentTimeMillis()
): Boolean {
    val inicio = animal.arquivadoEm ?: return false
    return animal.arquivado && agora - inicio >= PRAZO_ARQUIVO_MS
}

// Arredonda para cima: uma fração de dia ainda conta como um dia restante.
fun diasRestantesArquivoAnimal(
    animal: AnimalExemplo,
    agora: Long = System.currentTimeMillis()
): Int {
    val inicio = animal.arquivadoEm ?: return 30
    val restante = (PRAZO_ARQUIVO_MS - (agora - inicio))
        .coerceIn(0L, PRAZO_ARQUIVO_MS)

    return ((restante + DIA_ARQUIVO_MS - 1) / DIA_ARQUIVO_MS).toInt()
}

// Inicia o prazo de arquivos antigos sem data e remove os vencidos.
fun prepararArquivosAnimais(
    animais: List<AnimalExemplo>,
    agora: Long = System.currentTimeMillis()
): ArrayList<AnimalExemplo> {
    val preparados = animais.map { animal ->
        when {
            animal.arquivado && animal.arquivadoEm == null ->
                animal.copy(arquivadoEm = agora)

            !animal.arquivado && animal.arquivadoEm != null ->
                animal.copy(arquivadoEm = null)

            else -> animal
        }
    }

    return ArrayList(
        preparados.filterNot { arquivoAnimalVencido(it, agora) }
    )
}

private fun arquivoLocalFichas(contexto: Context): AtomicFile =
    AtomicFile(File(contexto.filesDir, "fichas_animais_v1.bin"))

// Retorna null somente quando ainda não existe um arquivo local.
fun carregarFichasLocais(contexto: Context): ArrayList<AnimalExemplo>? {
    val arquivo = arquivoLocalFichas(contexto)
    val copiaSeguranca = File(arquivo.baseFile.path + ".bak")

    if (!arquivo.baseFile.exists() && !copiaSeguranca.exists()) {
        return null
    }

    return ObjectInputStream(arquivo.openRead()).use { entrada ->
        val lista = entrada.readObject() as? List<*>
            ?: error("Formato de fichas inválido.")

        ArrayList(lista.map { item ->
            item as? AnimalExemplo
                ?: error("Ficha inválida no armazenamento.")
        })
    }
}

// Grava uma cópia completa e mantém o arquivo anterior se houver falha.
fun salvarFichasLocais(
    contexto: Context,
    animais: List<AnimalExemplo>
) {
    val arquivo = arquivoLocalFichas(contexto)
    val saida = arquivo.startWrite()

    try {
        val objetos = ObjectOutputStream(saida)
        objetos.writeObject(ArrayList(animais))
        objetos.flush()
        arquivo.finishWrite(saida)
    } catch (erro: Exception) {
        arquivo.failWrite(saida)
        throw erro
    }
}


package com.example.gerenciacao

import java.io.Serializable
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// Armazena datas e horarios como texto validado, compativel com Android 7 e posteriores.
data class CompromissoAbrigo(val id: String = UUID.randomUUID().toString(), val titulo: String,
                             val tipo: String, val data: String, val horario: String, val animalId: String? = null,
                             val local: String = "", val observacoes: String = "") : Serializable

// Produtos e recebimentos de bichinhos nao possuem valor em reais no resumo financeiro.
enum class NaturezaAbrigo { DINHEIRO, PRODUTOS, BICHINHO, DESPESA }
data class LancamentoAbrigo(
    val id: String = UUID.randomUUID().toString(),
    val descricao: String,
    val natureza: NaturezaAbrigo,
    val data: String,
    val centavos: Long = 0,
    val quantidade: String = "",
    val categoria: String = "",
    val animalId: String? = null,
    val observacoes: String = "",
    // Mantém o horário separado da data para preservar os filtros mensais.
    val horario: String = ""
) : Serializable

// Ofertas de exemplo nao incluem links reais nem promessas comerciais.
data class OfertaAbrigo(
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val categoria: String,
    val parceiro: String,
    val descricao: String,
    val link: String = "",
    val tipo: String = "Produto",
    val centavos: Long? = null,
    val imagemUri: String? = null,
    // Identifica o público do produto: Cães, Gatos ou Ambos.
    // Vazio significa que essa informação ainda não foi cadastrada.
    val especieAlvo: String = ""
) : Serializable

// Define os cargos disponíveis no cadastro, na edição e nos filtros da equipe.
enum class FuncaoAbrigo(val titulo: String) {
    // Administração do abrigo.
    ADMIN("Administrador"),
    // Gestão das atividades do abrigo.
    GESTOR("Gestor"),
    // Cuidados diários dos animais.
    CUIDADOR("Cuidador"),
    // Atendimento veterinário.
    VETERINARIO("Veterinário"),
    // Treinamento e comportamento dos animais.
    ADESTRADOR("Adestrador"),
    // Colaboração nas atividades autorizadas.
    VOLUNTARIO("Voluntário")
}

// Guarda os dados do integrante e preserva os cadastros antigos com valores padrão.
data class MembroAbrigo(
    // Identifica o membro na lista e nas permissões.
    val id: String,
    // Guarda o nome completo exibido na equipe.
    val nome: String,
    // Define a função do integrante no abrigo.
    val funcao: FuncaoAbrigo,
    // Preserva o sobrenome informado no cadastro.
    val sobrenome: String = "",
    // Guarda o e-mail de contato do integrante.
    val email: String = "",
    // Guarda o telefone, quando informado.
    val telefone: String = "",
    // Guarda o endereço, quando informado.
    val endereco: String = "",
    // Guarda o endereço da fotografia selecionada no aparelho.
    val fotoUri: String? = null
) : Serializable

data class ConviteAbrigo(val id: String, val email: String, val funcao: FuncaoAbrigo, val codigo: String) : Serializable
// Guarda o perfil do próprio abrigo sem alterar a ordem dos campos antigos.
data class InformacoesAbrigo(
    // Identifica o abrigo no aplicativo.
    val nome: String = "Abrigo Esperança",
    // Guarda o telefone cadastrado.
    val telefone: String = "",
    // Guarda a rede social informada.
    val redeSocial: String = "",
    // Define a capacidade usada no cálculo de vagas.
    val capacidade: Int = 60,
    // Guarda a apresentação do abrigo.
    val descricao: String = "",
    // Identifica o bairro.
    val bairro: String = "",
    // Guarda a cidade e o estado informados.
    val cidade: String = "",
    // Guarda o endereço de contato.
    val endereco: String = "",
    // Guarda o e-mail do abrigo.
    val email: String = "",
    // Guarda a foto escolhida para a capa.
    val fotoUri: String? = null
) : Serializable

// Uma unica colecao compartilhada mantem todas as novas telas coerentes durante a sessao.
data class DadosRestantes(
    val compromissos: List<CompromissoAbrigo> = emptyList(),
    val agendaDataAlvo: String? = null,
    val lancamentos: List<LancamentoAbrigo> = emptyList(),
    val ofertas: List<OfertaAbrigo> = emptyList(),
    val abrigo: InformacoesAbrigo = InformacoesAbrigo(),
    val membros: List<MembroAbrigo> = listOf(MembroAbrigo("helena", "Helena", FuncaoAbrigo.ADMIN),
        MembroAbrigo("ana", "Ana", FuncaoAbrigo.CUIDADOR), MembroAbrigo("paulo", "Paulo", FuncaoAbrigo.VOLUNTARIO)),
    val usuarioLocalId: String = "helena",
    val convites: List<ConviteAbrigo> = emptyList(),
    val compromissoId: String? = null, val editarCompromissoId: String? = null,
    val ofertaId: String? = null, val membroId: String? = null,
    val modeloPostagem: String = "Procura um lar"
) : Serializable

// Inicializa exemplos explicitos sem fabricar relacoes para bichinhos cadastrados depois.
fun dadosRestantesExemplo(animais: List<AnimalExemplo>): DadosRestantes {
    val lua = animais.firstOrNull { it.nome == "Lua" }?.id
    val bento = animais.firstOrNull { it.nome == "Bento" }?.id
    val entradas = listOf(60000L, 80000L, 50000L, 90000L)
    val saidas = listOf(30000L, 40000L, 32000L, 40000L)
    val dias = listOf("03", "10", "17", "24")
    val movimentos = entradas.indices.flatMap { i -> listOf(
        LancamentoAbrigo(descricao = "Doação", natureza = NaturezaAbrigo.DINHEIRO,
            data = "${dias[i]}/11/2026", centavos = entradas[i]),
        LancamentoAbrigo(descricao = "Despesa de ração", natureza = NaturezaAbrigo.DESPESA,
            data = "${dias[i]}/11/2026", centavos = saidas[i], categoria = "Outros"))
    } + LancamentoAbrigo(descricao = "Ração recebida", natureza = NaturezaAbrigo.PRODUTOS,
        data = "07/11/2026", quantidade = "20 kg")
    return DadosRestantes(compromissos = listOf(
        CompromissoAbrigo(titulo = "Vacina da Lua", tipo = "Cuidados", data = "07/11/2026", horario = "14:00", animalId = lua,
            local = "Clínica parceira", observacoes = "Levar a carteirinha de vacinação."),
        CompromissoAbrigo(titulo = "Visita ao Bento", tipo = "Visitas", data = "07/11/2026", horario = "16:00", animalId = bento)),
        lancamentos = movimentos, ofertas = listOf(
            OfertaAbrigo(titulo = "Ração para abrigo", categoria = "Alimentação", parceiro = "Marca parceira",
                descricao = "Consulte tamanhos, disponibilidade e valores com o parceiro."),
            OfertaAbrigo(titulo = "Consulta veterinária", categoria = "Saúde", parceiro = "Clínica parceira", tipo = "Serviço",
                descricao = "Consulte a região atendida e as condições do serviço."),
            OfertaAbrigo(titulo = "Banho e tosa", categoria = "Serviços", parceiro = "Atendimento local", tipo = "Serviço",
                descricao = "Consulte a disponibilidade e as condições do serviço."),
            OfertaAbrigo(titulo = "Medicamentos", categoria = "Saúde", parceiro = "Produtos veterinários",
                descricao = "Consulte as condições com o parceiro. Não substitui orientação veterinária.")))
}

// Rejeita datas inexistentes e texto extra, sem normalizar 31/02 para outro mes.
fun dataValidaAbrigo(texto: String): Boolean {
    if (!Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(texto)) return false
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
    val posicao = ParsePosition(0)
    return formato.parse(texto, posicao) != null && posicao.index == texto.length && texto.takeLast(4).toInt() in 1900..2200
}

// Retorna uma chave ordenavel sem depender de java.time ou de desugaring.
fun chaveDataAbrigo(data: String): String = data.takeLast(4) + data.substring(3, 5) + data.take(2)
fun hojeAbrigo(): String = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(java.util.Date())

// Recebe reais sem separador de milhares; usa inteiros para evitar arredondamento binario.
fun centavosAbrigo(texto: String): Long? {
    if (!Regex("[0-9]{1,9}(,[0-9]{1,2})?").matches(texto.trim())) return null
    return runCatching { BigDecimal(texto.trim().replace(',', '.')).movePointRight(2).longValueExact() }
        .getOrNull()?.takeIf { it > 0 }
}

// Converte o valor monetario para a apresentacao brasileira.
fun reaisAbrigo(valor: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    .format(BigDecimal.valueOf(valor, 2))

// Agrupa registros pela mesma competencia usada no resumo e no relatorio.
fun doPeriodoAbrigo(data: String, mes: Int, ano: Int): Boolean =
    dataValidaAbrigo(data) && data.substring(3, 5).toInt() == mes && data.takeLast(4).toInt() == ano

// Verifica somente a permissao demonstrativa da pessoa local, sem substituir uma API segura.
fun administradorAbrigo(dados: DadosRestantes): Boolean = dados.membros.any { it.id == dados.usuarioLocalId && it.funcao == FuncaoAbrigo.ADMIN }
package com.example.gerenciacao

import android.content.Context

// Habilita os animais fictícios usados na apresentação do protótipo.
// Quando houver integração real, substitua a origem dos dados pela API.
const val MODO_DEMONSTRACAO = true

// Cria os animais com fotos locais, sem consultar banco ou rede.
// Os IDs fixos preservam os vínculos entre animal, ficha e compromissos.
fun criarAnimaisDemonstracao(contexto: Context): ArrayList<AnimalExemplo> {
    // Permite desativar a carga de exemplos sem excluir telas.
    if (!MODO_DEMONSTRACAO) return arrayListOf()

    // Monta um endereço de imagem que os carregadores atuais já conseguem abrir.
    fun fotoDrawable(nome: String): String =
        "android.resource://" + contexto.packageName + "/drawable/" + nome

    return arrayListOf(
        AnimalExemplo(
            // Identificador estável deste animal fictício.
            id = "demo-lua",
            // Nome exibido na lista e na ficha.
            nome = "Lua",
            // Idade usada na demonstração.
            idade = "2 anos",
            // Situação apresentada na etiqueta.
            situacao = "Disponível",
            // Categoria utilizada pelo filtro de espécies.
            especie = "Gatos",
            // Raça informada na ficha.
            raca = "SRD",
            // Sexo apresentado na ficha.
            sexo = "Fêmea",
            // Situação reprodutiva usada na demonstração.
            castrado = true,
            // Imagem já existente no drawable; troque iconcat pela foto desejada.
            fotoUri = fotoDrawable("gato1"),
            // Descrição fictícia exibida na ficha individual.
            sobre = "Lua é uma gatinha carinhosa, curiosa e tranquila. Gosta de companhia e de brincar.",
            // Não inventa registros clínicos para a demonstração.
            historicoMedico = "",
            // Mantém a ficha visível na lista de animais.
            arquivado = false
        ),
        AnimalExemplo(
            // Identificador estável do segundo animal fictício.
            id = "demo-bento",
            // Nome apresentado nas telas.
            nome = "Bento",
            // Idade usada no exemplo.
            idade = "5 anos",
            // Situação apresentada na etiqueta.
            situacao = "Disponível",
            // Categoria do filtro de espécies.
            especie = "Cães",
            // Raça apresentada na ficha.
            raca = "SRD",
            // Sexo apresentado na ficha.
            sexo = "Macho",
            // Situação reprodutiva do exemplo.
            castrado = true,
            // Imagem local já existente; troque icondog pela foto do Bento.
            fotoUri = fotoDrawable("dog1"),
            // Descrição ilustrativa do animal.
            sobre = "Bento é um cão companheiro e sociável. Gosta de passear e receber carinho.",
            // Mantém o histórico vazio até haver informação.
            historicoMedico = "",
            // Mantém a ficha ativa na lista.
            arquivado = false
        ),
        AnimalExemplo(
            // Preserva Mel como exemplo para o protótipo atual.
            id = "demo-mel",
            // Nome apresentado na lista.
            nome = "Mel",
            // Idade ilustrativa.
            idade = "1 ano",
            // Demonstra outra situação da ficha.
            situacao = "Visita marcada",
            // Permite filtrar por cães.
            especie = "Cães",
            // Raça utilizada no exemplo.
            raca = "SRD",
            // Sexo utilizado no exemplo.
            sexo = "Fêmea",
            // Castração não informada neste exemplo.
            castrado = null,
            // Mel permanece sem imagem até você fornecer uma foto específica.
            fotoUri = null,
            // Descrição fictícia da ficha.
            sobre = "Mel é brincalhona e gosta de companhia.",
            // Mantém o histórico sem dados fictícios de atendimento.
            historicoMedico = "",
            // Mantém a ficha visível.
            arquivado = false
        ),
        AnimalExemplo(
            // Mantém o animal utilizado no processo demonstrativo de adoção.
            id = "demo-tico",
            // Nome utilizado pelo protótipo.
            nome = "Tico",
            // Idade ilustrativa.
            idade = "3 anos",
            // Preserva a situação adotado já usada no aplicativo.
            situacao = "Adotado",
            // Categoria ilustrativa.
            especie = "Cães",
            // Raça ilustrativa.
            raca = "SRD",
            // Sexo ilustrativo.
            sexo = "Macho",
            // Castração não informada.
            castrado = null,
            // Aguarda uma foto específica para este animal.
            fotoUri = null,
            // Descrição do registro demonstrativo.
            sobre = "Tico é um animal fictício usado para demonstrar o acompanhamento da adoção.",
            // Mantém o histórico vazio.
            historicoMedico = "",
            // Ficha continua visível para demonstrar o filtro Adotado.
            arquivado = false
        )
    )
}

// Cria o perfil com uma foto inicial apenas para a demonstração.
fun criarPerfilDemonstracao(contexto: Context): DadosMeuPerfil {
    return DadosMeuPerfil(
        // Usa a imagem emy.jpeg que já existe no drawable.
        // Troque "emy" pelo nome de outra foto, sem a extensão.
        fotoUri = if (MODO_DEMONSTRACAO) {
            "android.resource://" + contexto.packageName + "/drawable/persona"
        } else {
            null
        }
    )
}

// Cadastra a vacinação fictícia usada na apresentação do protótipo.
fun adicionarCompromissoApresentacao(
    // Recebe os registros atuais para preservar os demais compromissos.
    dados: DadosRestantes,
    // Permite relacionar o compromisso à ficha e à foto da Lua.
    animais: List<AnimalExemplo>
): DadosRestantes {
    // Ativa este cadastro automático somente no modo de demonstração.
    if (!MODO_DEMONSTRACAO) return dados

    // Usa um identificador fixo para evitar compromissos duplicados.
    val idExemplo = "demo-vacinacao-lua-04122026"

    // Preserva o compromisso existente e as alterações feitas nele.
    if (dados.compromissos.any { it.id == idExemplo }) return dados

    val compromisso = CompromissoAbrigo(
        // Identifica este compromisso de demonstração.
        id = idExemplo,
        // Define o nome que aparece na agenda e no aviso da tela inicial.
        titulo = "Vacinação da Lua",
        // Aplica a categoria Cuidados e sua identificação verde.
        tipo = "Cuidados",
        // Marca o compromisso para 4 de dezembro de 2026.
        data = "04/12/2026",
        // Define o horário ilustrativo da vacinação.
        horario = "14:00",
        // Vincula a Lua cadastrada para exibir sua foto no compromisso.
        animalId = animais.firstOrNull {
            it.id == "demo-lua" || it.nome.equals("Lua", ignoreCase = true)
        }?.id,
        // Define um local ilustrativo para a demonstração.
        local = "Abrigo Esperança",
        // Identifica o registro como fictício.
        observacoes = "Vacinação fictícia para demonstração do aplicativo."
    )

    // Acrescenta o compromisso sem apagar os outros dados.
    return dados.copy(compromissos = dados.compromissos + compromisso)

}

// Acrescenta a foto fictícia do abrigo somente na demonstração.
fun adicionarFotoAbrigoDemonstracao(
    // Identifica os recursos de imagem do aplicativo.
    contexto: Context,
    // Recebe os dados atuais para preservar os cadastros.
    dados: DadosRestantes
): DadosRestantes {
    // Preserva uma foto já cadastrada e não interfere fora da demonstração.
    if (!MODO_DEMONSTRACAO || !dados.abrigo.fotoUri.isNullOrBlank()) {
        return dados
    }

    return dados.copy(
        // Mantém as informações existentes do abrigo.
        abrigo = dados.abrigo.copy(
            // Usa a imagem fictícia salva no drawable.
            fotoUri = "android.resource://" +
                    contexto.packageName +
                    "/" + R.drawable.abrigopersona
        )
    )
}


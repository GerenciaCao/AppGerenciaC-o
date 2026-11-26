package com.example.gerenciacao

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Usa os mesmos campos na criação e na edição da ficha.
@Composable
fun TelaNovaFicha(
    // Preenche o formulário quando um animal existente está sendo editado.
    animalInicial: AnimalExemplo? = null,
    // Retorna à origem do formulário.
    aoVoltar: () -> Unit,
    // Abre o início.
    aoInicio: () -> Unit,
    // Abre a agenda.
    aoAgenda: () -> Unit,
    // Abre a lista de animais.
    aoAnimais: () -> Unit,
    // Abre as ações rápidas.
    aoAdicionar: () -> Unit,
    // Salva a ficha no estado compartilhado do aplicativo.
    aoSalvar: (AnimalExemplo) -> Unit,
    // Preserva a ação de arquivar durante a edição.
    aoArquivar: ((String) -> Unit)? = null
) {
    // Guarda os dados básicos durante a edição e as rotações do aparelho.
    var nome by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.nome.orEmpty()) }
    // Recupera a situação na edição e inicia novos cadastros como Disponível.
    var situacao by rememberSaveable(animalInicial?.id) {
        mutableStateOf(
            animalInicial?.situacao?.takeIf { it.isNotBlank() } ?: "Disponível"
        )
    }
    var especie by rememberSaveable(animalInicial?.id) {
        mutableStateOf(animalInicial?.especie?.takeUnless { it == "Não informada" }.orEmpty())
    }
    var sexo by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.sexo ?: "Não informado") }
    var raca by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.raca.orEmpty()) }
    var idade by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.idade.orEmpty()) }
    var porte by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.porte.orEmpty()) }
    var cor by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.corPelagem.orEmpty()) }
    var castrado by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.castrado) }
    // Guarda as informações de saúde sem inventar diagnósticos.
    var vacinacoes by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.vacinacoes.orEmpty()) }
    var condicoes by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.condicoesSaude.orEmpty()) }
    var medicamentos by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.medicamentos.orEmpty()) }
    // Guarda a apresentação e as observações do animal.
    var descricao by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.sobre.orEmpty()) }
    var comportamento by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.comportamento.orEmpty()) }
    var observacoes by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.observacoes.orEmpty()) }
    // Preserva o histórico antigo caso a ficha já o possua.
    var historico by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.historicoMedico.orEmpty()) }
    var fotoUri by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.fotoUri) }
    var tentou by rememberSaveable(animalInicial?.id) { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val contexto = LocalContext.current
    // Usa a mesma correção de orientação aplicada na ficha individual.
    val estadoFotoFormulario = lembrarFotoOriginalFicha(fotoUri)
    val miniatura = estadoFotoFormulario.imagem

    // Preserva a imagem original para futuros reenquadramentos.
    var fotoOriginalUri by rememberSaveable(animalInicial?.id) {
        mutableStateOf(
            animalInicial?.fotoOriginalUri ?: animalInicial?.fotoUri
        )
    }

    // Guarda a foto que está sendo ajustada, sem substituir a anterior.
    var fotoParaEnquadrar by rememberSaveable(animalInicial?.id) {
        mutableStateOf<String?>(null)
    }
    // Controla o menu de escolha entre câmera e galeria.
    var mostrarOpcoesFoto by rememberSaveable(animalInicial?.id) {
        mutableStateOf(false)
    }

// Preserva o destino da captura enquanto a câmera está aberta.
    var fotoCameraPendente by rememberSaveable(animalInicial?.id) {
        mutableStateOf<String?>(null)
    }

    // Detecta alterações em todos os campos antes de permitir sair sem salvar.
    val alterado =
        situacao != (animalInicial?.situacao?.takeIf { it.isNotBlank() } ?: "Disponível") ||
                nome != animalInicial?.nome.orEmpty() ||
                especie != animalInicial?.especie?.takeUnless { it == "Não informada" }.orEmpty() ||
            sexo != (animalInicial?.sexo ?: "Não informado") ||
            raca != animalInicial?.raca.orEmpty() || idade != animalInicial?.idade.orEmpty() ||
            porte != animalInicial?.porte.orEmpty() || cor != animalInicial?.corPelagem.orEmpty() ||
            castrado != animalInicial?.castrado || vacinacoes != animalInicial?.vacinacoes.orEmpty() ||
            condicoes != animalInicial?.condicoesSaude.orEmpty() || medicamentos != animalInicial?.medicamentos.orEmpty() ||
            descricao != animalInicial?.sobre.orEmpty() || comportamento != animalInicial?.comportamento.orEmpty() ||
            observacoes != animalInicial?.observacoes.orEmpty() || historico != animalInicial?.historicoMedico.orEmpty() ||
            fotoUri != animalInicial?.fotoUri
    val proteger = protegerRascunhoNoCabecalho(alterado)
    val abrirLoja = LocalAbrirLoja.current
    val abrirFinancas = LocalAbrirFinancas.current
    // Exige somente o nome; foto e demais informações podem ficar sem preenchimento.
    val erroNome = if (nome.trim().none { it.isLetter() }) "Informe o nome do animal." else null
    val escolherFoto = rememberLauncherForActivityResult(
        // Abre o seletor de documentos para manter o acesso à foto depois do cadastro.
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }// Abre o editor antes de aplicar a nova foto.
                .onSuccess { fotoParaEnquadrar = uri.toString() }
                .onFailure { aviso = "Não foi possível manter o acesso à foto. Escolha outra imagem." }
        }
    }

// Recebe o resultado do aplicativo de câmera.
    val tirarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        val endereco = fotoCameraPendente
        fotoCameraPendente = null

        if (sucesso && endereco != null) {
            // Abre o mesmo editor utilizado pelas imagens da galeria.
            fotoParaEnquadrar = endereco
        } else if (endereco != null) {
            // Remove o arquivo vazio quando a captura é cancelada.
            runCatching {
                contexto.contentResolver.delete(
                    Uri.parse(endereco),
                    null,
                    null
                )
            }
        }
    }

// Mostra as opções ao tocar na área da foto ou em Alterar imagem.
    if (mostrarOpcoesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcoesFoto = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Adicionar foto",
                    fontWeight = FontWeight.Bold,
                    color = MarromPadraoApp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            mostrarOpcoesFoto = false

                            // Guarda o arquivo original para permitir reenquadrar depois.
                            var arquivoCriado: File? = null

                            try {
                                val pasta = File(
                                    contexto.filesDir,
                                    "fotos_camera"
                                )

                                check(pasta.exists() || pasta.mkdirs())

                                val arquivo = File.createTempFile(
                                    "animal_",
                                    ".jpg",
                                    pasta
                                )
                                arquivoCriado = arquivo

                                // Fornece à câmera acesso somente ao arquivo da captura.
                                val uri = FileProvider.getUriForFile(
                                    contexto,
                                    "${contexto.packageName}.fotos",
                                    arquivo
                                )

                                fotoCameraPendente = uri.toString()
                                tirarFoto.launch(uri)
                            } catch (erro: Exception) {
                                // Mantém a foto anterior se a câmera não puder abrir.
                                arquivoCriado?.delete()
                                fotoCameraPendente = null
                                aviso = "Não foi possível abrir a câmera. Você pode escolher uma foto da galeria."
                            }
                        },
                        enabled = fotoCameraPendente == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MarromPadraoApp,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Tirar foto",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            mostrarOpcoesFoto = false

                            // Mantém o seletor de imagens que já existe.
                            escolherFoto.launch(arrayOf("image/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Escolher da galeria",
                            fontSize = 16.sp,
                            color = MarromPadraoApp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { mostrarOpcoesFoto = false }
                ) {
                    Text(
                        text = "Cancelar",
                        color = MarromPadraoApp
                    )
                }
            }
        )
    }

    // Apresenta o editor quando uma imagem é escolhida.
    fotoParaEnquadrar?.let { original ->
        DialogoEnquadrarFotoAnimal(
            // Sempre permite ajustar a imagem original.
            origem = original,
            // Mantém a foto anterior quando o usuário cancela.
            aoCancelar = {
                fotoParaEnquadrar = null
            },
            // Atualiza a prévia somente depois da confirmação.
            aoConfirmar = { recorte ->
                fotoOriginalUri = original
                fotoUri = recorte
                fotoParaEnquadrar = null
            }
        )
    }

    // Aplica a mesma confirmação ao botão do sistema e às saídas da tela.
    BackHandler { proteger(aoVoltar) }
    CompositionLocalProvider(
        // Protege os dados ao navegar pela barra inferior.
        LocalAbrirLoja provides { proteger(abrirLoja) },
        LocalAbrirFinancas provides { proteger(abrirFinancas) },
    ) {
        MolduraPadraoApp(
            // Mantém a identidade visual das telas internas.
            containerColor = FundoPadraoApp,
            // Protege as saídas pelo perfil e pelos três pontos.
            interceptarCabecalho = proteger,
            // Reutiliza a navegação inferior do aplicativo.
            bottomBar = {
                MenuInferiorInicio(
                    avisar = { aviso = it },
                    selecionado = "Animais",
                    aoInicio = { proteger(aoInicio) },
                    aoAnimais = { proteger(aoAnimais) },
                    aoAdicionar = { proteger(aoAdicionar) },
                    aoAgenda = { proteger(aoAgenda) }
                )
            }
        ) { espacos ->
            Column(
                // Permite preencher todo o formulário sem cortar campos pelo teclado.
                modifier = Modifier.fillMaxSize().padding(espacos).imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa os campos de cada seção.
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TituloPaginaPadrao(
                    // Mantém os títulos distintos para criação e edição.
                    titulo = if (animalInicial == null) "Nova ficha" else "Editar ficha",
                    // Retorna com confirmação de descarte quando necessária.
                    aoVoltar = { proteger(aoVoltar) }
                )
                Surface(
                    // Permite escolher ou trocar a imagem tocando na área da foto.
                    onClick = { mostrarOpcoesFoto = true },
                    // Mantém um espaço uniforme para a imagem no formulário.
                    // Usa a mesma proporção do editor e da ficha individual.
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                    // Mantém os cantos arredondados.
                    shape = RoundedCornerShape(18.dp),
                    // Usa o azul padrão atrás da imagem transparente do animal.
                    color = Color(0xFFB7E7FC)
                ) {
                    if (miniatura != null) {
                        Image(
                            // Mostra a imagem selecionada, sem alterar seu arquivo.
                            bitmap = miniatura,
                            // Explica a ação de troca.
                            contentDescription = "Foto do animal; toque para alterar",
                            // Ocupa o espaço da imagem.
                            modifier = Modifier.fillMaxSize(),
                            // Usa o mesmo comportamento de exibição da ficha individual.
                            contentScale = if (estadoFotoFormulario.transparente) {
                                ContentScale.Fit
                            } else {
                                ContentScale.Crop
                            }
                        )
                    } else {
                        Column(
                            // Centraliza o controle de escolha da imagem.
                            modifier = Modifier.fillMaxSize(),
                            // Centraliza os elementos horizontalmente.
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Separa o ícone da orientação.
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                        ) {
                            Icon(
                                // Reutiliza o ícone de câmera existente.
                                painter = painterResource(R.drawable.ficha_camera),
                                // O texto abaixo explica a ação.
                                contentDescription = null,
                                // Define uma medida legível para a câmera.
                                modifier = Modifier.size(28.dp),
                                // Usa o marrom dos controles.
                                tint = MarromPadraoApp
                            )
                            Text("Adicionar imagem", color = MarromPadraoApp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (fotoUri != null) {
                    TextButton(
                        // Mantém acessível a troca da foto.
                        onClick = { escolherFoto.launch(arrayOf("image/*")) }
                    ) { Text("Alterar imagem", color = MarromPadraoApp)}
                    if (fotoUri != null) {
                        TextButton(
                            // Reabre a foto original para permitir outro enquadramento.
                            onClick = {
                                fotoParaEnquadrar = fotoOriginalUri ?: fotoUri
                            }
                        ) {
                            Text(
                                // Identifica o ajuste manual.
                                text = "Editar enquadramento",
                                // Mantém a cor dos controles.
                                color = MarromPadraoApp
                            )
                        }
                     }
                }
                SecaoFormularioAnimal("Informações básicas")
                CampoFormularioAnimal("Nome", nome, { nome = it }, "Ex.: Lua", erro = if (tentou) erroNome else null)
                SeletorRestante(
                    rotulo = "Situação do animal",
                    valor = situacao,
                    // Mantém as situações já utilizadas nas fichas e nos filtros.
                    opcoes = (
                            listOf(
                                "Disponível",
                                "No abrigo",
                                "Visita marcada",
                                "Em tratamento",
                                "Adotado"
                            ) + situacao
                            ).distinct().map { it to it },
                    selecionar = { situacao = it }
                )

                Text(
                    text = when (situacao) {
                        "Disponível" -> "Pode ser adotado."
                        "No abrigo" -> "Está acolhido, mas ainda não está disponível para adoção."
                        "Visita marcada" -> "Tem uma visita de adoção agendada."
                        "Em tratamento" -> "Está recebendo cuidados de saúde."
                        "Adotado" -> "Já foi adotado e não ocupa uma vaga no abrigo."
                        else -> "Você pode atualizar a situação ao editar a ficha."
                    },
                    fontSize = 13.sp,
                    color = MarromPadraoApp
                )
                SeletorRestante(
                    // Mostra os nomes amigáveis e salva os valores esperados pelos filtros.
                    rotulo = "Espécie",
                    valor = when (especie) { "Cães" -> "Cão"; "Gatos" -> "Gato"; "" -> "Selecionar"; else -> especie },
                    opcoes = listOf("" to "Não informado", "Cães" to "Cão", "Gatos" to "Gato", "Outros" to "Outro"),
                    selecionar = { especie = it }
                )
                SeletorRestante("Sexo", sexo, listOf("Fêmea" to "Fêmea", "Macho" to "Macho", "Não informado" to "Não informado")) { sexo = it }
                CampoFormularioAnimal("Raça", raca, { raca = it }, "Ex.: SRD")
                CampoFormularioAnimal("Idade", idade, { idade = it }, "Ex.: 2 anos")
                SeletorRestante("Porte", porte.ifBlank { "Selecionar" },
                    listOf("" to "Não informado", "Pequeno" to "Pequeno", "Médio" to "Médio", "Grande" to "Grande")) { porte = it }
                CampoFormularioAnimal("Cor", cor, { cor = it }, "Ex.: Caramelo")

                SecaoFormularioAnimal("Saúde e cuidados")
                SeletorRestante("Castração", when (castrado) {
                    true -> if (sexo == "Fêmea") "Castrada" else "Castrado"
                    false -> if (sexo == "Fêmea") "Não castrada" else "Não castrado"
                    null -> "Não informado"
                }, listOf("sim" to "Sim", "nao" to "Não", "" to "Não informado")) {
                    castrado = when (it) { "sim" -> true; "nao" -> false; else -> null }
                }
                CampoFormularioAnimal("Vacinações", vacinacoes, { vacinacoes = it }, "Informe as vacinas, se houver...", longo = true)
                CampoFormularioAnimal("Condições de saúde", condicoes, { condicoes = it }, "Alergias, doenças, necessidades especiais", longo = true)
                CampoFormularioAnimal("Medicamentos em uso", medicamentos, { medicamentos = it }, "Informe os medicamentos, se houver...", longo = true)
                if (!animalInicial?.historicoMedico.isNullOrBlank()) {
                    // Mantém os registros anteriores acessíveis durante a edição.
                    CampoFormularioAnimal("Histórico anterior", historico, { historico = it }, "Registros já cadastrados", longo = true)
                }

                SecaoFormularioAnimal("Descrição e observações")
                CampoFormularioAnimal("Descrição", descricao, { descricao = it }, "Informe uma breve descrição do animal", longo = true)
                CampoFormularioAnimal("Observações adicionais", observacoes, { observacoes = it }, "Outras informações importantes...", longo = true)
                Row(
                    // Mantém as ações finais lado a lado, como na referência.
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    // Separa Cancelar e Salvar.
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        // Volta sem salvar depois da confirmação, quando necessária.
                        onClick = { proteger(aoVoltar) },
                        // Divide a largura com o botão de salvar.
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        // Mantém o formato dos controles do aplicativo.
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Cancelar", color = MarromPadraoApp) }
                    Button(
                        // Valida os dados obrigatórios e salva todos os campos.
                        onClick = {
                            tentou = true
                            if (erroNome == null) {
                                val idadeFinal = idade.trim().toIntOrNull()?.let {
                                    it.toString() + if (it == 1) " ano" else " anos"
                                } ?: idade.trim()
                                // Preserva o ID, a situação e o arquivamento durante a edição.
                                val base = animalInicial ?: AnimalExemplo(
                                    nome = nome.trim(), idade = idadeFinal, situacao = situacao
                                )
                                aoSalvar(base.copy(
                                    // Salva a situação tanto no cadastro quanto na edição.
                                    situacao = situacao,
                                    nome = nome.trim(), especie = especie, sexo = sexo,
                                    raca = raca.trim(), idade = idadeFinal, porte = porte,
                                    corPelagem = cor.trim(), castrado = castrado, fotoUri = fotoUri,
                                    // Guarda o original junto da versão enquadrada.
                                    fotoOriginalUri = fotoOriginalUri,
                                    vacinacoes = vacinacoes.trim(), condicoesSaude = condicoes.trim(),
                                    medicamentos = medicamentos.trim(), sobre = descricao.trim(),
                                    comportamento = comportamento.trim(), observacoes = observacoes.trim(),
                                    historicoMedico = historico.trim()
                                ))
                            }
                        },
                        // Divide a largura com Cancelar e mantém uma área de toque confortável.
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        // Mantém o formato do botão.
                        shape = RoundedCornerShape(14.dp),
                        // Usa a cor de destaque do aplicativo.
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB414),
                            contentColor = TextoPadraoApp
                        )
                    ) { Text("Salvar", fontWeight = FontWeight.Bold) }
                }

            }
        }
    }

    // Mostra mensagens sem apagar os dados preenchidos.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Destaca cada grupo sem criar uma caixa para o formulário inteiro.
@Composable
private fun SecaoFormularioAnimal(titulo: String) {
    Text(
        // Identifica o grupo de campos.
        text = titulo,
        // Separa as seções.
        modifier = Modifier.padding(top = 12.dp),
        // Mantém a hierarquia dos títulos secundários.
        fontSize = 20.sp,
        // Destaca o título da seção.
        fontWeight = FontWeight.Bold,
        // Usa o marrom dos títulos.
        color = TextoPadraoApp
    )
}

// Mantém os rótulos externos e campos que crescem conforme a necessidade.
@Composable
private fun CampoFormularioAnimal(
    titulo: String,
    valor: String,
    mudar: (String) -> Unit,
    dica: String,
    longo: Boolean = false,
    erro: String? = null
) {
    Column(
        // Mantém o rótulo próximo do seu campo.
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MarromPadraoApp)
        OutlinedTextField(
            // Mostra o valor atual.
            value = valor,
            // Evita textos excessivos sem limitar as observações a uma linha.
            onValueChange = { mudar(it.take(if (longo) 4000 else 150)) },
            // Ocupa a largura do formulário sem uma altura fixa.
            modifier = Modifier.fillMaxWidth(),
            // Permite textos maiores nos campos descritivos.
            singleLine = !longo,
            // Reserva mais espaço para os textos de saúde e apresentação.
            minLines = if (longo) 3 else 1,
            // Mostra uma orientação quando o campo está vazio.
            placeholder = { Text(dica, fontSize = 14.sp) },
            // Indica os erros junto ao campo.
            isError = erro != null,
            // Mantém os cantos arredondados.
            shape = RoundedCornerShape(14.dp),
            // Usa a paleta da referência sem alterar as demais telas.
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFF3E5),
                unfocusedContainerColor = Color(0xFFFFF3E5),
                focusedBorderColor = MarromPadraoApp,
                unfocusedBorderColor = Color(0xFFEADFD2),
                cursorColor = MarromPadraoApp
            )
        )
        if (erro != null) Text(erro, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
    }
}

// Permite conferir todos os campos no editor.
@Preview(
    // Identifica a prévia do formulário.
    name = "Nova ficha completa",
    // Mostra o fundo da tela.
    showBackground = true,
    // Define a largura de referência.
    widthDp = 412,
    // Define a altura de referência.
    heightDp = 917
)
@Composable
private fun PreviaNovaFichaCompleta() {
    MaterialTheme {
        TelaNovaFicha(
            aoVoltar = {}, aoInicio = {}, aoAgenda = {}, aoAnimais = {},
            aoAdicionar = {}, aoSalvar = {}
        )
    }
}
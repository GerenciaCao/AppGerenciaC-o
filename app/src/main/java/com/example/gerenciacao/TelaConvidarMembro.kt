package com.example.gerenciacao

import android.content.Intent
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mostra a descrição de cada cargo no perfil e na tela de permissões.
// Estas descrições não concedem acesso por conta própria.
fun descricaoFuncaoAbrigo(funcao: FuncaoAbrigo): String = when (funcao) {
    // Descreve a administração.
    FuncaoAbrigo.ADMIN ->
        "Gerencia dados, equipe e permissões do abrigo."

    // Descreve a gestão.
    FuncaoAbrigo.GESTOR ->
        "Organiza as atividades e acompanha a rotina do abrigo."

    // Descreve os cuidados diários.
    FuncaoAbrigo.CUIDADOR ->
        "Acompanha os cuidados e a rotina dos animais."

    // Descreve o atendimento clínico.
    FuncaoAbrigo.VETERINARIO ->
        "Acompanha a saúde e os registros clínicos dos animais."

    // Descreve o acompanhamento comportamental.
    FuncaoAbrigo.ADESTRADOR ->
        "Acompanha o comportamento e o treinamento dos animais."

    // Descreve a colaboração voluntária.
    FuncaoAbrigo.VOLUNTARIO ->
        "Colabora nas atividades autorizadas pelo abrigo."
}

// Mantém o nome da função para preservar a navegação já existente.
@Composable
fun TelaConvidarMembro(
    // Recebe os dados da equipe e as ações de navegação.
    ctx: ContextoAbrigo,
    // Preenche o formulário quando um perfil existente estiver sendo editado.
    membroEdicaoId: String? = null
) {
    // Localiza o cadastro usado como base da edição.
    val original = ctx.dados.membros.firstOrNull { it.id == membroEdicaoId }
    // Preserva o nome enquanto o formulário estiver aberto.
    var nome by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.nome.orEmpty()) }
    // Preserva o e-mail digitado.
    var email by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.email.orEmpty()) }
    // Preserva o telefone digitado.
    var telefone by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.telefone.orEmpty()) }
    // Preserva o endereço digitado.
    var endereco by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.endereco.orEmpty()) }
    // Aguarda a escolha de um cargo antes de salvar.
    var funcaoNome by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.funcao?.name.orEmpty()) }
    // Guarda somente o endereço da foto, sem salvar a imagem no estado da tela.
    var fotoUri by rememberSaveable(membroEdicaoId) { mutableStateOf(original?.fotoUri) }
    // Guarda o aviso de validação ou de leitura da imagem.
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    // Identifica quando o formulário contém dados que ainda não foram salvos.
    val alterado = nome != original?.nome.orEmpty() ||
            email != original?.email.orEmpty() ||
            telefone != original?.telefone.orEmpty() ||
            endereco != original?.endereco.orEmpty() ||
            funcaoNome != original?.funcao?.name.orEmpty() ||
            fotoUri != original?.fotoUri
    // Reutiliza a confirmação de descarte do aplicativo.
    val protegerSaida = protegerRascunhoNoCabecalho(alterado)
    // Acessa o seletor de arquivos do Android.
    val contexto = LocalContext.current
    // Carrega uma miniatura da foto fora da interface.
    val foto = lembrarFotoMeuPerfil(fotoUri)
    // Mantém a restrição de cadastro para administradores.
    val podeAdicionar = administradorAbrigo(ctx.dados)
    // Converte a opção escolhida em uma função reconhecida pelo aplicativo.
    val funcao = FuncaoAbrigo.values().firstOrNull { it.name == funcaoNome }

    val escolherFoto = rememberLauncherForActivityResult(
        // Abre o seletor de documentos para permitir acesso persistente à imagem.
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                // Mantém a permissão de leitura para mostrar a foto na lista depois.
                contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.onSuccess {
                fotoUri = uri.toString()
            }.onFailure {
                erro = "Não foi possível manter o acesso a essa foto. Escolha outra imagem."
            }
        }
    }

    // Aplica a mesma confirmação ao botão de voltar do Android.
    BackHandler { protegerSaida { ctx.voltar() } }
    CompositionLocalProvider(
        // Usa azul somente no cabeçalho deste formulário.
        LocalCorCabecalhoApp provides Color(0xFF8AC8FC),
        // Mantém a foto do usuário e os três pontos sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false),
        // Protege os dados digitados ao abrir Loja.
        LocalAbrirLoja provides { protegerSaida { ctx.principal("loja") } },
        // Protege os dados digitados ao abrir Finanças.
        LocalAbrirFinancas provides { protegerSaida { ctx.principal("financas") } }
    ) {
        MolduraPadraoApp(
            // Mantém o conteúdo branco, como a tela da equipe.
            containerColor = Color.White,
            // Confirma o descarte ao navegar pelo perfil ou pelos três pontos.
            interceptarCabecalho = protegerSaida,
            // Mantém o botão de mais funcionando e protege o formulário.
            floatingActionButton = {
                BotaoAdicionarFlutuante(
                    // Abre Ações rápidas depois da confirmação, se necessária.
                    aoAdicionar = { protegerSaida { ctx.principal("adicionar") } }
                )
            },
            // Mantém a navegação padronizada do aplicativo.
            bottomBar = {
                MenuInferiorInicio(
                    // Mostra avisos da navegação.
                    avisar = { erro = it },
                    // Não seleciona uma aba principal para o formulário.
                    selecionado = "",
                    // Abre o início com proteção de dados não salvos.
                    aoInicio = { protegerSaida { ctx.principal("inicio") } },
                    // Abre Animais com proteção de dados não salvos.
                    aoAnimais = { protegerSaida { ctx.principal("animais") } },
                    // Abre Ações rápidas com proteção de dados não salvos.
                    aoAdicionar = { protegerSaida { ctx.principal("adicionar") } },
                    // Abre a Agenda com proteção de dados não salvos.
                    aoAgenda = { protegerSaida { ctx.principal("agenda") } }
                )
            }
        ) { espacos ->
            Column(
                // Respeita as barras, permite rolagem e acompanha a abertura do teclado.
                modifier = Modifier.fillMaxSize().padding(espacos).imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa as seções do formulário.
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TituloPaginaPadrao(
                    // Identifica o cadastro direto de um membro.
                    titulo = if (original == null) "Adicionar membro" else "Editar perfil",
                    // Mantém a seta ao lado do título e confirma o descarte.
                    aoVoltar = { protegerSaida { ctx.voltar() } }
                )
                if (!podeAdicionar) {
                    Text(
                        // Informa a restrição para usuários sem administração.
                        text = "Apenas administradores podem adicionar membros.",
                        // Mantém contraste no fundo branco.
                        color = TextoRestante
                    )
                } else {
                    Surface(
                        // Abre a galeria ao tocar na área da foto.
                        onClick = { escolherFoto.launch(arrayOf("image/*")) },
                        // Padroniza o tamanho do recipiente da foto.
                        modifier = Modifier.align(Alignment.CenterHorizontally).size(116.dp),
                        // Arredonda a área de imagem.
                        shape = RoundedCornerShape(24.dp),
                        // Usa azul claro para identificar o espaço da foto.
                        color = Color(0xFFEAF4FC),
                        // Delimita o espaço da foto no fundo branco.
                        border = BorderStroke(1.dp, Color(0xFFB9D9EF))
                    ) {
                        if (foto != null) {
                            Image(
                                // Mostra a foto selecionada para o novo integrante.
                                bitmap = foto,
                                // Descreve a foto para leitores de tela.
                                contentDescription = "Foto do novo membro; toque para alterar",
                                // Preenche a área reservada.
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                                // Recorta sem distorcer a fotografia.
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                // Centraliza o ícone e a orientação.
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                // Centraliza os elementos horizontalmente.
                                horizontalAlignment = Alignment.CenterHorizontally,
                                // Mantém o ícone próximo da orientação.
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                            ) {
                                Icon(
                                    // Reutiliza o ícone de câmera já existente.
                                    painter = painterResource(R.drawable.ficha_camera),
                                    // O texto abaixo descreve a ação.
                                    contentDescription = null,
                                    // Define a medida do ícone de fotografia.
                                    modifier = Modifier.size(28.dp),
                                    // Mantém o ícone na cor marrom do aplicativo.
                                    tint = MarromRestante
                                )
                                Text(
                                    // Orienta a escolha da foto.
                                    text = "Adicionar foto",
                                    // Mantém o texto dentro da área da imagem.
                                    fontSize = 12.sp,
                                    // Usa a cor dos textos do aplicativo.
                                    color = TextoRestante
                                )
                            }
                        }
                    }
                    if (fotoUri != null) {
                        TextButton(
                            // Permite trocar a imagem já selecionada.
                            onClick = { escolherFoto.launch(arrayOf("image/*")) },
                            // Centraliza a ação abaixo da fotografia.
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) { Text("Alterar foto") }
                    }
                    CampoAdicionarMembro("Nome", nome, { nome = it }, "Digite o nome")
                    CampoAdicionarMembro("E-mail", email, { email = it }, "Digite o e-mail", KeyboardType.Email)
                    CampoAdicionarMembro("Telefone/Celular · opcional", telefone, { telefone = it }, "(XX) XXXXX-XXXX", KeyboardType.Phone)
                    CampoAdicionarMembro("Endereço · opcional", endereco, { endereco = it }, "Digite o endereço")
                    SeletorRestante(
                        // Identifica o campo de função na equipe.
                        rotulo = "Cargo",
                        // Mostra o cargo escolhido ou a orientação inicial.
                        valor = funcao?.titulo ?: "Selecione um cargo",
                        // Usa somente as funções existentes no aplicativo.
                        opcoes = FuncaoAbrigo.values().map { it.name to it.titulo },
                        // Guarda a seleção para salvar com o membro.
                        selecionar = { funcaoNome = it }
                    )
                    // Separa os campos das ações finais.
                    Spacer(Modifier.height(8.dp))
                    Row(
                        // Mantém os dois botões na largura do formulário.
                        modifier = Modifier.fillMaxWidth(),
                        // Separa Cancelar e Salvar.
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            // Volta à equipe e confirma o descarte se houver dados.
                            onClick = { protegerSaida { ctx.voltar() } },
                            // Divide a largura igualmente com Salvar.
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            // Mantém o formato dos botões da referência.
                            shape = RoundedCornerShape(14.dp),
                            // Aplica laranja ao fundo e marrom ao texto.
                            colors = ButtonDefaults.buttonColors(
                                // Define o fundo do botão Cancelar.
                                containerColor = Color(0xFFFFAE49),
                                // Define a cor do texto.
                                contentColor = TextoRestante
                            )
                        ) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                        Button(
                            // Valida e adiciona o integrante ao estado local da equipe.
                            onClick = {
                                val emailLimpo = email.trim()
                                when {
                                    !administradorAbrigo(ctx.dados) -> erro = "Acesso de administração necessário."
                                    nome.isBlank() -> erro = "Informe o nome."
                                    !Patterns.EMAIL_ADDRESS.matcher(emailLimpo).matches() -> erro = "Informe um e-mail válido."
                                    funcao == null -> erro = "Selecione um cargo."
                                    ctx.dados.membros.any { it.id != membroEdicaoId && it.email.isNotBlank() && it.email.equals(emailLimpo, true) } ->
                                        erro = "Já existe um membro com esse e-mail."
                                    else -> {
                                        val novo = MembroAbrigo(
                                            // Cria um identificador único para o integrante.
                                            id = original?.id ?: java.util.UUID.randomUUID().toString(),
                                            // Mantém o nome completo compatível com a lista existente.
                                            nome = nome.trim(),
                                            // Salva a função selecionada.
                                            funcao = funcao,
                                            // Mantém compatibilidade com registros antigos, sem exibir outro campo.
                                            sobrenome = original?.sobrenome.orEmpty(),
                                            // Guarda o contato informado.
                                            email = emailLimpo,
                                            // Guarda o telefone opcional.
                                            telefone = telefone.trim(),
                                            // Guarda o endereço opcional.
                                            endereco = endereco.trim(),
                                            // Associa a foto ao novo membro.
                                            fotoUri = fotoUri
                                        )
                                        // Preserva as regras existentes para troca de cargo.
                                        val erroCargo = if (original != null && original.funcao != funcao) {
                                            erroMudancaFuncaoAbrigo(ctx.dados, original.id, funcao)
                                        } else null
                                        if (membroEdicaoId != null && original == null) {
                                            erro = "Este membro não está mais na equipe."
                                        } else if (erroCargo != null) {
                                            erro = erroCargo
                                        } else {
                                            // Atualiza somente o integrante editado ou acrescenta um novo cartão.
                                            val membrosAtualizados = if (original != null) {
                                                ctx.dados.membros.map { if (it.id == original.id) novo else it }
                                            } else ctx.dados.membros + novo
                                            // Preserva todos os outros dados do aplicativo.
                                            ctx.atualizar(ctx.dados.copy(membros = membrosAtualizados))
                                            // Retorna ao perfil na edição ou à equipe no cadastro.
                                            ctx.voltar()
                                        }
                                    }
                                }
                            },
                            // Divide a largura igualmente com Cancelar.
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            // Mantém o formato dos botões da referência.
                            shape = RoundedCornerShape(14.dp),
                            // Mantém as cores iguais às do botão Cancelar.
                            colors = ButtonDefaults.buttonColors(
                                // Define o fundo do botão Salvar.
                                containerColor = Color(0xFFFFAE49),
                                // Define a cor do texto.
                                contentColor = TextoRestante
                            )
                        ) { Text("Salvar", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
    // Exibe erros sem apagar os dados preenchidos.
    erro?.let { MensagemRestante(it) { erro = null } }
}

// Mantém os rótulos acima dos campos, como na referência do formulário.
@Composable
private fun CampoAdicionarMembro(
    // Define o rótulo do campo.
    titulo: String,
    // Recebe o conteúdo digitado.
    valor: String,
    // Atualiza o conteúdo no formulário.
    aoMudar: (String) -> Unit,
    // Define a orientação exibida no campo vazio.
    dica: String,
    // Escolhe o teclado apropriado ao conteúdo.
    teclado: KeyboardType = KeyboardType.Text
) {
    Column(
        // Separa o rótulo da área de digitação.
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            // Mostra o rótulo acima do campo.
            text = titulo,
            // Mantém o tamanho de leitura dos rótulos.
            fontSize = 14.sp,
            // Usa a cor dos títulos do aplicativo.
            color = TextoRestante
        )
        OutlinedTextField(
            // Mostra o conteúdo atual.
            value = valor,
            // Limita o comprimento sem apagar o estado dos demais campos.
            onValueChange = { aoMudar(it.take(200)) },
            // Mantém os campos alinhados na largura da tela.
            modifier = Modifier.fillMaxWidth(),
            // Mantém uma única linha de digitação.
            singleLine = true,
            // Mostra a orientação quando o campo está vazio.
            placeholder = { Text(dica) },
            // Seleciona o teclado de texto, e-mail ou telefone.
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            // Arredonda as bordas dos campos.
            shape = RoundedCornerShape(12.dp),
            // Usa pêssego claro nos campos, mantendo o fundo da página branco.
            colors = OutlinedTextFieldDefaults.colors(
                // Define o fundo durante a edição.
                focusedContainerColor = Color(0xFFFFF3E5),
                // Define o fundo sem foco.
                unfocusedContainerColor = Color(0xFFFFF3E5),
                // Destaca o campo ativo com marrom.
                focusedBorderColor = MarromRestante,
                // Mantém a borda discreta nos demais campos.
                unfocusedBorderColor = Color(0xFFEADFD2),
                // Define o texto durante a edição.
                focusedTextColor = TextoRestante,
                // Define o texto sem foco.
                unfocusedTextColor = TextoRestante,
                // Usa marrom no cursor.
                cursorColor = MarromRestante
            )
        )
    }
}

// Mostra o formulário completo no Android Studio.
@Preview(
    // Identifica esta prévia no painel.
    name = "Adicionar membro",
    // Mostra o fundo da prévia.
    showBackground = true,
    // Define a largura de referência.
    widthDp = 412,
    // Define a altura de referência.
    heightDp = 917
)
@Composable
private fun PreviewTelaConvidarMembro() {
    MaterialTheme {
        TelaConvidarMembro(
            // Usa os dados demonstrativos do projeto.
            ctx = contextoPreviewRestante()
        )
    }
}
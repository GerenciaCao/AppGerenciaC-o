package com.example.gerenciacao

import android.content.Intent
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mostra o próprio abrigo como um perfil; o formulário fica atrás de Editar abrigo.
@Composable
fun TelaMeuAbrigo(
    // Recebe o cadastro, a equipe e as ações existentes.
    ctx: ContextoAbrigo
) {
    // Alterna entre consulta e edição sem precisar criar uma rota nova.
    var editando by rememberSaveable { mutableStateOf(false) }
    CompositionLocalProvider(
        // Mantém azul apenas no fluxo do próprio abrigo.
        LocalCorCabecalhoApp provides Color(0xFF8AC8FC),
        // Preserva foto e três pontos, sem repetir a saudação.
        LocalCabecalhoApp provides LocalCabecalhoApp.current.copy(mostrarSaudacao = false)
    ) {
        if (editando && administradorAbrigo(ctx.dados)) {
            EditarMeuAbrigo(ctx) { editando = false }
        } else {
            PerfilMeuAbrigo(ctx) { editando = true }
        }
    }
}

// Apresenta os dados cadastrados sem abrir campos de edição imediatamente.
@Composable
private fun PerfilMeuAbrigo(ctx: ContextoAbrigo, editar: () -> Unit) {
    val abrigo = ctx.dados.abrigo
    // Usa o mesmo cálculo do formulário anterior.
    val acolhidos = ctx.animais.count { !it.arquivado && it.situacao != "Adotado" }
    val vagas = (abrigo.capacidade - acolhidos).coerceAtLeast(0)
    val localizacao = listOf(abrigo.bairro, abrigo.cidade).filter { it.isNotBlank() }.joinToString(" · ")
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Preserva os destinos da barra inferior.
        LocalAbrirLoja provides { ctx.principal("loja") },
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Usa o fundo padrão das páginas de leitura.
            containerColor = FundoPadraoApp,
            // Mantém a navegação existente do aplicativo.
            bottomBar = {
                MenuInferiorInicio(
                    avisar = { aviso = it },
                    selecionado = "",
                    aoInicio = { ctx.principal("inicio") },
                    aoAnimais = { ctx.principal("animais") },
                    aoAdicionar = { ctx.principal("adicionar") },
                    aoAgenda = { ctx.principal("agenda") }
                )
            }
        ) { espacos ->
            Column(
                // Mantém margens uniformes e permite percorrer o perfil.
                modifier = Modifier.fillMaxSize().padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa apresentação, indicadores e contatos.
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Mantém a seta ao lado do título.
                TituloPaginaPadrao("Meu abrigo", ctx.voltar)
                CapaMeuAbrigo(abrigo.fotoUri, abrigo.nome)
                Column(
                    // Aproxima o nome da localização.
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        // Mostra o nome que foi salvo no cadastro.
                        text = abrigo.nome,
                        // Destaca o nome do abrigo.
                        fontSize = 25.sp,
                        // Mantém respiro para nomes que ocupam duas linhas.
                        lineHeight = 30.sp,
                        // Dá destaque ao nome.
                        fontWeight = FontWeight.Bold,
                        // Usa o marrom dos títulos.
                        color = TextoRestante
                    )
                    Text(
                        // Mostra somente a localização cadastrada.
                        text = localizacao.ifBlank { "Localização não informada" },
                        // Mantém a localização secundária.
                        fontSize = 13.sp,
                        // Usa a cor auxiliar do aplicativo.
                        color = MarromRestante
                    )
                }
                Text(
                    // Apresenta o abrigo sem copiar a história de outro cadastro.
                    text = abrigo.descricao.ifBlank { "Descrição não informada." },
                    // Mantém a apresentação legível.
                    fontSize = 15.sp,
                    // Separa as linhas da apresentação.
                    lineHeight = 22.sp,
                    // Usa a cor dos textos do aplicativo.
                    color = TextoRestante
                )
                Row(
                    // Distribui os indicadores na largura disponível.
                    modifier = Modifier.fillMaxWidth(),
                    // Separa as três áreas de números.
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NumeroMeuAbrigo(acolhidos, "Animais", Modifier.weight(1f))
                    NumeroMeuAbrigo(abrigo.capacidade, "Capacidade", Modifier.weight(1f))
                    NumeroMeuAbrigo(vagas, "Vagas", Modifier.weight(1f))
                }
                CartaoRestante {
                    Text("Contato", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                    SelectionContainer {
                        Column(
                            // Separa as informações que podem ser selecionadas e copiadas.
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ContatoMeuAbrigo("E-mail", abrigo.email)
                            ContatoMeuAbrigo("Telefone", abrigo.telefone)
                            ContatoMeuAbrigo("Endereço", abrigo.endereco)
                            ContatoMeuAbrigo("Rede social", abrigo.redeSocial)
                        }
                    }
                }
                Text("Equipe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                Row(
                    // Permite percorrer os integrantes sem comprimir seus nomes.
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    // Separa as identificações da equipe.
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ctx.dados.membros.forEach { membro ->
                        Column(
                            // Reserva uma largura uniforme por integrante.
                            modifier = Modifier.width(76.dp),
                            // Centraliza a identificação do integrante.
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Separa a inicial do nome.
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                // Mostra a inicial em um círculo de identificação.
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(CremeRestante),
                                // Centraliza a inicial no círculo.
                                contentAlignment = Alignment.Center
                            ) {
                                // Carrega a foto cadastrada para este integrante.
                                val fotoCadastrada = lembrarFotoMeuPerfil(membro.fotoUri)

// Para a pessoa conectada, reutiliza a mesma foto mostrada no cabeçalho.
                                val fotoMembro = if (membro.id == ctx.dados.usuarioLocalId) {
                                    LocalCabecalhoApp.current.foto ?: fotoCadastrada
                                } else {
                                    fotoCadastrada
                                }

                                if (fotoMembro != null) {
                                    Image(
                                        // Mostra a foto da pessoa correspondente.
                                        bitmap = fotoMembro,
                                        // Identifica o integrante.
                                        contentDescription = "Foto de " + membro.nome,
                                        // Preenche o círculo já existente.
                                        modifier = Modifier.fillMaxSize(),
                                        // Enquadra a foto sem deformar.
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        // Mostra a inicial somente quando a pessoa não tem foto.
                                        text = membro.nome.trim().take(1).uppercase(),
                                        // Define o tamanho da inicial.
                                        fontSize = 21.sp,
                                        // Destaca a inicial.
                                        fontWeight = FontWeight.Bold,
                                        // Mantém a cor do aplicativo.
                                        color = MarromRestante
                                    )
                                }
                            }
                            Text(membro.nome.trim().substringBefore(" "), fontSize = 12.sp, color = TextoRestante)
                        }
                    }
                }
                TextButton(
                    // Abre a página de equipe que já existe.
                    onClick = { ctx.abrir("equipe_abrigo") }
                ) { Text("Ver equipe do abrigo", color = MarromRestante) }
                if (administradorAbrigo(ctx.dados)) {
                    Button(
                        // Abre o formulário apenas após a solicitação de edição.
                        onClick = editar,
                        // Mantém a ação na largura da página.
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        // Mantém o formato dos botões do aplicativo.
                        shape = RoundedCornerShape(14.dp),
                        // Destaca a edição com marrom e texto branco.
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF493020),
                            contentColor = Color.White
                        )
                    ) { Text("Editar abrigo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Usa a foto do próprio abrigo na apresentação e na edição.
@Composable
private fun CapaMeuAbrigo(uri: String?, nome: String) {
    val foto = lembrarFotoMeuPerfil(uri)
    Surface(
        // Mantém a capa na largura do perfil.
        modifier = Modifier.fillMaxWidth().height(180.dp),
        // Arredonda a imagem.
        shape = RoundedCornerShape(18.dp),
        // Usa um fundo neutro quando não há foto cadastrada.
        color = Color(0xFFEAF4FC)
    ) {
        if (foto != null) {
            Image(
                // Mostra a foto cadastrada do abrigo.
                bitmap = foto,
                // Identifica o conteúdo da capa.
                contentDescription = "Foto de " + nome,
                // Preenche a área de capa.
                modifier = Modifier.fillMaxSize(),
                // Enquadra a foto sem deformá-la.
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                // Ocupa a área da imagem.
                modifier = Modifier.fillMaxSize(),
                // Centraliza a indicação de foto ausente.
                horizontalAlignment = Alignment.CenterHorizontally,
                // Separa o desenho e a mensagem.
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Icon(
                    // Reutiliza o desenho de abrigo disponível no projeto.
                    painter = painterResource(R.drawable.inicio_inicio),
                    // O texto abaixo já descreve o estado vazio.
                    contentDescription = null,
                    // Define a medida do ícone.
                    modifier = Modifier.size(36.dp),
                    // Mantém a identidade do aplicativo.
                    tint = MarromRestante
                )
                Text("Foto do abrigo não cadastrada", fontSize = 13.sp, color = MarromRestante)
            }
        }
    }
}

// Apresenta o valor e a legenda de cada indicador.
@Composable
private fun NumeroMeuAbrigo(valor: Int, legenda: String, modifier: Modifier) {
    Surface(
        // Divide o espaço com os demais indicadores.
        modifier = modifier,
        // Usa o creme dos indicadores da referência.
        color = CremeRestante,
        // Mantém os cantos arredondados.
        shape = RoundedCornerShape(15.dp)
    ) {
        Column(
            // Define o espaço interno do número.
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 15.dp),
            // Centraliza o número e sua legenda.
            horizontalAlignment = Alignment.CenterHorizontally,
            // Separa o número da identificação.
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(valor.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            Text(legenda, fontSize = 12.sp, color = MarromRestante)
        }
    }
}

// Mostra contatos cadastrados sem executar chamadas ou enviar mensagens.
@Composable
private fun ContatoMeuAbrigo(rotulo: String, valor: String) {
    Column(
        // Separa o rótulo do conteúdo.
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(rotulo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MarromRestante)
        Text(valor.ifBlank { "Não informado" }, fontSize = 15.sp, lineHeight = 21.sp, color = TextoRestante)
    }
}

// Edita os dados locais do abrigo e calcula a ocupacao pelas fichas existentes.
@Composable
private fun EditarMeuAbrigo(ctx: ContextoAbrigo, aoFechar: () -> Unit) {
    val original = ctx.dados.abrigo
    var nome by rememberSaveable { mutableStateOf(original.nome) }
    var telefone by rememberSaveable { mutableStateOf(original.telefone) }
    var rede by rememberSaveable { mutableStateOf(original.redeSocial) }
    var capacidade by rememberSaveable { mutableStateOf(original.capacidade.toString()) }
    // Preserva os novos campos enquanto a edição está aberta.
    var descricao by rememberSaveable { mutableStateOf(original.descricao) }
    var bairro by rememberSaveable { mutableStateOf(original.bairro) }
    var cidade by rememberSaveable { mutableStateOf(original.cidade) }
    var endereco by rememberSaveable { mutableStateOf(original.endereco) }
    var email by rememberSaveable { mutableStateOf(original.email) }
    var fotoUri by rememberSaveable { mutableStateOf(original.fotoUri) }
    // Abre o seletor de imagens do Android.
    val contexto = LocalContext.current
    val escolherFoto = rememberLauncherForActivityResult(
        // Permite preservar a permissão de leitura da imagem.
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.onSuccess { fotoUri = uri.toString() }
        }
    }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val ocupacao = ctx.animais.count { !it.arquivado && it.situacao != "Adotado" }
    val alterado = nome != original.nome || telefone != original.telefone ||
            rede != original.redeSocial || capacidade != original.capacidade.toString() ||
            descricao != original.descricao || bairro != original.bairro || cidade != original.cidade ||
            endereco != original.endereco || email != original.email || fotoUri != original.fotoUri
    // Preserva a confirmação de descarte ao cancelar a edição.
    val proteger = protegerRascunhoNoCabecalho(alterado)
    // Faz a seta voltar ao perfil, em vez de sair da página Meu abrigo.
    val contextoEdicao = ContextoAbrigo(
        dados = ctx.dados, animais = ctx.animais, atualizar = ctx.atualizar,
        abrir = ctx.abrir, voltar = aoFechar, principal = ctx.principal, verAnimal = ctx.verAnimal
    )
    BaseRestante(contextoEdicao, "Editar abrigo", alterado = alterado) {
        if (!administradorAbrigo(ctx.dados)) Text("Esta demonstração exige a função Administrador para editar o abrigo.")
        else {
            // Mostra a imagem escolhida antes de confirmar o cadastro.
            CapaMeuAbrigo(fotoUri, nome)
            TextButton(
                // Abre a galeria para adicionar ou substituir a foto.
                onClick = { escolherFoto.launch(arrayOf("image/*")) }
            ) { Text(if (fotoUri == null) "Adicionar foto do abrigo" else "Alterar foto", color = MarromRestante) }
            CampoRestante("Nome do abrigo", nome, { nome = it })
            CampoRestante("Sobre o abrigo · opcional", descricao, { descricao = it }, longo = true)
            CampoRestante("Bairro · opcional", bairro, { bairro = it })
            CampoRestante("Cidade/UF · opcional", cidade, { cidade = it }, dica = "São Paulo/SP")
            CampoRestante("Endereço · opcional", endereco, { endereco = it })
            CampoRestante("E-mail · opcional", email, { email = it })
            CampoRestante("Telefone · opcional", telefone, { telefone = it }, dica = "DDD e número")
            CampoRestante("Rede social · opcional", rede, { rede = it }, dica = "@nomedoabrigo")
            CampoRestante("Capacidade · número de espaços", capacidade, { capacidade = it })
            CartaoRestante(VerdeRestante) {
                Text("$ocupacao bichinhos acolhidos", fontWeight = FontWeight.Bold)
                val limite = capacidade.toIntOrNull()
                Text(if (limite != null && limite >= ocupacao) "${limite - ocupacao} espaços disponíveis" else "A capacidade deve comportar os bichinhos acolhidos.")
                Text("Ocupação calculada pelas fichas ativas, excluindo adotados e arquivados.")
            }
            TextButton(
                // Volta ao perfil com confirmação quando houver dados não salvos.
                onClick = { proteger(aoFechar) }
            ) { Text("Cancelar", color = MarromRestante) }
            // Revalida antes de salvar para nao permitir capacidade inferior a ocupacao.
            BotaoRestante("Salvar informações") {
                val limite = capacidade.toIntOrNull()
                val digitos = telefone.filter(Char::isDigit)
                when {
                    !administradorAbrigo(ctx.dados) -> aviso = "Acesso de administração necessário."
                    nome.trim().length < 3 -> aviso = "Informe o nome do abrigo com pelo menos 3 caracteres."
                    email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> aviso = "Informe um e-mail válido."
                    telefone.isNotBlank() && digitos.length !in 10..11 -> aviso = "Informe um telefone com DDD, totalizando 10 ou 11 dígitos."
                    limite == null || limite !in 1..100000 || limite < ocupacao -> aviso = "Informe uma capacidade válida, igual ou maior que a ocupação atual."
                    else -> {
                        ctx.atualizar(ctx.dados.copy(abrigo = original.copy(
                            // Preserva o cadastro e atualiza os campos preenchidos.
                            nome = nome.trim(), telefone = telefone.trim(), redeSocial = rede.trim(),
                            capacidade = limite, descricao = descricao.trim(), bairro = bairro.trim(),
                            cidade = cidade.trim(), endereco = endereco.trim(), email = email.trim(),
                            fotoUri = fotoUri
                        )))
                        // Retorna ao perfil com os dados atualizados.
                        aoFechar()
                    }
                }
            }
        }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}


// Mostra o perfil de leitura na prévia do Android Studio.
@Preview(
    // Identifica a página no painel.
    name = "Meu abrigo - perfil",
    // Mostra o fundo da tela.
    showBackground = true,
    // Define a largura da prévia.
    widthDp = 412,
    // Define a altura da prévia.
    heightDp = 917
)
@Composable
private fun PreviewTelaMeuAbrigo() {
    MaterialTheme { TelaMeuAbrigo(contextoPreviewRestante()) }
}
package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Mostra os dados cadastrados da pessoa selecionada na equipe.
@Composable
fun TelaPerfilMembro(
    // Recebe a equipe atual, a seleção e as ações de navegação.
    ctx: ContextoAbrigo
) {
    // Busca pelo ID, evitando mostrar o perfil de outra pessoa.
    val membro = ctx.dados.membros.firstOrNull { it.id == ctx.dados.membroId }
    // Reutiliza a foto atual do usuário quando o perfil aberto for o dele.
    val cabecalho = LocalCabecalhoApp.current
    // Carrega a foto escolhida no cadastro do integrante.
    val fotoSalva = lembrarFotoMeuPerfil(membro?.fotoUri)
    // Prioriza a foto do perfil conectado e usa a foto cadastrada para os demais.
    val foto = cabecalho.foto.takeIf { membro?.id == ctx.dados.usuarioLocalId } ?: fotoSalva
    // Mantém a edição e as permissões restritas à administração existente.
    val podeEditar = administradorAbrigo(ctx.dados)
    // Guarda eventuais avisos da navegação.
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Retorna pelo botão do sistema.
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Usa o mesmo azul da tela de equipe somente neste perfil.
        LocalCorCabecalhoApp provides Color(0xFF8AC8FC),
        // Mantém a foto e os três pontos sem a saudação.
        LocalCabecalhoApp provides cabecalho.copy(mostrarSaudacao = false),
        // Mantém o acesso à loja pelo menu inferior.
        LocalAbrirLoja provides { ctx.principal("loja") },
        // Mantém o acesso às finanças pelo menu inferior.
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Mantém o fundo branco solicitado para as telas da equipe.
            containerColor = Color.White,
            // Mantém as ações rápidas disponíveis pelo botão de mais.
            floatingActionButton = {
                BotaoAdicionarFlutuante(
                    // Abre a tela de ações rápidas.
                    aoAdicionar = { ctx.principal("adicionar") }
                )
            },
            // Reutiliza a navegação inferior do aplicativo.
            bottomBar = {
                MenuInferiorInicio(
                    // Exibe avisos produzidos pelo menu.
                    avisar = { aviso = it },
                    // Não seleciona uma aba principal neste perfil.
                    selecionado = "",
                    // Abre a tela inicial.
                    aoInicio = { ctx.principal("inicio") },
                    // Abre a lista de animais.
                    aoAnimais = { ctx.principal("animais") },
                    // Abre as ações rápidas.
                    aoAdicionar = { ctx.principal("adicionar") },
                    // Abre a agenda.
                    aoAgenda = { ctx.principal("agenda") }
                )
            }
        ) { espacos ->
            Column(
                // Respeita as barras e permite rolar até os botões finais.
                modifier = Modifier.fillMaxSize().padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                // Separa os blocos de informações.
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TituloPaginaPadrao(
                    // Identifica a consulta de um integrante.
                    titulo = "Perfil do membro",
                    // Mantém a seta ao lado do título.
                    aoVoltar = ctx.voltar
                )
                if (membro == null) {
                    Text(
                        // Evita mostrar dados de outro integrante quando não há seleção válida.
                        text = "Membro não encontrado. Volte à equipe e selecione uma pessoa.",
                        // Mantém contraste com o fundo branco.
                        color = TextoRestante
                    )
                } else {
                    Surface(
                        // Usa um fundo suave na apresentação da pessoa.
                        color = Color(0xFFFFF3E5),
                        // Arredonda o cartão de apresentação.
                        shape = RoundedCornerShape(20.dp),
                        // Ocupa a largura entre as margens da página.
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            // Define o espaço interno em torno da foto e do nome.
                            modifier = Modifier.padding(20.dp),
                            // Centraliza foto, nome e cargo.
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Separa os elementos do cabeçalho pessoal.
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                // Mantém a foto circular e com uma medida fixa.
                                modifier = Modifier.size(88.dp).clip(CircleShape)
                                    .background(Color(0xFFEAF4FC)),
                                // Centraliza a inicial quando não há foto.
                                contentAlignment = Alignment.Center
                            ) {
                                if (foto != null) {
                                    Image(
                                        // Usa a imagem vinculada a este integrante.
                                        bitmap = foto,
                                        // Identifica a pessoa para leitores de tela.
                                        contentDescription = "Foto de " + membro.nome,
                                        // Preenche a área circular.
                                        modifier = Modifier.fillMaxSize(),
                                        // Recorta a foto sem deformá-la.
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        // Mostra a inicial enquanto não houver fotografia.
                                        text = membro.nome.trim().take(1).uppercase(),
                                        // Mantém a inicial proporcional ao círculo.
                                        fontSize = 32.sp,
                                        // Destaca a inicial.
                                        fontWeight = FontWeight.Bold,
                                        // Mantém contraste com o fundo claro.
                                        color = TextoRestante
                                    )
                                }
                            }
                            Text(
                                // Mostra o nome completo em um único campo.
                                text = membro.nome,
                                // Destaca a pessoa cujo perfil está aberto.
                                fontSize = 22.sp,
                                // Dá destaque ao nome.
                                fontWeight = FontWeight.Bold,
                                // Mantém a cor dos títulos.
                                color = TextoRestante
                            )
                            Text(
                                // Mostra o cargo atual na equipe.
                                text = membro.funcao.titulo,
                                // Mantém o cargo secundário ao nome.
                                fontSize = 14.sp,
                                // Usa a cor dos textos auxiliares.
                                color = MarromRestante
                            )
                        }
                    }
                    Text(
                        // Identifica o grupo de dados pessoais.
                        text = "Informações pessoais",
                        // Mantém o tamanho dos títulos de seção.
                        fontSize = 18.sp,
                        // Destaca o início da seção.
                        fontWeight = FontWeight.Bold,
                        // Usa a cor padrão dos títulos.
                        color = TextoRestante
                    )
                    // Mantém a mesma ordem dos campos do cadastro, sem Sobrenome.
                    DadoPerfilMembro("Nome", membro.nome)
                    DadoPerfilMembro("E-mail", membro.email)
                    DadoPerfilMembro("Telefone/Celular", membro.telefone)
                    DadoPerfilMembro("Endereço", membro.endereco)
                    Text(
                        // Identifica o resumo das permissões existentes.
                        text = "Permissões de acesso",
                        // Mantém o tamanho dos títulos de seção.
                        fontSize = 18.sp,
                        // Destaca a seção.
                        fontWeight = FontWeight.Bold,
                        // Usa a cor dos títulos do aplicativo.
                        color = TextoRestante
                    )
                    Surface(
                        // Agrupa o cargo e a descrição usados pelo projeto.
                        modifier = Modifier.fillMaxWidth(),
                        // Mantém a paleta suave do perfil.
                        color = Color(0xFFFFF3E5),
                        // Arredonda a área das permissões.
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            // Define o respiro interno da seção.
                            modifier = Modifier.padding(16.dp),
                            // Separa o cargo da descrição.
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                // Mostra a função que determina o resumo abaixo.
                                text = membro.funcao.titulo,
                                // Destaca o cargo.
                                fontWeight = FontWeight.Bold,
                                // Mantém a cor padrão dos textos.
                                color = TextoRestante
                            )
                            Text(
                                // Reutiliza a descrição real do cargo no protótipo.
                                text = descricaoFuncaoAbrigo(membro.funcao),
                                // Mantém o texto legível.
                                fontSize = 14.sp,
                                // Separa as linhas da descrição.
                                lineHeight = 20.sp,
                                // Usa a cor dos textos auxiliares.
                                color = MarromRestante
                            )
                        }
                    }
                    if (podeEditar) {
                        TextButton(
                            // Abre a tela de permissões com este integrante selecionado.
                            onClick = { ctx.abrir("permissoes") }
                        ) { Text("Gerenciar permissões", color = MarromRestante) }
                        Button(
                            // Abre o formulário preenchido com os dados desta pessoa.
                            onClick = { ctx.abrir("editar_membro") },
                            // Mantém a ação na largura do formulário.
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            // Arredonda o botão como os demais da equipe.
                            shape = RoundedCornerShape(14.dp),
                            // Usa laranja para a ação principal.
                            colors = ButtonDefaults.buttonColors(
                                // Define o fundo do botão.
                                containerColor = Color(0xFFFFAE49),
                                // Mantém o texto escuro para contraste.
                                contentColor = TextoRestante
                            )
                        ) {
                            Text(
                                // Identifica a edição dos dados pessoais.
                                text = "Editar perfil",
                                // Destaca a ação principal.
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    // Mostra os avisos sem substituir o perfil.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra um campo somente para leitura; a alteração acontece em Editar perfil.
@Composable
private fun DadoPerfilMembro(
    // Define o rótulo da informação.
    rotulo: String,
    // Recebe o conteúdo cadastrado.
    valor: String
) {
    Column(
        // Separa o rótulo do valor.
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            // Identifica a informação mostrada.
            text = rotulo,
            // Usa o tamanho dos rótulos do formulário.
            fontSize = 14.sp,
            // Mantém contraste no fundo branco.
            color = TextoRestante
        )
        Surface(
            // Mantém o campo na largura do perfil.
            modifier = Modifier.fillMaxWidth(),
            // Usa o fundo suave do formulário.
            color = Color(0xFFFFF3E5),
            // Arredonda o campo de leitura.
            shape = RoundedCornerShape(12.dp),
            // Delimita o campo com uma borda discreta.
            border = BorderStroke(1.dp, Color(0xFFEADFD2))
        ) {
            Text(
                // Explica a ausência de dados sem inventar informações.
                text = valor.ifBlank { "Não informado" },
                // Mantém espaço entre o texto e a borda.
                modifier = Modifier.padding(14.dp),
                // Mantém a informação legível.
                fontSize = 15.sp,
                // Mantém contraste com o fundo claro.
                color = TextoRestante
            )
        }
    }
}

// Exibe um integrante demonstrativo no painel de prévia.
@Preview(
    // Identifica a prévia da nova tela.
    name = "Perfil do membro",
    // Mostra o fundo da prévia.
    showBackground = true,
    // Define a largura da prévia.
    widthDp = 412,
    // Define a altura da prévia.
    heightDp = 917
)
@Composable
private fun PreviewTelaPerfilMembro() {
    MaterialTheme {
        val base = contextoPreviewRestante()
        // Seleciona um integrante existente sem mudar os dados do aplicativo.
        val ctx = ContextoAbrigo(
            dados = base.dados.copy(membroId = base.dados.membros.firstOrNull()?.id),
            animais = base.animais,
            atualizar = base.atualizar,
            abrir = base.abrir,
            voltar = base.voltar,
            principal = base.principal,
            verAnimal = base.verAnimal
        )
        TelaPerfilMembro(ctx)
    }
}
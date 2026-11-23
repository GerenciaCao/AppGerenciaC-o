package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Define o azul usado somente no cabeçalho desta tela.
private val AzulEquipe = Color(0xFF8AC8FC)

// Mostra a equipe cadastrada e preserva os destinos de convite e permissões.
@Composable
fun TelaEquipeAbrigo(
    // Recebe os membros, as permissões e as ações de navegação.
    ctx: ContextoAbrigo,
    // Permite associar fotos do drawable ao ID de cada membro.
    fotosMembros: Map<String, Int> = emptyMap()
) {
    // Guarda a busca durante alterações de configuração do aparelho.
    var busca by rememberSaveable { mutableStateOf("") }
    // Guarda a função selecionada no filtro.
    var funcao by rememberSaveable { mutableStateOf("Todas") }
    // Controla a abertura do menu de filtros.
    var filtroAberto by rememberSaveable { mutableStateOf(false) }
    // Controla os avisos da navegação inferior.
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    // Reutiliza os dados atuais do perfil, incluindo a foto.
    val cabecalho = LocalCabecalhoApp.current
    // Mantém o controle de administração existente no projeto.
    val podeGerenciar = administradorAbrigo(ctx.dados)
    // Combina o nome digitado com a função selecionada.
    val visiveis = ctx.dados.membros.filter {
        it.nome.contains(busca.trim(), ignoreCase = true) &&
                (funcao == "Todas" || it.funcao.titulo == funcao)
    }

    // Mantém o retorno pelo botão do Android.
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(
        // Restringe o cabeçalho azul a esta tela.
        LocalCorCabecalhoApp provides AzulEquipe,
        // Mantém a foto e os três pontos, sem repetir a saudação.
        LocalCabecalhoApp provides cabecalho.copy(mostrarSaudacao = false),
        // Mantém o acesso à loja pela navegação inferior.
        LocalAbrirLoja provides { ctx.principal("loja") },
        // Mantém o acesso às finanças pela navegação inferior.
        LocalAbrirFinancas provides { ctx.principal("financas") }
    ) {
        MolduraPadraoApp(
            // Define o fundo branco solicitado para a página.
            containerColor = Color.White,
            // Mostra o botão de mais e abre as ações rápidas.
            floatingActionButton = {
                BotaoAdicionarFlutuante(
                    // Redireciona para a tela Ações rápidas.
                    aoAdicionar = { ctx.principal("adicionar") }
                )
            },
            // Mantém a navegação usada nas demais telas.
            bottomBar = {
                MenuInferiorInicio(
                    // Exibe os avisos produzidos pela navegação.
                    avisar = { aviso = it },
                    // Nenhuma aba principal corresponde à equipe.
                    selecionado = "",
                    // Abre a página inicial.
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
            LazyColumn(
                // Ocupa a tela e respeita os espaços da estrutura compartilhada.
                modifier = Modifier.fillMaxSize().padding(espacos),
                // Mantém as margens e o respiro superior das outras telas.
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
                // Separa os cartões sem aumentar demais a altura da lista.
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Mantém a seta ao lado do título, como nas outras páginas.
                    TituloPaginaPadrao("Equipe do abrigo", ctx.voltar)
                    // Separa o título da pesquisa.
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        // Mostra a pesquisa atual.
                        value = busca,
                        // Atualiza os resultados enquanto o usuário digita.
                        onValueChange = { busca = it.take(120) },
                        // Mantém a busca na largura da lista.
                        modifier = Modifier.fillMaxWidth(),
                        // Mantém a pesquisa em uma linha.
                        singleLine = true,
                        // Mostra a orientação dentro da busca vazia.
                        placeholder = { Text("Pesquisar membro") },
                        // Mostra a lupa padrão do aplicativo.
                        leadingIcon = {
                            Icon(
                                // Usa o recurso de busca existente.
                                painter = painterResource(R.drawable.inicio_busca),
                                // A lupa é decorativa.
                                contentDescription = null,
                                // Padroniza a medida da lupa.
                                modifier = Modifier.size(20.dp),
                                // Aplica a cor marrom dos controles.
                                tint = MarromRestante
                            )
                        },
                        // Abre o filtro de membros pelo botão à direita da busca.
                        trailingIcon = {
                            Box {
                                IconButton(
                                    // Abre o menu de funções da equipe.
                                    onClick = { filtroAberto = true }
                                ) {
                                    Box(
                                        // Desenha o círculo azul do controle.
                                        modifier = Modifier.size(32.dp).background(AzulEquipe, CircleShape),
                                        // Centraliza o desenho no círculo.
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(
                                            // Define a área dos traços do filtro.
                                            modifier = Modifier.size(16.dp)
                                                .semantics { contentDescription = "Filtrar membros por função" }
                                        ) {
                                            // Desenha três linhas de larguras decrescentes.
                                            listOf(0.3f to 0.9f, 0.5f to 0.6f, 0.7f to 0.3f).forEach { (y, largura) ->
                                                drawLine(
                                                    // Usa marrom para contraste com o azul.
                                                    color = MarromRestante,
                                                    // Centraliza o início de cada linha.
                                                    start = Offset(size.width * (1 - largura) / 2, size.height * y),
                                                    // Centraliza o final de cada linha.
                                                    end = Offset(size.width * (1 + largura) / 2, size.height * y),
                                                    // Define a espessura do desenho.
                                                    strokeWidth = 1.5.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                }
                                DropdownMenu(
                                    // Mostra o menu somente após tocar no filtro.
                                    expanded = filtroAberto,
                                    // Fecha o menu ao tocar fora dele.
                                    onDismissRequest = { filtroAberto = false }
                                ) {
                                    (listOf("Todas") + FuncaoAbrigo.values().map { it.titulo }).forEach { opcao ->
                                        DropdownMenuItem(
                                            // Marca a função que está sendo usada no filtro.
                                            text = { Text((if (funcao == opcao) "✓ " else "") + opcao) },
                                            // Atualiza a função e fecha o menu.
                                            onClick = { funcao = opcao; filtroAberto = false }
                                        )
                                    }
                                }
                            }
                        },
                        // Mantém o formato arredondado da busca.
                        shape = RoundedCornerShape(28.dp),
                        // Usa branco no campo e bordas discretas.
                        colors = OutlinedTextFieldDefaults.colors(
                            // Destaca o campo durante a digitação.
                            focusedBorderColor = MarromRestante,
                            // Define o contorno sem foco.
                            unfocusedBorderColor = BordaRestante,
                            // Mantém o fundo branco com foco.
                            focusedContainerColor = Color.White,
                            // Mantém o fundo branco sem foco.
                            unfocusedContainerColor = Color.White
                        )
                    )
                    // Separa a pesquisa da lista de membros.
                    Spacer(Modifier.height(24.dp))
                    Text(
                        // Identifica a lista de pessoas do abrigo.
                        text = "Membros",
                        // Dá destaque ao título da seção.
                        fontSize = 20.sp,
                        // Usa negrito no título.
                        fontWeight = FontWeight.Bold,
                        // Mantém a cor dos títulos do aplicativo.
                        color = TextoRestante
                    )
                }
                items(
                    // Mostra somente os membros que correspondem à pesquisa.
                    items = visiveis,
                    // Preserva a identidade de cada cartão ao filtrar a lista.
                    key = { it.id }
                ) { membro ->
                    Surface(
                        // Abre o perfil do integrante selecionado.
                        onClick = {
                            ctx.atualizar(ctx.dados.copy(membroId = membro.id))
                            ctx.abrir("perfil_membro")
                        },
                        // Permite consultar o perfil; a edição continua restrita à administração.
                        enabled = true,
                        // Ocupa a largura da lista.
                        modifier = Modifier.fillMaxWidth(),
                        // Mantém os cartões brancos.
                        color = Color.White,
                        // Evita que os cartões inativos mudem para cinza.
                        contentColor = TextoRestante,
                        // Arredonda o cartão como na referência.
                        shape = RoundedCornerShape(18.dp),
                        // Delimita o cartão sobre o fundo branco.
                        border = BorderStroke(1.dp, BordaRestante),
                        // Aplica uma sombra discreta.
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            // Mantém a altura confortável e o espaço interno do cartão.
                            modifier = Modifier.heightIn(min = 76.dp).padding(12.dp),
                            // Centraliza a foto, os textos e a seta verticalmente.
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                // Mantém a foto circular e com tamanho padronizado.
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFEAF4FC)),
                                // Centraliza a inicial quando não há foto cadastrada.
                                contentAlignment = Alignment.Center
                            ) {
                                // Carrega a foto salva no cadastro; usa o perfil atual quando disponível.
                                val fotoSalva = lembrarFotoMeuPerfil(membro.fotoUri)
                                val fotoAtual = cabecalho.foto.takeIf { membro.id == ctx.dados.usuarioLocalId } ?: fotoSalva
                                val fotoLocal = fotosMembros[membro.id]
                                if (fotoAtual != null) {
                                    Image(
                                        // Usa a foto atual da pessoa conectada.
                                        bitmap = fotoAtual,
                                        // Identifica a pessoa para leitores de tela.
                                        contentDescription = "Foto de " + membro.nome,
                                        // Preenche a área circular.
                                        modifier = Modifier.fillMaxSize(),
                                        // Recorta a foto sem deformá-la.
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (fotoLocal != null) {
                                    Image(
                                        // Carrega a foto associada ao ID deste membro.
                                        painter = painterResource(fotoLocal),
                                        // Identifica a pessoa da foto.
                                        contentDescription = "Foto de " + membro.nome,
                                        // Preenche a área circular.
                                        modifier = Modifier.fillMaxSize(),
                                        // Recorta a foto sem deformá-la.
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        // Usa a inicial até uma imagem ser associada ao membro.
                                        text = membro.nome.trim().take(1).uppercase(),
                                        // Mantém a inicial legível.
                                        fontSize = 20.sp,
                                        // Destaca a inicial.
                                        fontWeight = FontWeight.Bold,
                                        // Mantém contraste com o círculo claro.
                                        color = TextoRestante
                                    )
                                }
                            }
                            // Separa a imagem dos textos.
                            Spacer(Modifier.width(12.dp))
                            Column(
                                // Reserva o espaço restante para nome e função.
                                modifier = Modifier.weight(1f),
                                // Separa o nome da função.
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    // Mostra o nome já cadastrado no aplicativo.
                                    text = membro.nome,
                                    // Define o tamanho do nome.
                                    fontSize = 16.sp,
                                    // Destaca o nome.
                                    fontWeight = FontWeight.Bold,
                                    // Usa o marrom dos títulos.
                                    color = TextoRestante
                                )
                                Text(
                                    // Mostra a função atual da pessoa.
                                    text = membro.funcao.titulo,
                                    // Mantém a função secundária ao nome.
                                    fontSize = 12.sp,
                                    // Usa cinza escuro para a informação secundária.
                                    color = Color(0xFF6E665F)
                                )
                            }
                            if (true) {
                                Icon(
                                    // Reutiliza a seta dos outros cartões do aplicativo.
                                    painter = painterResource(R.drawable.inicio_seta),
                                    // O cartão já representa a ação de abrir o membro.
                                    contentDescription = null,
                                    // Mantém a seta proporcional ao cartão.
                                    modifier = Modifier.size(18.dp),
                                    // Usa o marrom dos controles.
                                    tint = MarromRestante
                                )
                            }
                        }
                    }
                }
                if (podeGerenciar) {
                    item {
                        // Separa os controles administrativos da lista de membros.
                        Spacer(Modifier.height(12.dp))
                        Button(
                            // Abre o formulário de convite já usado no aplicativo.
                            onClick = { ctx.abrir("convidar_membro") },
                            // Mantém o botão na largura dos cartões e facilita o toque.
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            // Mantém o formato arredondado do botão anterior.
                            shape = RoundedCornerShape(14.dp),
                            // Define as cores do convite de membros.
                            colors = ButtonDefaults.buttonColors(
                                // Usa o amarelo do botão mostrado na referência.
                                containerColor = Color(0xFFFFB414),
                                // Mantém contraste no texto do botão.
                                contentColor = TextoRestante
                            )
                        ) {
                            Text(
                                // Mantém o nome da ação original.
                                text = "Convidar membro",
                                // Destaca o título da ação.
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    item {
                        AtalhoRestante(
                            // Identifica o acesso às funções da equipe.
                            "Gerenciar permissões",
                            // Explica o conteúdo da página de destino.
                            "Revisar funções da equipe"
                        ) {
                            // Abre a página de permissões já existente.
                            ctx.abrir("permissoes")
                        }
                    }
                }
                if (visiveis.isEmpty()) {
                    item {
                        Text(
                            // Explica o resultado vazio da busca.
                            text = "Nenhum membro encontrado.",
                            // Mantém o contraste com o fundo branco.
                            color = TextoRestante
                        )
                    }
                }
            }
        }
    }
    // Mostra os avisos da navegação sem substituir o conteúdo da tela.
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Exibe a equipe no editor sem executar a Activity.
@Preview(
    // Define o nome da prévia.
    name = "Equipe do abrigo",
    // Mostra o fundo do dispositivo.
    showBackground = true,
    // Define a largura da prévia.
    widthDp = 412,
    // Define a altura da prévia.
    heightDp = 917
)
@Composable
private fun PreviewTelaEquipeAbrigo() {
    MaterialTheme {
        TelaEquipeAbrigo(
            // Usa os dados demonstrativos disponíveis no projeto.
            ctx = contextoPreviewRestante()
        )
    }
}
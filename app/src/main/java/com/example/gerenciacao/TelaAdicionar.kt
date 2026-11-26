package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Habilita a prévia no Android Studio.
import androidx.compose.ui.tooling.preview.Preview

// Reune as cinco acoes do botao mais e encaminha o cadastro para a Nova ficha.
@Composable
fun TelaAdicionar(
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    // Abre a agenda pelo menu compartilhado.
    aoAgenda: () -> Unit,
    aoAnimais: () -> Unit,
    aoCadastrarBichinho: () -> Unit,
    aoRegistrarDoacao: () -> Unit,
    aoRegistrarDespesa: () -> Unit,
    aoAgendarCuidado: () -> Unit,
    aoCriarPostagem: () -> Unit,
    aoParcerias: () -> Unit,
    aoDivulgacao: () -> Unit,
    // Identifica a aba que abriu a tela de ações.
    abaOrigem: String = "Início"
) {
    // Mantem as cores e o comportamento de avisos das outras telas do aplicativo.
    val fundo = Color(0xFFFFFCF7)
    val texto = Color(0xFF38291F)
    val marrom = Color(0xFF783E19)
    val creme = Color(0xFFFFEFC5)
    val amarelo = Color(0xFFFFB414)
    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }
    BackHandler(onBack = aoVoltar)

    // Fixa o menu inferior e deixa os cartoes rolarem em aparelhos menores.
    Column(Modifier.fillMaxSize().background(fundo)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            modifier = Modifier.weight(1f), containerColor = fundo,
            snackbarHost = { SnackbarHost(avisos) },
            // Esconde o botão + enquanto as ações já estão abertas.
            floatingActionButton = {},
            bottomBar = {
                MenuInferiorInicio(
                    // Mantém os avisos da navegação.
                    avisar = avisar,
                    // Destaca em marrom a aba que abriu as ações.
                    selecionado = abaOrigem,
                    // Preserva os destinos existentes.
                    aoInicio = aoInicio,
                    aoAnimais = aoAnimais,
                    // Esta página já apresenta as ações de adicionar.
                    aoAdicionar = {},
                    aoAgenda = aoAgenda
                )
            }
        ) { espacos ->
            Column(
                Modifier.fillMaxSize().padding(espacos).verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)
            ) {
                // Repete o cabecalho da referencia com o logotipo e o sino.
                // O cabecalho compartilhado agora apresenta o perfil da pessoa.
                TituloPaginaPadrao("Ações rápidas")
                Spacer(Modifier.height(28.dp))
                Text("O que deseja adicionar?", fontSize = 18.sp, lineHeight = 22.sp,
                    color = texto, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("Escolha uma ação para organizar sua rotina.", fontSize = 13.sp,
                    lineHeight = 18.sp, color = Color(0xFF7E7066))
                Spacer(Modifier.height(26.dp))

                // O Início mantém acesso a todas as ações.
                val mostrarTodas = abaOrigem == "Início"

                Column(
                    // Mantém os cartões na largura disponível.
                    modifier = Modifier.fillMaxWidth(),
                    // Separa somente os cartões que estão visíveis.
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mostrarTodas || abaOrigem == "Animais") {
                        AcaoAdicionar(
                            // Identifica o cadastro de uma ficha.
                            titulo = "Cadastrar bichinho",
                            // Explica a ação.
                            descricao = "Registre um animal no abrigo",
                            // Usa a mesma patinha da navegação.
                            icone = R.drawable.iconanimais,
                            // Define o fundo do ícone.
                            fundoIcone = creme,
                            // Abre o formulário já existente.
                            aoClicar = aoCadastrarBichinho
                        )

                        AcaoAdicionar(
                            // Identifica a divulgação.
                            titulo = "Criar postagem",
                            // Explica o destino.
                            descricao = "Prepare a divulgação de um bichinho",
                            // Reutiliza o ícone existente.
                            icone = R.drawable.adicionar_postagem,
                            // Define o fundo do ícone.
                            fundoIcone = Color(0xFFE1F3E8),
                            // Abre a criação de postagem já ligada.
                            aoClicar = aoCriarPostagem
                        )
                    }

                    if (mostrarTodas || abaOrigem == "Finanças") {
                        AcaoAdicionar(
                            // Identifica o registro de recebimentos.
                            titulo = "Registrar doação",
                            // Inclui doações em dinheiro e produtos.
                            descricao = "Anote o que o abrigo recebeu",
                            // Relaciona a ação à área financeira.
                            icone = R.drawable.inicio_carteira,
                            // Define o fundo do ícone.
                            fundoIcone = Color(0xFFE1F3E8),
                            // Abre o formulário de doação.
                            aoClicar = aoRegistrarDoacao
                        )

                        AcaoAdicionar(
                            // Identifica o registro de gastos.
                            titulo = "Registrar despesa",
                            // Explica a ação.
                            descricao = "Registre os gastos do abrigo",
                            // Reutiliza o ícone de recibo.
                            icone = R.drawable.inicio_recibo,
                            // Define o fundo do ícone.
                            fundoIcone = Color(0xFFFFEFE2),
                            // Abre o formulário de despesa.
                            aoClicar = aoRegistrarDespesa
                        )
                    }

                    if (mostrarTodas || abaOrigem == "Agenda") {
                        AcaoAdicionar(
                            // Identifica o agendamento.
                            titulo = "Agendar cuidado",
                            // Abrange as categorias da agenda.
                            descricao = "Vacinas, visitas e serviços",
                            // Usa o mesmo calendário da navegação.
                            icone = R.drawable.iconcalendario,
                            // Define o fundo do ícone.
                            fundoIcone = creme,
                            // Abre o formulário de compromisso.
                            aoClicar = aoAgendarCuidado
                        )
                    }

                    if (abaOrigem == "Loja") {
                        Text(
                            // Evita apresentar ações de outros módulos na Loja.
                            text = "Nenhuma ação de cadastro disponível nesta aba.",
                            // Mantém a leitura do conteúdo.
                            fontSize = 14.sp,
                            // Usa a cor secundária.
                            color = Color(0xFF7E7066)
                        )
                    }
                }
            }
        }
    }
}

// Desenha cada container com icone, titulo, descricao e seta, como na imagem recebida.
@Composable
private fun AcaoAdicionar(
    titulo: String,
    descricao: String,
    icone: Int,
    fundoIcone: Color,
    aoClicar: () -> Unit
) {
    Surface(
        onClick = aoClicar, modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(20.dp)),
        color = Color.White, shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFEADFD2))
    ) {
        Row(Modifier.heightIn(min = 84.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(fundoIcone, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center) {
                Icon(painterResource(icone), null, Modifier.size(24.dp), tint = Color(0xFF783E19))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 17.sp, lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF38291F))
                Spacer(Modifier.height(6.dp))
                Text(descricao, fontSize = 12.sp, lineHeight = 16.sp, color = Color(0xFF7E7066))
            }
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = Color(0xFF783E19))
        }
    }
}

// Exibe Ações rápidas na prévia do editor.
@Preview(
    // Define o nome da prévia.
    name = "Ações rápidas",
    // Mostra o fundo da tela.
    showBackground = true,
    // Define a largura em dp.
    widthDp = 412,
    // Define a altura em dp.
    heightDp = 917
)

@Composable
private fun PreviaTelaAdicionar() {
    MaterialTheme {
        TelaAdicionar(
            // Mantém as ações vazias apenas na prévia.
            aoVoltar = {},
            // Representa o acesso ao início.
            aoInicio = {},
            // Representa o acesso à agenda.
            aoAgenda = {},
            // Representa o acesso aos animais.
            aoAnimais = {},
            // Representa o cadastro de um animal.
            aoCadastrarBichinho = {},
            // Representa o registro de doação.
            aoRegistrarDoacao = {},
            // Representa o registro de despesa.
            aoRegistrarDespesa = {},
            // Representa o agendamento de cuidado.
            aoAgendarCuidado = {},
            // Representa a criação de postagem.
            aoCriarPostagem = {},
            // Representa o acesso às parcerias.
            aoParcerias = {},
            // Representa o acesso à divulgação.
            aoDivulgacao = {}
        )
    }
}
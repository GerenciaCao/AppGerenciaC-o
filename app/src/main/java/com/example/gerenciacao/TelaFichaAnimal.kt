package com.example.gerenciacao

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Define as cores da ficha individual.
// Usa o mesmo fundo das telas internas do aplicativo.
private val FundoFicha = FundoPadraoApp
private val LaranjaFicha = Color(0xFFFFAD43)
private val MarromFicha = Color(0xFF493020)
private val PessegoFicha = Color(0xFFFFDFB8)

// Mostra os dados do animal selecionado na lista.
// Os campos adicionais têm valores padrão até serem incluídos no cadastro.
@Composable
fun TelaFichaAnimal(
    animal: AnimalExemplo,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAgenda: () -> Unit,
    aoAdicionar: () -> Unit,
    aoEditar: () -> Unit,
    aoAcompanharAdocao: () -> Unit,
    corPelagem: String = animal.corPelagem,
    pesoPorte: String = animal.porte,
    comportamento: String = animal.comportamento,
    resumoAdocao: String = "",
    etapasAdocao: List<String> = emptyList()
) {
    // Controla os detalhes complementares e o acompanhamento na própria ficha.
    var adocaoAberta by rememberSaveable(animal.id) { mutableStateOf(false) }

    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }

    // Carrega a fotografia original, preservando a imagem enviada pelo usuário.
    val estadoFoto = lembrarFotoOriginalFicha(animal.fotoUri)
    val foto = estadoFoto.imagem

    // Reúne os registros para leitura sem gravar textos duplicados no cadastro.
    val historicoExibido = listOfNotNull(
        animal.vacinacoes.takeIf { it.isNotBlank() }?.let { "Vacinações\n" + it },
        animal.condicoesSaude.takeIf { it.isNotBlank() }?.let { "Condições de saúde\n" + it },
        animal.medicamentos.takeIf { it.isNotBlank() }?.let { "Medicamentos em uso\n" + it },
        animal.historicoMedico.takeIf { it.isNotBlank() }
    ).joinToString("\n\n")
    // Define a cor da disponibilidade cadastrada.
    val corSituacao = when (animal.situacao) {
        "Disponível" -> Color(0xFF96C665)
        "Visita marcada" -> Color(0xFFFFDE83)
        "Em tratamento" -> Color(0xFFFFBD83)
        "Adotado" -> Color(0xFFFFAAAA)
        else -> Color(0xFFE5DFD5)
    }

    // Mostra o símbolo correspondente ao sexo informado.
    val simboloSexo = when (animal.sexo) {
        "Fêmea" -> "♀"
        "Macho" -> "♂"
        else -> ""
    }

    val corSexo = if (animal.sexo == "Fêmea") {
        Color(0xFFFF6587)
    } else {
        Color(0xFF428ACB)
    }

    // Converte a informação de castração em um texto legível.
    val castracao = when (animal.castrado) {
        true -> if (animal.sexo == "Fêmea") "Castrada" else "Castrado"
        false -> if (animal.sexo == "Fêmea") {
            "Não castrada"
        } else {
            "Não castrado"
        }
        null -> "Não informado"
    }

    BackHandler(onBack = aoVoltar)

    // Mantém a estrutura e o menu inferior já usados no projeto.
    MolduraPadraoApp(
        containerColor = FundoFicha,
        // Oculta o botão de mais somente na ficha individual.
        floatingActionButton = {},
        snackbarHost = {
            SnackbarHost(avisos)
        },
        bottomBar = {
            MenuInferiorInicio(
                avisar,
                selecionado = "Animais",
                aoInicio = aoInicio,
                aoAnimais = aoVoltar,
                aoAdicionar = aoAdicionar,
                aoAgenda = aoAgenda
            )
        }
    ) { espacos ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacos)
                .background(FundoFicha)
                .verticalScroll(rememberScrollState())
        ) {
            // Usa o mesmo padrão de retorno de Novo compromisso, sem outro fundo laranja.
            Row(
                modifier = Modifier
                    // Ocupa toda a largura disponível.
                    .fillMaxWidth()
                    // Repete as margens das telas de ação do aplicativo.
                    .padding(horizontal = 22.dp)
                    // Separa o título do cabeçalho e da foto.
                    .padding(top = 22.dp, bottom = 12.dp),
                // Mantém nome, sexo e disponibilidade alinhados verticalmente.
                verticalAlignment = Alignment.CenterVertically,
                // Separa os elementos do título.
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TituloMaiorFicha(
                    // Mostra o nome do animal selecionado, em vez de Ficha Individual.
                    titulo = animal.nome,
                    // Mantém o símbolo junto ao nome em qualquer ficha.
                    simboloSexo = simboloSexo,
                    // Mantém a cor correspondente ao sexo cadastrado.
                    corSexo = corSexo,
                    // Usa a mesma seta de retorno das outras telas de ação.
                    aoVoltar = aoVoltar,
                    // Reserva o espaço restante para o nome.
                    modifier = Modifier.weight(1f)
                )



                Surface(
                    modifier = Modifier
                        // Evita que uma situação longa ocupe o cabeçalho inteiro.
                        .widthIn(max = 140.dp),
                    // Mantém a etiqueta arredondada.
                    shape = RoundedCornerShape(24.dp),
                    // Usa a cor da disponibilidade do animal.
                    color = corSituacao
                ) {
                    Text(
                        // Exibe a situação real da ficha.
                        text = animal.situacao,
                        modifier = Modifier
                            // Define o respiro interno da etiqueta.
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        // Mantém a etiqueta secundária ao nome.
                        // Aumenta a leitura da disponibilidade.
                        fontSize = 15.sp,
                        // Permite quebrar a situação em mais de uma linha se necessário.
                        lineHeight = 19.sp,
                        // Destaca a disponibilidade.
                        fontWeight = FontWeight.Bold,
                        // Mantém contraste com a cor de fundo.
                        color = MarromFicha
                    )
                }
            }
            // Apresenta a foto original em um recipiente padrão para todas as fichas.
            Surface(
                // Mantém as margens e a altura iguais para todos os animais.
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                    .padding(bottom = 20.dp).height(240.dp),
                // Arredonda os cantos do recipiente.
                shape = RoundedCornerShape(20.dp),
                // Mantém um fundo neutro nas áreas não ocupadas pela foto.
                color = Color(0xFF96D7F4),
                // Delimita a imagem de maneira discreta.
                border = BorderStroke(1.dp, Color(0xFFE6DDD3))
            ) {
                Box(
                    // Usa toda a área disponível dentro dos cantos arredondados.
                    modifier = Modifier.fillMaxSize(),
                    // Centraliza a fotografia ou o estado vazio.
                    contentAlignment = Alignment.Center
                ) {
                    val imagem = foto
                    if (imagem != null) {
                        Image(
                            // Mostra a fotografia do animal selecionado, incluindo o cenário original.
                            bitmap = imagem,
                            // Identifica o animal da foto.
                            contentDescription = "Foto de " + animal.nome,
                            // Ocupa a área padrão sem mudar a altura da ficha.
                            modifier = Modifier.fillMaxSize(),
                            // PNG transparente aparece inteiro; foto opaca preenche a área com recorte central.
                            contentScale = if (estadoFoto.transparente) {
                                // Preserva o recorte inteiro sobre o azul.
                                ContentScale.Fit
                            } else {
                                // Preenche o recipiente sem faixas laterais e sem esticar a foto.
                                ContentScale.Crop
                            },
                            // Centraliza fotos verticais, horizontais e PNGs transparentes.
                            alignment = Alignment.Center
                        )
                    } else if (estadoFoto.carregando) {
                        CircularProgressIndicator(
                            // Mantém o indicador proporcional ao recipiente.
                            modifier = Modifier.size(28.dp),
                            // Usa a cor dos controles do aplicativo.
                            color = MarromFicha
                        )
                    } else {
                        Column(
                            // Mantém respiro em torno da indicação de ausência de imagem.
                            modifier = Modifier.padding(24.dp),
                            // Centraliza ícone e mensagem.
                            horizontalAlignment = Alignment.CenterHorizontally,
                            // Separa a patinha da mensagem.
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                // Usa a patinha neutra já existente no projeto.
                                painter = painterResource(R.drawable.inicio_pata),
                                // A mensagem abaixo descreve o estado.
                                contentDescription = null,
                                // Mantém o ícone proporcional ao recipiente.
                                modifier = Modifier.size(40.dp),
                                // Usa um marrom suave.
                                tint = MarromFicha.copy(alpha = 0.45f)
                            )
                            Text(
                                // Diferencia uma imagem ausente de uma falha de leitura.
                                text = if (estadoFoto.erro) "Não foi possível abrir a foto" else "Sem foto cadastrada",
                                // Mantém o aviso legível.
                                fontSize = 14.sp,
                                // Centraliza uma mensagem que ocupe mais de uma linha.
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                // Usa a cor auxiliar da ficha.
                                color = Color(0xFF796B60)
                            )
                        }
                    }
                }
            }
            // Separa a fotografia dos dados com apenas uma linha horizontal.
            HorizontalDivider(
                // Mantém a linha alinhada ao conteúdo.
                modifier = Modifier.padding(horizontal = 22.dp),
                // Usa um traço discreto.
                thickness = 1.dp,
                // Mantém a cor suave das divisórias do protótipo.
                color = Color(0xFFE6DDD3)
            )
            Column(
                // Mantém margens uniformes sem criar moldura externa.
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)
                    .padding(top = 22.dp, bottom = 16.dp)
            ) {
                TituloSecaoLeituraFicha("Dados do animal")
                // Aproxima a primeira linha do título da seção.
                Spacer(Modifier.height(8.dp))
                LinhaLeituraFicha(
                    // Identifica a espécie.
                    titulo = "Espécie",
                    // Mantém a nomenclatura singular na ficha.
                    valor = when (animal.especie) {
                        "Cães" -> "Cão"
                        "Gatos" -> "Gato"
                        else -> animal.especie
                    }
                )
                LinhaLeituraFicha("Raça", animal.raca)
                LinhaLeituraFicha("Idade", animal.idade)
                LinhaLeituraFicha("Cor", corPelagem)

                // Separa os dados objetivos do texto sobre o animal.
                Spacer(Modifier.height(28.dp))
                TituloSecaoLeituraFicha("Sobre " + animal.nome)
                // Aproxima o parágrafo do seu título.
                Spacer(Modifier.height(12.dp))
                Text(
                    // Exibe a descrição recebida, sem limitar artificialmente as linhas.
                    text = animal.sobre.ifBlank { "Sem descrição cadastrada." },
                    // Mantém a descrição legível como no PDF.
                    fontSize = 17.sp,
                    // Define respiro entre as linhas.
                    lineHeight = 25.sp,
                    // Mantém o marrom principal.
                    color = MarromFicha
                )

                if (animal.observacoes.isNotBlank()) {
                    // Mostra as observações complementares sem criar um bloco vazio.
                    Spacer(Modifier.height(20.dp))
                    TituloSecaoLeituraFicha("Observações")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        // Exibe o texto que foi salvo no formulário.
                        text = animal.observacoes,
                        // Mantém a leitura dos demais parágrafos.
                        fontSize = 16.sp,
                        // Separa as linhas.
                        lineHeight = 23.sp,
                        // Usa o marrom principal.
                        color = MarromFicha
                    )
                }
                // Separa os cuidados do texto de apresentação.
                Spacer(Modifier.height(28.dp))
                TituloSecaoLeituraFicha("Saúde e cuidados")
                // Aproxima as linhas do título.
                Spacer(Modifier.height(8.dp))
                LinhaLeituraFicha("Status reprodutivo", castracao)
                // Mantém peso e porte juntos porque o cadastro atual fornece um único texto.
                LinhaLeituraFicha("Porte", pesoPorte)
                LinhaLeituraFicha("Comportamento", comportamento)

                // Separa o histórico das informações de rotina.
                Spacer(Modifier.height(28.dp))
                TituloSecaoLeituraFicha("Histórico de saúde")
                // Separa o título da área de registros.
                Spacer(Modifier.height(12.dp))
                Surface(
                    // Mantém a área de registros na largura da ficha.
                    modifier = Modifier.fillMaxWidth(),
                    // Usa o fundo suave do PDF.
                    color = Color(0xFFFFF1DF),
                    // Arredonda somente o bloco do histórico.
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        // Define o espaço interno do bloco de registros.
                        modifier = Modifier.padding(16.dp),
                        // Separa o aviso vazio da explicação.
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (historicoExibido.isBlank()) {
                            Text(
                                // Explica a ausência de registros sem inventar tratamentos.
                                text = "Nenhum registro cadastrado",
                                // Destaca o estado vazio.
                                fontSize = 16.sp,
                                // Destaca o título do aviso.
                                fontWeight = FontWeight.SemiBold,
                                // Usa o marrom da ficha.
                                color = MarromFicha
                            )
                            Text(
                                // Explica o conteúdo futuro da seção.
                                text = "Vacinas, consultas e tratamentos aparecerão aqui quando forem registrados.",
                                // Mantém a explicação secundária.
                                fontSize = 14.sp,
                                // Mantém a leitura em mais de uma linha.
                                lineHeight = 20.sp,
                                // Usa a cor auxiliar do protótipo.
                                color = Color(0xFF796B60)
                            )
                        } else {
                            Text(
                                // Exibe somente o histórico do animal selecionado.
                                text = historicoExibido,
                                // Mantém os registros legíveis.
                                fontSize = 16.sp,
                                // Separa as linhas dos registros.
                                lineHeight = 23.sp,
                                // Mantém contraste com o fundo claro.
                                color = MarromFicha
                            )
                        }
                    }
                }

                // Mantém a adoção integrada à ficha, abaixo das informações de saúde.
                Spacer(Modifier.height(28.dp))
                TituloSecaoLeituraFicha("Adoção")
                // Separa o título do resumo.
                Spacer(Modifier.height(12.dp))
                Text(
                    // Usa o acompanhamento recebido; não inventa interessados.
                    text = resumoAdocao.ifBlank { "Nenhum acompanhamento informado." },
                    // Mantém o resumo legível.
                    fontSize = 16.sp,
                    // Separa as linhas.
                    lineHeight = 23.sp,
                    // Mantém a informação secundária.
                    color = Color(0xFF796B60)
                )
                // Separa o resumo do botão.
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    // Abre diretamente a tela de acompanhamento deste animal.
                    onClick = aoAcompanharAdocao,
                    // Mantém o botão na largura do conteúdo.
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    // Mantém o formato aprovado no PDF.
                    shape = RoundedCornerShape(14.dp),
                    // Usa um contorno marrom discreto.
                    border = BorderStroke(1.dp, MarromFicha),
                    // Mantém o texto marrom sobre o fundo da ficha.
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MarromFicha)
                ) {
                    Text(
                        // Define o título fixo do botão.
                        text = "Acompanhar adoção",
                        // Mantém o tamanho do texto dos botões.
                        fontSize = 16.sp,
                        // Destaca a ação.
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Separa a ação principal do acompanhamento.
                Spacer(Modifier.height(24.dp))
                Button(
                    // Abre o formulário de edição já existente.
                    onClick = aoEditar,
                    // Usa toda a largura com uma área de toque confortável.
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    // Mantém os cantos arredondados do PDF.
                    shape = RoundedCornerShape(14.dp),
                    // Usa o marrom como fundo da ação principal.
                    colors = ButtonDefaults.buttonColors(
                        // Define o fundo do botão.
                        containerColor = MarromFicha,
                        // Mantém contraste no texto.
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        // Identifica a ação principal.
                        text = "Editar ficha",
                        // Mantém o texto legível.
                        fontSize = 16.sp,
                        // Destaca a ação.
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }


}

// Aumenta apenas o título da ficha, preservando os títulos das outras telas.
@Composable
private fun TituloMaiorFicha(
    // Recebe o nome do animal selecionado.
    titulo: String,
    // Retorna à página que abriu a ficha.
    aoVoltar: () -> Unit,
    // Recebe o espaço disponível na linha do cabeçalho.
    modifier: Modifier = Modifier,
    // Recebe o símbolo referente ao sexo do animal selecionado.
    simboloSexo: String = "",
    // Recebe a cor do símbolo sem mudar a cor do nome.
    corSexo: Color = MarromFicha
) {
    Row(
        // Mantém altura confortável para tocar na seta.
        modifier = modifier.heightIn(min = 56.dp),
        // Centraliza a seta e o nome verticalmente.
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            // Executa a ação de retorno já existente.
            onClick = aoVoltar,
            // Mantém a área de toque padrão.
            modifier = Modifier.size(48.dp)
        ) {
            androidx.compose.foundation.Canvas(
                // Define o tamanho e a identificação acessível da seta.
                modifier = Modifier.size(28.dp).semantics { contentDescription = "Voltar" }
            ) {
                // Define a ponta da seta.
                val ponta = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.50f)
                // Define as extremidades dos três segmentos.
                val extremidades = listOf(
                    androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.50f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.20f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.80f)
                )
                extremidades.forEach { fim ->
                    drawLine(
                        // Mantém o marrom dos controles.
                        color = MarromFicha,
                        // Inicia os segmentos na ponta.
                        start = ponta,
                        // Define a extremidade de cada segmento.
                        end = fim,
                        // Mantém a espessura da seta padrão.
                        strokeWidth = 2.5.dp.toPx(),
                        // Arredonda as pontas.
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
        // Separa a seta do nome.
        Spacer(Modifier.width(8.dp))
        Text(
            // Mostra o nome real do animal.
            text = androidx.compose.ui.text.buildAnnotatedString {
                // Mantém nome e símbolo dentro do mesmo texto, sem espaço flexível entre eles.
                append(titulo)
                if (simboloSexo.isNotEmpty()) {
                    // Separa o símbolo do nome com somente um espaço.
                    append(" ")
                    // Aplica a cor e a medida somente ao símbolo.
                    pushStyle(
                        androidx.compose.ui.text.SpanStyle(
                            // Mantém a cor correspondente ao sexo.
                            color = corSexo,
                            // Mantém o símbolo proporcional ao nome.
                            fontSize = 28.sp
                        )
                    )
                    append(simboloSexo)
                    pop()
                }
            },
            // Permite que nomes longos ocupem mais de uma linha.
            modifier = Modifier.weight(1f),
            // Aumenta somente o nome nesta ficha.
            fontSize = 30.sp,
            // Define o espaço entre linhas do nome.
            lineHeight = 36.sp,
            // Destaca o nome como título.
            fontWeight = FontWeight.Bold,
            // Mantém a cor principal da ficha.
            color = MarromFicha
        )
    }
}

// Identifica uma seção sem criar caixas ou títulos gigantes.
@Composable
private fun TituloSecaoLeituraFicha(
    // Recebe o título da seção.
    titulo: String
) {
    Text(
        // Mostra o título.
        text = titulo,
        // Mantém a hierarquia aprovada no PDF.
        fontSize = 20.sp,
        // Define a altura das linhas do título.
        lineHeight = 26.sp,
        // Dá destaque à seção.
        fontWeight = FontWeight.Bold,
        // Mantém o marrom principal.
        color = MarromFicha
    )
}

// Alinha o rótulo à esquerda e o dado à direita, com uma divisória abaixo.
@Composable
private fun LinhaLeituraFicha(
    // Identifica a informação.
    titulo: String,
    // Recebe o valor cadastrado.
    valor: String
) {
    // Padroniza a apresentação de dados ausentes.
    val semDado = valor.isBlank() || valor.trim().equals("Não informado", ignoreCase = true)
    Column {
        Row(
            // Deixa a altura crescer quando há textos longos ou fonte ampliada.
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 12.dp),
            // Separa o rótulo do valor.
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // Alinha pelo topo para manter a leitura em múltiplas linhas.
            verticalAlignment = Alignment.Top
        ) {
            Text(
                // Mostra o nome do campo de forma discreta.
                text = titulo,
                // Reserva parte da linha para o rótulo.
                modifier = Modifier.weight(0.46f),
                // Mantém o rótulo menor que o dado.
                fontSize = 15.sp,
                // Define a distância entre linhas do rótulo.
                lineHeight = 22.sp,
                // Usa a cor auxiliar do PDF.
                color = Color(0xFF796B60)
            )
            Text(
                // Usa dois hífens quando não há informação.
                text = if (semDado) "--" else valor.trim(),
                // Reserva o restante da linha e informa o significado dos hífens.
                modifier = Modifier.weight(0.54f).semantics {
                    if (semDado) contentDescription = titulo + ": não informado"
                },
                // Destaca o valor do campo.
                fontSize = 18.sp,
                // Mantém a leitura confortável em mais de uma linha.
                lineHeight = 24.sp,
                // Destaca o dado sem usar títulos grandes.
                fontWeight = FontWeight.SemiBold,
                // Alinha os valores pela mesma margem direita.
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                // Suaviza o indicador de ausência de dados.
                color = if (semDado) Color(0xFF796B60) else MarromFicha
            )
        }
        HorizontalDivider(
            // Usa um traço fino entre as informações.
            thickness = 1.dp,
            // Mantém a cor discreta das divisórias do PDF.
            color = Color(0xFFE6DDD3)
        )
    }
}
// Guarda o resultado da leitura sem misturar fotos de animais diferentes.
private data class EstadoFotoOriginalFicha(
    // Identifica a fotografia à qual o resultado pertence.
    val origem: String? = null,
    // Guarda a imagem pronta para exibição.
    val imagem: ImageBitmap? = null,
    // Identifica transparência real nos pixels, sem depender da extensão do arquivo.
    val transparente: Boolean = false,
    // Indica que a leitura ainda está em andamento.
    val carregando: Boolean = false,
    // Indica que a fotografia cadastrada não pôde ser lida.
    val erro: Boolean = false
)

// Carrega a fotografia original com redução de memória e correção de orientação.
@Composable
private fun lembrarFotoOriginalFicha(
    // Recebe o endereço da imagem cadastrada.
    endereco: String?
): EstadoFotoOriginalFicha {
    // Obtém o leitor de imagens do Android.
    val contexto = LocalContext.current
    val resultado by produceState(
        // Inicia sem reaproveitar a foto de outro animal.
        initialValue = EstadoFotoOriginalFicha(
            origem = endereco,
            carregando = !endereco.isNullOrBlank()
        ),
        // Refaz a leitura quando o endereço da foto muda.
        key1 = endereco,
        // Refaz a leitura quando o contexto muda.
        key2 = contexto
    ) {
        if (endereco.isNullOrBlank()) {
            value = EstadoFotoOriginalFicha(origem = endereco)
        } else {
            value = EstadoFotoOriginalFicha(origem = endereco, carregando = true)
            try {
                val imagem = withContext(Dispatchers.IO) {
                    val uri = Uri.parse(endereco)
                    val resolver = contexto.contentResolver
                    val limites = BitmapFactory.Options().apply {
                        // Consulta as dimensões sem carregar toda a foto.
                        inJustDecodeBounds = true
                    }
                    resolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, limites)
                    }
                    if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                        throw java.io.IOException("Imagem inválida.")
                    }
                    val opcoes = BitmapFactory.Options().apply {
                        // Limita a resolução para não carregar uma foto gigante na memória.
                        inSampleSize = 1
                        while (maxOf(limites.outWidth, limites.outHeight) / inSampleSize > 1600) {
                            inSampleSize *= 2
                        }
                        // Preserva as partes transparentes quando a foto já é PNG.
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val original = resolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, opcoes)
                    } ?: throw java.io.IOException("Não foi possível ler a imagem.")

                    // Lê a rotação da câmera usando a API que já existe no Android 7 ou superior.
                    val orientacao = runCatching {
                        resolver.openInputStream(uri)?.use {
                            android.media.ExifInterface(it).getAttributeInt(
                                android.media.ExifInterface.TAG_ORIENTATION,
                                android.media.ExifInterface.ORIENTATION_NORMAL
                            )
                        } ?: android.media.ExifInterface.ORIENTATION_NORMAL
                    }.getOrDefault(android.media.ExifInterface.ORIENTATION_NORMAL)

                    val matriz = android.graphics.Matrix()
                    when (orientacao) {
                        android.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matriz.setScale(-1f, 1f)
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matriz.setRotate(180f)
                        android.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                            matriz.setRotate(180f)
                            matriz.postScale(-1f, 1f)
                        }
                        android.media.ExifInterface.ORIENTATION_TRANSPOSE -> {
                            matriz.setRotate(90f)
                            matriz.postScale(-1f, 1f)
                        }
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matriz.setRotate(90f)
                        android.media.ExifInterface.ORIENTATION_TRANSVERSE -> {
                            matriz.setRotate(-90f)
                            matriz.postScale(-1f, 1f)
                        }
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matriz.setRotate(-90f)
                    }
                    val corrigida = if (matriz.isIdentity) original else {
                        // Gira apenas a cópia em memória, preservando o arquivo enviado.
                        Bitmap.createBitmap(
                            original, 0, 0, original.width, original.height, matriz, true
                        )
                    }
                    // Analisa os pixels; hasAlpha sozinho também pode ser verdadeiro em uma foto opaca.
                    val preparacao = prepararTransparenciaFicha(corrigida)
                    preparacao.first.asImageBitmap() to preparacao.second
                }
                value = EstadoFotoOriginalFicha(
                    // Associa o resultado ao animal correto.
                    origem = endereco,
                    // Exibe a foto ou o recorte transparente preparado.
                    imagem = imagem.first,
                    // Escolhe automaticamente o fundo e o enquadramento.
                    transparente = imagem.second
                )
            } catch (cancelado: kotlinx.coroutines.CancellationException) {
                // Cancela a leitura quando a ficha é fechada.
                throw cancelado
            } catch (_: Exception) {
                // Mantém uma indicação neutra quando a foto não está acessível.
                value = EstadoFotoOriginalFicha(origem = endereco, erro = true)
            }
        }
    }
    // Impede mostrar a foto anterior durante a troca de animal.
    return if (resultado.origem == endereco) resultado else {
        EstadoFotoOriginalFicha(origem = endereco, carregando = !endereco.isNullOrBlank())
    }
}
// Reconhece transparência real e remove apenas margens externas vazias do PNG.
private fun prepararTransparenciaFicha(
    // Recebe a foto com a orientação já corrigida.
    bitmap: Bitmap
): Pair<Bitmap, Boolean> {
    // Fotos sem canal de transparência usam Crop diretamente.
    if (!bitmap.hasAlpha()) return bitmap to false

    val pixels = IntArray(bitmap.width)
    var transparente = false
    var esquerda = bitmap.width
    var direita = -1
    var topo = bitmap.height
    var base = -1

    for (y in 0 until bitmap.height) {
        // Lê uma linha por vez para não criar outro vetor do tamanho da imagem inteira.
        bitmap.getPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, 1)
        for (x in pixels.indices) {
            val alpha = pixels[x] ushr 24
            // Detecta áreas realmente transparentes ou parcialmente transparentes.
            if (alpha < 250) transparente = true
            // Localiza os limites do conteúdo visível.
            if (alpha >= 16) {
                esquerda = minOf(esquerda, x)
                direita = maxOf(direita, x)
                topo = minOf(topo, y)
                base = maxOf(base, y)
            }
        }
    }
    // Um PNG sem pixels transparentes deve preencher o recipiente como uma foto comum.
    if (!transparente) return bitmap to false
    if (direita < esquerda || base < topo) {
        throw java.io.IOException("A imagem está completamente transparente.")
    }
    // Mantém uma pequena margem para não encostar o animal nas bordas.
    val margem = (maxOf(direita - esquerda + 1, base - topo + 1) * 0.04f).toInt()
    val x = (esquerda - margem).coerceAtLeast(0)
    val y = (topo - margem).coerceAtLeast(0)
    val limiteDireito = (direita + 1 + margem).coerceAtMost(bitmap.width)
    val limiteInferior = (base + 1 + margem).coerceAtMost(bitmap.height)

    // Retira somente os espaços transparentes excessivos; não remove cenário nem altera o animal.
    val recorte = Bitmap.createBitmap(
        bitmap, x, y, limiteDireito - x, limiteInferior - y
    )
    return recorte to true
}

// Mostra a ficha no painel de prévia do Android Studio.
@Preview(
    name = "Ficha do animal",
    showBackground = true,
    widthDp = 412,
    heightDp = 917
)
@Composable
private fun PreviaTelaFichaAnimal() {
    MaterialTheme {
        TelaFichaAnimal(
            // Usa a foto de demonstração quando o modo estiver ativo.
            animal = criarAnimaisDemonstracao(LocalContext.current).firstOrNull() ?: AnimaisExemplo.first(),
            aoVoltar = {},
            aoInicio = {},
            aoAgenda = {},
            aoAdicionar = {},
            aoEditar = {},
            aoAcompanharAdocao = {}
        )
    }
}
package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.WebhookUtils.getOrCreateWebhook
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class TioDoPaveCommand : AbstractCommand("tiodopave", listOf("piada"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.tiodopave.description")

	companion object {
		// https://www.reddit.com/r/tiodopave/top/?sort=top&t=all&count=375&after=t3_666izg
		val PIADAS = listOf(
				"Você pode até escrever uma carta para todos os cachorros do mundo...\n\n... mas só o Rottweiler",
				"Porque a empregada não luta Jiu Jitsu?\n\nPorque ela luta capoeira",
				"Por que o Brasil é cheiroso?\n\nPorque ele já foi colônia.",
				"Sempre que minha sobrinha assiste comercial de remédio na TV ela ri\n\nSerá pq o ministério da saúde adverte?",
				"Por que no quartel não falta energia elétrica?\n\nPorque todos os cabos foram soldados",
				"Por que o vidente se mudou para Berlim?\n\nPara aprender a-lê-mão",
				"Por que o A sempre vai pra cadeia?\n\nPorque o @",
				"Pq é importante ter uma cobra no navio?\n\nPorque quando alguém se afoga ela dá o bote.",
				"Se papai noel morrer...\n\nEle não estará mais em trenós.",
				"Por que pessoas com bafo falam tanto?\n\nPara desabafar",
				"Epilepsia...\n\n...um assunto a se debater.",
				"O Faustão está num restaurante com a esposa e um cara chamado Lô...\n\nA esposa pergunta, \"cadê o pão que estava aqui?\" E o Faustão responde, \"O Lô comeu.\"",
				"Por que os jardineiros aconselham que você não cuide de plantas pequenas?\n\n...porque dessa prática nada de bonsai.",
				"Como o padre se energizar quando tá com sono?\n\nCafé",
				"Qual é o XMEN que pede para crescer?\n\nA Mística, entenderam? Mi-istica",
				"O que os ditadores da URSS faziam no telefone?\n\nPassavam Trotsky.",
				"Sabe qual o carro que saiu do forno?\n\nO Kia Soul",
				"Por que em dias frios eu fico mais atraente?\n\nPorque eu fico sensuar",
				"- Manoel, tu sabias que as caixas pretas dos aviões na verdade são laranjas?\n\nO que?! Não são caixas?!",
				"O que acontece quando chove na Inglaterra?\n\nVira Inglabarro.",
				"Imagina se todos os mosquitos sumissem da Terra?\n\nAí seria o fim da picada.",
				"Uma vez uma menina chamada Guerra se olhou no espelho. Sabe o que aconteceu?\n\nGuerra civil.",
				"qual o contrário de \"paixão\"?\n\nMãeteto",
				"Fui numa vidente e ela me exigiu de pagamento meu carro...\n\nEla realmente é uma excelente car tomante.",
				"Qual o alimento que tem a melhor bunda de todas?\n\nA better raba",
				"Sabe qual o rapper brasileiro que sempre abre portas?\n\nGabriel opens a door",
				"O que que é uma rena + uma rena?\n\nUm cálculo renal",
				"Na capital do Rio Grande do Norte já é Natal!\n\n.",
				"Por que o pinheiro nunca se perde?\n\nPorque ele tem uma pinha",
				"O que é vermelho, mas cheira que nem tinta azul?\n\nTinta Vermelha",
				"O que acontece quando o Jabuti entra em extinção?\n\nJabuticaba",
				"Qual é o cantor que sabe fazer planilhas?\n\nO Excel Rose",
				"Quem fala errado, a Mônica ou o Cebolinha?\n\nA Mônica. O Cebolinha fala \"elado\". Ouvi de um mendigo",
				"Por que o Goku, Vegeta e Gohan tiraram nota 10 em nado sincronizado?\n\nPorque eles estavam super ensaiyajinhos.",
				"Qual é a tia que não te deixa comer doces?\n\nA tia Bete",
				"quando ficar triste, procure sapatos.\n\n...um sapato com sola",
				"Quem vota no Trump e no PT?\n\nO Trumpetista 🎷",
				"Não gosto de ir ao podólogo...\n\nporque lá sempre pegam no meu pé. :/",
				"qual carro movido a suco?\n\nR: musTANG",
				"Qual o contrário de volátil?\n\nVem cá sobrinho.",
				"Visitando os EUA fiquei impressionado com o nível da educação pública americana\n\nVárias crianças de 3-4 anos já falando inglês",
				"Meu amigo foi até Belém só por causa de uma mina...\n\n... Meu Deus, aonde ele foi pará",
				"O pai lava o carro com o filho\n\nApós alguns momentos o filho fala: pai você não pode usar uma esponja?",
				"Qual lugar mais certo do Brasil?\n\nO sertão.",
				"Hoje morreu o inventor do corretor automático.\n\nQue descafeinado em paz.",
				"Fui viajar para o Japão e deixei meu cão com a minha irmã. Semana passada, eu soube que ele adoeceu.....\n\nE agora perdi meu cão por eutanásia.",
				"Os ursos polares adoram o frio...\n\nOs ursos bipolares às vezes adoram, às vezes não.",
				"O Harry Potter foi morar um tempo com os tios dele...\n\nE o tio dele jantou e deixou toda a louça em cima da pia. Sabe o que o Harry falou quando viu toda a sujeira? Vemcátio Lavealouça Entenderam? Entenderam?",
				"Sabe porque Napoleão era chamado pra todas as festas?\n\nPorque ele era bom na party",
				"Quais são as três ferramentas que o gaúcho mais usa?\n\nAlicatchê Serrotchê E o martelo. Sabe porque o martelo? PORQUE ELE BATCHÊ",
				"qual vitamina vem depois do Zinco?\n\nZeis",
				"Aonde o mamão foi?\n\nPapaia",
				"Porque não se deve comer a Bíblia?\n\nPorque tem salmonela.",
				"Um gago morreu na prisao\n\nAntes de completar a sentença.",
				"Qual é o emprego mais comum no México?\n\nEncanador, pois lá tem muito mexicano.",
				"O que um álcool disse pro outro?\n\n\"Eta-nóis\"",
				"o que é um terapeuta?\n\n1024 gigapeutas.",
				"O que aconteceu quando o policial se olhou no espelho?\n\nO policial civil",
				"Choveu achocolatado\n\nMe molhei toddynho",
				"O que é um pontinho vermelho que pula e dança na selva?\n\nUm MORANGO-TANGO ehuehuehueh Desculpe.",
				"Qual é o nome do filho do Haddad?\n\nHadson.",
				"Porque o Mário pequeno é melhor nas fases aquáticas?\n\nPorque ele é o marinho",
				"Qual é a especialidade de um técnico em informática evangélico?\n\nConverter arquivos",
				"Um vigia noturno, um triatleta e um padre apostam uma corrida. quem chega primeiro?\n\no vigia, porque segurança em primeiro lugar.",
				"Qual o nome da régua feita de Oxigênio?\n\nArquimedes",
				"Por que Hitler não conquistou a Rússia?\n\nPorque ele Moscou",
				"Vocês sabiam?\n\nSe o Hino for cantado de trás pra frente, ele deixar de ser Hino e passa a se chamar \"Voltano\".",
				"Porque a fita isolante é melhor que a fita crepe?\n\nPorque ela é faixa preta 🥋",
				"Em plena primavera, registramos neve em Gramado.\n\nFlagrante do sinistro",
				"Por que a mala estava servindo café?\n\nPorque ela é uma malabarista",
				"O que o gato faz na academia ?\n\nAbdomiau.",
				"Como são chamadas as favelas italianas?\n\nSpaghettos",
				"se no Brasil, os filmes são baseados em fatos reais\n\nentão nos EUA são baseados em fatos dólares?",
				"Em qual estado correm as águas do rio São Francisco\n\nLíquido.",
				"Qual o estilo musical preferido das plantas?\n\nReggae.",
				"O que o Ash falou qnd o Pikachu ganhou dele no Poquer\n\nPô, que mão",
				"Crianças, esse é o dever de casa de vocês\n\nMas é de ver ou de fazer?",
				"Cheguei em uma estrela e perguntei \"pq vc não mia?\" E ela respondeu\n\n\"astronomia\"",
				"Me ligaram falando de um boleto que venceu\n\nQue bom, estava torcendo por ele.",
				"Quebrei meu braço em dois lugares diferentes\n\nO médico me recomendou parar de ir nesses lugares.",
				"Parece até piada\n\nhttp://i.imgur.com/VRYqHmi.jpg",
				"Sabe porque japonês não cai da moto?\n\nPorque ele senta na moto Yamaha.",
				"Eu não sabia que meu pai tinha um segundo emprego como professor...\n\nMas um dia, quando cheguei em casa, todas as provas estavam lá.",
				"O quê um programador baiano disse pro outro?\n\nOh, meu array!",
				"Qual é o filme em que um salgado lidera um exército em busca da vitória?\n\nCroissaint valente.",
				"Qual é o país onde ninguém sabe nadar?\n\nÉ o Afoganistão",
				"Por que o Banco do Brasil patrocina a seleção brasileira de vôlei?\n\nPorque ele ajuda nos saques.",
				"Um absurdo! Meu vizinho veio bater aqui na porta de casa as 3 horas da madrugada!\n\nSorte dele que eu tava acordado tocando bateria.",
				"Era uma vez um pintinho que não ria...\n\nJoguei ele na parede e ele rachou o bico.",
				"Qual é a diferença entre o Hulk e a Katniss (jogos vorazes)?\n\nO Hulk é verdão, a Katniss éverdin",
				"Por que a cobra resolveu virar uma escova de cabelo?\n\nPorque ela estava cansada de serpente. ENTENDEU? ENTENDEU? SERPENTE -- SER PENTE",
				"Porque a plantinha não vai no médico de domingo?\n\nPorque só tem médico de plantão",
				"Se o Tiririca virasse terrorista, qual seria o grupo dele?\n\nAbestado Islâmico.",
				"Smartphone do caipira\n\nO caipira ganhou um smartphone na loteria, o curioso vai lá e pergunta: Já sabe o que cê vai fazer com o prêmio? O caipira responde: Bom, o fone vou ficar para mim e o smart vou dar pra minha irmã passar nas zunha.",
				"Oi gata! Qual é o seu nome?\n\n-Luzinete -Putz! -O que? não gostou do meu nome? -Não é isso! É que me lembra 2 contas atrasada!",
				"um cara entra na loja sem enxergar e saiu vendo.\n\npagou à vista",
				"O que é um terapeuta?\n\nSão 1024 gigapeutas!",
				"Quando é que o Renato Aragão acorda?\n\nDidia",
				"Tio, me ajuda com o dever de casa?\n\nMas é de ver ou de fazer?",
				"Quanto vale um terapeuta?\n\n1024 gigapeutas",
				"Por que colocaram um pula-pula no navio?\n\nPara os triPULANTES",
				"Aonde moram os minions?\n\nEm condominions.",
				"qual comida é árabe e alemã?\n\nA esführer",
				"pra qual santo você reza quando esquece a senha?\n\nSão Login",
				"O que o pintinho falou quando saiu do ovo?\n\nEstou chocado.",
				"Vocês viram aquele terremoto que aconteceu na Itália?\n\nFoi uma destruição em massa!",
				"Sabe porque o bombeiro não anda?\n\nPorque ele so-corre",
				"Porque você nunca deve perguntar as horas para uma velhinha?\n\nPorque ela é uma senhora",
				"Papai Noel fica doente ao saber que falta uma rena e não pode entregar os presentes...\n\n... ele ficou com insuficiência renal.",
				"Porquê botaram o Guinness Book no liquidificador?\n\nPra bater todos os recordes",
				"Sabe por que tem cama elástica no polo norte?\n\nPro urso polar.",
				"Mês passado, comecei a aprender alemão com meu tio...\n\n... acredita que agora já consigo até prever o futuro das pessoas?",
				"Um homem aceitou tomar 1000 latas de cocas de uma vez, mas só conseguiu tomar 999. Qual o nome do filme?\n\nMil são impossível.",
				"Por que o Lula tem medo de vestir a camisa virada quando compra roupa?\n\nPorque todo mundo é inocente até provar o contrário.",
				"Qual a parte do corpo que é um Deus?\n\nbiGOD",
				"Punhado é um tanto que cabe no punho, bocado é um tanto que cabe na boca...\n\n...ainda bem que não tenho cunhado.👌",
				"O Draco Malfoy\n\ne já voltou.",
				"Sabe porque Hitler não conseguiu tomar a Rússia?\n\nPor que ele Moscou.",
				"Eu adoro o movimento de rotação da Terra...\n\nIsso simplesmente faz o meu dia.",
				"Se o Pantera Negra e a Tempestade tiverem filhos...\n\nEles seriam os Thundercats?",
				"O que são biscoitos recheados caindo do espaço?\n\nMeteóreos.",
				"Quem faz o melhor milkshake do asilo?\n\nO vô Maltine.",
				"Qual é a mulher que sempre aparece em público machucada?\n\nEmma Thomas.",
				"Por que o Chile é um saco?\n\nPorque fica embaixo do Peru.",
				"Quando cai guarana no chão, junta formigas...\n\n...quando cai Guaraná Jesus, junta smilinguidos 😂😂😂",
				"Vou aprender a tricotar\n\nAssim posso viver de renda",
				"Qual a fórmula química da água benta?\n\nH DEUS O",
				"Qual loja de roupas se come na noite do dia 24 de dezembro?\n\nA C&A de Natal.",
				"O que é um ponto branco na neve fazendo abdominal?\n\nO ABDOMINÁVEL homem das neves.",
				"Esse sub me parece bem morto\n\nTeve só 3 posts o ano inteiro! Feliz ano novo!",
				"Deveriam colocar a Nigéria como país integrante do bloco BRICS de países emergentes\n\nPorque aí faríamos parte dos BRINCS",
				"Qual a fruta favorita dos videntes?\n\nLimão",
				"Qual o antivírus que os flintstones usam?\n\nYabadaBAIDU",
				"Para onde é que vai um fã de Senhor dos Anéis quando morre?\n\nPara a floresta de Fangorn. Sabe Por quê? Porque lá é que estão seus Ents Queridos.",
				"receita de lula frita\n\nderreta Mantega adicione molho italiano marca Palocci Pimentel a gosto deixe de Moro até amanhã sirva Dilma vez",
				"Vocês conhecem a piada do vetor sem a pontinha?\n\nEsquece, ela é sem sentido.",
				"Por que a testa do Harry Potter é redonda?\n\nPorque ela tem raio.",
				"Qual é o carro que acabou de sair do forno?\n\nO kia Soul.heheheh",
				"Se te oferecerem um par de óculos sem lente, tome cuidado...\n\n...é uma armação. Huehuehuehue",
				"Já imaginou se chovesse macarrão?\n\nIa ser massa demais.",
				"Eleição Presidencial 2018 no Brasil\n\nÉ isso, essa é a piada.",
				"O que diz o fungo Power Ranger?\n\nÉ HORA DE MOFAR",
				"Cono se diz topless em chinês?\n\nShen shu tian.",
				"Por que o louco, quando joga futebol, só consegue marcar gol com os pés?\n\nPor que ele não bate bem de cabeça.",
				"O que o bispo foi fazer na sala de informática?\n\nConverter arquivos",
				"Bom mesmo é o site do cavalo:\n\nwww.cavalo.com.com.com.com.com.com",
				"Aposto que esta vocês não sabiam...\n\nPlantas pequenas não falam pelo simples fato de serem mudas.",
				"Quantos franceses cabem em um círculo?\n\nDois Pierre.",
				"Qual a cidade que não tem táxi?\n\nUberlândia",
				"O que o matemático foi fazer no banheiro?\n\nFoi fazer π π. (foi mal galera, foi só dessa vez)",
				"O que é um pontinho marrom em 1500?\n\nPedro Álvares Cabrown",
				"Já registrou o menino?\n\nSim, o nome dele é PELÉ! Mas não era Edson? Não. Edson Era Antes do Nascimento..",
				"O que um pinheiro contou para o outro?\n\nUma pinhada",
				"O que o Exaltasamba faz no Céu?\n\nEles tocam PaGod",
				"O que é um ponto escuro na casa do pinóquio?\n\nO Gepreto",
				"Qual o sistema econômico do inferno?\n\nÉ o capetalismo",
				"Eram três irmãos o Pum, Calaboca e Respeito...\n\nO Pum foi preso e o Calaboca foi falar com o delegado e o Respeito ficou do lado de fora esperando. E o delegado disse:\n-Qual é o seu nome?\nCalaboca.\n-Cadê o Respeito?\nEstá lá fora.\n-O que você veio fazer aqui?\nSoltar o Pum.",
				"Qual é o nome do peixe que caiu do décimo andar?\n\nAaaaah tum.",
				"viram a guitarra inspirada no Bob Esponja?\n\né a fender do bikini",
				"Como fala \"paralelepípedo\" em inglês?\n\nSTOPREADREADPIFOOTOF",
				"Qual o almoço preferido do super usuário?\n\nbeiROOT (ou um SUDOiche qualquer, depende da sua distro) (tiodopave opensource)",
				"Um vendedor de instrumentos musicais tenta atirar em mim...\n\nMas tudo bem, o que vende baixo não me atinge.",
				"Como chama o ato de levar seu amigo para fazer um ultrassom?\n\nMANOgrafia",
				"Manuel registrando o filho no cartório...\n\n-- Qual o nome do bebê? -- Ora pois, será Arquibancada do Vasco. -- O que? Esse nome eu não posso colocar! -- E porque não? O meu amigo Joaquim conseguiu dar o nome do filho dele de Geraldo Santos!",
				"lembram que antigamente o açougue embalava as carnes no jornal?\n\nhoje é moderno, o jornal já vem embutido.",
				"Era uma vez um gato com 16 vidas...\n\nQue foi atropelado por uma 4x4 e morreu.",
				"Qual é o maior ovo de SP?\n\nO vão do MASP.",
				"Papai Noel fez tatuagem...\n\n...mas é de Rena.",
				"O que o cebolinha disse depois de chegar atrasado à reunião da maçonaria?\n\nDesculpe, demolay um pouco.",
				"Por que mulheres idosas não costumam ter relógios?\n\nPorque elas são senhoras!",
				"O que é um pontinho verde no Pólo Sul?\n\nUm PinGreen.",
				"Sabe por que a formiga só tem quatro amigas?\n\nPor que se tivessem cinco, seria uma fivemiga",
				"Qual o prato preferido dos maconheiros?\n\nBife aserbolado",
				"Cuidado: camisinhas não são tão seguras quanto você pensa\n\nEstava usando uma e quase fui atropelado por uma moto quando fui atravessar a rua...",
				"Qual é o jogo de luta favorito do gaúcho?\n\nMortal Kom-BAH-TCHÊ",
				"Por que o Spock passou mal?\n\nPorque ele teve um star-treco.",
				"qual a religião favorita dos internautas?\n\numbanda larga",
				"O gaúcho não achava o carro no estacionamento. Qual é o modelo do carro?\n\nKadetchê.",
				"O que o Faustão mede com um multímetro?\n\n(voz do Faustão) A tensão, galera!",
				"O que se pode dizer da água que não cabe dentro de um pote?\n\nQue a água não é potável.",
				"Uma impressora perguntou pra outra:\n\nESSA FOLHA É SUA OU É IMPRESSÃO MINHA?",
				"Quantas patas há em uma dúzia de galinhas?\n\nNenhuma. São galinhas, não patas.",
				"O que o Harry Potter lê quando está doente?\n\nSaramago.",
				"Sabe qual o biscoito favorito do He-Man?\n\nNegreyskull",
				"Eu ia doar sangue hoje mas eles começaram a fazer muitas perguntas...\n\ndo tipo, \"De quem é esse sangue?\" e \"Como você coletou isso?\"",
				"qual o país mais musculoso do mundo?\n\nSómalha (Somália)",
				"O que a mãe açaí disse para seus filhos?\n\nO último açaí fecha a porta",
				"Qual o cúmulo da procrastinação?\n\nDepois te conto...",
				"Como chamavam o Jackson 5 quando eles iam cantar na igreja?\n\nO coro cabeludo",
				"O que são vários chinelos numa tempestade?\n\nRyders on the storm.",
				"FRASE MOTIVACIONAL DO DIA\n\nIndependente de todas as dificuldades que você esteja passando, lembre-se existem caminhos que levam a Vitória e outros que levam a Conquista.. Para Vitória pegue a BR-101.. já para Conquista, vá pela BR 116.",
				"Acho que eu deveria fazer um curso de chaveiro\n\nÉ um curso que abre portas",
				"Por que a caipira tranquila se dá bem no reddit?\n\nPorque ela ta cheia de karma",
				"Está com Frio?\n\nQuando você estiver com frio, é só ir pro canto da sala, lá tá 90 graus.",
				"Por que instalaram escadas no oculista?\n\nPara óculos de-graus",
				"O que o ascensorista disse pro Batman no elevador?\n\nVai DC?",
				"Para que santo rezar quando você esquece a senha?\n\nSão Login",
				"Uma orquestra foi atingida por um raio. Só o maestro sobreviveu.\n\nEle foi despedido por ser um mau condutor.",
				"Por que o Mário pequeno é o melhor nas fases aquáticas?\n\nPorque ele é o Marinho",
				"Por que a camisa da Dilma estava amassada no último debate?\n\nPorque ela passou mal.",
				"O que um matemático disse para um índio?\n\n8 pi.",
				"A namorada do Usain Bolt queria terminar com ele\n\nEntão ele responde: \"Quer terminar? termina, mas depois não adianta correr atrás!\"",
				"Sabe por quê eu não gosto de piada de anão?\n\nPorque é baixaria.",
				"Minha mãe disse que estava com problema de vista...\n\nAí eu instalei o Windows 10 pra ela...",
				"Por que o besouro fez tratamento para rejuvenescimento?\n\nEle queria se tornar um ex-cara-velho.",
				"Cheguei pro meu amigo na joalheria onde ele trabalha.\n\nEu perguntei: \"E aí, tudo jóia?\".",
				"Demoraram muito pra entregar o vinho\n\nCulpa do engarrafamento",
				"Que Deus olhe por mim\n\ne o celso portiolli.",
				"Maior dente do mundo\n\nA: Qual é o maior dente do mundo? B: Não sei, o de dinossauro? A: O do Morais. B: ???? Por que? A: Porque quando ele foi extraído tiveram que fazer uma avenida pro dente de Morais.",
				"Se o cachorro tivesse religião, qual seria?\n\nCão-domblé",
				"O que o pato falou para a pata ?\n\nVem quá",
				"Mas isto dai é maionese...\n\nou é junhonese?",
				"Numa cidade praticamente todas as motos eram Yamaha. Qual era o nome do filme?\n\nPocahondas.heheheehhehehhehehehheeheheheh",
				"Sabe quem Castrou Alves?\n\nO Machado de Assis. Mas essa piada não é minha. Essa é de Queiroz",
				"Qualquer pergunta\n\nExistia um grande sábio, que conseguia responder qualquer pergunta. Um dia, um jovem subiu uma grande montanha em busca do sábio, chegando ao topo encontra um monge meditando, e então pergunta: Mestre Su, me responda, por que todos asiáticos são iguais. O mestre então responde: Eu não sou o mestre Su",
				"Uma gorda foi presa ao ser pega roubando remédio para emagrecer\n\n... agora ela vai ser obrigada a fazer um Regime Fechado.",
				"por quê a velhinha nao usa relogio?\n\nporque ela é um sen hora",
				"o que é um homem morto dirigindo um carro?\n\num mortorista",
				"Qual animal que dissolve na água?\n\nOrangoTANG",
				"Por que o professor de Lingua Portuguesa não para nos semáforos quando conduz o próprio carro?\n\nPorque ele considera \"dirigir\" um verbo transitivo direto.",
				"Qual é o estado americano que só tem tempestade?\n\n... Ohio",
				"Hoje me disseram que me vestia de uma forma muito gay...\n\n...eu respondi que minhas roupas saíram do armário hoje de manhã.",
				"Na vida, pretendo ter apenas 3 filhos.\n\nZé Roberto, Humberto e Doisberto serão os nomes deles, nesta ordem.",
				"Por que o cego nunca entra numa discussão?\n\nPor não ter um ponto de vista.",
				"Pq portugues compra moto e não anda?\n\nPq ele compra yamaha hahaha",
				"Como um míope sobe a escada?\n\nCom óculos degrau",
				"O que é um alcoólotra?\n\nUm guri foi até seu pai e lhe fez uma pergunta: Pai, o que é um alcoólatra? Está vendo aquelas quatro árvores filhão? Pois então, um alcoólatra é quem vê oito delas. Mas pai, eu só vejo duas árvores.",
				"Levar um tombo na Ucrania pode ser fatal...\n\n...porque todo traumatismo lá é UCRANIANO",
				"Temer vira para Marcela e pergunta: \"Amor, onde jantaremos hoje?\"\n\nEla responde: Fora, Temer",
				"Os nadadores americanos não são culpados\n\nO que as gravações não mostram é eles perguntando a um cara na rua aonde ele podiam ir pra mijar, e o cara respondendo: \"Lá no posto ipiranga\"",
				"Construir prédio não é fácil...\n\nedifício.",
				"Vocês ouviram falar dos dois bandidos que roubaram um calendário?\n\n...parece que pegaram seis meses cada um! Kkkkkkkk",
				"Um paulista chega num hotel em Porto Alegre\n\nE pergunta ao recepcionista: Moço, como está o quarto 306? O recepcionista olha o computador, e depois de alguns segundos Mas bah guri, esse quarto está tri-vago!",
				"Sabe onde os italianos dançam e festejam??\n\nNa Rave oli.",
				"Qual o médico que é bem desligado?\n\no OFFtalmologista",
				"Por que o asilo não precisa pagar a conta de luz?\n\nPorque lá está cheio de velinhas.",
				"Por que o Mario foi ao psicólogo?\n\nporque estava passando por uma fase difícil",
				"Porque existe cama elástica no polo norte?\n\nPara o urso polar.",
				"333\n\nMeio besta",
				"O que acontece a uma Jiboia quando ela não sabe nadar?\n\nEla Jafunda",
				"O saci é um cara muito chato.\n\nEle só da mancada.",
				"Não deixe a obesidade\n\nvirar um peso na sua vida.",
				"Piadas de mãe\n\nVou começar! Sua mãe é tão gorda que quando ela troca o celular de bolso o DDD muda.",
				"Por que o PC da Nasa quebrou?\n\nPorque o HD foi pro espaço.",
				"Dois biscoitos no forno\n\nO primeiro disse: nossa está calor aqui né?\nO segundo respondeu: Socorroooo! Um biscoito falante.",
				"Qual a música mais tocada nas ilhas Cayman?\n\nIt's Raining Man",
				"Droga de piada\n\nO que é maconha enrolada em jornal? Não sei. Baseado em fatos reais!",
				"Reduza a queda de cabelo em 50%...\n\nTome banho sentado.",
				"Por que comprou patê?\n\nPatê o que comê.",
				"Teve um dia que eu estava andando de ônibus e...\n\n... e uma nuvem sentou ao meu lado. Fiquei sem entender essa situação. Mas depois me toquei que era uma nuvem passageira.",
				"O cara era treinador de ursos e aposentou. Qual o nome do filme?\n\nO ex ursista.",
				"\"Anota meu telefone: 7070-7070\"\n\nSe não completar a ligação, cê tenta de novo.",
				"Este calor é culpa do PT\n\nSe fosse o Aécio, seria Neves.",
				"Eu acordo mais tarde\n\nO Edir Macedo",
				"Oi pai tem pão?\n\nQue tempão o que filho eu te vi hoje de manhã.",
				"Quer um Chokito?\n\nPõe o dedito na tomadita",
				"Aproveitaram muito o Black Friday ontem? Ficou triste porque acabou?\n\nNão se preocupem: hoje é black Sabbath",
				"Um homem tomou um suco de laranja e logo após se jogou da torre Eiffel. Qual o nome do filme?\n\nO último Tang em Paris.",
				"Como se chama uma pessoa que pinta carros?\n\nCar-pinteiro",
				"Imagina se todo mundo tá no postinho tomando soro e aí chega a vez do\n\nFernando e sorocaba?",
				"Qual prato nunca poderá ser servido num restaurante fast-food?\n\nPolenta de lentilhas.",
				"O que diz um programador aprendendo inglês?\n\nDebug is on the table",
				"Por que os cães de hoje são mais silenciosos do que no passado?\n\nPorque o latim é uma língua em extinção.",
				"Qual o ator preferido das abelhas?\n\nO Mel Gibson",
				"Uma cobra estava em divida com outra cobra....\n\nNo jornal: Cobra cobra cobra",
				"Sabe o que acontece quando uma galinha morre?\n\nEla vira uma alma penada.",
				"O que o saci disse para a sacia?\n\nFica de 3.",
				"Qual o político brasileiro que tem parkinson?\n\nO Michel Tremer",
				"Como faz para queimar 1500 calorias em 30 minutos?\n\nDeixa a pizza no forno alto",
				"Porque a aranha é o bicho mais carente do mundo?\n\nPorque ela é aracNEEDyou",
				"qual cidade é dividida em 4?\n\nFOURtaleza",
				"O quê o Faustão disse pra Chapéuzinho Vermelho na floresta?\n\nÔ lobo, meu!",
				"Mãe, o Rio rouba assim desde garotinho?\n\nNão, filho, isso vem desde Cabral.",
				"Estava no banho quando tive a sensação que alguém estava me reparando\n\ndepois percebi q era o shampoo reparação total",
				"Quantas crianças cabem em uma circunferência?\n\n2(pi)raio",
				"Qual é a ave que transmite um vírus?\n\nHIVota",
				"Amanhã é o dia mais doido do ano.\n\nDoi de Mai.",
				"O que aconteceu com o lápis que caiu no chão?\n\nFicou triste e desapontado.",
				"uma obesa engasgou com comida e morreu tossindo. qual nome da doença?\n\ntubergulosa",
				"Para quem não curte carnaval, estou organizando um Retiro\n\nPara casais custa 100,00 solteiros custa 50,00 vocês depositam em minha conta e eu RETIRO.",
				"Qual é a marca de tênis favorita dos pedreiros?\n\nReebok.",
				"Por que a galinha anarcocapitalista atravessou a rua?\n\nPorque ela pagou pedágio",
				"Qual é o vegetal que tem a melhor bunda de todas\n\nA better raba Credito @LoboRuivo",
				"Um anão foi no centro espirita e estava muito triste...\n\ne quando ele saiu de la ele estava muito feliz. E peguntaram: Por que você esta feliz? Então ele disse: E porque descobrir que não sou mais anão sou medium.",
				"Por que o programador tomou paracetamol?\n\nPorque estava COMPUTADOR de cabeça.",
				"Por que o míope não vai ao zoológico?\n\nPorque ele usa lente di-ver-gente e não di-ver-bicho",
				"Qual a semelhança entre o meu celular e um amazonense com sono?\n\nOs dois estão sempre procurando rede.",
				"Sabe qual maquina vc tira do vulcão?\n\nmaquina de lava",
				"Qual é a profissão mais frustrante do mundo?\n\nProfessor de Natação. Sabe porquê? Ele ensina, ensina e ensina e o aluno NADA.",
				"Não vou contar a piada da nuvem...\n\nPorque é o cumulus ! PS: essa piada é passageira",
				"Duas baleias entraram num bar\n\nComeçaram a discutir e se mataram com pistolas. No outro dia o jornal noticia a calamidade: Baleia baleia baleia",
				"Qual a diferença entre ignorância e apatia?\n\nEu não sei e eu não me importo.",
				"Se cuidar de um cachorro já é difícil...\n\nImagine Dragons"
		)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "summon tiodopave")

		val temmie = getOrCreateWebhook(context.event.textChannel!!, "Tio do Pavê")

		context.sendMessage(temmie, WebhookMessageBuilder()
				.setUsername("Tio do Pavê")
				.setContent(context.getAsMention(true) + PIADAS.random())
				.setAvatarUrl("https://loritta.website/assets/img/tio_do_pave.jpg")
				.build())
	}
}
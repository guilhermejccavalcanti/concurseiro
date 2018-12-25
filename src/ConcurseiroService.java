import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class ConcurseiroService {

	public void run() throws InterruptedException {
		while(true){
			Set<String> novosConcursos = new HashSet<>();
			Map<String,String> sitesDeBusca = getSites();
			Map<String,String> historico = getHistorico();


			try {
				for(String criterio : sitesDeBusca.keySet()){
					String siteDeBusca = sitesDeBusca.get(criterio);
					String ultimaUrl = historico.get(criterio);
					if(ultimaUrl == null){
						System.err.println("Missing last url from history in: " + criterio);
						System.exit(-1);
					}
					Set<String> novosLinks = getNovosLinks(siteDeBusca, ultimaUrl);
					if(!novosLinks.isEmpty()){
						novosConcursos.addAll(novosLinks);
						historico.put(criterio, novosLinks.iterator().next()); //atualiza o histórico daquele critério de busca
					}
				}
			} catch(Exception e){
				e.printStackTrace();
			}


			enviarEmail(novosConcursos);
			atualizarHistorico(historico);

			TimeUnit.HOURS.sleep(24);
		}
	}

	private Map<String,String> getSites(){
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("concursos.in"))){
			Map<String,String> listaSites = new LinkedHashMap<>();
			List<String> linhas = reader.lines().collect(Collectors.toList());
			for(String l : linhas){
				String[] colunas = l.split(";");
				String criterio = colunas[0];
				String url = colunas[1];
				listaSites.put(criterio, url);
			}
			return listaSites;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String,String> getHistorico(){
		try (BufferedReader reader = Files.newBufferedReader(Paths.get("historico.data"))){
			Map<String,String> listaSites = new LinkedHashMap<>();
			List<String> linhas = reader.lines().collect(Collectors.toList());
			for(String l : linhas){
				String[] colunas = l.split(";");
				String criterio = colunas[0];
				String url = colunas[1];
				listaSites.put(criterio, url);
			}
			return listaSites;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	private Set<String> getNovosLinks(String siteDeBusca, String ultimaUrl) throws Exception {
		Set<String> novosConcursos = new HashSet<>();
		try (final WebClient webClient = new WebClient()) {
			webClient.getOptions().setJavaScriptEnabled(false);
			HtmlPage page = webClient.getPage(siteDeBusca);
			List<Object> links = page.getByXPath("//div[@class='ca']//a"); 
			if(!links.isEmpty()){
				for(Object link : links){
					String url = ((HtmlAnchor)link).getHrefAttribute();
					if(url.equals(ultimaUrl)){
						break;
					} else {
						novosConcursos.add(url);
					}
				}
			}
		}
		return novosConcursos;
	}

	private void atualizarHistorico(Map<String, String> historico) {
		try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("historico.data"))){
			String content = "";
			for(String criterio : historico.keySet()){content += criterio + ";" + historico.get(criterio) + "\n";}
			writer.write(content);
		} catch(Exception e){
			System.err.println(e.toString());
		}
	}

	private void enviarEmail(Set<String> novosConcursos) {
		String assunto = novosConcursos.isEmpty() ? "[CONCURSOS] Sem novos concursos hoje" : "[CONCURSOS] Novos concursos anunciados!";
		String mensagem = "";
		if(novosConcursos.isEmpty()){
			mensagem = "Sem novos concursos hoje.";
		} else {
			for(String novoConcurso : novosConcursos)mensagem += novoConcurso + "\n\n";
		}
		JavaEmailSender.enviar(assunto, mensagem);
	}

	public static void main(String[] args) {
		try{
			ConcurseiroService service = new ConcurseiroService();
			service.run();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}

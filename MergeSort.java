import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MergeSort {
    public static void main(String[] args) {
        try {
            File inputFile = new File(args[0]);
            Scanner scanner = new Scanner(inputFile);
            FileWriter outputFile = new FileWriter(args[1]);
            BufferedWriter writer = new BufferedWriter(outputFile);
            
            SistemaFiscalizacao sistema = new SistemaFiscalizacao(writer);

            int qtdCadastrados = lerQuantidade(scanner);
            if (qtdCadastrados > 0) {
                processarContainers(scanner, sistema, qtdCadastrados, true);
            }

            int qtdSelecionados = lerQuantidade(scanner);
            if (qtdSelecionados > 0) {
                processarContainers(scanner, sistema, qtdSelecionados, false);
            }

            sistema.realizarFiscalizacao();
            
            writer.close();
            scanner.close();
        } catch (IOException e) {
        	 System.out.println("Erro para LER A ENTRADA");
        }
    }

    private static int lerQuantidade(Scanner scanner) {
    	String  line  = scanner.nextLine().trim();
        return Integer.parseInt(line);
    }

    private static void processarContainers(Scanner scanner, SistemaFiscalizacao sistema, int quantidade, boolean autoado) {
        for (int i = 0; i < quantidade; i++) {
            String [] dados = scanner.nextLine().trim().split("\\s+");
            String codigo = dados[0];
            String cnpj = dados[1];
            double peso = Double.parseDouble(dados[2]);

            Container container = new Container(codigo, cnpj, peso);
            if (autoado) {
                sistema.adicionarContainerCadastrado(container);
            } else {
                sistema.adicionarContainerSelecionado(container);
            }
        }
    }
}

class Container {
    private String codigo;
    private String cnpj;
    private double peso;
    private int prioridade;

    public Container(String codigo, String cnpj, double peso) {
        this.codigo = codigo;
        this.cnpj = cnpj;
        this.peso = peso;
    }

    public String getCodigo() { 
    	return codigo;
    }
    public String getCnpj() {
    	return cnpj; 
    }
    public double getPeso() {
    	return peso;
    }
    public int getPrioridade() { return prioridade; }
    
    public void setPrioridade(int prioridade) { 
    	this.prioridade = prioridade;
    }

    public double calcularDiferenca(Container outro) {
        return this.peso - outro.getPeso();
    }

    public double calcularDiferencaPercentual(Container outro) {
		double diferenca = (this.peso - outro.getPeso()) / outro.getPeso();
		return diferenca * 100;
	}
    
    public boolean verificarDiferencaCNPJ(Container outro) {
        return !this.cnpj.equals(outro.getCnpj());
    }
}

class SistemaFiscalizacao {
    private ArrayList<Container> containersCadastrados = new ArrayList<>();
    private ArrayList<Container> containersSelecionados = new ArrayList<>();
    private ArrayList<Container> containersFiscalizados = new ArrayList<>();
    private BufferedWriter writer;

    public SistemaFiscalizacao(BufferedWriter writer) {
        this.setWriter(writer);
    }
    
    public BufferedWriter getWriter() {
		return writer;
	}

	public void setWriter(BufferedWriter writer) {
		this.writer = writer;
	}

    public void adicionarContainerCadastrado(Container container) {
        containersCadastrados.add(container);
    }

    public void adicionarContainerSelecionado(Container container) {
        containersSelecionados.add(container);
    }

    public void realizarFiscalizacao() throws IOException {
        for (Container cadastrado : containersCadastrados) {
            for (Container selecionado : containersSelecionados) {
                if (selecionado.getCodigo().equals(cadastrado.getCodigo())) {
                    if (selecionado.verificarDiferencaCNPJ(cadastrado)) {
                        selecionado.setPrioridade(0);
                        containersFiscalizados.add(selecionado);
                    } else {
                        double diferencaPercentual = selecionado.calcularDiferencaPercentual(cadastrado);
                        if (diferencaPercentual > 10) {
                            selecionado.setPrioridade(1);
                            containersFiscalizados.add(selecionado);
                        }
                    }
                }
            }
        }
        MergeSort(containersFiscalizados);
        exibirResultados();
    }

    private void exibirResultados() {
        try {
            BufferedWriter escritor = this.getWriter();

            int i = containersFiscalizados.size();
            for (Container container : containersFiscalizados) {
                for (Container cadastrado : containersCadastrados) {
                    if (container.getCodigo().equals(cadastrado.getCodigo())) {
                        if (container.getPrioridade() == 0) {
                            escritor.write(container.getCodigo() + ":" + cadastrado.getCnpj() + "<->" + container.getCnpj());
                        } else if (container.getPrioridade() == 1) {
                            double diferenca = container.calcularDiferenca(cadastrado);
                            double diferencaPercentual = container.calcularDiferencaPercentual(cadastrado);
                            escritor.write(String.format("%s:%dkg (%.0f%%)", container.getCodigo(), (int) diferenca, diferencaPercentual));
                        }
                        i--;
                        if (i > 0) escritor.newLine();
                    }
                }
            }
            
            escritor.close();
        } catch (IOException e) {
            System.out.println("Erro para imprimir");
        }
    }

	private void MergeSort(ArrayList<Container> containers) {
		if (containers.size() > 1) {
			int meio = containers.size() / 2;

			// Divide a lista em duas sublistas
			ArrayList<Container> esquerda = new ArrayList<>(containers.subList(0, meio));
			ArrayList<Container> direita = new ArrayList<>(containers.subList(meio, containers.size()));
			MergeSort(esquerda);
			MergeSort(direita);

			int i = 0, j = 0, k = 0;
			while (i < esquerda.size() && j < direita.size()) {
				if (esquerda.get(i).getPrioridade() <= direita.get(j).getPrioridade()) {
					containers.set(k++, esquerda.get(i++));
				} else {
					containers.set(k++, direita.get(j++));
				}
			}
			while (i < esquerda.size()) {
				containers.set(k++, esquerda.get(i++));
			}
			while (j < direita.size()) {
				containers.set(k++, direita.get(j++));
			}
		}
	}
}
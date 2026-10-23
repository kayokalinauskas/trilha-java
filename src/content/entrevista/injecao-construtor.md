---
pergunta: "Por que injeção por construtor é preferível à injeção por campo com @Autowired?"
tema: "Spring"
nivel: jr
idioma: pt
modulo: 08-spring-boot
intencao: "Boas práticas de Spring e design testável."
pontosChave:
  - "Dependências obrigatórias e explícitas"
  - "Campos final: objeto imutável e sempre completo"
  - "Testável sem Spring (new com mocks)"
  - "Evidencia classes com dependências demais"
  - "Um único construtor dispensa @Autowired"
---

"Com injeção por construtor, as dependências ficam **explícitas** e **obrigatórias**: o objeto não pode existir sem elas. Os campos podem ser `final`, então o bean é imutável depois de criado. E a classe é testável sem o Spring: no teste, faço `new PedidoService(repositorioFake, notificadorMock)`.

Com `@Autowired` no campo, as dependências ficam escondidas, os campos não podem ser `final`, e para testar eu preciso de reflexão ou subir o contexto do Spring. Tem ainda um benefício indireto: se o construtor passa de cinco ou seis parâmetros, isso fica visível e sinaliza que a classe tem responsabilidades demais.

Desde o Spring 4.3, se a classe tem um único construtor, o `@Autowired` nem é necessário."

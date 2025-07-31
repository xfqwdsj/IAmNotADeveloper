[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/IAmNotADeveloper)

[简体中文](README.md) | [English](README_en.md) | **Português (Brasil)** | [Türkçe](README_tr-TR.md)

# IAmNotADeveloper

Um módulo para ocultar o status das opções do desenvolvedor do sistema Android.

## Perguntas frequentes

### P: Ativei o módulo, mas ele informa "Módulo não ativado". O que devo fazer?

Etapas de solução de problemas:

1. Certifique-se de ter ativado o módulo.
2. Certifique-se de que você tenha forçado a parada do app do módulo após a ativação bem-sucedida.
3. Pesquise por problemas relacionados na seção [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues).
4. Se você não conseguir encontrar um problema relacionado, capture logs e faça capturas de tela do app do módulo, garantindo que o cartão de status completo do módulo esteja visível (se não couber em uma captura de tela, use várias).
5. Envie um novo problema em [Issues](https://github.com/xfqwdsj/IAmNotADeveloper/issues) e carregue os logs.

### P: Como posso confirmar se o módulo está ativado no LSPosed?

Você pode confirmar seguindo estes passos:

1. Abra o LSPosed por qualquer meio.
2. Na página "Módulos", localize o módulo "IAmNotADeveloper".
3. Certifique-se de que a chave "Ativar módulo" esteja ativada.

### P: Como faço para capturar logs?

Você pode capturar logs seguindo estes passos:

1. Abra o LSPosed por qualquer meio.
2. Na página "Registros", toque no ícone "Salvar" no canto superior direito.
3. Escolha um local adequado para salvar, como "Downloads", e não altere o nome do arquivo.
4. Toque no botão "Salvar".

### P: Ativei o módulo para um determinado app, mas o app trava/não tem efeito/é detectado por um app detector. O que devo fazer?

Este módulo funciona injetando diretamente no app de destino. Para apps com proteção contra injeção integrada, o módulo pode não funcionar ou fazer com que o app se recuse a executar.

Solução: Nenhuma no momento. Consulte o [Issue #104](https://github.com/xfqwdsj/IAmNotADeveloper/issues/104) para obter detalhes. Aguarde pacientemente; no momento, não há ETA (Hora Estimada de Chegada). **Não envie nenhum issue relacionado a este problema; eles serão encerrados sem maiores explicações.**

## Como contribuir

Se você quiser contribuir com código para este projeto, consulte [CONTRIBUTING.md](CONTRIBUTING_pt-BR.md).

## Acordo de privacidade

A função "Testar" deste app obterá o status das chaves do sistema correspondentes, incluindo:

- Opções do desenvolvedor
- Depuração USB
- Depuração por Wi-Fi

No entanto, este app não coletará nenhuma informação sobre você.

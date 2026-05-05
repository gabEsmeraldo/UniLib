# UniLib — Prototype vs Implementation Code Review

Comparison between the low-fidelity prototype (`Prototipo Mobile UniLib.excalidraw`) and the current Kotlin/Android implementation on branch `dev`.

The review is at the **abstraction level of the prototype**: presence of the conceptual elements (buttons, fields, sections, navigation targets), not pixel-level fidelity. Stylistic differences (colors, paddings, icons) are ignored.

Modals are **explicitly out of scope** per your instruction and are listed only as a checklist at the end.

---

## Legend

- ✅ Present in both, conceptually matching
- ⚠️ Present but with notable differences
- ❌ Missing in implementation (prototype has it)
- ➕ Extra in implementation (not in prototype)

---

## 1. Start Page

**Prototype:** UniLib logo, two buttons (Logar / Cadastre-se), welcome line "Seja bem-vindo ao UniLib!".

**Implementation:** [start_page.xml](app/src/main/res/layout/start_page.xml), [StartPage.kt](app/src/main/java/com/example/unilib/activities/StartPage.kt)

| Element | Status | Notes |
|---|---|---|
| Logo / brand "UniLib" | ✅ | Logo box + title present |
| Welcome / subtitle text | ⚠️ | Prototype: "Seja bem-vindo ao UniLib!". Impl: "Acesse o acervo, reserve livros e acompanhe seus empréstimos com facilidade." (different copy, same purpose) |
| Login button → LoginPage | ✅ | `btnLogin` → `LoginPage` |
| Cadastrar button → CadastroPage | ✅ | `btnRegister` → `CadastroPage` |
| Institutional badge (UNIFOR) | ➕ | Extra: "UNIFOR · BIBLIOTECA" subtitle and "Universidade de Fortaleza" badge |

---

## 2. Login Page

**Prototype:** Header "Login", "Insira seus dados" hint, E-mail field, Senha field, "Esqueceu sua senha?" link, Logar button. Frame is reachable from Start.

**Implementation:** [login_page.xml](app/src/main/res/layout/login_page.xml), [LoginPage.kt](app/src/main/java/com/example/unilib/activities/LoginPage.kt)

| Element | Status | Notes |
|---|---|---|
| Header "Login" with back button | ✅ | |
| "Insira seus dados" / welcome hint | ⚠️ | Impl uses "Olá de volta! / Entre com suas credenciais" — semantically equivalent |
| E-mail field | ✅ | `editEmail` |
| Senha field | ✅ | `editPassword` |
| "Esqueceu sua senha?" link | ✅ | `TextViewForgotPassword` (not yet wired to a flow) |
| Logar / Entrar button | ✅ | `btnEnter` |
| Link to Cadastro | ✅ | `TextViewCreateAccount` "Criar conta gratuita" |
| Admin entry path | ➕ | Impl routes to `AdminHomePage` if email contains "admin" — prototype shows separate "Admin" label/path on the login frame; this is an undocumented dev shortcut |

---

## 3. Cadastro Page

**Prototype:** Header "Cadastro", "Insira seus dados", fields: **Nome, E-mail, Senha, Confirme a senha**, Cadastrar button.

**Implementation:** [cadastro_page.xml](app/src/main/res/layout/cadastro_page.xml), [CadastroPage.kt](app/src/main/java/com/example/unilib/activities/CadastroPage.kt)

| Element | Status | Notes |
|---|---|---|
| Header "Criar Conta" with back | ✅ | |
| Nome completo field | ✅ | `etNomeCompleto` |
| E-mail field | ✅ | `etEmail` |
| Senha field | ✅ | `etSenha` |
| Confirme a senha field | ✅ | `etConfirmeSenha` |
| Cadastrar button | ✅ | `btnCadastrar` |
| Link "Fazer login" | ✅ | `btnFazerLogin` |
| **CPF field** | ➕ | Extra in implementation — not present in the prototype frames I reviewed. (Note: per recent commit `af230a7`, CPF replaced an earlier `matricula` field; the prototype shows neither in the cadastro frame, only "matrícula" appears later on the user account screen) |

---

## 4. User Homepage

**Prototype:** Greeting "Olá, Narak!", alert "Você tem 2 empréstimos ativos", section "Recomendações" with book cards (Dom Casmurro, Algoritmos, Mobile…), bottom nav (Home / Pesquisa / Empréstimos / Conta), floating "?" / chat IA button.

**Implementation:** [user_home_page.xml](app/src/main/res/layout/user_home_page.xml) (also a fragment variant [fragment_home.xml](app/src/main/res/layout/fragment_home.xml))

| Element | Status | Notes |
|---|---|---|
| Greeting with user name | ✅ | "Boa tarde, Lucas Silva 👋" (prototype uses "Olá, Narak!") |
| Alert: empréstimos ativos count | ✅ | "2 empréstimos ativos · Devolução em 3 dias" |
| Recommendations section with horizontal book cards | ✅ | "Recomendados para você" — Algoritmos / Clean Code / Engenharia |
| Bottom nav (Home / Pesquisa / Empréstimos / Conta) | ✅ | All four tabs |
| Chat IA floating button (the "?") | ✅ | `fabChat` |
| Notifications icon in header | ➕ | Header bell icon with unread dot — not on the prototype homepage; prototype reaches notifications via a separate modal |
| **"Novidades no acervo" section** | ➕ | Extra horizontal scroll section — not in prototype |

---

## 5. User Search Page

**Prototype:** Header "Pesquisa", search field with text "Dom casmurro", list of result cards (title / author / "X Disponíveis"). Bottom nav present.

**Implementation:** Two layouts: [user_search_page.xml](app/src/main/res/layout/user_search_page.xml) (used by `UserSearchPage` activity) and [fragment_search.xml](app/src/main/res/layout/fragment_search.xml) (empty-state-only fragment).

| Element | Status | Notes |
|---|---|---|
| Header "Pesquisa" | ✅ | activity uses "Pesquisar Livros"; fragment uses "Pesquisa" |
| Search input field | ✅ | `EditTextSearch` / `searchInput` |
| Results list with title, author, ISBN, "X disponíveis" | ✅ | activity has 4 hardcoded cards; fragment shows empty state only |
| Bottom nav | ⚠️ | Activity has bottom nav; fragment has none (presumably hosted by an activity) |
| Tag chips on each card (Computação / Engenharia / Redes) | ➕ | Extra: implementation adds tag chips and "Indisponível" status badge — prototype only shows "X Disponíveis" |
| **Two parallel implementations (activity + fragment)** | ⚠️ | Architectural note: same screen done twice with different visual treatments — see "Cross-cutting" section |

---

## 6. Book Details (User)

**Prototype:** Header "Detalhes livro", book cover, title "Dom Casmurro", "Machado de Assis", "8 Disponíveis", "2 Emprestados", ISBN, Sinopse, Tags (Romance, etc), **Reservar Livro** button. (A "Localizar Livro" button also appears on adjacent frame and ties to the Mapa flow.)

**Implementation:** [book_details.xml](app/src/main/res/layout/book_details.xml)

| Element | Status | Notes |
|---|---|---|
| Header with back button + title | ✅ | "Detalhes do Livro" |
| Book cover hero | ✅ | |
| Title + author + ISBN | ✅ | |
| "Disponíveis" stat | ✅ | |
| "Emprestados" stat | ✅ | |
| Tags row | ✅ | Computação / Algoritmos / Estruturas |
| Sinopse | ✅ | |
| Reservar button | ✅ | `btnReservar` |
| Localizar button | ✅ | `btnLocalizar` (links to Mapa) |
| **"Localização" stat (e.g., A-12)** | ➕ | Extra stat card showing shelf code |
| Bottom nav | ➕ | Implementation includes bottom nav on detail screen; prototype detail frame does not show one |

---

## 7. Empréstimos Page (User)

**Prototype:** Header "Empréstimos", list of cards: active emprestimo ("Emprestado até 01/04/26"), atrasado ("Devolução atrasada"), and a reserve card ("Reservas ativas").

**Implementation:** [emprestimos_page.xml](app/src/main/res/layout/emprestimos_page.xml) and [fragment_emprestimos.xml](app/src/main/res/layout/fragment_emprestimos.xml). Card subviews: [emprestimo_card_ativo.xml](app/src/main/res/layout/emprestimo_card_ativo.xml), [emprestimo_card_atrasado.xml](app/src/main/res/layout/emprestimo_card_atrasado.xml), [emprestimo_card_reserva.xml](app/src/main/res/layout/emprestimo_card_reserva.xml).

| Element | Status | Notes |
|---|---|---|
| Header "Empréstimos" | ✅ | "Meus Empréstimos" |
| ATIVOS section | ✅ | |
| Card: ativo with devolução date | ✅ | `emprestimo_card_ativo` |
| Card: atrasado with fine | ✅ | `emprestimo_card_atrasado` (shows "R$ 2,50") |
| RESERVAS ATIVAS section with reserve card | ✅ | `emprestimo_card_reserva` (shows "Expira em: 45 min") |
| Bottom nav | ✅ | activity layout has it; fragment layout does not |
| Two parallel layouts (activity + fragment) | ⚠️ | Same architectural duplication as Search |

---

## 8. Chat IA Page

**Prototype:** Header "Chat IA", IA bubble "Olá, como posso te ajudar hoje?", user bubble "Preciso de recomendações de livro de programação", IA bubble "Ótimo, algumas recomendações: Introdução ao C++…", input bar "Envie sua mensagem".

**Implementation:** [chat_ia_page.xml](app/src/main/res/layout/chat_ia_page.xml)

| Element | Status | Notes |
|---|---|---|
| Header "Chat IA" with back | ✅ | |
| IA greeting bubble | ✅ | "Olá, como posso te ajudar hoje?" |
| User message bubble | ✅ | "Preciso de recomendações de livro de programação" |
| IA recommendation reply | ✅ | "Ótimo, algumas recomendações:" with bulleted list incl. C++ / Clean Code / Algoritmos |
| Input bar with placeholder | ✅ | "Envie sua mensagem" + send button |
| Bottom nav | ➕ | Bottom nav present in chat — prototype chat frame does not include one |
| Recommended book card inline | ➕ | Extra card inside the chat suggesting "Algoritmos e Estruturas de Dados — Recomendado" |

---

## 9. Conta Page (User Profile)

**Prototype:** Header "Conta", user name "Narak Silva", **Matrícula** field, section "Últimos empréstimos" with cards (Algoritmos 1 — "Devolvido em 01/03/26", Desenvolvimento — "Devolvido em 15/02/26").

**Implementation:** [user_account.xml](app/src/main/res/layout/user_account.xml) (used by `user_account` activity) and [fragment_conta.xml](app/src/main/res/layout/fragment_conta.xml).

| Element | Status | Notes |
|---|---|---|
| Header "Conta" / "Minha Conta" | ✅ | |
| Avatar + user name | ✅ | "Lucas Silva" |
| **Matrícula** | ⚠️ | Prototype shows "Matrícula" label. **`user_account.xml` does NOT show matrícula** — only e-mail and "Usuário Ativo" badge. **`fragment_conta.xml` does show "Matrícula: 2021001234"**. Inconsistent across the two account layouts. |
| "Últimos empréstimos" section with returned books and dates | ✅ | Only in `user_account.xml` (Algoritmos 1 / Desenvolvimento with return dates matching prototype) — **missing from `fragment_conta.xml`** |
| Bottom nav | ✅ | |
| **Reservas ativas (horizontal book cards)** | ➕ | `user_account.xml` adds a horizontal "Reservas ativas" section (Cálculo Vol. 1 / Clean Architecture / Design Patterns) — not in prototype Conta frame (prototype puts reservas elsewhere) |
| Settings menu (Editar Perfil / Privacidade / Sair) | ➕ / ⚠️ | Sair is visible; Editar Perfil and Privacidade are present but `visibility="gone"`. Prototype Conta has no settings menu. |
| **fragment_conta** has Histórico, Configurações, Sair as visible menu items not in prototype | ➕ | |

---

## 10. Mapa Page

**Prototype:** Header "Mapa", "Mapa biblioteca para o livro", text "Estante X Prateleira Y". Reachable from Book Details via "Localizar Livro".

**Implementation:** [map_page.xml](app/src/main/res/layout/map_page.xml)

| Element | Status | Notes |
|---|---|---|
| Header "Mapa da Biblioteca" with back | ✅ | |
| Reference to book (title shown) | ✅ | "Algoritmos e Estruturas de Dados" inside the location card |
| Map image / floor plan | ✅ | `imgLibraryMap` (drawable `mapa_biblioteca`) |
| Estante / Prateleira reference | ✅ | "Como encontrar" card lists "Siga até a Estante B / Procure a Prateleira 3" |
| Bottom nav | ➕ | Bottom nav present — prototype frame doesn't show one |
| Multiple map markers (Entrada / Acesso / Livro) | ➕ | Implementation adds 3 markers and a legend block — prototype is much simpler |
| Step-by-step instructions (1, 2, 3, 4) | ➕ | Two info cards with numbered routing instructions |

---

## 11. Admin Homepage

**Prototype:** Header "Homepage Admin", greeting "Olá, admin", section "Acervo Atual" with book cards (Dom Casmurro / POO Simplificado / Desenvolvimento with "X Disponíveis").

**Implementation:** [admin_home_page.xml](app/src/main/res/layout/admin_home_page.xml)

| Element | Status | Notes |
|---|---|---|
| Header "UniLib Admin" / greeting "Bibliotecário(a)" | ⚠️ | Prototype: "Olá, admin"; impl: "Bem-vindo(a), Bibliotecário(a)" — same intent, different copy |
| Acervo cards section | ✅ | "Acervo em Destaque" with 3 horizontal book cards (Algoritmos / Clean Code / Banco de Dados) — different titles than prototype but same structure |
| Add book button | ✅ | `btnAddBook` in header |
| Bottom nav (3 tabs: Home / Pesquisar / Empréstimos) | ✅ | |
| **Stats hero (Pendentes / Ativos / No Acervo)** | ➕ | 3 metric cards in hero — not in prototype |
| **Aprovações Pendentes section with cards** | ➕ | "Clean Code / Engenharia de Software" pending approval cards with timestamp — not in prototype admin homepage |
| Logout button in header | ➕ | `btnAdminLogout` — not in prototype |

---

## 12. Admin Search Page

**Prototype:** Header similar to user search, list of acervo books with author + "X Disponíveis".

**Implementation:** [admin_search_page.xml](app/src/main/res/layout/admin_search_page.xml)

| Element | Status | Notes |
|---|---|---|
| Header "Acervo" + search field | ✅ | |
| Result cards with title / author / ISBN | ✅ | 4 hardcoded cards |
| "X Exemplares totais" + "X disponíveis" tags | ✅ | Total + available exposed; prototype only showed "X Disponíveis" |
| Bottom admin nav (3 tabs) | ✅ | |

---

## 13. Admin Book Details

**Prototype:** Header "Detalhes livro", same book hero (cover, title, author, ISBN, "8 Disponíveis", "2 Emprestados"), **Excluir Livro** button, plus entry points to Editar Tags / Editar Sinopse (which open modals).

**Implementation:** [admin_book_details.xml](app/src/main/res/layout/admin_book_details.xml)

| Element | Status | Notes |
|---|---|---|
| Header with back + "Detalhes - Admin" | ✅ | |
| Book cover hero, title, author, ISBN | ✅ | |
| Tags row | ✅ | |
| **Disponíveis / Emprestados stats** | ✅ | |
| Editar Sinopse entry (action row) | ✅ | `btnEditarSinopse` |
| Editar Tags entry (action row) | ✅ | `btnEditarTags` |
| Excluir Livro button | ✅ | `btnExcluirLivro` |
| Bottom admin nav (3 tabs) | ✅ | |
| **"Total" stat card (5)** | ➕ | Extra metric — not in prototype |
| **"Reservados" stat card (0)** | ➕ | Extra metric — not in prototype |
| **No "Editar Quantidade exemplares" entry point** | ❌ | Prototype shows a "Modal Quantidade exemplares" — needs an entry point on this screen to trigger it. There is no row/button for this. |
| **No "Editar Nome do livro" entry point** | ❌ | Prototype shows a "Modal Nome do livro" — no entry point in implementation |
| **No "Editar Autor do livro" entry point** | ❌ | Prototype shows a "Modal Autor do livro" — no entry point |
| **No "Editar Imagem do livro" entry point** | ❌ | Prototype shows a "Modal imagem do livro" — no entry point |
| Sinopse text block | ❌ | Prototype shows the sinopse text on this screen; impl shows only an "Editar Sinopse" row, no read-only sinopse display |

---

## 14. Admin Add Book

**Prototype:** Header "Adicionar livro", **Adicionar imagem do livro** placeholder, fields: **Nome do livro, Autor, Tags, Sinopse, Qntd exemplares, ISBN**, Confirmar / Cancelar.

**Implementation:** [admin_add_book.xml](app/src/main/res/layout/admin_add_book.xml)

| Element | Status | Notes |
|---|---|---|
| Header "Adicionar Livro" with back | ✅ | |
| Nome do livro field | ✅ | |
| Autor field | ✅ | |
| ISBN field | ✅ | |
| Tags field | ✅ | |
| Sinopse field | ✅ | |
| Quantidade de exemplares field | ✅ | |
| Adicionar imagem placeholder (dashed box) | ✅ | "🖼️ Adicionar Imagem da Capa" |
| Confirmar button | ✅ | `btnEnter` "Confirmar e Adicionar" |
| **Cancelar button** | ❌ | Prototype shows a Cancelar button on this frame; impl only has back arrow + Confirmar |

---

## 15. Admin Empréstimos — Pendentes

**Prototype:** Header "Empréstimos", tabs **Pendentes / Ativos**, list of pendente cards each with cover, title, author, "Mat: 240XXXX", **Pendente** badge.

**Implementation:** [emprestimos_admin_page.xml](app/src/main/res/layout/emprestimos_admin_page.xml) + [admin_emprestimo_card_pendente.xml](app/src/main/res/layout/admin_emprestimo_card_pendente.xml)

| Element | Status | Notes |
|---|---|---|
| Header "Empréstimos" | ✅ | |
| Tabs Pendentes / Ativos | ✅ | `tabPendentes` / `tabAtivos` |
| Card with cover, title, user, status badge "● Pendente" | ✅ | |
| **Mat: (matrícula)** | ⚠️ | Prototype shows "Mat: 2401111" (matrícula). **Implementation shows "CPF: 123.456.789-00"** — consistent with the recent CPF migration, but no longer matches the prototype label |
| Time since reservation ("Reservado há 5 min") | ✅ | (prototype doesn't show timing on the pendente card; this is extra detail but conceptually fits) |
| Bottom admin nav | ✅ | |

---

## 16. Admin Empréstimos — Ativos

**Prototype:** Same shell, tab Ativos selected, cards with status "Ativo" or "Atrasado".

**Implementation:** [emprestimos_admin_page_ativos.xml](app/src/main/res/layout/emprestimos_admin_page_ativos.xml) + [admin_emprestimo_card_ativo.xml](app/src/main/res/layout/admin_emprestimo_card_ativo.xml) + [admin_emprestimo_card_atrasado.xml](app/src/main/res/layout/admin_emprestimo_card_atrasado.xml)

| Element | Status | Notes |
|---|---|---|
| Header + Tabs (Ativos selected) | ✅ | |
| Ativo card (title / user / devolução date / "● Ativo") | ✅ | |
| Atrasado card (title / user / dias atraso / fine / "● Atrasado") | ✅ | |
| **Mat: (matrícula)** | ⚠️ | Same as Pendentes — impl uses CPF, prototype uses Matrícula |
| Bottom admin nav | ✅ | |

---

## 17. Reservas Ativas (Standalone)

**Prototype:** A separate frame "Reservas ativas" listing reserved book cards. (The actual code-display "Apresente o código…" is a modal, listed below.)

**Implementation:** No standalone activity for this. It is folded into:
- The Empréstimos page "RESERVAS ATIVAS" section (`emprestimo_card_reserva`)
- The User Conta page "Reservas ativas" horizontal scroll (`user_account.xml`)

| Element | Status | Notes |
|---|---|---|
| Standalone Reservas Ativas screen | ❌ | Prototype shows it as its own frame; impl integrates it into Empréstimos and Conta. May or may not be a problem depending on intended IA — flag as a divergence to confirm. |

---

## Cross-cutting observations

### A. Activity vs Fragment duplication

Several screens exist as **both an activity layout and a fragment layout** with diverging content:

| Screen | Activity layout | Fragment layout | Divergence |
|---|---|---|---|
| Home | `user_home_page.xml` | `fragment_home.xml` | Activity has "Lucas Silva 👋" + cardIDs; fragment has emoji prefixes "📖 Recomendados" + "Ver todos" links + no IDs on cards |
| Search | `user_search_page.xml` | `fragment_search.xml` | Activity = card list with results; fragment = empty state only |
| Empréstimos | `emprestimos_page.xml` | `fragment_emprestimos.xml` | Activity has bottom nav, fragment doesn't |
| Conta | `user_account.xml` | `fragment_conta.xml` | Activity has Reservas + Últimos empréstimos + Sair only; fragment has Matrícula + Histórico/Configurações/Sair menu — completely different content |

The fragments exist under [fragments/](app/src/main/java/com/example/unilib/fragments/) (`HomeFragment.kt`, `SearchFragment.kt`, `EmprestimosFragment.kt`, `ContaFragment.kt`) but the AndroidManifest only registers activities. **Worth confirming which path is actually used at runtime** — likely one path is dead code. The prototype implies a single set of screens.

### B. Matrícula vs CPF

The prototype consistently uses "Mat: …" (matrícula) on admin empréstimo cards and the user Conta page. The recent commit `af230a7 — removed matricula field in favor of cpf` flipped this. Result:

- `admin_emprestimo_card_*.xml` → CPF (matches new code)
- `user_account.xml` → no matrícula at all
- `fragment_conta.xml` → still says "Matrícula: 2021001234" (stale)
- Cadastro page → has CPF field (new)

If the prototype is the source of truth, the migration is incomplete; if CPF is the new direction, the prototype labels are simply outdated.

### C. Bottom nav on detail screens

Several detail/secondary screens that the prototype shows **without** a bottom nav have one in the implementation: Book Details, Chat IA, Mapa. Minor — flag for design intent confirmation.

### D. Login admin shortcut

`LoginPage.kt` routes to `AdminHomePage` whenever the email contains "admin" (case-insensitive). The prototype shows a separate "Admin" affordance on the login frame. The current implementation works as a dev shortcut but isn't reflected on the UI; either the prototype's admin entry needs to be implemented, or the shortcut should be made explicit.

### E. Unused / orphan layouts and activities

- `MainActivity` exists in the manifest but is not the launcher (StartPage is). Verify if `MainActivity` is needed.
- `LoginActivity` is registered but I see no references to it (the active class is `LoginPage`). Looks orphaned.
- `activity_main.xml` exists in res/layout — likely for `MainActivity`; verify if used.
- `admin_emprestimo_card_*` activities (`admin_emprestimo_card_pendente.kt`, `_ativo.kt`, `_atrasado.kt`) and `emprestimo_card_*` activities — these are card include layouts but registered as full activities. Suspicious — they may not need to be activities at all.

---

## Modals — out of scope (none should be implemented yet)

These appear in the prototype as separate modal/overlay frames. The implementation has **none of them** as actual dialogs, which matches your instruction. Listed here only as a reference checklist for future work:

### User flow modals
- Modal Reserva — confirmar reserva
- Modal Livro Reservado (success state with timer)
- Modal Reserva ativa (code "6XH-987" + 17-min timer)
- Modal Devolução atrasada — taxa de R$ 6,00
- Modal Notificações (notificações não lidas, "Marcar como lido")

### Auth modals
- Modal Reset de senha (e-mail + código + nova senha + códigos correto/incorreto + sucesso)
- Modal dados incorretos (login)
- Modal E-mail inexistente (login)
- Modal E-mail já cadastrado (cadastro)

### Admin modals
- Modal Quantidade exemplares
- Modal Sinopse (editar)
- Modal Tags (editar)
- Modal Confirmar Exclusão (livro)
- Modal Imagem do livro
- Modal Nome do livro
- Modal Autor do livro
- Modal Detalhes Empréstimo Pendente
- Modal Confirmar Empréstimo (código de retirada — A4C123 keypad, código inválido)
- Modal Retirada Confirmada
- Modal Detalhes Empréstimo Ativo (com taxa atual)
- Modal Detalhes Empréstimo Atrasado
- Modal Devolução confirmada

---

## Summary of action items

**Missing from implementation (relative to prototype):**
1. Cancelar button on Admin Add Book screen
2. Sinopse read-only display on Admin Book Details
3. Entry points on Admin Book Details for: editar nome / editar autor / editar imagem / editar quantidade
4. Standalone "Reservas ativas" screen (currently only inline in Empréstimos and Conta)
5. Explicit Admin login affordance (vs the email-contains-"admin" shortcut)
6. Matrícula on user Conta page (or align prototype to CPF)
7. Forgot-password flow target (link exists, no destination)

**Extra in implementation (not in prototype) — confirm they are wanted:**
1. Cadastro: CPF field
2. Home: Notifications bell, "Novidades no acervo" section
3. Search: tag chips, indisponível badge
4. Conta: Reservas ativas horizontal scroll, Sair menu, hidden Editar Perfil / Privacidade entries
5. Mapa: 3 markers with legend, numbered step instructions, two info cards
6. Admin Home: 3 hero metrics, "Aprovações Pendentes" inline section, logout button
7. Admin Book Details: Total / Reservados stat cards
8. Bottom nav present on Book Details, Chat IA, Mapa

**Architectural cleanup worth a separate pass:**
1. Activity-vs-fragment duplication on Home / Search / Empréstimos / Conta — choose one
2. `fragment_conta` shows stale "Matrícula" label after the CPF migration
3. Card-style XMLs registered as activities in the manifest (`emprestimo_card_*`, `admin_emprestimo_card_*`)
4. Orphan `LoginActivity` / `MainActivity` if confirmed unused

# Open issues

Problems found by AI agents while working in this repo, left **unfixed on purpose** for the owner to resolve.
Agents add new findings here instead of fixing code they weren't asked to touch. Delete an entry once it's resolved.
Each entry: what's wrong, where, why it matters, and a suggested fix.

## Rendering

### R1. After a command, the menu is drawn at the top of the pane, over the first narration (2026-11-05)
- Seen with oasis: after `n` from the travel menu, the menu description and its buttons sit at y = 0, on top of the
  first narration; the narrations below are placed correctly. The code before the detached-elements change does the
  same (checked with a build that had only the `Magid-Session` fix).
- Likely cause: `UiElementRenderer.renderResponses` renders a menu without `defer` at once, while narrations with
  `defer` are rendered later (`renderHelper`), so the menu takes the first floor of `MultiStoreUiRelocator`.
  Its height is also still 0 when the buttons are placed after it.
- Fix idea: keep the response order on the floors (reserve each element's floor when it is reached, fill it when its
  deferred render runs), and redraw the floors after layout.

### R2. An async text input loses focus while the user types (2026-11-05)
- `DetachedNodes.input` sends `update-symbol` 500 ms after the last change; `Client.proceedWith` then redraws the
  whole screen, so a new text field replaces the one being typed into.
- Fix idea: after `update-symbol`, keep the focused input (or restore focus and caret to the input with the same
  `input-name`).

### R3. A menu with anchored elements doesn't scroll (2026-11-05)
- With a `by-anchor` element in the description, the menu text is a `Label` holding a `TextFlow` (a `TextArea` can't
  hold controls), so a long description is cut off instead of scrolling.
- Fix idea: put the holder into a `ScrollPane` styled like the text area.

## Styling

### S1. The oasis stylesheets cause JavaFX `ClassCastException` warnings (2026-11-05)
- Many `CssStyleHelper calculateValue` warnings: a `String` cast to `javafx.scene.paint.Color`/`Paint`. A rule in the
  story's `fx.*.css` gives a colour property a value JavaFX can't parse (e.g. a CSS variable or a web-only colour
  syntax), so that rule is ignored.
- Fix idea: find the rules (log the property with `-Djavafx.css.debug`/a CSS logger) and fix them in the story's CSS.

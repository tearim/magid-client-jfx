# Magid JavaFX client

A desktop client for the Magid story server, written in JavaFX. It was the first Magid client and the reference the web
client (magid-client-rct, on `@magiden/client-core`) was modelled on.

## Status: mostly retired

This client is **mostly retired**. It gets updates only when the owner has time for them, and it may lag behind the
server protocol and the web client. For new work, use the web client.

It was written by hand. Only the latest updates were made by AI agents (the `Magid-Session` header and detached
elements, 2026-11-05; `story-reply`, 2026-11-08), and future updates are expected to be made by agents as well. Known problems are listed in
[`OPEN-ISSUES.md`](OPEN-ISSUES.md).

## What it supports

- Sessions (`Magid-Session` header), freshness-key/scene recovery, `client-type=javafx` stylesheets.
- Menu choices sent as `cmd=story-reply&reply=…` (`cmd` names only server commands such as `set-xml`, `list-xmls`).
- Narrations with `DCSTP_` typing, menus and commands, fade/unfade transitions, background music.
- Detached elements, routed as in `@magiden/client-core`: inputs (text, number, checkbox; async ones send
  `update-symbol`) and images, at their anchors in a narration or the menu description, or before/after the menu
  description; standalone images.

Not supported: HTML in display text (tags show as text), most of the web client's presentation features (implied
classes, typing styles).

## Build and run

Java 24 and the Maven wrapper:

```sh
./mvnw compile
./mvnw clean javafx:run
```

The server address and the story are chosen in the app's options. The server repository (`com.zarterstein`) is the
source of truth for the protocol.

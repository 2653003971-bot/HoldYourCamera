# HoldYourCamera — Store Page (English)

> Paste-ready for CurseForge / Modrinth. Suggested summary line first, then the long description.

## Summary (short, one-liner)

Hold an item, press J, pick a camera — per-item perspective rules with zero config-file knowledge required.

## Description (long)

**HoldYourCamera** is a tiny client-side QoL mod that switches your camera perspective automatically based on what you're holding. First person for pickaxes, third-person-back for swords, third-person-front for showing off your skin — set it once, and it just works every time you swap items.

### Point-and-click rules — no IDs, no TOML

- **Hold any item and press J**: an editor pops up with the item's icon rendered right there. Pick a scope — *this item only* or *the whole mod* — pick one of three perspectives (first person / third person back / third person front), save, done.
- **Empty-hand J** (or the **Config** button in Forge's mod list) opens the rule manager: global toggles, edit entries, and deletion (inside each rule's edit page).
- Rules are matched top-to-bottom; first match wins.

### Sensible defaults

- When a rule matches, your camera switches; when it stops matching, your **previous perspective is restored** automatically (it remembers what you had before taking over).
- You can still override manually with F5 at any time — unless you enable the global **lock** toggle, which holds the perspective every tick while a rule matches.
- Everything is client-side and per-player: rules live in `config/holdyourcamera-client.toml`, hot-reloaded on save if you prefer hand-editing.

### Requirements

- Minecraft 1.20.1, Forge (expected to run on NeoForge 1.20.1 47.1.x)
- **Client-side only** — safe to use on any server; nothing is sent, nothing is required server-side.

Open source under MIT. Issues and PRs welcome on GitHub.

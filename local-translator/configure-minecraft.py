from __future__ import annotations

import os
import shutil
from datetime import datetime
from pathlib import Path


SETTINGS = {
    "enabled": "true",
    "endpoint": "http://127.0.0.1:8765/translate",
    "apiKey": "",
    "showTranslations": "true",
    "chatEnabled": "true",
    "gameMessagesEnabled": "true",
    "scoreboardEnabled": "true",
    "hologramsEnabled": "true",
    "maxCharacters": "500",
}


def main() -> int:
    default_game_dir = Path(os.environ.get("APPDATA", Path.home())) / ".minecraft"
    entered = input(
        f"Minecraft のゲームフォルダーを入力してください。標準の場所を使う場合は Enter: [{default_game_dir}] "
    ).strip().strip('"')
    game_dir = Path(entered) if entered else default_game_dir
    config_dir = game_dir / "config"
    config_dir.mkdir(parents=True, exist_ok=True)
    config_path = config_dir / "kotoba-client.properties"

    values: dict[str, str] = {}
    comments: list[str] = []
    if config_path.exists():
        backup = config_path.with_name(f"kotoba-client.properties.backup-{datetime.now():%Y%m%d-%H%M%S}")
        shutil.copy2(config_path, backup)
        for line in config_path.read_text(encoding="latin-1").splitlines():
            stripped = line.strip()
            if not stripped:
                comments.append("")
            elif stripped.startswith(("#", "!")):
                comments.append(line)
            else:
                separator = next((i for i, char in enumerate(line) if char in "=:\t"), -1)
                if separator >= 0:
                    values[line[:separator].strip()] = line[separator + 1 :].strip()
    values.update(SETTINGS)
    output = comments + [f"{key}={value}" for key, value in values.items()]
    config_path.write_text("\n".join(output) + "\n", encoding="ascii")
    print(f"Local translation settings are ready: {config_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

from __future__ import annotations

import ctypes
import json
import os
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path


ROOT = Path(__file__).resolve().parent
os.environ.setdefault("ARGOS_DEVICE_TYPE", "cpu")
os.environ.setdefault("ARGOS_COMPUTE_TYPE", "int8")
os.environ.setdefault("ARGOS_INTER_THREADS", "1")
os.environ.setdefault("ARGOS_INTRA_THREADS", "1")
os.environ.setdefault("ARGOS_PACKAGES_DIR", str(ROOT / "models"))


def lower_process_priority() -> None:
    if os.name == "nt":
        try:
            # BELOW_NORMAL_PRIORITY_CLASS: yield CPU time to Minecraft and other foreground apps.
            ctypes.windll.kernel32.SetPriorityClass(ctypes.windll.kernel32.GetCurrentProcess(), 0x00004000)
        except (AttributeError, OSError):
            pass


def load_translation():
    import argostranslate.translate

    languages = argostranslate.translate.get_installed_languages()
    english = next((language for language in languages if language.code == "en"), None)
    japanese = next((language for language in languages if language.code == "ja"), None)
    if english is None or japanese is None:
        raise RuntimeError("English-to-Japanese model is missing. Run Install-Local-Translator.bat first.")
    translation = english.get_translation(japanese)
    if translation is None:
        raise RuntimeError("English-to-Japanese model is missing. Run Install-Local-Translator.bat first.")
    return translation


TRANSLATION = None


class Handler(BaseHTTPRequestHandler):
    server_version = "KotobaLocalTranslate/1.0"

    def send_json(self, status: int, payload: dict) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        if self.path.rstrip("/") == "/health":
            self.send_json(200, {"status": "ok"})
        else:
            self.send_json(404, {"error": "not found"})

    def do_POST(self) -> None:
        if self.path.rstrip("/") != "/translate":
            self.send_json(404, {"error": "not found"})
            return
        try:
            length = int(self.headers.get("Content-Length", "0"))
        except ValueError:
            self.send_json(400, {"error": "invalid content length"})
            return
        if length < 1 or length > 32_768:
            self.send_json(413, {"error": "request is empty or too large"})
            return
        try:
            request = json.loads(self.rfile.read(length).decode("utf-8"))
            text = request.get("q", "")
            if not isinstance(text, str) or not text.strip() or len(text) > 2_000:
                self.send_json(400, {"error": "q must contain 1 to 2000 characters"})
                return
            source = request.get("source", "en")
            target = request.get("target", "ja")
            if source not in ("en", "auto") or target != "ja":
                self.send_json(400, {"error": "this local service supports English to Japanese only"})
                return
            translated = TRANSLATION.translate(text)
            self.send_json(200, {"translatedText": translated})
        except (UnicodeDecodeError, json.JSONDecodeError, AttributeError):
            self.send_json(400, {"error": "invalid JSON request"})
        except Exception as error:
            print(f"Translation failed: {error}", file=sys.stderr, flush=True)
            self.send_json(500, {"error": "translation failed"})

    def log_message(self, format: str, *args) -> None:
        # Do not log chat text, API bodies, or other message contents.
        print(f"[{self.log_date_time_string()}] {format % args}", flush=True)


def main() -> int:
    global TRANSLATION
    lower_process_priority()
    try:
        TRANSLATION = load_translation()
        server = HTTPServer(("127.0.0.1", 8765), Handler)
    except Exception as error:
        print(f"Could not start local translator: {error}", file=sys.stderr)
        return 1
    print("Kotoba local translator is ready at http://127.0.0.1:8765", flush=True)
    print("CPU only, one translation at a time, below-normal process priority.", flush=True)
    print("Keep this window open while playing. Press Ctrl+C to stop.", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("Stopping local translator...", flush=True)
    finally:
        server.server_close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

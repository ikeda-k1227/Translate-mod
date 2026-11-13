from __future__ import annotations

import sys

import argostranslate.package
import argostranslate.translate


def has_english_japanese() -> bool:
    languages = argostranslate.translate.get_installed_languages()
    english = next((language for language in languages if language.code == "en"), None)
    japanese = next((language for language in languages if language.code == "ja"), None)
    return english is not None and japanese is not None and english.get_translation(japanese) is not None


def main() -> int:
    if has_english_japanese():
        print("English-to-Japanese model is already installed.")
        return 0

    print("Fetching the Argos Translate model list...")
    argostranslate.package.update_package_index()
    package = next(
        (
            item
            for item in argostranslate.package.get_available_packages()
            if item.from_code == "en" and item.to_code == "ja"
        ),
        None,
    )
    if package is None:
        print("Could not find an English-to-Japanese model in the official Argos package index.", file=sys.stderr)
        return 1

    print("Downloading and installing the English-to-Japanese model (about 115 MB)...")
    downloaded = package.download()
    argostranslate.package.install_from_path(downloaded)
    if not has_english_japanese():
        print("The model installation did not complete successfully.", file=sys.stderr)
        return 1
    print("Model ready. Future translations run locally on this computer.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

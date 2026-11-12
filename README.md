# Kotoba Client（Minecraft Java 26.2 / Fabric）

海外サーバーなどから受信した英語のチャットとゲームメッセージ、スコアボードのタイトル、名前付きエンティティのホログラムを、日本語に翻訳してクライアント側で表示する Fabric mod です。サーバー側への導入は不要です。

## 仕様

- F8（操作設定から変更可）で、以後受信する文を「原文表示／翻訳表示」に切り替えます。チャット・ゲームメッセージは原文と翻訳の同時表示をしません。
- 翻訳は表示用で、英語の発言をサーバーへ送信したり通信内容を書き換えたりしません。
- 初期状態では無効です。翻訳を有効にするには外部の LibreTranslate 互換 API が必要です。
- 日本語を含む文、空文、設定した文字数を超える文は翻訳しません。
- スコアボードはタイトル部分、ホログラムはエンティティのカスタム名を対象にします。スコアの行や `text_display` の本文、サイン、本、GUI、ボタン、アイテム名は対象外です。
- 翻訳対象の文章は設定した API 提供元へ送られます。料金・プライバシー・利用条件を確認してください。

## 別 PC でのビルドと動作確認

手順書 [別PC動作確認仕様書.md](別PC動作確認仕様書.md) に、必要環境、ビルド、ゲームへの導入、確認項目、結果記録欄をまとめています。

## ビルド環境

- Minecraft Java Edition 26.2
- Java / JDK 25
- Fabric Loader 0.19.3 以上
- Fabric API 0.155.2+26.2
- Gradle 9.5.1 以上

プロジェクトのルートで `gradle build`（Windows は `gradle.bat build`）を実行します。出力 JAR は `build/libs/kotoba-client-0.1.0.jar` です。Java と Gradle がインストールされた PC が必要です。

## 設定

Fabric プロファイルで一度起動すると `config/kotoba-client.properties` が作られます。ゲームを終了してから編集し、翻訳 API の URL と必要な API キーを設定してください。`enabled` は翻訳全体、`chatEnabled` / `gameMessagesEnabled` / `scoreboardEnabled` / `hologramsEnabled` は個別の対象、`showTranslations` は表示モードの初期値です。ゲーム中は F8 でも原文／翻訳の表示を切り替えられます。

```properties
enabled=true
chatEnabled=true
gameMessagesEnabled=true
scoreboardEnabled=true
hologramsEnabled=true
showTranslations=true
endpoint=https://libretranslate.com/translate
apiKey=
maxCharacters=500
```

API ごとに利用条件・キーの要否が異なります。LibreTranslate 互換でも公開サービスの URL が変更・停止される場合があります。必要なら利用可能な API の URL に変更してください。初期設定の `enabled=false` では外部通信しません。

# Kotoba Client（Minecraft Java 26.2 / Fabric）

海外サーバーなどから受信した英語のチャットとゲームメッセージ、スコアボードのタイトル、名前付きエンティティのホログラムを、日本語に翻訳してクライアント側で表示する Fabric mod です。サーバー側への導入は不要です。初期設定で API キーなしの Google 翻訳 Web エンドポイントを利用します。

## 仕様

- F8（操作設定から変更可）で、以後受信する文を「原文表示／翻訳表示」に切り替えます。チャット・ゲームメッセージは原文と翻訳の同時表示をしません。
- 翻訳は表示用で、英語の発言をサーバーへ送信したり通信内容を書き換えたりしません。
- 初期設定では無料の Google 翻訳 Web エンドポイントを使い、別アプリや API キーの設定は不要です。この接続は非公式で、制限・仕様変更・停止の可能性があります。代替としてキー不要の MyMemory も選べますが、匿名利用は1日5,000文字までで、問い合わせ文は長期保存されます。
- 日本語を含む文、空文、設定した文字数を超える文は翻訳しません。
- スコアボードはタイトル部分、ホログラムはエンティティのカスタム名を対象にします。スコアの行や `text_display` の本文、サイン、本、GUI、ボタン、アイテム名は対象外です。
- 翻訳対象の英語文は選択した外部サービスへ送信されます。Google 接続は非公式 Web API で、MyMemory は問い合わせ文を長期保存する規約です。外部へ送信したくない場合は翻訳を無効にしてください。

## 別 PC でのビルドと動作確認

別 PC の確認項目は [別PC動作確認仕様書.md](別PC動作確認仕様書.md) を参照してください。

## ビルド環境

- Minecraft Java Edition 26.2
- Java / JDK 25
- Fabric Loader 0.19.3 以上
- Fabric API 0.155.2+26.2
- Gradle 9.5.1 以上

プロジェクトのルートで `gradle build`（Windows は `gradle.bat build`）を実行します。出力 JAR は `build/libs/kotoba-client-0.1.0.jar` です。Java と Gradle がインストールされた PC が必要です。

## 設定

初回起動時に `config/kotoba-client.properties` を作成します。`enabled` は翻訳全体、`chatEnabled` / `gameMessagesEnabled` / `scoreboardEnabled` / `hologramsEnabled` は個別の対象、`showTranslations` は表示モードの初期値です。ゲーム中は F8 でも原文／翻訳の表示を切り替えられます。`provider=google` がキー不要の既定値です。MyMemory を選ぶ場合は `provider=mymemory`、LibreTranslate 互換 API を使う場合は `provider=libretranslate` にして `endpoint` と必要なら `apiKey` を設定してください。

# Kotoba Client 別 PC 動作確認仕様書

## 1. 目的

開発 PC に Minecraft がないため、Minecraft Java Edition 26.2 と Fabric を利用できる別 PC でビルドとゲーム内動作を確認します。確認結果と、問題発生時のログを記録してください。

## 2. 対象環境

- Minecraft Java Edition **26.2**
- Java / JDK **25**
- Fabric Loader **0.19.3 以上**
- Fabric API **0.155.2+26.2**
- Gradle **9.5.1 以上**
- 利用可能な LibreTranslate 互換翻訳 API（T3 / T5 の確認時）

バージョンは必ず 26.2 向けに揃えます。Fabric API と Kotoba Client はクライアント側に導入します。テストはシングルプレイ、または許可を得たテストサーバーで行ってください。

## 3. ビルド

1. `kotoba-client` プロジェクト一式を別 PC にコピーします。
2. JDK 25 と Gradle 9.5.1 以上をインストールし、ターミナルで `java -version` と `gradle --version` を確認します。
3. プロジェクトのルート（`build.gradle` があるフォルダ）で実行します。
   - Windows: `gradle.bat build`
   - macOS / Linux: `gradle build`
4. `build/libs/kotoba-client-0.1.0.jar` が生成されればビルド成功です。
5. 失敗した場合は OS、`java -version`、`gradle --version`、エラー全文を記録します。

## 4. Minecraft への導入

1. 公式ランチャーで Minecraft 26.2 を一度起動して終了します。
2. Fabric Installer で Minecraft **26.2** 用の Fabric Loader をインストールします。
3. 次の 2 つの JAR を、ゲームディレクトリの `mods` フォルダに入れます。
   - ビルドした `kotoba-client-0.1.0.jar`
   - Minecraft 26.2 対応の Fabric API（`0.155.2+26.2`）
4. Fabric プロファイルを起動し、タイトル画面まで進めることを確認します。
5. 一度ゲームを終了し、`config/kotoba-client.properties` ができていることを確認します。
6. 利用する翻訳 API の URL と必要な API キーを設定し、`enabled=true` にして保存します。個別設定 `chatEnabled`、`gameMessagesEnabled`、`scoreboardEnabled`、`hologramsEnabled` も必要に応じて切り替えます。
7. 再起動します。

代表的なゲームディレクトリは次のとおりです。

- Windows: `%AppData%\\.minecraft`
- macOS: `~/Library/Application Support/minecraft`
- Linux: `~/.minecraft`

## 5. 確認ケース

| ID | 操作 | 期待結果 | 合否 |
|---|---|---|---|
| T1 | Fabric プロファイルを起動 | クラッシュせずタイトル画面に到達。ログに Kotoba Client が読み込まれた記録がある | |
| T2 | 初期設定（`enabled=false`）で英語チャットを受信 | 原文が通常どおり表示され、翻訳通信は行われない | |
| T3 | 有効な API を設定し、翻訳表示モードで `Hello, how are you?` を受信 | 原文の代わりに日本語訳がチャット欄に表示される | |
| T4 | 日本語だけのチャットを受信 | 原文がそのまま表示され、翻訳通信は行われない | |
| T5 | 英語の通常システムメッセージを発生させる | 翻訳表示モードでは日本語訳が表示される | |
| T6 | API URL を無効にする、または一時的に通信を切る | ゲームが操作不能にならず、翻訳に失敗した文は原文で表示される | |
| T7 | 英語の発言を送信 | 送信内容は元のままで、mod が翻訳文をサーバーに送らない | |
| T8 | `maxCharacters` を超える英語文を受信 | 翻訳行が追加されない | |
| T9 | アクションバーの英語システム文を表示 | 翻訳表示モードでは訳文がアクションバーに表示される | |
| T10 | 操作設定で Kotoba の表示切替キーを確認し、F8 を押して英語チャットを受信 | 原文表示に切り替わり、訳文は表示されない。もう一度押すと、以後の受信文は翻訳表示 | |
| T11 | `chatEnabled=false` にして英語チャットを受信 | チャット原文が表示され、翻訳されない。他の対象は個別設定に従う | |
| T12 | 英語のスコアボードタイトルを表示 | `scoreboardEnabled=true` かつ翻訳表示モードなら、タイトルが日本語になる（初回応答後に反映） | |
| T13 | 英語のカスタム名を持つ Armor Stand 等の名前付きホログラムを表示 | `hologramsEnabled=true` かつ翻訳表示モードなら、名前が日本語になる（初回応答後に反映） | |

## 6. 合格基準と記録

T1〜T13 を実施して合否を記録します。T1、T3、T4、T5、T6、T7、T10〜T13 が合格し、ゲームがクラッシュしないことを基本合格とします。API の停止・レート制限・無効なキーによる翻訳失敗は、その状況も記録してください。

- 確認日 / 確認者:
- PC / OS:
- Minecraft / Fabric Loader / Fabric API / Java:
- ビルド結果:
- T1〜T13 の結果:
- 備考・再現手順:

問題がある場合は `logs/latest.log` と該当するクラッシュレポートを添付します。API キー、サーバーの認証情報、個人情報はログ共有前に削除してください。

## 7. 既知の制限

翻訳表示モードは新しく受信するチャット／ゲームメッセージに適用し、すでにチャット履歴に表示された過去の行は書き換えません。スコアボード対応はタイトル部分です。ホログラム対応は Entity のカスタム名（例: Armor Stand）です。スコアの行、`text_display` の本文、サイン、本、GUI、ボタン、アイテム名などは対象外です。翻訳品質・応答速度・利用料金は API 提供元に依存します。開発 PC には Minecraft がないため、別 PC でのゲーム起動と動作確認は未実施です。

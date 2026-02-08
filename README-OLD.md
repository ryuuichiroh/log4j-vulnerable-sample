# log4j-vulnerable-sample

- Dependency-Track (DT) を使った脆弱性管理の検証用のプロジェクトです。
- 意図的に脆弱性のある OSS を利用しています。

## GitHub Actions

- PR 発行時に SBOM 生成 + 脆弱性スキャン
- main マージ時に DT へ登録
- (手動) VEX の引継ぎ
- (手動) 古いプロジェクトのディアクティブ

## 検証手順

### SBOM 作成、DT への登録から更新まで

1. log4j 1.1.3 に依存があるプロジェクトで SBOM を作成する

    ```bash
    docker run --rm -v $(pwd):/work aquasec/trivy fs \
      --scanners vuln \
      --format cyclonedx \
      --output /work/sbom-log4j-1.1.3.json \
      /work
    ```

1. log4j 1.1.3 への依存関係を持つ SBOM を DT に送信する

    DT 上でのプロジェクトバージョンは 1.0 とする。

    ```bash
    curl -X "POST" "http://xx.xx.xx.xx:8081/api/v1/bom" \
      -H "X-Api-Key:odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" \
      -F "autoCreate=true" \
      -F "projectName=log4j-vulnerable-sample" \
      -F "projectVersion=1.0" \
      -F "bom=@sbom-log4j-1.1.3.json" -v -i -sS
    ```

1. DT 上で 1.0 のプロジェクトを表示し、任意の脆弱性を1つ解決済みにする

1. 1.0 のプロジェクトの VEX を取得する

    ```bash
    curl http://xx.xx.xx.xx:8081/api/v1/vex/cyclonedx/project/87ba7cf1-72f1-4e00-ae7a-4f208afd56a3 -H "X-Api-Key:odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    ```

1. pom.xml を更新し、log4j のバージョンを 1.1.3 から 1.2.17 に更新し、SBOM を作成する

    ```bash
    docker run --rm -v $(pwd):/work aquasec/trivy fs \
      --scanners vuln \
      --format cyclonedx \
      --output /work/sbom-log4j-1.2.17.json \
      /work
    ```

1. log4j 1.2.17 への依存関係を持つ SBOM を DT に送信する

    DT 上でのプロジェクトバージョンは 2.0 とする。

    ```bash
    curl -X "POST" "http://xx.xx.xx.xx:8081/api/v1/bom" \
      -H "X-Api-Key:odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" \
      -F "autoCreate=true" \
      -F "projectName=log4j-vulnerable-sample" \
      -F "projectVersion=2.0" \
      -F "bom=@sbom-log4j-1.2.17.json" -v -i -sS
    ```
1. VEX を 2.0 のプロジェクトに適用する

    ```bash
    curl -X "POST" "http://xx.xx.xx.xx:8081/api/v1/vex" \
      -H "X-Api-Key:odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" \
      -F "projectName=log4j-vulnerable-sample" \
      -F "projectVersion=2.0" \
      -F "vex=@vex.json" -v -i -sS
    ```

1. 2.0 のプロジェクトの VEX を取得する

    ```bash
    # GET /v1/vex/cyclonedx/project/{uuid}
    curl "http://xx.xx.xx.xx:8081/api/v1/vex/cyclonedx/project/faaaac4d-3226-4e98-a8a7-2304038e290c" \
      -H "X-Api-Key:odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    ```
1. 1.0 のプロジェクトを非アクティブにする

    ```bash
    # PATCH /v1/project/{uuid}
    curl -X PATCH "http://xx.xx.xx.xx:8081/api/v1/project/87ba7cf1-72f1-4e00-ae7a-4f208afd56a3" \
      -H "X-Api-Key: odt_xxxxxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" \
      -H "Content-Type: application/json" \
      -d '{
        "active": false
      }' -v -i -sS
    ```

### DT のプロジェクトに紐づくプロパティの管理

#### ProjectProperty

1. ProjectProperty を取得する

    ```bash
    # Project
    # GET /v1/project
    curl http://20.78.124.206:8081/api/v1/project -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2"

    # ProjectProperty of a specified project
    # GET /v1/project/{uuid}
    curl http://20.78.124.206:8081/api/v1/project/dab05b7f-738b-488b-8dc3-775b5946926f/property -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2"
    ```

1. ProjectProperty を登録する

    ```bash
    # PUT /v1/project/{uuid}/property
    # maxLenth of groupName/propertyName/description: 255
    # propertyType: [ BOOLEAN, INTEGER, NUMBER, STRING, ENCRYPTEDSTRING, TIMESTAMP, URL, UUID ]
    curl -X PUT http://20.78.124.206:8081/api/v1/project/dab05b7f-738b-488b-8dc3-775b5946926f/property \
      -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2" \
      -H "Content-Type: application/json" \
      -d '{
        "groupName": "customGroup",
        "propertyName": "name 1",
        "propertyValue": "false",
        "propertyType": "STRING",
        "description": "description 1"
      }'
    ```

#### ComponentProperty

1. ComponentProperty を取得する

    ```bash
    # Component of a specified project
    # GET /v1/component/project/{uuid}
    curl http://20.78.124.206:8081/api/v1/component/project/dab05b7f-738b-488b-8dc3-775b5946926f -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2"

    # ComponentProperty of a specified component
    # GET /v1/component/{uuid}
    curl http://20.78.124.206:8081/api/v1/component/17cf02f3-87ff-4115-b81d-b332d08a6985/property -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2"
    ```

1. ComponentProperty を登録する

    ```bash
    # PUT /v1/component/{uuid}/property
    # maxLenth of groupName/propertyName/description: 255
    # propertyType: [ BOOLEAN, INTEGER, NUMBER, STRING, ENCRYPTEDSTRING, TIMESTAMP, URL, UUID ]
    curl -X PUT http://20.78.124.206:8081/api/v1/component/17cf02f3-87ff-4115-b81d-b332d08a6985/property \
      -H "X-Api-Key:odt_xCqztWmz_Ie9NeWzYwcbP7cmrehZVikEDQoY1UwR2" \
      -H "Content-Type: application/json" \
      -d '{
        "groupName": "customGroup",
        "propertyName": "name 1",
        "propertyValue": "false",
        "propertyType": "STRING",
        "description": "description 1"
      }'
    ```

## 補足

- DT の Web API 仕様書: dependency-track/openapi.json

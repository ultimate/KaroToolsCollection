# Übergabe: Codex-Cloud-Umgebung für Karopapier Wrapper

Diese Anleitung kann vollständig an einen Codex-Environment-Setup-Agenten weitergegeben werden. Sie beschreibt die eingerichtete und nach Veröffentlichung erneut geprüfte Umgebung. Sie enthält keine Zugangsdaten.

## Auftrag an den Setup-Agenten

Richte die Umgebung für das Repository `https://gitlab.xosofox.de/karopapier/wrapper.git` ein. Verwende den vorhandenen Cloud-Onboarding-Skill, falls verfügbar. Führe Installation, Initialisierung und Prüfungen in der tatsächlichen Maschine aus und speichere anschließend die unten angegebenen Inhalte als `install_script` und `start_skill` im Umgebungsentwurf.

Nutze das bestehende Checkout, sofern es am vorgesehenen Pfad vorhanden ist. Jede Cloud-Aufgabe ist bereits isoliert; lege ohne ausdrücklichen Auftrag keinen Git-Worktree an. Erhalte bestehende lokale Änderungen. Verändere beim Einrichten keine versionierten Quelldateien, Tests, Composer-Dateien, Lockfiles oder Formatierungsregeln. Generierte Dateien und Hilfsprogramme liegen außerhalb des Checkouts oder in dessen ignorierten Verzeichnissen.

Die Einrichtung soll allgemein auf dem Standardbranch funktionieren, nicht nur für einen einzelnen Issue-Branch. Bekannte fehlgeschlagene Projektprüfungen werden dokumentiert, nicht durch geänderte Assertions oder ausgeschaltete Prüfungen verdeckt.

## Zielzustand und Voraussetzungen

| Bestandteil | Einrichtung |
| --- | --- |
| Host der geprüften Umgebung | Debian GNU/Linux 13, Benutzer `agent`, Bash |
| Projekt | `/workspace/wrapper`, Standardbranch `master` |
| Git-Remote | `origin = https://gitlab.xosofox.de/karopapier/wrapper.git` |
| PHP | 8.3.36, CLI, 1024 MiB memory_limit |
| Composer | 2.11.2 als `composer` |
| PHPUnit | 9.6.38 aus der vorhandenen `composer.lock` |
| PHP-CS-Fixer | 3.95.27 aus der vorhandenen `composer.lock` |
| PHPStan | Aus der vorhandenen `composer.lock`, mit registrierten Erweiterungen |
| PHP-Laufzeit | Vorbereitetes Docker-Image `codex-php83:local` |
| Docker | Funktionierender verwalteter Socket `unix:///var/run/docker.sock`; hier rootless Docker |
| Hilfsdateien | `/workspace/.gitlab-setup` und `/workspace/.php83` |
| Befehle im Benutzer-PATH | `~/.local/bin/php` und `~/.local/bin/composer` |
| Testdatenbank | SQLite in-memory gemäß `.env.test`; Fixtures über vorhandenes Liip-/DAMA-Setup |

PHP ist über normale Befehle verfügbar, wird intern aber bei jedem Aufruf in einem kurzlebigen Container ausgeführt. Es ist keine native PHP-Systeminstallation auf dem Host und es muss kein dauerhafter PHP-Testcontainer gestartet werden.

Die Docker-Basisimages sind im Installationsskript per Digest festgelegt. APT-Pakete werden aus signierten Debian-Paketquellen installiert; ihre einzelnen Versionen sind nicht fixiert. Damit ist dies eine Anleitung für eine funktional gleiche Umgebung, keine Zusicherung eines byteidentischen Dateisystems. Andere Repository-Stände können andere Werkzeugversionen und Testergebnisse ergeben.

Das vorhandene GitHub-Dummy-Repository war lediglich die ursprüngliche Plattformauswahl. Es ist keine Projektabhängigkeit und kein Push-Ziel. GitLab wird durch das unten stehende Skript geklont; eine gegebenenfalls ausschließlich GitHub unterstützende Repository-Auswahl der Plattform ersetzt diesen Schritt nicht.

## Zugang und Netzwerk

### GITLAB_TOKEN

Der jeweilige Benutzer muss `GITLAB_TOKEN` sicher als direkt verfügbare Umgebungsvariable in seinen Umgebungseinstellungen hinterlegen. Der Token braucht Zugriff auf das Projekt und für spätere Pushes die GitLab-Berechtigung `write_repository`. Branch-Schutzregeln gelten zusätzlich.

Der Tokenwert gehört nicht in diese Anleitung, den Chat, eine Git-URL, ein Image, das Repository oder einen Installationsskript-Literalwert. Das Skript liest ihn erst zur Laufzeit über Git Askpass aus der Host-Umgebung. Der PHP-Wrapper reicht ihn nicht in den PHP-Container weiter.

Vorhandene Variablen und Zugangsmöglichkeiten zuerst prüfen; Werte niemals ausgeben. Ein eigener Token ist nötig, der Token des ursprünglichen Umgebungsbesitzers wird nicht übertragen.

### Freigaben

Aktiviere das Plattform-Preset für Paketmanager-Domains und ergänze mindestens die benutzerdefinierten Freigaben:

```text
gitlab.xosofox.de
api.github.com
```

Die tatsächlich verwendeten Downloadziele umfassen außerdem:

```text
github.com
codeload.github.com
repo.packagist.org
cdn.jsdelivr.net
deb.debian.org
registry-1.docker.io
auth.docker.io
```

Dazu kommen gegebenenfalls die vom Docker-Registry-Dienst verwendeten Download-/CDN-Hosts. Verwende vorhandene Paketmanager-/Registry-Presets und die verwaltete Registry-Konfiguration; ergänze konkret gemeldete erforderliche Ziele, wenn sie fehlen. Erhalte alle anderen bereits benötigten Freigaben, statt eine unbekannte Allowlist zu ersetzen.

Wichtig ist der Unterschied zwischen gespeichertem Entwurf und aktiver Netzwerkpolitik der laufenden Instanz. Prüfe nach Übernahme/Veröffentlichung erneut mit tatsächlichen Git-, Composer- und Docker-Aufrufen. In dieser Einrichtung war `api.github.com` zunächst nur im Entwurf freigegeben; erst nach Veröffentlichung funktionierten normale Composer-Dist-Downloads.

### Proxy und Zertifikate

Erhalte die geerbte Proxy-Konfiguration und die verwalteten Zertifikate. Der PHP-Wrapper bindet `/etc/ssl/certs/ca-certificates.crt` vom aktuellen Host schreibgeschützt ein und setzt:

- `GIT_SSL_CAINFO`
- `SSL_CERT_FILE`
- `COMPOSER_CAFILE`
- PHP `openssl.cafile` und `curl.cainfo`

Nur `COMPOSER_CAFILE` reicht nicht für Git-Source-Downloads. TLS-, Paket- und Artefaktverifikation bleiben eingeschaltet. Mit den richtigen Freigaben sind keine URL-Ersetzungen in Lockfiles erforderlich.

## Vollständiges install_script

Den folgenden Block als vollständigen Inhalt des Konfigurationsfelds `install_script` speichern und in der tatsächlichen Umgebung ausführen. Er erzeugt Dockerfile und Wrapper selbst; zusätzliche Anhänge werden nicht benötigt.

Das Skript benötigt Schreibzugriff auf `/workspace` sowie für die Befehlslinks auf `~/.local/bin`. Unterstützte Berechtigungs-/Setup-Werkzeuge der jeweiligen Cloud-Umgebung verwenden; keine Sandbox- oder sudo-Konfiguration verändern. Vorhandene fremde Befehlslinks oder abweichende Checkouts werden nicht überschrieben.

Die PHP-Basis bringt unter anderem ctype, curl, DOM/XML, fileinfo, iconv, json, mbstring, OpenSSL und SQLite bereits mit. Das Dockerfile ergänzt GD mit JPEG/Freetype, Intl, Zip, PDO-MySQL, MySQLi, Bcmath, GMP und Calendar sowie Git und unzip.

```bash
#!/usr/bin/env bash
set -euo pipefail
mkdir -p /workspace/.gitlab-setup
cat > /workspace/.gitlab-setup/askpass <<'EOF'
#!/usr/bin/env bash
case "$1" in
  *Username*) printf '%s\n' oauth2 ;;
  *Password*) printf '%s\n' "${GITLAB_TOKEN:?GITLAB_TOKEN fehlt}" ;;
  *) exit 1 ;;
esac
EOF
chmod 700 /workspace/.gitlab-setup/askpass
export GIT_ASKPASS=/workspace/.gitlab-setup/askpass
export GIT_TERMINAL_PROMPT=0
url=https://gitlab.xosofox.de/karopapier/wrapper.git
target=/workspace/wrapper
if [ ! -e "$target" ]; then
  git -c credential.helper= clone "$url" "$target"
fi
test "$(git -C "$target" rev-parse --show-toplevel)" = "$target"
test "$(git -C "$target" remote get-url origin)" = "$url"
git -C "$target" config credential.helper ''
git -C "$target" config core.askPass /workspace/.gitlab-setup/askpass
git -C "$target" config remote.pushDefault origin
git -C "$target" config push.default current
git -C "$target" ls-remote origin HEAD

# PHP 8.3 and Composer: reproducible user-level CLI tools backed by Docker.
mkdir -p /workspace/.php83/bin
cat > /workspace/.php83/Dockerfile <<'PHP83_DOCKER'
FROM composer:2@sha256:0c8e663f14c94d07b3c45b175c51f5a14718508f21fe4dd7e54f09488dd0ed71 AS composer
FROM php:8.3-cli-bookworm@sha256:232331112d3a7c5b98edeeddc3b85f610172f99352c0635fe6b3e166e47062cb
RUN apt-get update && apt-get install -y --no-install-recommends git unzip libpng-dev libjpeg62-turbo-dev libfreetype6-dev libicu-dev libxml2-dev libonig-dev libzip-dev libgmp-dev \
    && rm -rf /var/lib/apt/lists/*
RUN docker-php-ext-configure gd --with-freetype --with-jpeg \
    && docker-php-ext-install -j4 gd intl zip pdo_mysql mysqli bcmath gmp calendar \
    && rm -rf /var/lib/apt/lists/*
COPY --from=composer /usr/bin/composer /usr/local/bin/composer
RUN php -r 'exit(PHP_MAJOR_VERSION === 8 && PHP_MINOR_VERSION === 3 ? 0 : 1);' && composer --version
PHP83_DOCKER
cat > /workspace/.php83/bin/php <<'PHP83_WRAPPER'
#!/usr/bin/env bash
set -euo pipefail
extra=()
for name in APP_ENV APP_DEBUG DATABASE_URL SYMFONY_DEPRECATIONS_HELPER COMPOSER_CACHE_DIR PHP_CS_FIXER_IGNORE_ENV; do
  if [[ -v "$name" ]]; then extra+=(-e "$name"); fi
done
exec env -u DOCKER_HOST -u DOCKER_CONTEXT -u DOCKER_TLS -u DOCKER_TLS_VERIFY -u DOCKER_CERT_PATH \
  docker --host=unix:///var/run/docker.sock run --pull=never --rm -i \
  --user "$(id -u):$(id -g)" \
  --mount type=bind,src=/workspace,dst=/workspace \
  --mount type=bind,src=/tmp,dst=/tmp \
  --mount type=bind,src=/etc/ssl/certs/ca-certificates.crt,dst=/run/host-ca.pem,readonly \
  -e GIT_SSL_CAINFO=/run/host-ca.pem -e SSL_CERT_FILE=/run/host-ca.pem -e COMPOSER_CAFILE=/run/host-ca.pem \
  -e COMPOSER_HOME=/workspace/.php83/composer-home \
  "${extra[@]}" -w "$PWD" codex-php83:local php \
  -d memory_limit=1024M -d openssl.cafile=/run/host-ca.pem -d curl.cainfo=/run/host-ca.pem "$@"
PHP83_WRAPPER
cat > /workspace/.php83/bin/composer <<'COMPOSER_WRAPPER'
#!/usr/bin/env bash
set -euo pipefail
exec /workspace/.php83/bin/php /usr/local/bin/composer "$@"
COMPOSER_WRAPPER
chmod 755 /workspace/.php83/bin/php /workspace/.php83/bin/composer
if ! env -u DOCKER_HOST -u DOCKER_CONTEXT -u DOCKER_TLS -u DOCKER_TLS_VERIFY -u DOCKER_CERT_PATH docker --host=unix:///var/run/docker.sock image inspect codex-php83:local >/dev/null 2>&1; then
  env -u DOCKER_HOST -u DOCKER_CONTEXT -u DOCKER_TLS -u DOCKER_TLS_VERIFY -u DOCKER_CERT_PATH docker --host=unix:///var/run/docker.sock build -t codex-php83:local /workspace/.php83
fi
mkdir -p "$HOME/.local/bin"
for tool in php composer; do
  link="$HOME/.local/bin/$tool"
  if [ ! -e "$link" ] && [ ! -L "$link" ]; then
    ln -s "/workspace/.php83/bin/$tool" "$link"
  else
    test "$(readlink "$link")" = "/workspace/.php83/bin/$tool"
  fi
done
export PATH="/workspace/.php83/bin:$PATH"
cd /workspace/wrapper
php -v
composer --version
composer check-platform-reqs --lock

# Install locked application and development dependencies without applying recipes.
cd /workspace/wrapper
composer install --no-scripts --no-plugins --no-interaction --prefer-dist
# Activate the configured plugins for generated autoload/runtime files.
composer dump-autoload --no-scripts --no-interaction
# Tested project post-install event: registers PHPStan extensions and runs
# cache:clear, assets:install and importmap:install (generated/ignored outputs).
APP_ENV=test APP_DEBUG=0 composer run-script post-install-cmd --no-interaction
composer check-platform-reqs
vendor/bin/phpunit --testsuite Karopapier
sha256sum composer.lock > /workspace/.php83/installed-composer-lock.sha256

```

### Warum diese Composer-Schritte?

1. `composer install --no-scripts --no-plugins` installiert die exakten Entwicklungsabhängigkeiten, ohne Rezepte auf Projektdateien anzuwenden.
2. `composer dump-autoload --no-scripts` initialisiert die konfigurierten Plugins und generiert unter anderem den Symfony-Runtime-Autoloader.
3. `APP_ENV=test APP_DEBUG=0 composer run-script post-install-cmd` registriert die PHPStan-Erweiterungen und führt die getesteten Projektschritte `cache:clear`, `assets:install public` und `importmap:install` aus.
4. `composer check-platform-reqs` prüft die tatsächlich installierten Pakete gegen die wirkliche Laufzeit.
5. Die benannte Kernsuite prüft anschließend den regulären Bootstrap und funktionale Tests.

Nur `--no-scripts` dauerhaft beizubehalten wäre unvollständig: Dann fehlen insbesondere Importmap-Assets und die automatisch registrierten PHPStan-Erweiterungen. Die genannten Initialisierungsschritte schrieben in der geprüften Umgebung nur generierte/ignorierte Dateien.

## Vollständiges start_skill

Den folgenden Block als Inhalt des Konfigurationsfelds `start_skill` speichern. Die englische Fassung ist die tatsächlich gespeicherte Agentenanweisung.

```text
Work in /workspace/wrapper for all project commands. Each cloud task is isolated; reuse the existing checkout and do not create Git worktrees unless the user explicitly requests one. Preserve local changes and read repository agent/setup documentation.

PHP 8.3.36 and Composer 2.11.2 are available through ~/.local/bin and /workspace/.php83/bin. If needed, export PATH="/workspace/.php83/bin:$PATH". The commands use the retained codex-php83:local Docker image and managed unix:///var/run/docker.sock. No persistent PHP container needs restarting; do not create ad hoc test/tool containers. /workspace and /tmp are mounted at identical paths. The runtime mounts the current managed CA bundle and sets GIT_SSL_CAINFO, SSL_CERT_FILE, COMPOSER_CAFILE, openssl.cafile and curl.cainfo. Keep verification and proxy settings enabled. GITLAB_TOKEN stays in the host environment and is never embedded in the image, logs, or remote URLs.

At task start, reuse the prepared vendor directory and generated assets. Run the saved install_script if the checkout, tool wrappers, Docker image, vendor/bin/phpunit, vendor/bin/php-cs-fixer, importmap assets, or vendor/autoload_runtime.php are absent, or if (cd /workspace/wrapper && sha256sum -c /workspace/.php83/installed-composer-lock.sha256) fails. The script clones only if absent, installs exact locked dependencies (including dev tools), initializes autoload/plugins, runs the tested post-install event and checks the platform and core suite. It does not reset local work, change lockfiles, apply recipes, or format source. Installation needs api.github.com, codeload.github.com, github.com, repo.packagist.org and cdn.jsdelivr.net. Report destination denials rather than disabling verification or rewriting lockfile URLs.

Normal project-wide commands, from /workspace/wrapper:
- composer check-platform-reqs
- vendor/bin/phpunit
- vendor/bin/phpunit --testsuite Karopapier
- vendor/bin/phpunit <test-file-or-directory>
- vendor/bin/php-cs-fixer fix --config=.php-cs-fixer.dist.php --dry-run --using-cache=no --diff (append paths to check selected files)
- APP_ENV=test APP_DEBUG=0 vendor/bin/phpstan analyse -c phpstan.neon --memory-limit=512M --no-progress

Post-install setup tested in APP_ENV=test APP_DEBUG=0: composer run-script post-install-cmd --no-interaction. This registers PHPStan extensions and runs cache:clear, assets:install public and importmap:install; outputs are in ignored locations. Functional tests use the repository's .env.test SQLite in-memory database and load fixtures through the existing Liip/DAMA test setup. No external database is required for the verified default suite. Do not copy production credentials into test overrides.

Validation on unchanged master 1fb057e3ead37608d10cd587428420da66c1d973: core suite 209 tests / 1255 assertions / 2 skipped; full standard run 352 tests / 3523 assertions / 1 failure / 2 skipped. The failure is the timing comparison in Modules/MapGenerator/ulli/MapCharacteristicsTest::testGetWithKnownCharacteristics, also reproduced in isolation; do not disable it or report the full suite as green. The default run excludes the repository's long-running group. Full PHP-CS-Fixer check ran over 735 files and found 15 files needing formatting. PHPStan ran with its configured extensions and reported 84 existing code/baseline findings. These are repository findings, not missing tools. See /workspace/.php83/READINESS.md and reports for details. The independent codex/281 comparison also passed its 12 illumination tests and 4-file style check; setup is not specific to that branch.

For requested code changes, validate, commit the intended changes and push the task branch with git push -u origin HEAD to https://gitlab.xosofox.de/karopapier/wrapper.git. Verify origin and any push URL first. Never push this work to ultimate/dummy, overwrite unrelated changes, or force-push. Report authorization or branch protection failures.

```

## Reguläre Arbeit und Prüfung

Alle folgenden Befehle werden aus `/workspace/wrapper` ausgeführt:

```bash
cd /workspace/wrapper
php -v
php -m
composer --version
composer check-platform-reqs

# Gesamter Standard-Testlauf mit der vorhandenen Projektkonfiguration:
vendor/bin/phpunit

# Benannte Kernsuite, einschließlich funktionaler Tests:
vendor/bin/phpunit --testsuite Karopapier

# Beliebige einzelne Testdatei oder ein Verzeichnis:
vendor/bin/phpunit tests/src/Entity/UserExtendedSettingsTest.php

# Gesamter Codestyle-Check ohne Änderungen:
vendor/bin/php-cs-fixer fix \
  --config=.php-cs-fixer.dist.php --dry-run --using-cache=no --diff

# Bei Bedarf Dateipfade an den Codestyle-Aufruf anhängen.
# Statische Analyse mit der vorhandenen Konfiguration:
APP_ENV=test APP_DEBUG=0 vendor/bin/phpstan analyse \
  -c phpstan.neon --memory-limit=512M --no-progress

git diff --check
git status --short
```

Die Tools werden aus der Projekt-Lockdatei installiert, nicht durch ein separates temporäres PHPUnit-/PHP-CS-Fixer-Projekt ersetzt. Der reguläre PHPUnit-Aufruf lädt `phpunit.xml.dist`, `tests/bootstrap.php`, den Symfony-Listener, DAMA-Erweiterung und Projekt-Testhooks.

`GITLAB_TOKEN` wird für Host-Git verwendet. Der PHP-Wrapper reicht die in seinem Skript ausdrücklich aufgelisteten Variablen weiter; beliebige zusätzliche Host-Variablen werden nicht automatisch in den Container übernommen. Falls künftig weitere Variablen für Tests erforderlich sind, muss deren Weitergabe gezielt ergänzt werden. `/workspace` und `/tmp` sind in Host und Container unter denselben Pfaden verfügbar.

Nach Codeänderungen sollen die beabsichtigten Änderungen geprüft, committet und an den Arbeitsbranch dieses GitLab-Repositories gepusht werden, entsprechend dem Auftrag des jeweiligen Besitzers. Kein Force-Push und kein Push zum Dummy-Repository.

## Abnahme nach Veröffentlichung und Wiederherstellung

Das Speichern eines Entwurfs führt Skripte nicht aus und veröffentlicht keine Umgebung. Nach der tatsächlichen Einrichtung den Entwurf über die Plattform veröffentlichen und in einer daraus wiederhergestellten Instanz prüfen:

1. Der gewünschte Arbeitsordner, das Checkout und der Branch sind vorhanden.
2. `php` und `composer` sind im PATH verfügbar; das Docker-Image ist vorhanden oder kann durch das Installationsskript wieder aufgebaut werden.
3. `composer check-platform-reqs` besteht.
4. Git-Lesezugriff funktioniert; ein Push-Dry-Run kann die Verbindung zum Push-Endpunkt prüfen, ändert aber keine Remote-Refs und garantiert nicht die Annahme späterer tatsächlicher Pushes durch Branch-Regeln.
5. Eine Installation mit leerem Cache nutzt die regulären Downloadziele und aktive TLS-Verifikation.
6. Tests und Codestyle laufen über die Projektwerkzeuge, ohne Minimal-Bootstrap und ohne deaktivierte Assertions.
7. Versionierte Projektdateien und Lockfiles bleiben bei der Einrichtung unverändert.
 
### Installation mit leerem Cache prüfen

Dieser isolierte Test installiert dieselben Pakete in ein temporäres Verzeichnis, ohne die vorbereiteten Projektabhängigkeiten zu verändern:

```bash
probe=$(mktemp -d /tmp/wrapper-cold-install.XXXXXX)
mkdir -p "$probe/project"
cp /workspace/wrapper/composer.json /workspace/wrapper/composer.lock "$probe/project/"

COMPOSER_CACHE_DIR="$probe/cache" composer install \
  --working-dir="$probe/project" \
  --no-plugins --no-scripts --no-autoloader --no-interaction --prefer-dist
```

Dies prüft Downloads und Paketinstallation. Das eigentliche Projekt wird separat mit den oben beschriebenen Plugins, Autoload-Dateien und Autoskripten initialisiert und getestet.

## Nachgewiesene Ergebnisse dieser Umgebung

Geprüfter Repository-Stand: `master`, Commit `1fb057e3ead37608d10cd587428420da66c1d973`. Die Ergebnisse wurden nach Veröffentlichung in einer neu wiederhergestellten Instanz erneut geprüft.

| Prüfung | Ergebnis |
| --- | --- |
| PHP / Composer | 8.3.36 / 2.11.2 verfügbar |
| Plattformanforderungen | Erfolgreich gegen installierte Pakete geprüft |
| Frische Installation, leerer Cache | 198 gesperrte Pakete erfolgreich installiert; normale Dist-URLs, aktive TLS-Prüfung |
| GitLab-Lesezugriff | Erfolgreich |
| Push-Dry-Run | Erfolgreich, keine Remote-Refs verändert |
| Kernsuite `Karopapier` | 209 Tests, 1.255 Assertions, 2 übersprungen, keine Fehler |
| Gesamter Standard-Testlauf | 352 Tests, 3.523 Assertions, 1 Fehler, 2 übersprungen |
| Vollständiger PHP-CS-Fixer-Check | 735 Dateien geprüft, 15 mit Formatierungsbedarf |
| PHPStan | Erweiterungen geladen; 84 Code-/Baseline-Befunde |
| Repository nach Einrichtung | Sauber, keine versionierten Änderungen |

Die einzelne fehlgeschlagene Testprüfung ist `Tests\\App\\Modules\\MapGenerator\\ulli\\MapCharacteristicsTest::testGetWithKnownCharacteristics`, Zeile 246. Sie verlangt, dass eine inkrementelle Berechnung schneller als die vollständige Berechnung ist. Dieser Laufzeitvergleich scheiterte auch isoliert; nach Wiederherstellung lagen die Messwerte etwa bei 0,195 s gegenüber 0,175 s. Der Test blieb unverändert und aktiv.

Die Codestyle-Pipeline auf diesem `master` war bereits als fehlschlagend bekannt. Die Formatierungs- und PHPStan-Befunde gehören zum überprüften Repository-Stand. Sie sind kein Anlass, Checks auszuschalten oder beim Onboarding Quellcode zu verändern.

Zusätzlich wurde der damalige Issue-Branch `codex/281` in einer separaten temporären Quellkopie geprüft: 12 Beleuchtungstests mit 31 Assertions sowie der Codestyle-Check für seine vier geänderten Dateien bestanden. Die Umgebung hängt nicht von diesem Branch ab.

## Umfang und verbleibende Grenzen

- Der Standard-Testlauf berücksichtigt die im Repository konfigurierte Ausklammerung der Gruppe `long-running`. Diese Gruppe wurde nicht ausgeführt.
- Funktionale Tests der geprüften Standardsuite funktionieren mit SQLite und den vorhandenen Fixtures. Eine externe MySQL-Datenbank oder produktionsgleiche Daten wurden nicht eingerichtet.
- Frontend-Toolchain, Node-16-Pinning, Webpack-Build und Frontend-Tests sind nicht Bestandteil dieser vorbereiteten PHP-Testumgebung.
- Ein öffentlicher Webserver oder Browser-Preview wurde nicht eingerichtet.

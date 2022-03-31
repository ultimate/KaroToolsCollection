ultimate's KaroToolsCollection
==============================

## Repository information
This repo is a collection of all my tools, scripts and contributed content to www.karopapier.de

## License
All content in the project is published under the GNU General Public License (GPL) (see http://www.gnu.org/copyleft/gpl.html).
If you have further questions about using the content please don't hesitate to contact me.

## Content Overview (in alphabetical order)
* [archive](https://github.com/ultimate/KaroToolsCollection/tree/master/archive) -> contains all past builds that have been published in ready-to-use zip-files.
* [CraZZZy Crash Challenge](https://github.com/ultimate/KaroToolsCollection/tree/master/CraZZZy%20Crash%20Challenge) -> contains the data from the CraZZZy Crash Challenge (saved KaroMUSKEL series files, evaluation, etc.)
* [improvements](https://github.com/ultimate/KaroToolsCollection/tree/master/improvements) -> contains contributions and proposals to www.karopapier.de 
* [KaroAPI4J](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroAPI4J) -> contains the Karopapier API wrapper for Java
* [KaroEval](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroEval) -> contains scripts for automatic evaluation of gameseries (especially CraZZZy Crash Challenge)
* [KaroMUSKEL-Codes](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroMUSKEL-Codes) -> contains unlock codes for the KaroMUSKEL V2.x
* [KaroMUSKEL-Concept](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroMUSKEL-Concept) -> contains concept work for the KaroMUSKEL
* [KaroMUSKEL-Java](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroMUSKEL-Java) -> contains the java-based versions of the KaroMUSKEL (V1.x - V3.x) (for version history see below)
* [KaroMUSKEL-Web](https://github.com/ultimate/KaroToolsCollection/tree/master/KaroMUSKEL-Web) -> contains the html/js-based versions of the KaroMUSKEL (V0.x & V4.x) (for version history see below)
* [maps](https://github.com/ultimate/KaroToolsCollection/tree/master/maps) -> contains my maps created for www.karopapier.de
* [other](https://github.com/ultimate/KaroToolsCollection/tree/master/other) -> contains tools form other players I collected for reference

## Content Details

### KaroMUSKEL (Maschinelle-Ultimative-Spielserien-für-Karopapier-Erstellungs-Lösung)

Der KaroMUSKEL wird seit Anfang 2008 von mir (<a href="http://github.com/ultimate">ultimate</a>) entwickelt. Anfangs habe ich das Skript nur für den Eigenbedarf geschrieben, später habe ich es auf Anfrage aber auch an andere Spieler weitergegeben.
Nach der Fertigstellung der Version 1.0 Mitte 2008 hat sich lange nichts beim Skript getan. Es wurden einzig zwischendurch mal ein paar Anpassungen auf Neuerungen von <a href="http://www.karopapier.de">karopapier.de</a> eingebaut, damit das Skipt weiter Funktionsfähig bleibt. Im März 2010 habe ich mich dann mal ran gesetzt das ganze rund zu erneuern. Das Ergebnis war die Version 2.x, die sich über 5 Jahre erfolgreich hielt, bis ich den Entschluss gefasst habe nochmal ein komplettes Redesign mit Technologie-Wechsel zu machen. So entstand Version 4. Nachfolgend sind die Änderungen/Neuerungen der einzelnen Versionen (von neu nach alt) kurz aufgelistet.

#### Version 4.x
Um den Nutzerkreis zu erweitern und die Benutzung zu vereinfachen, habe ich mich entschlossen für die Version 4 auf HTML5/JavaScript zu setzen. Das bedeutete eine komplette Neuentwicklung mit vielen neuen Funktionen. Diese Version ist noch Work-in-Progress

#### Version 3.x
Version 3.0 hat zwar noch die gleiche GUI wie vorher, wurde jedoch "unter der Haube" vollständig überarbeitet. Ziel war eigentlich nur die vollständige Umstellung aller Funktionen auf die KaroAPI, aber im Zuge Umbauten, habe ich gleich noch ein paar andere Verbesserungen "unter der Haube" vorgenommen. Die grundsätzlichen Funktionen sind aber gleich geblieben. Wesentliche Änderungen sind:

* Umstellung der Server-Calls auf die KaroAPI. Das ist nicht nur besser wartbar, sondern war auch notwendig, weil durch eine Änderung im Login der KaroMUSKEL nicht mehr benutzbar war.
* Anpassung der Datenstruktur auf die KaroAPI, sowie neues internes Format für Spieleserien, welches die Erweiterung um weitere Spieleserientypen zuküftig einfacher macht.
* Neues externes Format für Spieleserien: JSON. Dieses ebnet einerseits den Weg, für eine potenzielle Web-Version 4.0 des KaroMUSKELS, falls ich mal dazu komme, wo dann die Spieleserien einfach importiert werden können. Andererseits ermöglicht das neue Format auch einfache Korrekturen an den Spieleserien, falls mal was schief läuft (wie z. B. ersetzen eines Spielers oder eines Spiels). Dazu kann nun einfach die JSON-Datei mit einem Text-Editor bearbeitet werden. Und zu guter letzt, kann man so auch die Spieleserien in eigene Skripte importieren.
* Rückwärtskompatibilität zu "alten" V2-Spieleserien. Diese können weiterhin geladen werden und werden dabei automatisch in das neue Format konvertiert. Speichern ist nur noch im neuen Format möglich.
* Wiederverwendbarkeit von gespeicherten Spieleserien. Gespeicherte Spieleserien können nun nicht nur geladen werden um noch die nicht erstellen Spiele zu erstellen. Es ist jetzt auch möglich "rückwärts" durch die Einstellungen zu navigieren und diese zu ändern. Auf diese Weise kann eine einmal erstellte Spieleserie als Vorlage für eine neue dienen. Einfach laden, "rückwärts" navigieren, einzelne Einstellungen ändern (z.B. Titel anpassen und Spieler oder Karten austauschen) und dann wieder "vorwärts" navigieren und die Spiele neu planen lassen. Ideal für immer wieder stattfindende Serien wie z. B. die KaroLiga.
* Abschaffung der Freischalt-Codes und stattdessen Auswertung des "Supercreator-Flags" vom Server. Dies hat Auswirkungen auf die Spieleerstellung, weil (bedingt durch die serverseitige Prüfung) nun nur noch die Einladbarkeit ignoriert werden kann, wenn man ein "Supercreator" ist. Um "Supercreator" zu werden wende dich bitte an Didi.
Viele weitere kleine Änderungen "unter der Haube"


#### Version 2.x
Bei dem Schritt auf Version 2 hat sich so viel verändert, dass hier einmal der (halbwegs) komplette Funktionsumfang aufgelistet wird. Was unter KaroMUSKEL 1 steht ist also nicht mehr zwangsläufig möglich...
- Erweiterte Möglichkeiten der Spieleserienerstellung nach verschiedenen Serientypen: "Einfache Spieleserie", "Liga", "KO-Meisterschaft"
- Spielerstellung unterteilt in mehrere Teilschritte: "Einstellungen", "Regeln", "Spielerauswahl", "Streckenauswahl", "Zusammenfassung"
- Speichern und Laden von Spielserien
- Optionales Beachten oder Nicht-Beachten der Einladbarkeit- Spielerauswahl je nach Spieleserientyp unterschiedlich möglich
- Meisterschaften können sowohl für Einzelspieler, als auch für ganze Teams erstellt werden
- Spierersteller kann auch an Meisterschaften teilnehmen
- Spieler können in mehreren Teams mitfahren
- Einschränkung der Streckenauswahl für Zufallskarten oder feste Streckenauswahl
- Meisterschaften erstellbar mit Zufallskarten oder Heimkarten
- Automatische Erstellung von Folgerunden bei KO-Meisterschaften
- Feste und zufällige Regelfestlegung möglich (ZZZ, TC, Richtung,...)
- überprüfung und Zusammenfassung aller Spieler vor der Erstellung
- Anpassung der Spielparameter (Name, Karte, Spieler, Regeln) für einzelne Spiele vor der Erstellung
- Erstellung einzelner Spiele steuerbar. Andere Spiele können auch später erstellt werden
- Automatischer Ausstieg aus allen oder nur einigen Spielen möglich- Erstellung der Spiele in Threads für verringerte Wartezeit
- Spielerlogin vor Spieleerstellung- Anzeige aller verfügbaren Spieler mit aktueller Spielzahl und Maximum, soweit bekannt
- Spielerkontrolle für die Beschränkung der maximalen Spielanzahl
- Minivorschau für Karten#
- uvm. was in den Updates 2.1+ dazu kam (Details in der History im Programm)

#### Version 1.x
Dies ist die erste verfügbare Version gewesen. Daher hier die komplette Liste der Funktionen.
- Grundlegende Funktionsbereitstellung für einfache Spieleserien
- Konfiguration der Spieleserie auf einer einzigen Seite
- Unterstützung einer festen Strecke oder Zufallswahl der Strecke
- Spielerauswahl in drei Kategorien möglich
- Erstellung der Spiele in Threads für verringerte Wartezeit
- Spielerlogin vor Spieleerstellung
- Anzeige aller verfügbaren Spieler mit aktueller Spielzahl und Maximum, soweit bekannt
- Minivorschau für Karten

#### Version 0.x
Bevor ich angefangen habe das ganze vernünftig in Java zu programmieren gab es noch eine ganz einfache, schnell zusammengetippte HTML/JavaScript-Version. Die wurde aber überhaupt nicht weiterverfolgt und ist nur durch Zufall mal wieder auf meiner Festplatte aufgetaucht...

<<<<<<< HEAD
KaroMUSKEL Archive
==================

<b>This is a backup branch for all published builds of the KaroMUSKEL!<br>
See other branches for specific versions!</b>

KaroMUSKEL V0.x
===============

<b>This is a backup and maintenance branch for the outdated Version 0 only!<br>
See other branches for other versions!</b>

KaroMUSKEL V1.x
===============

<b>This is a backup and maintenance branch for the outdated Version 1 only!<br>
See other branches for other versions!</b>

KaroMUSKEL V2.x
===============

<b>This is a backup and maintenance branch for the outdated Version 2 only!<br>
See other branches for other versions!</b>

KaroMUSKEL V3.x
===============

<b>This is a backup and maintenance branch for the temporary Version 3 only!<br>
See other branches for other versions!</b>


KaroMUSKEL
==========

### Maschinelle-Ultimative-Spielserien-für-Karopapier-Erstellungs-Lösung

Der KaroMUSKEL wird seit Anfang 2008 von mir (<a href="http://github.com/ultimate">ultimate</a>) entwickelt. Anfangs habe ich das Skript nur für den Eigenbedarf geschrieben, später habe ich es auf Anfrage aber auch an andere Spieler weitergegeben.
Nach der Fertigstellung der Version 1.0 Mitte 2008 hat sich lange nichts beim Skript getan. Es wurden einzig zwischendurch mal ein paar Anpassungen auf Neuerungen von <a href="http://www.karopapier.de">karopapier.de</a> eingebaut, damit das Skipt weiter Funktionsfähig bleibt. Im März 2010 habe ich mich dann mal ran gesetzt das ganze rund zu erneuern. Das Ergebnis war die Version 2.x, die sich über 5 Jahre erfolgreich hielt, bis ich den Entschluss gefasst habe nochmal ein komplettes Redesign mit Technologie-Wechsel zu machen. So entstand Version 4. Nachfolgend sind die Änderungen/Neuerungen der einzelnen Versionen (von neu nach alt) kurz aufgelistet.

##Version 4.x

###Version 4.0
Um den Nutzerkreis zu erweitern und die Benutzung zu vereinfachen, habe ich mich entschlossen für die Version 4 auf HTML5/JavaScript zu setzen. Das bedeutete eine komplette Neuentwicklung mit vielen neuen Funktionen. Der Vollständigkeit halber sind die wichtigsten Neuerungen hier kurz zusammengefasst.
- ... kommt noch ...

##Version 3.x

###Version 3.0-dev
Bevor ich mich entschlossen den Technologie-Wechsel (zurück) zu HTML5/JavaScript zu vollziehen, hatte ich schon mal begonnen, eine neues Karopapier-Backend-Anbindung in Java zu implementieren. Diese sollte (im Gegensatz zur vorherigen Version) auf der KaroAPI beruhen und so direkt mit JSON-Daten gefüttert werden, statt die für die Nutzer gedachten HTML-Seiten zu parsen. Auch wenn es hier nie zu eine benutzbaren Version des KaroMUSKELs kam, ist die <a href="http://github.com/ultimate/KaroAPI4J">KaroAPI4J</a> dennoch als Relikt erhalten geblieben.

##Version 2.x

###Version 2.3
Version 2.3 wurde für die KaroLiga (und natürlich andere Meisterschaften im Liga-Format) angepasst.
Ein spezieller Algorithmus sorgt nun dafür, dass die Anzahl der Heim- und Auswärtsspiele für jeden Spieler in Hin- und Rückrunde ausgeglichen verteilt ist.

###Version 2.2
Version 2.2 war nur eine kleine Anpassung nach Einführung der KaroAPI, damit das Skript Funktionsfähig blieb.

###Version 2.1
Version 2.1 wurde speziell für die CraZZZy Crash Challenge in Angriff genommen. Zusätzlich wurden noch ein paar Wünsche von Nutzern des Skripts berücksichtigt.
- Neuer Serientyp "Ausgewogene Spieleserie"
- Neue Strukturierung der Platzhalter
- Hinzufügen neuer Platzhalter
- Bug-Fix bei der Erstellung von mehr als 50 Spielen

###Version 2.0
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
- Minivorschau für Karten

##Version 1.x

###Version 1.1
Version 1.1 war nur eine kleine Anpassung an Änderungen in der "newgame.php", damit das Skript Funktionsfähig blieb.

###Version 1.0
Dies ist die erste verfügbare Version gewesen. Daher hier die komplette Liste der Funktionen.
- Grundlegende Funktionsbereitstellung für einfache Spieleserien
- Konfiguration der Spieleserie auf einer einzigen Seite
- Unterstützung einer festen Strecke oder Zufallswahl der Strecke
- Spielerauswahl in drei Kategorien möglich
- Erstellung der Spiele in Threads für verringerte Wartezeit
- Spielerlogin vor Spieleerstellung
- Anzeige aller verfügbaren Spieler mit aktueller Spielzahl und Maximum, soweit bekannt
- Minivorschau für Karten

##Version 0.x

###Version 0.1
Bevor ich angefangen habe das ganze vernünftig in Java zu programmieren gab es noch eine ganz einfache, schnell zusammengetippte HTML/JavaScript-Version. Die wurde aber überhaupt nicht weiterverfolgt und ist nur durch Zufall mal wieder auf meiner Festplatte aufgetaucht...

Repository Information
======================
Hinweis: Dieses Repository enthält verschiedene Versionen des KaroMUSKELs. Bitte schaut in die einzelnen Branches für mehr Details.
Note: This repository contains different versions of the KaroMUSKEL. Please refer to the respective branch for details.


License
=========
All content in the project is published under the GNU General Public License (GPL) (see http://www.gnu.org/copyleft/gpl.html).
If you have further questions about using the content please don't hesitate to contact me.

=======
KaroAPI4J
=========

A www.karopapier.de JSON-API-Wrapper for Java


License
=========

All content in the project is published under the GNU General Public License (GPL) (see http://www.gnu.org/copyleft/gpl.html).
If you have further questions about using the content please don't hesitate to contact me.
>>>>>>> refs/remotes/KaroAPI4J/master

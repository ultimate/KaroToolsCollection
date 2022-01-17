Hi Didi,

Ich habe mir mal das Chat-Scroll-Problem vorgenommen und das ganze natürlich auch in FF 3.6, IE8 und Opera 11 getestet!!!

Laut Recherche ist das nicht ohne JavaScript möglich, was ja aber kein Problem sein sollte, da Karo 2.0 ja ohnehin ohne JS nicht funktioniert...

Im Anhang findest du eine Archiv, in dem alles enthalten ist:

- ich habe deine CSS-Dateien heruntergeladen und angepasst:
	chat.css - ein paar kleine Änderungen
	karoold.css - hinfällig, jetzt theme-blau.css
	karo.css - aufgeteilt, jetzt karo.css und theme-schwarz.css
	(aus karo.css wurden die Farb- und Schriftinfos entfernt, du kannst so theoretisch beliebig viele themes machen und muss nur die entsprechende css, sowie die bilder im entsprechenden ordner anpassen...)
	
- ich habe gleich noch die Rahmen in Blau dazu gepackt...

- direkt darunter ist ein kleines bisschen JS, was bei Änderung der Fenstergröße die Größen der einzelnen Divs neu berechnet
	(nur so sieht es bei jeder Fenstergröße (wenn es nicht zu klein wird) gut aus)
	vermutlich wirst du das in eine extra (oder schon vorhandene) JS-Datei kopieren

- mit <!-- --> Kommentaren habe ich besondere  Stellen gekennzeichnet, die entweder eine kleine Änderung enthalten oder aber von dir übernommen wurden (ohne Änderung)

- du musst ggf. noch die absoluten Pfade wieder in relative Pfade ändern (bei den CSS-Angaben und beim Spiegelei-Bild)
	ich habe die im Beispiel geändert, damit du dir das ganze bevor du es umsetzt im Browser probeangucken kannst.
	in den CSS habe ich es nicht relativ gelassen, aber komischer weise will er die Hintergrundbilder bei mir trotzdem nicht anzeigen :-/ Sollte aber alles richtig sein, weil ich die Pfade ja nur übernommen habe...

Beste Grüße
ultimate/Julian

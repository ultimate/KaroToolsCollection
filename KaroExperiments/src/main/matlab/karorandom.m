clear all;
close all;
hold on;

% Streckenparameter
curveMax = 1.0;
curveMin = 0.0;

speedMax = 25;
speedMin = 1;

width = 4;
length = 30;

% Initialisierung einmalig
points = zeros(length+1,2);
points(1,:) = [0 0];

triesForPoint = 20;
validRoute = false;

while ~validRoute
    % Am Anfang ist die Route immer gueltig
    validRoute = true;
    
    % Initialisierung fuer jeden Durchlauf
    xMin = 0;
    xMax = 0;
    yMin = 0;
    yMax = 0;
    angle = 0;
    vecLength = 0;
    
    % Finde alle Punkte entlang der Route
    for i = 2:length+1
        disp(['Suche Punkt #' num2str(i) '...']);
        oldangle = angle;
        
        % Mehrere Versuche pro Punkt, falls Regelverletzung
        for j = 1:triesForPoint
            % Berechnung des Winkels (Alter Winkel + Drehung)
            angle = oldangle + (rand()*2-1)*(curveMax-curveMin)*pi+curveMin*pi;
                        
            % Berechnung der Abschnittslaenge
            vecLength = rand()*(speedMax-speedMin)+speedMin;

            % Berechnung des Vektors
            vecX = cos(angle)*vecLength;
            vecY = sin(angle)*vecLength;

            % Berechnung des neuen Punktes (Letzter Punkt + Vektor)
            point = points(i-1,:) + [vecX vecY];

            % Gueltigkeitspruefungen
            valid = true;
            % Pruefung 1
            % Ist der Punkt zu nah an einer anderen Linie 
            % (ausser der aktuellen)
            % Minimaler Abstand ist Breite der Strecke + delta
            for h = 1:i-2
                if (distanceToLine(point', points(h,:)', points(h+1,:)') < (width + 3) )
                    valid = false;
                    break;
                end
            end
            % Pruefung 2
            % Ist ein anderer Punkt zu nah an dieser Linie 
            % (ausser den aktuellen Punkten)
            % Minimaler Abstand ist Breite der Strecke + delta
            for h = 1:i-2
                if (distanceToLine(points(h,:)', point', points(i-1,:)') < (width + 3) )
                    valid = false;
                    break;
                end
            end
            % Pruefung 3
            % Schneidet man eine Linie?
            for h = 1:i-2     
                if cuts(point', points(i-1,:)', points(h,:)', points(h+1,:)')
                    valid = false;
                end;
            end
            % Pruefung fehlgeschlagen
            if valid
                break;
            end
            
            % Zu viele Versuche sind fehlgeschlagen
            % Komplette Neuberechnung der Route
            if (j == triesForPoint)
                validRoute = false;
            end
        end

        % Komplette Neuberechnung der Route
        if ~validRoute
            break;
        end
        
        % Punkt ist gueltig und wird Route hinzugefuegt
        points(i,:) = point;
        disp(['Punkt gefunden #' num2str(i) '!']);
        
        % Bereichsgrenzen updaten
        if point(1) < xMin
            xMin = point(1);
        end
        if point(1) > xMax
            xMax = point(1);
        end
        if point(2) < yMin
            yMin = point(2);
        end
        if point(2) > yMax
            yMax = point(2);
        end
    end
end

points

% Bereichsgrenzen runden und um Breite erweitern
xMin = floor(xMin)-(width/2+3);
xMax = ceil(xMax)+(width/2+3);
yMin = floor(yMin)-(width/2+3);
yMax = ceil(yMax)+(width/2+3);

% Bereichsgroesse
dx = xMax-xMin+1;
dy = yMax-yMin+1;

% Bereich initialisieren
map = zeros(dx, dy);

% Alle Punkte des Bereichs pruefen
for x = 1:dx
    for y = 1:dy
        % Aktueller Punkt
        p = [x+xMin y+yMin];
        % Minimalen Abstand zur Strecke pruefen
        minDist = max(dx,dy);
        % Abstand zu Liniensegmenten        
        for i = 1:length
            currDist = distanceToLine(p', points(i,:)', points(i+1,:)');
            if (currDist < minDist)
                minDist = currDist;
            end
        end
        
        if (minDist <= width/2)
            map(x,y) = 'O';
            plot(p(1), p(2), 'rsquare');
        else            
            if (minDist <= (width/2+2))
                map(x,y) = 'Y';
            else
                map(x,y) = 'X';
            end
        end
    end
end

disp(char(map));

plot(points(:,1), points(:,2));


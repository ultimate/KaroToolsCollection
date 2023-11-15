function dist = distanceToLine(p, p1, p2)

    angleLine = atan2(p2(2)-p1(2), p2(1)-p1(1));
    angleP1 = atan2(p(2)-p1(2), p(1)-p1(1));
    angleP2 = atan2(p(2)-p2(2), p(1)-p2(1));
    
    angle1 = angleP1 - angleLine;
    angle1 = mod(angle1, 2*pi);
    angle2 = angleP2 - angleLine;
    angle2 = mod(angle2, 2*pi);
    
    distanceP1 = distance(p, p1);
    distanceP2 = distance(p, p2);
    
    if ((angle1 >= pi*3/2) || (angle1 <= pi/2))
        if ((angle2 >= pi/2) && (angle2 <= pi*3/2))
            dist = abs(sin(angle1)*distanceP1);
        else            
            dist = distanceP2;
        end
    else
        dist = distanceP1;
    end
end
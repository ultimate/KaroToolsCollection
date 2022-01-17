var mapStringNormal = "Karte ";
var mapStringNight  = "Nachtkarte ";
var playersString   = " Spieler";
var playerAlreadyInListString = "Spieler ist bereits in Liste!";
var invalidPlayerEntryString = "Eintrag für Spieler ist keine Zahl!\n      - oder -\nName ist nicht in Spieler-Liste!\nBitte Liste aktualisieren, falls nötig!";
var noPlayerFoundString = "Keinen Spieler nach RegExp gefunden!\nBitte Liste aktualisieren, falls nötig!";
var tooMuchPlayersString = "Die Karte reicht nicht aus für diese Zahl Spieler!";
var tooMuchMaxPlayersString = "Es gibt keine Karte mit so vielen Spielern,\n oder die gewählte Karte ist nicht für so viele Spieler geeignet!";

var mainURL = "http://www.karopapier.de/";
var newgameURL = "newgame.php";
var name 	= "name";
var space 	= "%20";
var map 	= "mapid";
var player	= "teilnehmer";
var cps	= "checkers=on";
var zzz	= "zzz";
var crash	= "crashallowed";

function Player(id, name)
{
	this.id = id;
  this.name = name;
}

function Map(id, pictureURL, name, maxPlayers, night)
{
	this.id = id;
  this.name = name;
  this.maxPlayers = maxPlayers;
  this.pictureURL = pictureURL;
  this.night = night;
}

function initMaps()
{
	for(var i = 0; i < mapsNormal.length; i++)
  {
  	if(mapsNormal[i].maxPlayers > 0)
    {
	    var newMapString = mapStringNormal + mapsNormal[i].id + ": " + mapsNormal[i].name + " (" + mapsNormal[i].maxPlayers + playersString + ")";
	    var newMap = new Option(newMapString, mapsNormal[i].id, false, false);
	    document.newgame.map.options[document.newgame.map.options.length] = newMap;
    }
  }
	for(var i = 0; i < mapsNight.length; i++)
  {
  	if(mapsNight[i].maxPlayers > 0)
    {
	    var newMapString = mapStringNight + mapsNight[i].id + ": " + mapsNight[i].name + " (" + mapsNight[i].maxPlayers + playersString + ")";
	    var newMap = new Option(newMapString, mapsNight[i].id, false, false);
	    document.newgame.map.options[document.newgame.map.options.length] = newMap;
    }
  }
  changeMapPreview();
}

function changeMapPreview()
{
	var pictureURL;
	if(document.newgame.mapRandom.checked == true)
  	pictureURL = "mapPreviews/0.png";
  else
	{
		var selectedOption = document.newgame.map.options[document.newgame.map.selectedIndex];
    var index = selectedOption.value;
    if(index < 1000)
     	pictureURL = mapsNormal[index-1].pictureURL;
    else
  		pictureURL = mapsNight[index-1000].pictureURL;
	}
	document.newgame.mapPreview.src = pictureURL;
	checkPlayersMaxValue(false);
  checkPlayersInList(false);
}

function initPlayers()
{
	for(var i = 0; i < specialPlayers.length; i++)
  {
  	var newPlayer = new Option(specialPlayers[i].id + " (" + specialPlayers[i].name + ")", specialPlayers[i].id, false, true);
		addPlayer(newPlayer, false);
  }
}

function clearPlayers()
{
	while(document.newgame.player.options.length > 1)
  {
  	document.newgame.player.options[document.newgame.player.options.length-1] = null;
  }
}

function deletePlayer()
{
	if(document.newgame.player.options.selectedIndex <= 0)
  	return;
	document.newgame.player.options[document.newgame.player.options.selectedIndex] = null;
}

function addNewPlayer()
{
	var newPlayerNumberInput = document.newgame.newPlayerField.value;
  var newPlayerIndex = new Array(1);
  	newPlayerIndex[0] = -1;
  if(newPlayerNumberInput == "")
  	return;
  if(!(newPlayerNumberInput < 0) && !(newPlayerNumberInput >= 0))
  //if((typeof newPlayerNumberInput) != "number")
  {
  	//not a number
	  for(var i = 0; i < allPlayers.length; i++)
	  {
	    if(allPlayers[i].name == newPlayerNumberInput)
	    {
	      newPlayerIndex[0] = i;
	      break;
	    }
	  }
    //regexp
    if(newPlayerNumberInput.charAt(0) == '/')
    {
    	if( (newPlayerNumberInput.charAt(newPlayerNumberInput.length-1) == '/') ||
      		(newPlayerNumberInput.substring(newPlayerNumberInput.length-2,newPlayerNumberInput.length) == "/i") ||
      		(newPlayerNumberInput.substring(newPlayerNumberInput.length-2,newPlayerNumberInput.length) == "/g") ||
      		(newPlayerNumberInput.substring(newPlayerNumberInput.length-3,newPlayerNumberInput.length) == "/gi") ||
      		(newPlayerNumberInput.substring(newPlayerNumberInput.length-3,newPlayerNumberInput.length) == "/ig") )
      {
        var regexp;
        var regExpString = newPlayerNumberInput.substring(newPlayerNumberInput.indexOf("/")+1,newPlayerNumberInput.lastIndexOf("/"));
    		if(newPlayerNumberInput.charAt(newPlayerNumberInput.length-1) == '/')
          regexp = new RegExp(regExpString);
      	else
        {
        	var regExpParam = newPlayerNumberInput.substring(newPlayerNumberInput.lastIndexOf("/")+1,newPlayerNumberInput.length);
          regexp = new RegExp(regExpString, regExpParam);
        }
	      for(var i = 0; i < allPlayers.length; i++)
	      {
        	if(regexp.test(allPlayers[i].name))
	        {
          	if(newPlayerIndex[0] == -1)
            {
	          	newPlayerIndex[0] = i;
            }
            else
            {
	          	newPlayerIndex[newPlayerIndex.length] = i;
            }
	        }
	      }
      }
      if(newPlayerIndex[0] < 0)
	    {
	      alert(noPlayerFoundString);
	      return;
	    }
    }
    else if(newPlayerIndex[0] < 0)
  	{
    	alert(invalidPlayerEntryString);
      return;
    }
  }
  else
  {
	  for(var i = 0; i < allPlayers.length; i++)
	  {
	    if(allPlayers[i].id == newPlayerNumberInput)
	    {
      	newPlayerIndex[0] = i;
	      break;
	    }
	  }
  }
  for(var i = 0; i < newPlayerIndex.length; i++)
  {
	 	var newPlayerText = allPlayers[newPlayerIndex[i]].id + " (" + allPlayers[newPlayerIndex[i]].name + ")";
		var newPlayer = new Option(newPlayerText,allPlayers[newPlayerIndex[i]].id,false,true);
		if(!addPlayer(newPlayer, true))
    	return;
  }
}

function addPlayer(newPlayer, showDialog)
{
  for(var i = 0; i < document.newgame.player.options.length; i++)
  {
  	if(document.newgame.player.options[i].text == newPlayer.text)
    {
    	if(showDialog == true)
    		alert(playerAlreadyInListString);
      document.newgame.newPlayerField.value = "";
      return true;
    }
  }
  if(!checkPlayersInList(true))
  	return false;
	document.newgame.player.options[document.newgame.player.options.length] = newPlayer;
	document.newgame.newPlayerField.value = "";
  return true;
}

function checkPlayersInList(wantToAdd)
{
	var add = -1;
  if(wantToAdd)
  	add = 0;
	if( document.newgame.player.options.length + add > getMaxPlayersForSelectedMap() )
  {
  	alert(tooMuchPlayersString);
    return false;
  }
  return true;
}

function checkPlayersMaxValue(alertB)
{
	var playersMaxValue = document.newgame.playersMaxValueField.value;
  var playersMinValue = document.newgame.playersMinValueField.value;
  var playersMax = getMaxPlayersForSelectedMap();
  var playersMaxAbs = getMaxPlayers(document.newgame.randomMapRadio[getSelectedIndex(document.newgame.randomMapRadio)].value);
  if( playersMaxValue > playersMax)
	{
  	if(alertB)
  		alert(tooMuchMaxPlayersString + "\nMaximum für diese Karte ist " + playersMax + "!\nAllgemeines Maximum ist " + playersMaxAbs + "!");
    document.newgame.playersMaxValueField.value = playersMax;
  }
  if(playersMaxValue < playersMinValue)
    document.newgame.playersMaxValueField.value = playersMinValue;
}

function randomMapStatusChanged()
{
	if(document.newgame.mapRandom.checked == true)
  {
  	document.newgame.map.disabled = true;
  	document.newgame.randomMapRadio[0].disabled = false;
  	document.newgame.randomMapRadio[1].disabled = false;
  	document.newgame.randomMapRadio[2].disabled = false;
  }
  else
  {
  	document.newgame.map.disabled = false;
  	document.newgame.randomMapRadio[0].disabled = true;
  	document.newgame.randomMapRadio[1].disabled = true;
  	document.newgame.randomMapRadio[2].disabled = true;
  }
  changeMapPreview();
  checkPlayersMaxValue(false);
}

function addPlayersStatusChanged()
{
	if(document.newgame.playersMax.checked == true)
  {
  	document.newgame.playersMaxRadio[0].disabled = false;
  	document.newgame.playersMaxRadio[1].disabled = false;
  	document.newgame.playersMaxValueField.disabled = false;
  	document.newgame.playersMin.disabled = false;
  	document.newgame.playersMinValueField.disabled = false;
  }
  else
  {
  	document.newgame.playersMaxRadio[0].disabled = true;
  	document.newgame.playersMaxRadio[1].disabled = true;
  	document.newgame.playersMaxValueField.disabled = true;
  	document.newgame.playersMin.disabled = true;
  	document.newgame.playersMinValueField.disabled = true;
  }
}

function createGames()
{
	var doit = confirm("Erstelle Spiele?");
  if(!doit)
  	return;
  var maxNOG = document.newgame.gameNumber.value;
  maxNOG++;
  maxNOG--;
  var nogStart = document.newgame.numberStart.value;
  nogStart++;
  nogStart--;
  var w1;
  for(var noG = nogStart; noG < nogStart + maxNOG; noG++)
	{
	  //page to call
	  var url = mainURL + newgameURL + "?";
	  //name of the game(s)
    var nameString = createNameString(noG);
	  url += name + "=" + nameString;
    var mapString = getMap();
    if(mapString == "Error")
    {
    	alert(tooMuchMaxPlayersString);
      return;
    }
    url += "&" + map + "=" + mapString;
    var players = getPlayers(mapString);
    for(var i = 0; i < players.length; i++)
    {
    	url += "&" + player + "[" + i + "]=" + players[i];
    }
    var settings = readSettings();
    url += settings;
    //w1.name = nameString;
	  //w1.location.href = url;
    w1 = window.open(url,nameString,"width=400,height=300,left=100,top=100");
    self.focus();
    var ready = sleep(1000);
    w1.close();
    self.focus(); 
  }
  alert("Fertig!");
}

function createNameString(noG)
{
  var nameString = document.newgame.gameName.value;
  var number = noG;  //TOCHANGE
  //where to insert number
  var numberPos = document.newgame.numberPosition.value;
  var pos;
  if(numberPos == "ende")
  {
    pos = new Array(1);
    pos[0] = nameString.length;
  }
  else if(numberPos == "keine")
  {
    pos = new Array(0);
  }
  else
  {
    var numberOfPos = 1;
    for(var i = 0; i < numberPos.length; i++)
    {
      if( (numberPos.charAt(i) == ',') || (numberPos.charAt(i) == ';') )
        numberOfPos++;
    }
    var currPos = 0;
    pos = new Array(numberOfPos);
    for(var i = 0; i < pos.length; i++)
    {
      pos[i] = "";
    }
    for(var i = 0; i < numberPos.length; i++)
    {
      if( (numberPos.charAt(i) == ',') || (numberPos.charAt(i) == ';') )
        currPos++;
      else if (numberPos.charAt(i) != ' ')
        pos[currPos] = pos[currPos] + "" + numberPos.charAt(i);
    }
  }
  for(var i = 0; i < pos.length; i++)
  {
    pos[i]++;
    pos[i]--;
  }
  for(var i = 0; i < pos.length; i++)
  {
    var currPos = pos[i];
    var temp = toMinDigitsString(number, document.newgame.numberDigits.value);
    nameString = nameString.substring(0, currPos) + temp + nameString.substring(currPos, nameString.length);
    for(var j = i+1; j < pos.length; j++)
    {
      pos[j] += temp.length;
    }
  }
  nameString = toCorrectURLString(nameString);
	return nameString;
}

function toCorrectURLString(stringWithSpaces)
{
  while(stringWithSpaces.indexOf(" ") != -1)
  {
    stringWithSpaces = stringWithSpaces.substring(0, stringWithSpaces.indexOf(" ")) + space + stringWithSpaces.substring(stringWithSpaces.indexOf(" ") + 1, stringWithSpaces.length);
  }
  return stringWithSpaces;
}

function toMinDigitsString(number, digits)
{
	if(digits <= 0)
  	return number + "";
	var ret = number + "";
	while( ret.substring(ret.indexOf("-")+1, ret.length).length < digits )
  	ret = ret.substr(0,ret.indexOf("-")+1) + "0" + ret.substring(ret.indexOf("-")+1, ret.length);
  return ret;
}

function getMap()
{
	if(document.newgame.mapRandom.checked == false)
		return document.newgame.map.options[document.newgame.map.selectedIndex].value;
	var maxPlayers = getMaxPlayers(document.newgame.randomMapRadio[getSelectedIndex(document.newgame.randomMapRadio)].value);
  var requestedPlayers = 0;
	if(document.newgame.playersMax.checked == true)
  {
  	if(document.newgame.playersMaxRadio[getSelectedIndex(document.newgame.playersMaxRadio)].value == "maxNumber")
    	requestedPlayers = document.newgame.playersMaxValueField.value;
    else
    	requestedPlayers = document.newgame.player.options.length-1;
    if(document.newgame.playersMin.checked == true)
    {
    	if(document.newgame.playersMinValueField.value > requestedPlayers)
      	requestedPlayers = document.newgame.playersMinValueField.value;
    }
  }
  else
  {
  	requestedPlayers = document.newgame.player.options.length-1;
  }
  if(requestedPlayers > maxPlayers)
		return "Error";
  var id = 25;
  var whichMaps = document.newgame.randomMapRadio[getSelectedIndex(document.newgame.randomMapRadio)].value;
  do
  {
  	if(whichMaps == "all")
  		id = Math.floor(Math.random()*(mapsNight.length + mapsNormal.length - 1));
    else if(whichMaps == "normal")
  		id = Math.floor(Math.random()*(mapsNormal.length - 1));
		else
      id = Math.floor(Math.random()*(mapsNight.length - 1) + mapsNormal.length);
  	if(id < mapsNormal.length)
    {
    	if(mapsNormal[id].maxPlayers >= requestedPlayers)
      {
      	id = mapsNormal[id].id;
				break;
      }
    }
    else
    {
    	if(mapsNight[id-mapsNormal.length].maxPlayers >= requestedPlayers)
			{
      	id = mapsNight[id-mapsNormal.length].id;
				break;
      }
    }
  }while(true);
  return id;
}

function getPlayers(map)
{
	var noP = 0;
  if(document.newgame.playersMax.checked == true)
  {
  	if(document.newgame.playersMaxRadio[getSelectedIndex(document.newgame.playersMaxRadio)].value == "maxNumber")
    	noP = document.newgame.playersMaxValueField.value;
    else
  	{
    	for(var i = 0; i < mapsNormal.length; i++)
      {
      	if(mapsNormal[i].id == map)
        {
        	noP = mapsNormal[i].maxPlayers;
          break;
        }
      }
    	for(var i = 0; i < mapsNight.length; i++)
      {
      	if(mapsNight[i].id == map)
        {
        	noP = mapsNight[i].maxPlayers;
          break;
        }
      }
    }
  }
  else
  {
  	noP = document.newgame.player.options.length-1;
  }
  var players = new Array(noP);
  for(var i = 0; i < document.newgame.player.options.length-1; i++)
  {
  	players[i] = document.newgame.player.options[i+1].value;
  }
  for(var i = document.newgame.player.options.length-1; i < players.length; i++)
  {
  	do
    {
  		players[i] = allPlayers[Math.floor(Math.random()*allPlayers.length)].id; //Zufallsspieler
    }
    while(containsDuplicates(players, i));
  }
  return players;
}

function containsDuplicates(players, k)
{
	for(var i1 = 0; i1 <= k; i1++)
  {
  	for(var i2 = i1+1; i2 <= k; i2++)
    {
    	if(players[i1] == players[i2])
      	return true;
    }
  }
  return false;
}

function readSettings()
{
	var settings = "";
  if(document.newgame.checkpoints.checked == true)
  	settings += "&" + cps;
  settings += "&" + zzz + "=" + document.newgame.customZZZvalue.value;
  settings += "&" + crash + "=" + document.newgame.crashAllowed.options[document.newgame.crashAllowed.selectedIndex].value;
  return settings;
}

function getMaxPlayers(whichMaps)
{
  var maxPlayers = 0;
  if(whichMaps == "all" || whichMaps == "normal")
  {
	  for(var i = 0; i < mapsNormal.length; i++)
	  {
	    if(mapsNormal[i].maxPlayers > maxPlayers)
	      maxPlayers = mapsNormal[i].maxPlayers;
	  }
  }
  if(whichMaps == "all" || whichMaps == "night")
  {
	  for(var i = 0; i < mapsNight.length; i++)
	  {
	    if(mapsNight[i].maxPlayers > maxPlayers)
	      maxPlayers = mapsNight[i].maxPlayers;
	  }
  }
  return maxPlayers;
}

function getMaxPlayersForSelectedMap()
{
	if(document.newgame.mapRandom.checked == true)
  	return getMaxPlayers(document.newgame.randomMapRadio[getSelectedIndex(document.newgame.randomMapRadio)].value);
  else
	{
		var selectedOption = document.newgame.map.options[document.newgame.map.selectedIndex];
    var index = selectedOption.value;
    if(index < 1000)
     	return mapsNormal[index-1].maxPlayers;
    else
  		return mapsNight[index-1000].maxPlayers;
	}
}

function getSelectedIndex(radioGroup)
{
	for(var i = 0; i < radioGroup.length; i++)
  {
  	if(radioGroup[i].checked == true)
    	return i;
  }
}

function sleep(millis)
{
	var startTime = Date.parse(new Date());
  while(Date.parse(new Date())-startTime < millis)
  {
  }
  return true;
}
/**
 * KaroMUSKEL - Copyright (c) 2015 ultimate
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version
 * 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MECHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Plublic License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
game = {
	"name":"Mit der API erstellt",
	"players":[1411],
	"map":105,
	"options":{
		"startdirection":"classic",
		"withCheckpoints":true,
		"zzz":4,
		"crashallowed":"forbidden"
		}
	}
*/
 
var KaroMUSKEL = (function() {	
	// constants
	var VERSION = "4.0-dev";
	var KARO_URL = "http://www.karopapier.de/api/";
	var LOADER_VALUE_ID = "loader_value";
	var LOADER_TEXT_ID = "loader_text";
	var DATA_USER_LIST = "userList";
	var DATA_MAP_LIST = "mapList";
	// private variables
	var wrapper;
	var loader;
	var ui;
	var versionInfo;
	var local = (document.location.href.indexOf("file://") != -1);
	var initialized = false;
	// karopapier data
	var userList;
	var mapList;
	var currentUser;
	
	// private functions
	var create = function(game) {
		var gameS = JSON.stringify(game);
		var request = new XMLHttpRequest();
		request.headers = { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" };
		request.open(HTTP.POST, KARO_URL + "game/add.json", true);
		request.withCredentials = true;
		request.onreadystatechange = function() {
			if(request.readyState == 4)
			{
				if((request.status >= 200) && (request.status < 300))
				{
					console.log("game created");
					console.log(request);
				}
				else
				{
					console.error("game creation failed");
					console.error(request);
				}
			}
		};
		request.send(gameS);
	};
	var initLoader = function() {	
		loader = document.createElement("div");
		loader.classList.add("loader");
		var loaderTitle = document.createElement("div");
			loaderTitle.classList.add("title");
			loaderTitle.innerHTML = "Lade...";	
		var loaderBar = document.createElement("div");
			loaderBar.classList.add("bar");			
			var loaderBackground = document.createElement("div");
				loaderBackground.classList.add("background");
				loaderBackground.innerHTML = "KaroMUSKEL";
			var loaderValue = document.createElement("div");
				loaderValue.id = LOADER_VALUE_ID;
				loaderValue.classList.add("value");	
			loaderBar.appendChild(loaderBackground);
			loaderBar.appendChild(loaderValue);
		var loaderText = document.createElement("div");
			loaderText.id = LOADER_TEXT_ID;
			loaderText.classList.add("text");
			loaderText.innerHTML = "0%";			
		loader.appendChild(loaderTitle);
		loader.appendChild(loaderBar);
		loader.appendChild(loaderText);
		wrapper.appendChild(loader);
	};
	var initVersionInfo = function() {
		versionInfo = document.createElement("div");
		versionInfo.classList.add("version");
		versionInfo.innerHTML = VERSION;
		wrapper.appendChild(versionInfo);
	};
	var initUI = function() {
		ui = document.createElement("div");
		ui.classList.add("ui");
		
		// update UI
		loader.style.display = "none";
		wrapper.appendChild(ui);
	};
	
	// get KaroMUSKELWrapper
	{
		wrapper = document.getElementById("KaroMUSKELWrapper");
		if(wrapper == null)
		{
			wrapper = document.body;
			console.warn("KaroMUSKEL: KaroMUSKELWrapper not found - using body instead!");
		}
	}
	// create loader bar
	initLoader();
	initVersionInfo();
	// load dependencies
	if(DependencyManager)
	{
		console.log("KaroMUSKEL: DependencyManager found - loading dependencies...");
		// init DependencyManager
		DependencyManager.instantLoad = false;
		// register syncnapsis-core-utils
		DependencyManager.register("Arrays", 		"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Arrays.js", 				false, true);
		DependencyManager.register("Elements", 		"http://cdn.rawgit.com/ultimate/syncnapsis/13a0536b4fd0c9490f89cdfe9373d38c6fde5336/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Elements.js", 			false, true);
		DependencyManager.register("Events", 		"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Events.js", 				false, true);
		DependencyManager.register("Strings", 		"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Strings.js", 			false, true);
		// register syncnapsis-universe components
		DependencyManager.register("ComboSelect", 	"http://cdn.rawgit.com/ultimate/syncnapsis/b59bcaf1e8673bb5f3fc1ba9521ad7a9f3622657/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/comboselect.js", 	false, true);
		DependencyManager.register("Select", 		"http://cdn.rawgit.com/ultimate/syncnapsis/d0687d2ebc385fbf1fc757cb4ffc3e65299920b5/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/select.js", 		false, true);
		DependencyManager.register("Tabs", 			"http://cdn.rawgit.com/ultimate/syncnapsis/11b13002162abb8672f126068f0dd3bb4c8d4740/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/tabs.js", 			false, true);
		// register JSON-data
		DependencyManager.register(DATA_USER_LIST, 	KARO_URL + "user/list.json", true, true);
		DependencyManager.register(DATA_MAP_LIST, 	KARO_URL + "map/list.json?nocode=true", true, true);
		// get the data indexes
		var userListIndex = DependencyManager.indexOf(DATA_USER_LIST);		
		var mapListIndex = DependencyManager.indexOf(DATA_MAP_LIST);			
		if(local)
		{
			// fake JSON-data since AJAX won't work on file system (with just some reduced data)
			DependencyManager.scriptContents[userListIndex] = "[{\"id\": 1,\"login\": \"Didi\",\"color\": \"ffffff\",\"lastVisit\": 0,\"signup\": 4877,\"dran\": 15,\"activeGames\": 140,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 154,\"sound\": 1,\"soundfile\": \"/mp3/quiek.mp3\",\"size\": 11,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/bb493dfa04160c4c284b8740a5b23557?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1411,\"login\": \"ultimate\",\"color\": \"10FF01\",\"lastVisit\": 0,\"signup\": 3011,\"dran\": 11,\"activeGames\": 360,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 0,\"sound\": 0,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 10,\"border\": 1,\"desperate\": true,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/20d5212b8d0fcb0b04576f1db9b25839?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1413,\"login\": \"tepetz\",\"color\": \"990033\",\"lastVisit\": 0,\"signup\": 3009,\"dran\": 0,\"activeGames\": 22,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 20,\"sound\": 1,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 20,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/7d10a6052eb3ebf7fbe2665b5dcbd00c?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1823,\"login\": \"A300\",\"color\": \"330000\",\"lastVisit\": 1,\"signup\": 2617,\"dran\": 13,\"activeGames\": 91,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 500,\"sound\": 1,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 12,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/5913830e0b83c43281bcb484fcc590de?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1830,\"login\": \"sir tobi\",\"color\": \"FFFFFF\",\"lastVisit\": 0,\"signup\": 2602,\"dran\": 0,\"activeGames\": 80,\"acceptsDayGames\": true,\"acceptsNightGames\": false,\"maxGames\": 250,\"sound\": 10,\"soundfile\": \"/mp3/fiep.mp3\",\"size\": 12,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/6328af4498c6fa580cbb489c3077fdc7?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"}]";
			DependencyManager.scriptAdded[userListIndex] = true;		
			DependencyManager.scriptContents[mapListIndex] = "[{\"id\": 1,\"name\": \"Die Erste\",\"author\": \"Didi\",\"cols\": 60,\"rows\": 25,\"rating\": 4.11111,\"players\": 5,\"cps\": [\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\"]},{\"id\": 2,\"name\": \"Die Zweite\",\"author\": \"Didi\",\"cols\": 60,\"rows\": 25,\"rating\": 4.03226,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 3,\"name\": \"Die Dritte\",\"author\": \"Didi\",\"cols\": 80,\"rows\": 33,\"rating\": 3.83333,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 4,\"name\": \"\",\"author\": \"(unknown)\",\"cols\": 80,\"rows\": 31,\"rating\": 3.30769,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 5,\"name\": \"\",\"author\": \"(unknown)\",\"cols\": 80,\"rows\": 35,\"rating\": 3.83333,\"players\": 5,\"cps\": [\"1\",\"2\",\"3\"]}]";
			DependencyManager.scriptAdded[mapListIndex] = true;
			
			DependencyManager.scriptsLoaded += 2;
		}
		// complete registration
		DependencyManager.onLoadingProgressed(DependencyManager.defaultOnLoadingProgressed(LOADER_VALUE_ID, LOADER_TEXT_ID));
		DependencyManager.onLoadingFinished(function() {
			// init
			console.log("KaroMUSKEL: all dependencies loaded!");
			console.log("KaroMUSKEL: initializing...");
			// get karopapier data from JSON
			eval(DATA_USER_LIST + " = " + DependencyManager.scriptContents[userListIndex]);
			eval(DATA_MAP_LIST  + " = " + DependencyManager.scriptContents[mapListIndex]);
			// lookup current user
			console.log("KaroMUSKEL: you are logged in as '" + ME.login + "'");
			for(var i = 0; i < userList.length; i++)
			{
				if(userList[i].login == ME.login)
				{
					currentUser = userList[i];
					break;
				}
			}
			if(currentUser == null)
			{
				console.error("KaroMUSKEL: current user not found in user list!");
			}
			initUI();
			initialized = true;
		});
		DependencyManager.registrationDone();
	}
	else
	{
		console.error("KaroMUSKEL: DependencyManager required - please include syncnapsis Request.js");
	}
	
	// export public functions
	return {
		// getters
		isInitialized: 	function() { 	return initialized; 	},
		getUserList : 	function() { 	return userList; 		},
		getMapList : 	function() { 	return mapList; 		},
		getCurrentUser: function() { 	return currentUser;		},
		// export private functions
		createGame:		create,
	};
})();
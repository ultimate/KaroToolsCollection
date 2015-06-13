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

document.addEventListener("DOMContentLoaded", function(){	
	// check availability of KaroMUSKELWrapper
	var wrapper = document.getElementById("KaroMUSKELWrapper");
	if(wrapper == null)
	{
		wrapper = document.body;
		console.warn("KaroMUSKELWrapper not found - using body instead!");
	}
	if(DependencyManager)
	{
		console.log("DependencyManager found - initializing...");
		// create load-bar
		var progress = document.createElement("div");
		progress.id = "progress";		
		progress.classList.add("progress");
		var progressTitle = document.createElement("div");
		progressTitle.classList.add("title");	
		progressTitle.innerHTML = "Lade...";	
		var progressBackground = document.createElement("div");
		progressBackground.classList.add("background");	
		progressBackground.innerHTML = "KaroMUSKEL";
		var progressValue = document.createElement("div");
		progressValue.id = "progress_value";
		progressValue.classList.add("value");	
		progressValue.innerHTML = "KaroMUSKEL";	
		var progressText = document.createElement("div");
		progressText.id = "progress_text";
		progressText.classList.add("text");	
		progressText.innerHTML = "0 %";			
		progress.appendChild(progressTitle);
		progress.appendChild(progressBackground);
		progress.appendChild(progressValue);
		progress.appendChild(progressText);
		wrapper.appendChild(progress);
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
		// complete registration
		DependencyManager.onLoadingProgressed(DependencyManager.defaultOnLoadingProgressed(progressValue.id, progressText.id));
		/*
		DependencyManager.onLoadingProgressed(function() {
			var progress = DependencyManager.getProgress();
		});
		*/
		DependencyManager.onLoadingFinished(function() {
			// init
			console.log("init");
		});
		DependencyManager.registrationDone();
	}
	else
	{
		console.error("DependencyManager and KaroMUSKELWrapper required execution!");
		console.error("please include syncnapsis Request.js");
	}
}, false);
/*

var KaroMUSKEL = (function(debug, local) {
	// private variables
	var karoURL = "http://www.karopapier.de/api/";
	var userList;
	var userListSet = false;
	var mapList;
	var mapListSet = false;
	var currentUser;
	var currentUserSet = false;
	var documentLoaded = false;
		
	// private functions
	var log = function(msg) {
		if(typeof(msg) == "string")
		{
			console.log("KaroMUSKEL: " + msg);
		}
		else
		{
			console.log("KaroMUSKEL:");
			console.log(msg);
		}
	};
	var addScript = function(url, callback, async) {
		var s = document.createElement("script");
		s.type = "text/javascript";
		if(callback)
		{
			if(url.indexOf("?") == -1)
				url += "?callback=" + callback;
			else
				url += "&callback=" + callback;
		}
		s.src = url;
		document.head.appendChild(s);
	};
	var create = function(game) {
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
		
		var gameS = JSON.stringify(game);
		var request = new XMLHttpRequest();
		request.headers = { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" };
		request.open("POST", karoURL + "game/add.json", true);
		request.withCredentials = true;
		request.onreadystatechange = function() {
			if(request.readyState == 4)
			{
				if((request.status >= 200) && (request.status < 300))
				{
					log("game created");
					log(request);
				}
				else
				{
					log("game creation failed");
					log(request);
				}
			}
		};
		request.send(gameS);
	};
	var init = function() {
		log("all content loaded - initing...");
	};
	var initWhenReady = function() {
		if(userListSet && mapListSet && currentUserSet && documentLoaded)
			init();
	};
	
	// general initialization
	Events.addEventListener("DOMContentLoaded", function(){	
		// add data script nodes
		addScript(karoURL + "user/list.json", "KaroMUSKEL.setUserList");
		addScript(karoURL + "map/list.json?nocode=true", "KaroMUSKEL.setMapList");
		addScript(karoURL + "user/check.json", "KaroMUSKEL.setCurrentUser");
	
		documentLoaded = true;
		initWhenReady();		
	}, false);
	
	// DEBUGGING
	if(local)
	{
		var base = document.location.href;
		base = base.substring(0, base.lastIndexOf("/") - "KaroMUSKEL".length);
		base += "api/";
		karoURL = base;
	}
	if(debug)
	{	
		log("Karo-API-URL used is '" + karoURL + "'");
	}
	
	return {
		setUserList : function(data) {
			log("setting user list");
			userList = data;
			userListSet = true;
			if(currentUser != null)
			{
				// overwrite the current user with the matching user from the list
				// so there is only one object for the user
				for(var i = 0; i < userList.length; i++)
				{
					if(userList[i].id == currentUser.id)
					{
						currentUser = userList[i]
						currentUserSet = true;
						break;
					}
				}
			}
			initWhenReady();
		},
		getUserList : function() {
			return userList;
		},
		setMapList : function(data) {
			log("setting map list");
			mapList = data;
			mapListSet = true;
			initWhenReady();
		},
		getMapList : function() {
			return mapList;
		},
		setCurrentUser : function(data) {
			log("setting current user");
			if(userList != null)
			{
				// set the current user with the matching user from the list
				// so there is only one object for the user
				for(var i = 0; i < userList.length; i++)
				{
					if(userList[i].id == data.id)
					{
						currentUser = userList[i]
						currentUserSet = true;
						break;
					}
				}
			}
			else
			{
				// set the current user temporarily until the list is loaded
				currentUser = data;
			}
			initWhenReady();
		},
		getCurrentUser : function() {
			return currentUser;
		},
		createGame : create,
	};
})(true, false);
*/
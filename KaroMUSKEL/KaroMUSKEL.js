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
	var addScript = function(url, callback) {
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
		*/
		var gameS = JSON.stringify(game);
		var request = new XMLHttpRequest();
		request.headers = { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" };
		request.open("POST", karoURL + "game/add.json", true);
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
		log("KaroMUSKEL: all content loaded - initing...");
	};
	var initWhenReady = function() {
		if(userListSet && mapListSet && currentUserSet && documentLoaded)
			init();
	};
	
	// general initialization
	Events.addEventListener("DOMContentLoaded", function(){	
		// add script nodes
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
		log("KaroMUSKEL: Karo-API-URL used is '" + karoURL + "'");
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

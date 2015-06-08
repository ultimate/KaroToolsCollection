var KaroMUSKEL = (function(enableDebug) {
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
	var init = function() {
		console.log("KaroMUSKEL: all content loaded - initing...");
	};
	var initWhenReady = function() {
		if(userListSet && mapListSet && currentUserSet && documentLoaded)
			init();
	};
	
	
	// general initialization
	Events.addEventListener("DOMContentLoaded", function(){	
		documentLoaded = true;
		initWhenReady();		
	}, false);
	
	// DEBUGGING
	if(enableDebug)
	{		
		var base = document.location.href;
		base = base.substring(0, base.lastIndexOf("/") + 1);
		base += "api/";
		karoURL = base;
		console.log("KaroMUSKEL: Karo-API-URL used is '" + karoURL + "'");
	}
	
	return {
		setUserList : function(data) {
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
			mapList = data;
			mapListSet = true;
			initWhenReady();
		},
		getMapList : function() {
			return mapList;
		},
		setCurrentUser : function(data) {
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
	};
})(true);

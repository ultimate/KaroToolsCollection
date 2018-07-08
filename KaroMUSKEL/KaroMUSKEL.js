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

var ME; // init variable in order to be able to check if for undefined
 
var KaroMUSKEL = (function() {	
	// constants
	const VERSION = "4.0-dev";
	const KARO_URL = "http://www.karopapier.de/api/";
	const LOADER_VALUE_ID = "loader_value";
	const LOADER_TEXT_ID = "loader_text";
	const MENU_ID = "menu";
	const PLACEHOLDERS_ID = "placeholders";
	const TITLE_ID = "title";
	const PREVIEW_ID = "preview";
	const TAB_BAR_ID = "tab_bar";
	const TAB_CONTENT_ID = "tab_content";
	const DATA_USER_LIST = "userList";
	const DATA_MAP_LIST = "mapList";
	const VERSION_HISTORY = "versionHistory";
	const STYLE_SHEET = "styleSheet";
	const UTILS_SCRIPT = "utils";
	const COMPONENTFACTORY_SCRIPT = "componentFactory";
	// private variables
	var wrapper;
	var loader;
	var ui;
	var tabs;
	var tabIndex = 0;
	var versionInfo;
    var versionHistory;
	var local = (document.location.href.indexOf("file://") != -1);
	var initialized = false;
	var gameSeries = null;
	// karopapier data
	var userList;
	var mapList;
	var currentUser;
	
	// private functions
	var create = function(game) {
		var gameS = "game=" + JSON.stringify(game);
		var request = new XMLHttpRequest();
		request.open(HTTP.POST, KARO_URL + "game/add.json", true);
		request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
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
        // include style here, since CSS is not yet loaded
        loader.style.marginLeft     = "auto";
        loader.style.marginRight    = "auto";
        loader.style.marginTop      = "100px";
	    loader.style.marginBottom   = "100px";
	    loader.style.position       = "relative";
	    loader.style.left           = "0px";
	    loader.style.right          = "0px";
	    loader.style.top            = "0px";
	    loader.style.bottom         = "0px";
	    loader.style.width          = "400px";
	    loader.style.height         = "20px";
        // add children by html, since this makes it easier to include style
        loader.innerHTML = "<div style='position: absolute; left: 0px; height: 100%; width: 100px;'>Lade...</div>\
                            <div style='position: absolute;	left: 100px; height: 100%; width: 200px; text-align: center;'>\
                                <div style='position: absolute; left: 0px; height: 100%; text-align: center; border: 1px solid rgb(51,51,153); width: 100%;'>KaroMUSKEL</div>\
                                <div style='position: absolute; left: 0px; height: 100%; text-align: center; border: 1px solid rgba(0,0,0,0); background-color: rgb(51,51,153);	opacity: 0.5;' id='" + LOADER_VALUE_ID + "'></div>\
                            </div>\
                            <div style='position: absolute; right: 0px; height: 100%;	width: 100px; text-align: right;' id='" + LOADER_TEXT_ID + "'>0%</div>";
		wrapper.appendChild(loader);
	};
	var initVersionInfo = function() {
		versionInfo = document.createElement("div");
		versionInfo.innerHTML = VERSION;
        // include style here, since CSS is not yet loaded
        versionInfo.style.position  = "absolute";
        versionInfo.style.right     = "20px";
        versionInfo.style.bottom    = "-22px";
        versionInfo.style.textAlign = "right";
        versionInfo.style.width     = "100px";
		wrapper.appendChild(versionInfo);
	};
	var initUI = function() {
		ui = document.createElement("div");
		ui.classList.add("ui");
		var tabBar = document.createElement("div");
			tabBar.id = TAB_BAR_ID;
			/*
			tabBar.innerHTML = "<a id='bar_1' class='selected'><div class='frame'>&Uuml;bersicht</div></a>\
								<a id='bar_2'><div class='frame'>Spieler / Teams</div></a>\
								<a id='bar_3'><div class='frame'>Spieltage / Runden</div></a>\
								<a id='bar_4'><div class='frame'>Zusammenfassung</div></a>\
								<a id='bar_5'><div class='frame'>Auswertung</div></a>";
								*/
			tabBar.appendChild(createADiv("bar_1", "&Uuml;bersicht", 		"selected", "frame", null));
			tabBar.appendChild(createADiv("bar_2", "Spieler / Teams", 		null, 		"frame", null));
			tabBar.appendChild(createADiv("bar_3", "Spieltage / Runden", 	null, 		"frame", null));
			tabBar.appendChild(createADiv("bar_4", "Zusammenfassung", 		null, 		"frame", null));
			tabBar.appendChild(createADiv("bar_5", "Auswertung", 			null, 		"frame", null));
		var tabContent = document.createElement("div");
			tabContent.id = TAB_CONTENT_ID;
			//tabContent.classList.add("frame");
			tabContent.innerHTML = "<div>\
										<div id='overview' class='container selected'><!-- Tab 0 - filled in updateUI(gameSeries) --></div>\
										<div id='players_teams' class='container'><!-- Tab 1 - filled in updateUI(gameSeries) --></div>\
										<div id='gamedays_rounds' class='container'><!-- Tab 2 - filled in updateUI(gameSeries) --></div>\
										<div id='summary' class='container'><!-- Tab 3 - filled in updateUI(gameSeries) --></div>\
										<div id='evaluation' class='container'><!-- Tab 4 - filled in updateUI(gameSeries) --></div>\
										<div id='new' class='container'><h1>Neue Spieleserie erstellen</h1></div>\
										<div id='info' class='container'></div>\
									</div>";
        var mainMenu = document.createElement("div");
			//mainMenu.id = MENU_ID;
            mainMenu.classList.add("menu");
            mainMenu.classList.add("tabbar_vertical");
            mainMenu.appendChild(createADiv("menu_1", "Neu", null, "frame", function() {
					//TODO add confirm dialog
					
					// show type selection
					tabs.select(5);
					
					//gameSeries = new KaroMUSKEL.GameSeries();
					//updateUI(gameSeries);
					updateUI(null);
				}));
            mainMenu.appendChild(createADiv("menu_2", "&Ouml;ffnen", 	null, "frame"));
            mainMenu.appendChild(createADiv("menu_3", "Speichern", 	null, "frame"));
            mainMenu.appendChild(createADiv("menu_4", "Info", 			null, "frame", function() {  
                tabs.select(6);
                if(tabContent.firstChild.lastChild.children.length <= 1)
                {
                    var data = JSON.stringify({
                        text: versionHistory,
                        mode: "markdown",
                        context: "ultimate/KaroMUSKEL"
                        });
                    console.log(data);
                    AJAX.sendRequestUrlEncoded("http://api.github.com/markdown", data, HTTP.POST, function(request) {
                        console.log(request.responseText);
                        tabContent.firstChild.lastChild.firstChild.innerHTML = request.responseText;
						tabContent.firstChild.lastChild.firstChild.appendChild(createCloseButton());
                    });                   
                }
            }));
		var typeSelection = document.createElement("div");
			typeSelection.classList.add("type_selection");
			typeSelection.classList.add("tabbar_horizontal");
			typeSelection.classList.add("centered");
			var typeSelected = function(type) { return function() { gameSeries = new KaroMUSKEL.GameSeries(type, document.getElementById("useTeams").checked); updateUI(gameSeries); tabs.select(0); }; };
			typeSelection.appendChild(createADiv("type_simple", 	"<div class='centered double_row'>Einfache Spieleserie</div>", 		null, "frame", typeSelected(KaroMUSKEL.SERIES_TYPES[0]) ));
			typeSelection.appendChild(createADiv("type_balanced", 	"<div class='centered double_row'>Ausgewogene Spieleserie</div>", 	null, "frame", typeSelected(KaroMUSKEL.SERIES_TYPES[1]) ));
			typeSelection.appendChild(createADiv("type_league", 	"<div class='centered single_row'>Liga</div>", 						null, "frame", typeSelected(KaroMUSKEL.SERIES_TYPES[2]) ));
			typeSelection.appendChild(createADiv("type_ko", 		"<div class='centered double_row'>KO- Meisterschaft</div>", 		null, "frame", typeSelected(KaroMUSKEL.SERIES_TYPES[3]) ));
		var useTeams = document.createElement("div");
			useTeams.classList.add("use_teams");
			useTeams.classList.add("centered");
			useTeams.innerHTML = "<input id='useTeams' type='checkbox'/><span>Teams verwenden?</span>";
        ui.appendChild(mainMenu);
		ui.appendChild(tabBar);
		ui.appendChild(tabContent);
		
		// update UI
		loader.style.display = "none";
		wrapper.appendChild(ui);
		// append sub-content
		document.getElementById("new").appendChild(typeSelection);
		document.getElementById("new").appendChild(useTeams);
		document.getElementById("new").appendChild(createCloseButton());
		
		// init initelligent elements
		// tabs = new Tabs(barId, barMode, containerId, containerMode, selectedOverwriteWidth, selectedOverwriteHeight)
		tabs = new Tabs(TAB_BAR_ID, TABS_HORIZONTAL, TAB_CONTENT_ID, TABS_HORIZONTAL);
		tabs.onSelect = function(index) {
			if(index < 5)
				tabIndex = index;
		};
	};
	var updateUI = function(gameSeries) {		
		if(gameSeries == null)
		{
			// reset the view
			var message = "Nutze das Men&uuml; links um mit der Spielerstellung zu beginnen...";
			document.getElementById("overview").innerHTML = message;
			document.getElementById("players_teams").innerHTML = message;
			document.getElementById("gamedays_rounds").innerHTML = message;
			document.getElementById("summary").innerHTML = message;
			document.getElementById("evaluation").innerHTML = "coming soon...";
		}
		else
		{
			// TODO placeholder_values so anpassen, dass diese den echten Spiel-Properties entsprechen,
			// so dass darüber generisch auf die Eigenschaften zugegriffen werden kann.
			var placeholders = "<ul id='" + PLACEHOLDERS_ID + "' class='size200'>\
									<li class='tree_item frame'><b>+</b> Platzhalter einf&uuml;gen\
										<ul class='size200'>\
											<li class='tree_item frame'>Allgemeine Einstellungen\
												<ul class='size100'>\
													<li class='tree_item frame' placeholder_value='${spiel.zzz}'>ZZZ</li>\
													<li class='tree_item frame' placeholder_value='${spiel.tc}'>TC</li>\
													<li class='tree_item frame' placeholder_value='${spiel.cps}'>CPs</li>\
												</ul>\
											</li>\
											<li class='tree_item frame'>Spieler / Teams\
												<ul class='size200'>\
													<li class='tree_item frame' placeholder_value='${spiel.spieler2}'>Spieler (ohne Spielersteller)</li>\
													<li class='tree_item frame' placeholder_value='${spiel.spieler}'>Spieler (mit Spielersteller)</li>\
													<li class='tree_item frame' placeholder_value='${spiel.teams.heim}'>Heim-Spieler/Team</li>\
													<li class='tree_item frame' placeholder_value='${spiel.teams.gast}'>Gast-SpielerTeam</li>\
												</ul>\
											</li>\
											<li class='tree_item frame'>Spieltage / Runden\
												<ul class='size250'>\
													<li class='tree_item frame' placeholder_value='${serie.spielnummer.total}'>Laufende Spielnummer (total)</li>\
													<li class='tree_item frame' placeholder_value='${serie.spielnummer.spieltag}'>Laufende Spielnummer (Spieltag)</li>\
													<li class='tree_item frame' placeholder_value='${serie.spielanzahl.total}'>Anzahl Spiele (total)</li>\
													<li class='tree_item frame' placeholder_value='${serie.spielanzahl.spieltag}'>Anzahl Spiele (Spieltag)</li>\
												</ul>\
											</li>\
										</ul>\
									</li>\
								</ul>";
			var teamOptions = "<span class='label empty'>&nbsp;</span><input id='playersInMultipleteams' type='checkbox'/>Spieler in mehreren Teams?<br/>\
							   <span class='label empty'>&nbsp;</span><input id='shuffleTeams' type='checkbox'/>Teams mischen?<br/>\
							   <span class='label empty'>&nbsp;</span><input id='autoTeamNames' type='checkbox'/>Automatische Teamnamen?<br/>\
							   <span class='label empty'>&nbsp;</span><input id='creatorNoTeam' type='checkbox'/>Spielersteller Neutral?<br/>";
			document.getElementById("overview").innerHTML = "<span class='label'>Titel</span><input id='" + TITLE_ID + "' style='width: 500px;' type='text' placeholder='Bitte gebe der Spielserie einen Namen...'/><br/>\
															 <span class='label empty'>&nbsp;</span><input id='" + PREVIEW_ID + "' type='text' disabled='disabled' style='width: 500px;' value='preview...'/><br/>\
															 " + placeholders + "<br/>\
															 <div class='spacer'></div>\
															 <div class='spacer'></div>\
															 <span class='label'>Typ</span><span id='type'></span><br/>\
															 <div class='spacer'></div>\
															 <span class='label'>Team-Optionen</span><input type='checkbox' disabled='disabled' " + (gameSeries.useTeams ? "checked='checked'" : "") + "/>Teams verwenden?<br/>\
															 " + (gameSeries.useTeams ? teamOptions : "") + "\
															 <div class='spacer'></div>\
															 <span class='label'>Sonstige-Optionen</span><input id='checkInvitable' type='checkbox' checked='checked'/>Einladbarkeit beachten?<br/>\
															 <span class='label empty'>&nbsp;</span><input id='creatorLeaveMatches' type='checkbox' disabled='disabled'/>Spielersteller aussteigen?<br/>\
															 ";
			document.getElementById("players_teams").innerHTML  = ""; // TODO
															 
			// update form
			document.getElementById("type").innerHTML = "\"" + gameSeries.type + "\"";
			
			// add event handlers
			document.getElementById(TITLE_ID).onkeypress = function(event) {
					var title = document.getElementById(TITLE_ID).value;
					// TODO replace placeholders
					document.getElementById(PREVIEW_ID).value = title;
				};			
			document.getElementById(TITLE_ID).onkeydown = document.getElementById(TITLE_ID).onkeypress;
			document.getElementById(TITLE_ID).onkeyup = document.getElementById(TITLE_ID).onkeypress;
			document.getElementById(PLACEHOLDERS_ID).onclick = function(event) {
					var placeholder = event.target.getAttribute("placeholder_value");
					if(placeholder != null)
					{
						console.log("inserting placeholder '" + placeholder + "' at caret");
						insertAtCaret(TITLE_ID, placeholder); 
						document.getElementById(TITLE_ID).onkeypress();
					}					
				};
			
			// TODO auf der Auswahl-Seite für die Art der Spieleserie eine Check-box "Teams verwenden" ergänzen und dieses flag gleich bei der Erstellung übergeben
		}
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
		DependencyManager.register("Arrays", 				"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Arrays.js", 				false, true);
		DependencyManager.register("Elements", 				"http://cdn.rawgit.com/ultimate/syncnapsis/13a0536b4fd0c9490f89cdfe9373d38c6fde5336/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Elements.js", 			false, true);
		DependencyManager.register("Events", 				"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Events.js", 				false, true);
		DependencyManager.register("Strings", 				"http://cdn.rawgit.com/ultimate/syncnapsis/71656b61de31b1c0fb0b1d2680ea32bec743fef8/syncnapsis-core/syncnapsis-core-utils/src/main/webapp/scripts/util/Strings.js", 			false, true);
		// register syncnapsis-universe components
		DependencyManager.register("ComboSelect", 			"http://cdn.rawgit.com/ultimate/syncnapsis/b59bcaf1e8673bb5f3fc1ba9521ad7a9f3622657/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/comboselect.js", 	false, true);
		DependencyManager.register("Select", 				"http://cdn.rawgit.com/ultimate/syncnapsis/d0687d2ebc385fbf1fc757cb4ffc3e65299920b5/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/select.js", 		false, true);
		DependencyManager.register("Tabs", 					"http://cdn.rawgit.com/ultimate/syncnapsis/11b13002162abb8672f126068f0dd3bb4c8d4740/syncnapsis-universe/syncnapsis-universe-conquest/src/main/webapp/scripts/tabs.js", 			false, true);
		// register JSON-data
		DependencyManager.register(DATA_USER_LIST, 			KARO_URL + "user/list.json", true, true);
		DependencyManager.register(DATA_MAP_LIST, 			KARO_URL + "map/list.json?nocode=true", true, true);
        // register other dependencies
		DependencyManager.register(COMPONENTFACTORY_SCRIPT, "http://rawgit.com/ultimate/KaroMUSKEL/V4.x/KaroMUSKEL/ComponentFactory.js", false, true);
		DependencyManager.register(UTILS_SCRIPT, 			"http://rawgit.com/ultimate/KaroMUSKEL/V4.x/KaroMUSKEL/Utils.js", false, true);
		DependencyManager.register(VERSION_HISTORY, 		"http://rawgit.com/ultimate/KaroMUSKEL/V4.x/README.md", true, true);
		DependencyManager.register(STYLE_SHEET,     		"http://rawgit.com/ultimate/KaroMUSKEL/V4.x/KaroMUSKEL/KaroMUSKEL.css", true, true);
		// get the data indexes
		let userListIndex       = DependencyManager.indexOf(DATA_USER_LIST);		
		let mapListIndex        = DependencyManager.indexOf(DATA_MAP_LIST);		
		let versionHistoryIndex = DependencyManager.indexOf(VERSION_HISTORY);	
		let styleSheetIndex     = DependencyManager.indexOf(STYLE_SHEET);		
		let utilsIndex     		= DependencyManager.indexOf(UTILS_SCRIPT);		
		let compFacIndex	    = DependencyManager.indexOf(COMPONENTFACTORY_SCRIPT);			
		if(local)
		{
			// fake JSON-data since AJAX won't work on file system (with just some reduced data)
			DependencyManager.scriptContents[userListIndex] = "[{\"id\": 1,\"login\": \"Didi\",\"color\": \"ffffff\",\"lastVisit\": 0,\"signup\": 4877,\"dran\": 15,\"activeGames\": 140,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 154,\"sound\": 1,\"soundfile\": \"/mp3/quiek.mp3\",\"size\": 11,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/bb493dfa04160c4c284b8740a5b23557?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1411,\"login\": \"ultimate\",\"color\": \"10FF01\",\"lastVisit\": 0,\"signup\": 3011,\"dran\": 11,\"activeGames\": 360,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 0,\"sound\": 0,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 10,\"border\": 1,\"desperate\": true,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/20d5212b8d0fcb0b04576f1db9b25839?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1413,\"login\": \"tepetz\",\"color\": \"990033\",\"lastVisit\": 0,\"signup\": 3009,\"dran\": 0,\"activeGames\": 22,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 20,\"sound\": 1,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 20,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/7d10a6052eb3ebf7fbe2665b5dcbd00c?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1823,\"login\": \"A300\",\"color\": \"330000\",\"lastVisit\": 1,\"signup\": 2617,\"dran\": 13,\"activeGames\": 91,\"acceptsDayGames\": true,\"acceptsNightGames\": true,\"maxGames\": 500,\"sound\": 1,\"soundfile\": \"/mp3/brumm.mp3\",\"size\": 12,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/5913830e0b83c43281bcb484fcc590de?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"},{\"id\": 1830,\"login\": \"sir tobi\",\"color\": \"FFFFFF\",\"lastVisit\": 0,\"signup\": 2602,\"dran\": 0,\"activeGames\": 80,\"acceptsDayGames\": true,\"acceptsNightGames\": false,\"maxGames\": 250,\"sound\": 10,\"soundfile\": \"/mp3/fiep.mp3\",\"size\": 12,\"border\": 1,\"desperate\": false,\"birthdayToday\": false,\"karodayToday\": false,\"gravatar\": \"http://www.gravatar.com/avatar/6328af4498c6fa580cbb489c3077fdc7?default=http%3A%2F%2Fwww.karopapier.de%2Ffavicon.gif&size=40\"}]";
			DependencyManager.scriptAdded[userListIndex] = true;		
			DependencyManager.scriptContents[mapListIndex] = "[{\"id\": 1,\"name\": \"Die Erste\",\"author\": \"Didi\",\"cols\": 60,\"rows\": 25,\"rating\": 4.11111,\"players\": 5,\"cps\": [\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\"]},{\"id\": 2,\"name\": \"Die Zweite\",\"author\": \"Didi\",\"cols\": 60,\"rows\": 25,\"rating\": 4.03226,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 3,\"name\": \"Die Dritte\",\"author\": \"Didi\",\"cols\": 80,\"rows\": 33,\"rating\": 3.83333,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 4,\"name\": \"\",\"author\": \"(unknown)\",\"cols\": 80,\"rows\": 31,\"rating\": 3.30769,\"players\": 5,\"cps\": [\"1\",\"2\"]},{\"id\": 5,\"name\": \"\",\"author\": \"(unknown)\",\"cols\": 80,\"rows\": 35,\"rating\": 3.83333,\"players\": 5,\"cps\": [\"1\",\"2\",\"3\"]}]";
			DependencyManager.scriptAdded[mapListIndex] = true;
            // fake style sheet & Utils etc., so we are working with the local version --> content is added regular via KaroMUSKEL.html instead
			DependencyManager.scriptContents[styleSheetIndex] = "/*empty*/";
			DependencyManager.scriptAdded[styleSheetIndex] = true;
			DependencyManager.scriptContents[utilsIndex] = "/*empty*/";
			DependencyManager.scriptAdded[utilsIndex] = true;
			DependencyManager.scriptContents[compFacIndex] = "/*empty*/";
			DependencyManager.scriptAdded[compFacIndex] = true;
			
			// Note: DependencyManager will attempt to load the scripts anyway and may produce cross-origin-request errors, but scripts won't be added twice
            
			DependencyManager.scriptsLoaded += 5;
		}
		// complete registration
		DependencyManager.onLoadingProgressed(DependencyManager.defaultOnLoadingProgressed(LOADER_VALUE_ID, LOADER_TEXT_ID));
		DependencyManager.onLoadingFinished(function() {
			// init
			console.log("KaroMUSKEL: all dependencies loaded!");
			console.log("KaroMUSKEL: initializing...");
			// prepare other loaded from DependencyManager
			versionHistory = DependencyManager.scriptContents[versionHistoryIndex];
            var style = document.createElement("style");
            style.type = "text/css";
            style.innerHTML = DependencyManager.scriptContents[styleSheetIndex];
            document.head.appendChild(style);
			// get karopapier data from JSON
			eval(DATA_USER_LIST + " = " + DependencyManager.scriptContents[userListIndex]);
			eval(DATA_MAP_LIST  + " = " + DependencyManager.scriptContents[mapListIndex]);
			// lookup current user
			if(ME)
			{
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
				updateUI(null);
				initialized = true;
			}
			else
			{				
				console.log("KaroMUSKEL: you are not logged in!");
				// TODO show not logged in message
				let elem = document.getElementById(LOADER_TEXT_ID).parentElement;
				let div = document.createElement("div");
				div.appendChild(document.createTextNode("Du bist nicht eingeloggt! Bitte einloggen und neu laden!"));
				// not sure if css is loaded properly
				div.style.color = "red";
				div.style.position = "relative";
				div.style.top = "2em";
				div.style.textAlign = "center";
				elem.appendChild(div);
			}
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
		getGameSeries:	function() { 	return gameSeries;		},
		// export private functions
		createGame:		create,
        // Karopapier constants
        DIRECTIONS: ["classic", "free", "formula1", "random"],
        CRASHS: ["allowed", "forbidden", "free", "random"],
		// KaroMUSKEL constants
		SERIES_TYPES: ["simple", "balanced", "league", "KO" ],
		// internal data types
		GameSeries: function(type, useTeams) {
			this.type = type;
			this.useTeams = useTeams;
		},
        // Karopapier data types     
        Game: function(name, players, map, options) {
            this.name = name;
            this.players = players;
            this.map = map;
            this.options = options;
        },
        Options: function(startdirection, withCheckpoints, zzz, crashallowed) {
            this.startdirection = startdirection;
            this.withCheckpoints = withCheckpoints;
            this.zzz = zzz;
            this.crashallowed = crashallowed;
        }
	};
})();
